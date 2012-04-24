package net.minecraft.src;

import java.io.*;
import java.util.*;
import java.util.logging.Logger;
import net.minecraft.server.MinecraftServer;

public class ServerConfigurationManager
{
    /** Reference to the logger. */
    public static Logger logger = Logger.getLogger("Minecraft");

    /** A list of player entities that exist on this server. */
    public List playerEntities;

    /** Reference to the MinecraftServer object. */
    private MinecraftServer mcServer;
    private PlayerManager playerManagerObj[];

    /** the maximum amount of players that can be connected */
    private int maxPlayers;

    /** the set of all banned players names */
    private Set bannedPlayers;

    /** A set containing the banned IPs. */
    private Set bannedIPs;

    /** A set containing the OPs. */
    private Set ops;

    /** the set of all white listed IP addresses */
    private Set whiteListedIPs;

    /** The file that contains the banned players. */
    private File bannedPlayersFile;

    /** the file which contains the list of banned IPs */
    private File ipBanFile;

    /** the file which contains the list of ops */
    private File opFile;

    /** File containing list of whitelisted players */
    private File whitelistPlayersFile;

    /** Reference to the PlayerNBTManager object. */
    private IPlayerFileData playerNBTManagerObj;

    /**
     * Server setting to only allow OP's and whitelisted players to join the server
     */
    private boolean whiteListEnforced;
    private int field_35482_p;

    public ServerConfigurationManager(MinecraftServer par1MinecraftServer)
    {
        playerEntities = new ArrayList();
        bannedPlayers = new HashSet();
        bannedIPs = new HashSet();
        ops = new HashSet();
        whiteListedIPs = new HashSet();
        field_35482_p = 0;
        playerManagerObj = new PlayerManager[3];
        mcServer = par1MinecraftServer;
        bannedPlayersFile = par1MinecraftServer.getFile("banned-players.txt");
        ipBanFile = par1MinecraftServer.getFile("banned-ips.txt");
        opFile = par1MinecraftServer.getFile("ops.txt");
        whitelistPlayersFile = par1MinecraftServer.getFile("white-list.txt");
        int i = par1MinecraftServer.propertyManagerObj.getIntProperty("view-distance", 10);
        playerManagerObj[0] = new PlayerManager(par1MinecraftServer, 0, i);
        playerManagerObj[1] = new PlayerManager(par1MinecraftServer, -1, i);
        playerManagerObj[2] = new PlayerManager(par1MinecraftServer, 1, i);
        maxPlayers = par1MinecraftServer.propertyManagerObj.getIntProperty("max-players", 20);
        whiteListEnforced = par1MinecraftServer.propertyManagerObj.getBooleanProperty("white-list", false);
        readBannedPlayers();
        loadBannedList();
        loadOps();
        loadWhiteList();
        writeBannedPlayers();
        saveBannedList();
        saveOps();
        saveWhiteList();
    }

    /**
     * Sets the NBT manager to the one for the worldserver given
     */
    public void setPlayerManager(WorldServer par1ArrayOfWorldServer[])
    {
        playerNBTManagerObj = par1ArrayOfWorldServer[0].getSaveHandler().getPlayerNBTManager();
    }

    /**
     * called when a player is teleported to a new dimension in order to clean up old dim refs, send them new dim
     * chunks, and make sure their new location chunk is loaded and initialized
     */
    public void joinNewPlayerManager(EntityPlayerMP par1EntityPlayerMP)
    {
        playerManagerObj[0].removePlayer(par1EntityPlayerMP);
        playerManagerObj[1].removePlayer(par1EntityPlayerMP);
        playerManagerObj[2].removePlayer(par1EntityPlayerMP);
        getPlayerManager(par1EntityPlayerMP.dimension).addPlayer(par1EntityPlayerMP);
        WorldServer worldserver = mcServer.getWorldManager(par1EntityPlayerMP.dimension);
        worldserver.chunkProviderServer.loadChunk((int)par1EntityPlayerMP.posX >> 4, (int)par1EntityPlayerMP.posZ >> 4);
    }

