package net.minecraft.src;

import java.util.Random;

public class EntityAIWatchClosest extends EntityAIBase
{
    private EntityLiving field_46110_a;

    /** The closest entity which is being watched by this one. */
    private Entity closestEntity;
    private float field_46106_d;
    private int field_46107_e;
    private float field_48241_e;
    private Class field_48240_f;

    public EntityAIWatchClosest(EntityLiving par1EntityLiving, Class par2Class, float par3)
    {
        field_46110_a = par1EntityLiving;
        field_48240_f = par2Class;
        field_46106_d = par3;
        field_48241_e = 0.02F;
        setMutexBits(2);
    }

    public EntityAIWatchClosest(EntityLiving par1EntityLiving, Class par2Class, float par3, float par4)
    {
        field_46110_a = par1EntityLiving;
        field_48240_f = par2Class;
        field_46106_d = par3;
        field_48241_e = par4;
        setMutexBits(2);
    }

    /**
     * Returns whether the EntityAIBase should begin execution.
     */
    public boolean shouldExecute()
    {
        if (field_46110_a.getRNG().nextFloat() >= field_48241_e)
        {
            return false;
        }

        if (field_48240_f == (net.minecraft.src.EntityPlayer.class))
        {
            closestEntity = field_46110_a.worldObj.getClosestPlayerToEntity(field_46110_a, field_46106_d);
        }
        else
        {
            closestEntity = field_46110_a.worldObj.findNearestEntityWithinAABB(field_48240_f, field_46110_a.boundingBox.expand(field_46106_d, 3D, field_46106_d), field_46110_a);
        }

        return closestEntity != null;
    }

    /**
     * Returns whether an in-progress EntityAIBase should continue executing
     */
    public boolean continueExecuting()
    {
        if (!closestEntity.isEntityAlive())
        {
            return false;
        }

        if (field_46110_a.getDistanceSqToEntity(closestEntity) > (double)(field_46106_d * field_46106_d))
        {
            return false;
        }
        else
        {
            return field_46107_e > 0;
        }
    }

    /**
     * Execute a one shot task or start executing a continuous task
     */
    public void startExecuting()
    {
        field_46107_e = 40 + field_46110_a.getRNG().nextInt(40);
    }

    /**
     * Resets the task
     */
    public void resetTask()
    {
        closestEntity = null;
    }

    /**
     * Updates the task
     */
    public void updateTask()
    {
        field_46110_a.getLookHelper().setLookPosition(closestEntity.posX, closestEntity.posY + (double)closestEntity.getEyeHeight(), closestEntity.posZ, 10F, field_46110_a.getVerticalFaceSpeed());
        field_46107_e--;
    }
}
