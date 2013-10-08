package me.dalton.capturethepoints.beans;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import net.milkbowl.vault.economy.EconomyResponse;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.DyeColor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityRegainHealthEvent;
import org.bukkit.event.entity.EntityRegainHealthEvent.RegainReason;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

import me.dalton.capturethepoints.CaptureThePoints;
import me.dalton.capturethepoints.ConfigOptions;
import me.dalton.capturethepoints.HealingItems;
import me.dalton.capturethepoints.beans.tasks.AutoStartTimer;
import me.dalton.capturethepoints.beans.tasks.EndCountDownTimer;
import me.dalton.capturethepoints.beans.tasks.ItemCoolDownsTask;
import me.dalton.capturethepoints.beans.tasks.PlayTimer;
import me.dalton.capturethepoints.beans.tasks.ScoreGenerationTask;
import me.dalton.capturethepoints.beans.tasks.ScoreMessengerTask;
import me.dalton.capturethepoints.enums.ArenaLeaveReason;
import me.dalton.capturethepoints.enums.Status;
import me.dalton.capturethepoints.events.CTPEndEvent;
import me.dalton.capturethepoints.events.CTPPlayerJoinEvent;
import me.dalton.capturethepoints.events.CTPPlayerLeaveEvent;
import me.dalton.capturethepoints.listeners.TagAPIListener;
import me.dalton.capturethepoints.util.LangTools;
import me.dalton.capturethepoints.util.PotionManagement;

/** Arena Data of the saved arenas for playing {@link CaptureThePoints}.
 * 
 * @author graywolf336
 */
public class Arena {
	//general
	private CaptureThePoints ctp;
    private String name = "";
    private String world;
    private Vector corner1, corner2;
    
    //config
    private ConfigOptions co;
    
    private HashMap<String, Spawn> teamSpawns;
    private List<Team> teams;
    private HashSet<Points> capturePoints;
    private List<String> waitingToMove;
    private Map<String, PlayerData> players;
    private HashMap<String, Location> previousLocation;
    private Lobby lobby;
    private Stands stands;
    
    //Scheduler, status, etc
    private Status status;
    private AutoStartTimer startTimer;
    private EndCountDownTimer endTimer;
    private ItemCoolDownsTask itemCoolDowns;
    private PlayTimer playTime;
    private ScoreGenerationTask scoreGen;
    private ScoreMessengerTask scoreMsg;
    private boolean move = true;
    
    private int minimumPlayers = 2;
    private int maximumPlayers = 9999;
    
    /**
     * Initiates a new arena instance.
     * <p />
     * 
     * @param plugin The CTP plugin instance.
     * @param name The name of the arena.
     */
    public Arena(CaptureThePoints plugin, String name, Status status, int startSeconds, int endSeconds, int playingTime) {
    	this.ctp = plugin;
    	this.name = name;
    	this.status = status;
    	this.teamSpawns = new HashMap<String, Spawn>();
    	this.teams = new ArrayList<Team>();
    	this.capturePoints = new HashSet<Points>();
    	this.waitingToMove = new LinkedList<String>();
    	this.players = new ConcurrentHashMap<String, PlayerData>();
    	this.previousLocation = new HashMap<String, Location>();
    	
    	this.startTimer = new AutoStartTimer(ctp, this, startSeconds);
    	this.endTimer = new EndCountDownTimer(ctp, this, endSeconds);
    	this.itemCoolDowns = new ItemCoolDownsTask(ctp, this);
    	this.playTime = new PlayTimer(ctp, this, playingTime * 60 * 20); //Convert minutes to seconds, seconds to ticks
    	this.scoreGen = new ScoreGenerationTask(ctp, this);
    	this.scoreMsg = new ScoreMessengerTask(ctp, this);
    }
    
    /** Sets the name of this arena. */
    public void setName(String name) {
    	this.name = name;
    }
    
    /** Gets the name of this arena. */
    public String getName() {
    	return this.name;
    }
    
    /** Sets the name of the world this arena is in. */
    public void setWorld(String world) {
    	this.world = world;
    }

