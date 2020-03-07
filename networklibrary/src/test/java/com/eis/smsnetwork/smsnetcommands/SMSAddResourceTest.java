package com.eis.smsnetwork.smsnetcommands;

import com.eis.communication.network.commands.CommandExecutor;
import com.eis.smsnetwork.SMSJoinableNetManager;
import com.eis.smsnetwork.SMSNetworkManager;
import com.eis.smsnetwork.smsnetcommands.SMSAddResource;


import org.junit.Test;

import static org.junit.Assert.*;

public class SMSAddResourceTest {

    private final SMSNetworkManager networkManager = SMSJoinableNetManager.getInstance();

    private final String key = "key";
    private final String value = "value";

    private final SMSAddResource addResource = new SMSAddResource(key, value);

    @Test
    public void execute() {
        CommandExecutor.execute(addResource);
        assertEquals(networkManager.getNetDictionary().getResource(key), value);
    }
}