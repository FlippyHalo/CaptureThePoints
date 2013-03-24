package me.dalton.capturethepoints.commands;

import me.dalton.capturethepoints.CaptureThePoints;
import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.entity.Player;

public class AutoCommand extends CTPCommand {
    private String worldname = "";
    
    /** This command will bring all players on a world into a random lobby which is guaranteed to hold everyone (if not, use the already selected arena) */
    public AutoCommand(CaptureThePoints instance) {
        this(instance, "");
    }
    
    /** If the world name is supplied */
    public AutoCommand(CaptureThePoints instance, String worldname) {
        this.worldname = worldname;
        super.ctp = instance;
        super.aliases.add("auto");
        super.notOpCommand = false;
        super.requiredPermissions = new String[]{"ctp.*", "ctp.admin", "ctp.admin.auto"};
        super.senderMustBePlayer = false;
        super.minParameters = 3;
        super.maxParameters = 3;
        super.usageTemplate = "/ctp auto <worldname|this>";
    }

    @Override
    public void perform() {
        if (sender instanceof Player) {
            String error = ctp.getArenaMaster().checkArena(ctp.getArenaMaster().getSelectedArena(), player);
            if (!error.isEmpty()) {
                sendMessage(error);
                return;
            }
        } else {
            if (ctp.getArenaMaster().getSelectedArena() == null) {
                sendMessage(ChatColor.RED + "Please create an arena first");
                return;
            }
            if (ctp.getArenaMaster().getSelectedArena().getLobby() == null) {
                sendMessage(ChatColor.RED + "Please create arena lobby");
                return;
            }
        }
        if (this.worldname.isEmpty()) {
            this.worldname = parameters.get(2);
        }
        if (this.worldname.equalsIgnoreCase("this") && player != null) {
            this.worldname = player.getWorld().getName();
        }

        World world = ctp.getServer().getWorld(worldname);
        if (world == null) {
            sendMessage(ChatColor.RED + worldname + " is not a recognised world.");
            sendMessage(ChatColor.RED + "Hint: your first world's name is \"" + ctp.getServer().getWorlds().get(0).getName() + "\".");
            return;
        }

        if (ctp.getArenaMaster().hasSuitableArena(world.getPlayers().size())) {
            //ctp.chooseSuitableArena(world.getPlayers().size()); // Choose a suitable arena based on the number of players in the world. TODO
        } else {
            sendMessage("[CTP] You do not have an arena that will accomodate "+world.getPlayers().size()+" players. Please change your min/max player settings.");
            return;
        }
        
        if (ctp.getArenaMaster().getSelectedArena().isGameRunning()) {
            sendMessage("A previous Capture The Points game has been terminated.");
            ctp.getArenaMaster().getSelectedArena().endGame(true);
        }

        for (Player p : world.getPlayers())
            ctp.getArenaMaster().moveToLobby(ctp.getArenaMaster().getSelectedArena(), p);

        return;
    }
}