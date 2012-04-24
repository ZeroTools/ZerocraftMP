package net.minecraft.src;

public class EntityDamageSourceIndirect extends EntityDamageSource
{
    private Entity indirectEntity;

    public EntityDamageSourceIndirect(String par1Str, Entity par2Entity, Entity par3Entity)
    {
        super(par1Str, par2Entity);
        indirectEntity = par3Entity;
    }

    public Entity getSourceOfDamage()
    {
        return damageSourceEntity;
    }

    public Entity getEntity()
    {
        return indirectEntity;
    }

    /**
     * Returns the message to be displayed on player death.
     */
    public String getDeathMessage(EntityPlayer par1EntityPlayer)
    {
        return StatCollector.translateToLocalFormatted((new StringBuilder()).append("death.").append(damageType).toString(), new Object[]
                {
                    par1EntityPlayer.username, indirectEntity.getUsername()
                });
    }
}
