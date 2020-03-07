package com.eis.smsnetwork.smsnetcommands;

import com.eis.communication.network.commands.CommandExecutor;
import com.eis.smslibrary.SMSPeer;
import com.eis.smsnetwork.RequestType;
import com.eis.smsnetwork.SMSJoinableNetManager;
import com.eis.smsnetwork.broadcast.BroadcastSender;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.util.HashSet;
import java.util.Set;

import static org.junit.Assert.*;
import static org.powermock.api.mockito.PowerMockito.mockStatic;
import static org.powermock.api.mockito.PowerMockito.verifyStatic;

@RunWith(PowerMockRunner.class)
@PrepareForTest({BroadcastSender.class})
public class SMSQuitNetworkTest {

    @Test
    public void quitNetwork() {
        SMSPeer peer1 = new SMSPeer("+393408140326");
        SMSPeer peer2 = new SMSPeer("+393408140366");
        Set<SMSPeer> addedPeers = new HashSet<>();
        addedPeers.add(peer1);
        addedPeers.add(peer2);
        SMSJoinableNetManager.getInstance().getNetSubscriberList().addSubscriber(peer1);
        SMSJoinableNetManager.getInstance().getNetSubscriberList().addSubscriber(peer2);
        mockStatic(BroadcastSender.class);

        CommandExecutor.execute(new SMSQuitNetwork());

        assertTrue(SMSJoinableNetManager.getInstance().getNetSubscriberList().getSubscribers().isEmpty());
        verifyStatic();
        BroadcastSender.broadcastMessage(addedPeers, RequestType.QuitNetwork.asString());
    }
}