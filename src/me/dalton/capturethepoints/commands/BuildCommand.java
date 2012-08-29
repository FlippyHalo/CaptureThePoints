package me.dalton.capturethepoints.commands;

import java.io.File;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import me.dalton.capturethepoints.*;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.BlockFace;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

public class BuildCommand extends CTPCommand {

    // Kj -- This could be broken down further, perhaps into a new package compeltely.
    public BuildCommand(CaptureThePoints instance) {
        super.ctp = instance;
        super.aliases.add("build");
        super.aliases.add("create");
        super.aliases.add("make");
        super.aliases.add("b");
        super.notOpCommand = false;
        super.requiredPermissions = new String[]{"ctp.*", "ctp.admin",
            "ctp.admin.setpoint", "ctp.admin.removepoint", "ctp.admin.create", "ctp.admin.delete", "ctp.admin.selectarena",
            "ctp.admin.setarena", "ctp.admin.setlobby", "ctp.admin.arenalist", "ctp.admin.pointlist", "ctp.admin.setboundary",
            "ctp.admin.maximumplayers", "ctp.admin.minimumplayers" , "ctp.admin.save", "ctp.admin.restore"};
        super.senderMustBePlayer = true;
        super.minParameters = 2;
        super.maxParameters = 99;    // Lol cant make in the other way
        super.usageTemplate = "/ctp build [help] [pagenumber]";
    }

