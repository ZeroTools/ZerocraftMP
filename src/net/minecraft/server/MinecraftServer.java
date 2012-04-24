package net.minecraft.server;

import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.minecraft.src.AnvilSaveConverter;
import net.minecraft.src.AnvilSaveHandler;
import net.minecraft.src.AxisAlignedBB;
import net.minecraft.src.ChunkCoordinates;
import net.minecraft.src.ChunkProviderServer;
import net.minecraft.src.ConsoleCommandHandler;
import net.minecraft.src.ConsoleLogManager;
import net.minecraft.src.ConvertProgressUpdater;
import net.minecraft.src.EntityTracker;
import net.minecraft.src.ICommandListener;
import net.minecraft.src.ISaveFormat;
import net.minecraft.src.IServer;
import net.minecraft.src.IUpdatePlayerListBox;
import net.minecraft.src.MathHelper;
import net.minecraft.src.NetworkListenThread;
import net.minecraft.src.Packet;
import net.minecraft.src.Packet4UpdateTime;
import net.minecraft.src.PropertyManager;
import net.minecraft.src.RConConsoleSource;
import net.minecraft.src.RConThreadMain;
import net.minecraft.src.RConThreadQuery;
import net.minecraft.src.ServerCommand;
import net.minecraft.src.ServerConfigurationManager;
import net.minecraft.src.ServerGUI;
import net.minecraft.src.StatList;
import net.minecraft.src.ThreadCommandReader;
import net.minecraft.src.ThreadServerApplication;
import net.minecraft.src.ThreadServerSleep;
import net.minecraft.src.Vec3D;
import net.minecraft.src.WorldInfo;
import net.minecraft.src.WorldManager;
import net.minecraft.src.WorldProvider;
import net.minecraft.src.WorldServer;
import net.minecraft.src.WorldServerMulti;
import net.minecraft.src.WorldSettings;
import net.minecraft.src.WorldType;

public class MinecraftServer implements Runnable, ICommandListener, IServer
{
    /** The logging system. */
    public static Logger logger = Logger.getLogger("Minecraft");
    public static HashMap field_6037_b = new HashMap();

    /** The server's hostname */
    private String hostname;

    /** The server's port */
    private int serverPort;

    /** listening server socket and client accept thread */
    public NetworkListenThread networkServer;

    /** Reference to the PropertyManager object. */
    public PropertyManager propertyManagerObj;
    public WorldServer worldMngr[];
    public long field_40027_f[];
    public long field_40028_g[][];

    /** the server config manager for this server */
    public ServerConfigurationManager configManager;
    private ConsoleCommandHandler commandHandler;

    /**
     * Indicates whether the server is running or not. Set to false to initiate a shutdown.
     */
    private boolean serverRunning;

    /** Indicates to other classes that the server is safely stopped. */
    public boolean serverStopped;
    int deathTime;

    /**
     * the task the server is currently working on(and will output on ouputPercentRemaining)
     */
    public String currentTask;

    /** the percentage of the current task finished so far */
    public int percentDone;

    /** List of names of players who are online. */
    private List playersOnline;

    /** A list containing all the commands entered. */
    private List commands;
    public EntityTracker entityTracker[];

    /** True if the server is in online mode. */
    public boolean onlineMode;

    /** True if server has animals turned on */
    public boolean spawnPeacefulMobs;
    public boolean field_44002_p;

    /** Indicates whether PvP is active on the server or not. */
    public boolean pvpOn;

    /** Determines if flight is Allowed or not */
    public boolean allowFlight;

    /** The server MOTD string. */
    public String motd;

    /** Maximum build height */
    public int buildLimit;
    private long field_48074_E;
    private long field_48075_F;
    private long field_48076_G;
    private long field_48077_H;
    public long field_48080_u[];
    public long field_48079_v[];
    public long field_48078_w[];
    public long field_48082_x[];
    private RConThreadQuery rconQueryThread;
    private RConThreadMain rconMainThread;

