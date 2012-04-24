package net.minecraft.src;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.server.MinecraftServer;

public class PlayerManager
{
    /** players in the current instance */
    public List players;

    /** the hash of all playerInstances created */
    private LongHashMap playerInstances;

    /** the playerInstances(chunks) that need to be updated */
    private List playerInstancesToUpdate;

    /** Reference to the MinecraftServer object. */
    private MinecraftServer mcServer;

    /**
     * Holds the player dimension object. 0 is the surface, -1 is the Nether.
     */
    private int playerDimension;

    /**
     * Number of chunks the server sends to the client. Valid 3<=x<=15. In server.properties.
     */
    private int playerViewRadius;
    private final int xzDirectionsConst[][] =
    {
        {
            1, 0
        }, {
            0, 1
        }, {
            -1, 0
        }, {
            0, -1
        }
    };

    public PlayerManager(MinecraftServer par1MinecraftServer, int par2, int par3)
    {
        players = new ArrayList();
        playerInstances = new LongHashMap();
        playerInstancesToUpdate = new ArrayList();

        if (par3 > 15)
        {
            throw new IllegalArgumentException("Too big view radius!");
        }

        if (par3 < 3)
        {
            throw new IllegalArgumentException("Too small view radius!");
        }
        else
        {
            playerViewRadius = par3;
            mcServer = par1MinecraftServer;
            playerDimension = par2;
            return;
        }
    }

    /**
     * Returns the MinecraftServer associated with the PlayerManager.
     */
    public WorldServer getMinecraftServer()
    {
        return mcServer.getWorldManager(playerDimension);
    }

    /**
     * updates all the player instances that need to be updated
     */
    public void updatePlayerInstances()
    {
        for (int i = 0; i < playerInstancesToUpdate.size(); i++)
        {
            ((PlayerInstance)playerInstancesToUpdate.get(i)).onUpdate();
        }

        playerInstancesToUpdate.clear();

        if (players.isEmpty())
        {
            WorldServer worldserver = mcServer.getWorldManager(playerDimension);
            WorldProvider worldprovider = worldserver.worldProvider;

            if (!worldprovider.canRespawnHere())
            {
                worldserver.chunkProviderServer.unloadAllChunks();
            }
        }
    }

    /**
     * passi n the chunk x and y and a flag as to whether or not the instance should be made if it doesnt exist
     */
    private PlayerInstance getPlayerInstance(int par1, int par2, boolean par3)
    {
        long l = (long)par1 + 0x7fffffffL | (long)par2 + 0x7fffffffL << 32;
        PlayerInstance playerinstance = (PlayerInstance)playerInstances.getValueByKey(l);

        if (playerinstance == null && par3)
        {
            playerinstance = new PlayerInstance(this, par1, par2);
            playerInstances.add(l, playerinstance);
        }

        return playerinstance;
    }

    public void markBlockNeedsUpdate(int par1, int par2, int par3)
    {
        int i = par1 >> 4;
        int j = par3 >> 4;
        PlayerInstance playerinstance = getPlayerInstance(i, j, false);

        if (playerinstance != null)
        {
            playerinstance.markBlockNeedsUpdate(par1 & 0xf, par2, par3 & 0xf);
        }
    }

    /**
     * Adds an EntityPlayerMP to the PlayerManager.
     */
    public void addPlayer(EntityPlayerMP par1EntityPlayerMP)
    {
        int i = (int)par1EntityPlayerMP.posX >> 4;
        int j = (int)par1EntityPlayerMP.posZ >> 4;
        par1EntityPlayerMP.managedPosX = par1EntityPlayerMP.posX;
        par1EntityPlayerMP.managedPosZ = par1EntityPlayerMP.posZ;
        int k = 0;
        int l = playerViewRadius;
        int i1 = 0;
        int j1 = 0;
        getPlayerInstance(i, j, true).addPlayer(par1EntityPlayerMP);

        for (int k1 = 1; k1 <= l * 2; k1++)
        {
            for (int i2 = 0; i2 < 2; i2++)
            {
                int ai[] = xzDirectionsConst[k++ % 4];

                for (int j2 = 0; j2 < k1; j2++)
                {
                    i1 += ai[0];
                    j1 += ai[1];
                    getPlayerInstance(i + i1, j + j1, true).addPlayer(par1EntityPlayerMP);
                }
            }
        }

        k %= 4;

        for (int l1 = 0; l1 < l * 2; l1++)
        {
            i1 += xzDirectionsConst[k][0];
            j1 += xzDirectionsConst[k][1];
            getPlayerInstance(i + i1, j + j1, true).addPlayer(par1EntityPlayerMP);
        }

        players.add(par1EntityPlayerMP);
    }

