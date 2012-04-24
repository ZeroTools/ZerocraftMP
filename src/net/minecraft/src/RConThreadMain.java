package net.minecraft.src;

import java.io.IOException;
import java.net.*;
import java.util.*;

public class RConThreadMain extends RConThreadBase
{
    /** Port RCon is running on */
    private int rconPort;

    /** Port the server is running on */
    private int serverPort;

    /** Hostname RCon is running on */
    private String hostname;

    /** The RCon ServerSocke */
    private ServerSocket serverSocket;

    /** The RCon password */
    private String rconPassword;

    /** A map of client addresses to their running Threads */
    private HashMap clientThreads;

    public RConThreadMain(IServer par1IServer)
    {
        super(par1IServer);
        serverSocket = null;
        rconPort = par1IServer.getIntProperty("rcon.port", 0);
        rconPassword = par1IServer.getStringProperty("rcon.password", "");
        hostname = par1IServer.getHostname();
        serverPort = par1IServer.getPort();

        if (0 == rconPort)
        {
            rconPort = serverPort + 10;
            log((new StringBuilder()).append("Setting default rcon port to ").append(rconPort).toString());
            par1IServer.setProperty("rcon.port", Integer.valueOf(rconPort));

            if (0 == rconPassword.length())
            {
                par1IServer.setProperty("rcon.password", "");
            }

            par1IServer.saveProperties();
        }

        if (0 == hostname.length())
        {
            hostname = "0.0.0.0";
        }

        initClientTh();
        serverSocket = null;
    }

    private void initClientTh()
    {
        clientThreads = new HashMap();
    }

    /**
     * Cleans up the clientThreads map by removing client Threads that are not running
     */
    private void cleanClientThreadsMap()
    {
        Iterator iterator = clientThreads.entrySet().iterator();

        do
        {
            if (!iterator.hasNext())
            {
                break;
            }

            java.util.Map.Entry entry = (java.util.Map.Entry)iterator.next();

            if (!((RConThreadClient)entry.getValue()).isRunning())
            {
                iterator.remove();
            }
        }
        while (true);
    }

    public void run()
    {
        log((new StringBuilder()).append("RCON running on ").append(hostname).append(":").append(rconPort).toString());

        try
        {
            do
            {
                if (!running)
                {
                    break;
                }

                try
                {
                    Socket socket = serverSocket.accept();
                    socket.setSoTimeout(500);
                    RConThreadClient rconthreadclient = new RConThreadClient(server, socket);
                    rconthreadclient.startThread();
                    clientThreads.put(socket.getRemoteSocketAddress(), rconthreadclient);
                    cleanClientThreadsMap();
                }
                catch (SocketTimeoutException sockettimeoutexception)
                {
                    cleanClientThreadsMap();
                }
                catch (IOException ioexception)
                {
                    if (running)
                    {
                        log((new StringBuilder()).append("IO: ").append(ioexception.getMessage()).toString());
                    }
                }
            }
            while (true);
        }
        finally
        {
            closeServerSocket(serverSocket);
        }
    }

    /**
     * Creates a new Thread object from this class and starts running
     */
    public void startThread()
    {
        if (0 == rconPassword.length())
        {
            logWarning((new StringBuilder()).append("No rcon password set in '").append(server.getSettingsFilename()).append("', rcon disabled!").toString());
            return;
        }

        if (0 >= rconPort || 65535 < rconPort)
        {
            logWarning((new StringBuilder()).append("Invalid rcon port ").append(rconPort).append(" found in '").append(server.getSettingsFilename()).append("', rcon disabled!").toString());
            return;
        }

        if (running)
        {
            return;
        }

        try
        {
            serverSocket = new ServerSocket(rconPort, 0, InetAddress.getByName(hostname));
            serverSocket.setSoTimeout(500);
            super.startThread();
        }
        catch (IOException ioexception)
        {
            logWarning((new StringBuilder()).append("Unable to initialise rcon on ").append(hostname).append(":").append(rconPort).append(" : ").append(ioexception.getMessage()).toString());
        }
    }
}
