package net.minecraft.src;

import java.io.IOException;
import java.net.*;
import java.util.*;

public class RConThreadQuery extends RConThreadBase
{
    /** The time of the last client auth check */
    private long lastAuthCheckTime;

    /** The RCon query port */
    private int queryPort;

    /** Port the server is running on */
    private int serverPort;

    /** The maximum number of players allowed on the server */
    private int maxPlayers;

    /** The current server message of the day */
    private String serverMotd;

    /** The name of the currently lo */
    private String worldName;

    /** The remote socket querying the server */
    private DatagramSocket querySocket;
    private byte buffer[];

    /** Storage for incoming DatagramPackets */
    private DatagramPacket incomingPacket;
    private HashMap field_40452_p;

    /** The hostname of this query server */
    private String queryHostname;

    /** The hostname of the running server */
    private String serverHostname;

    /** A map */
    private HashMap queryClients;
    private long field_40448_t;

    /** The RConQuery output stream */
    private RConOutputStream output;

    /** The time of the last query response sent */
    private long lastQueryResponseTime;

    public RConThreadQuery(IServer par1IServer)
    {
        super(par1IServer);
        querySocket = null;
        buffer = new byte[1460];
        incomingPacket = null;
        queryPort = par1IServer.getIntProperty("query.port", 0);
        serverHostname = par1IServer.getHostname();
        serverPort = par1IServer.getPort();
        serverMotd = par1IServer.getMotd();
        maxPlayers = par1IServer.getMaxPlayers();
        worldName = par1IServer.getWorldName();
        lastQueryResponseTime = 0L;
        queryHostname = "0.0.0.0";

        if (0 == serverHostname.length() || queryHostname.equals(serverHostname))
        {
            serverHostname = "0.0.0.0";

            try
            {
                InetAddress inetaddress = InetAddress.getLocalHost();
                queryHostname = inetaddress.getHostAddress();
            }
            catch (UnknownHostException unknownhostexception)
            {
                logWarning((new StringBuilder()).append("Unable to determine local host IP, please set server-ip in '").append(par1IServer.getSettingsFilename()).append("' : ").append(unknownhostexception.getMessage()).toString());
            }
        }
        else
        {
            queryHostname = serverHostname;
        }

        if (0 == queryPort)
        {
            queryPort = serverPort;
            log((new StringBuilder()).append("Setting default query port to ").append(queryPort).toString());
            par1IServer.setProperty("query.port", Integer.valueOf(queryPort));
            par1IServer.setProperty("debug", Boolean.valueOf(false));
            par1IServer.saveProperties();
        }

        field_40452_p = new HashMap();
        output = new RConOutputStream(1460);
        queryClients = new HashMap();
        field_40448_t = (new Date()).getTime();
    }

    /**
     * Sends a byte array as a DatagramPacket response to the client who sent the given DatagramPacket
     */
    private void sendResponsePacket(byte par1ArrayOfByte[], DatagramPacket par2DatagramPacket) throws SocketException, IOException
    {
        querySocket.send(new DatagramPacket(par1ArrayOfByte, par1ArrayOfByte.length, par2DatagramPacket.getSocketAddress()));
    }

