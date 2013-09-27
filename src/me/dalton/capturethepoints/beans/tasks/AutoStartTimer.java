package me.dalton.capturethepoints.beans.tasks;

import me.dalton.capturethepoints.CaptureThePoints;
import me.dalton.capturethepoints.beans.Arena;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;

public class AutoStartTimer {
	private CaptureThePoints pl;
    private Arena arena;
    private int seconds;
    private Timer timer;
    private boolean started;
    
    public AutoStartTimer(CaptureThePoints plugin, Arena arena, int seconds) {
    	this.pl		   = plugin;
        this.arena     = arena;
        this.seconds   = seconds;
        this.started   = false;
    }
    
    /**
     * Starts the timer.
     * The method is idempotent, meaning if the timer was already
     * started, nothing happens if the method is called again.
     */
    public void start() {
        if (!started) {
        	pl.getLogger().info("Count down started for " + arena.getName() + " with " + seconds + " seconds.");
            timer = new Timer(seconds);
            timer.start();
            started = true;
        }
    }
    
    /** Cancels the task in the scheduler and sets the id to -1. */
	public int stop() {
		if(getTaskId() != -1) {
			pl.getServer().getScheduler().cancelTask(getTaskId());
			timer.id = -1;
			this.started = false;
		}
		
		return getTaskId();
	}
    
    public boolean isRunning() {
        return (timer != null && started);
    }
    
    public int getRemaining() {
        return timer != null ? timer.getRemaining() : -1;
    }
    
    public int getTaskId() {
    	return timer != null ? timer.id : -1;
    }
    
    /** Returns the about of seconds that the timer starts at. */
    public int getStartTime() {
    	return this.seconds;
    }
    
    /**
     * The internal timer class used for the auto-join-timer setting.
     * Using an extra internal object allows the interruption of a current
     * timer, followed by the creation of a new. Thus, no timers should
     * ever interfere with each other.
     */
    private class Timer implements Runnable {
        private int remaining;
        private int id;
        
        private Timer(int seconds) {
            this.remaining = seconds;
        }
        
        /**
         * Start the timer
         */
        public synchronized void start() {
            id = arena.scheduleDelayedRepeatingTask(this, 20, 20);
            pl.getLogger().info("Starting the count down with an id of " + id + " and remaining of " + remaining + ".");
        }
        
        /**
         * Get the remaining number of seconds
         * @return number of seconds left
         */
        public synchronized int getRemaining() {
            return remaining;
        }
    
        public void run() {
            synchronized(this) {
            	pl.getLogger().info("Count down is at " + remaining);
            	
                // Abort if the arena is running, or if players have left
                if (arena.getStatus().isRunning() || arena.getPlayers().size() == 0) {
                    started = false;
                    arena.setMoveAbility(true);
                    this.notifyAll();
                    Bukkit.getScheduler().cancelTask(id);
                    return;
                }
                
                pl.getLogger().info("Lowering the remaining count by one now.");
                
                // Count down
                remaining--;
                
                // Start if 0
                if (remaining <= 0) {
                    arena.updateStatusToRunning();
                    started = false;
                    
                    arena.setMoveAbility(true);
                    arena.startOtherTasks();
                    
                    pl.getLogger().info("CaptureThePoints arena '" + arena.getName() + "' has started!");
                    pl.getUtil().sendMessageToPlayers(arena, ChatColor.ITALIC + "...Go!");
                    
                    this.notifyAll();
                    Bukkit.getScheduler().cancelTask(id);
                } else {                	
                    // Warn at x seconds left
                	pl.getUtil().sendMessageToPlayers(arena, ChatColor.ITALIC + pl.getLanguage().START_COUNTDOWN.replaceAll("%CS", String.valueOf(remaining)));
                }
                
                this.notifyAll();
            }
        }
    }
}