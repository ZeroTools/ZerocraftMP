package net.minecraft.src;

import java.util.Random;

public abstract class BlockFluid extends Block
{
    protected BlockFluid(int par1, Material par2Material)
    {
        super(par1, (par2Material != Material.lava ? 12 : 14) * 16 + 13, par2Material);
        float f = 0.0F;
        float f1 = 0.0F;
        setBlockBounds(0.0F + f1, 0.0F + f, 0.0F + f1, 1.0F + f1, 1.0F + f, 1.0F + f1);
        setTickRandomly(true);
    }

    public boolean getBlocksMovement(IBlockAccess par1IBlockAccess, int par2, int par3, int par4)
    {
        return blockMaterial != Material.lava;
    }

    /**
     * Returns the percentage of the fluid block that is air, based on the given flow decay of the fluid.
     */
    public static float getFluidHeightPercent(int par0)
    {
        if (par0 >= 8)
        {
            par0 = 0;
        }

        float f = (float)(par0 + 1) / 9F;
        return f;
    }

    /**
     * Returns the block texture based on the side being looked at.  Args: side
     */
    public int getBlockTextureFromSide(int par1)
    {
        if (par1 == 0 || par1 == 1)
        {
            return blockIndexInTexture;
        }
        else
        {
            return blockIndexInTexture + 1;
        }
    }

    /**
     * Returns the amount of fluid decay at the coordinates, or -1 if the block at the coordinates is not the same
     * material as the fluid.
     */
    protected int getFlowDecay(World par1World, int par2, int par3, int par4)
    {
        if (par1World.getBlockMaterial(par2, par3, par4) != blockMaterial)
        {
            return -1;
        }
        else
        {
            return par1World.getBlockMetadata(par2, par3, par4);
        }
    }

    /**
     * Returns the flow decay but converts values indicating falling liquid (values >=8) to their effective source block
     * value of zero.
     */
    protected int getEffectiveFlowDecay(IBlockAccess par1IBlockAccess, int par2, int par3, int par4)
    {
        if (par1IBlockAccess.getBlockMaterial(par2, par3, par4) != blockMaterial)
        {
            return -1;
        }

        int i = par1IBlockAccess.getBlockMetadata(par2, par3, par4);

        if (i >= 8)
        {
            i = 0;
        }

        return i;
    }

    /**
     * If this block doesn't render as an ordinary block it will return False (examples: signs, buttons, stairs, etc)
     */
    public boolean renderAsNormalBlock()
    {
        return false;
    }

    /**
     * Is this block (a) opaque and (b) a full 1m cube?  This determines whether or not to render the shared face of two
     * adjacent blocks and also whether the player can attach torches, redstone wire, etc to this block.
     */
    public boolean isOpaqueCube()
    {
        return false;
    }

    /**
     * Returns whether this block is collideable based on the arguments passed in Args: blockMetaData, unknownFlag
     */
    public boolean canCollideCheck(int par1, boolean par2)
    {
        return par2 && par1 == 0;
    }

    /**
     * Returns Returns true if the given side of this block type should be rendered (if it's solid or not), if the
     * adjacent block is at the given coordinates. Args: blockAccess, x, y, z, side
     */
    public boolean isBlockSolid(IBlockAccess par1IBlockAccess, int par2, int par3, int par4, int par5)
    {
        Material material = par1IBlockAccess.getBlockMaterial(par2, par3, par4);

        if (material == blockMaterial)
        {
            return false;
        }

        if (par5 == 1)
        {
            return true;
        }

        if (material == Material.ice)
        {
            return false;
        }
        else
        {
            return super.isBlockSolid(par1IBlockAccess, par2, par3, par4, par5);
        }
    }

    /**
     * Returns a bounding box from the pool of bounding boxes (this means this box can change after the pool has been
     * cleared to be reused)
     */
    public AxisAlignedBB getCollisionBoundingBoxFromPool(World par1World, int par2, int par3, int i)
    {
        return null;
    }

    /**
     * The type of render function that is called for this block
     */
    public int getRenderType()
    {
        return 4;
    }

    /**
     * Returns the ID of the items to drop on destruction.
     */
    public int idDropped(int par1, Random par2Random, int par3)
    {
        return 0;
    }

    /**
     * Returns the quantity of items to drop on block destruction.
     */
    public int quantityDropped(Random par1Random)
    {
        return 0;
    }