    /**
     * Parses an incoming DatagramPacket, returning true if the packet was valid
     */
    private boolean parseIncomingPacket(DatagramPacket par1DatagramPacket) throws IOException
    {
        byte abyte0[] = par1DatagramPacket.getData();
        int i = par1DatagramPacket.getLength();
        SocketAddress socketaddress = par1DatagramPacket.getSocketAddress();
        logInfo((new StringBuilder()).append("Packet len ").append(i).append(" [").append(socketaddress).append("]").toString());

        if (3 > i || -2 != abyte0[0] || -3 != abyte0[1])
        {
            logInfo((new StringBuilder()).append("Invalid packet [").append(socketaddress).append("]").toString());
            return false;
        }

        logInfo((new StringBuilder()).append("Packet '").append(RConUtils.getByteAsHexString(abyte0[2])).append("' [").append(socketaddress).append("]").toString());

        switch (abyte0[2])
        {
            case 9:
                sendAuthChallenge(par1DatagramPacket);
                logInfo((new StringBuilder()).append("Challenge [").append(socketaddress).append("]").toString());
                return true;

            case 0:
                if (!verifyClientAuth(par1DatagramPacket).booleanValue())
                {
                    logInfo((new StringBuilder()).append("Invalid challenge [").append(socketaddress).append("]").toString());
                    return false;
                }

                if (15 != i)
                {
                    RConOutputStream rconoutputstream = new RConOutputStream(1460);
                    rconoutputstream.writeInt(0);
                    rconoutputstream.writeByteArray(getRequestID(par1DatagramPacket.getSocketAddress()));
                    rconoutputstream.writeString(serverMotd);
                    rconoutputstream.writeString("SMP");
                    rconoutputstream.writeString(worldName);
                    rconoutputstream.writeString(Integer.toString(getNumberOfPlayers()));
                    rconoutputstream.writeString(Integer.toString(maxPlayers));
                    rconoutputstream.writeShort((short)serverPort);
                    rconoutputstream.writeString(queryHostname);
                    sendResponsePacket(rconoutputstream.toByteArray(), par1DatagramPacket);
                    logInfo((new StringBuilder()).append("Status [").append(socketaddress).append("]").toString());
                }
                else
                {
                    sendResponsePacket(createQueryResponse(par1DatagramPacket), par1DatagramPacket);
                    logInfo((new StringBuilder()).append("Rules [").append(socketaddress).append("]").toString());
                }

                break;
        }

        return true;
    }

    /**
     * Creates a query response as a byte array for the specified query DatagramPacket
     */
    private byte[] createQueryResponse(DatagramPacket par1DatagramPacket) throws IOException
    {
        long l = System.currentTimeMillis();

        if (l < lastQueryResponseTime + 5000L)
        {
            byte abyte0[] = output.toByteArray();
            byte abyte1[] = getRequestID(par1DatagramPacket.getSocketAddress());
            abyte0[1] = abyte1[0];
            abyte0[2] = abyte1[1];
            abyte0[3] = abyte1[2];
            abyte0[4] = abyte1[3];
            return abyte0;
        }

        lastQueryResponseTime = l;
        output.reset();
        output.writeInt(0);
        output.writeByteArray(getRequestID(par1DatagramPacket.getSocketAddress()));
        output.writeString("splitnum");
        output.writeInt(128);
        output.writeInt(0);
        output.writeString("hostname");
        output.writeString(serverMotd);
        output.writeString("gametype");
        output.writeString("SMP");
        output.writeString("game_id");
        output.writeString("MINECRAFT");
        output.writeString("version");
        output.writeString(server.getVersionString());
        output.writeString("plugins");
        output.writeString(server.getPlugin());
        output.writeString("map");
        output.writeString(worldName);
        output.writeString("numplayers");
        output.writeString((new StringBuilder()).append("").append(getNumberOfPlayers()).toString());
        output.writeString("maxplayers");
        output.writeString((new StringBuilder()).append("").append(maxPlayers).toString());
        output.writeString("hostport");
        output.writeString((new StringBuilder()).append("").append(serverPort).toString());
        output.writeString("hostip");
        output.writeString(queryHostname);
        output.writeInt(0);
        output.writeInt(1);
        output.writeString("player_");
        output.writeInt(0);
        String as[] = server.getPlayerNamesAsList();
        byte byte0 = (byte)as.length;

        for (byte byte1 = (byte)(byte0 - 1); byte1 >= 0; byte1--)
        {
            output.writeString(as[byte1]);
        }

        output.writeInt(0);
        return output.toByteArray();
    }

    /**
     * Returns the request ID provided by the authorized client
     */
    private byte[] getRequestID(SocketAddress par1SocketAddress)
    {
        return ((RConThreadQueryAuth)queryClients.get(par1SocketAddress)).getRequestID();
    }

    /**
     * Returns true if the client has a valid auth, otherwise false
     */
    private Boolean verifyClientAuth(DatagramPacket par1DatagramPacket)
    {
        SocketAddress socketaddress = par1DatagramPacket.getSocketAddress();

        if (!queryClients.containsKey(socketaddress))
        {
            return Boolean.valueOf(false);
        }

        byte abyte0[] = par1DatagramPacket.getData();

        if (((RConThreadQueryAuth)queryClients.get(socketaddress)).getRandomChallenge() != RConUtils.getBytesAsBEint(abyte0, 7, par1DatagramPacket.getLength()))
        {
            return Boolean.valueOf(false);
        }
        else
        {
            return Boolean.valueOf(true);
        }
    }