    public int getMaxTrackingDistance()
    {
        return playerManagerObj[0].getMaxTrackingDistance();
    }

    /**
     * returns the player manager object for the specified dimension
     */
    private PlayerManager getPlayerManager(int par1)
    {
        if (par1 == -1)
        {
            return playerManagerObj[1];
        }

        if (par1 == 0)
        {
            return playerManagerObj[0];
        }

        if (par1 == 1)
        {
            return playerManagerObj[2];
        }
        else
        {
            return null;
        }
    }

    /**
     * called during player login. reads the player information from disk.
     */
    public void readPlayerDataFromFile(EntityPlayerMP par1EntityPlayerMP)
    {
        playerNBTManagerObj.readPlayerData(par1EntityPlayerMP);
    }

    /**
     * Called when a player successfully logs in. Reads player data from disk and inserts the player into the world.
     */
    public void playerLoggedIn(EntityPlayerMP par1EntityPlayerMP)
    {
        sendPacketToAllPlayers(new Packet201PlayerInfo(par1EntityPlayerMP.username, true, 1000));
        playerEntities.add(par1EntityPlayerMP);
        WorldServer worldserver = mcServer.getWorldManager(par1EntityPlayerMP.dimension);
        worldserver.chunkProviderServer.loadChunk((int)par1EntityPlayerMP.posX >> 4, (int)par1EntityPlayerMP.posZ >> 4);

        for (; worldserver.getCollidingBoundingBoxes(par1EntityPlayerMP, par1EntityPlayerMP.boundingBox).size() != 0; par1EntityPlayerMP.setPosition(par1EntityPlayerMP.posX, par1EntityPlayerMP.posY + 1.0D, par1EntityPlayerMP.posZ)) { }

        worldserver.spawnEntityInWorld(par1EntityPlayerMP);
        getPlayerManager(par1EntityPlayerMP.dimension).addPlayer(par1EntityPlayerMP);
        func_52018_u();

        for (int i = 0; i < playerEntities.size(); i++)
        {
            EntityPlayerMP entityplayermp = (EntityPlayerMP)playerEntities.get(i);
            par1EntityPlayerMP.playerNetServerHandler.sendPacket(new Packet201PlayerInfo(entityplayermp.username, true, entityplayermp.ping));
        }
    }

    /**
     * using player's dimension, update their movement when in a vehicle (e.g. cart, boat)
     */
    public void serverUpdateMountedMovingPlayer(EntityPlayerMP par1EntityPlayerMP)
    {
        getPlayerManager(par1EntityPlayerMP.dimension).updateMountedMovingPlayer(par1EntityPlayerMP);
    }

    /**
     * Called when a player disconnects from the game. Writes player data to disk and removes them from the world.
     */
    public void playerLoggedOut(EntityPlayerMP par1EntityPlayerMP)
    {
        playerNBTManagerObj.writePlayerData(par1EntityPlayerMP);
        mcServer.getWorldManager(par1EntityPlayerMP.dimension).setEntityDead(par1EntityPlayerMP);
        playerEntities.remove(par1EntityPlayerMP);
        getPlayerManager(par1EntityPlayerMP.dimension).removePlayer(par1EntityPlayerMP);
        sendPacketToAllPlayers(new Packet201PlayerInfo(par1EntityPlayerMP.username, false, 9999));
    }

