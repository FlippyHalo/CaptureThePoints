package me.dalton.capturethepoints.util;

import me.dalton.capturethepoints.CaptureThePoints;
import me.dalton.capturethepoints.HealingItems;
import me.dalton.capturethepoints.Util;
import me.dalton.capturethepoints.beans.Arena;
import me.dalton.capturethepoints.beans.Items;

import org.bukkit.ChatColor;
import org.bukkit.DyeColor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

public class InvManagement {
	private CaptureThePoints ctp;
	
	public InvManagement(CaptureThePoints plugin) {
		ctp = plugin;
	}
	
    public void restoreThings(Player p) {
    	Arena a = ctp.getArenaMaster().getArenaPlayerIsIn(p.getName());
        a.getPlayerData(p.getName()).setJustJoined(true);
        
        restoreInv(p);

        Location loc = ctp.getPrevoiusPosition().get(p.getName());
        loc.setYaw((float) a.getLobby().getDir());
        if(!loc.getWorld().isChunkLoaded(loc.getChunk())) {
        	loc.getWorld().loadChunk(loc.getChunk());
        }
        
        p.teleport(ctp.getPrevoiusPosition().get(p.getName()));

        // do not check double signal
        if (a.getPlayerData(p.getName()) == null) {
            return;
        }
        
        PotionManagement.removeAllEffects(p);
        PotionManagement.restorePotionEffects(p, a.getPlayerData(p.getName()).getPotionEffects());

        p.setFoodLevel(a.getPlayerData(p.getName()).getFoodLevel());
        if (a.getPlayerData(p.getName()).wasInCreative()) {
            p.setGameMode(GameMode.CREATIVE);
        }

        if (a.getPlayerData(p.getName()).getHealth() > 200 || a.getPlayerData(p.getName()).getHealth() < 0) {
            p.setHealth(20);
        } else {
            p.setHealth(a.getPlayerData(p.getName()).getHealth());
        }
       
    }
	
	/**
	 * Restore the player's inventory and armor that we stored when they joined.
	 * 
	 * @param player The player to restore to
	 */
	@SuppressWarnings("deprecation")
	public void restoreInv (Player player) {
        PlayerInventory PlayerInv = player.getInventory();

        // Just to be sure that inventory is saved
        if (ctp.getInventories().get(player.getName()) != null) {
            PlayerInv.setContents(ctp.getInventories().get(player.getName()));
            ctp.getInventories().remove(player.getName());

            PlayerInv.setBoots(ctp.getArmor().get(player.getName())[0].getTypeId() == 0 ? null
                    : ctp.getArmor().get(player.getName())[0]); // Kj -- removed redundant casts
            PlayerInv.setLeggings(ctp.getArmor().get(player.getName())[1].getTypeId() == 0
                    ? null : ctp.getArmor().get(player.getName())[1]);
            PlayerInv.setChestplate(ctp.getArmor().get(player.getName())[2].getTypeId() == 0
                    ? null : ctp.getArmor().get(player.getName())[2]);
            PlayerInv.setHelmet(ctp.getArmor().get(player.getName())[3].getTypeId() == 0
                    ? null : ctp.getArmor().get(player.getName())[3]);
            ctp.getArmor().remove(player.getName());
            
            //It's deprecated but it's currently the only way to get the desired effect.
            player.updateInventory();
        }
    }
	
	/**
	 * Save the player's inventory when they join to a hashmap.
	 * 
	 * @param player The player to save for
	 */
    public void saveInv (Player player) {
        PlayerInventory PlayerInv = player.getInventory();
        ctp.getInventories().put(player.getName(), PlayerInv.getContents());
        PlayerInv.clear();
        
        ctp.getArmor().put(player.getName(), PlayerInv.getArmorContents());
        PlayerInv.setHelmet(null);
        PlayerInv.setChestplate(null);
        PlayerInv.setLeggings(null);
        PlayerInv.setBoots(null);
    }
    
