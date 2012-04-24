package net.minecraft.src;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.util.logging.Logger;
import javax.swing.*;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;
import net.minecraft.server.MinecraftServer;

public class ServerGUI extends JComponent implements ICommandListener
{
    /** Reference to the logger. */
    public static Logger logger = Logger.getLogger("Minecraft");

    /** Reference to the MinecraftServer object. */
    private MinecraftServer mcServer;

    /**
     * Initialises the GUI components.
     */
    public static void initGui(MinecraftServer par0MinecraftServer)
    {
        try
        {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        }
        catch (Exception exception) { }

        ServerGUI servergui = new ServerGUI(par0MinecraftServer);
        JFrame jframe = new JFrame("Minecraft server");
        jframe.add(servergui);
        jframe.pack();
        jframe.setLocationRelativeTo(null);
        jframe.setVisible(true);
        jframe.addWindowListener(new ServerWindowAdapter(par0MinecraftServer));
    }

    public ServerGUI(MinecraftServer par1MinecraftServer)
    {
        mcServer = par1MinecraftServer;
        setPreferredSize(new Dimension(854, 480));
        setLayout(new BorderLayout());

        try
        {
            add(getLogComponent(), "Center");
            add(getStatsComponent(), "West");
        }
        catch (Exception exception)
        {
            exception.printStackTrace();
        }
    }

    /**
     * Returns a new JPanel with a new GuiStatsComponent inside.
     */
    private JComponent getStatsComponent()
    {
        JPanel jpanel = new JPanel(new BorderLayout());
        jpanel.add(new GuiStatsComponent(mcServer), "North");
        jpanel.add(getPlayerListComponent(), "Center");
        jpanel.setBorder(new TitledBorder(new EtchedBorder(), "Stats"));
        return jpanel;
    }

    /**
     * Returns a new JScrollPane with a new PlayerListBox inside.
     */
    private JComponent getPlayerListComponent()
    {
        PlayerListBox playerlistbox = new PlayerListBox(mcServer);
        JScrollPane jscrollpane = new JScrollPane(playerlistbox, 22, 30);
        jscrollpane.setBorder(new TitledBorder(new EtchedBorder(), "Players"));
        return jscrollpane;
    }

    /**
     * Returns a new JPanel with a new GuiStatsComponent inside.
     */
    private JComponent getLogComponent()
    {
        JPanel jpanel = new JPanel(new BorderLayout());
        JTextArea jtextarea = new JTextArea();
        logger.addHandler(new GuiLogOutputHandler(jtextarea));
        JScrollPane jscrollpane = new JScrollPane(jtextarea, 22, 30);
        jtextarea.setEditable(false);
        JTextField jtextfield = new JTextField();
        jtextfield.addActionListener(new ServerGuiCommandListener(this, jtextfield));
        jtextarea.addFocusListener(new ServerGuiFocusAdapter(this));
        jpanel.add(jscrollpane, "Center");
        jpanel.add(jtextfield, "South");
        jpanel.setBorder(new TitledBorder(new EtchedBorder(), "Log and chat"));
        return jpanel;
    }

    /**
     * Logs the message with a level of INFO.
     */
    public void log(String par1Str)
    {
        logger.info(par1Str);
    }

    /**
     * Gets the players username.
     */
    public String getUsername()
    {
        return "CONSOLE";
    }

    /**
     * Returns the MinecraftServer associated with the ServerGui.
     */
    static MinecraftServer getMinecraftServer(ServerGUI par0ServerGUI)
    {
        return par0ServerGUI.mcServer;
    }
}