    /**
     * Called when a player tries to login. Checks whether they are banned/server is full etc.
     */
    public EntityPlayerMP login(NetLoginHandler par1NetLoginHandler, String par2Str)
    {
        if (bannedPlayers.contains(par2Str.trim().toLowerCase()))
        {
            par1NetLoginHandler.kickUser("You are banned from this server!");
            return null;
        }

        if (!isAllowedToLogin(par2Str))
        {
            par1NetLoginHandler.kickUser("You are not white-listed on this server!");
            return null;
        }

        String s = par1NetLoginHandler.netManager.getRemoteAddress().toString();
        s = s.substring(s.indexOf("/") + 1);
        s = s.substring(0, s.indexOf(":"));

        if (bannedIPs.contains(s))
        {
            par1NetLoginHandler.kickUser("Your IP address is banned from this server!");
            return null;
        }

        if (playerEntities.size() >= maxPlayers)
        {
            par1NetLoginHandler.kickUser("The server is full!");
            return null;
        }

        for (int i = 0; i < playerEntities.size(); i++)
        {
            EntityPlayerMP entityplayermp = (EntityPlayerMP)playerEntities.get(i);

            if (entityplayermp.username.equalsIgnoreCase(par2Str))
            {
                entityplayermp.playerNetServerHandler.kickPlayer("You logged in from another location");
            }
        }

        return new EntityPlayerMP(mcServer, mcServer.getWorldManager(0), par2Str, new ItemInWorldManager(mcServer.getWorldManager(0)));
    }

    /**
     * Called on respawn
     */
    public EntityPlayerMP recreatePlayerEntity(EntityPlayerMP par1EntityPlayerMP, int par2, boolean par3)
    {
        mcServer.getEntityTracker(par1EntityPlayerMP.dimension).removeTrackedPlayerSymmetric(par1EntityPlayerMP);
        mcServer.getEntityTracker(par1EntityPlayerMP.dimension).untrackEntity(par1EntityPlayerMP);
        getPlayerManager(par1EntityPlayerMP.dimension).removePlayer(par1EntityPlayerMP);
        playerEntities.remove(par1EntityPlayerMP);
        mcServer.getWorldManager(par1EntityPlayerMP.dimension).removePlayer(par1EntityPlayerMP);
        ChunkCoordinates chunkcoordinates = par1EntityPlayerMP.getSpawnChunk();
        par1EntityPlayerMP.dimension = par2;
        EntityPlayerMP entityplayermp = new EntityPlayerMP(mcServer, mcServer.getWorldManager(par1EntityPlayerMP.dimension), par1EntityPlayerMP.username, new ItemInWorldManager(mcServer.getWorldManager(par1EntityPlayerMP.dimension)));

        if (par3)
        {
            entityplayermp.copyPlayer(par1EntityPlayerMP);
        }

        entityplayermp.entityId = par1EntityPlayerMP.entityId;
        entityplayermp.playerNetServerHandler = par1EntityPlayerMP.playerNetServerHandler;
        WorldServer worldserver = mcServer.getWorldManager(par1EntityPlayerMP.dimension);
        entityplayermp.itemInWorldManager.toggleGameType(par1EntityPlayerMP.itemInWorldManager.getGameType());
        entityplayermp.itemInWorldManager.func_35695_b(worldserver.getWorldInfo().getGameType());

        if (chunkcoordinates != null)
        {
            ChunkCoordinates chunkcoordinates1 = EntityPlayer.verifyRespawnCoordinates(mcServer.getWorldManager(par1EntityPlayerMP.dimension), chunkcoordinates);

            if (chunkcoordinates1 != null)
            {
                entityplayermp.setLocationAndAngles((float)chunkcoordinates1.posX + 0.5F, (float)chunkcoordinates1.posY + 0.1F, (float)chunkcoordinates1.posZ + 0.5F, 0.0F, 0.0F);
                entityplayermp.setSpawnChunk(chunkcoordinates);
            }
            else
            {
                entityplayermp.playerNetServerHandler.sendPacket(new Packet70Bed(0, 0));
            }
        }

        worldserver.chunkProviderServer.loadChunk((int)entityplayermp.posX >> 4, (int)entityplayermp.posZ >> 4);

        for (; worldserver.getCollidingBoundingBoxes(entityplayermp, entityplayermp.boundingBox).size() != 0; entityplayermp.setPosition(entityplayermp.posX, entityplayermp.posY + 1.0D, entityplayermp.posZ)) { }

        entityplayermp.playerNetServerHandler.sendPacket(new Packet9Respawn(entityplayermp.dimension, (byte)entityplayermp.worldObj.difficultySetting, entityplayermp.worldObj.getWorldInfo().getTerrainType(), entityplayermp.worldObj.getHeight(), entityplayermp.itemInWorldManager.getGameType()));
        entityplayermp.playerNetServerHandler.teleportTo(entityplayermp.posX, entityplayermp.posY, entityplayermp.posZ, entityplayermp.rotationYaw, entityplayermp.rotationPitch);
        func_28170_a(entityplayermp, worldserver);
        getPlayerManager(entityplayermp.dimension).addPlayer(entityplayermp);
        worldserver.spawnEntityInWorld(entityplayermp);
        playerEntities.add(entityplayermp);
        entityplayermp.func_20057_k();
        entityplayermp.func_22068_s();
        return entityplayermp;
    }

