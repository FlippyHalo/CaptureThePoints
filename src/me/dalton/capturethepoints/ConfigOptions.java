package me.dalton.capturethepoints;

/** Class for the config options ctp accepts. */
public class ConfigOptions {
    /** Allow breaking of blocks during a ctp game /||/ Disallow breaking of blocks during a ctp game (except for capturing points) [Default: false] */
    public boolean allowBlockBreak = false; // Kjhf
    
    /** Allows explosions to break blocks during the game /||/ Disallows explosions from breaking blocks during the game. [Default: false] */
    public boolean allowExplosionBlockBreak = false;
    
    /** Allow placement of blocks during a ctp game /||/ Disallow placement of blocks during a ctp game (except for capturing points) [Default: false] */
    public boolean allowBlockPlacement = false; // Kjhf
    
    /** Allow the breaking of their own team mate's wool in a point they have captured /||/ Disallow breaking their own captured point wool. [Default: false] */
    public boolean allowBreakingOwnCapturedPointWool = false;
    
    /** Allow use of commands during a ctp game /||/ Disallow use of commands during a ctp game (excludes /ctp) [Default: false] */
    public boolean allowCommands = false;
    
    /** Allow players to drop items during a ctp game -- may be exploited /||/ Disallow players to drop items during a ctp game [Default: false] */
    public boolean allowDropItems = false; // Kjhf
    
    /** Allow joining a ctp game if one has already started /||/ Disallow joining a ctp game if one has already started [Default: true] */
    public boolean allowLateJoin = true; // Kjhf
    
    /** Allow the game to start automatically when there are enough players /||/ Don't start game until an admin does /ctp start [Default: true] */
    public boolean autoStart = true; // Kjhf

    /** Breaking blocks in game drops items /||/ Cancel any item drops resulting from breaking blocks in game [Default: false] */
    public boolean breakingBlocksDropsItems = false; // Kjhf

    /** Dissables kill mesages in the chat for less spam /||/ Kills messages are written to chat [Default: false] */
    public boolean disableKillMessages = false;

    /** Drop wool if a player dies /||/ Don't drop wool if a player dies [Default: true] */
    public boolean dropWoolOnDeath = true; // Kjhf
       
    /** Teams must be even on start (may result in one person being left at lobby) /||/ Teams can be uneven on start (everyone can play) [Default: true] */
    public boolean exactTeamMemberCount = true;
    
    /** Replenish items on respawn /||/ Limited ammo and durability! [Default: true] */
    public boolean giveNewRoleItemsOnRespawn = true;
    
    /** Use scoring system /||/ Use points system [Default: false] */
    public boolean useScoreGeneration = false;
    
    /** Only choose the arena that an admin has picked (/ctp select) or one specified as main. /||/ Allow suitable arena pick. [Default: false] */
    public boolean useSelectedArenaOnly = false; // Kjhf
    
    /** If eggs thrown in the arena are used as grenades. [Default: false] @since 1.5.0-b172 */
    public boolean eggsAreGrenades = false;
    
    /** Sets the power of the grenades, TNT is 4.0 so compare to that. [Default: 2.0] */
    public double grenadePower = 2.0;

    /** Auto balance teams if teams differ by this number of people. 0 disables. [Default: 2] */
    public int balanceTeamsWhenPlayerLeaves = 2;
    
    /** The amount of money it costs for joining the arena. [Default: 0] */
    public int economyMoneyCostForJoiningArena = 0;
    
    /** The starting wool players are given. [Default: 64] */
    public int givenWoolNumber = 64;
    
    /** The time, in seconds, players are given to ready up else they are kicked. [Default: 60] */
    public int lobbyKickTime = 60; // Kjhf
    
    /** The maximum player health in game. [Default: 20] */
    public int maxPlayerHealth = 20;
    
    /** An option to allow automatic health regain from their food/hunger being satisfied. [Default: false] */
    public boolean regainHealth = false;
    
    /** The starting money players are given. [Default: 0] */
    public int moneyAtTheLobby = 0;
    
    /** The money players are awarded every 30s. [Default: 100] */
    public int moneyEvery30Sec = 100;
    
    /** The money players are awarded per kill [Default: 100] */
    public int moneyForKill = 100;
    
    /** The money players are awarded for a point capture [Default: 100] */
    public int moneyForPointCapture = 100;
    
    /** How much score a team gets per point per 30s [Default: 1] */
    public int onePointGeneratedScoreEvery30sec = 1;
    
    /** Maximum play time in minutes [Default: 10] */
    public int playTime = 10;
    
    /** Points a team much reach to win [Default: 1] */
    public int pointsToWin = 1;
    
    /** Spawn camping protection radius [Default: 10] */
    public int protectionDistance = 10;
    
    /** The block ID of a point [Default: 7 (Bedrock)] */
    public int ringBlock = 7;
    
    /** How often scores are announced in seconds [Default: 30] */
    public int scoreAnnounceTime = 30;
    
    /** The score a team much reach to win [Default: 15] */
    public int scoreToWin = 15;

    /** Multiples gained score after all possible points are captured by that team [Default: 2] **/
    public int scoreMyltiplier = 2;
    
    /** Should we count down on game start or not. [Default: false] */
    public boolean useStartCountDown = false;
    
    /** The amount of time in seconds we should count down for the start counter. [Default: 5] */
    public int startCountDownTime = 5;
    
    /** Should we count down on game end or not. [Default: false] */
    public boolean useEndCountDown = false;
    
    /** The amount of time in seconds we should count down for the end counter. [Default: 5] */
    public int endCountDownTime = 5;
    
    /** The KillStreak messages generated, starting from 2 kills in a row. <br>
     * [Default: "%player strikes again!", "%player is on a killing spree!", "%player is on a rampage!", "%player is unstoppable!", "%player is GOD-LIKE!"] 
     * @see KillStreakMessages */
    public KillStreakMessages killStreakMessages = new KillStreakMessages();
    
    /** If the plugin should display all the debug messages. [Default: false] */
    public boolean debugMessages = false;
    
    /** Should we write to MySQL? [Default: false] */
    public boolean enableHardArenaRestore = false; // Write to MYSQL
    
    /** The port we should use when connecting to the database server. [Default: 3306] */
    public int mysqlPort = 3306;
    
    /** The host we should connect to for the database server. [Default: localhost] */
    public String mysqlAddress = "localhost";
    
    /** The database that we should use. [Default: ""] */
    public String mysqlDatabase = "";
    
    /** The username that we should use when connecting to the database. [Default: root] */
    public String mysqlUser = "root";
    
    /** The password that we should use when connecting to the database. [Default: ""] */
    public String mysqlPass = "";
}