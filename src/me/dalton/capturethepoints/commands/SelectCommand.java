package me.dalton.capturethepoints.commands;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import me.dalton.capturethepoints.CaptureThePoints;
import me.dalton.capturethepoints.beans.ArenaData;

import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;

public class SelectCommand extends CTPCommand {
   
    /** Allows admin to select an arena to play. */
    public SelectCommand(CaptureThePoints instance) {
        super.ctp = instance;
        super.aliases.add("setarena");
        super.aliases.add("selectarena");
        super.aliases.add("select");
        super.aliases.add("arena");
        super.notOpCommand = false;
        super.requiredPermissions = new String[]{"ctp.*", "ctp.admin", "ctp.admin.setarena", "ctp.admin.select", "ctp.admin.selectarena"};
        super.senderMustBePlayer = false;
        super.minParameters = 3;
        super.maxParameters = 3;
        super.usageTemplate = "/ctp select <Arena name>";
    }

    @Override
    public void perform() {
        String newarena = parameters.get(2);
        
        if (!ctp.arena_list.contains(newarena)) {
            sendMessage(ChatColor.RED + "Could not load arena " + ChatColor.GOLD + newarena + ChatColor.RED + " because the name cannot be found. Check your config file and existing arenas.");
            return;
        }
        
        ArenaData loadArena = ctp.loadArena(newarena);
        
        if (loadArena == null) {
            sendMessage(ChatColor.RED + "Could not load arena " + ChatColor.GOLD + newarena + ChatColor.RED + " because it isn't finished yet. Check your config file and existing arenas.");
            return;            
        }

        if (ctp.mainArena != null && !ctp.mainArena.getName().isEmpty()) {
            sendMessage(ChatColor.GREEN + "Changed selected arena from " + ctp.mainArena.getName() + " to " + newarena + " to play.");
        } else {
            sendMessage(ChatColor.GREEN + "Selected " + newarena + " for playing.");
        }
        sendMessage(ChatColor.GREEN + "If you wanted to edit this arena instead, use " +ChatColor.WHITE+ "/ctp build selectarena <arena>");
        
        ctp.mainArena = loadArena;

        FileConfiguration config = ctp.load();
        config.addDefault("Arena", newarena);
        try {
            config.options().copyDefaults(true);
            config.save(CaptureThePoints.globalConfigFile);
        } catch (IOException ex) {
            Logger.getLogger(BuildCommand.class.getName()).log(Level.SEVERE, null, ex);
        }

        return;
    }
}