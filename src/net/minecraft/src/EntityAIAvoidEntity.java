package net.minecraft.src;

import java.util.List;

public class EntityAIAvoidEntity extends EntityAIBase
{
    /** The entity we are attached to */
    private EntityCreature theEntity;
    private float field_48235_b;
    private float field_48236_c;
    private Entity field_48233_d;
    private float field_48234_e;
    private PathEntity field_48231_f;

    /** The PathNavigate of our entity */
    private PathNavigate entityPathNavigate;

    /** The class of the entity we should avoid */
    private Class targetEntityClass;

    public EntityAIAvoidEntity(EntityCreature par1EntityCreature, Class par2Class, float par3, float par4, float par5)
    {
        theEntity = par1EntityCreature;
        targetEntityClass = par2Class;
        field_48234_e = par3;
        field_48235_b = par4;
        field_48236_c = par5;
        entityPathNavigate = par1EntityCreature.getNavigator();
        setMutexBits(1);
    }

    /**
     * Returns whether the EntityAIBase should begin execution.
     */
    public boolean shouldExecute()
    {
        if (targetEntityClass == (net.minecraft.src.EntityPlayer.class))
        {
            if ((theEntity instanceof EntityTameable) && ((EntityTameable)theEntity).isTamed())
            {
                return false;
            }

            field_48233_d = theEntity.worldObj.getClosestPlayerToEntity(theEntity, field_48234_e);

            if (field_48233_d == null)
            {
                return false;
            }
        }
        else
        {
            List list = theEntity.worldObj.getEntitiesWithinAABB(targetEntityClass, theEntity.boundingBox.expand(field_48234_e, 3D, field_48234_e));

            if (list.size() == 0)
            {
                return false;
            }

            field_48233_d = (Entity)list.get(0);
        }

        if (!theEntity.func_48318_al().canSee(field_48233_d))
        {
            return false;
        }

        Vec3D vec3d = RandomPositionGenerator.func_48394_b(theEntity, 16, 7, Vec3D.createVector(field_48233_d.posX, field_48233_d.posY, field_48233_d.posZ));

        if (vec3d == null)
        {
            return false;
        }

        if (field_48233_d.getDistanceSq(vec3d.xCoord, vec3d.yCoord, vec3d.zCoord) < field_48233_d.getDistanceSqToEntity(theEntity))
        {
            return false;
        }

        field_48231_f = entityPathNavigate.func_48650_a(vec3d.xCoord, vec3d.yCoord, vec3d.zCoord);

        if (field_48231_f == null)
        {
            return false;
        }

        return field_48231_f.func_48426_a(vec3d);
    }

    /**
     * Returns whether an in-progress EntityAIBase should continue executing
     */
    public boolean continueExecuting()
    {
        return !entityPathNavigate.noPath();
    }

    /**
     * Execute a one shot task or start executing a continuous task
     */
    public void startExecuting()
    {
        entityPathNavigate.setPath(field_48231_f, field_48235_b);
    }

    /**
     * Resets the task
     */
    public void resetTask()
    {
        field_48233_d = null;
    }

    /**
     * Updates the task
     */
    public void updateTask()
    {
        if (theEntity.getDistanceSqToEntity(field_48233_d) < 49D)
        {
            theEntity.getNavigator().func_48654_a(field_48236_c);
        }
        else
        {
            theEntity.getNavigator().func_48654_a(field_48235_b);
        }
    }
}
