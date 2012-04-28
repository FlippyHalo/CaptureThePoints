package me.dalton.capturethepoints.listeners;

import java.util.HashMap;
import java.util.List;
import me.dalton.capturethepoints.CTPPotionEffect;
import me.dalton.capturethepoints.CaptureThePoints;
import me.dalton.capturethepoints.HealingItems;
import me.dalton.capturethepoints.Items;
import me.dalton.capturethepoints.Spawn;
import me.dalton.capturethepoints.Util;
import org.bukkit.ChatColor;
import org.bukkit.DyeColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.material.Wool;

public class CaptureThePointsEntityListener  implements Listener {

    private final CaptureThePoints ctp;

    public CaptureThePointsEntityListener(CaptureThePoints plugin) {
        this.ctp = plugin;
    }


    @EventHandler(priority = EventPriority.NORMAL)
    public void onEntityExplode(EntityExplodeEvent event)
    {
        if (!ctp.isGameRunning())
            return;
        if(ctp.globalConfigOptions.enableHardArenaRestore)
            return;

        if (ctp.playerListener.isInside(event.getLocation().getBlockX(), ctp.mainArena.x1, ctp.mainArena.x2) && ctp.playerListener.isInside(event.getLocation().getBlockY(), ctp.mainArena.y1, ctp.mainArena.y2) && ctp.playerListener.isInside(event.getLocation().getBlockZ(), ctp.mainArena.z1, ctp.mainArena.z2) && event.getLocation().getWorld().getName().equalsIgnoreCase(ctp.mainArena.world))
        {
            List<Block> explodedBlocks = event.blockList();

            for (Block block : explodedBlocks)
                ctp.arenaRestore.addBlock(block, true);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onEntityDeath(EntityDeathEvent event)
    {
        if (!(event.getEntity() instanceof Player))
            return;
        if((this.ctp.playerData.get((Player) event.getEntity()) == null))
            return;
        if(!ctp.isGameRunning() && this.ctp.playerData.get((Player) event.getEntity()).isInLobby)
        {
//            Player player = (Player) event.getEntity();
            event.setDroppedExp(0);
            event.getDrops().clear();

//            this.ctp.playerData.get((Player) event.getEntity()).isRespawningAfterTrueDeath = true;
//            ctp.playerData.get(player).isInArena = false;
//            ctp.playerData.get(player).isInLobby = false;
//            ctp.mainArena.lobby.playersinlobby.remove(player);
//            ctp.leaveGame(player);
//            player.sendMessage(ChatColor.LIGHT_PURPLE + "[CTP] You left the CTP game.");
            return;
        }

        event.setDroppedExp(0);
        event.getDrops().clear();
//        this.ctp.playerData.get((Player) event.getEntity()).isRespawningAfterTrueDeath = true;

//        Player player = (Player) event.getEntity();
//        respawnPlayer(player, null);
    }
    

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onEntityDamage(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player)) {
            // Kj -- Didn't involve a player. So we don't care.
            return;
        }

        //Only check if game is running
        if (ctp.isGameRunning()) {
            Player attacker = null;
            if ((this.ctp.playerData.get((Player) event.getEntity()) != null)) {

                // for melee
                if (checkForPlayerEvent(event)) {
                    attacker = ((Player) ((EntityDamageByEntityEvent) event).getDamager());
                }

                // for arrows
                if ((event.getCause() == EntityDamageEvent.DamageCause.PROJECTILE) && (((Projectile) ((EntityDamageByEntityEvent) event).getDamager()).getShooter() instanceof Player)) {
                    attacker = (Player) ((Projectile) ((EntityDamageByEntityEvent) event).getDamager()).getShooter();
                }

                Player playa = (Player) event.getEntity();

                // Kj -- helmet checker
                /*boolean helmetRemoved = ctp.playerListener.checkHelmet(attacker);
                if (helmetRemoved)
                {
                ctp.playerListener.fixHelmet(attacker);
                event.setCancelled(true);
                return;
                }*/

                // lobby damage check
                if (this.ctp.playerData.get(playa).isInLobby || (attacker != null && this.ctp.playerData.get(attacker) != null && this.ctp.playerData.get(attacker).isInLobby))
                {
                    event.setCancelled(true);
                    return;
                }

                if (isProtected(playa)) {
                    // If you damage yourself
                    if (attacker != null) {
                        attacker.sendMessage(ChatColor.LIGHT_PURPLE + "You can't damage enemy in their spawn!");
                    }
                    event.setCancelled(true);
                    return;
                }

                //disable pvp damage
                if (attacker != null)
                {
                    if ((this.ctp.playerData.get(playa) != null) && (this.ctp.playerData.get(attacker) != null))
                    {
                        if (this.ctp.playerData.get(playa).team.color.equalsIgnoreCase(this.ctp.playerData.get(attacker).team.color))
                        {
                            attacker.sendMessage(ctp.playerData.get(playa).team.chatcolor + playa.getName() + ChatColor.LIGHT_PURPLE + " is on your team!");
                            event.setCancelled(true);
                            return;
                        } 
                        else
                        {   // This is if there exists something like factions group protection
                            if (event.isCancelled()) {
                                event.setCancelled(false);
                            }
                        }
                    }
                }
//		EntityDamageEvent ede = new EntityDamageEvent((Player)target, EntityDamageEvent.DamageCause.CUSTOM, dmg);
//		Bukkit.getPluginManager().callEvent(ede);
//		if (ede.isCancelled()) return;

                //Player has "died"
                if ((this.ctp.playerData.get(playa) != null) && (playa.getHealth() - event.getDamage() <= 0))
                {
                    event.setCancelled(true);
                    respawnPlayer(playa, attacker);
                }
            }
        }
        if (ctp.playerData.get((Player) event.getEntity()) != null && ctp.playerData.get((Player) event.getEntity()).isInLobby) {
            event.setCancelled(true);
        }
    }

//    public boolean hasLobbyProtection(Player player) {
//        Lobby lobby = ctp.mainArena.lobby;
//        Location protectionPoint = new Location(ctp.getServer().getWorld(ctp.mainArena.world), lobby.x, lobby.y, lobby.z);
//        double distance = player.getLocation().distance(protectionPoint);
//
//        return distance <= ctp.configOptions.protectionDistance;
//    }
    