    /** Gets the world object this arena is in. */
    public World getWorld() {
    	return world == null ? null : ctp.getServer().getWorld(this.world);
    }
    
    /** Gets the name of the world this arena is in. */
    public String getWorldName() {
    	return this.world;
    }
    
    /** Sets the arena's config options
     * @see ConfigOptions */
    public void setConfigOptions(ConfigOptions co) {
    	this.co = co;
    }
    
    /** Gets the arena's config options
     * @see ConfigOptions */
    public ConfigOptions getConfigOptions() {
    	return this.co;
    }
    
    /** Gets the teamspawns this arena has (Hashmap of Teamcolor, Spawn). 
     * @see Spawn */
    public HashMap<String, Spawn> getTeamSpawns() {
    	return this.teamSpawns;
    }
    
    /** Gets the Teams stored by CaptureThePoints.
     * @see Team */
    public List<Team> getTeams() {
    	return this.teams;
    }
    
    /** Gets the capture points this arena has. 
     * @see Points */
    public HashSet<Points> getCapturePoints() {
    	return this.capturePoints;
    }
    
    /** Sets this arena's Lobby 
     * @see Lobby */
    public void setLobby(Lobby lobby) {
    	this.lobby = lobby;
    }
    
    /** Gets this arena's Lobby 
     * @see Lobby */
    public Lobby getLobby() {
    	return this.lobby;
    }
    
    /** Sets the arena's stands.
     * 
     * @param stands The stands instance.
     * @see Stands
     */
    public void setStands(Stands stands) {
    	this.stands = stands;
    }
    
    /** Gets the arena's stands.
     * @return The stands teleport location for this arena.
     * @see Stands
     */
    public Stands getStands() {
    	return this.stands;
    }
    
    /**
     * Gets the status of the {@link Arena}.
     * 
     * @return The {@link Status status} of the arena.
     */
    public Status getStatus() {
    	return this.status;
    }
    
    /** Sets the status of the {@link Arena arena}.
     * 
     * @param status Status to set the arena to.
     */
    public void setStatus(Status status) {
    	this.status = status;
    }
    
    /** Updates the status of the arena to what is suitable per players. */
    public void updateStatusToRunning(boolean countingDown) {
    	if(countingDown) {
    		status = Status.COUNTING_DOWN;
    	}else if(players.size() == maximumPlayers)
    		status = Status.FULL_GAME;
    	else
    		status = Status.IN_GAME;
    }
    
    /** Sets whether the players can move or not. */
    public void setMoveAbility(boolean move) {
    	this.move = move;
    }
    
    /** Returns whether the players can move or not. */
    public boolean canMove() {
    	return this.move;
    }
    
    /** Returns the {@link AutoStartTimer start timer} for this arena. */
    public AutoStartTimer getStartTimer() {
    	return this.startTimer;
    }
    
    /** Returns the {@link EndCountDownTimer end timer} for this arena. */
    public EndCountDownTimer getEndTimer() {
    	return this.endTimer;
    }
    
    /** Returns the {@link ItemCoolDownsTask} for this arena. */
    public ItemCoolDownsTask getItemCoolDownTask() {
    	return this.itemCoolDowns;
    }
    
    /** Returns the {@link PlayTimer} for the timer countdown. */
    public PlayTimer getPlayTimer() {
    	return this.playTime;
    }
    
    /** Returns the {@link ScoreGenerationTask} for the score generation task. */
    public ScoreGenerationTask getScoreGenTask() {
    	return this.scoreGen;
    }
    
    /** Returns the {@link ScoreMessengerTask} for the score messaging to the players. */
    public ScoreMessengerTask getScoreMessenger() {
    	return this.scoreMsg;
    }
    
    /** Sets the first corner to the given block coords. */
    public void setFirstCorner(int x, int y, int z) {
    	if(x == 0 && y == 0 && z == 0) return;
    	
    	this.corner1 = new Vector(x, y, z);
    }
    
