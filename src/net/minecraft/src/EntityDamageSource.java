package net.minecraft.src;

public class EntityDamageSource extends DamageSource
{
    protected Entity damageSourceEntity;

    public EntityDamageSource(String par1Str, Entity par2Entity)
    {
        super(par1Str);
        damageSourceEntity = par2Entity;
    }

    public Entity getEntity()
    {
        return damageSourceEntity;
    }

    /**
     * Returns the message to be displayed on player death.
     */
    public String getDeathMessage(EntityPlayer par1EntityPlayer)
    {
        return StatCollector.translateToLocalFormatted((new StringBuilder()).append("death.").append(damageType).toString(), new Object[]
                {
                    par1EntityPlayer.username, damageSourceEntity.getUsername()
                });
    }
}