    @Override
    public void perform() {
        int size = parameters.size();
        // ctp = parameters.get(0)
        // build = parameters.get(1)
        String arg = size > 2 ? parameters.get(2) : "help"; // Kj -- grab the arguments with null -> empty checking. If only /ctp build, assume help.
        String arg2 = size > 3 ? parameters.get(3) : "";
        String arg3 = size > 4 ? parameters.get(4) : "";
        if (arg.equals("1")) { // ctp build 1
            arg = "help";
            arg2 = "1";
        } else if(arg.equals("2")) {  // ctp build 2
            arg = "help";
            arg2 = "2";
        }else if(arg.equals("3")){
            arg = "help";
            arg2 = "3";
        }

        if (arg.equalsIgnoreCase("help")){
            String pagenumber = arg2;
            if (pagenumber.isEmpty() || pagenumber.equals("1")) {
                sendMessage(ChatColor.RED + "CTP Build Commands: " + ChatColor.GOLD + " Page 1/3");
                sendMessage(ChatColor.DARK_GREEN + "/ctp b help [pagenumber] " + ChatColor.WHITE + "- view this menu.");

                if (ctp.canAccess(player, false, new String[]{"ctp.*", "ctp.admin", "ctp.admin.arenalist"})) {
                    sendMessage(ChatColor.GREEN + "/ctp b arenalist " + ChatColor.WHITE + "- show list of existing arenas");
                }
                if (ctp.canAccess(player, false, new String[]{"ctp.*", "ctp.admin", "ctp.admin.create"})) {
                    sendMessage(ChatColor.GREEN + "/ctp b create <Arena name> " + ChatColor.WHITE + "- create an arena");
                }
                if (ctp.canAccess(player, false, new String[]{"ctp.*", "ctp.admin", "ctp.admin.delete"})) {
                    sendMessage(ChatColor.GREEN + "/ctp b delete <Arena name> " + ChatColor.WHITE + "- delete an existing arena");
                }
                if (ctp.canAccess(player, false, new String[]{"ctp.*", "ctp.admin", "ctp.admin.maximumplayers"})) {
                    sendMessage(ChatColor.GREEN + "/ctp b maximumplayers <number> " + ChatColor.WHITE + "- set maximum players of the arena");
                }
                if (ctp.canAccess(player, false, new String[]{"ctp.*", "ctp.admin", "ctp.admin.minimumplayers"})) {
                    sendMessage(ChatColor.GREEN + "/ctp b minimumplayers <number> " + ChatColor.WHITE + "- set minimum players of the arena");
                }
                if (ctp.canAccess(player, false, new String[]{"ctp.*", "ctp.admin", "ctp.admin.pointlist"})) {
                    sendMessage(ChatColor.GREEN + "/ctp b pointlist " + ChatColor.WHITE + "- shows selected arena capture points list");
                }
                if (ctp.canAccess(player, false, new String[]{"ctp.*", "ctp.admin", "ctp.admin.removepoint"})) {
                    sendMessage(ChatColor.GREEN + "/ctp b removepoint <Point name> " + ChatColor.WHITE + "- removes an existing capture point");
                }
            } 
            else if (pagenumber.equals("2"))
            {
                sendMessage(ChatColor.RED + "CTP Build Commands: " + ChatColor.GOLD + " Page 2/3");
                if (ctp.canAccess(player, false, new String[]{"ctp.*", "ctp.admin.removespawn", "ctp.admin"})) {
                    sendMessage(ChatColor.GREEN + "/ctp b removespawn <Team color> " + ChatColor.WHITE + "- removes spawn point of selected color");
                }
                if (ctp.canAccess(player, false, new String[]{"ctp.*", "ctp.admin", "ctp.admin.selectarena"})) {
                	ctp.sendMessage(player, ChatColor.GREEN + "/ctp b selectarena <Arena name> " + ChatColor.WHITE + "- selects arena for editing");
                }
                if (ctp.canAccess(player, false, new String[]{"ctp.*", "ctp.admin", "ctp.admin.setarena"})) {
                	ctp.sendMessage(player, ChatColor.GREEN + "/ctp b setarena <Arena name> " + ChatColor.WHITE + "- sets main arena for playing");
                }
                if (ctp.canAccess(player, false, new String[]{"ctp.*", "ctp.admin", "ctp.admin.setboundary"})) {
                	ctp.sendMessage(player, ChatColor.GREEN + "/ctp b setboundary <1 | 2> " + ChatColor.WHITE + "- sets boundary (1 or 2) of the arena");
                }
                if (ctp.canAccess(player, false, new String[]{"ctp.*", "ctp.admin", "ctp.admin.setlobby"})) {
                	ctp.sendMessage(player, ChatColor.GREEN + "/ctp b setlobby " + ChatColor.WHITE + "- sets arena lobby");
                }
                if (ctp.canAccess(player, false, new String[]{"ctp.*", "ctp.admin", "ctp.admin.setpoint"})) {
                	ctp.sendMessage(player, ChatColor.GREEN + "/ctp b setpoint <Point name> <vert | hor> [teams which can't capture]" + ChatColor.WHITE + "- creates new capture point");
                }
                if (ctp.canAccess(player, false, new String[]{"ctp.*", "ctp.admin.setspawn", "ctp.admin"})) {
                	ctp.sendMessage(player, ChatColor.GREEN + "/ctp b setspawn <Team color> " + ChatColor.WHITE + "- sets the place people are teleported to when they die or when they join the game");
                }
            }
            else if(pagenumber.equals("3")) {
            	ctp.sendMessage(player, ChatColor.RED + "CTP Build Commands: " + ChatColor.GOLD + " Page 3/3");
            	
                if (ctp.canAccess(player, false, new String[]{"ctp.*", "ctp.admin.save", "ctp.admin"})) {
                	ctp.sendMessage(player, ChatColor.GREEN + "/ctp b save " + ChatColor.WHITE + "- saves selected for editing arena data to mySQL database");
                }
                
                if (ctp.canAccess(player, false, new String[]{"ctp.*", "ctp.admin.restore", "ctp.admin"})) {
                	ctp.sendMessage(player, ChatColor.GREEN + "/ctp b restore " + ChatColor.WHITE + "- restores arena from mySQL database");
                }
                
                ctp.sendMessage(player, ChatColor.GREEN + "/ctp b findchests <arena name>" + ChatColor.WHITE + "- shows all chests in arena");
            }
            return;
        }

        // Kj -- if the arena being edited is null, make a new one to avoid NPEs.
        if (ctp.editingArena == null) {
            ctp.editingArena = new ArenaData();
        }
        // Kj -- if the mainArena is null, make a new one to avoid NPEs.
        if (ctp.mainArena == null) {
            ctp.mainArena = new ArenaData();
        }

        if (arg.equalsIgnoreCase("setspawn")) {
            if (ctp.canAccess(player, false, new String[]{"ctp.*", "ctp.admin.setspawn", "ctp.admin"})) {
                if (parameters.size() < 4) {
                	ctp.sendMessage(player, ChatColor.WHITE + "Usage: " + ChatColor.GREEN + "/ctp build setspawn <Team color> ");
                    return;
                }
                if (ctp.editingArena == null || ctp.editingArena.name.isEmpty()) {
                	ctp.sendMessage(player, ChatColor.RED + "No arena selected!");
                    return;
                }
                Location loc = player.getLocation();

                File arenaFile = new File(CaptureThePoints.mainDir + File.separator + "Arenas" + File.separator + ctp.editingArena.name + ".yml");
                FileConfiguration arenaConf = YamlConfiguration.loadConfiguration(arenaFile);

                if ((arenaConf.getString("World") != null) && (!arenaConf.getString("World").equals(player.getWorld().getName()))) {
                	ctp.sendMessage(player, ChatColor.RED + "Please build all arena team spawns in the same world ---->" + ChatColor.GREEN + arenaConf.getString("World"));
                    return;
                }

                if ((arg2.equalsIgnoreCase("white")) || (arg2.equalsIgnoreCase("lightgray")) || (arg2.equalsIgnoreCase("gray")) || (arg2.equalsIgnoreCase("black"))
                		|| (arg2.equalsIgnoreCase("red")) || (arg2.equalsIgnoreCase("orange")) || (arg2.equalsIgnoreCase("yellow"))
                        || (arg2.equalsIgnoreCase("lime")) || (arg2.equalsIgnoreCase("green")) || (arg2.equalsIgnoreCase("blue"))
                        || (arg2.equalsIgnoreCase("cyan")) || (arg2.equalsIgnoreCase("lightblue")) || (arg2.equalsIgnoreCase("purple"))
                        || (arg2.equalsIgnoreCase("pink")) || (arg2.equalsIgnoreCase("magenta")) || (arg2.equalsIgnoreCase("brown"))) {
                    Spawn spawn = new Spawn();
                    spawn.name = arg2.toLowerCase();
                    spawn.x = Double.valueOf(loc.getX()).doubleValue();
                    spawn.y = Double.valueOf(loc.getY()).doubleValue();
                    spawn.z = Double.valueOf(loc.getZ()).doubleValue();
                    spawn.dir = loc.getYaw();

                    String aWorld = arenaConf.getString("World");
                    if (aWorld == null) {
                        arenaConf.addDefault("World", player.getWorld().getName());
                    } 
                    else if (!aWorld.equals(player.getWorld().getName())) {
                    	ctp.sendMessage(player, ChatColor.RED + "Please build arena lobby in same world as its spawns and capture points!");
                        return;
                    }
                    arenaConf.addDefault("Team-Spawns." + arg2 + ".X", Double.valueOf(loc.getX()));
                    arenaConf.addDefault("Team-Spawns." + arg2 + ".Y", Double.valueOf(loc.getY()));
                    arenaConf.addDefault("Team-Spawns." + arg2 + ".Z", Double.valueOf(loc.getZ()));
                    arenaConf.addDefault("Team-Spawns." + arg2 + ".Dir", Double.valueOf(spawn.dir));
                    try {
                        arenaConf.options().copyDefaults(true);
                        arenaConf.save(arenaFile);
                    } catch (IOException ex) {
                        Logger.getLogger(BuildCommand.class.getName()).log(Level.SEVERE, null, ex);
                    }

                    if (ctp.mainArena.world == null) {
                        //ctp.mainArena = new ArenaData();
                        ctp.mainArena.world = player.getWorld().getName();
                        ctp.mainArena.name = ctp.editingArena.name;
                    }
                    if (ctp.mainArena.world.equals(player.getWorld().getName())) {
                        ctp.mainArena.teamSpawns.put(arg2, spawn);
                        Team team = new Team();
                        team.spawn = spawn;
                        team.color = arg2;
                        team.memberCount = 0;
                        try {
                            team.chatcolor = ChatColor.valueOf(spawn.name.toUpperCase()); // Kj -- init teamchat colour
                        } catch (Exception ex) {
                            team.chatcolor = ChatColor.GREEN;
                        }
                        // Check if this spawn is already in the list
                        boolean hasTeam = false;

                        for (Team aTeam : ctp.mainArena.teams) {
                            if (aTeam.color.equalsIgnoreCase(arg2)) {
                                hasTeam = true;
                                //ctp.teams.remove(aTeam);
                            }
                        }

                        if (!hasTeam) {
                            ctp.mainArena.teams.add(team);
                        }
                    }
                    ctp.sendMessage(player, "You set the " + ChatColor.GREEN + arg2 + ChatColor.WHITE + " team spawn point.");
                    return;
                }

                ctp.sendMessage(player, ChatColor.RED + "There is no such color!");
                return;
            }
            ctp.sendMessage(player, ChatColor.RED + "You do not have permission to do that.");
            return;
        }

        if (arg.equalsIgnoreCase("removespawn")) {
            if (ctp.canAccess(player, false, new String[]{"ctp.*", "ctp.admin.removespawn", "ctp.admin"})) {
                if (parameters.size() < 4) {
                	ctp.sendMessage(player, ChatColor.WHITE + "Usage: " + ChatColor.GREEN + "/ctp build removespawn <Team color> ");
                    return;
                }
                arg2 = arg2.toLowerCase();

                if (ctp.editingArena == null || ctp.editingArena.name.isEmpty()) {
                	ctp.sendMessage(player, ChatColor.RED + "No arena selected!");
                    return;
                }

                File arenaFile = new File("plugins/CaptureThePoints" + File.separator + "Arenas" + File.separator + ctp.editingArena.name + ".yml");
                FileConfiguration arenaConf = YamlConfiguration.loadConfiguration(arenaFile);

                if (arenaConf.getString("Team-Spawns." + arg2 + ".X") == null) {
                	ctp.sendMessage(player, ChatColor.RED + "This arena spawn does not exist! -----> " + ChatColor.GREEN + arg2);
                    return;
                }
                arenaConf.set("Team-Spawns." + arg2, null);
                try {
                    arenaConf.options().copyDefaults(true);
                    arenaConf.save(arenaFile);
                } catch (IOException ex) {
                    Logger.getLogger(BuildCommand.class.getName()).log(Level.SEVERE, null, ex);
                }

                if (ctp.editingArena.name.equalsIgnoreCase(ctp.mainArena.name)) {
                    ctp.mainArena.teamSpawns.remove(arg2);
                }
                for (int i = 0; i < ctp.mainArena.teams.size(); i++) {
                    if (!ctp.mainArena.teams.get(i).color.equals(arg2)) {
                        continue;
                    }
                    ctp.mainArena.teams.remove(i);
                    break;
                }

                ctp.sendMessage(player, ChatColor.GREEN + arg2 + " " + ChatColor.WHITE + "spawn was removed.");
                return;
            }
            ctp.sendMessage(player, ChatColor.RED + "You do not have permission to do that.");
            return;
        }

        if (arg.equalsIgnoreCase("setpoint")) {
            if (ctp.canAccess(player, false, new String[]{"ctp.*", "ctp.admin", "ctp.admin.setpoint"})) {
                if (parameters.size() < 5) {
                	ctp.sendMessage(player, ChatColor.WHITE + "Usage: " + ChatColor.GREEN + "/ctp build setpoint <Point name> <vert | hor>");
                    return;
                }
                arg2 = arg2.toLowerCase();
                arg3 = arg3.toLowerCase();

                if ((!arg3.equals("vert")) && (!arg3.equals("hor"))) {
                	ctp.sendMessage(player, ChatColor.RED + "Points can be vertical or horizontal: " + ChatColor.GREEN + "vert | hor");
                    return;
                }

                if (ctp.editingArena == null || ctp.editingArena.name.isEmpty()) {
                	ctp.sendMessage(player, ChatColor.RED + "No arena selected!");
                    return;
                }
                CTPPoints tmps = new CTPPoints();
                tmps.name = arg2;
                Location loc = player.getLocation();
                int start_x;
                tmps.x = (start_x = loc.getBlockX());
                int start_y;
                tmps.y = (start_y = loc.getBlockY());
                int start_z;
                tmps.z = (start_z = loc.getBlockZ());

                File arenaFile = new File(CaptureThePoints.mainDir + File.separator + "Arenas" + File.separator + ctp.editingArena.name + ".yml");
                FileConfiguration arenaConf = YamlConfiguration.loadConfiguration(arenaFile);

                if ((arenaConf.getString("World") != null) && (!arenaConf.getString("World").equals(player.getWorld().getName()))) {
                    ctp.sendMessage(player, ChatColor.RED + "Please build all arena points in same world ----> " + ChatColor.GREEN + arenaConf.getString("World"));
                    return;
                }

                for (CTPPoints point : ctp.editingArena.capturePoints) {
                    Location protectionPoint = new Location(player.getWorld(), point.x, point.y, point.z);
                    double distance = player.getLocation().distance(protectionPoint);
                    if (distance < 5.0D) {
                    	ctp.sendMessage(player, ChatColor.RED + "You are trying to build too close to another point!"); // Kj to -> too
                        return;
                    }
                }

                if (arg3.equals("vert")) {
                    double yaw = loc.getYaw();

                    while (yaw < 0.0D) {
                        yaw += 360.0D;
                    }
                    BlockFace direction;
                    if ((yaw > 315.0D) || (yaw <= 45.0D)) {
                        direction = BlockFace.WEST;
                    } else {
                        if ((yaw > 45.0D) && (yaw <= 135.0D)) {
                            direction = BlockFace.NORTH;
                        } else {
                            if ((yaw > 135.0D) && (yaw <= 225.0D)) {
                                direction = BlockFace.EAST;
                            } else {
                                direction = BlockFace.SOUTH;
                            }
                        }
                    }
                    switch (direction) {
                        case NORTH:
                            Util.buildVert(player, start_x, start_y - 1, start_z - 1, 2, 4, 4, ctp.globalConfigOptions.ringBlock);
                            player.getWorld().getBlockAt(start_x, start_y, start_z).setTypeId(0);
                            player.getWorld().getBlockAt(start_x, start_y + 1, start_z).setTypeId(0);
                            player.getWorld().getBlockAt(start_x, start_y, start_z + 1).setTypeId(0);
                            player.getWorld().getBlockAt(start_x, start_y + 1, start_z + 1).setTypeId(0);
                            arenaConf.addDefault("Points." + arg2 + ".Dir", "NORTH");
                            tmps.pointDirection = "NORTH";
                            break;
                        case EAST:
                            Util.buildVert(player, start_x - 1, start_y - 1, start_z, 4, 4, 2, ctp.globalConfigOptions.ringBlock);
                            player.getWorld().getBlockAt(start_x, start_y, start_z).setTypeId(0);
                            player.getWorld().getBlockAt(start_x, start_y + 1, start_z).setTypeId(0);
                            player.getWorld().getBlockAt(start_x + 1, start_y, start_z).setTypeId(0);
                            player.getWorld().getBlockAt(start_x + 1, start_y + 1, start_z).setTypeId(0);
                            arenaConf.addDefault("Points." + arg2 + ".Dir", "EAST");
                            tmps.pointDirection = "EAST";
                            break;
                        case SOUTH:
                            Util.buildVert(player, start_x - 1, start_y - 1, start_z - 1, 2, 4, 4, ctp.globalConfigOptions.ringBlock);
                            player.getWorld().getBlockAt(start_x, start_y, start_z).setTypeId(0);
                            player.getWorld().getBlockAt(start_x, start_y + 1, start_z).setTypeId(0);
                            player.getWorld().getBlockAt(start_x, start_y, start_z + 1).setTypeId(0);
                            player.getWorld().getBlockAt(start_x, start_y + 1, start_z + 1).setTypeId(0);
                            arenaConf.addDefault("Points." + arg2 + ".Dir", "SOUTH");
                            tmps.pointDirection = "SOUTH";
                            break;
                        case WEST:
                            Util.buildVert(player, start_x - 1, start_y - 1, start_z - 1, 4, 4, 2, ctp.globalConfigOptions.ringBlock);
                            player.getWorld().getBlockAt(start_x, start_y, start_z).setTypeId(0);
                            player.getWorld().getBlockAt(start_x, start_y + 1, start_z).setTypeId(0);
                            player.getWorld().getBlockAt(start_x + 1, start_y, start_z).setTypeId(0);
                            player.getWorld().getBlockAt(start_x + 1, start_y + 1, start_z).setTypeId(0);
                            arenaConf.addDefault("Points." + arg2 + ".Dir", "WEST");
                            tmps.pointDirection = "WEST";
                    }
                }

                if (arg3.equals("hor")) {
                    for (int x = start_x + 2; x >= start_x - 1; x--) {
                        for (int y = start_y - 1; y <= start_y; y++) {
                            for (int z = start_z - 1; z <= start_z + 2; z++) {
                                player.getWorld().getBlockAt(x, y, z).setTypeId(ctp.globalConfigOptions.ringBlock);
                            }
                        }
                    }

                    player.getWorld().getBlockAt(start_x, start_y, start_z).setTypeId(0);
                    player.getWorld().getBlockAt(start_x + 1, start_y, start_z).setTypeId(0);
                    player.getWorld().getBlockAt(start_x + 1, start_y, start_z + 1).setTypeId(0);
                    player.getWorld().getBlockAt(start_x, start_y, start_z + 1).setTypeId(0);
                }

                String aWorld = arenaConf.getString("World");
                if (aWorld == null) {
                    arenaConf.addDefault("World", player.getWorld().getName());
                } else if (!aWorld.equals(player.getWorld().getName())) {
                    sendMessage(ChatColor.RED + "Please build arena lobby in same world as its spawns and capture points!");
                    return;
                } 

                // save arena point data
                if(parameters.size() > 5) {
                    tmps.notAllowedToCaptureTeams = new ArrayList<String>();
                    String colors = "";
                    for(int i = 5; i < parameters.size(); i++) {
                        tmps.notAllowedToCaptureTeams.add(parameters.get(i).toLowerCase());
                        colors = colors + parameters.get(i) + ", ";
                    }
                    
                    colors = colors.substring(0, colors.length() - 2);
                    arenaConf.addDefault("Points." + arg2 + ".NotAllowedToCaptureTeams", colors);
                } else {
                    tmps.notAllowedToCaptureTeams = null;
                }

                arenaConf.addDefault("Points." + arg2 + ".X", Double.valueOf(tmps.x));
                arenaConf.addDefault("Points." + arg2 + ".Y", Double.valueOf(tmps.y));
                arenaConf.addDefault("Points." + arg2 + ".Z", Double.valueOf(tmps.z));
                try {
                    arenaConf.options().copyDefaults(true);
                    arenaConf.save(arenaFile);
                } catch (IOException ex) {
                    Logger.getLogger(BuildCommand.class.getName()).log(Level.SEVERE, null, ex);
                }

                if (ctp.mainArena.world == null) {
                    ctp.mainArena.world = player.getWorld().getName();
                    ctp.mainArena.name = ctp.editingArena.name;
                }

                if (ctp.mainArena.name.equals(ctp.editingArena.name))
                {
                    ctp.mainArena.capturePoints.add(tmps);
                }
                sendMessage(ChatColor.WHITE + "You created capture point -----> " + ChatColor.GREEN + arg2);
                return;
            }
            sendMessage(ChatColor.RED + "You do not have permission to do that.");
            return;
        }

        if (arg.equalsIgnoreCase("removepoint")) {
            if (ctp.canAccess(player, false, new String[]{"ctp.*", "ctp.admin", "ctp.admin.removepoint"})) {
                if (parameters.size() < 4) {
                    sendMessage(ChatColor.WHITE + "Usage: " + ChatColor.GREEN + "/ctp build removepoint <Point name>");
                    return;
                }
                arg2 = arg2.toLowerCase();
                if (ctp.editingArena == null || ctp.editingArena.name.isEmpty()) {
                    sendMessage(ChatColor.RED + "No arena selected!");
                    return;
                }

                File arenaFile = new File(CaptureThePoints.mainDir + File.separator + "Arenas" + File.separator + ctp.editingArena.name + ".yml");
                FileConfiguration arenaConf = YamlConfiguration.loadConfiguration(arenaFile);

                if (arenaConf.getString("Points." + arg2 + ".X") == null) {
                    sendMessage(ChatColor.RED + "This arena point does not exist! -----> " + ChatColor.GREEN + arg2);
                    return;
                }
                if ((arenaConf.getConfigurationSection("Points").getKeys(false).size() == 1) && (!arenaConf.contains("Team-Spawns")))
                {
                    arenaConf.set("World", null);
                }
                int start_x = arenaConf.getInt("Points." + arg2 + ".X", 0);
                int start_y = arenaConf.getInt("Points." + arg2 + ".Y", 0);
                int start_z = arenaConf.getInt("Points." + arg2 + ".Z", 0);

                // Kj -- s -> aPoint
                if (ctp.mainArena.name.equals(player.getWorld().getName())) {
                    for (CTPPoints aPoint : ctp.mainArena.capturePoints) {
                        if (aPoint.name.equalsIgnoreCase(arg2)) {
                            ctp.mainArena.capturePoints.remove(aPoint);
                            break;
                        }
                    }
                }
                //Remove blocks
                if (arenaConf.getString("Points." + arg2 + ".Dir") == null) {
                    for (int x = start_x + 2; x >= start_x - 1; x--) {
                        for (int y = start_y - 1; y <= start_y; y++) {
                            for (int z = start_z - 1; z <= start_z + 2; z++) {
                                if (player.getWorld().getBlockAt(x, y, z).getTypeId() == ctp.globalConfigOptions.ringBlock) {
                                    player.getWorld().getBlockAt(x, y, z).setTypeId(0);
                                }
                            }
                        }
                    }
                } else {
                    String direction = arenaConf.getString("Points." + arg2 + ".Dir");
                    Util.removeVertPoint(player, direction, start_x, start_y, start_z, ctp.globalConfigOptions.ringBlock);
                }

                arenaConf.set("Points." + arg2, null);
                try {
                    arenaConf.options().copyDefaults(true);
                    arenaConf.save(arenaFile);
                } catch (IOException ex) {
                    Logger.getLogger(BuildCommand.class.getName()).log(Level.SEVERE, null, ex);
                }
                sendMessage(ChatColor.WHITE + "You removed capture point -----> " + ChatColor.GREEN + arg2);

                return;
            }
            sendMessage(ChatColor.RED + "You do not have permission to do that.");
            return;
        }

        if (arg.equalsIgnoreCase("create")) {
            if (ctp.canAccess(player, false, new String[]{"ctp.*", "ctp.admin", "ctp.admin.create"})) {
                if (parameters.size() < 4) {
                    sendMessage(ChatColor.WHITE + "Usage: " + ChatColor.GREEN + "/ctp build create <Arena name>");
                    return;
                }
                arg2 = arg2.toLowerCase();
                if (ctp.arena_list.contains(arg2)) {
                    sendMessage(ChatColor.RED + "This arena already exists! -----> " + ChatColor.GREEN + arg2); // Kj -- typo
                    return;
                }
                ctp.editingArena.name = arg2;
                FileConfiguration config = ctp.load();
                //Seting main arena if this is first arena

                if (!config.contains("Arena"))
                {
                    config.addDefault("Arena", arg2);
                    try {
                        config.options().copyDefaults(true);
                        config.save(CaptureThePoints.globalConfigFile);
                    } catch (IOException ex) {
                        Logger.getLogger(BuildCommand.class.getName()).log(Level.SEVERE, null, ex);
                    }

                    ctp.mainArena = new ArenaData();
                    ctp.mainArena.name = arg2;
                    ctp.mainArena.world = null;
                }

                ctp.arena_list.add(arg2);
                sendMessage("You created arena: " + ChatColor.GREEN + arg2);

                return;
            }
            sendMessage(ChatColor.RED + "You do not have permission to do that.");
            return;
        }

        if (arg.equalsIgnoreCase("delete")) {
            if (ctp.canAccess(player, false, new String[]{"ctp.*", "ctp.admin", "ctp.admin.delete"})) {
                if (parameters.size() < 4) {
                    sendMessage(ChatColor.WHITE + "Usage: " + ChatColor.GREEN + "/ctp build delete <Arena name>");
                    return;
                }
                arg2 = arg2.toLowerCase();
                if (!ctp.arena_list.contains(arg2)) {
                    sendMessage(ChatColor.RED + "This arena does not exist! -----> " + ChatColor.GREEN + arg2);
                    return;
                }
                if ((ctp.isGameRunning()) && (ctp.mainArena.name.equals(arg2))) {
                    sendMessage(ChatColor.RED + "Cannot delete arena while game is running in it!");
                    return;
                }
                File arenaFile = new File("plugins/CaptureThePoints" + File.separator + "Arenas" + File.separator + arg2 + ".yml");

                //Remove blocks
                FileConfiguration arenaConf = YamlConfiguration.loadConfiguration(arenaFile);
                FileConfiguration config = ctp.load();

                if (arenaConf.getString("Points") != null) {
                    for (String str : arenaConf.getConfigurationSection("Points").getKeys(false))
                    {
                        str = "Points." + str;
                        int start_x = arenaConf.getInt(str + ".X", 0);
                        int start_y = arenaConf.getInt(str + ".Y", 0);
                        int start_z = arenaConf.getInt(str + ".Z", 0);

                        if (arenaConf.getString(str + ".Dir") == null) {
                            for (int x = start_x + 2; x >= start_x - 1; x--) {
                                for (int y = start_y - 1; y <= start_y; y++) {
                                    for (int z = start_z - 1; z <= start_z + 2; z++) {
                                        if (player.getWorld().getBlockAt(x, y, z).getTypeId() == ctp.globalConfigOptions.ringBlock) {
                                            player.getWorld().getBlockAt(x, y, z).setTypeId(0);
                                        }
                                    }
                                }
                            }
                        } else {
                            String direction = arenaConf.getString(str + ".Dir");
                            Util.removeVertPoint(player, direction, start_x, start_y, start_z, ctp.globalConfigOptions.ringBlock);
                        }
                    }
                }

                //Delete mysql data
                ctp.arenaRestore.arenaToDelete = arg2;
                if(ctp.globalConfigOptions.enableHardArenaRestore) {
                    ctp.getServer().getScheduler().scheduleAsyncDelayedTask(ctp, new Runnable() {
                        public void run () {
                            ctp.mysqlConnector.connectToMySql();
                            ctp.arenaRestore.deleteArenaData(ctp.arenaRestore.arenaToDelete);
                            ctp.arenaRestore.arenaToDelete = null;
                        }
                    }, 5L);
                }

                arenaFile.delete();
                ctp.arena_list.remove(arg2);
                if (arg2.equals(ctp.mainArena.name)) {
                    ctp.arenasBoundaries.remove(ctp.mainArena.name);
                    ctp.mainArena.teams.clear();
                    ctp.mainArena = null;

                    config.set("Arena", null);
                    try {
                        config.options().copyDefaults(true);
                        config.save(CaptureThePoints.globalConfigFile);
                    } catch (IOException ex) {
                        Logger.getLogger(BuildCommand.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
                if (arg2.equals(ctp.editingArena.name)) {
                    ctp.editingArena = null;
                }
                sendMessage("You deleted arena: " + ChatColor.GREEN + arg2);

                return;
            }
            sendMessage(ChatColor.RED + "You do not have permission to do that.");
            return;
        }
        //  sets arena for editing/creating
        if (arg.equalsIgnoreCase("selectarena")) {
            if (ctp.canAccess(player, false, new String[]{"ctp.*", "ctp.admin", "ctp.admin.selectarena"})) {
                if (parameters.size() < 4) {
                    sendMessage(ChatColor.WHITE + "Usage: " + ChatColor.GREEN + "/ctp build selectarena <Arena name>");
                    return;
                }
                arg2 = arg2.toLowerCase();
                if (!ctp.arena_list.contains(arg2)) {
                    sendMessage(ChatColor.RED + "This arena does not exist! -----> " + ChatColor.GREEN + arg2);
                    return;
                }
                ctp.editingArena = ctp.loadArena(arg2);
                ctp.editingArena.name = arg2;

                sendMessage(ChatColor.WHITE + "Arena selected for editing: " + ChatColor.GREEN + arg2);

                return;
            }
            sendMessage(ChatColor.RED + "You do not have permission to do that.");
            return;
        }

        if (arg.equalsIgnoreCase("setarena")) {
            if (ctp.canAccess(player, false, new String[]{"ctp.*", "ctp.admin", "ctp.admin.setarena"})) {
                if (parameters.size() < 4) {
                    sendMessage(ChatColor.WHITE + "Usage: " + ChatColor.GREEN + "/ctp build setarena <Arena name>");
                    return;
                }
                arg2 = arg2.toLowerCase();
                if (!ctp.arena_list.contains(arg2)) {
                    sendMessage(ChatColor.RED + "This arena does not exist! -----> " + ChatColor.GREEN + arg2);
                    return;
                }

                ArenaData arena = ctp.loadArena(arg2);
                boolean canLoad = true;
                if (arena.capturePoints.size() < 1) {
                    sendMessage(ChatColor.RED + "Please add at least one capture point");
                    canLoad = false;
                }
                if (arena.teamSpawns.size() < 2) {
                    sendMessage(ChatColor.RED + "Please add at least two teams' spawn points");
                    canLoad = false;
                }
                if (arena.lobby == null) {
                    sendMessage(ChatColor.RED + "Please create arena lobby");
                    canLoad = false;
                }
                if ((arena.x1 == 0) && (arena.x2 == 0) && (arena.z1 == 0) && (arena.z2 == 0)) {
                    sendMessage(ChatColor.RED + "Please set arena boundaries");
                    canLoad = false;
                }

                String mainArenaCheckError = ctp.checkMainArena(player, arena); // Kj -- Check arena, if there is an error, an error message is returned.
                if (!mainArenaCheckError.isEmpty() && canLoad) {
                    sendMessage(mainArenaCheckError);
                    return;
                }

                if (canLoad) {
                    FileConfiguration config = ctp.load();
                    config.addDefault("Arena", arg2);
                    try {
                        config.options().copyDefaults(true);
                        config.save(CaptureThePoints.globalConfigFile);
                    } catch (IOException ex) {
                        Logger.getLogger(BuildCommand.class.getName()).log(Level.SEVERE, null, ex);
                    }
                    ctp.mainArena = null;
                    ctp.mainArena = arena;
                    // And to be sure that everything is fine reload all config

                    ctp.clearConfig();
                    ctp.loadConfigFiles();

                    sendMessage(ChatColor.WHITE + "Arena selected for playing: " + ChatColor.GREEN + arg2);
                } else {
                    sendMessage(ChatColor.GREEN + "If you wanted to edit this arena instead, use " +ChatColor.WHITE+ "/ctp b selectarena "+arg2);
                }

                return;
            }
            sendMessage(ChatColor.RED + "You do not have permission to do that.");
            return;
        }

        if (arg.equalsIgnoreCase("setlobby")) {
            if (ctp.canAccess(player, false, new String[]{"ctp.*", "ctp.admin", "ctp.admin.setlobby"})) {
                if (ctp.editingArena == null || ctp.editingArena.name.isEmpty()) {
                    sendMessage(ChatColor.RED + "No arena selected!");
                    return;
                }
                File arenaFile = new File(CaptureThePoints.mainDir + File.separator + "Arenas" + File.separator + ctp.editingArena.name + ".yml");

                FileConfiguration arenaConf = YamlConfiguration.loadConfiguration(arenaFile);

                String aWorld = arenaConf.getString("World");
                if (aWorld == null) {
                    arenaConf.addDefault("World", player.getWorld().getName());
                } else if (!aWorld.equals(player.getWorld().getName())) {
                    sendMessage(ChatColor.RED + "Please build arena lobby in same world as its spawns and capture points!");
                    return;
                }
                // Kj -- changed from CTPoints
                Lobby lobby = new Lobby(
                        player.getLocation().getX(),
                        player.getLocation().getY(),
                        player.getLocation().getZ(),
                        player.getLocation().getYaw());

                ctp.editingArena.lobby = lobby;

                if(ctp.editingArena.name.equalsIgnoreCase(ctp.mainArena.name)) {
                    ctp.mainArena.lobby = lobby;
                }

                arenaConf.addDefault("Lobby.X", Double.valueOf(lobby.x));
                arenaConf.addDefault("Lobby.Y", Double.valueOf(lobby.y));
                arenaConf.addDefault("Lobby.Z", Double.valueOf(lobby.z));
                arenaConf.addDefault("Lobby.Dir", Double.valueOf(lobby.dir));
                
                try {
                    arenaConf.options().copyDefaults(true);
                    arenaConf.save(arenaFile);
                } catch (IOException ex) {
                    Logger.getLogger(BuildCommand.class.getName()).log(Level.SEVERE, null, ex);
                }
                
                sendMessage(ChatColor.GREEN + ctp.editingArena.name + ChatColor.WHITE + " arena lobby created");
                return;
            }
            sendMessage(ChatColor.RED + "You do not have permission to do that.");
            return;
        }

        if (arg.equalsIgnoreCase("arenalist")) {
            if (ctp.canAccess(player, false, new String[]{"ctp.*", "ctp.admin", "ctp.admin.arenalist"})) {
                // Reload arena list (matbe there is a new arena there)

                File file = new File(CaptureThePoints.mainDir + File.separator + "Arenas");
                ctp.loadArenas(file);
                
                String arenas = "";
                boolean firstTime = true;
                for (String arena : ctp.arena_list) {
                    if (firstTime) {
                        arenas = arena;
                        firstTime = false;
                    } else {
                        arenas = arena + ", " + arenas;
                    }
                }
                
                if(arenas.equalsIgnoreCase("")) {
                	sendMessage("There are currently no arenas.");
                	return;
                }else {
                	sendMessage("Arena list:");
                	sendMessage("  " + arenas);
                	return;
                } 
            }
            sendMessage(ChatColor.RED + "You do not have permission to do that.");
            return;
        }

        if (arg.equalsIgnoreCase("pointlist")) {
            if (ctp.canAccess(player, false, new String[]{"ctp.*", "ctp.admin", "ctp.admin.pointlist"})) {
                if (ctp.editingArena == null || ctp.editingArena.name.isEmpty()) {
                    sendMessage(ChatColor.RED + "No arena selected!");
                    return;
                }

                String points = "";
                boolean firstTime = true;

                //Kj -- s -> aPoint
                for (CTPPoints aPoint : ctp.editingArena.capturePoints) {
                    if (firstTime) {
                        points = aPoint.name;
                        firstTime = false;
                    } else {
                        points = aPoint.name + ", " + points;
                    }
                }
                
                if(points.equalsIgnoreCase("")) {
                	sendMessage("There are currently no points.");
                	return;
                }else {
                	sendMessage(ChatColor.GREEN + ctp.editingArena.name + ChatColor.WHITE + " point list:");
                    sendMessage(points);
                    return;
                }
            }
            sendMessage(ChatColor.RED + "You do not have permission to do that.");
            return;
        }

        if (arg.equalsIgnoreCase("setboundary")) {
            if (ctp.canAccess(player, false, new String[]{"ctp.*", "ctp.admin", "ctp.admin.setboundary"})) {
                if (parameters.size() < 4) {
                    ctp.sendMessage(player, ChatColor.WHITE + "Usage: " + ChatColor.GREEN + "/ctp build setboundary <1 | 2>");
                    return;
                }
                if (ctp.editingArena == null || ctp.editingArena.name.isEmpty()) {
                	ctp.sendMessage(player, ChatColor.RED + "No arena selected!");
                    return;
                }

                Location loc = player.getLocation();
                if (arg2.equalsIgnoreCase("1")) {
                    if (ctp.editingArena.name.equalsIgnoreCase(ctp.mainArena.name)) {
                        ctp.mainArena.x1 = loc.getBlockX();
                        ctp.mainArena.y1 = loc.getBlockY();
                        ctp.mainArena.z1 = loc.getBlockZ();
                    }

                    ctp.editingArena.x1 = loc.getBlockX();
                    ctp.editingArena.y1 = loc.getBlockY();
                    ctp.editingArena.z1 = loc.getBlockZ();

                    // Check arena world
                    if(ctp.editingArena.world == null || !ctp.editingArena.world.equalsIgnoreCase(loc.getWorld().getName())) {
                        ctp.editingArena.world = loc.getWorld().getName();
                    }

                    File arenaFile = new File("plugins/CaptureThePoints" + File.separator + "Arenas" + File.separator + ctp.editingArena.name + ".yml");
                    FileConfiguration arenaConf = YamlConfiguration.loadConfiguration(arenaFile);

                    arenaConf.addDefault("World", ctp.editingArena.world);
                    arenaConf.addDefault("Boundarys.X1", Integer.valueOf(loc.getBlockX()));
                    arenaConf.addDefault("Boundarys.Y1", Integer.valueOf(loc.getBlockY()));
                    arenaConf.addDefault("Boundarys.Z1", Integer.valueOf(loc.getBlockZ()));
                    try {
                        arenaConf.options().copyDefaults(true);
                        arenaConf.save(arenaFile);
                    } catch (IOException ex) {
                        Logger.getLogger(BuildCommand.class.getName()).log(Level.SEVERE, null, ex);
                    }

                    // To boundaries property
                    if(ctp.arenasBoundaries.containsKey(ctp.editingArena.name)) {
                        ArenaBoundaries bound = ctp.arenasBoundaries.get(ctp.editingArena.name);
                        bound.world = ctp.editingArena.world;
                        bound.x1 = ctp.editingArena.x1;
                        bound.y1 = ctp.editingArena.y1;
                        bound.z1 = ctp.editingArena.z1;
                    }
                    else {   // New arena
                        ArenaBoundaries bound = new  ArenaBoundaries();
                        bound.world = loc.getWorld().getName();
                        bound.x1 = ctp.editingArena.x1;
                        bound.y1 = ctp.editingArena.y1;
                        bound.z1 = ctp.editingArena.z1;
                        ctp.arenasBoundaries.put(ctp.editingArena.name, bound);
                    }

                    sendMessage("First boundary point set.");
                } else if (arg2.equalsIgnoreCase("2")) {
                    if (ctp.editingArena.name.equalsIgnoreCase(ctp.mainArena.name)) {
                        ctp.mainArena.x2 = loc.getBlockX();
                        ctp.mainArena.y2 = loc.getBlockY();
                        ctp.mainArena.z2 = loc.getBlockZ();
                    }

                    ctp.editingArena.x2 = loc.getBlockX();
                    ctp.editingArena.y2 = loc.getBlockY();
                    ctp.editingArena.z2 = loc.getBlockZ();

                    // Check arena world
                    if(ctp.editingArena.world == null || !ctp.editingArena.world.equalsIgnoreCase(loc.getWorld().getName())) {
                        ctp.editingArena.world = loc.getWorld().getName();
                    }

                    File arenaFile = new File("plugins/CaptureThePoints" + File.separator + "Arenas" + File.separator + ctp.editingArena.name + ".yml");
                    FileConfiguration arenaConf = YamlConfiguration.loadConfiguration(arenaFile);

                    arenaConf.addDefault("World", ctp.editingArena.world);
                    arenaConf.addDefault("Boundarys.X2", Integer.valueOf(loc.getBlockX()));
                    arenaConf.addDefault("Boundarys.Y2", Integer.valueOf(loc.getBlockY()));
                    arenaConf.addDefault("Boundarys.Z2", Integer.valueOf(loc.getBlockZ()));
                    try {
                        arenaConf.options().copyDefaults(true);
                        arenaConf.save(arenaFile);
                    } catch (IOException ex) {
                        Logger.getLogger(BuildCommand.class.getName()).log(Level.SEVERE, null, ex);
                    }

                    // To boundaries property
                    if(ctp.arenasBoundaries.containsKey(ctp.editingArena.name))
                    {
                        ArenaBoundaries bound = ctp.arenasBoundaries.get(ctp.editingArena.name);
                        bound.world = ctp.editingArena.world;
                        bound.x2 = ctp.editingArena.x2;
                        bound.y2 = ctp.editingArena.y2;
                        bound.z2 = ctp.editingArena.z2;
                    } else {   // New arena
                        ArenaBoundaries bound = new  ArenaBoundaries();
                        bound.world = loc.getWorld().getName();
                        bound.x2 = ctp.editingArena.x2;
                        bound.y2 = ctp.editingArena.y2;
                        bound.z2 = ctp.editingArena.z2;
                        ctp.arenasBoundaries.put(ctp.editingArena.name, bound);
                    }

                    sendMessage("Second boundary point set.");
                }

                return;
            }
        }

        // Kj
        if (arg.equalsIgnoreCase("maximumplayers") || arg.equalsIgnoreCase("maxplayers") || arg.equalsIgnoreCase("max")) {
            if (ctp.canAccess(player, false, new String[]{"ctp.*", "ctp.admin", "ctp.admin.maximumplayers"})) {
                if (parameters.size() < 4) {
                    sendMessage(ChatColor.WHITE + "Usage: " + ChatColor.GREEN + "/ctp build maximumplayers <number>");
                    return;
                }
                if (ctp.editingArena == null || ctp.editingArena.name.isEmpty()) {
                    sendMessage(ChatColor.RED + "No arena selected!");
                    return;
                }

                int amount = 0;
                try {
                    amount = Integer.parseInt(arg2);
                } catch (Exception ex) {
                    sendMessage(ChatColor.WHITE + arg2 + " is not a number.");
                    return;
                }

                File arenaFile = new File("plugins/CaptureThePoints" + File.separator + "Arenas" + File.separator + ctp.editingArena.name + ".yml");
                FileConfiguration arenaConf = YamlConfiguration.loadConfiguration(arenaFile);
                arenaConf.addDefault("MaximumPlayers", amount);
                try {
                    arenaConf.options().copyDefaults(true);
                    arenaConf.save(arenaFile);
                } catch (IOException ex) {
                    Logger.getLogger(BuildCommand.class.getName()).log(Level.SEVERE, null, ex);
                }

                ctp.editingArena.maximumPlayers = amount;
                sendMessage(ChatColor.GREEN + "Set maximum players of " + ctp.editingArena.name + " to " + amount + ".");
                return;
            }
            sendMessage(ChatColor.RED + "You do not have permission to do that.");
            return;
        }
        
        // Kj
        if (arg.equalsIgnoreCase("minimumplayers") || arg.equalsIgnoreCase("minplayers") || arg.equalsIgnoreCase("min")) {
            if (ctp.canAccess(player, false, new String[]{"ctp.*", "ctp.admin", "ctp.admin.minimumplayers"})) {
                if (parameters.size() < 4) {
                    sendMessage(ChatColor.WHITE + "Usage: " + ChatColor.GREEN + "/ctp build minimumplayers <number>");
                    return;
                }
                if (ctp.editingArena == null || ctp.editingArena.name.isEmpty()) {
                    sendMessage(ChatColor.RED + "No arena selected!");
                    return;
                }

                int amount = 0;
                try {
                    amount = Integer.parseInt(arg2);
                } catch (Exception ex) {
                    sendMessage(ChatColor.WHITE + arg2 + " is not a number.");
                    return;
                }

                File arenaFile = new File("plugins/CaptureThePoints" + File.separator + "Arenas" + File.separator + ctp.editingArena.name + ".yml");
                FileConfiguration arenaConf = YamlConfiguration.loadConfiguration(arenaFile);
                arenaConf.addDefault("MinimumPlayers", amount);
                try {
                    arenaConf.options().copyDefaults(true);
                    arenaConf.save(arenaFile);
                } catch (IOException ex) {
                    Logger.getLogger(BuildCommand.class.getName()).log(Level.SEVERE, null, ex);
                }

                ctp.editingArena.minimumPlayers = amount;
                sendMessage(ChatColor.GREEN + "Set minimum players of " + ctp.editingArena.name + " to " + amount + ".");
                return;
            }
            sendMessage(ChatColor.RED + "You do not have permission to do that.");
            return;
        }

        if (arg.equalsIgnoreCase("save")) {
            if (ctp.canAccess(player, false, new String[]{"ctp.*", "ctp.admin", "ctp.admin.save"})) {
                if(ctp.globalConfigOptions.enableHardArenaRestore && ctp.editingArena.x2 != 0 && ctp.editingArena.y2 != 0 && ctp.editingArena.z2 != 0 && ctp.editingArena.x1 != 0 && ctp.editingArena.y1 != 0 && ctp.editingArena.z1 != 0) {
                    ctp.getServer().getScheduler().scheduleAsyncDelayedTask(ctp, new Runnable() {
                        public void run () {
                            int xlow = ctp.editingArena.x1;
                            int xhigh = ctp.editingArena.x2;
                            if (ctp.editingArena.x2 < ctp.editingArena.x1) {
                                xlow = ctp.editingArena.x2;
                                xhigh = ctp.editingArena.x1;
                            }
                            
                            int ylow = ctp.editingArena.y1;
                            int yhigh = ctp.editingArena.y2;
                            if (ctp.editingArena.y2 < ctp.editingArena.y1) {
                                ylow = ctp.editingArena.y2;
                                yhigh = ctp.editingArena.y1;
                            }
                            
                            int zlow = ctp.editingArena.z1;
                            int zhigh = ctp.editingArena.z2;
                            if (ctp.editingArena.z2 < ctp.editingArena.z1) {
                                zlow = ctp.editingArena.z2;
                                zhigh = ctp.editingArena.z1;
                            }
                            ctp.mysqlConnector.connectToMySql();

                            ctp.arenaRestore.checkForArena(ctp.editingArena.name, ctp.editingArena.world);
                            World world = ctp.getServer().getWorld(ctp.editingArena.world);

                            Spawn firstPoint = new Spawn();
                            Spawn secondPoint = new Spawn();

                            for (int x = xlow; x <= xhigh; x++) {
                                for (int y = ylow; y <= yhigh; y++) {
                                    boolean first = true; // If it is first block in the stack
                                    int id = -1;
                                    int data = 0;
                                    firstPoint.x = 0; firstPoint.y = 0; firstPoint.z = 0;
                                    secondPoint.x = 0; secondPoint.y = 0; secondPoint.z = 0;
                                    
                                    for (int z = zlow; z <= zhigh; z++) {
                                        if(ctp.arenaRestore.canStackBlocksToMySQL(world.getBlockAt(x, y, z).getTypeId(), id, first, data, world.getBlockAt(x, y, z).getData())) {
                                            if(first) { // First block in the stack
                                                first = false;
                                                id = world.getBlockAt(x, y, z).getTypeId();
                                                data = world.getBlockAt(x, y, z).getData();
                                                firstPoint.x = x; firstPoint.y = y; firstPoint.z = z;
                                                secondPoint.x = x; secondPoint.y = y; secondPoint.z = z;
                                            } else {  // Add one block to stack
                                                secondPoint.z = z;
                                            }
                                        } else { // Cant stack
                                            if(first) { // Only one block to write
                                                firstPoint.x = x; firstPoint.y = y; firstPoint.z = z;
                                                secondPoint.x = x; secondPoint.y = y; secondPoint.z = z;
                                                
                                                ctp.arenaRestore.storeBlock(world.getBlockAt((int)firstPoint.x, (int)firstPoint.y, (int)firstPoint.z), firstPoint, secondPoint, ctp.editingArena.name);
                                                first = true;
                                                id = -1;
                                                data = 0;
                                            } else { // Last block in stack
                                                ctp.arenaRestore.storeBlock(world.getBlockAt((int)firstPoint.x, (int)firstPoint.y, (int)firstPoint.z), firstPoint, secondPoint, ctp.editingArena.name);
                                                
                                                id = world.getBlockAt(x, y, z).getTypeId();
                                                data = world.getBlockAt(x, y, z).getData();
                                                firstPoint.x = x; firstPoint.y = y; firstPoint.z = z;
                                                secondPoint.x = x; secondPoint.y = y; secondPoint.z = z;
                                            }
                                        }
                                    }
                                    
                                    // Check if there is something to write to mySQL
                                    if(!first) {
                                        ctp.arenaRestore.storeBlock(world.getBlockAt(x, y, (int)firstPoint.z), firstPoint, secondPoint, ctp.editingArena.name);
                                    }
                                }
                            }
                            sendMessage("Arena data saved.");
                        }
                    }, 5L);
                    return;
                } else {
                sendMessage(ChatColor.RED + "EnableHardArenaRestore is not enabled or some arena points are not defined. Arena: " + ChatColor.GREEN + ctp.mainArena.name);
                return;
                }
            }
            sendMessage(ChatColor.RED + "You do not have permission to do that.");
            return;
        }

        if (arg.equalsIgnoreCase("restore")) {
            if(!ctp.globalConfigOptions.enableHardArenaRestore) {
                sendMessage(ChatColor.RED + "Hard arena restore is not enabled.");
                return;
            }
            
            if (ctp.canAccess(player, false, new String[]{"ctp.*", "ctp.admin", "ctp.admin.restore"})) {
                ctp.arenaRestore.restoreMySQLBlocks();
                return;
            }
            
            sendMessage(ChatColor.RED + "You do not have permission to do that.");
            return;
        }


        if (arg.equalsIgnoreCase("findchests")) {
            if (!ctp.canAccess(player, false, new String[]{"ctp.*", "ctp.admin"})) {
                sendMessage(ChatColor.RED + "You do not have permission to do that.");
                return;
            }

            String arenaName;
            if(arg2 == null || arg2 == "")
                arenaName = ctp.mainArena.name;

            arenaName = arg2;
            ctp.mysqlConnector.connectToMySql();

            ResultSet rezult = ctp.mysqlConnector.getData("SELECT * FROM Simple_block WHERE Simple_block.arena_name = '" + arenaName + "' AND Simple_block.`block_type` = " + BlockID.CHEST);
            try {
                int chestCount = 0;
                int totalItemsCount = 0;
                System.out.println("------------------------------------------------------------");//60
                System.out.println(String.format("|             Arena name:           %15s        |", arenaName));
                while (rezult.next()) {
                    chestCount++;
                    System.out.println("------------------------------------------------------------");
                    System.out.println(String.format("|             Skrynios NR.:                 %5d          |", chestCount));
                    System.out.println("|----------------------------------------------------------|");
                    System.out.println("| NR. | Daikto pav.   | Kiekis | Patvarumas | Vieta skryn. |");
                    ResultSet rezult2 = ctp.mysqlConnector.getData("SELECT distinct `type` , `durability` , `amount` , `place_in_inv`"
                            + "FROM Simple_block, item WHERE Simple_block.arena_name = '" + arenaName + "' AND Simple_block.`block_type` = " + BlockID.CHEST + " "
                            + "AND item.`block_ID` = " + rezult.getInt("id"));

                    int itemCount = 0;
                    int itemCountInChest = 0;
                    while(rezult2.next()) {
                        if(rezult2.getInt("type") == 0)
                            continue;

                        itemCount++;
                        itemCountInChest = itemCountInChest + rezult2.getInt("amount");
                        System.out.println("|-----+---------------+--------+------------+--------------|");
                        System.out.println(String.format("|%4d |%-15s| %6d | %10d | %12d |", itemCount, Material.getMaterial(rezult2.getInt("type")).name(),
                                rezult2.getInt("amount"), rezult2.getInt("durability"), rezult2.getInt("place_in_inv")));
                    }
                    
                    totalItemsCount = totalItemsCount + itemCountInChest;
                    System.out.println("|-----+----------------------------------------------------|");
                    System.out.println(String.format("|     | Total in chests:         %10d          |", itemCountInChest));
                    System.out.println("------+-----------------------------------------------------\n\n");
                }

                System.out.println("Total chests: " + chestCount);
                System.out.println("Total in chests: " + totalItemsCount);

                sendMessage(ChatColor.GREEN + "Report is ready, please check server consol!");
            } catch (SQLException ex) {
                Logger.getLogger(ArenaRestore.class.getName()).log(Level.SEVERE, null, ex);
            }

        }
    }
}