    /** Returns the first corner of this arena in {@link Vector} form. */
    public Vector getFirstCorner() {
    	return this.corner1;
    }
    
    /** Sets the second corner to the given block coords. */
    public void setSecondCorner(int x, int y, int z) {
    	if(x == 0 && y == 0 && z == 0) return;
    	
    	this.corner2 = new Vector(x, y, z);
    }
    
    /** Returns the second corner of this arena in {@link Vector} form. */
    public Vector getSecondCorner() {
    	return this.corner2;
    }
    
    /** Sets the minimum number of players this arena can take. [Default: 2] */
    public void setMinPlayers(int amount) {
    	this.minimumPlayers = amount;
    }
    
    /** Gets the minimum number of players this arena can take. [Default: 2] */
    public int getMinPlayers() {
    	return this.minimumPlayers;
    }
    
    /** Sets the maximum number of players this arena can take. [Default: 9999] */
    public void setMaxPlayers(int amount) {
    	this.maximumPlayers = amount;
    }
    
    /** Gets the maximum number of players this arena can take. [Default: 9999] */
    public int getMaxPlayers() {
    	return this.maximumPlayers;
    }
    
    /** Gets the List<String> of the players waiting to be moved in this arena. */
	public List<String> getWaitingToMove(){
		return this.waitingToMove;
	}
    
    /** Returns a list of all the players in the arena, including the lobby, as a List of Strings of their name.
     * <p />
     * 
     * @return The player name list
     * @since 1.5.0-b123
     */
    public List<String> getPlayers() {
        List<String> toReturn = new ArrayList<String>();
        
        for (String p : players.keySet())
            toReturn.add(p);
            
        return toReturn;
    }
    
    /**
     * Returns the given player's arena data.
     * <p />
     * 
     * @param player The player who's data to get.
     * @return The player data, null if nothing.
     * @since 1.5.0-b123
     */
    public PlayerData getPlayerData(String player) {
    	return this.players.get(player);
    }
    
    /**
     * Returns the given player's arena data.
     * <p />
     * 
     * @param player The player who's data to get.
     * @return The player data, null if nothing.
     * @since 1.5.0-b126
     */
    public PlayerData getPlayerData(Player player) {
    	return this.getPlayerData(player.getName());
    }
    
    /**
     * Adds a player and his/her data to the list.
     * 
     * @param player The player who is being added.
     * @param playerdata The data about this player.
     * @since 1.5.0-148
     */
    public void addPlayerData(String player, PlayerData playerdata) {
    	this.players.put(player, playerdata);
    }
    
    /**
     * Adds a player and his/her data to the list.
     * 
     * @param player The player who is being added.
     * @param playerdata The data about this player.
     * @since 1.5.0-165
     */
    public void addPlayerData(Player player, PlayerData playerdata) {
    	this.players.put(player.getName(), playerdata);
    }
    
    /**
     * Returns a Map of all the players in the arena and their corresponding data.
     * <p />
     * 
     * @return Every player in this arena's data.
     * @since 1.5.0-b123
     */
    public Map<String, PlayerData> getPlayersData() {
    	return this.players;
    }
    
    /** Get all Players that are playing in this arena as a list of playername strings
     * <p />
     * 
     * @return The player name list
     */
    public List<String> getPlayersPlaying() {
        List<String> toReturn = new ArrayList<String>();
        
        for (String p : players.keySet()) {
        	if(!players.get(p).inLobby())
        		toReturn.add(p);
        	else
        		continue;
        }
        
        return toReturn;
    }
    
    /** Send message to Players that are playing in an arena
     * <p />
     * 
     * @param arena The arena to send the message to it's players.
     * @param message The message to send. "[CTP] " has been included.
     */
    public void sendMessageToPlayers(String message) {
        for (String player : getPlayersData().keySet()) {
        	Player p = ctp.getServer().getPlayerExact(player);
            p.sendMessage(ChatColor.AQUA + "[CTP] " + ChatColor.WHITE + message); // Kj
        }
    }
    
