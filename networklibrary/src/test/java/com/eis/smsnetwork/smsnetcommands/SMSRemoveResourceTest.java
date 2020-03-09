package com.eis.smsnetwork.smsnetcommands;

import com.eis.communication.network.NetSubscriberList;
import com.eis.communication.network.commands.CommandExecutor;
import com.eis.smslibrary.SMSPeer;
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
public class SMSRemoveResourceTest {

    private final SMSNetworkManager networkManager = SMSJoinableNetManager.getInstance();

    @Test
    public void removeResource() {
        SMSPeer peer1 = new SMSPeer("+393335435433");
        SMSPeer peer2 = new SMSPeer("+393479879876");
        NetSubscriberList<SMSPeer> subscribers =
                SMSJoinableNetManager.getInstance().getNetSubscriberList();
        subscribers.addSubscriber(peer1);
        subscribers.addSubscriber(peer2);
        String key1 = "key";
        String value1 = "value";
        String key2 = "lmao";
        String value2 = "fuck";
        SMSJoinableNetManager.getInstance().getNetDictionary().addResource(key1, value1);
        SMSJoinableNetManager.getInstance().getNetDictionary().addResource(key2, value2);
        String removeResourceMessage =
                RequestType.RemoveResource.asString() + BroadcastReceiver.FIELD_SEPARATOR +
                        SMSNetDictionary.addEscapes(key1);
        PowerMockito.mockStatic(BroadcastSender.class);

        CommandExecutor.execute(new SMSRemoveResource(key1));

        assertNull(networkManager.getNetDictionary().getResource(key1));
        assertNotNull(networkManager.getNetDictionary().getResource(key2));
        PowerMockito.verifyStatic();
        BroadcastSender.broadcastMessage(subscribers.getSubscribers(), removeResourceMessage);
    }

    @Test(expected = IllegalArgumentException.class)
    public void removeResourceWithInvalidKey() {
        CommandExecutor.execute(new SMSRemoveResource("rofl\\"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void removeResourceWithNullKey() {
        //noinspection ConstantConditions
        CommandExecutor.execute(new SMSRemoveResource(null));
    }
}