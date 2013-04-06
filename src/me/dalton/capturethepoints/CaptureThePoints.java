package me.dalton.capturethepoints;

import me.dalton.capturethepoints.listeners.CaptureThePointsPlayerListener;
import me.dalton.capturethepoints.listeners.CaptureThePointsBlockListener;
import me.dalton.capturethepoints.listeners.CaptureThePointsEntityListener;
import me.dalton.capturethepoints.util.ArenaUtils;
import me.dalton.capturethepoints.util.ConfigTools;
import me.dalton.capturethepoints.util.MoneyUtils;
import me.dalton.capturethepoints.util.InvManagement;
import me.dalton.capturethepoints.util.Permissions;
import me.dalton.capturethepoints.beans.ArenaBoundaries;
import me.dalton.capturethepoints.beans.Arena;
import me.dalton.capturethepoints.beans.Items;
import me.dalton.capturethepoints.beans.PlayerData;
import me.dalton.capturethepoints.beans.Rewards;
import me.dalton.capturethepoints.beans.Spawn;
import me.dalton.capturethepoints.beans.Team;
import me.dalton.capturethepoints.commands.*;
import me.dalton.capturethepoints.enums.ArenaLeaveReason;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.io.IOException;

import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.permission.Permission;

import org.bukkit.ChatColor;
import org.bukkit.DyeColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.configuration.file.FileConfiguration;

public class CaptureThePoints extends JavaPlugin {
	private Permission permission = null;
    private Economy economyHandler = null;
    private boolean UsePermissions;

    /** "plugins/CaptureThePoints" */
    private String mainDir;

    /** "plugins/CaptureThePoints/CaptureSettings.yml" */
    //public static final File myFile = new File(mainDir + File.separator + "CaptureSettings.yml");
    private File globalConfigFile = null;

    private PluginManager pluginManager = null;
    
    /** The master control over most of the arena related stuff. */
    private ArenaMaster arenaMaster = null;

    /** List of commands accepted by CTP */
    private List<CTPCommand> commands = new ArrayList<CTPCommand>(); // Kj

    private final CaptureThePointsBlockListener blockListener = new CaptureThePointsBlockListener(this);//TODO: Make these all start off null and then onEnable set them.
    private final CaptureThePointsEntityListener entityListener = new CaptureThePointsEntityListener(this);
    private final CaptureThePointsPlayerListener playerListener = new CaptureThePointsPlayerListener(this);
    private ArenaUtils aUtil = new ArenaUtils(this);
    private ConfigTools cTools = new ConfigTools(this);
    private InvManagement invMan = new InvManagement(this);
    private MoneyUtils mUtil = new MoneyUtils(this);
    private Permissions perm = new Permissions(this);
    private Util util = new Util(this);
    private ArenaRestore arenaRestore = new ArenaRestore(this);
    private MysqlConnector mysqlConnector = new MysqlConnector(this);
    
    //General scheduler ids
    private int lobbyActivity = 0;

    private final HashMap<String, ItemStack[]> Inventories = new HashMap<String, ItemStack[]>();

    private HashMap<String, ItemStack[]> armor = new HashMap<String, ItemStack[]>();
    
    /** The global config options for CTP. */
    private ConfigOptions globalConfigOptions = new ConfigOptions();

    /** The roles/classes stored by CTP. (HashMap: Role's name, and the Items it contains) 
     * @see Items */
    private HashMap<String, List<Items>> roles = new HashMap<String, List<Items>>();

    /** The list of Healing Items stored by CTP. */
    private List<HealingItems> healingItems = new LinkedList<HealingItems>();

    /** The list of Rewards stored by CTP. */
    private Rewards rewards = new Rewards();

    public int arenaRestoreTimesRestored = 0;
    public int arenaRestoreMaxRestoreTimes = 0;
    public int arenaRestoreTimesRestoredSec = 0;   //For second time
    public int arenaRestoreMaxRestoreTimesSec = 0;
    
    /** If we're loading the plugin for the first time, defaults to false. */
    private boolean firstTime = false;

    /** Name of the player who needs teleporting. */
    public String playerNameForTeleport = ""; // Block destroy - teleport protection