    /** Send message to Players that are playing in the given arena but exclude a person.
     * <p />
     * 
     * @param arena The arena to send the message to it's players.
     * @param exclude The Player to exclude
     * @param s The message to send. "[CTP] " has been included.
     */
    public void sendMessageToPlayers(Player exclude, String s) {
        for (String player : getPlayersData().keySet()) {
        	if(player.equalsIgnoreCase(exclude.getName())) continue;
        	
        	Player p = ctp.getServer().getPlayerExact(player);
            if (p != null)
                p.sendMessage(ChatColor.AQUA + "[CTP] " + ChatColor.WHITE + s); // Kj
        }
    }
    
    /**
     * Player's previous Locations before they started playing CTP.
     * 
     * @return A HashMap of the players pervious locations.
     * @since 1.5.0-b155
     */
    public HashMap<String, Location> getPrevoiusPosition() {
    	return this.previousLocation;
    }
    
    /** Check to see if this Arena has a lobby.
     * <p />
     * 
     * @return true if Arena has a lobby, else false.
     */
    public boolean hasLobby() {
        return this.lobby != null;
    }
    
    /**
     * Schedules a repeating task to run after the given delayed time and repeats after the given time, both in ticks.
     * 
     * @param r The count down task to schedule
     * @param delay The ticks to wait before running the task
     * @param period The ticks to wait between runs
     * @return The id of the task.
     */
	public int scheduleDelayedRepeatingTask(Runnable r, long delay, long period) {
		return Bukkit.getScheduler().runTaskTimer(ctp, r, delay, period).getTaskId();
	}
	
	/**
	 * Schedules a task which ones once at after the specified time, delay, has passed.
	 * 
	 * @param r The task to be scheduled
	 * @param delay The time to pass before we run this
	 * @return The id of the scheduled task, -1 if something went wrong.
	 */
	public int scheduleDelayedTask(Runnable r, long delay) {
		return Bukkit.getScheduler().scheduleSyncDelayedTask(ctp, r, delay);
	}
	
	/** 
	 * Starts all the tasks except for start count down and end count down.
	 * 
	 * <ul>
	 *  <li>Play timer, if not using score generation</li>
	 *  <li>Score Generation Task, if score generation is enabled</li>
	 *  <li>Score Messenger Task, if score generation is enabled</li>
	 *  <li>Item Cool Down Task</li>
	 * </ul>
	 */
	public void startOtherTasks() {
        //Start all the other tasks and timers, since the game is starting.
        if(!getConfigOptions().useScoreGeneration)
        	getPlayTimer().schedule();
        
        if(getConfigOptions().useScoreGeneration) {
        	getScoreGenTask().start();
        	getScoreMessenger().start();
        }
        
        getItemCoolDownTask().start();
	}
	