    /**
     * moves provided player from overworld to nether or vice versa
     */
    public void sendPlayerToOtherDimension(EntityPlayerMP par1EntityPlayerMP, int par2)
    {
        int i = par1EntityPlayerMP.dimension;
        WorldServer worldserver = mcServer.getWorldManager(par1EntityPlayerMP.dimension);
        par1EntityPlayerMP.dimension = par2;
        WorldServer worldserver1 = mcServer.getWorldManager(par1EntityPlayerMP.dimension);
        par1EntityPlayerMP.playerNetServerHandler.sendPacket(new Packet9Respawn(par1EntityPlayerMP.dimension, (byte)par1EntityPlayerMP.worldObj.difficultySetting, worldserver1.getWorldInfo().getTerrainType(), worldserver1.getHeight(), par1EntityPlayerMP.itemInWorldManager.getGameType()));
        worldserver.removePlayer(par1EntityPlayerMP);
        par1EntityPlayerMP.isDead = false;
        double d = par1EntityPlayerMP.posX;
        double d1 = par1EntityPlayerMP.posZ;
        double d2 = 8D;

        if (par1EntityPlayerMP.dimension == -1)
        {
            d /= d2;
            d1 /= d2;
            par1EntityPlayerMP.setLocationAndAngles(d, par1EntityPlayerMP.posY, d1, par1EntityPlayerMP.rotationYaw, par1EntityPlayerMP.rotationPitch);

            if (par1EntityPlayerMP.isEntityAlive())
            {
                worldserver.updateEntityWithOptionalForce(par1EntityPlayerMP, false);
            }
        }
        else if (par1EntityPlayerMP.dimension == 0)
        {
            d *= d2;
            d1 *= d2;
            par1EntityPlayerMP.setLocationAndAngles(d, par1EntityPlayerMP.posY, d1, par1EntityPlayerMP.rotationYaw, par1EntityPlayerMP.rotationPitch);

            if (par1EntityPlayerMP.isEntityAlive())
            {
                worldserver.updateEntityWithOptionalForce(par1EntityPlayerMP, false);
            }
        }
        else
        {
            ChunkCoordinates chunkcoordinates = worldserver1.getEntrancePortalLocation();
            d = chunkcoordinates.posX;
            par1EntityPlayerMP.posY = chunkcoordinates.posY;
            d1 = chunkcoordinates.posZ;
            par1EntityPlayerMP.setLocationAndAngles(d, par1EntityPlayerMP.posY, d1, 90F, 0.0F);

            if (par1EntityPlayerMP.isEntityAlive())
            {
                worldserver.updateEntityWithOptionalForce(par1EntityPlayerMP, false);
            }
        }

        if (i != 1 && par1EntityPlayerMP.isEntityAlive())
        {
            worldserver1.spawnEntityInWorld(par1EntityPlayerMP);
            par1EntityPlayerMP.setLocationAndAngles(d, par1EntityPlayerMP.posY, d1, par1EntityPlayerMP.rotationYaw, par1EntityPlayerMP.rotationPitch);
            worldserver1.updateEntityWithOptionalForce(par1EntityPlayerMP, false);
            worldserver1.chunkProviderServer.chunkLoadOverride = true;
            (new Teleporter()).placeInPortal(worldserver1, par1EntityPlayerMP);
            worldserver1.chunkProviderServer.chunkLoadOverride = false;
        }

        joinNewPlayerManager(par1EntityPlayerMP);
        par1EntityPlayerMP.playerNetServerHandler.teleportTo(par1EntityPlayerMP.posX, par1EntityPlayerMP.posY, par1EntityPlayerMP.posZ, par1EntityPlayerMP.rotationYaw, par1EntityPlayerMP.rotationPitch);
        par1EntityPlayerMP.setWorld(worldserver1);
        par1EntityPlayerMP.itemInWorldManager.setWorld(worldserver1);
        func_28170_a(par1EntityPlayerMP, worldserver1);
        func_30008_g(par1EntityPlayerMP);
    }

