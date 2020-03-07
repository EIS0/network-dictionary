package com.eis.smsnetwork;

import com.eis.smslibrary.SMSPeer;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertArrayEquals;

/**
 * Unit tests for the SMSNetSubscriberList class
 *
 * @author Marco Cognolato
 * @author Giovanni Velludo
 */
public class SMSNetSubscriberListTest {

    public static final SMSPeer PEER1 = new SMSPeer("+393423541601");
    public static final SMSPeer PEER2 = new SMSPeer("+393423541602");
    public static final SMSPeer PEER3 = new SMSPeer("+393423541603");

    private SMSNetSubscriberList netSubscribers;

    @Before
    public void setup() {
        netSubscribers = new SMSNetSubscriberList();
    }

    @Test
    public void addOnePeer_getsAdded() {
        netSubscribers.addSubscriber(PEER1);
        assertArrayEquals(netSubscribers.getSubscribers().toArray(),
                new SMSPeer[]{PEER1});
    }

    @Test
    public void addTwoPeer_getsAdded() {
        netSubscribers.addSubscriber(PEER1);
        netSubscribers.addSubscriber(PEER2);
        assertArrayEquals(netSubscribers.getSubscribers().toArray(),
                new SMSPeer[]{PEER2, PEER1});
    }

    @Test
    public void addThreePeer_getsAdded() {
        netSubscribers.addSubscriber(PEER1);
        netSubscribers.addSubscriber(PEER2);
        netSubscribers.addSubscriber(PEER3);
        assertArrayEquals(netSubscribers.getSubscribers().toArray(),
                new SMSPeer[]{PEER3, PEER2, PEER1});
    }

    @Test
    public void removePeer_getsRemoved() {
        netSubscribers.addSubscriber(PEER1);
        netSubscribers.removeSubscriber(PEER1);
        assertArrayEquals(netSubscribers.getSubscribers().toArray(),
                new SMSPeer[]{});
    }

    @Test(expected = IllegalArgumentException.class)
    public void removeNonPresentPeer_throws() {
        netSubscribers.removeSubscriber(PEER1);
    }

    @Test(expected = IllegalArgumentException.class)
    public void addNullPeer_throws() {
        //noinspection ConstantConditions
        netSubscribers.addSubscriber(null);
    }
}