package net.minecraft.src;

public class EntityAIOwnerHurtTarget extends EntityAITarget
{
    EntityTameable field_48304_a;
    EntityLiving field_48303_b;

    public EntityAIOwnerHurtTarget(EntityTameable par1EntityTameable)
    {
        super(par1EntityTameable, 32F, false);
        field_48304_a = par1EntityTameable;
        setMutexBits(1);
    }

    /**
     * Returns whether the EntityAIBase should begin execution.
     */
    public boolean shouldExecute()
    {
        if (!field_48304_a.isTamed())
        {
            return false;
        }

        EntityLiving entityliving = field_48304_a.getOwner();

        if (entityliving == null)
        {
            return false;
        }
        else
        {
            field_48303_b = entityliving.getLastAttackingEntity();
            return func_48284_a(field_48303_b, false);
        }
    }

    /**
     * Execute a one shot task or start executing a continuous task
     */
    public void startExecuting()
    {
        taskOwner.setAttackTarget(field_48303_b);
        super.startExecuting();
    }
}