    /**
     * self explanitory
     */
    public void onTick()
    {
        if (++field_35482_p > 200)
        {
            field_35482_p = 0;
        }

        if (field_35482_p < playerEntities.size())
        {
            EntityPlayerMP entityplayermp = (EntityPlayerMP)playerEntities.get(field_35482_p);
            sendPacketToAllPlayers(new Packet201PlayerInfo(entityplayermp.username, true, entityplayermp.ping));
        }

        for (int i = 0; i < playerManagerObj.length; i++)
        {
            playerManagerObj[i].updatePlayerInstances();
        }
    }

    public void markBlockNeedsUpdate(int par1, int par2, int par3, int par4)
    {
        getPlayerManager(par4).markBlockNeedsUpdate(par1, par2, par3);
    }

    /**
     * sends a packet to all players
     */
    public void sendPacketToAllPlayers(Packet par1Packet)
    {
        for (int i = 0; i < playerEntities.size(); i++)
        {
            EntityPlayerMP entityplayermp = (EntityPlayerMP)playerEntities.get(i);
            entityplayermp.playerNetServerHandler.sendPacket(par1Packet);
        }
    }

    /**
     * Sends a packet to all players in the specified Dimension
     */
    public void sendPacketToAllPlayersInDimension(Packet par1Packet, int par2)
    {
        for (int i = 0; i < playerEntities.size(); i++)
        {
            EntityPlayerMP entityplayermp = (EntityPlayerMP)playerEntities.get(i);

            if (entityplayermp.dimension == par2)
            {
                entityplayermp.playerNetServerHandler.sendPacket(par1Packet);
            }
        }
    }

    /**
     * returns a string containing a comma-seperated list of player names
     */
    public String getPlayerList()
    {
        String s = "";

        for (int i = 0; i < playerEntities.size(); i++)
        {
            if (i > 0)
            {
                s = (new StringBuilder()).append(s).append(", ").toString();
            }

            s = (new StringBuilder()).append(s).append(((EntityPlayerMP)playerEntities.get(i)).username).toString();
        }

        return s;
    }

    /**
     * Returns a list of usernames of all connected players
     */
    public String[] getPlayerNamesAsList()
    {
        String as[] = new String[playerEntities.size()];

        for (int i = 0; i < playerEntities.size(); i++)
        {
            as[i] = ((EntityPlayerMP)playerEntities.get(i)).username;
        }

        return as;
    }

    /**
     * add this player to the banned player list and save the ban list
     */
    public void banPlayer(String par1Str)
    {
        bannedPlayers.add(par1Str.toLowerCase());
        writeBannedPlayers();
    }

    /**
     * remove this player from the banned player list and save the ban list
     */
    public void pardonPlayer(String par1Str)
    {
        bannedPlayers.remove(par1Str.toLowerCase());
        writeBannedPlayers();
    }

    /**
     * Reads the banned players file from disk.
     */
    private void readBannedPlayers()
    {
        try
        {
            bannedPlayers.clear();
            BufferedReader bufferedreader = new BufferedReader(new FileReader(bannedPlayersFile));

            for (String s = ""; (s = bufferedreader.readLine()) != null;)
            {
                bannedPlayers.add(s.trim().toLowerCase());
            }

            bufferedreader.close();
        }
        catch (Exception exception)
        {
            logger.warning((new StringBuilder()).append("Failed to load ban list: ").append(exception).toString());
        }
    }