	/**
	 * Sends the player to the lobby.
	 * 
	 * @param p The player to send to the lobby.
	 * @return Whether it was successful or not.
	 */
	public boolean joinLobby(Player player) {
		//Don't add someone who is already in
		if(players.get(player.getName()) != null) return false;
		
		String mainArenaCheckError = ctp.getArenaMaster().checkArena(this, player); // Check arena, if there is an error, an error message is returned.
        if(!mainArenaCheckError.isEmpty()) {
            ctp.sendMessage(player, mainArenaCheckError);
            return false;
        }

        // Some more checks
        if(player.isInsideVehicle()) {
            try {
                player.leaveVehicle();
            } catch (Exception e) {
                player.kickPlayer(ctp.getLanguage().checks_PLAYER_IN_VEHICLE); // May sometimes reach this if player is riding an entity other than a Minecart
                return false;
            }
        }
        
        if(player.isSleeping()) {
            player.kickPlayer(ctp.getLanguage().checks_PLAYER_SLEEPING);
            return false;
        }

        if(players.isEmpty())
            lobby.getPlayersInLobby().clear();   //Reset if first to come

    	//Call a custom event for when players join the arena
        CTPPlayerJoinEvent event = new CTPPlayerJoinEvent(player, this, ctp.getLanguage().PLAYER_JOIN.replaceAll("%PN", player.getName()));
        ctp.getPluginManager().callEvent(event);
        player = event.getPlayer(); //In case some plugin sets data to this
        
        if(event.isCancelled())
        	return false; //Some plugin cancelled the event, so don't go forward and allow the plugin to handle the message that is sent when cancelled.
        
        if(ctp.getEconomy() != null && getConfigOptions().economyMoneyCostForJoiningArena != 0) {
            EconomyResponse r = ctp.getEconomy().bankWithdraw(player.getName(), getConfigOptions().economyMoneyCostForJoiningArena);
            if(r.transactionSuccess()) {
                ctp.sendMessage(player,
                		ctp.getLanguage().SUCCESSFUL_PAYING_FOR_JOINING
                			.replaceAll("%EA", String.valueOf(r.amount))
                			.replaceAll("%AN", name));
            } else {
                ctp.sendMessage(player, ctp.getLanguage().NOT_ENOUGH_MONEY_FOR_JOINING);
                event.setCancelled(true);
                return false;
            }
        }
        
        // Assign player's PlayerData
        PlayerData data = new PlayerData(player, getConfigOptions().moneyAtTheLobby, getConfigOptions().playerLives);
        
        // Store and remove potion effects on player
        data.setPotionEffects(PotionManagement.storePlayerPotionEffects(player));
        PotionManagement.removeAllEffects(player);
        
        // Save player's previous state 
        if (player.getGameMode() == GameMode.CREATIVE) {
            data.inCreative(true);
            player.setGameMode(GameMode.SURVIVAL);
        }

        addPlayerData(player, data);
        getLobby().getPlayersInLobby().put(player.getName(), false); // Kj
        getLobby().getPlayersWhoWereInLobby().add(player.getName()); // Kj
        
        player.setFlying(false);
        player.setFoodLevel(20);
        player.setMaxHealth(getConfigOptions().maxPlayerHealth);//Sets their health to the custom maximum.
        
        //Set the player's health and also trigger an event to happen because of it, add compability with other plugins
        player.setHealth(getConfigOptions().maxPlayerHealth);
        EntityRegainHealthEvent regen = new EntityRegainHealthEvent(player, (double)getConfigOptions().maxPlayerHealth, RegainReason.CUSTOM);
    	ctp.getPluginManager().callEvent(regen);
    	player = (Player) regen.getEntity(); //In case some plugin sets something different here
        
        // Get lobby location and move player to it.
        Location loc = new Location(getWorld(), getLobby().getX(), getLobby().getY(), getLobby().getZ());
        loc.setYaw((float) getLobby().getDir());
        if(!loc.getWorld().isChunkLoaded(loc.getChunk()))
        	loc.getWorld().loadChunk(loc.getChunk());
        
        getPrevoiusPosition().put(player.getName(), player.getLocation());
        ctp.getInvManagement().saveInv(player);

        sendMessageToPlayers(event.getJoinMessage());

        // Get lobby location and move player to it.        
        player.teleport(loc); // Teleport player to lobby

        //clear the inventory again in case some other plugin restored some inventory to them after we teleported them (Multiverse inventories)
        ctp.getInvManagement().clearInventory(player, true);
        
        ctp.sendMessage(player, ctp.getLanguage().LOBBY_JOIN.replaceAll("%AN", name));
        getPlayerData(player).setInLobby(true);
        
        if(getConfigOptions().usePlayerTime) {
        	int time = 18000;
        	String t = getConfigOptions().playerTime;
        	
        	if(t.equalsIgnoreCase("dawn"))
        		time = 0;
        	else if(t.equalsIgnoreCase("midday"))
        		time = 6000;
        	else if(t.equalsIgnoreCase("dusk"))
        		time = 12000;
        	else if(t.equalsIgnoreCase("midnight"))
        		time = 18000;
        	
        	player.setPlayerTime(time, false);
        }
        
		return true;
	}
    
