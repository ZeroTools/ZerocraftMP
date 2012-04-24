package net.minecraft.src;

public class EntityAISit extends EntityAIBase
{
    private EntityTameable theEntity;
    private boolean field_48211_b;

    public EntityAISit(EntityTameable par1EntityTameable)
    {
        field_48211_b = false;
        theEntity = par1EntityTameable;
        setMutexBits(5);
    }

    /**
     * Returns whether the EntityAIBase should begin execution.
     */
    public boolean shouldExecute()
    {
        if (!theEntity.isTamed())
        {
            return false;
        }

        if (theEntity.isInWater())
        {
            return false;
        }

        if (!theEntity.onGround)
        {
            return false;
        }

        EntityLiving entityliving = theEntity.getOwner();

        if (entityliving == null)
        {
            return true;
        }

        if (theEntity.getDistanceSqToEntity(entityliving) < 144D && entityliving.getAITarget() != null)
        {
            return false;
        }
        else
        {
            return field_48211_b;
        }
    }

    /**
     * Execute a one shot task or start executing a continuous task
     */
    public void startExecuting()
    {
        theEntity.getNavigator().clearPathEntity();
        theEntity.func_48369_c(true);
    }

    /**
     * Resets the task
     */
    public void resetTask()
    {
        theEntity.func_48369_c(false);
    }

    public void func_48210_a(boolean par1)
    {
        field_48211_b = par1;
    }
}
