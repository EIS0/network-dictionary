package com.eis.smsnetwork.smsnetcommands;

import com.eis.communication.network.commands.CommandExecutor;
import com.eis.smslibrary.SMSPeer;
import com.eis.smsnetwork.RequestType;
import com.eis.smsnetwork.SMSJoinableNetManager;
import com.eis.smsnetwork.broadcast.BroadcastReceiver;
import com.eis.smsnetwork.broadcast.BroadcastSender;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import static org.junit.Assert.*;

/**
 * @author Marco Cognolato
 * @author Giovanni Velludo
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest({BroadcastSender.class})
public class SMSAddPeerTest {

    private final SMSPeer peerToAdd = new SMSPeer("+393408140326");
    private final SMSJoinableNetManager networkManager = SMSJoinableNetManager.getInstance();

    @Test
    public void addPeer() {
        String expectedMessage = RequestType.AddPeer.asString() + BroadcastReceiver.FIELD_SEPARATOR
                + peerToAdd.getAddress();
        PowerMockito.mockStatic(BroadcastSender.class);

        CommandExecutor.execute(new SMSAddPeer(peerToAdd));

        assertTrue(networkManager.getNetSubscriberList().getSubscribers().contains(peerToAdd));
        PowerMockito.verifyStatic();
        BroadcastSender.broadcastMessage(networkManager.getNetSubscriberList().getSubscribers(),
                expectedMessage);
    }

    @Test(expected = IllegalArgumentException.class)
    public void addNullPeer() {
        //noinspection ConstantConditions
        CommandExecutor.execute(new SMSAddPeer(null));
    }
}