    public void leaveGame(Player p, ArenaLeaveReason reason) {
        //On exit we get double signal
        if (players.get(p.getName()) == null)
            return;
        
        if (getWaitingToMove() != null && !getWaitingToMove().isEmpty()) {
            if (p.getName() == getWaitingToMove().get(0) && getWaitingToMove().size() == 1)
            	getWaitingToMove().clear(); // The player who left was someone in the lobby waiting to join. We need to remove them from the queue
            else
            	getWaitingToMove().remove(p.getName());
        }
        
        ctp.getInvManagement().removeCoolDowns(p.getName());
        
        sendMessageToPlayers(p, ctp.getLanguage().PLAYER_LEFT.replaceAll("%PN", p.getName())); // Won't send to "player".
        
        //Remove the number count from the teamdata
        if (players.get(p.getName()).getTeam() != null) {
        	for(Team t : getTeams())
        		if(t == players.get(p.getName()).getTeam()) {
        			t.substractOneMember();
        			break;
        		}
        }

        CTPPlayerLeaveEvent event = new CTPPlayerLeaveEvent(p, this, players.get(p.getName()), reason);
        ctp.getPluginManager().callEvent(event);
        
        getLobby().getPlayersInLobby().remove(p.getName());
        ctp.getInvManagement().restoreThings(p);
        getPrevoiusPosition().remove(p.getName());
        players.remove(p.getName());

        // Check for player replacement if there is someone waiting to join the game
        boolean wasReplaced = false;
        if (getConfigOptions().exactTeamMemberCount && status.isRunning()) {
            for (String playerName : players.keySet()) {
                if (players.get(playerName).inLobby() && players.get(playerName).isReady()) {
                    ctp.getArenaUtil().moveToSpawns(this, playerName);
                    wasReplaced = true;
                    break;
                }
            }
        }

        //check for player count, only then were no replacement
        if (!wasReplaced)
            checkForGameEndWhenPlayerLeft();
            
        //If there was no replacement we should move one member to lobby
        if (!wasReplaced && getConfigOptions().exactTeamMemberCount && status.isRunning())
            if (getConfigOptions().balanceTeamsWhenPlayerLeaves > 0)
                balanceTeams(0);
    }
    
    /**
     * Ends the current game that is happening in the arena, whether to give rewards or not.
     * 
     * @param rewards True to give rewards, false to not give rewards.
     * @param countdown True to countdown to the end, false to just straight up end it.
     */
    public void endGame(final boolean rewards, boolean countdown) {
    	if(countdown)
    		if(getConfigOptions().useEndCountDown)
    			endTimer.start(rewards);
    		else
    			endGameNoCountDown(rewards);
    	else
    		endGameNoCountDown(rewards);
    }
    
    public void endGameNoCountDown(boolean rewards) {
    	CTPEndEvent event = new CTPEndEvent(this, ctp.getLanguage().GAME_ENDED);
    	ctp.getPluginManager().callEvent(event);
    	
        sendMessageToPlayers(event.getEndMessage());

        // Task canceling
        if (playTime.getTaskId() != -1) {
            playTime.cancel();
        }
        
        if (scoreGen.getTaskId() != -1) {
        	scoreGen.cancel();
        }
        
        if (scoreMsg.getTaskId() != -1) {
            scoreMsg.cancel();
        }
        
        if (itemCoolDowns.getTaskId() != -1) {
        	itemCoolDowns.cancel();
        }
        
        if(startTimer.getTaskId() != -1) {
        	startTimer.stop();
        }
        
        if(endTimer.getTaskId() != -1) {
        	endTimer.stop();
        }

        for (Points s : getCapturePoints())
            s.setControlledByTeam(null);
        
        setMoveAbility(true);
        status = Status.JOINABLE;

        for (String player : getPlayersData().keySet()) {
        	Player p = ctp.getServer().getPlayer(player);
        	ctp.getInvManagement().restoreThings(p);
            if (rewards)
                ctp.getUtil().rewardPlayer(this, p);
            if(ctp.useTag())//if we're using tag, refresh it on ending
            	TagAPIListener.refreshTag(p);
        }
        
        //Arena restore
        if(ctp.getGlobalConfigOptions().enableHardArenaRestore)
            ctp.getArenaRestore().restoreMySQLBlocks(this);
        else
            ctp.getArenaRestore().restoreAllBlocks();

        for (HealingItems item : ctp.getHealingItems())
            if (!item.cooldowns.isEmpty())
                item.cooldowns.clear();
        
        getLobby().clearLobbyPlayerData();
        if(getStands() != null) getStands().clearStandsPlayers();
        getPrevoiusPosition().clear();
        getPlayersData().clear();
        getPlayers().clear();
        
        for (Team t : getTeams()) {
            t.setMemberCount(0);
            t.setControlledPoints(0);
            t.setScore(0);
    	}
    }
    