    public MinecraftServer()
    {
        field_40027_f = new long[100];
        serverRunning = true;
        serverStopped = false;
        deathTime = 0;
        playersOnline = new ArrayList();
        commands = Collections.synchronizedList(new ArrayList());
        entityTracker = new EntityTracker[3];
        field_48080_u = new long[100];
        field_48079_v = new long[100];
        field_48078_w = new long[100];
        field_48082_x = new long[100];
        new ThreadServerSleep(this);
    }

    /**
     * Initialises the server and starts it.
     */
    private boolean startServer() throws UnknownHostException
    {
        commandHandler = new ConsoleCommandHandler(this);
        ThreadCommandReader threadcommandreader = new ThreadCommandReader(this);
        threadcommandreader.setDaemon(true);
        threadcommandreader.start();
        ConsoleLogManager.init();
        logger.info("Starting minecraft server version 1.2.5");

        if (Runtime.getRuntime().maxMemory() / 1024L / 1024L < 512L)
        {
            logger.warning("To start the server with more ram, launch it as \"java -Xmx1024M -Xms1024M -jar minecraft_server.jar\"");
        }

        logger.info("Loading properties");
        propertyManagerObj = new PropertyManager(new File("server.properties"));
        hostname = propertyManagerObj.getStringProperty("server-ip", "");
        onlineMode = propertyManagerObj.getBooleanProperty("online-mode", true);
        spawnPeacefulMobs = propertyManagerObj.getBooleanProperty("spawn-animals", true);
        field_44002_p = propertyManagerObj.getBooleanProperty("spawn-npcs", true);
        pvpOn = propertyManagerObj.getBooleanProperty("pvp", true);
        allowFlight = propertyManagerObj.getBooleanProperty("allow-flight", false);
        motd = propertyManagerObj.getStringProperty("motd", "A Minecraft Server");
        motd.replace('\247', '$');
        InetAddress inetaddress = null;

        if (hostname.length() > 0)
        {
            inetaddress = InetAddress.getByName(hostname);
        }

        serverPort = propertyManagerObj.getIntProperty("server-port", 25565);
        logger.info((new StringBuilder()).append("Starting Minecraft server on ").append(hostname.length() != 0 ? hostname : "*").append(":").append(serverPort).toString());

        try
        {
            networkServer = new NetworkListenThread(this, inetaddress, serverPort);
        }
        catch (IOException ioexception)
        {
            logger.warning("**** FAILED TO BIND TO PORT!");
            logger.log(Level.WARNING, (new StringBuilder()).append("The exception was: ").append(ioexception.toString()).toString());
            logger.warning("Perhaps a server is already running on that port?");
            return false;
        }

        if (!onlineMode)
        {
            logger.warning("**** SERVER IS RUNNING IN OFFLINE/INSECURE MODE!");
            logger.warning("The server will make no attempt to authenticate usernames. Beware.");
            logger.warning("While this makes the game possible to play without internet access, it also opens up the ability for hackers to connect with any username they choose.");
            logger.warning("To change this, set \"online-mode\" to \"true\" in the server.settings file.");
        }

        configManager = new ServerConfigurationManager(this);
        entityTracker[0] = new EntityTracker(this, 0);
        entityTracker[1] = new EntityTracker(this, -1);
        entityTracker[2] = new EntityTracker(this, 1);
        long l = System.nanoTime();
        String s = propertyManagerObj.getStringProperty("level-name", "world");
        String s1 = propertyManagerObj.getStringProperty("level-seed", "");
        String s2 = propertyManagerObj.getStringProperty("level-type", "DEFAULT");
        long l1 = (new Random()).nextLong();

        if (s1.length() > 0)
        {
            try
            {
                long l2 = Long.parseLong(s1);

                if (l2 != 0L)
                {
                    l1 = l2;
                }
            }
            catch (NumberFormatException numberformatexception)
            {
                l1 = s1.hashCode();
            }
        }

        WorldType worldtype = WorldType.parseWorldType(s2);

        if (worldtype == null)
        {
            worldtype = WorldType.DEFAULT;
        }

        buildLimit = propertyManagerObj.getIntProperty("max-build-height", 256);
        buildLimit = ((buildLimit + 8) / 16) * 16;
        buildLimit = MathHelper.clamp_int(buildLimit, 64, 256);
        propertyManagerObj.setProperty("max-build-height", Integer.valueOf(buildLimit));
        logger.info((new StringBuilder()).append("Preparing level \"").append(s).append("\"").toString());
        initWorld(new AnvilSaveConverter(new File(".")), s, l1, worldtype);
        long l3 = System.nanoTime() - l;
        String s3 = String.format("%.3fs", new Object[]
                {
                    Double.valueOf((double)l3 / 1000000000D)
                });
        logger.info((new StringBuilder()).append("Done (").append(s3).append(")! For help, type \"help\" or \"?\"").toString());

        if (propertyManagerObj.getBooleanProperty("enable-query", false))
        {
            logger.info("Starting GS4 status listener");
            rconQueryThread = new RConThreadQuery(this);
            rconQueryThread.startThread();
        }

        if (propertyManagerObj.getBooleanProperty("enable-rcon", false))
        {
            logger.info("Starting remote control listener");
            rconMainThread = new RConThreadMain(this);
            rconMainThread.startThread();
        }

        return true;
    }

