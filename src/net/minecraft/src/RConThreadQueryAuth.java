package net.minecraft.src;

import java.net.DatagramPacket;
import java.util.Date;
import java.util.Random;

class RConThreadQueryAuth
{
    /** The creation timestamp for this auth */
    private long timestamp;

    /** A */
    private int randomChallenge;
    private byte requestID[];
    private byte challengeValue[];

    /** The request ID stored as a String */
    private String requestIDstring;

    /** The RConThreadQuery that this is probably an inner class of */
    final RConThreadQuery queryThread;

    public RConThreadQueryAuth(RConThreadQuery par1RConThreadQuery, DatagramPacket par2DatagramPacket)
    {
        queryThread = par1RConThreadQuery;
        timestamp = (new Date()).getTime();
        byte abyte0[] = par2DatagramPacket.getData();
        requestID = new byte[4];
        requestID[0] = abyte0[3];
        requestID[1] = abyte0[4];
        requestID[2] = abyte0[5];
        requestID[3] = abyte0[6];
        requestIDstring = new String(requestID);
        randomChallenge = (new Random()).nextInt(0x1000000);
        challengeValue = String.format("\t%s%d\0", new Object[]
                {
                    requestIDstring, Integer.valueOf(randomChallenge)
                }).getBytes();
    }

    /**
     * Returns true if the auth's creation timestamp is less than the given time, otherwise false
     */
    public Boolean hasExpired(long par1)
    {
        return Boolean.valueOf(timestamp < par1);
    }

    /**
     * Returns the random challenge number assigned to this auth
     */
    public int getRandomChallenge()
    {
        return randomChallenge;
    }

    /**
     * Returns the auth challenge value
     */
    public byte[] getChallengeValue()
    {
        return challengeValue;
    }

    /**
     * Returns the request ID provided by the client
     */
    public byte[] getRequestID()
    {
        return requestID;
    }
}