    @Override
    public void onEnable () {
    	pluginManager = getServer().getPluginManager();
    	if(!pluginManager.isPluginEnabled("Vault")) {
    		getLogger().severe("Vault is required in order to use this plugin.");
    		getLogger().severe("dev.bukkit.org/server-mods/vault/");
			pluginManager.disablePlugin(this);
			return;
		}
    	
    	mainDir = this.getDataFolder().toString();
    	if(mainDir.isEmpty()) firstTime = true;
    	globalConfigFile = new File(mainDir + File.separator + "CaptureSettings.yml");
    	
    	arenaMaster = new ArenaMaster(this);
    	
        enableCTP(false);
    }

    /**
     * Loads everything from the configs and registers the events (if not reloading).
     * 
     * @param reloading Are we reloading the plugin?
     */
    public void enableCTP (boolean reloading) {
        if (!reloading) {
            setupPermissions();
            setupEconomy();

            // REGISTER EVENTS-----------------------------------------------------------------------------------
            pluginManager.registerEvents(blockListener, this);
            pluginManager.registerEvents(entityListener, this);
            pluginManager.registerEvents(playerListener, this);

            populateCommands();
        }
        
        loadConfigFiles(reloading);

        // Checks for mysql
        if(this.globalConfigOptions.enableHardArenaRestore)
            mysqlConnector.checkMysqlData();

        //Kj: LobbyActivity timer.
        lobbyActivity = this.getServer().getScheduler().scheduleSyncRepeatingTask(this, new Runnable() {
            public void run () {
            	if(arenaMaster.getArenas().isEmpty()) return; //if we don't have any arenas, return and do nothing
            	
                if (globalConfigOptions.lobbyKickTime <= 0) {
                    return;
                }

                for(Arena a : arenaMaster.getArenas()) {
	                for (String player : a.getPlayersData().keySet()) {
	                    PlayerData data = a.getPlayerData(player);
	                    Player p = getServer().getPlayer(player);
	                    if (data.inLobby() && !data.isReady()) {
	                        // Kj -- Time inactivity warning.
	                        if (((System.currentTimeMillis() - data.getLobbyJoinTime()) >= ((globalConfigOptions.lobbyKickTime * 1000) / 2)) && !data.hasBeenWarned()) {
	                            sendMessage(p, ChatColor.LIGHT_PURPLE + "Please choose your class and ready up, else you will be kicked from the lobby!");
	                            data.isWarned(true);
	                        }
	
	                        // Kj -- Time inactive in the lobby is greater than the lobbyKickTime specified in config (in ms)
	                        if ((System.currentTimeMillis() - data.getLobbyJoinTime() >= (globalConfigOptions.lobbyKickTime * 1000)) && data.hasBeenWarned()) {
	                            data.setInLobby(false);
	                            data.setInArena(false);
	                            data.isWarned(false);
	                            a.leaveGame(p, ArenaLeaveReason.SERVER_STOP);
	                            sendMessage(p, ChatColor.LIGHT_PURPLE + "You have been kicked from the lobby for not being ready on time.");
	                        }
	                    }
	                }
                }
            }

        }, 200L, 200L); // 10 sec
        
        logInfo("Loaded " + arenaMaster.getArenas().size() + " arena" + ((arenaMaster.getArenas().size() > 1 || arenaMaster.getArenas().size() == 0) ? "s!" : "!"));
    }

    @Override
    public void onDisable () {
        if (lobbyActivity != 0) {
            getServer().getScheduler().cancelTask(lobbyActivity);
            lobbyActivity = 0;
        }
        
        arenaRestore.cancelArenaRestoreSchedules();
        clearConfig();
        pluginManager = null;
        permission = null;
        commands.clear();
    }
    
    public void clearConfig() {
    	for(Arena a : getArenaMaster().getArenas())
    		a.endGame(false, false);//Don't give rewards, the game ended prematurely
    	
        healingItems.clear();
        rewards = null;
        roles.clear();
    }

    @Override
    public boolean onCommand (CommandSender sender, Command command, String label, String[] args) {
        if (!command.getName().equalsIgnoreCase("ctp")) {
            return true;
        }

        if (commands == null || commands.isEmpty()) { // Really weird bug that rarely occurs. Could call it a sanity check.
            populateCommands();
        }

        List<String> parameters = new ArrayList<String>();
        parameters.add(command.getName());
        parameters.addAll(Arrays.asList(args));

        if (parameters.size() == 1) {
            sendHelp(sender);
            return true;
        }

        for (CTPCommand each : commands) {
            for (String aString : each.aliases) {
                if (aString.equalsIgnoreCase(parameters.get(1))) { // Search the command aliases for the first argument given. If found, execute command.
                    each.execute(sender, parameters);
                    return true;
                }
            }
        }
        
        // Comand not found

        getLogger().info(sender.getName() + " issued an unknown CTP command. It has " + parameters.size() + " Parameters: " + parameters + ". Displaying help to them.");
        sendHelp(sender);
        return true;
    }