    /**
     * Initialises the world object.
     */
    private void initWorld(ISaveFormat par1ISaveFormat, String par2Str, long par3, WorldType par5WorldType)
    {
        if (par1ISaveFormat.isOldMapFormat(par2Str))
        {
            logger.info("Converting map!");
            par1ISaveFormat.convertMapFormat(par2Str, new ConvertProgressUpdater(this));
        }

        worldMngr = new WorldServer[3];
        field_40028_g = new long[worldMngr.length][100];
        int i = propertyManagerObj.getIntProperty("gamemode", 0);
        i = WorldSettings.validGameType(i);
        logger.info((new StringBuilder()).append("Default game type: ").append(i).toString());
        boolean flag = propertyManagerObj.getBooleanProperty("generate-structures", true);
        WorldSettings worldsettings = new WorldSettings(par3, i, flag, false, par5WorldType);
        AnvilSaveHandler anvilsavehandler = new AnvilSaveHandler(new File("."), par2Str, true);

        for (int j = 0; j < worldMngr.length; j++)
        {
            byte byte0 = 0;

            if (j == 1)
            {
                byte0 = -1;
            }

            if (j == 2)
            {
                byte0 = 1;
            }

            if (j == 0)
            {
                worldMngr[j] = new WorldServer(this, anvilsavehandler, par2Str, byte0, worldsettings);
            }
            else
            {
                worldMngr[j] = new WorldServerMulti(this, anvilsavehandler, par2Str, byte0, worldsettings, worldMngr[0]);
            }

            worldMngr[j].addWorldAccess(new WorldManager(this, worldMngr[j]));
            worldMngr[j].difficultySetting = propertyManagerObj.getIntProperty("difficulty", 1);
            worldMngr[j].setAllowedSpawnTypes(propertyManagerObj.getBooleanProperty("spawn-monsters", true), spawnPeacefulMobs);
            worldMngr[j].getWorldInfo().setGameType(i);
            configManager.setPlayerManager(worldMngr);
        }

        char c = '\304';
        long l = System.currentTimeMillis();

        for (int k = 0; k < 1; k++)
        {
            logger.info((new StringBuilder()).append("Preparing start region for level ").append(k).toString());
            WorldServer worldserver = worldMngr[k];
            ChunkCoordinates chunkcoordinates = worldserver.getSpawnPoint();

            for (int i1 = -c; i1 <= c && serverRunning; i1 += 16)
            {
                for (int j1 = -c; j1 <= c && serverRunning; j1 += 16)
                {
                    long l1 = System.currentTimeMillis();

                    if (l1 < l)
                    {
                        l = l1;
                    }

                    if (l1 > l + 1000L)
                    {
                        int k1 = (c * 2 + 1) * (c * 2 + 1);
                        int i2 = (i1 + c) * (c * 2 + 1) + (j1 + 1);
                        outputPercentRemaining("Preparing spawn area", (i2 * 100) / k1);
                        l = l1;
                    }

                    worldserver.chunkProviderServer.loadChunk(chunkcoordinates.posX + i1 >> 4, chunkcoordinates.posZ + j1 >> 4);

                    while (worldserver.updatingLighting() && serverRunning) ;
                }
            }
        }

        clearCurrentTask();
    }