    /**
     * Sends an auth challenge DatagramPacket to the client and adds the client to the queryClients map
     */
    private void sendAuthChallenge(DatagramPacket par1DatagramPacket) throws SocketException, IOException
    {
        RConThreadQueryAuth rconthreadqueryauth = new RConThreadQueryAuth(this, par1DatagramPacket);
        queryClients.put(par1DatagramPacket.getSocketAddress(), rconthreadqueryauth);
        sendResponsePacket(rconthreadqueryauth.getChallengeValue(), par1DatagramPacket);
    }

    /**
     * Removes all clients whose auth is no longer valid
     */
    private void cleanQueryClientsMap()
    {
        if (!running)
        {
            return;
        }

        long l = System.currentTimeMillis();

        if (l < lastAuthCheckTime + 30000L)
        {
            return;
        }

        lastAuthCheckTime = l;
        Iterator iterator = queryClients.entrySet().iterator();

        do
        {
            if (!iterator.hasNext())
            {
                break;
            }

            java.util.Map.Entry entry = (java.util.Map.Entry)iterator.next();

            if (((RConThreadQueryAuth)entry.getValue()).hasExpired(l).booleanValue())
            {
                iterator.remove();
            }
        }
        while (true);
    }

    public void run()
    {
        log((new StringBuilder()).append("Query running on ").append(serverHostname).append(":").append(queryPort).toString());
        lastAuthCheckTime = System.currentTimeMillis();
        incomingPacket = new DatagramPacket(buffer, buffer.length);

        try
        {
            while (running)
            {
                try
                {
                    querySocket.receive(incomingPacket);
                    cleanQueryClientsMap();
                    parseIncomingPacket(incomingPacket);
                }
                catch (SocketTimeoutException sockettimeoutexception)
                {
                    cleanQueryClientsMap();
                }
                catch (PortUnreachableException portunreachableexception) { }
                catch (IOException ioexception)
                {
                    stopWithException(ioexception);
                }
            }
        }
        finally
        {
            closeAllSockets();
        }
    }

    /**
     * Creates a new Thread object from this class and starts running
     */
    public void startThread()
    {
        if (running)
        {
            return;
        }

        if (0 >= queryPort || 65535 < queryPort)
        {
            logWarning((new StringBuilder()).append("Invalid query port ").append(queryPort).append(" found in '").append(server.getSettingsFilename()).append("' (queries disabled)").toString());
            return;
        }

        if (initQuerySystem())
        {
            super.startThread();
        }
    }

    /**
     * Stops the query server and reports the given Exception
     */
    private void stopWithException(Exception par1Exception)
    {
        if (!running)
        {
            return;
        }

        logWarning((new StringBuilder()).append("Unexpected exception, buggy JRE? (").append(par1Exception.toString()).append(")").toString());

        if (!initQuerySystem())
        {
            logSevere("Failed to recover from buggy JRE, shutting down!");
            running = false;
            server.func_40010_o();
        }
    }

    /**
     * Initializes the query system by binding it to a port
     */
    private boolean initQuerySystem()
    {
        try
        {
            querySocket = new DatagramSocket(queryPort, InetAddress.getByName(serverHostname));
            registerSocket(querySocket);
            querySocket.setSoTimeout(500);
            return true;
        }
        catch (SocketException socketexception)
        {
            logWarning((new StringBuilder()).append("Unable to initialise query system on ").append(serverHostname).append(":").append(queryPort).append(" (Socket): ").append(socketexception.getMessage()).toString());
        }
        catch (UnknownHostException unknownhostexception)
        {
            logWarning((new StringBuilder()).append("Unable to initialise query system on ").append(serverHostname).append(":").append(queryPort).append(" (Unknown Host): ").append(unknownhostexception.getMessage()).toString());
        }
        catch (Exception exception)
        {
            logWarning((new StringBuilder()).append("Unable to initialise query system on ").append(serverHostname).append(":").append(queryPort).append(" (E): ").append(exception.getMessage()).toString());
        }

        return false;
    }
}