    /**
     * Returns a vector indicating the direction and intensity of fluid flow.
     */
    private Vec3D getFlowVector(IBlockAccess par1IBlockAccess, int par2, int par3, int par4)
    {
        Vec3D vec3d = Vec3D.createVector(0.0D, 0.0D, 0.0D);
        int i = getEffectiveFlowDecay(par1IBlockAccess, par2, par3, par4);

        for (int j = 0; j < 4; j++)
        {
            int k = par2;
            int l = par3;
            int i1 = par4;

            if (j == 0)
            {
                k--;
            }

            if (j == 1)
            {
                i1--;
            }

            if (j == 2)
            {
                k++;
            }

            if (j == 3)
            {
                i1++;
            }

            int j1 = getEffectiveFlowDecay(par1IBlockAccess, k, l, i1);

            if (j1 < 0)
            {
                if (par1IBlockAccess.getBlockMaterial(k, l, i1).blocksMovement())
                {
                    continue;
                }

                j1 = getEffectiveFlowDecay(par1IBlockAccess, k, l - 1, i1);

                if (j1 >= 0)
                {
                    int k1 = j1 - (i - 8);
                    vec3d = vec3d.addVector((k - par2) * k1, (l - par3) * k1, (i1 - par4) * k1);
                }

                continue;
            }

            if (j1 >= 0)
            {
                int l1 = j1 - i;
                vec3d = vec3d.addVector((k - par2) * l1, (l - par3) * l1, (i1 - par4) * l1);
            }
        }

        if (par1IBlockAccess.getBlockMetadata(par2, par3, par4) >= 8)
        {
            boolean flag = false;

            if (flag || isBlockSolid(par1IBlockAccess, par2, par3, par4 - 1, 2))
            {
                flag = true;
            }

            if (flag || isBlockSolid(par1IBlockAccess, par2, par3, par4 + 1, 3))
            {
                flag = true;
            }

            if (flag || isBlockSolid(par1IBlockAccess, par2 - 1, par3, par4, 4))
            {
                flag = true;
            }

            if (flag || isBlockSolid(par1IBlockAccess, par2 + 1, par3, par4, 5))
            {
                flag = true;
            }

            if (flag || isBlockSolid(par1IBlockAccess, par2, par3 + 1, par4 - 1, 2))
            {
                flag = true;
            }

            if (flag || isBlockSolid(par1IBlockAccess, par2, par3 + 1, par4 + 1, 3))
            {
                flag = true;
            }

            if (flag || isBlockSolid(par1IBlockAccess, par2 - 1, par3 + 1, par4, 4))
            {
                flag = true;
            }

            if (flag || isBlockSolid(par1IBlockAccess, par2 + 1, par3 + 1, par4, 5))
            {
                flag = true;
            }

            if (flag)
            {
                vec3d = vec3d.normalize().addVector(0.0D, -6D, 0.0D);
            }
        }

        vec3d = vec3d.normalize();
        return vec3d;
    }

    /**
     * Can add to the passed in vector for a movement vector to be applied to the entity. Args: x, y, z, entity, vec3d
     */
    public void velocityToAddToEntity(World par1World, int par2, int par3, int par4, Entity par5Entity, Vec3D par6Vec3D)
    {
        Vec3D vec3d = getFlowVector(par1World, par2, par3, par4);
        par6Vec3D.xCoord += vec3d.xCoord;
        par6Vec3D.yCoord += vec3d.yCoord;
        par6Vec3D.zCoord += vec3d.zCoord;
    }

    /**
     * How many world ticks before ticking
     */
    public int tickRate()
    {
        if (blockMaterial == Material.water)
        {
            return 5;
        }

        return blockMaterial != Material.lava ? 0 : 30;
    }

    /**
     * Ticks the block if it's been scheduled
     */
    public void updateTick(World par1World, int par2, int par3, int par4, Random par5Random)
    {
        super.updateTick(par1World, par2, par3, par4, par5Random);
    }

    /**
     * Called whenever the block is added into the world. Args: world, x, y, z
     */
    public void onBlockAdded(World par1World, int par2, int par3, int par4)
    {
        checkForHarden(par1World, par2, par3, par4);
    }

    /**
     * Lets the block know when one of its neighbor changes. Doesn't know which neighbor changed (coordinates passed are
     * their own) Args: x, y, z, neighbor blockID
     */
    public void onNeighborBlockChange(World par1World, int par2, int par3, int par4, int par5)
    {
        checkForHarden(par1World, par2, par3, par4);
    }

    /**
     * Forces lava to check to see if it is colliding with water, and then decide what it should harden to.
     */
    private void checkForHarden(World par1World, int par2, int par3, int par4)
    {
        if (par1World.getBlockId(par2, par3, par4) != blockID)
        {
            return;
        }

        if (blockMaterial == Material.lava)
        {
            boolean flag = false;

            if (flag || par1World.getBlockMaterial(par2, par3, par4 - 1) == Material.water)
            {
                flag = true;
            }

            if (flag || par1World.getBlockMaterial(par2, par3, par4 + 1) == Material.water)
            {
                flag = true;
            }

            if (flag || par1World.getBlockMaterial(par2 - 1, par3, par4) == Material.water)
            {
                flag = true;
            }

            if (flag || par1World.getBlockMaterial(par2 + 1, par3, par4) == Material.water)
            {
                flag = true;
            }

            if (flag || par1World.getBlockMaterial(par2, par3 + 1, par4) == Material.water)
            {
                flag = true;
            }

            if (flag)
            {
                int i = par1World.getBlockMetadata(par2, par3, par4);

                if (i == 0)
                {
                    par1World.setBlockWithNotify(par2, par3, par4, Block.obsidian.blockID);
                }
                else if (i <= 4)
                {
                    par1World.setBlockWithNotify(par2, par3, par4, Block.cobblestone.blockID);
                }

                triggerLavaMixEffects(par1World, par2, par3, par4);
            }
        }
    }

    /**
     * Creates fizzing sound and smoke. Used when lava flows over block or mixes with water.
     */
    protected void triggerLavaMixEffects(World par1World, int par2, int par3, int par4)
    {
        par1World.playSoundEffect((float)par2 + 0.5F, (float)par3 + 0.5F, (float)par4 + 0.5F, "random.fizz", 0.5F, 2.6F + (par1World.rand.nextFloat() - par1World.rand.nextFloat()) * 0.8F);

        for (int i = 0; i < 8; i++)
        {
            par1World.spawnParticle("largesmoke", (double)par2 + Math.random(), (double)par3 + 1.2D, (double)par4 + Math.random(), 0.0D, 0.0D, 0.0D);
        }
    }
}
