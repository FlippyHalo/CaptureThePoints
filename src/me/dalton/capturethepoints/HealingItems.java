package me.dalton.capturethepoints;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import me.dalton.capturethepoints.beans.Items;

/**
 *
 * @author Humsas
 */
public class HealingItems {
    public int hotHeal = 0;
    public int cooldown = 0;
    public int duration = 0;
    public int instantHeal = 0;
    public int hotInterval = 0;

    public boolean resetCooldownOnDeath = true;

    public Items item = null;

    public Map<String, PlayersAndCooldowns> cooldowns = new ConcurrentHashMap<String, PlayersAndCooldowns>();  // String - player name
}