package net.minecraft.src;

import java.io.*;
import java.util.*;

public class StringTranslate
{
    /** Is the private singleton instance of StringTranslate. */
    private static StringTranslate instance = new StringTranslate();

    /**
     * Contains all key/value pairs to be translated - is loaded from '/lang/en_US.lang' when the StringTranslate is
     * created.
     */
    private Properties translateTable;
    private TreeMap languageList;
    private String currentLanguage;
    private boolean isUnicode;

    private StringTranslate()
    {
        translateTable = new Properties();
        loadLanguageList();
        setLanguage("en_US");
    }

    /**
     * Return the StringTranslate singleton instance
     */
    public static StringTranslate getInstance()
    {
        return instance;
    }

    private void loadLanguageList()
    {
        TreeMap treemap = new TreeMap();

        try
        {
            BufferedReader bufferedreader = new BufferedReader(new InputStreamReader((net.minecraft.src.StringTranslate.class).getResourceAsStream("/lang/languages.txt"), "UTF-8"));

            for (String s = bufferedreader.readLine(); s != null; s = bufferedreader.readLine())
            {
                String as[] = s.split("=");

                if (as != null && as.length == 2)
                {
                    treemap.put(as[0], as[1]);
                }
            }
        }
        catch (IOException ioexception)
        {
            ioexception.printStackTrace();
            return;
        }

        languageList = treemap;
    }

    private void loadLanguage(Properties par1Properties, String par2Str) throws IOException
    {
        BufferedReader bufferedreader = new BufferedReader(new InputStreamReader((net.minecraft.src.StringTranslate.class).getResourceAsStream((new StringBuilder()).append("/lang/").append(par2Str).append(".lang").toString()), "UTF-8"));

        for (String s = bufferedreader.readLine(); s != null; s = bufferedreader.readLine())
        {
            s = s.trim();

            if (s.startsWith("#"))
            {
                continue;
            }

            String as[] = s.split("=");

            if (as != null && as.length == 2)
            {
                par1Properties.setProperty(as[0], as[1]);
            }
        }
    }

    public void setLanguage(String par1Str)
    {
        if (!par1Str.equals(this.currentLanguage))
        {
            Properties var2 = new Properties();

            try
            {
                this.loadLanguage(var2, "en_US");
            }
            catch (IOException var8)
            {
                ;
            }

            this.isUnicode = false;

            if (!"en_US".equals(par1Str))
            {
                try
                {
                    this.loadLanguage(var2, par1Str);
                    Enumeration var3 = var2.propertyNames();

                    while (var3.hasMoreElements() && !this.isUnicode)
                    {
                        Object var4 = var3.nextElement();
                        Object var5 = var2.get(var4);

                        if (var5 != null)
                        {
                            String var6 = var5.toString();

                            for (int var7 = 0; var7 < var6.length(); ++var7)
                            {
                                if (var6.charAt(var7) >= 256)
                                {
                                    this.isUnicode = true;
                                    break;
                                }
                            }
                        }
                    }
                }
                catch (IOException var9)
                {
                    var9.printStackTrace();
                    return;
                }
            }

            this.currentLanguage = par1Str;
            this.translateTable = var2;
        }
    }

    /**
     * Translate a key to current language.
     */
    public String translateKey(String par1Str)
    {
        return translateTable.getProperty(par1Str, par1Str);
    }

    /**
     * Translate a key to current language applying String.format()
     */
    public String translateKeyFormat(String par1Str, Object par2ArrayOfObj[])
    {
        String s = translateTable.getProperty(par1Str, par1Str);
        return String.format(s, par2ArrayOfObj);
    }
}
