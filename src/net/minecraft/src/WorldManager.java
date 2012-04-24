package net.minecraft.src;

import net.minecraft.server.MinecraftServer;

public class WorldManager implements IWorldAccess
{
    /** Reference to the MinecraftServer object. */
    private MinecraftServer mcServer;

    /** The world itself. */
    private WorldServer world;

    public WorldManager(MinecraftServer par1MinecraftServer, WorldServer par2WorldServer)
    {
        mcServer = par1MinecraftServer;
        world = par2WorldServer;
    }

    /**
     * Spawns a particle. Arg: particleType, x, y, z, velX, velY, velZ
     */
    public void spawnParticle(String s, double d, double d1, double d2, double d3, double d4, double d5)
    {
    }

    /**
     * Start the skin for this entity downloading, if necessary, and increment its reference counter
     */
    public void obtainEntitySkin(Entity par1Entity)
    {
        mcServer.getEntityTracker(world.worldProvider.worldType).trackEntity(par1Entity);
    }

    /**
     * Decrement the reference counter for this entity's skin image data
     */
    public void releaseEntitySkin(Entity par1Entity)
    {
        mcServer.getEntityTracker(world.worldProvider.worldType).untrackEntity(par1Entity);
    }

    /**
     * Plays the specified sound. Arg: x, y, z, soundName, unknown1, unknown2
     */
    public void playSound(String s, double d, double d1, double d2, float f, float f1)
    {
    }

    /**
     * Called across all registered IWorldAccess instances when a block range is invalidated. Args: minX, minY, minZ,
     * maxX, maxY, maxZ
     */
    public void markBlockRangeNeedsUpdate(int i, int j, int k, int l, int i1, int j1)
    {
    }

    /**
     * Will mark the block and neighbors that their renderers need an update (could be all the same renderer
     * potentially) Args: x, y, z
     */
    public void markBlockNeedsUpdate(int par1, int par2, int par3)
    {
        mcServer.configManager.markBlockNeedsUpdate(par1, par2, par3, world.worldProvider.worldType);
    }

    /**
     * As of mc 1.2.3 this method has exactly the same signature and does exactly the same as markBlockNeedsUpdate
     */
    public void markBlockNeedsUpdate2(int i, int j, int k)
    {
    }

    /**
     * Plays the specified record. Arg: recordName, x, y, z
     */
    public void playRecord(String s, int i, int j, int k)
    {
    }

    /**
     * In all implementations, this method does nothing.
     */
    public void doNothingWithTileEntity(int par1, int par2, int par3, TileEntity par4TileEntity)
    {
        mcServer.configManager.sentTileEntityToPlayer(par1, par2, par3, par4TileEntity);
    }

    /**
     * Plays a pre-canned sound effect along with potentially auxiliary data-driven one-shot behaviour (particles, etc).
     */
    public void playAuxSFX(EntityPlayer par1EntityPlayer, int par2, int par3, int par4, int par5, int par6)
    {
        mcServer.configManager.func_28171_a(par1EntityPlayer, par3, par4, par5, 64D, world.worldProvider.worldType, new Packet61DoorChange(par2, par3, par4, par5, par6));
    }
}
