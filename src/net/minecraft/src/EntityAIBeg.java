package net.minecraft.src;

import java.util.Random;

public class EntityAIBeg extends EntityAIBase
{
    private EntityWolf theWolf;
    private EntityPlayer field_48145_b;
    private World field_48146_c;
    private float field_48143_d;
    private int field_48144_e;

    public EntityAIBeg(EntityWolf par1EntityWolf, float par2)
    {
        theWolf = par1EntityWolf;
        field_48146_c = par1EntityWolf.worldObj;
        field_48143_d = par2;
        setMutexBits(2);
    }

    /**
     * Returns whether the EntityAIBase should begin execution.
     */
    public boolean shouldExecute()
    {
        field_48145_b = field_48146_c.getClosestPlayerToEntity(theWolf, field_48143_d);

        if (field_48145_b == null)
        {
            return false;
        }
        else
        {
            return func_48142_a(field_48145_b);
        }
    }

    /**
     * Returns whether an in-progress EntityAIBase should continue executing
     */
    public boolean continueExecuting()
    {
        if (!field_48145_b.isEntityAlive())
        {
            return false;
        }

        if (theWolf.getDistanceSqToEntity(field_48145_b) > (double)(field_48143_d * field_48143_d))
        {
            return false;
        }
        else
        {
            return field_48144_e > 0 && func_48142_a(field_48145_b);
        }
    }

    /**
     * Execute a one shot task or start executing a continuous task
     */
    public void startExecuting()
    {
        theWolf.func_48378_e(true);
        field_48144_e = 40 + theWolf.getRNG().nextInt(40);
    }

    /**
     * Resets the task
     */
    public void resetTask()
    {
        theWolf.func_48378_e(false);
        field_48145_b = null;
    }

    /**
     * Updates the task
     */
    public void updateTask()
    {
        theWolf.getLookHelper().setLookPosition(field_48145_b.posX, field_48145_b.posY + (double)field_48145_b.getEyeHeight(), field_48145_b.posZ, 10F, theWolf.getVerticalFaceSpeed());
        field_48144_e--;
    }

    private boolean func_48142_a(EntityPlayer par1EntityPlayer)
    {
        ItemStack itemstack = par1EntityPlayer.inventory.getCurrentItem();

        if (itemstack == null)
        {
            return false;
        }

        if (!theWolf.isTamed() && itemstack.itemID == Item.bone.shiftedIndex)
        {
            return true;
        }
        else
        {
            return theWolf.isWheat(itemstack);
        }
    }
}