    /**
     * Removes an EntityPlayerMP from the PlayerManager.
     */
    public void removePlayer(EntityPlayerMP par1EntityPlayerMP)
    {
        int i = (int)par1EntityPlayerMP.managedPosX >> 4;
        int j = (int)par1EntityPlayerMP.managedPosZ >> 4;

        for (int k = i - playerViewRadius; k <= i + playerViewRadius; k++)
        {
            for (int l = j - playerViewRadius; l <= j + playerViewRadius; l++)
            {
                PlayerInstance playerinstance = getPlayerInstance(k, l, false);

                if (playerinstance != null)
                {
                    playerinstance.removePlayer(par1EntityPlayerMP);
                }
            }
        }

        players.remove(par1EntityPlayerMP);
    }

    /**
     * args: targetChunkX, targetChunkZ, playerChunkX, playerChunkZ - return true if the target chunk is outside the
     * cube of player visibility
     */
    private boolean isOutsidePlayerViewRadius(int par1, int par2, int par3, int par4)
    {
        int i = par1 - par3;
        int j = par2 - par4;

        if (i < -playerViewRadius || i > playerViewRadius)
        {
            return false;
        }

        return j >= -playerViewRadius && j <= playerViewRadius;
    }

    /**
     * update chunks around a player being moved by server logic (e.g. cart, boat)
     */
    public void updateMountedMovingPlayer(EntityPlayerMP par1EntityPlayerMP)
    {
        int i = (int)par1EntityPlayerMP.posX >> 4;
        int j = (int)par1EntityPlayerMP.posZ >> 4;
        double d = par1EntityPlayerMP.managedPosX - par1EntityPlayerMP.posX;
        double d1 = par1EntityPlayerMP.managedPosZ - par1EntityPlayerMP.posZ;
        double d2 = d * d + d1 * d1;

        if (d2 < 64D)
        {
            return;
        }

        int k = (int)par1EntityPlayerMP.managedPosX >> 4;
        int l = (int)par1EntityPlayerMP.managedPosZ >> 4;
        int i1 = i - k;
        int j1 = j - l;

        if (i1 == 0 && j1 == 0)
        {
            return;
        }

        for (int k1 = i - playerViewRadius; k1 <= i + playerViewRadius; k1++)
        {
            for (int l1 = j - playerViewRadius; l1 <= j + playerViewRadius; l1++)
            {
                if (!isOutsidePlayerViewRadius(k1, l1, k, l))
                {
                    getPlayerInstance(k1, l1, true).addPlayer(par1EntityPlayerMP);
                }

                if (isOutsidePlayerViewRadius(k1 - i1, l1 - j1, i, j))
                {
                    continue;
                }

                PlayerInstance playerinstance = getPlayerInstance(k1 - i1, l1 - j1, false);

                if (playerinstance != null)
                {
                    playerinstance.removePlayer(par1EntityPlayerMP);
                }
            }
        }

        par1EntityPlayerMP.managedPosX = par1EntityPlayerMP.posX;
        par1EntityPlayerMP.managedPosZ = par1EntityPlayerMP.posZ;
    }

    public int getMaxTrackingDistance()
    {
        return playerViewRadius * 16 - 16;
    }

    /**
     * get the hash of all player instances
     */
    static LongHashMap getPlayerInstances(PlayerManager par0PlayerManager)
    {
        return par0PlayerManager.playerInstances;
    }

    /**
     * retrieve the list of all playerInstances that need to be updated on tick
     */
    static List getPlayerInstancesToUpdate(PlayerManager par0PlayerManager)
    {
        return par0PlayerManager.playerInstancesToUpdate;
    }
}
