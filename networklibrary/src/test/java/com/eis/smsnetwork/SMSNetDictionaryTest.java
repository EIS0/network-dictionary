package com.eis.smsnetwork;

import com.eis.smsnetwork.SMSNetDictionary;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Unit tests for the SMSNetDictionary class
 *
 * @author Marco Cognolato
 */
public class SMSNetDictionaryTest {

    private final String KEY1 = "ResourceKey";
    private final String KEY2 = "OtherKey";
    private final String INVALID_KEY = "This is not a valid key";

    private final String RESOURCE1 = "This is a valid resource";
    private final String RESOURCE2 = "This is another valid resource";

    private SMSNetDictionary netDictionary;

    @Before
    public void setup() {
        netDictionary = new SMSNetDictionary();
    }

    @Test
    public void addResource_getsAdded() {
        netDictionary.addResource(KEY1, RESOURCE1);
        assertEquals(netDictionary.getResource(KEY1), RESOURCE1);
    }

    @Test
    public void addAnotherResource_getsAdded() {
        netDictionary.addResource(KEY1, RESOURCE1);
        netDictionary.addResource(KEY2, RESOURCE2);
        assertEquals(netDictionary.getResource(KEY1), RESOURCE1);
        assertEquals(netDictionary.getResource(KEY2), RESOURCE2);
    }

    @Test(expected = IllegalArgumentException.class)
    public void addInvalidKey_throws() {
        netDictionary.addResource(INVALID_KEY, RESOURCE1);
    }

    @Test(expected = IllegalArgumentException.class)
    public void getInvalidKey_throws() {
        netDictionary.getResource(INVALID_KEY);
    }

    @Test(expected = IllegalArgumentException.class)
    public void removeInvalidKey_throws() {
        netDictionary.removeResource(INVALID_KEY);
    }

    @Test
    public void searchResourceNotPresent_returnsNull() {
        assertNull(netDictionary.getResource(KEY1));
    }

    @Test
    public void searchResourceNotAdded_returnsNull() {
        netDictionary.addResource(KEY2, RESOURCE1);
        assertNull(netDictionary.getResource(KEY1));
    }

    @Test
    public void removeNonAddedResource_doesNothing() {
        netDictionary.removeResource(KEY1);
    }

    @Test
    public void removeResource_getsRemoved() {
        netDictionary.addResource(KEY2, RESOURCE1);
        netDictionary.removeResource(KEY2);
        assertNull(netDictionary.getResource(KEY2));
    }
}