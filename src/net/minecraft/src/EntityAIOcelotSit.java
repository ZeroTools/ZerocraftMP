package net.minecraft.src;

import java.util.Random;

public class EntityAIOcelotSit extends EntityAIBase
{
    private final EntityOcelot field_50019_a;
    private final float field_50017_b;
    private int field_50018_c;
    private int field_52005_h;
    private int field_50015_d;
    private int field_50016_e;
    private int field_50013_f;
    private int field_50014_g;

    public EntityAIOcelotSit(EntityOcelot par1EntityOcelot, float par2)
    {
        field_50018_c = 0;
        field_52005_h = 0;
        field_50015_d = 0;
        field_50016_e = 0;
        field_50013_f = 0;
        field_50014_g = 0;
        field_50019_a = par1EntityOcelot;
        field_50017_b = par2;
        setMutexBits(5);
    }

    /**
     * Returns whether the EntityAIBase should begin execution.
     */
    public boolean shouldExecute()
    {
        return field_50019_a.isTamed() && !field_50019_a.isSitting() && field_50019_a.getRNG().nextDouble() <= 0.0065000001341104507D && func_50012_f();
    }

    /**
     * Returns whether an in-progress EntityAIBase should continue executing
     */
    public boolean continueExecuting()
    {
        return field_50018_c <= field_50015_d && field_52005_h <= 60 && func_50011_a(field_50019_a.worldObj, field_50016_e, field_50013_f, field_50014_g);
    }

    /**
     * Execute a one shot task or start executing a continuous task
     */
    public void startExecuting()
    {
        field_50019_a.getNavigator().func_48658_a((double)(float)field_50016_e + 0.5D, field_50013_f + 1, (double)(float)field_50014_g + 0.5D, field_50017_b);
        field_50018_c = 0;
        field_52005_h = 0;
        field_50015_d = field_50019_a.getRNG().nextInt(field_50019_a.getRNG().nextInt(1200) + 1200) + 1200;
        field_50019_a.func_50021_C().func_48210_a(false);
    }

    /**
     * Resets the task
     */
    public void resetTask()
    {
        field_50019_a.func_48369_c(false);
    }

    /**
     * Updates the task
     */
    public void updateTask()
    {
        field_50018_c++;
        field_50019_a.func_50021_C().func_48210_a(false);

        if (field_50019_a.getDistanceSq(field_50016_e, field_50013_f + 1, field_50014_g) > 1.0D)
        {
            field_50019_a.func_48369_c(false);
            field_50019_a.getNavigator().func_48658_a((double)(float)field_50016_e + 0.5D, field_50013_f + 1, (double)(float)field_50014_g + 0.5D, field_50017_b);
            field_52005_h++;
        }
        else if (!field_50019_a.isSitting())
        {
            field_50019_a.func_48369_c(true);
        }
        else
        {
            field_52005_h--;
        }
    }

    private boolean func_50012_f()
    {
        int i = (int)field_50019_a.posY;
        double d = 2147483647D;

        for (int j = (int)field_50019_a.posX - 8; (double)j < field_50019_a.posX + 8D; j++)
        {
            for (int k = (int)field_50019_a.posZ - 8; (double)k < field_50019_a.posZ + 8D; k++)
            {
                if (!func_50011_a(field_50019_a.worldObj, j, i, k) || !field_50019_a.worldObj.isAirBlock(j, i + 1, k))
                {
                    continue;
                }

                double d1 = field_50019_a.getDistanceSq(j, i, k);

                if (d1 < d)
                {
                    field_50016_e = j;
                    field_50013_f = i;
                    field_50014_g = k;
                    d = d1;
                }
            }
        }

        return d < 2147483647D;
    }

    private boolean func_50011_a(World par1World, int par2, int par3, int par4)
    {
        int i = par1World.getBlockId(par2, par3, par4);
        int j = par1World.getBlockMetadata(par2, par3, par4);

        if (i == Block.chest.blockID)
        {
            TileEntityChest tileentitychest = (TileEntityChest)par1World.getBlockTileEntity(par2, par3, par4);

            if (tileentitychest.numUsingPlayers < 1)
            {
                return true;
            }
        }
        else
        {
            if (i == Block.stoneOvenActive.blockID)
            {
                return true;
            }

            if (i == Block.bed.blockID && !BlockBed.isBlockFootOfBed(j))
            {
                return true;
            }
        }

        return false;
    }
}
