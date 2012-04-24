package net.minecraft.src;

import java.util.List;
import java.util.Random;

public abstract class EntityThrowable extends Entity
{
    private int xTile;
    private int yTile;
    private int zTile;
    private int inTile;
    protected boolean inGround;
    public int throwableShake;

    /** The entity that threw this throwable item. */
    protected EntityLiving thrower;
    private int ticksInGround;
    private int ticksInAir;

    public EntityThrowable(World par1World)
    {
        super(par1World);
        xTile = -1;
        yTile = -1;
        zTile = -1;
        inTile = 0;
        inGround = false;
        throwableShake = 0;
        ticksInAir = 0;
        setSize(0.25F, 0.25F);
    }

    protected void entityInit()
    {
    }

    public EntityThrowable(World par1World, EntityLiving par2EntityLiving)
    {
        super(par1World);
        xTile = -1;
        yTile = -1;
        zTile = -1;
        inTile = 0;
        inGround = false;
        throwableShake = 0;
        ticksInAir = 0;
        thrower = par2EntityLiving;
        setSize(0.25F, 0.25F);
        setLocationAndAngles(par2EntityLiving.posX, par2EntityLiving.posY + (double)par2EntityLiving.getEyeHeight(), par2EntityLiving.posZ, par2EntityLiving.rotationYaw, par2EntityLiving.rotationPitch);
        posX -= MathHelper.cos((rotationYaw / 180F) * (float)Math.PI) * 0.16F;
        posY -= 0.10000000149011612D;
        posZ -= MathHelper.sin((rotationYaw / 180F) * (float)Math.PI) * 0.16F;
        setPosition(posX, posY, posZ);
        yOffset = 0.0F;
        float f = 0.4F;
        motionX = -MathHelper.sin((rotationYaw / 180F) * (float)Math.PI) * MathHelper.cos((rotationPitch / 180F) * (float)Math.PI) * f;
        motionZ = MathHelper.cos((rotationYaw / 180F) * (float)Math.PI) * MathHelper.cos((rotationPitch / 180F) * (float)Math.PI) * f;
        motionY = -MathHelper.sin(((rotationPitch + func_40040_d()) / 180F) * (float)Math.PI) * f;
        setThrowableHeading(motionX, motionY, motionZ, func_40044_c(), 1.0F);
    }

    public EntityThrowable(World par1World, double par2, double par4, double par6)
    {
        super(par1World);
        xTile = -1;
        yTile = -1;
        zTile = -1;
        inTile = 0;
        inGround = false;
        throwableShake = 0;
        ticksInAir = 0;
        ticksInGround = 0;
        setSize(0.25F, 0.25F);
        setPosition(par2, par4, par6);
        yOffset = 0.0F;
    }

    protected float func_40044_c()
    {
        return 1.5F;
    }

    protected float func_40040_d()
    {
        return 0.0F;
    }

    /**
     * Similar to setArrowHeading, it's point the throwable entity to a x, y, z direction.
     */
    public void setThrowableHeading(double par1, double par3, double par5, float par7, float par8)
    {
        float f = MathHelper.sqrt_double(par1 * par1 + par3 * par3 + par5 * par5);
        par1 /= f;
        par3 /= f;
        par5 /= f;
        par1 += rand.nextGaussian() * 0.0074999998323619366D * (double)par8;
        par3 += rand.nextGaussian() * 0.0074999998323619366D * (double)par8;
        par5 += rand.nextGaussian() * 0.0074999998323619366D * (double)par8;
        par1 *= par7;
        par3 *= par7;
        par5 *= par7;
        motionX = par1;
        motionY = par3;
        motionZ = par5;
        float f1 = MathHelper.sqrt_double(par1 * par1 + par5 * par5);
        prevRotationYaw = rotationYaw = (float)((Math.atan2(par1, par5) * 180D) / Math.PI);
        prevRotationPitch = rotationPitch = (float)((Math.atan2(par3, f1) * 180D) / Math.PI);
        ticksInGround = 0;
    }

