package net.minecraft.src;

import java.util.logging.*;
import javax.swing.JTextArea;
import javax.swing.text.Document;

public class GuiLogOutputHandler extends Handler
{
    private int field_998_b[];
    private int field_1001_c;
    Formatter field_999_a;
    private JTextArea field_1000_d;

    public GuiLogOutputHandler(JTextArea par1JTextArea)
    {
        field_998_b = new int[1024];
        field_1001_c = 0;
        field_999_a = new GuiLogFormatter(this);
        setFormatter(field_999_a);
        field_1000_d = par1JTextArea;
    }

    public void close()
    {
    }

    public void flush()
    {
    }

    public void publish(LogRecord par1LogRecord)
    {
        int i = field_1000_d.getDocument().getLength();
        field_1000_d.append(field_999_a.format(par1LogRecord));
        field_1000_d.setCaretPosition(field_1000_d.getDocument().getLength());
        int j = field_1000_d.getDocument().getLength() - i;

        if (field_998_b[field_1001_c] != 0)
        {
            field_1000_d.replaceRange("", 0, field_998_b[field_1001_c]);
        }

        field_998_b[field_1001_c] = j;
        field_1001_c = (field_1001_c + 1) % 1024;
    }
}