    /**
     * Writes the banned players file to disk.
     */
    private void writeBannedPlayers()
    {
        try
        {
            PrintWriter printwriter = new PrintWriter(new FileWriter(bannedPlayersFile, false));
            String s;

            for (Iterator iterator = bannedPlayers.iterator(); iterator.hasNext(); printwriter.println(s))
            {
                s = (String)iterator.next();
            }

            printwriter.close();
        }
        catch (Exception exception)
        {
            logger.warning((new StringBuilder()).append("Failed to save ban list: ").append(exception).toString());
        }
    }

    /**
     * Returns a list of banned player names
     */
    public Set getBannedPlayersList()
    {
        return bannedPlayers;
    }

    /**
     * Returns the list of banned IP addresses
     */
    public Set getBannedIPsList()
    {
        return bannedIPs;
    }

    /**
     * add the ip to the banned ip list and save ban list
     */
    public void banIP(String par1Str)
    {
        bannedIPs.add(par1Str.toLowerCase());
        saveBannedList();
    }

    /**
     * removes the ip from the banned ip list and save ban list
     */
    public void pardonIP(String par1Str)
    {
        bannedIPs.remove(par1Str.toLowerCase());
        saveBannedList();
    }

    /**
     * loads the list of banned players
     */
    private void loadBannedList()
    {
        try
        {
            bannedIPs.clear();
            BufferedReader bufferedreader = new BufferedReader(new FileReader(ipBanFile));

            for (String s = ""; (s = bufferedreader.readLine()) != null;)
            {
                bannedIPs.add(s.trim().toLowerCase());
            }

            bufferedreader.close();
        }
        catch (Exception exception)
        {
            logger.warning((new StringBuilder()).append("Failed to load ip ban list: ").append(exception).toString());
        }
    }

    /**
     * saves the list of banned players
     */
    private void saveBannedList()
    {
        try
        {
            PrintWriter printwriter = new PrintWriter(new FileWriter(ipBanFile, false));
            String s;

            for (Iterator iterator = bannedIPs.iterator(); iterator.hasNext(); printwriter.println(s))
            {
                s = (String)iterator.next();
            }

            printwriter.close();
        }
        catch (Exception exception)
        {
            logger.warning((new StringBuilder()).append("Failed to save ip ban list: ").append(exception).toString());
        }
    }

    /**
     * This adds a username to the ops list, then saves the op list
     */
    public void addOp(String par1Str)
    {
        ops.add(par1Str.toLowerCase());
        saveOps();
    }

    /**
     * This removes a username from the ops list, then saves the op list
     */
    public void removeOp(String par1Str)
    {
        ops.remove(par1Str.toLowerCase());
        saveOps();
    }

    /**
     * loads the ops from the ops file
     */
    private void loadOps()
    {
        try
        {
            ops.clear();
            BufferedReader bufferedreader = new BufferedReader(new FileReader(opFile));

            for (String s = ""; (s = bufferedreader.readLine()) != null;)
            {
                ops.add(s.trim().toLowerCase());
            }

            bufferedreader.close();
        }
        catch (Exception exception)
        {
            logger.warning((new StringBuilder()).append("Failed to load operators list: ").append(exception).toString());
        }
    }

    /**
     * saves the ops to the ops file
     */
    private void saveOps()
    {
        try
        {
            PrintWriter printwriter = new PrintWriter(new FileWriter(opFile, false));
            String s;

            for (Iterator iterator = ops.iterator(); iterator.hasNext(); printwriter.println(s))
            {
                s = (String)iterator.next();
            }

            printwriter.close();
        }
        catch (Exception exception)
        {
            logger.warning((new StringBuilder()).append("Failed to save operators list: ").append(exception).toString());
        }
    }