    /**
     * Removes the given player's cool downs.
     * 
     * @param player The player's name to remove.
     * @since 1.5.0-b123
     */
    public void removeCoolDowns(String player) {
        // Removing player cooldowns
        for (HealingItems item : ctp.healingItems) {
            if (item != null && item.cooldowns != null && item.cooldowns.size() > 0) {
                for (String playName : item.cooldowns.keySet()) {
                    if (playName.equalsIgnoreCase(player)) {
                        item.cooldowns.remove(playName);
                    }
                }
            }
        }
    }
    
    /**
     * This assigns a new role role to the given player.
     * <p />
     * 
     * @param a The arena the player is in.
     * @param p The player whom to change.
     * @param role The role to change to.
     * @return True if we could assign a new role, false if not.
     */
	@SuppressWarnings("deprecation")
	public boolean assignRole(Arena a, Player p, String role) {
        // role changing cooldown
        if(a.getPlayerData(p.getName()).getClassChangeTime() == 0) {
        	a.getPlayerData(p.getName()).setClassChangeTime(System.currentTimeMillis());
        } else if((System.currentTimeMillis() - a.getPlayerData(p.getName()).getClassChangeTime() <= 1000)) { // 1 sec 
            ctp.sendMessage(p, ChatColor.RED + "You can change roles only every 1 sec!");
            return false;
        } else {
        	a.getPlayerData(p.getName()).setClassChangeTime(System.currentTimeMillis());
        }

        p.setHealth(20);
        PlayerInventory inv = p.getInventory();
        inv.clear();
        inv.setHelmet(null);
        if(a.getPlayerData(p.getName()).getTeam() != null) {
            DyeColor color1 = DyeColor.valueOf(a.getPlayerData(p.getName()).getTeam().getColor().toUpperCase());

            ItemStack helmet = new ItemStack(Material.WOOL, 1, color1.getData());
            p.getInventory().setHelmet(helmet);
        }

        inv.setChestplate(null);
        inv.setLeggings(null);
        inv.setBoots(null);
        
		//It's deprecated but it's currently the only way to get the desired effect.
		p.updateInventory();

		a.getPlayerData(p.getName()).setRole(role);

        for (Items item : ctp.roles.get(role.toLowerCase())) {
            if (Util.ARMORS_TYPE.contains(item.getItem()) && (!Util.HELMETS_TYPE.contains(item.getItem()))) {
                ItemStack i = new ItemStack(item.getItem(), 1);
                
                // Add enchantments
                for(int j = 0; j < item.getEnchantments().size(); j++) {
                    i.addEnchantment(item.getEnchantments().get(j), item.getEnchantmentLevels().get(j));
                }
                
                ctp.getUtil().equipArmorPiece(i, inv);
            } else {
                ItemStack stack;
                // If something is wrong in config file
                try {
                    // If exp or economy money - do not allow to pass(only for rewards)
                    if(item.getItem().equals(Material.AIR))
                        continue;

                    stack = new ItemStack(item.getItem());
                    stack.setAmount(item.getAmount());
                    if(item.getType() != -1)
                        stack.setDurability(item.getType());

                    // Add enchantments
                    for(int j = 0; j < item.getEnchantments().size(); j++) {
                        stack.addEnchantment(item.getEnchantments().get(j), item.getEnchantmentLevels().get(j));
                    }
                } catch(Exception e) {
                    ctp.logInfo("There is error in your config file, with roles. Please check them!");
                    return false;
                }
                inv.addItem(stack);
            }
        }
        
		//It's deprecated but it's currently the only way to get the desired effect.
		p.updateInventory();

        return true;
    }
	
	/**
	 * Fixes the helmet on the player, puts the correct color of wool back on.
	 * <p />
	 * 
	 * @param p The player to fix the helmet for.
	 */
	@SuppressWarnings("deprecation")
	public void fixHelmet(Player p) {
        PlayerInventory inv = p.getInventory();
        ctp.sendMessage(p, ChatColor.RED + "Do not remove your helmet.");
        DyeColor color1 = DyeColor.valueOf(ctp.getArenaMaster().getArenaPlayerIsIn(p.getName()).getPlayerData(p.getName()).getTeam().getColor().toUpperCase());
        ItemStack helmet = new ItemStack(Material.WOOL, 1, (short) color1.getData());

        inv.remove(Material.WOOL);
        p.getInventory().setHelmet(helmet);
        
		p.updateInventory();
    }
}