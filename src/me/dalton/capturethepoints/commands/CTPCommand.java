package me.dalton.capturethepoints.commands;

import java.util.ArrayList;
import java.util.List;
import me.dalton.capturethepoints.CaptureThePoints;
import me.dalton.capturethepoints.util.Permissions;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/** The backbone of commands used by CTP */
public abstract class CTPCommand {

    /** CTP plugin instance */
    public CaptureThePoints ctp;
    
    /** List of aliases this command recognizes */
    public List<String> aliases;
    
    /** The permissions associated with this command. Only one has to match to allow usage. */
    public String[] requiredPermissions;
    
    /** Set to true if anyone can use the command, else false if the command issuer has to be an op or CTP admin. */
    public boolean notOpCommand;
    
    /** Command sender must be online as a player to use this command (i.e. it can't be done from the console) */
    public boolean senderMustBePlayer;
    
    /** What to display if the command sender gets the parameters wrong (e.g. /ctp command <parameter>)*/
    public String usageTemplate;
    
    /** The sender of this command */
    public CommandSender sender;
    
    /** The sender of this command as a player (if available) */
    public Player player;
    
    /** The parameters of the command. Starts at parameters.get(0) == "ctp" */
    public List<String> parameters;
    
    /** The minimum parameters necessary for the command to work. "ctp" counts as a parameter, "/ctp command" would be 2 for example. */
    public int minParameters;
    
    /** The maximum parameters necessary for the command to work. "ctp" counts as a parameter, "/ctp command [help]" would be 3 for example. */
    public int maxParameters;
    
    /** ?The maximum parameters necessary for the command to work. "ctp" counts as a parameter, "/ctp command [help]" would be 3 for example. */
    public int actionIndex;

    /** The backbone of commands used by CTP */
    public CTPCommand() {
        aliases = new ArrayList<String>();

        notOpCommand = false;
        senderMustBePlayer = true;

        usageTemplate = "";

        minParameters = 0;
        maxParameters = 0;
    }

    /** Send a message to the sender of the command. [CTP] is put at the start for you.
     * @param message The message you want to send to the player, after the "[CTP] "
     */
    public void sendMessage(String message) {
        sender.sendMessage(ChatColor.AQUA + "[CTP] " + ChatColor.WHITE + message);
    }

    /**  
     * Preliminary tests then perform the command.
     * @param sender The sender issuing the command.
     * @param parameters The command's parameters
     */
    public final void execute(CommandSender sender, List<String> parameters) {
        this.sender = sender;
        this.parameters = parameters;

        if (senderMustBePlayer && !(sender instanceof Player)) {
            sendMessage("This command can only be used by players.");
            return;
        } else if (sender instanceof Player) {
            this.player = (Player) sender;
        }

        if (requiredPermissions != null) {
            if (requiredPermissions.length != 0 && !Permissions.canAccess(sender, notOpCommand, requiredPermissions)) {
                sendMessage(ChatColor.RED + "You need permission to use the " + ChatColor.WHITE + parameters.get(1) + ChatColor.RED + " command.");
                return;
            }
        }

        if ((parameters.size() < minParameters && minParameters != 0) || (parameters.size() > maxParameters) && maxParameters != 0) {
            usageError();
            return;
        }

        perform();
    }

    /** Perform this command by sending the CTPCommand data to the corresponding Command. This should not be called outside of execute(sender, parameters). */
    protected abstract void perform();

    /** Send a message to the command sender saying they've got the parameters wrong, and give them a reminder of how the command should be written. */
    protected final void usageError() {
        sendMessage(ChatColor.AQUA + "Try: " + ChatColor.WHITE + usageTemplate);
    }
}