    /** Checks and calculates the Player's killstreak and deathstreak and outputs an appropriate message according to config.
     * 
     * <p>
     * 
     * @param player The player
     * @param died If they died (false if they were the killer).
     */
    public void checkForKillMSG(Player player, boolean died) {
        PlayerData data = getPlayerData(player);
        if (died) {
            data.addOneDeath();
            data.addOneDeathInARow();
            data.setKillsInARow(0);
        } else {
            data.addOneKill();
            data.addOneKillInARow();
            data.setDeathsInARow(0);
            String message = getConfigOptions().killStreakMessages.getMessage(data.getKillsInARow());

            if (!message.isEmpty())
            	sendMessageToPlayers(LangTools.getColorfulMessage(message.replace("%player",
            			data.getTeam().getChatColor() + player.getName() + ChatColor.WHITE)));
        }
    }
    
    private void checkForGameEndWhenPlayerLeft() {
        if (getPlayersData().size() < 2) {
            //maybe dc or something. it should be moved to checking to see players who left the game
            boolean zeroPlayers = true;
            for (Team team : getTeams()) {
                if (team.getMemberCount() == 1) {
                    zeroPlayers = false;
                    
                    sendMessageToPlayers(ctp.getLanguage().GAME_ENDED_TOO_FEW_PLAYERS
	            		.replaceAll("%TC", team.getChatColor() + "")
	            		.replaceAll("%TN", team.getColor().toUpperCase())
	            		.replaceAll("%WS", team.getScore() + ""));
                    
                    endGame(false, true);//Game ended prematurely, don't give rewards but do countdown.
                    break;
                }
            }
            
            if (zeroPlayers) {
            	sendMessageToPlayers(ctp.getLanguage().NO_PLAYERS_LEFT);
                endGame(false, false);//Game ended prematurely, don't give rewards to ghost players we may have.
            }
        }
    }
    
