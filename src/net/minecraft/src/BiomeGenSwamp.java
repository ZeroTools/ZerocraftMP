package net.minecraft.src;

import java.util.Random;

public class BiomeGenSwamp extends BiomeGenBase
{
    protected BiomeGenSwamp(int par1)
    {
        super(par1);
        biomeDecorator.treesPerChunk = 2;
        biomeDecorator.flowersPerChunk = -999;
        biomeDecorator.deadBushPerChunk = 1;
        biomeDecorator.mushroomsPerChunk = 8;
        biomeDecorator.reedsPerChunk = 10;
        biomeDecorator.clayPerChunk = 1;
        biomeDecorator.waterlilyPerChunk = 4;
        waterColorMultiplier = 0xe0ffae;
    }

    /**
     * Gets a WorldGen appropriate for this biome.
     */
    public WorldGenerator getRandomWorldGenForTrees(Random par1Random)
    {
        return worldGenSwamp;
    }
}