    /** Send the CTP help to this sender */
    public void sendHelp(CommandSender sender) {
        HelpCommand helpCommand = new HelpCommand(this);
        helpCommand.execute(sender, Arrays.asList("ctp"));
    }

    /** Attempt to balance the teams.
     * 
     * @param a The arena in which we're balancing the teams.
     * @param loop Times this has recursed (prevents overruns).
     * @param balanceThreshold The config value for balance threshold.
     * @return Whether the teams are balanced.
     */
    public boolean balanceTeams(Arena a, int loop, int balanceThreshold) {//TODO
        if (loop > 5) {
        	getLogger().warning("balanceTeams hit over 5 recursions. Aborting.");
            return false;
        }
        
        Team lowestTeam = null; // Team with the lower number of players
        int lowestmembercount = -1;
        Team highestTeam = null; // Team with the higher number of players
        int highestmembercount = -1;
        
        int difference = 0;

        for (Team aTeam : a.getTeams()) {
            if (lowestmembercount == -1) {
                lowestmembercount = aTeam.getMemberCount();
                lowestTeam = aTeam;
                highestmembercount = aTeam.getMemberCount();
                highestTeam = aTeam;
                continue;
            } else {
                if (aTeam.getMemberCount() != lowestmembercount || aTeam.getMemberCount() != highestmembercount) {
                    if (aTeam.getMemberCount() < lowestmembercount) {
                        lowestmembercount = aTeam.getMemberCount(); // Reassign new low
                        lowestTeam = aTeam;
                    } else if (aTeam.getMemberCount() > highestmembercount) {
                        highestmembercount = aTeam.getMemberCount(); // Reassign new high
                        highestTeam = aTeam;
                    } else {
                        continue; // Logic error
                    }
                } else {
                    continue; // These teams are balanced.
                }
            }
        }

        difference = highestmembercount - lowestmembercount;
        if ((highestTeam == lowestTeam) || difference < balanceThreshold) {
            // The difference between the teams is not great enough to balance the teams as defined by balancethreshold.
            return true;
        }

        if (difference % a.getTeams().size() == 0) {
            // The teams balance evenly.
        	String player = highestTeam.getRandomPlayer(a);
        	if(player != null) {
        		balancePlayer(a, player, lowestTeam); // Move one player from the team with the higher number of players to the lower.
        	}else {
        		loop++;
        		return false;
        	}
            
        } else {
        	String player = highestTeam.getRandomPlayer(a);
        	if(player != null) {
        		// The teams balance unevenly.
                balancePlayer(a, player, null); // Move one player from the team with the higher number of players to lobby.
        	}else {
        		loop++;
        		return false;
        	}
            
        }
        
        loop++;
        boolean balanced = balanceTeams(a, loop, balanceThreshold); // Check Teams again to check if balanced.
        return balanced;
    }

