package com.eis.smsnetwork.smsnetcommands;

import com.eis.communication.network.commands.CommandExecutor;
import com.eis.smsnetwork.RequestType;
import com.eis.smsnetwork.SMSJoinableNetManager;
import com.eis.smsnetwork.SMSNetDictionary;
import com.eis.smsnetwork.SMSNetworkManager;
import com.eis.smsnetwork.broadcast.BroadcastReceiver;
import com.eis.smsnetwork.broadcast.BroadcastSender;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import static org.junit.Assert.*;

@RunWith(PowerMockRunner.class)
@PrepareForTest({BroadcastSender.class})
public class SMSAddResourceTest {

    private final String key = "key";
    private final String value = "value";

    @Test
    public void addKeyValue() {
        SMSNetworkManager networkManager = SMSJoinableNetManager.getInstance();
        String expectedMessage =
                RequestType.AddResource.asString() + BroadcastReceiver.FIELD_SEPARATOR + SMSNetDictionary.addEscapes(key) + BroadcastReceiver.FIELD_SEPARATOR + SMSNetDictionary.addEscapes(value);
        PowerMockito.mockStatic(BroadcastSender.class);

        CommandExecutor.execute(new SMSAddResource(key, value));

        assertEquals(networkManager.getNetDictionary().getResource(key), value);
        PowerMockito.verifyStatic();
        BroadcastSender.broadcastMessage(networkManager.getNetSubscriberList().getSubscribers(),
                expectedMessage);
    }

    @Test(expected = IllegalArgumentException.class)
    public void addNullKey() {
        //noinspection ConstantConditions
        CommandExecutor.execute(new SMSAddResource(null, value));
    }
    @Test(expected = IllegalArgumentException.class)
    public void addNullValue() {
        //noinspection ConstantConditions
        CommandExecutor.execute(new SMSAddResource(key, null));
    }
}