    private boolean checkForPlayerEvent(EntityDamageEvent event) {
        if (!(event instanceof EntityDamageByEntityEvent)) {
            return false;
        }
        // You now know the player getting damaged was damaged by another entity
        if (!(((EntityDamageByEntityEvent) event).getDamager() instanceof Player)) {
            return false;
        }
        // You now know the entity that is attacking is a player
        return true;
    }
    
    private boolean dropWool(Player player) {
        if (!ctp.mainArena.co.dropWoolOnDeath) {
            return false;
        }

        PlayerInventory inv = player.getInventory();
        int ownedWool = 0;
        for (ItemStack item : inv.getContents()) {
            if (item != null && item.getTypeId() == 35) {
                if (!((Wool) item.getData()).getColor().toString().equalsIgnoreCase(ctp.playerData.get(player).team.color)) {
                    inv.remove(35);
                    ItemStack tmp = new ItemStack(item.getType(), item.getAmount(), (short) ((Wool) item.getData()).getColor().getData());
                    player.getWorld().dropItem(player.getLocation(), tmp);
                } else {
                    ownedWool += item.getAmount();
                }
            }
        }
        inv.remove(Material.WOOL);
        if (ownedWool != 0) {
            DyeColor color = DyeColor.valueOf(ctp.playerData.get(player).team.color.toUpperCase());
            ItemStack wool = new ItemStack(35, ownedWool, color.getData());
            player.getInventory().addItem(new ItemStack[]{wool});
            
            // Deprecated
            //p.updateInventory();
        }
        return true;
    }
    
