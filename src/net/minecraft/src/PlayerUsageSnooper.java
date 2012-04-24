package net.minecraft.src;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

public class PlayerUsageSnooper
{
    private Map field_52016_a;
    private final URL field_52015_b;

    public PlayerUsageSnooper(String par1Str)
    {
        field_52016_a = new HashMap();

        try
        {
            field_52015_b = new URL((new StringBuilder()).append("http://snoop.minecraft.net/").append(par1Str).toString());
        }
        catch (MalformedURLException malformedurlexception)
        {
            throw new IllegalArgumentException();
        }
    }

    public void func_52014_a(String par1Str, Object par2Obj)
    {
        field_52016_a.put(par1Str, par2Obj);
    }

    public void func_52012_a()
    {
        PlayerUsageSnooperThread playerusagesnooperthread = new PlayerUsageSnooperThread(this, "reporter");
        playerusagesnooperthread.setDaemon(true);
        playerusagesnooperthread.start();
    }

    static URL func_52013_a(PlayerUsageSnooper par0PlayerUsageSnooper)
    {
        return par0PlayerUsageSnooper.field_52015_b;
    }

    static Map func_52011_b(PlayerUsageSnooper par0PlayerUsageSnooper)
    {
        return par0PlayerUsageSnooper.field_52016_a;
    }
}