    /**
     * used to display a percent remaining given text and the percentage
     */
    private void outputPercentRemaining(String par1Str, int par2)
    {
        currentTask = par1Str;
        percentDone = par2;
        logger.info((new StringBuilder()).append(par1Str).append(": ").append(par2).append("%").toString());
    }

    /**
     * set current task to null and set its percentage to 0
     */
    private void clearCurrentTask()
    {
        currentTask = null;
        percentDone = 0;
    }

    /**
     * Saves the server's world, called by both save all and stop.
     */
    private void saveServerWorld()
    {
        logger.info("Saving chunks");

        for (int i = 0; i < worldMngr.length; i++)
        {
            WorldServer worldserver = worldMngr[i];
            worldserver.saveWorld(true, null);
            worldserver.func_30006_w();
        }
    }

    /**
     * Saves all necessary data as preparation for stopping the server.
     */
    private void stopServer()
    {
        logger.info("Stopping server");

        if (configManager != null)
        {
            configManager.savePlayerStates();
        }

        for (int i = 0; i < worldMngr.length; i++)
        {
            WorldServer worldserver = worldMngr[i];

            if (worldserver != null)
            {
                saveServerWorld();
            }
        }
    }

    /**
     * Sets the serverRunning variable to false, in order to get the server to shut down.
     */
    public void initiateShutdown()
    {
        serverRunning = false;
    }

    public void run()
    {
        try
        {
            if (this.startServer())
            {
                long var1 = System.currentTimeMillis();

                for (long var3 = 0L; this.serverRunning; Thread.sleep(1L))
                {
                    long var5 = System.currentTimeMillis();
                    long var7 = var5 - var1;

                    if (var7 > 2000L)
                    {
                        logger.warning("Can\'t keep up! Did the system time change, or is the server overloaded?");
                        var7 = 2000L;
                    }

                    if (var7 < 0L)
                    {
                        logger.warning("Time ran backwards! Did the system time change?");
                        var7 = 0L;
                    }

                    var3 += var7;
                    var1 = var5;

                    if (this.worldMngr[0].isAllPlayersFullyAsleep())
                    {
                        this.doTick();
                        var3 = 0L;
                    }
                    else
                    {
                        while (var3 > 50L)
                        {
                            var3 -= 50L;
                            this.doTick();
                        }
                    }
                }
            }
            else
            {
                while (this.serverRunning)
                {
                    this.commandLineParser();

                    try
                    {
                        Thread.sleep(10L);
                    }
                    catch (InterruptedException var57)
                    {
                        var57.printStackTrace();
                    }
                }
            }
        }
        catch (Throwable var58)
        {
            var58.printStackTrace();
            logger.log(Level.SEVERE, "Unexpected exception", var58);

            while (this.serverRunning)
            {
                this.commandLineParser();

                try
                {
                    Thread.sleep(10L);
                }
                catch (InterruptedException var56)
                {
                    var56.printStackTrace();
                }
            }
        }
        finally
        {
            try
            {
                this.stopServer();
                this.serverStopped = true;
            }
            catch (Throwable var54)
            {
                var54.printStackTrace();
            }
            finally
            {
                System.exit(0);
            }
        }
    }