    /** Attempt to balance the teams.
     * 
     * @param loop Times this has recursed (prevents overruns).
     * @return True if the teams are balanced, false if not.
     */
    public boolean balanceTeams(int loop) {
    	int balanceThreshold = getConfigOptions().balanceTeamsWhenPlayerLeaves;
    	
    	ctp.getLogger().info("Balancing teams in the arena '" + getName() + "' and we are in loop '" + loop + "'.");
    	
        if (loop > 5) {
        	ctp.getLogger().warning("balanceTeams hit over 5 recursions. Aborting.");
            return false;
        }
        
        Team lowestTeam = null; // Team with the lower number of players
        int lowestmembercount = -1;
        Team highestTeam = null; // Team with the higher number of players
        int highestmembercount = -1;
        
        int difference = 0;

        for (Team team : getTeams()) {
            if (lowestmembercount == -1) {
                lowestmembercount = team.getMemberCount();
                lowestTeam = team;
                highestmembercount = team.getMemberCount();
                highestTeam = team;
                continue;
            } else {
                if (team.getMemberCount() != lowestmembercount || team.getMemberCount() != highestmembercount) {
                    if (team.getMemberCount() < lowestmembercount) {
                        lowestmembercount = team.getMemberCount(); // Reassign new low
                        lowestTeam = team;
                    } else if (team.getMemberCount() > highestmembercount) {
                        highestmembercount = team.getMemberCount(); // Reassign new high
                        highestTeam = team;
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

        if (difference % getTeams().size() == 0) {
            // The teams balance evenly.
        	String player = highestTeam.getRandomPlayer(this);
        	if(player != null) {
        		balancePlayer(player, lowestTeam); // Move one player from the team with the higher number of players to the lower.
        	}else {
        		loop++;
        		return false;
        	}
            
        } else {
        	String player = highestTeam.getRandomPlayer(this);
        	if(player != null) {
        		// The teams balance unevenly.
                balancePlayer(player, null); // Move one player from the team with the higher number of players to lobby.
        	}else {
        		loop++;
        		return false;
        	}
            
        }
        
        loop++;
        boolean balanced = balanceTeams(loop); // Check Teams again to check if balanced.
        return balanced;
    }

	@SuppressWarnings("deprecation")
	private void balancePlayer(String p, Team newTeam) {
		if(newTeam == null)
			ctp.getLogger().info("Balancing the player '" + p + "' and they are moving to the lobby.");
		else
			ctp.getLogger().info("Balancing the player '" + p + "' and they are moving to the team " + newTeam.getName() + ".");
		
        // Reseting player data
		PlayerData data = getPlayerData(p);
        if (newTeam == null) {
            // Moving to Lobby
        	
            data.getTeam().substractOneMember();
            data.setTeam(null);
            data.setInArena(false);
            data.setInLobby(true);
            getLobby().getPlayersInLobby().put(p, false);
            data.setReady(false);
            data.setJustJoined(true); // Flag for teleport
            data.setLobbyJoinTime(System.currentTimeMillis());     
            data.isWarned(false);
            data.setRole(null);
            
            Player player = ctp.getServer().getPlayerExact(p);
            
            // Remove Helmet
            player.getInventory().setHelmet(null);
            player.getInventory().remove(Material.WOOL);
            
            
            //It's deprecated but it's currently the only way to get the desired effect.
            player.updateInventory();
        
            // Get lobby location and move player to it.
            Location loc = new Location(getWorld(), getLobby().getX(), getLobby().getY() + 1, getLobby().getZ());
            loc.setYaw((float) getLobby().getDir());
            loc.getWorld().loadChunk(loc.getBlockX(), loc.getBlockZ());
            player.teleport(loc); // Teleport player to lobby
            
            sendMessageToPlayers(ctp.getLanguage().TEAM_BALANCE_MOVE_TO_LOBBY.replaceAll("%PN", p));
            
        } else {
            // Moving to other Team
            String oldteam = data.getTeam().getName();
            ChatColor oldcc = data.getTeam().getChatColor();
            
            data.getTeam().substractOneMember();
            data.setTeam(newTeam);
            
            Player player = ctp.getServer().getPlayerExact(p);
                                   
            // Change wool color and Helmet
            int amountofwool = 0;
            for (ItemStack item : player.getInventory().getContents()) {
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
            Spawn spawn = getTeamSpawns().get(newTeam.getColor()) != null ? getTeamSpawns().get(newTeam.getColor()) : newTeam.getSpawn();
            Location loc = new Location(getWorld(), spawn.getX(), spawn.getY(), spawn.getZ());
            loc.setYaw((float) spawn.getDir());
            getWorld().loadChunk(loc.getBlockX(), loc.getBlockZ());
            
            boolean teleport = player.teleport(loc);
            if (!teleport)
            	player.teleport(new Location(player.getWorld(), spawn.getX(), spawn.getY(), spawn.getZ(), 0.0F, (float)spawn.getDir()));
            
            sendMessageToPlayers(ctp.getLanguage().TEAM_BALANCE_CHANGE_TEAMS
	    		.replaceAll("%PN", player.getName())
	    		.replaceAll("%OC", oldcc + "")
	    		.replaceAll("%OT", oldteam)
	    		.replaceAll("%NC", newTeam.getChatColor() + "")
	    		.replaceAll("%NT", newTeam.getName()));
            
            newTeam.addOneMember();
        }
    }
}
