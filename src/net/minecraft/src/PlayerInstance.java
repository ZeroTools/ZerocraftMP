package net.minecraft.src;

import java.util.*;

class PlayerInstance
{
    /** the list of all players in this instance (chunk) */
    private List players;

    /** the x coordinate of the chunk they are in */
    private int chunkX;

    /** the z coordinate of the chunk they are in */
    private int chunkZ;

    /** the chunk the player currently resides in */
    private ChunkCoordIntPair currentChunk;
    private short blocksToUpdate[];

    /** the number of blocks that need to be updated next tick */
    private int numBlocksToUpdate;
    private int field_48475_h;
    final PlayerManager playerManager;

    public PlayerInstance(PlayerManager par1PlayerManager, int par2, int par3)
    {
        playerManager = par1PlayerManager;
        players = new ArrayList();
        blocksToUpdate = new short[64];
        numBlocksToUpdate = 0;
        chunkX = par2;
        chunkZ = par3;
        currentChunk = new ChunkCoordIntPair(par2, par3);
        par1PlayerManager.getMinecraftServer().chunkProviderServer.loadChunk(par2, par3);
    }

    /**
     * adds this player to the playerInstance
     */
    public void addPlayer(EntityPlayerMP par1EntityPlayerMP)
    {
        if (players.contains(par1EntityPlayerMP))
        {
            throw new IllegalStateException((new StringBuilder()).append("Failed to add player. ").append(par1EntityPlayerMP).append(" already is in chunk ").append(chunkX).append(", ").append(chunkZ).toString());
        }
        else
        {
            par1EntityPlayerMP.listeningChunks.add(currentChunk);
            par1EntityPlayerMP.playerNetServerHandler.sendPacket(new Packet50PreChunk(currentChunk.chunkXPos, currentChunk.chunkZPos, true));
            players.add(par1EntityPlayerMP);
            par1EntityPlayerMP.loadedChunks.add(currentChunk);
            return;
        }
    }

    /**
     * remove player from this instance
     */
    public void removePlayer(EntityPlayerMP par1EntityPlayerMP)
    {
        if (!players.contains(par1EntityPlayerMP))
        {
            return;
        }

        players.remove(par1EntityPlayerMP);

        if (players.size() == 0)
        {
            long l = (long)chunkX + 0x7fffffffL | (long)chunkZ + 0x7fffffffL << 32;
            PlayerManager.getPlayerInstances(playerManager).remove(l);

            if (numBlocksToUpdate > 0)
            {
                PlayerManager.getPlayerInstancesToUpdate(playerManager).remove(this);
            }

            playerManager.getMinecraftServer().chunkProviderServer.dropChunk(chunkX, chunkZ);
        }

        par1EntityPlayerMP.loadedChunks.remove(currentChunk);

        if (par1EntityPlayerMP.listeningChunks.contains(currentChunk))
        {
            par1EntityPlayerMP.playerNetServerHandler.sendPacket(new Packet50PreChunk(chunkX, chunkZ, false));
        }
    }

    /**
     * mark the block as changed so that it will update clients who need to know about it
     */
    public void markBlockNeedsUpdate(int par1, int par2, int par3)
    {
        if (numBlocksToUpdate == 0)
        {
            PlayerManager.getPlayerInstancesToUpdate(playerManager).add(this);
        }

        field_48475_h |= 1 << (par2 >> 4);

        if (numBlocksToUpdate < 64)
        {
            short word0 = (short)(par1 << 12 | par3 << 8 | par2);

            for (int i = 0; i < numBlocksToUpdate; i++)
            {
                if (blocksToUpdate[i] == word0)
                {
                    return;
                }
            }

            blocksToUpdate[numBlocksToUpdate++] = word0;
        }
    }

    /**
     * sends the packet to all players in the current instance
     */
    public void sendPacketToPlayersInInstance(Packet par1Packet)
    {
        for (int i = 0; i < players.size(); i++)
        {
            EntityPlayerMP entityplayermp = (EntityPlayerMP)players.get(i);

            if (entityplayermp.listeningChunks.contains(currentChunk) && !entityplayermp.loadedChunks.contains(currentChunk))
            {
                entityplayermp.playerNetServerHandler.sendPacket(par1Packet);
            }
        }
    }

    public void onUpdate()
    {
        WorldServer worldserver = playerManager.getMinecraftServer();

        if (numBlocksToUpdate == 0)
        {
            return;
        }

        if (numBlocksToUpdate == 1)
        {
            int i = chunkX * 16 + (blocksToUpdate[0] >> 12 & 0xf);
            int l = blocksToUpdate[0] & 0xff;
            int k1 = chunkZ * 16 + (blocksToUpdate[0] >> 8 & 0xf);
            sendPacketToPlayersInInstance(new Packet53BlockChange(i, l, k1, worldserver));

            if (worldserver.func_48084_h(i, l, k1))
            {
                updateTileEntity(worldserver.getBlockTileEntity(i, l, k1));
            }
        }
        else if (numBlocksToUpdate == 64)
        {
            int j = chunkX * 16;
            int i1 = chunkZ * 16;
            sendPacketToPlayersInInstance(new Packet51MapChunk(worldserver.getChunkFromChunkCoords(chunkX, chunkZ), false, field_48475_h));

            for (int l1 = 0; l1 < 16; l1++)
            {
                if ((field_48475_h & 1 << l1) != 0)
                {
                    int j2 = l1 << 4;
                    List list = worldserver.getTileEntityList(j, j2, i1, j + 16, j2 + 16, i1 + 16);

                    for (int l2 = 0; l2 < list.size(); l2++)
                    {
                        updateTileEntity((TileEntity)list.get(l2));
                    }
                }
            }
        }
        else
        {
            sendPacketToPlayersInInstance(new Packet52MultiBlockChange(chunkX, chunkZ, blocksToUpdate, numBlocksToUpdate, worldserver));

            for (int k = 0; k < numBlocksToUpdate; k++)
            {
                int j1 = chunkX * 16 + (blocksToUpdate[k] >> 12 & 0xf);
                int i2 = blocksToUpdate[k] & 0xff;
                int k2 = chunkZ * 16 + (blocksToUpdate[k] >> 8 & 0xf);

                if (worldserver.func_48084_h(j1, i2, k2))
                {
                    updateTileEntity(worldserver.getBlockTileEntity(j1, i2, k2));
                }
            }
        }

        numBlocksToUpdate = 0;
        field_48475_h = 0;
    }

    /**
     * sends players update packet about the given entity
     */
    private void updateTileEntity(TileEntity par1TileEntity)
    {
        if (par1TileEntity != null)
        {
            Packet packet = par1TileEntity.getDescriptionPacket();

            if (packet != null)
            {
                sendPacketToPlayersInInstance(packet);
            }
        }
    }
}