    private void doTick()
    {
        long l = System.nanoTime();
        ArrayList arraylist = new ArrayList();

        for (Iterator iterator = field_6037_b.keySet().iterator(); iterator.hasNext();)
        {
            String s = (String)iterator.next();
            int j1 = ((Integer)field_6037_b.get(s)).intValue();

            if (j1 > 0)
            {
                field_6037_b.put(s, Integer.valueOf(j1 - 1));
            }
            else
            {
                arraylist.add(s);
            }
        }

        for (int i = 0; i < arraylist.size(); i++)
        {
            field_6037_b.remove(arraylist.get(i));
        }

        AxisAlignedBB.clearBoundingBoxPool();
        Vec3D.initialize();
        deathTime++;

        for (int j = 0; j < worldMngr.length; j++)
        {
            long l1 = System.nanoTime();

            if (j == 0 || propertyManagerObj.getBooleanProperty("allow-nether", true))
            {
                WorldServer worldserver = worldMngr[j];

                if (deathTime % 20 == 0)
                {
                    configManager.sendPacketToAllPlayersInDimension(new Packet4UpdateTime(worldserver.getWorldTime()), worldserver.worldProvider.worldType);
                }

                worldserver.tick();

                while (worldserver.updatingLighting()) ;

                worldserver.updateEntities();
            }

            field_40028_g[j][deathTime % 100] = System.nanoTime() - l1;
        }

        networkServer.handleNetworkListenThread();
        configManager.onTick();

        for (int k = 0; k < entityTracker.length; k++)
        {
            entityTracker[k].updateTrackedEntities();
        }

        for (int i1 = 0; i1 < playersOnline.size(); i1++)
        {
            ((IUpdatePlayerListBox)playersOnline.get(i1)).update();
        }

        try
        {
            commandLineParser();
        }
        catch (Exception exception)
        {
            logger.log(Level.WARNING, "Unexpected exception while parsing console command", exception);
        }

        field_40027_f[deathTime % 100] = System.nanoTime() - l;
        field_48080_u[deathTime % 100] = Packet.field_48099_n - field_48074_E;
        field_48074_E = Packet.field_48099_n;
        field_48079_v[deathTime % 100] = Packet.field_48100_o - field_48075_F;
        field_48075_F = Packet.field_48100_o;
        field_48078_w[deathTime % 100] = Packet.field_48101_l - field_48076_G;
        field_48076_G = Packet.field_48101_l;
        field_48082_x[deathTime % 100] = Packet.field_48102_m - field_48077_H;
        field_48077_H = Packet.field_48102_m;
    }

    /**
     * Adds a command to the command list for processing.
     */
    public void addCommand(String par1Str, ICommandListener par2ICommandListener)
    {
        commands.add(new ServerCommand(par1Str, par2ICommandListener));
    }

    /**
     * Parse the command line and call the corresponding action.
     */
    public void commandLineParser()
    {
        ServerCommand servercommand;

        for (; commands.size() > 0; commandHandler.handleCommand(servercommand))
        {
            servercommand = (ServerCommand)commands.remove(0);
        }
    }

    /**
     * Adds a player's name to the list of online players.
     */
    public void addToOnlinePlayerList(IUpdatePlayerListBox par1IUpdatePlayerListBox)
    {
        playersOnline.add(par1IUpdatePlayerListBox);
    }

    public static void main(String par0ArrayOfStr[])
    {
        StatList.func_27092_a();

        try
        {
            MinecraftServer minecraftserver = new MinecraftServer();

            if (!java.awt.GraphicsEnvironment.isHeadless() && (par0ArrayOfStr.length <= 0 || !par0ArrayOfStr[0].equals("nogui")))
            {
                ServerGUI.initGui(minecraftserver);
            }

            (new ThreadServerApplication("Server thread", minecraftserver)).start();
        }
        catch (Exception exception)
        {
            logger.log(Level.SEVERE, "Failed to start the minecraft server", exception);
        }
    }

    /**
     * Returns a File object from the specified string.
     */
    public File getFile(String par1Str)
    {
        return new File(par1Str);
    }

    /**
     * Logs the message with a level of INFO.
     */
    public void log(String par1Str)
    {
        logger.info(par1Str);
    }

    /**
     * logs the warning same as: logger.warning(String);
     */
    public void logWarning(String par1Str)
    {
        logger.warning(par1Str);
    }

    /**
     * Gets the players username.
     */
    public String getUsername()
    {
        return "CONSOLE";
    }