    /**
     * Called to update the entity's position/logic.
     */
    public void onUpdate()
    {
        lastTickPosX = posX;
        lastTickPosY = posY;
        lastTickPosZ = posZ;
        super.onUpdate();

        if (throwableShake > 0)
        {
            throwableShake--;
        }

        if (inGround)
        {
            int i = worldObj.getBlockId(xTile, yTile, zTile);

            if (i != inTile)
            {
                inGround = false;
                motionX *= rand.nextFloat() * 0.2F;
                motionY *= rand.nextFloat() * 0.2F;
                motionZ *= rand.nextFloat() * 0.2F;
                ticksInGround = 0;
                ticksInAir = 0;
            }
            else
            {
                ticksInGround++;

                if (ticksInGround == 1200)
                {
                    setDead();
                }

                return;
            }
        }
        else
        {
            ticksInAir++;
        }

        Vec3D vec3d = Vec3D.createVector(posX, posY, posZ);
        Vec3D vec3d1 = Vec3D.createVector(posX + motionX, posY + motionY, posZ + motionZ);
        MovingObjectPosition movingobjectposition = worldObj.rayTraceBlocks(vec3d, vec3d1);
        vec3d = Vec3D.createVector(posX, posY, posZ);
        vec3d1 = Vec3D.createVector(posX + motionX, posY + motionY, posZ + motionZ);

        if (movingobjectposition != null)
        {
            vec3d1 = Vec3D.createVector(movingobjectposition.hitVec.xCoord, movingobjectposition.hitVec.yCoord, movingobjectposition.hitVec.zCoord);
        }

        if (!worldObj.isRemote)
        {
            Entity entity = null;
            List list = worldObj.getEntitiesWithinAABBExcludingEntity(this, boundingBox.addCoord(motionX, motionY, motionZ).expand(1.0D, 1.0D, 1.0D));
            double d = 0.0D;

            for (int k = 0; k < list.size(); k++)
            {
                Entity entity1 = (Entity)list.get(k);

                if (!entity1.canBeCollidedWith() || entity1 == thrower && ticksInAir < 5)
                {
                    continue;
                }

                float f4 = 0.3F;
                AxisAlignedBB axisalignedbb = entity1.boundingBox.expand(f4, f4, f4);
                MovingObjectPosition movingobjectposition1 = axisalignedbb.calculateIntercept(vec3d, vec3d1);

                if (movingobjectposition1 == null)
                {
                    continue;
                }

                double d1 = vec3d.distanceTo(movingobjectposition1.hitVec);

                if (d1 < d || d == 0.0D)
                {
                    entity = entity1;
                    d = d1;
                }
            }

            if (entity != null)
            {
                movingobjectposition = new MovingObjectPosition(entity);
            }
        }

        if (movingobjectposition != null)
        {
            onImpact(movingobjectposition);
        }

        posX += motionX;
        posY += motionY;
        posZ += motionZ;
        float f = MathHelper.sqrt_double(motionX * motionX + motionZ * motionZ);
        rotationYaw = (float)((Math.atan2(motionX, motionZ) * 180D) / Math.PI);

        for (rotationPitch = (float)((Math.atan2(motionY, f) * 180D) / Math.PI); rotationPitch - prevRotationPitch < -180F; prevRotationPitch -= 360F) { }

        for (; rotationPitch - prevRotationPitch >= 180F; prevRotationPitch += 360F) { }

        for (; rotationYaw - prevRotationYaw < -180F; prevRotationYaw -= 360F) { }

        for (; rotationYaw - prevRotationYaw >= 180F; prevRotationYaw += 360F) { }

        rotationPitch = prevRotationPitch + (rotationPitch - prevRotationPitch) * 0.2F;
        rotationYaw = prevRotationYaw + (rotationYaw - prevRotationYaw) * 0.2F;
        float f1 = 0.99F;
        float f2 = func_40042_e();

        if (isInWater())
        {
            for (int j = 0; j < 4; j++)
            {
                float f3 = 0.25F;
                worldObj.spawnParticle("bubble", posX - motionX * (double)f3, posY - motionY * (double)f3, posZ - motionZ * (double)f3, motionX, motionY, motionZ);
            }

            f1 = 0.8F;
        }

        motionX *= f1;
        motionY *= f1;
        motionZ *= f1;
        motionY -= f2;
        setPosition(posX, posY, posZ);
    }

    protected float func_40042_e()
    {
        return 0.03F;
    }

    /**
     * Called when this EntityThrowable hits a block or entity.
     */
    protected abstract void onImpact(MovingObjectPosition movingobjectposition);

    /**
     * (abstract) Protected helper method to write subclass entity data to NBT.
     */
    public void writeEntityToNBT(NBTTagCompound par1NBTTagCompound)
    {
        par1NBTTagCompound.setShort("xTile", (short)xTile);
        par1NBTTagCompound.setShort("yTile", (short)yTile);
        par1NBTTagCompound.setShort("zTile", (short)zTile);
        par1NBTTagCompound.setByte("inTile", (byte)inTile);
        par1NBTTagCompound.setByte("shake", (byte)throwableShake);
        par1NBTTagCompound.setByte("inGround", (byte)(inGround ? 1 : 0));
    }

    /**
     * (abstract) Protected helper method to read subclass entity data from NBT.
     */
    public void readEntityFromNBT(NBTTagCompound par1NBTTagCompound)
    {
        xTile = par1NBTTagCompound.getShort("xTile");
        yTile = par1NBTTagCompound.getShort("yTile");
        zTile = par1NBTTagCompound.getShort("zTile");
        inTile = par1NBTTagCompound.getByte("inTile") & 0xff;
        throwableShake = par1NBTTagCompound.getByte("shake") & 0xff;
        inGround = par1NBTTagCompound.getByte("inGround") == 1;
    }

    /**
     * Called by a player entity when they collide with an entity
     */
    public void onCollideWithPlayer(EntityPlayer entityplayer)
    {
    }
}
