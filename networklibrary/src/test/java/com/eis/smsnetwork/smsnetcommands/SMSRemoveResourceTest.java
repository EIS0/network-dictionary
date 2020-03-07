package com.eis.smsnetwork.smsnetcommands;

import com.eis.communication.network.commands.CommandExecutor;
import com.eis.smsnetwork.SMSJoinableNetManager;
import com.eis.smsnetwork.SMSNetworkManager;
import com.eis.smsnetwork.smsnetcommands.SMSAddResource;
import com.eis.smsnetwork.smsnetcommands.SMSRemoveResource;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class SMSRemoveResourceTest {

    private final SMSNetworkManager networkManager = SMSJoinableNetManager.getInstance();

    private final String key1 = "key";
    private final String value1 = "value";

    private final String key2 = "lmao";
    private final String value2 = "fuck";

    private final SMSAddResource addResource1 = new SMSAddResource(key1, value1);
    private final SMSAddResource addResource2 = new SMSAddResource(key2, value2);
    private final SMSRemoveResource removeResource = new SMSRemoveResource(key1);
    @Before
    public void setUp(){
        CommandExecutor.execute(addResource1);
        CommandExecutor.execute(addResource2);
    }

    @Test
    public void execute1() {
        CommandExecutor.execute(removeResource);
        assertNull(networkManager.getNetDictionary().getResource(key1));
    }

    @Test
    public void execute2(){
        CommandExecutor.execute(removeResource);
        assertNotNull(networkManager.getNetDictionary().getResource(key2));
    }
}