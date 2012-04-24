package net.minecraft.src;

import java.util.Random;

public class BlockLockedChest extends Block
{
    protected BlockLockedChest(int par1)
    {
        super(par1, Material.wood);
        blockIndexInTexture = 26;
    }

    /**
     * Returns the block texture based on the side being looked at.  Args: side
     */
    public int getBlockTextureFromSide(int par1)
    {
        if (par1 == 1)
        {
            return blockIndexInTexture - 1;
        }

        if (par1 == 0)
        {
            return blockIndexInTexture - 1;
        }

        if (par1 == 3)
        {
            return blockIndexInTexture + 1;
        }
        else
        {
            return blockIndexInTexture;
        }
    }

    /**
     * Checks to see if its valid to put this block at the specified coordinates. Args: world, x, y, z
     */
    public boolean canPlaceBlockAt(World par1World, int par2, int par3, int i)
    {
        return true;
    }

    /**
     * Ticks the block if it's been scheduled
     */
    public void updateTick(World par1World, int par2, int par3, int par4, Random par5Random)
    {
        par1World.setBlockWithNotify(par2, par3, par4, 0);
    }
}