	@SuppressWarnings("deprecation")
	private void balancePlayer(Arena a, String p, Team newTeam) {
        // Reseting player data       
        if (newTeam == null) {
            // Moving to Lobby
            a.getPlayerData(p).getTeam().substractOneMemeberCount();
            a.getPlayerData(p).setTeam(null);
            a.getPlayerData(p).setInArena(false);
            a.getPlayerData(p).setInLobby(true);
            a.getLobby().getPlayersInLobby().put(p, false);
            a.getPlayerData(p).setReady(false);
            a.getPlayerData(p).setJustJoined(true); // Flag for teleport
            a.getPlayerData(p).setLobbyJoinTime(System.currentTimeMillis());     
            a.getPlayerData(p).isWarned(false);
            a.getPlayerData(p).setRole(null);
            
            Player player = getServer().getPlayer(p);
            
            // Remove Helmet
            player.getInventory().setHelmet(null);
            player.getInventory().remove(Material.WOOL);
            
            
            //It's deprecated but it's currently the only way to get the desired effect.
            player.updateInventory();
        
            // Get lobby location and move player to it.
            Location loc = new Location(a.getWorld(), a.getLobby().getX(), a.getLobby().getY() + 1, a.getLobby().getZ());
            loc.setYaw((float) a.getLobby().getDir());
            loc.getWorld().loadChunk(loc.getBlockX(), loc.getBlockZ());
            player.teleport(loc); // Teleport player to lobby
            getUtil().sendMessageToPlayers(a, ChatColor.GREEN + p + ChatColor.WHITE + " was moved to lobby! [Team-balancing]");
            
        } else {
            // Moving to other Team
            String oldteam = a.getPlayerData(p).getTeam().getColor();
            ChatColor oldcc = a.getPlayerData(p).getTeam().getChatColor();
            
            a.getPlayerData(p).getTeam().substractOneMemeberCount();
            a.getPlayerData(p).setTeam(newTeam);
            
            Player player = getServer().getPlayer(p);
                                   
            // Change wool colour and Helmet
            ItemStack[] contents = player.getInventory().getContents();
            int amountofwool = 0;
            for (ItemStack item : contents) {
                if (item == null) {
                    continue;
                }
                
                if (item.getType() == Material.WOOL) {
                    amountofwool += item.getAmount();
                }
            }
            
            player.getInventory().remove(Material.WOOL);
            
            //Give wool
            DyeColor color1 = DyeColor.valueOf(newTeam.getColor().toUpperCase());
            ItemStack helmet = new ItemStack(Material.WOOL, 1, color1.getData());
            player.getInventory().setHelmet(helmet);
            
            if (amountofwool !=0) {
                ItemStack wool = new ItemStack(Material.WOOL, amountofwool, color1.getData());
                player.getInventory().addItem(wool);
            }

            //It's deprecated but it's currently the only way to get the desired effect.
            player.updateInventory();
            
            // Get team spawn location and move player to it.
            Spawn spawn =
                    a.getTeamSpawns().get(newTeam.getColor()) != null ?
                    a.getTeamSpawns().get(newTeam.getColor()) :
                    newTeam.getSpawn();
            Location loc = new Location(a.getWorld(), spawn.getX(), spawn.getY(), spawn.getZ());
            loc.setYaw((float) spawn.getDir());
            a.getWorld().loadChunk(loc.getBlockX(), loc.getBlockZ());
            boolean teleport = player.teleport(loc);
            if (!teleport) {
            	player.teleport(new Location(player.getWorld(), spawn.getX(), spawn.getY(), spawn.getZ(), 0.0F, (float)spawn.getDir()));
            }
            getUtil().sendMessageToPlayers(a, 
                    newTeam.getChatColor() + player.getName() + ChatColor.WHITE + " changed teams from " 
                    + oldcc + oldteam + ChatColor.WHITE + " to "+ newTeam.getChatColor() + newTeam.getColor() + ChatColor.WHITE + "! [Team-balancing]");
            newTeam.addOneMemeberCount();
        }
    }

    public void checkForGameEndThenPlayerLeft(Arena a) {
        if (a.getPlayersData().size() < 2 && !a.isPreGame()) {
            //maybe dc or something. it should be moved to checking to see players who left the game
            boolean zeroPlayers = true;
            for (int i = 0; i < a.getTeams().size(); i++) {
                if (a.getTeams().get(i).getMemberCount() == 1) {
                    zeroPlayers = false;
                    getUtil().sendMessageToPlayers(a, "The game has stopped because there are too few players. "
                            + a.getTeams().get(i).getChatColor() + a.getTeams().get(i).getColor().toUpperCase() + ChatColor.WHITE + " wins! (With a final score of "
                            + a.getTeams().get(i).getScore() + ")");
                    a.endGame(false, true);//Game ended prematurely, don't give rewards but do countdown.
                    break;
                }
            }
            if (zeroPlayers == true) {
            	getUtil().sendMessageToPlayers(a, "No players left. Resetting game.");
                a.endGame(false, false);//Game ended prematurely, don't give rewards.
            }
        }
    }

    /** Checks and calculates the Player's killstreak and deathstreak and outputs an appropriate message according to config.
     * @param player The player
     * @param died If they died (false if they were the killer). */
    public void checkForKillMSG(Arena a, Player player, boolean died) {
        PlayerData data = a.getPlayerData(player);
        if (died) {
            data.addOneDeath();
            data.addOneDeathInARow();
            data.setKillsInARow(0);
        } else {
            data.addOneKill();
            data.addOneKillInARow();
            data.setDeathsInARow(0);
            String message = a.getConfigOptions().killStreakMessages.getMessage(data.getKillsInARow());

            if (!message.isEmpty()) {
            	getUtil().sendMessageToPlayers(a, message.replace("%player", a.getPlayerData(player).getTeam().getChatColor() + player.getName() + ChatColor.WHITE));
            }
        }

        a.addPlayerData(player, data);
    }