    /**
     * gets the worldServer by the given dimension
     */
    public WorldServer getWorldManager(int par1)
    {
        if (par1 == -1)
        {
            return worldMngr[1];
        }

        if (par1 == 1)
        {
            return worldMngr[2];
        }
        else
        {
            return worldMngr[0];
        }
    }

    /**
     * gets the entityTracker by the given dimension
     */
    public EntityTracker getEntityTracker(int par1)
    {
        if (par1 == -1)
        {
            return entityTracker[1];
        }

        if (par1 == 1)
        {
            return entityTracker[2];
        }
        else
        {
            return entityTracker[0];
        }
    }

    /**
     * Returns the specified property value as an int, or a default if the property doesn't exist
     */
    public int getIntProperty(String par1Str, int par2)
    {
        return propertyManagerObj.getIntProperty(par1Str, par2);
    }

    /**
     * Returns the specified property value as a String, or a default if the property doesn't exist
     */
    public String getStringProperty(String par1Str, String par2Str)
    {
        return propertyManagerObj.getStringProperty(par1Str, par2Str);
    }

    /**
     * Saves an Object with the given property name
     */
    public void setProperty(String par1Str, Object par2Obj)
    {
        propertyManagerObj.setProperty(par1Str, par2Obj);
    }

    /**
     * Saves all of the server properties to the properties file
     */
    public void saveProperties()
    {
        propertyManagerObj.saveProperties();
    }

    /**
     * Returns the filename where server properties are stored
     */
    public String getSettingsFilename()
    {
        File file = propertyManagerObj.getPropertiesFile();

        if (file != null)
        {
            return file.getAbsolutePath();
        }
        else
        {
            return "No settings file";
        }
    }

    /**
     * Returns the server hostname
     */
    public String getHostname()
    {
        return hostname;
    }

    /**
     * Returns the server port
     */
    public int getPort()
    {
        return serverPort;
    }

    /**
     * Returns the server message of the day
     */
    public String getMotd()
    {
        return motd;
    }

    /**
     * Returns the server version string
     */
    public String getVersionString()
    {
        return "1.2.5";
    }

    /**
     * Returns the number of players on the server
     */
    public int playersOnline()
    {
        return configManager.playersOnline();
    }

    /**
     * Returns the maximum number of players allowed on the server
     */
    public int getMaxPlayers()
    {
        return configManager.getMaxPlayers();
    }

    /**
     * Returns a list of usernames of all connected players
     */
    public String[] getPlayerNamesAsList()
    {
        return configManager.getPlayerNamesAsList();
    }

    /**
     * Returns the name of the currently loaded world
     */
    public String getWorldName()
    {
        return propertyManagerObj.getStringProperty("level-name", "world");
    }

    public String getPlugin()
    {
        return "";
    }

    public void func_40010_o()
    {
    }

    /**
     * Handle a command received by an RCon instance
     */
    public String handleRConCommand(String par1Str)
    {
        RConConsoleSource.instance.resetLog();
        commandHandler.handleCommand(new ServerCommand(par1Str, RConConsoleSource.instance));
        return RConConsoleSource.instance.getLogContents();
    }

    /**
     * Returns true if debugging is enabled, false otherwise
     */
    public boolean isDebuggingEnabled()
    {
        return false;
    }

    /**
     * Log severe error message
     */
    public void logSevere(String par1Str)
    {
        logger.log(Level.SEVERE, par1Str);
    }

    public void logIn(String par1Str)
    {
        if (isDebuggingEnabled())
        {
            logger.log(Level.INFO, par1Str);
        }
    }

    /**
     * Returns the list of ban
     */
    public String[] getBannedIPsList()
    {
        return (String[])configManager.getBannedIPsList().toArray(new String[0]);
    }

    /**
     * Returns a list of banned player names
     */
    public String[] getBannedPlayersList()
    {
        return (String[])configManager.getBannedPlayersList().toArray(new String[0]);
    }

    public String func_52003_getServerModName()
    {
        return "vanilla";
    }

    /**
     * Returns the boolean serverRunning.
     */
    public static boolean isServerRunning(MinecraftServer par0MinecraftServer)
    {
        return par0MinecraftServer.serverRunning;
    }
}
