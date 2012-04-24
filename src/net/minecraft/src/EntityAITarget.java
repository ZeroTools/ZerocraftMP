package net.minecraft.src;

import java.util.Random;

public abstract class EntityAITarget extends EntityAIBase
{
    /** The entity that this task belongs to */
    protected EntityLiving taskOwner;
    protected float field_48288_d;
    protected boolean field_48289_e;
    private boolean field_48292_a;
    private int field_48290_b;
    private int field_48286_f;
    private int field_48287_g;

    public EntityAITarget(EntityLiving par1EntityLiving, float par2, boolean par3)
    {
        this(par1EntityLiving, par2, par3, false);
    }

    public EntityAITarget(EntityLiving par1EntityLiving, float par2, boolean par3, boolean par4)
    {
        field_48290_b = 0;
        field_48286_f = 0;
        field_48287_g = 0;
        taskOwner = par1EntityLiving;
        field_48288_d = par2;
        field_48289_e = par3;
        field_48292_a = par4;
    }

    /**
     * Returns whether an in-progress EntityAIBase should continue executing
     */
    public boolean continueExecuting()
    {
        EntityLiving entityliving = taskOwner.getAttackTarget();

        if (entityliving == null)
        {
            return false;
        }

        if (!entityliving.isEntityAlive())
        {
            return false;
        }

        if (taskOwner.getDistanceSqToEntity(entityliving) > (double)(field_48288_d * field_48288_d))
        {
            return false;
        }

        if (field_48289_e)
        {
            if (!taskOwner.func_48318_al().canSee(entityliving))
            {
                if (++field_48287_g > 60)
                {
                    return false;
                }
            }
            else
            {
                field_48287_g = 0;
            }
        }

        return true;
    }

    /**
     * Execute a one shot task or start executing a continuous task
     */
    public void startExecuting()
    {
        field_48290_b = 0;
        field_48286_f = 0;
        field_48287_g = 0;
    }

    /**
     * Resets the task
     */
    public void resetTask()
    {
        taskOwner.setAttackTarget(null);
    }

    protected boolean func_48284_a(EntityLiving par1EntityLiving, boolean par2)
    {
        if (par1EntityLiving == null)
        {
            return false;
        }

        if (par1EntityLiving == taskOwner)
        {
            return false;
        }

        if (!par1EntityLiving.isEntityAlive())
        {
            return false;
        }

        if (par1EntityLiving.boundingBox.maxY <= taskOwner.boundingBox.minY || par1EntityLiving.boundingBox.minY >= taskOwner.boundingBox.maxY)
        {
            return false;
        }

        if (!taskOwner.func_48336_a(par1EntityLiving.getClass()))
        {
            return false;
        }

        if ((taskOwner instanceof EntityTameable) && ((EntityTameable)taskOwner).isTamed())
        {
            if ((par1EntityLiving instanceof EntityTameable) && ((EntityTameable)par1EntityLiving).isTamed())
            {
                return false;
            }

            if (par1EntityLiving == ((EntityTameable)taskOwner).getOwner())
            {
                return false;
            }
        }
        else if ((par1EntityLiving instanceof EntityPlayer) && !par2 && ((EntityPlayer)par1EntityLiving).capabilities.disableDamage)
        {
            return false;
        }

        if (!taskOwner.isWithinHomeDistance(MathHelper.floor_double(par1EntityLiving.posX), MathHelper.floor_double(par1EntityLiving.posY), MathHelper.floor_double(par1EntityLiving.posZ)))
        {
            return false;
        }

        if (field_48289_e && !taskOwner.func_48318_al().canSee(par1EntityLiving))
        {
            return false;
        }

        if (field_48292_a)
        {
            if (--field_48286_f <= 0)
            {
                field_48290_b = 0;
            }

            if (field_48290_b == 0)
            {
                field_48290_b = func_48285_a(par1EntityLiving) ? 1 : 2;
            }

            if (field_48290_b == 2)
            {
                return false;
            }
        }

        return true;
    }

    private boolean func_48285_a(EntityLiving par1EntityLiving)
    {
        field_48286_f = 10 + taskOwner.getRNG().nextInt(5);
        PathEntity pathentity = taskOwner.getNavigator().func_48661_a(par1EntityLiving);

        if (pathentity == null)
        {
            return false;
        }

        PathPoint pathpoint = pathentity.getFinalPathPoint();

        if (pathpoint == null)
        {
            return false;
        }
        else
        {
            int i = pathpoint.xCoord - MathHelper.floor_double(par1EntityLiving.posX);
            int j = pathpoint.zCoord - MathHelper.floor_double(par1EntityLiving.posZ);
            return (double)(i * i + j * j) <= 2.25D;
        }
    }
}