    public void giveRoleItemsAfterDeath(Player player)
    {
        PlayerInventory inv = player.getInventory();
        inv.remove(374); // Removes bottles
        for (Items item : ctp.roles.get(ctp.playerData.get(player).role))
        {
            if(item.item.equals(Material.AIR))
                continue;

            if (inv.contains(item.item))
            {
                if(item.item.getId() == 373)    // Potions
                {
                    ItemStack stack = new ItemStack(item.item);
                    stack.setAmount(item.amount);
                    stack.setDurability(item.type);

                    HashMap<Integer, ? extends ItemStack> slots = inv.all(item.item);
                    int amount = 0;
                    for (int slotNum : slots.keySet())
                    {
                        if(slots.get(slotNum).getDurability() == item.type)
                            amount += slots.get(slotNum).getAmount();
                    }

                    if (amount < item.amount)
                    {
                        //Removing old potions
                        for (int slotNum : slots.keySet())
                        {
                            if(slots.get(slotNum).getDurability() == item.type)
                                inv.setItem(slotNum, null);
                        }

                        //inv.remove(stack);
                        inv.addItem(stack);
                    }
                }
                else if (!Util.ARMORS_TYPE.contains(item.item)/* && (!Util.WEAPONS_TYPE.contains(item.getType()))*/)
                {
                    HashMap<Integer, ? extends ItemStack> slots = inv.all(item.item);
                    int amount = 0;
                    for (int slotNum : slots.keySet())
                    {
                        amount += slots.get(slotNum).getAmount();
                    }
                    // nzn apie sita
//                    for (Iterator<Integer> i$ = slots.keySet().iterator(); i$.hasNext();)
//                    {
//                        int slotNum = i$.next().intValue();
//                        amount += ((ItemStack) slots.get(Integer.valueOf(slotNum))).getAmount();
//                    }
                    if (amount < item.amount)
                    {
                        inv.remove(item.item);

                        ItemStack stack = new ItemStack(item.item);
                        stack.setAmount(item.amount);
                        if(item.type != -1)
                            stack.setDurability(item.type);
                        // Add enchantments
                        for(int j = 0; j < item.enchantments.size(); j++)
                        {
                            stack.addEnchantment(item.enchantments.get(j), item.enchLevels.get(j));
                        }
                        
                        inv.addItem(stack);
                    }
                }
            }
            else
            {
                if (!Util.ARMORS_TYPE.contains(item.item))
                {
                    ItemStack stack = new ItemStack(item.item);
                    stack.setAmount(item.amount);
                    if(item.type != -1)
                        stack.setDurability(item.type);
                    // Add enchantments
                    for(int j = 0; j < item.enchantments.size(); j++)
                    {
                        stack.addEnchantment(item.enchantments.get(j), item.enchLevels.get(j));
                    }
                    
                    inv.addItem(stack);
                } 
                else
                {// find if there is somethig equiped
                    ItemStack stack = new ItemStack(item.item, item.amount);

                    // Add enchantments
                    for(int j = 0; j < item.enchantments.size(); j++)
                    {
                        stack.addEnchantment(item.enchantments.get(j), item.enchLevels.get(j));
                    }
                    

                    if (Util.BOOTS_TYPE.contains(item.item))
                    {
                        if (inv.getBoots().getType() == item.item)
                        {
                            inv.setBoots(stack);
                        } 
                        else
                        {
                            inv.addItem(stack);
                        }
                    } 
                    else if (Util.LEGGINGS_TYPE.contains(item.item))
                    {
                        if (inv.getLeggings().getType() == item.item)
                        {
                            inv.setLeggings(stack);
                        } 
                        else
                        {
                            inv.addItem(stack);
                        }
                    } 
                    else if (Util.CHESTPLATES_TYPE.contains(item.item))
                    {
                        if (inv.getChestplate().getType() == item.item)
                        {
                            inv.setChestplate(stack);
                        } 
                        else
                        {
                            inv.addItem(stack);
                        }
                    }
                }
            }
        }
    }
    
