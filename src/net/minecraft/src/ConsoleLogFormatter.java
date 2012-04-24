package net.minecraft.src;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.logging.*;

final class ConsoleLogFormatter extends Formatter
{
    /** The date format to use in the console log. */
    private SimpleDateFormat dateFormat;

    ConsoleLogFormatter()
    {
        dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    }

    public String format(LogRecord par1LogRecord)
    {
        StringBuilder stringbuilder = new StringBuilder();
        stringbuilder.append(dateFormat.format(Long.valueOf(par1LogRecord.getMillis())));
        Level level = par1LogRecord.getLevel();

        if (level == Level.FINEST)
        {
            stringbuilder.append(" [FINEST] ");
        }
        else if (level == Level.FINER)
        {
            stringbuilder.append(" [FINER] ");
        }
        else if (level == Level.FINE)
        {
            stringbuilder.append(" [FINE] ");
        }
        else if (level == Level.INFO)
        {
            stringbuilder.append(" [INFO] ");
        }
        else if (level == Level.WARNING)
        {
            stringbuilder.append(" [WARNING] ");
        }
        else if (level == Level.SEVERE)
        {
            stringbuilder.append(" [SEVERE] ");
        }
        else if (level == Level.SEVERE)
        {
            stringbuilder.append((new StringBuilder()).append(" [").append(level.getLocalizedName()).append("] ").toString());
        }

        stringbuilder.append(par1LogRecord.getMessage());
        stringbuilder.append('\n');
        Throwable throwable = par1LogRecord.getThrown();

        if (throwable != null)
        {
            StringWriter stringwriter = new StringWriter();
            throwable.printStackTrace(new PrintWriter(stringwriter));
            stringbuilder.append(stringwriter.toString());
        }

        return stringbuilder.toString();
    }
}
