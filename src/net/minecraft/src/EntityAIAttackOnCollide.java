package net.minecraft.src;

import java.util.Random;

public class EntityAIAttackOnCollide extends EntityAIBase
{
    World worldObj;
    EntityLiving field_48156_b;
    EntityLiving entityTarget;
    int field_46095_d;
    float field_48155_e;
    boolean field_48153_f;
    PathEntity field_48154_g;
    Class field_48157_h;
    private int field_48158_i;

    public EntityAIAttackOnCollide(EntityLiving par1EntityLiving, Class par2Class, float par3, boolean par4)
    {
        this(par1EntityLiving, par3, par4);
        field_48157_h = par2Class;
    }

    public EntityAIAttackOnCollide(EntityLiving par1EntityLiving, float par2, boolean par3)
    {
        field_46095_d = 0;
        field_48156_b = par1EntityLiving;
        worldObj = par1EntityLiving.worldObj;
        field_48155_e = par2;
        field_48153_f = par3;
        setMutexBits(3);
    }

    /**
     * Returns whether the EntityAIBase should begin execution.
     */
    public boolean shouldExecute()
    {
        EntityLiving entityliving = field_48156_b.getAttackTarget();

        if (entityliving == null)
        {
            return false;
        }

        if (field_48157_h != null && !field_48157_h.isAssignableFrom(entityliving.getClass()))
        {
            return false;
        }
        else
        {
            entityTarget = entityliving;
            field_48154_g = field_48156_b.getNavigator().func_48661_a(entityTarget);
            return field_48154_g != null;
        }
    }

    /**
     * Returns whether an in-progress EntityAIBase should continue executing
     */
    public boolean continueExecuting()
    {
        EntityLiving entityliving = field_48156_b.getAttackTarget();

        if (entityliving == null)
        {
            return false;
        }

        if (!entityTarget.isEntityAlive())
        {
            return false;
        }

        if (!field_48153_f)
        {
            return !field_48156_b.getNavigator().noPath();
        }

        return field_48156_b.isWithinHomeDistance(MathHelper.floor_double(entityTarget.posX), MathHelper.floor_double(entityTarget.posY), MathHelper.floor_double(entityTarget.posZ));
    }

    /**
     * Execute a one shot task or start executing a continuous task
     */
    public void startExecuting()
    {
        field_48156_b.getNavigator().setPath(field_48154_g, field_48155_e);
        field_48158_i = 0;
    }

    /**
     * Resets the task
     */
    public void resetTask()
    {
        entityTarget = null;
        field_48156_b.getNavigator().clearPathEntity();
    }

    /**
     * Updates the task
     */
    public void updateTask()
    {
        field_48156_b.getLookHelper().setLookPositionWithEntity(entityTarget, 30F, 30F);

        if ((field_48153_f || field_48156_b.func_48318_al().canSee(entityTarget)) && --field_48158_i <= 0)
        {
            field_48158_i = 4 + field_48156_b.getRNG().nextInt(7);
            field_48156_b.getNavigator().func_48652_a(entityTarget, field_48155_e);
        }

        field_46095_d = Math.max(field_46095_d - 1, 0);
        double d = field_48156_b.width * 2.0F * (field_48156_b.width * 2.0F);

        if (field_48156_b.getDistanceSq(entityTarget.posX, entityTarget.boundingBox.minY, entityTarget.posZ) > d)
        {
            return;
        }

        if (field_46095_d > 0)
        {
            return;
        }
        else
        {
            field_46095_d = 20;
            field_48156_b.attackEntityAsMob(entityTarget);
            return;
        }
    }
}