    public boolean isProtected(Player player) {
        // Kj -- null checks
        if (ctp.mainArena == null || player == null) {
            return false;
        }
        if (ctp.playerData.get(player) == null) {
            return false;
        }

        Spawn spawn = new Spawn();

        try
        {
//            if(ctp.playerData.get(player).team.color != null && ctp.mainArena.teamSpawns.get(ctp.playerData.get(player).team.color) != null)
//
//
//
//
//            spawn =
//                ctp.mainArena.teamSpawns.get(ctp.playerData.get(player).team.color) != null ?
//                ctp.mainArena.teamSpawns.get(ctp.playerData.get(player).team.color) :
            spawn = ctp.playerData.get(player).team.spawn;
        }
        catch(Exception e)   // For debugging
        {
            System.out.println("[ERROR][CTP] Team spawn could not be found!  Player Name: " + player.getName());
            return false;
        }
                            
        Location protectionPoint = new Location(ctp.getServer().getWorld(ctp.mainArena.world), spawn.x, spawn.y, spawn.z);
        double distance = Util.getDistance(player.getLocation(), protectionPoint); // Kj -- this method is world-friendly.
        if (distance == Double.NaN) {
            return false; // Kj -- it will return Double.NaN if cross-world or couldn't work out distance for whatever reason.
        } else {
            return distance <= ctp.mainArena.co.protectionDistance;
        }
    }
    
    
    public void respawnPlayer (Player player, Player attacker)
    {
        if (attacker != null)
        {
            if(!ctp.globalConfigOptions.disableKillMessages)
            {
                System.out.println("" + ctp.globalConfigOptions.disableKillMessages);
                Util.sendMessageToPlayers(ctp, ctp.playerData.get(player).team.chatcolor + player.getName() + ChatColor.WHITE
                        + " was killed by " + ctp.playerData.get(attacker).team.chatcolor + attacker.getName());
            }
            dropWool(player);
            ctp.playerData.get(attacker).money += ctp.mainArena.co.moneyForKill;
            attacker.sendMessage("Money: " + ChatColor.GREEN + ctp.playerData.get(attacker).money);
            ctp.checkForKillMSG(attacker, false);
            ctp.checkForKillMSG(player, true);
        } 
        else
        {
            if(!ctp.globalConfigOptions.disableKillMessages)
                Util.sendMessageToPlayers(ctp, ctp.playerData.get(player).team.chatcolor + player.getName() + ChatColor.WHITE
                        + " was killed by " + ChatColor.LIGHT_PURPLE + "Herobrine");
            player.sendMessage(ChatColor.RED + "Please do not remove your Helmet.");
            ctp.checkForKillMSG(player, true);
        }

        CTPPotionEffect.removeAllEffects(player);
        player.setHealth(ctp.mainArena.co.maxPlayerHealth);
        player.setFoodLevel(20);
        Spawn spawn = 
//                ctp.mainArena.teamSpawns.get(ctp.playerData.get(player).team.color) != null ?
//                ctp.mainArena.teamSpawns.get(ctp.playerData.get(player).team.color) :
                ctp.playerData.get(player).team.spawn;

        if (ctp.mainArena.co.giveNewRoleItemsOnRespawn)
        {
            giveRoleItemsAfterDeath(player);
        }

        // Reseting player cooldowns
        for (HealingItems item : ctp.healingItems) {
            if (item != null && item.cooldowns != null && item.cooldowns.size() > 0 && item.resetCooldownOnDeath) {
                for (String playName : item.cooldowns.keySet()) {
                    if (playName.equalsIgnoreCase(player.getName())) {
                        item.cooldowns.remove(playName);
                    }
                }
            }
        }

        Location loc = new Location(ctp.getServer().getWorld(ctp.mainArena.world), spawn.x, spawn.y, spawn.z);
        loc.setYaw((float) spawn.dir);
        ctp.getServer().getWorld(ctp.mainArena.world).loadChunk(loc.getBlockX(), loc.getBlockZ());
        boolean teleport = player.teleport(loc);
        
        if (!teleport)
        {
            player.teleport(new Location(player.getWorld(), spawn.x, spawn.y, spawn.z, 0.0F, (float)spawn.dir));
        }
    }
}