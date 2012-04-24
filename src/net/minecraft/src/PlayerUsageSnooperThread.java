package net.minecraft.src;

class PlayerUsageSnooperThread extends Thread
{
    final PlayerUsageSnooper field_52017_a;

    PlayerUsageSnooperThread(PlayerUsageSnooper par1PlayerUsageSnooper, String par2Str)
    {
        super(par2Str);
        field_52017_a = par1PlayerUsageSnooper;
    }

    public void run()
    {
        PostHttp.func_52010_a(PlayerUsageSnooper.func_52013_a(field_52017_a), PlayerUsageSnooper.func_52011_b(field_52017_a), true);
    }
}
