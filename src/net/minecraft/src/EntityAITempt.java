package net.minecraft.src;

public class EntityAITempt extends EntityAIBase
{
    /** The entity using this AI that is tempted by the player. */
    private EntityCreature temptedEntity;
    private float field_48266_b;
    private double field_48267_c;
    private double field_48264_d;
    private double field_48265_e;
    private double field_48262_f;
    private double field_48263_g;

    /** The player that is tempting the entity that is using this AI. */
    private EntityPlayer temptingPlayer;

    /**
     * A counter that is decremented each time the shouldExecute method is called. The shouldExecute method will always
     * return false if delayTemptCounter is greater than 0.
     */
    private int delayTemptCounter;
    private boolean field_48271_j;

    /**
     * This field saves the ID of the items that can be used to breed entities with this behaviour.
     */
    private int breedingFood;

    /**
     * Whether the entity using this AI will be scared by the tempter's sudden movement.
     */
    private boolean scaredByPlayerMovement;
    private boolean field_48270_m;

    public EntityAITempt(EntityCreature par1EntityCreature, float par2, int par3, boolean par4)
    {
        delayTemptCounter = 0;
        temptedEntity = par1EntityCreature;
        field_48266_b = par2;
        breedingFood = par3;
        scaredByPlayerMovement = par4;
        setMutexBits(3);
    }

    /**
     * Returns whether the EntityAIBase should begin execution.
     */
    public boolean shouldExecute()
    {
        if (delayTemptCounter > 0)
        {
            delayTemptCounter--;
            return false;
        }

        temptingPlayer = temptedEntity.worldObj.getClosestPlayerToEntity(temptedEntity, 10D);

        if (temptingPlayer == null)
        {
            return false;
        }

        ItemStack itemstack = temptingPlayer.getCurrentEquippedItem();

        if (itemstack == null)
        {
            return false;
        }

        return itemstack.itemID == breedingFood;
    }

    /**
     * Returns whether an in-progress EntityAIBase should continue executing
     */
    public boolean continueExecuting()
    {
        if (scaredByPlayerMovement)
        {
            if (temptedEntity.getDistanceSqToEntity(temptingPlayer) < 36D)
            {
                if (temptingPlayer.getDistanceSq(field_48267_c, field_48264_d, field_48265_e) > 0.010000000000000002D)
                {
                    return false;
                }

                if (Math.abs((double)temptingPlayer.rotationPitch - field_48262_f) > 5D || Math.abs((double)temptingPlayer.rotationYaw - field_48263_g) > 5D)
                {
                    return false;
                }
            }
            else
            {
                field_48267_c = temptingPlayer.posX;
                field_48264_d = temptingPlayer.posY;
                field_48265_e = temptingPlayer.posZ;
            }

            field_48262_f = temptingPlayer.rotationPitch;
            field_48263_g = temptingPlayer.rotationYaw;
        }

        return shouldExecute();
    }

    /**
     * Execute a one shot task or start executing a continuous task
     */
    public void startExecuting()
    {
        field_48267_c = temptingPlayer.posX;
        field_48264_d = temptingPlayer.posY;
        field_48265_e = temptingPlayer.posZ;
        field_48271_j = true;
        field_48270_m = temptedEntity.getNavigator().func_48649_a();
        temptedEntity.getNavigator().func_48656_a(false);
    }

    /**
     * Resets the task
     */
    public void resetTask()
    {
        temptingPlayer = null;
        temptedEntity.getNavigator().clearPathEntity();
        delayTemptCounter = 100;
        field_48271_j = false;
        temptedEntity.getNavigator().func_48656_a(field_48270_m);
    }

    /**
     * Updates the task
     */
    public void updateTask()
    {
        temptedEntity.getLookHelper().setLookPositionWithEntity(temptingPlayer, 30F, temptedEntity.getVerticalFaceSpeed());

        if (temptedEntity.getDistanceSqToEntity(temptingPlayer) < 6.25D)
        {
            temptedEntity.getNavigator().clearPathEntity();
        }
        else
        {
            temptedEntity.getNavigator().func_48652_a(temptingPlayer, field_48266_b);
        }
    }

    public boolean func_48261_f()
    {
        return field_48271_j;
    }
}
