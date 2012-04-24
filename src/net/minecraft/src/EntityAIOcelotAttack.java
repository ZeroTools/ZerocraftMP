package net.minecraft.src;

public class EntityAIOcelotAttack extends EntityAIBase
{
    World theWorld;
    EntityLiving theEntity;
    EntityLiving field_48170_c;
    int field_48168_d;

    public EntityAIOcelotAttack(EntityLiving par1EntityLiving)
    {
        field_48168_d = 0;
        theEntity = par1EntityLiving;
        theWorld = par1EntityLiving.worldObj;
        setMutexBits(3);
    }

    /**
     * Returns whether the EntityAIBase should begin execution.
     */
    public boolean shouldExecute()
    {
        EntityLiving entityliving = theEntity.getAttackTarget();

        if (entityliving == null)
        {
            return false;
        }
        else
        {
            field_48170_c = entityliving;
            return true;
        }
    }

    /**
     * Returns whether an in-progress EntityAIBase should continue executing
     */
    public boolean continueExecuting()
    {
        if (!field_48170_c.isEntityAlive())
        {
            return false;
        }

        if (theEntity.getDistanceSqToEntity(field_48170_c) > 225D)
        {
            return false;
        }
        else
        {
            return !theEntity.getNavigator().noPath() || shouldExecute();
        }
    }

    /**
     * Resets the task
     */
    public void resetTask()
    {
        field_48170_c = null;
        theEntity.getNavigator().clearPathEntity();
    }

    /**
     * Updates the task
     */
    public void updateTask()
    {
        theEntity.getLookHelper().setLookPositionWithEntity(field_48170_c, 30F, 30F);
        double d = theEntity.width * 2.0F * (theEntity.width * 2.0F);
        double d1 = theEntity.getDistanceSq(field_48170_c.posX, field_48170_c.boundingBox.minY, field_48170_c.posZ);
        float f = 0.23F;

        if (d1 > d && d1 < 16D)
        {
            f = 0.4F;
        }
        else if (d1 < 225D)
        {
            f = 0.18F;
        }

        theEntity.getNavigator().func_48652_a(field_48170_c, f);
        field_48168_d = Math.max(field_48168_d - 1, 0);

        if (d1 > d)
        {
            return;
        }

        if (field_48168_d > 0)
        {
            return;
        }
        else
        {
            field_48168_d = 20;
            theEntity.attackEntityAsMob(field_48170_c);
            return;
        }
    }
}
