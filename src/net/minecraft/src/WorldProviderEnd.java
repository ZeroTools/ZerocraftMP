package net.minecraft.src;

public class WorldProviderEnd extends WorldProvider
{
    public WorldProviderEnd()
    {
    }

    /**
     * creates a new world chunk manager for WorldProvider
     */
    public void registerWorldChunkManager()
    {
        worldChunkMgr = new WorldChunkManagerHell(BiomeGenBase.sky, 0.5F, 0.0F);
        worldType = 1;
        hasNoSky = true;
    }

    /**
     * Returns the chunk provider back for the world provider
     */
    public IChunkProvider getChunkProvider()
    {
        return new ChunkProviderEnd(worldObj, worldObj.getSeed());
    }

    /**
     * Calculates the angle of sun and moon in the sky relative to a specified time (usually worldTime)
     */
    public float calculateCelestialAngle(long par1, float par3)
    {
        return 0.0F;
    }

    /**
     * True if the player can respawn in this dimension (true = overworld, false = nether).
     */
    public boolean canRespawnHere()
    {
        return false;
    }

    public boolean func_48567_d()
    {
        return false;
    }

    /**
     * Will check if the x, z position specified is alright to be set as the map spawn point
     */
    public boolean canCoordinateBeSpawn(int par1, int par2)
    {
        int i = worldObj.getFirstUncoveredBlock(par1, par2);

        if (i == 0)
        {
            return false;
        }
        else
        {
            return Block.blocksList[i].blockMaterial.blocksMovement();
        }
    }

    /**
     * Gets the hard-coded portal location to use when entering this dimension
     */
    public ChunkCoordinates getEntrancePortalLocation()
    {
        return new ChunkCoordinates(100, 50, 0);
    }

    public int getAverageGroundLevel()
    {
        return 50;
    }
}