    /**
     * Loads the white list file
     */
    private void loadWhiteList()
    {
        try
        {
            whiteListedIPs.clear();
            BufferedReader bufferedreader = new BufferedReader(new FileReader(whitelistPlayersFile));

            for (String s = ""; (s = bufferedreader.readLine()) != null;)
            {
                whiteListedIPs.add(s.trim().toLowerCase());
            }

            bufferedreader.close();
        }
        catch (Exception exception)
        {
            logger.warning((new StringBuilder()).append("Failed to load white-list: ").append(exception).toString());
        }
    }

    /**
     * Saves the white list file
     */
    private void saveWhiteList()
    {
        try
        {
            PrintWriter printwriter = new PrintWriter(new FileWriter(whitelistPlayersFile, false));
            String s;

            for (Iterator iterator = whiteListedIPs.iterator(); iterator.hasNext(); printwriter.println(s))
            {
                s = (String)iterator.next();
            }

            printwriter.close();
        }
        catch (Exception exception)
        {
            logger.warning((new StringBuilder()).append("Failed to save white-list: ").append(exception).toString());
        }
    }

    /**
     * Determine if the player is allowed to connect based on current server settings
     */
    public boolean isAllowedToLogin(String par1Str)
    {
        par1Str = par1Str.trim().toLowerCase();
        return !whiteListEnforced || ops.contains(par1Str) || whiteListedIPs.contains(par1Str);
    }

    /**
     * Returns true if the player is an OP, false otherwise.
     */
    public boolean isOp(String par1Str)
    {
        return ops.contains(par1Str.trim().toLowerCase());
    }

    /**
     * gets the player entity for the player with the name specified
     */
    public EntityPlayerMP getPlayerEntity(String par1Str)
    {
        for (int i = 0; i < playerEntities.size(); i++)
        {
            EntityPlayerMP entityplayermp = (EntityPlayerMP)playerEntities.get(i);

            if (entityplayermp.username.equalsIgnoreCase(par1Str))
            {
                return entityplayermp;
            }
        }

        return null;
    }

    /**
     * sends a chat message to the player with the name specified (not necessarily a whisper)
     */
    public void sendChatMessageToPlayer(String par1Str, String par2Str)
    {
        EntityPlayerMP entityplayermp = getPlayerEntity(par1Str);

        if (entityplayermp != null)
        {
            entityplayermp.playerNetServerHandler.sendPacket(new Packet3Chat(par2Str));
        }
    }

    /**
     * sends a packet to players within d3 of point (x,y,z)
     */
    public void sendPacketToPlayersAroundPoint(double par1, double par3, double par5, double par7, int par9, Packet par10Packet)
    {
        func_28171_a(null, par1, par3, par5, par7, par9, par10Packet);
    }

    public void func_28171_a(EntityPlayer par1EntityPlayer, double par2, double par4, double par6, double par8, int par10, Packet par11Packet)
    {
        for (int i = 0; i < playerEntities.size(); i++)
        {
            EntityPlayerMP entityplayermp = (EntityPlayerMP)playerEntities.get(i);

            if (entityplayermp == par1EntityPlayer || entityplayermp.dimension != par10)
            {
                continue;
            }

            double d = par2 - entityplayermp.posX;
            double d1 = par4 - entityplayermp.posY;
            double d2 = par6 - entityplayermp.posZ;

            if (d * d + d1 * d1 + d2 * d2 < par8 * par8)
            {
                entityplayermp.playerNetServerHandler.sendPacket(par11Packet);
            }
        }
    }

    /**
     * sends a chat message to all ops currently connected
     */
    public void sendChatMessageToAllOps(String par1Str)
    {
        Packet3Chat packet3chat = new Packet3Chat(par1Str);

        for (int i = 0; i < playerEntities.size(); i++)
        {
            EntityPlayerMP entityplayermp = (EntityPlayerMP)playerEntities.get(i);

            if (isOp(entityplayermp.username))
            {
                entityplayermp.playerNetServerHandler.sendPacket(packet3chat);
            }
        }
    }

