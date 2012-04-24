package net.minecraft.src;

public class EntityAIOwnerHurtByTarget extends EntityAITarget
{
    EntityTameable field_48294_a;
    EntityLiving field_48293_b;

    public EntityAIOwnerHurtByTarget(EntityTameable par1EntityTameable)
    {
        super(par1EntityTameable, 32F, false);
        field_48294_a = par1EntityTameable;
        setMutexBits(1);
    }

    /**
     * Returns whether the EntityAIBase should begin execution.
     */
    public boolean shouldExecute()
    {
        if (!field_48294_a.isTamed())
        {
            return false;
        }

        EntityLiving entityliving = field_48294_a.getOwner();

        if (entityliving == null)
        {
            return false;
        }
        else
        {
            field_48293_b = entityliving.getAITarget();
            return func_48284_a(field_48293_b, false);
        }
    }

    /**
     * Execute a one shot task or start executing a continuous task
     */
    public void startExecuting()
    {
        taskOwner.setAttackTarget(field_48293_b);
        super.startExecuting();
    }
}