    private void loadConfigFiles(boolean reloading) {
    	if(reloading) {
    		for(Arena a : arenaMaster.getArenas())
    			a.endGame(false, false);//Don't give rewards as the game ended prematurely.
    		arenaMaster.resetArenas();
    	}
    	
        loadRoles();
        loadRewards();
        loadHealingItems();

        //Load existing arenas
        getArenaMaster().loadArenas(new File(mainDir + File.separator + "Arenas"));

        // Load arenas boundaries
        for(Arena a : getArenaMaster().getArenas()) {
            ArenaBoundaries tmpBound = new ArenaBoundaries();
            tmpBound.setWorld(a.getWorld().getName());
            tmpBound.setx1(a.getX1());
            tmpBound.setx2(a.getX2());
            tmpBound.sety1(a.getY1());
            tmpBound.sety2(a.getY2());
            tmpBound.setz1(a.getZ1());
            tmpBound.setz2(a.getZ2());

            getArenaMaster().getArenasBoundaries().put(a.getName(), tmpBound);
        }

        globalConfigOptions = getConfigTools().getConfigOptions(globalConfigFile);
        FileConfiguration globalConfig = getConfigTools().load();

        String arenaName = globalConfig.getString("Arena");
        if (arenaName == null)
        	getArenaMaster().clearSelectedArena();
        else if (getArenaMaster().getArena(arenaName) == null)
        	getArenaMaster().clearSelectedArena();
        else
        	getArenaMaster().setSelectedArena(arenaName);
    }

    public void loadHealingItems() {
        FileConfiguration config = getConfigTools().load();
        // Healing items loading
        if (!config.contains("HealingItems")) {
            config.set("HealingItems.BREAD.HOTHeal", 1);
            config.set("HealingItems.BREAD.HOTInterval", 1);
            config.set("HealingItems.BREAD.Duration", 5);
            config.set("HealingItems.BREAD.Cooldown", 0);
            config.set("HealingItems.BREAD.ResetCooldownOnDeath", false);
            config.set("HealingItems.GOLDEN_APPLE.InstantHeal", 20);
            config.set("HealingItems.GOLDEN_APPLE.Cooldown", 60);
            config.set("HealingItems.GOLDEN_APPLE.ResetCooldownOnDeath", true);
            config.set("HealingItems.GRILLED_PORK.HOTHeal", 1);
            config.set("HealingItems.GRILLED_PORK.HOTInterval", 3);
            config.set("HealingItems.GRILLED_PORK.Duration", 5);
            config.set("HealingItems.GRILLED_PORK.Cooldown", 10);
            config.set("HealingItems.GRILLED_PORK.InstantHeal", 5);
            config.set("HealingItems.GRILLED_PORK.ResetCooldownOnDeath", true);
        }
        
        int itemNR = 0;
        for (String str : config.getConfigurationSection("HealingItems").getKeys(false)) {
            itemNR++;
            HealingItems hItem = new HealingItems();
            try {
                hItem.item = getUtil().getItemListFromString(str).get(0);
                hItem.instantHeal = config.getInt("HealingItems." + str + ".InstantHeal", 0);
                hItem.hotHeal = config.getInt("HealingItems." + str + ".HOTHeal", 0);
                hItem.hotInterval = config.getInt("HealingItems." + str + ".HOTInterval", 0);
                hItem.duration = config.getInt("HealingItems." + str + ".Duration", 0);
                hItem.cooldown = config.getInt("HealingItems." + str + ".Cooldown", 0);
                hItem.resetCooldownOnDeath = config.getBoolean("HealingItems." + str + ".ResetCooldownOnDeath", true);
            } catch (Exception e) {
            	getLogger().warning("Error while loading Healing items! " + itemNR + " item!");
            }

            healingItems.add(hItem);
        }
        
        try {
            config.options().copyDefaults(true);
            config.save(globalConfigFile);
        } catch (IOException ex) {
        	ex.printStackTrace();
            logSevere("Couldn't save the global config file, please see the StackTrace above.");
        }
    }

