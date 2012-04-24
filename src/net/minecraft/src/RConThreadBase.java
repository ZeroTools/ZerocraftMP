package net.minecraft.src;

import java.io.IOException;
import java.net.DatagramSocket;
import java.net.ServerSocket;
import java.util.*;

public abstract class RConThreadBase implements Runnable
{
    /** True i */
    protected boolean running;

    /** Reference to the server object */
    protected IServer server;

    /** Thread for this runnable class */
    protected Thread rconThread;
    protected int field_40415_d;

    /** A list of registered DatagramSockets */
    protected List socketList;

    /** A list of registered ServerSockets */
    protected List serverSocketList;

    RConThreadBase(IServer par1IServer)
    {
        running = false;
        field_40415_d = 5;
        socketList = new ArrayList();
        serverSocketList = new ArrayList();
        server = par1IServer;

        if (server.isDebuggingEnabled())
        {
            logWarning("Debugging is enabled, performance maybe reduced!");
        }
    }

    /**
     * Creates a new Thread object from this class and starts running
     */
    public synchronized void startThread()
    {
        rconThread = new Thread(this);
        rconThread.start();
        running = true;
    }

    /**
     * Returns true if the Thread is running, false otherwise
     */
    public boolean isRunning()
    {
        return running;
    }

    /**
     * Log information message
     */
    protected void logInfo(String par1Str)
    {
        server.logIn(par1Str);
    }

    /**
     * Log message
     */
    protected void log(String par1Str)
    {
        server.log(par1Str);
    }

    /**
     * Log warning message
     */
    protected void logWarning(String par1Str)
    {
        server.logWarning(par1Str);
    }

    /**
     * Log severe error message
     */
    protected void logSevere(String par1Str)
    {
        server.logSevere(par1Str);
    }

    /**
     * Returns the number of players on the server
     */
    protected int getNumberOfPlayers()
    {
        return server.playersOnline();
    }

    /**
     * Registers a DatagramSocket with this thread
     */
    protected void registerSocket(DatagramSocket par1DatagramSocket)
    {
        logInfo((new StringBuilder()).append("registerSocket: ").append(par1DatagramSocket).toString());
        socketList.add(par1DatagramSocket);
    }

    /**
     * Closes the specified Da
     */
    protected boolean closeSocket(DatagramSocket par1DatagramSocket, boolean par2)
    {
        logInfo((new StringBuilder()).append("closeSocket: ").append(par1DatagramSocket).toString());

        if (null == par1DatagramSocket)
        {
            return false;
        }

        boolean flag = false;

        if (!par1DatagramSocket.isClosed())
        {
            par1DatagramSocket.close();
            flag = true;
        }

        if (par2)
        {
            socketList.remove(par1DatagramSocket);
        }

        return flag;
    }

    /**
     * Closes the specified ServerSocket
     */
    protected boolean closeServerSocket(ServerSocket par1ServerSocket)
    {
        return closeServerSocket_do(par1ServerSocket, true);
    }

    /**
     * Closes the specified ServerSocket
     */
    protected boolean closeServerSocket_do(ServerSocket par1ServerSocket, boolean par2)
    {
        logInfo((new StringBuilder()).append("closeSocket: ").append(par1ServerSocket).toString());

        if (null == par1ServerSocket)
        {
            return false;
        }

        boolean flag = false;

        try
        {
            if (!par1ServerSocket.isClosed())
            {
                par1ServerSocket.close();
                flag = true;
            }
        }
        catch (IOException ioexception)
        {
            logWarning((new StringBuilder()).append("IO: ").append(ioexception.getMessage()).toString());
        }

        if (par2)
        {
            serverSocketList.remove(par1ServerSocket);
        }

        return flag;
    }

    /**
     * Closes all of the opened sockets
     */
    protected void closeAllSockets()
    {
        clos(false);
    }

    protected void clos(boolean par1)
    {
        int i = 0;
        Iterator iterator = socketList.iterator();

        do
        {
            if (!iterator.hasNext())
            {
                break;
            }

            DatagramSocket datagramsocket = (DatagramSocket)iterator.next();

            if (closeSocket(datagramsocket, false))
            {
                i++;
            }
        }
        while (true);

        socketList.clear();
        iterator = serverSocketList.iterator();

        do
        {
            if (!iterator.hasNext())
            {
                break;
            }

            ServerSocket serversocket = (ServerSocket)iterator.next();

            if (closeServerSocket_do(serversocket, false))
            {
                i++;
            }
        }
        while (true);

        serverSocketList.clear();

        if (par1 && 0 < i)
        {
            logWarning((new StringBuilder()).append("Force closed ").append(i).append(" sockets").toString());
        }
    }
}