    /**
     * sends a packet to the player with the name specified
     */
    public boolean sendPacketToPlayer(String par1Str, Packet par2Packet)
    {
        EntityPlayerMP entityplayermp = getPlayerEntity(par1Str);

        if (entityplayermp != null)
        {
            entityplayermp.playerNetServerHandler.sendPacket(par2Packet);
            return true;
        }
        else
        {
            return false;
        }
    }

    /**
     * Saves all of the player's states
     */
    public void savePlayerStates()
    {
        for (int i = 0; i < playerEntities.size(); i++)
        {
            playerNBTManagerObj.writePlayerData((EntityPlayer)playerEntities.get(i));
        }
    }

    /**
     * sends a tilentity to the player name specified
     */
    public void sentTileEntityToPlayer(int i, int j, int k, TileEntity tileentity)
    {
    }

    /**
     * add the specified player to the white list
     */
    public void addToWhiteList(String par1Str)
    {
        whiteListedIPs.add(par1Str);
        saveWhiteList();
    }

    /**
     * remove the specified player from the whitelist
     */
    public void removeFromWhiteList(String par1Str)
    {
        whiteListedIPs.remove(par1Str);
        saveWhiteList();
    }

    /**
     * returns the set of whitelisted ip addresses
     */
    public Set getWhiteListedIPs()
    {
        return whiteListedIPs;
    }

    /**
     * reloads the whitelist
     */
    public void reloadWhiteList()
    {
        loadWhiteList();
    }

    public void func_28170_a(EntityPlayerMP par1EntityPlayerMP, WorldServer par2WorldServer)
    {
        par1EntityPlayerMP.playerNetServerHandler.sendPacket(new Packet4UpdateTime(par2WorldServer.getWorldTime()));

        if (par2WorldServer.isRaining())
        {
            par1EntityPlayerMP.playerNetServerHandler.sendPacket(new Packet70Bed(1, 0));
        }
    }

    public void func_30008_g(EntityPlayerMP par1EntityPlayerMP)
    {
        par1EntityPlayerMP.func_28017_a(par1EntityPlayerMP.inventorySlots);
        par1EntityPlayerMP.func_30001_B();
    }

    /**
     * Returns the number of players on the server
     */
    public int playersOnline()
    {
        return playerEntities.size();
    }

    /**
     * Returns maximum amount of players that can join the server
     */
    public int getMaxPlayers()
    {
        return maxPlayers;
    }

    public String[] func_52019_t()
    {
        return mcServer.worldMngr[0].getSaveHandler().getPlayerNBTManager().func_52007_g();
    }

    private void func_52018_u()
    {
        PlayerUsageSnooper playerusagesnooper = new PlayerUsageSnooper("server");
        playerusagesnooper.func_52014_a("version", mcServer.getVersionString());
        playerusagesnooper.func_52014_a("os_name", System.getProperty("os.name"));
        playerusagesnooper.func_52014_a("os_version", System.getProperty("os.version"));
        playerusagesnooper.func_52014_a("os_architecture", System.getProperty("os.arch"));
        playerusagesnooper.func_52014_a("memory_total", Long.valueOf(Runtime.getRuntime().totalMemory()));
        playerusagesnooper.func_52014_a("memory_max", Long.valueOf(Runtime.getRuntime().maxMemory()));
        playerusagesnooper.func_52014_a("memory_free", Long.valueOf(Runtime.getRuntime().freeMemory()));
        playerusagesnooper.func_52014_a("java_version", System.getProperty("java.version"));
        playerusagesnooper.func_52014_a("cpu_cores", Integer.valueOf(Runtime.getRuntime().availableProcessors()));
        playerusagesnooper.func_52014_a("players_current", Integer.valueOf(playersOnline()));
        playerusagesnooper.func_52014_a("players_max", Integer.valueOf(getMaxPlayers()));
        playerusagesnooper.func_52014_a("players_seen", Integer.valueOf(func_52019_t().length));
        playerusagesnooper.func_52014_a("uses_auth", Boolean.valueOf(mcServer.onlineMode));
        playerusagesnooper.func_52014_a("server_brand", mcServer.func_52003_getServerModName());
        playerusagesnooper.func_52012_a();
    }
}