    public void loadRoles () {
        FileConfiguration config = getConfigTools().load();
        if (!config.contains("Roles")) {
            config.set("Roles.Tank.Items", "268, 297:16, DIAMOND_CHESTPLATE, 308, 309, SHEARS, CAKE");
            config.set("Roles.Fighter.Items", "272, 297:4, 261, 262:32, CHAINMAIL_CHESTPLATE, CHAINMAIL_LEGGINGS, CHAINMAIL_BOOTS");
            config.set("Roles.Ranger.Items", "268, 297:6, 261, 262:256, 299, 300, 301");
            config.set("Roles.Berserker.Items", "267, GOLDEN_APPLE:2");
        }
        
        for (String str : config.getConfigurationSection("Roles").getKeys(false)) {
            String text = config.getString("Roles." + str + ".Items");
            roles.put(str.toLowerCase(), getUtil().getItemListFromString(text));
        }
        
        try {
            config.options().copyDefaults(true);
            config.save(globalConfigFile);
        } catch (IOException ex) {
            ex.printStackTrace();
            logSevere("Couldn't save the global config file, please see the StackTrace above.");
        }
    }

    public void loadRewards () {
        FileConfiguration config = getConfigTools().load();
        if (!config.contains("Rewards")) {
			config.set("Rewards.WinnerTeam.ItemCount", "2");
			config.set("Rewards.WinnerTeam.Items", "DIAMOND_LEGGINGS, DIAMOND_HELMET, DIAMOND_CHESTPLATE, DIAMOND_BOOTS, DIAMOND_AXE, DIAMOND_HOE, DIAMOND_PICKAXE, DIAMOND_SPADE, DIAMOND_SWORD");
			config.set("Rewards.OtherTeams.ItemCount", "1");
			config.set("Rewards.OtherTeams.Items", "CAKE, RAW_FISH:5, COAL:5, 56, GOLDEN_APPLE");
			config.set("Rewards.ForKillingEnemy", "APPLE, BREAD, ARROW:10");
			config.set("Rewards.ForCapturingThePoint", "CLAY_BRICK, SNOW_BALL:2, SLIME_BALL, IRON_INGOT");
			config.set("Rewards.ExpRewardForKillingOneEnemy", "0");
        }
        
        rewards = new Rewards();
        rewards.setExpRewardForKillingEnemy(config.getInt("Rewards.ExpRewardForKillingOneEnemy", 0));
        rewards.setWinnerRewardCount(config.getInt("Rewards.WinnerTeam.ItemCount", 2));
        rewards.setWinnerRewards(getUtil().getItemListFromString(config.getString("Rewards.WinnerTeam.Items")));
        rewards.setOtherTeamRewardCount(config.getInt("Rewards.OtherTeams.ItemCount", 1));
        rewards.setLooserRewards(getUtil().getItemListFromString(config.getString("Rewards.OtherTeams.Items")));
        rewards.setRewardsForCapture(getUtil().getItemListFromString(config.getString("Rewards.ForCapturingThePoint")));
        rewards.setRewardsForKill(getUtil().getItemListFromString(config.getString("Rewards.ForKillingEnemy")));
        
        try {
            config.options().copyDefaults(true);
            config.save(globalConfigFile);
        } catch (IOException ex) {
            ex.printStackTrace();
            logSevere("Unable to save the global config file, please see the above StackTrace.");
        }
    }

    /** Add the CTP commands to the master commands list */
    private void populateCommands () {
        commands.clear();
        commands.add(new AliasesCommand(this));
        commands.add(new AutoCommand(this));
        commands.add(new BuildCommand(this));
        commands.add(new ColorsCommand(this));
        commands.add(new DebugCommand(this));
        commands.add(new HelpCommand(this));
        commands.add(new JoinAllCommand(this));
        commands.add(new JoinCommand(this));
        commands.add(new KickCommand(this));
        commands.add(new LateJoinCommand(this));
        commands.add(new LeaveCommand(this));
        commands.add(new ListCommand(this));
        commands.add(new PJoinCommand(this));
        commands.add(new ReloadCommand(this));
        //commands.add(new SaveCommand(this));
        commands.add(new SelectCommand(this));
        commands.add(new SetpointsCommand(this));
        //commands.add(new SetpointCommand(this));
        commands.add(new StartCommand(this));
        commands.add(new StatsCommand(this));
        commands.add(new StopCommand(this));
        commands.add(new TeamCommand(this));
        commands.add(new VersionCommand(this));
    }
    
