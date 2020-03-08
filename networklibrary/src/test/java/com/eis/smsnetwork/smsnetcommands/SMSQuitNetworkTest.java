package com.eis.smsnetwork.smsnetcommands;

import com.eis.communication.network.commands.CommandExecutor;
import com.eis.smslibrary.SMSManager;
import com.eis.smslibrary.SMSMessage;
import com.eis.smslibrary.SMSPeer;
import com.eis.smsnetwork.RequestType;
import com.eis.smsnetwork.SMSJoinableNetManager;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.util.HashSet;
import java.util.Set;

@RunWith(PowerMockRunner.class)
@PrepareForTest({SMSManager.class})
public class SMSQuitNetworkTest {

    @Captor
    ArgumentCaptor<SMSMessage> messageCaptor;

    @Test
    public void quitNetwork() {
        SMSPeer peer1 = new SMSPeer("+393408140326");
        SMSPeer peer2 = new SMSPeer("+393408140366");
        SMSJoinableNetManager.getInstance().getNetSubscriberList().addSubscriber(peer1);
        SMSJoinableNetManager.getInstance().getNetSubscriberList().addSubscriber(peer2);
        // we manually copy peers to this set instead of simply calling SMSJoinableNetManager
        // .getInstance().getNetSubscriberList().getSubscribers() because in that case we would
        // get a pointer to the list of subscribers, which is cleared after the execution of
        // SMSQuitNetwork. What we want though is the list of subscribers before the execution of
        // that command.
        Set<SMSPeer> subscribers =
                new HashSet<>(SMSJoinableNetManager.getInstance().getNetSubscriberList()
                        .getSubscribers());
        String quitNetworkMessage = RequestType.QuitNetwork.asString();
        SMSManager mockManager = Mockito.mock(SMSManager.class);
        PowerMockito.mockStatic(SMSManager.class);
        PowerMockito.when(SMSManager.getInstance()).thenReturn(mockManager);

        CommandExecutor.execute(new SMSQuitNetwork());

        Assert.assertTrue(SMSJoinableNetManager.getInstance().getNetSubscriberList()
                .getSubscribers().isEmpty());
        // verifying that a message was sent for each subscriber
        Mockito.verify(mockManager, Mockito.times(subscribers.size())).sendMessage(
                messageCaptor.capture());
        // verifying that the message sent is the same as quitNetworkMessage
        for (SMSMessage sentMessage : messageCaptor.getAllValues()) {
            Assert.assertEquals(quitNetworkMessage, sentMessage.getData());
        }
        // verifying that the peers we sent the message to were our subscribers
        Set<SMSPeer> recipients = new HashSet<>();
        for (SMSMessage sentMessage : messageCaptor.getAllValues()) {
            recipients.add(sentMessage.getPeer());
        }
        Assert.assertEquals(subscribers, recipients);
    }
}