    private boolean setupPermissions() {
    	RegisteredServiceProvider<Permission> permissionProvider = getServer().getServicesManager().getRegistration(net.milkbowl.vault.permission.Permission.class);
    	
        if (permissionProvider != null) {
            permission = permissionProvider.getProvider();
            getLogger().info("Vault plugin found, permission support enabled.");
            UsePermissions = true;
        }else {
        	getLogger().info("Permission system not detected, defaulting to OP");
            UsePermissions = false;
        }
        
        return (permission != null);
    }

    private boolean setupEconomy() {
        if (getServer().getPluginManager().getPlugin("Vault") == null) {
        	getLogger().info("Vault plugin not detected, disabling economy support.");
            return false;
        }

        RegisteredServiceProvider<Economy> economyProvider = getServer().getServicesManager().getRegistration(net.milkbowl.vault.economy.Economy.class);
        if (economyProvider != null) {
            economyHandler = economyProvider.getProvider();
        }

        if(economyHandler != null)
        	getLogger().info("Vault plugin found, economy support enabled.");

        return economyHandler != null;
    }
    
    public Economy getEconomyHandler() {
    	return this.economyHandler;
    }
    
    public boolean usePermissions() {
    	return this.UsePermissions;
    }
    
    /** Returns the plugin manager, non statically. */
    public PluginManager getPluginManager() {
    	return this.pluginManager;
    }
    
    /** Returns the ArenaMaster instance */
    public ArenaMaster getArenaMaster() {
    	return this.arenaMaster;
    }
    
    /** Returns the ArenaUtils instance. */
    public ArenaUtils getArenaUtil() {
    	return this.aUtil;
    }
    
    /** Returns the ConfigTools instance. */
    public ConfigTools getConfigTools() {
    	return this.cTools;
    }
    
    /** Returns the InvManagement instance. */
    public InvManagement getInvManagement() {
    	return this.invMan;
    }
    
    /** Returns the MoneyUtils instance. */
    public MoneyUtils getMoneyUtil() {
    	return this.mUtil;
    }
    
    /** Returns the Permissions instance. */
    public Permissions getPermissions() {
    	return this.perm;
    }
    
    /** Returns the Util instance. */
    public Util getUtil() {
    	return this.util;
    }
    
    /** Returns the ArenaRestore instance */
    public ArenaRestore getArenaRestore(){
    	return this.arenaRestore;
    }
    
    /** Returns the MySQL Connector */
    public MysqlConnector getMysqlConnector() {
    	return this.mysqlConnector;
    }
    
    public int getLobbyActivity() {
    	return this.lobbyActivity;
    }
    
    public boolean isFirstTime() {
    	return this.firstTime;
    }
    
    public File getGlobalConfig() {
    	return this.globalConfigFile;
    }
    
    public String getMainDirectory() {
    	return this.mainDir;
    }
    
    public ConfigOptions getGlobalConfigOptions() {
    	return this.globalConfigOptions;
    }
    
    /**
     * Returns the roles defined in the config.
     * 
     * @return The roles
     */
    public HashMap<String, List<Items>> getRoles() {
    	return this.roles;
    }
    
    /** The list of Healing Items stored by CTP.
     * 
     * @return The list of healing items.
     */
    public List<HealingItems> getHealingItems() {
    	return this.healingItems;
    }
    
    /**
     * Returns the Rewards data.
     * @see Rewards
     */
    public Rewards getRewards() {
    	return this.rewards;
    }
    
    /** Returns the Hashmap of the player inventories. */
    public HashMap<String, ItemStack[]> getInventories() {
    	return this.Inventories;
    }
    
    /** Returns the Hashmap of the armor stored. */
    public HashMap<String, ItemStack[]> getArmor() {
    	return this.armor;
    }
    
    /**
     * Send a message to the player, with color and a prefix.
     * 
     * @param p The player in which to send the message to.
     * @param message The message to send. "[CTP] " has been included.
     */
    public void sendMessage(Player p, String message) {
    	p.sendMessage(ChatColor.AQUA + "[CTP] " + ChatColor.WHITE + message);
    }
    
    /**
     * Provide a way to get the logger.
     */
    public void logSevere(String msg) {
    	getLogger().severe(msg);
    }
    
	public void logWarning(String msg) {
		getLogger().warning(msg);
	}
    
    public void logInfo(String msg) {
    	getLogger().info(msg);
    }
}