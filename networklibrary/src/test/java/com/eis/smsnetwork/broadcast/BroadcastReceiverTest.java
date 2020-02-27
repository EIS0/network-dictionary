package com.eis.smsnetwork.broadcast;

import android.content.res.AssetFileDescriptor;
import android.util.Log;

import com.eis.communication.network.commands.CommandExecutor;
import com.eis.smslibrary.SMSManager;
import com.eis.smslibrary.SMSMessage;
import com.eis.smslibrary.SMSPeer;
import com.eis.smsnetwork.RequestType;
import com.eis.smsnetwork.SMSInvitation;
import com.eis.smsnetwork.SMSJoinableNetManager;
import com.eis.smsnetwork.SMSNetDictionary;
import com.eis.smsnetwork.SMSNetSubscriberList;
import com.eis.smsnetwork.smsnetcommands.SMSAddPeer;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.util.HashSet;
import java.util.Set;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.powermock.api.mockito.PowerMockito.verifyStatic;
import static org.powermock.api.mockito.PowerMockito.when;
import static com.eis.smsnetwork.broadcast.BroadcastReceiver.FIELD_SEPARATOR;

/**
 * @author Giovanni Velludo
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest({SMSJoinableNetManager.class, SMSManager.class, Log.class})
public class BroadcastReceiverTest {

    @Captor
    ArgumentCaptor<SMSInvitation> invitationCaptor;

    @Captor
    ArgumentCaptor<SMSMessage> messageCaptor;

    @Before
    public void setUp() {
        PowerMockito.mockStatic(Log.class);
        when(Log.d(anyString(), anyString())).thenReturn(0);
    }

    @Test
    public void separatorRegex() {
        String message = "è" + FIELD_SEPARATOR + "greeting" + FIELD_SEPARATOR + "howdily\\" +
                FIELD_SEPARATOR + "doodily" + FIELD_SEPARATOR + "tree" + FIELD_SEPARATOR + "pinus\\"
                + FIELD_SEPARATOR + "pinaster\\" + FIELD_SEPARATOR;
        String[] expectedFields = {"è", "greeting", "howdily\\" + FIELD_SEPARATOR + "doodily",
                "tree", "pinus" + "\\" + FIELD_SEPARATOR + "pinaster\\" + FIELD_SEPARATOR};
        String[] fields = message.split(BroadcastReceiver.SEPARATOR_REGEX);
        Assert.assertArrayEquals(expectedFields, fields);

        message = "\\" + FIELD_SEPARATOR + "\\" + FIELD_SEPARATOR + FIELD_SEPARATOR + "\\hello";
        expectedFields = new String[]{"\\" + FIELD_SEPARATOR + "\\" + FIELD_SEPARATOR, "\\hello"};
        fields = message.split(BroadcastReceiver.SEPARATOR_REGEX);
        Assert.assertArrayEquals(expectedFields, fields);

        message = "word" + FIELD_SEPARATOR + "\\\\" + FIELD_SEPARATOR + "word" + FIELD_SEPARATOR +
                "\\" + FIELD_SEPARATOR + "word";
        expectedFields = new String[]{"word", "\\\\" + FIELD_SEPARATOR + "word",
                "\\" + FIELD_SEPARATOR + "word"};
        fields = message.split(BroadcastReceiver.SEPARATOR_REGEX);
        Assert.assertArrayEquals(expectedFields, fields);
    }

    @Test
    public void onMessageReceived_garbage_isIgnored() {
        BroadcastReceiver instance = new BroadcastReceiver();
        SMSPeer sender = new SMSPeer("+393492794133");
        SMSMessage garbage = new SMSMessage(sender, "aidsajfksda;ds");

        SMSJoinableNetManager mockManager = mock(SMSJoinableNetManager.class);
        when(mockManager.getNetSubscriberList()).thenReturn(new SMSNetSubscriberList());
        when(mockManager.getNetDictionary()).thenReturn(new SMSNetDictionary());
        PowerMockito.mockStatic(SMSJoinableNetManager.class);
        when(SMSJoinableNetManager.getInstance()).thenReturn(mockManager);

        instance.onMessageReceived(garbage);
        PowerMockito.verifyStatic(never());
        SMSJoinableNetManager.getInstance();
    }

    @Test
    public void onMessageReceived_inviteWithGarbage_isIgnored() {
        BroadcastReceiver instance = new BroadcastReceiver();
        SMSPeer sender = new SMSPeer("+393492794133");
        String garbageText = RequestType.Invite.asString() + ">";
        SMSMessage garbageMessage = new SMSMessage(sender, garbageText);

        SMSJoinableNetManager mockManager = mock(SMSJoinableNetManager.class);
        when(mockManager.getNetSubscriberList()).thenReturn(new SMSNetSubscriberList());
        when(mockManager.getNetDictionary()).thenReturn(new SMSNetDictionary());
        PowerMockito.mockStatic(SMSJoinableNetManager.class);
        when(SMSJoinableNetManager.getInstance()).thenReturn(mockManager);

        instance.onMessageReceived(garbageMessage);
        verify(mockManager, never()).checkInvitation(any());
    }

    @Test
    public void onMessageReceived_correctInvite() {
        BroadcastReceiver instance = new BroadcastReceiver();
        SMSPeer sender = new SMSPeer("+393492794133");
        String correctText = RequestType.Invite.asString();
        SMSMessage correctMessage = new SMSMessage(sender, correctText);
        Set<SMSPeer> subscribersSet = new HashSet<>();
        subscribersSet.add(sender);

        SMSNetSubscriberList mockSubscribers = mock(SMSNetSubscriberList.class);
        when(mockSubscribers.getSubscribers()).thenReturn(subscribersSet);
        SMSJoinableNetManager mockManager = mock(SMSJoinableNetManager.class);
        when(mockManager.getNetSubscriberList()).thenReturn(mockSubscribers);
        when(mockManager.getNetDictionary()).thenReturn(new SMSNetDictionary());
        PowerMockito.mockStatic(SMSJoinableNetManager.class);
        when(SMSJoinableNetManager.getInstance()).thenReturn(mockManager);

        instance.onMessageReceived(correctMessage);
        verify(mockManager).checkInvitation(invitationCaptor.capture());
        Assert.assertEquals(sender, invitationCaptor.getValue().getInviterPeer());
    }

    @Test
    public void onMessageReceived_correctAcceptInvitation() {
        BroadcastReceiver instance = new BroadcastReceiver();
        SMSPeer sender = new SMSPeer("+393492794133");
        SMSPeer subscriber = new SMSPeer("+393332734121");
        String correctText = RequestType.AcceptInvitation.asString();
        SMSMessage correctMessage = new SMSMessage(sender, correctText);
        Set<SMSPeer> subscribersSet = new HashSet<>();
        subscribersSet.add(subscriber);
        Set<SMSPeer> invitedPeers = new HashSet<>();
        invitedPeers.add(sender);
        String expectedMySubscribersText = RequestType.AddPeer.asString() + FIELD_SEPARATOR + subscriber;
        String expectedMyDictionaryText = RequestType.AddResource.asString() + FIELD_SEPARATOR + "Key¤This is a valid resource¤OtherKey¤This is another valid resource";
        String expectedAddPeerTextForSubscribers = RequestType.AddPeer.asString() + FIELD_SEPARATOR + sender;

        SMSManager mockSMSManager = mock(SMSManager.class);
        SMSNetSubscriberList mockSubscribers = mock(SMSNetSubscriberList.class);
        when(mockSubscribers.getSubscribers()).thenReturn(subscribersSet);
        SMSNetDictionary mockDictionary = mock(SMSNetDictionary.class);
        when(mockDictionary.getAllKeyResourcePairsForSMS()).thenReturn("Key¤This is a valid resource¤OtherKey¤This is another valid resource");
        SMSJoinableNetManager mockNetworkManager = mock(SMSJoinableNetManager.class);
        when(mockNetworkManager.getNetSubscriberList()).thenReturn(mockSubscribers);
        when(mockNetworkManager.getNetDictionary()).thenReturn(mockDictionary);
        when(mockNetworkManager.getInvitedPeers()).thenReturn(invitedPeers);
        PowerMockito.mockStatic(SMSJoinableNetManager.class);
        when(SMSJoinableNetManager.getInstance()).thenReturn(mockNetworkManager);
        PowerMockito.mockStatic(SMSManager.class);
        when(SMSManager.getInstance()).thenReturn(mockSMSManager);

        instance.onMessageReceived(correctMessage);
        verify(mockSMSManager, times(3)).sendMessage(messageCaptor.capture());
        Assert.assertEquals(sender, messageCaptor.getAllValues().get(0).getPeer());
        Assert.assertEquals(expectedMySubscribersText, messageCaptor.getAllValues().get(0).getData());
        Assert.assertEquals(sender, messageCaptor.getAllValues().get(1).getPeer());
        Assert.assertEquals(expectedMyDictionaryText, messageCaptor.getAllValues().get(1).getData());
        Assert.assertEquals(subscriber, messageCaptor.getAllValues().get(2).getPeer());
        Assert.assertEquals(expectedAddPeerTextForSubscribers, messageCaptor.getAllValues().get(2).getData());
        verify(mockSubscribers, times(2)).addSubscriber(sender);
    }

    @Test
    public void onMessageReceived_quitNetworkWithGarbage_isIgnored() {
        BroadcastReceiver instance = new BroadcastReceiver();
        SMSPeer sender = new SMSPeer("+393492794133");
        String garbageText = RequestType.QuitNetwork.asString() + ">";
        SMSMessage garbageMessage = new SMSMessage(sender, garbageText);
        Set<SMSPeer> subscribersSet = new HashSet<>();
        subscribersSet.add(sender);

        SMSNetSubscriberList mockSubscribers = mock(SMSNetSubscriberList.class);
        when(mockSubscribers.getSubscribers()).thenReturn(subscribersSet);
        SMSJoinableNetManager mockManager = mock(SMSJoinableNetManager.class);
        when(mockManager.getNetSubscriberList()).thenReturn(mockSubscribers);
        when(mockManager.getNetDictionary()).thenReturn(new SMSNetDictionary());
        PowerMockito.mockStatic(SMSJoinableNetManager.class);
        when(SMSJoinableNetManager.getInstance()).thenReturn(mockManager);

        instance.onMessageReceived(garbageMessage);
        verify(mockSubscribers, never()).removeSubscriber(any());
    }

    @Test
    public void onMessageReceived_correctQuitNetwork() {
        BroadcastReceiver instance = new BroadcastReceiver();
        SMSPeer sender = new SMSPeer("+393492794133");
        String correctText = RequestType.QuitNetwork.asString();
        SMSMessage correctMessage = new SMSMessage(sender, correctText);
        Set<SMSPeer> subscribersSet = new HashSet<>();
        subscribersSet.add(sender);

        SMSNetSubscriberList mockSubscribers = mock(SMSNetSubscriberList.class);
        when(mockSubscribers.getSubscribers()).thenReturn(subscribersSet);
        SMSJoinableNetManager mockManager = mock(SMSJoinableNetManager.class);
        when(mockManager.getNetSubscriberList()).thenReturn(mockSubscribers);
        when(mockManager.getNetDictionary()).thenReturn(new SMSNetDictionary());
        PowerMockito.mockStatic(SMSJoinableNetManager.class);
        when(SMSJoinableNetManager.getInstance()).thenReturn(mockManager);

        instance.onMessageReceived(correctMessage);
        verify(mockSubscribers).removeSubscriber(sender);
    }

    @Test
    public void onMessageReceived_addPeerFromNonSubscriber_isIgnored() {
        BroadcastReceiver instance = new BroadcastReceiver();
        SMSPeer sender = new SMSPeer("+393492794133");
        String garbageText = RequestType.AddPeer.asString() + FIELD_SEPARATOR + "+393478512584" +
                FIELD_SEPARATOR + "+393338512123";
        SMSMessage garbageMessage = new SMSMessage(sender, garbageText);
        Set<SMSPeer> subscribersSet = new HashSet<>();

        SMSNetSubscriberList mockSubscribers = mock(SMSNetSubscriberList.class);
        when(mockSubscribers.getSubscribers()).thenReturn(subscribersSet);
        SMSJoinableNetManager mockManager = mock(SMSJoinableNetManager.class);
        when(mockManager.getNetSubscriberList()).thenReturn(mockSubscribers);
        when(mockManager.getNetDictionary()).thenReturn(new SMSNetDictionary());
        PowerMockito.mockStatic(SMSJoinableNetManager.class);
        when(SMSJoinableNetManager.getInstance()).thenReturn(mockManager);

        instance.onMessageReceived(garbageMessage);
        verify(mockSubscribers, never()).addSubscriber(any());
    }

    @Test
    public void onMessageReceived_addPeerWithWrongNumber_isIgnored() {
        BroadcastReceiver instance = new BroadcastReceiver();
        SMSPeer sender = new SMSPeer("+393492794133");
        String garbageText = RequestType.AddPeer.asString() + FIELD_SEPARATOR + "+393478512584" +
                FIELD_SEPARATOR + "+0000333812123";
        SMSMessage garbageMessage = new SMSMessage(sender, garbageText);
        Set<SMSPeer> subscribersSet = new HashSet<>();
        subscribersSet.add(sender);

        SMSNetSubscriberList mockSubscribers = mock(SMSNetSubscriberList.class);
        when(mockSubscribers.getSubscribers()).thenReturn(subscribersSet);
        SMSJoinableNetManager mockManager = mock(SMSJoinableNetManager.class);
        when(mockManager.getNetSubscriberList()).thenReturn(mockSubscribers);
        when(mockManager.getNetDictionary()).thenReturn(new SMSNetDictionary());
        PowerMockito.mockStatic(SMSJoinableNetManager.class);
        when(SMSJoinableNetManager.getInstance()).thenReturn(mockManager);

        instance.onMessageReceived(garbageMessage);
        verify(mockSubscribers, never()).addSubscriber(any());
    }

    @Test
    public void onMessageReceived_correctAddPeer() {
        BroadcastReceiver instance = new BroadcastReceiver();
        SMSPeer sender = new SMSPeer("+393492794133");
        String correctText = RequestType.AddPeer.asString() + FIELD_SEPARATOR + "+393478512584" +
                FIELD_SEPARATOR + "+39333812123";
        SMSMessage correctMessage = new SMSMessage(sender, correctText);
        Set<SMSPeer> subscribersSet = new HashSet<>();
        subscribersSet.add(sender);

        SMSNetSubscriberList mockSubscribers = mock(SMSNetSubscriberList.class);
        when(mockSubscribers.getSubscribers()).thenReturn(subscribersSet);
        SMSJoinableNetManager mockManager = mock(SMSJoinableNetManager.class);
        when(mockManager.getNetSubscriberList()).thenReturn(mockSubscribers);
        when(mockManager.getNetDictionary()).thenReturn(new SMSNetDictionary());
        PowerMockito.mockStatic(SMSJoinableNetManager.class);
        when(SMSJoinableNetManager.getInstance()).thenReturn(mockManager);

        instance.onMessageReceived(correctMessage);
        verify(mockSubscribers).addSubscriber(new SMSPeer("+393478512584"));
        verify(mockSubscribers).addSubscriber(new SMSPeer("+39333812123"));
    }

    @Test
    public void onMessageReceived_addResourceWithNoResources_isIgnored() {
        BroadcastReceiver instance = new BroadcastReceiver();
        SMSPeer sender = new SMSPeer("+393492794133");
        String garbageText = RequestType.AddResource.asString();
        SMSMessage garbageMessage = new SMSMessage(sender, garbageText);
        Set<SMSPeer> subscribersSet = new HashSet<>();
        subscribersSet.add(sender);

        SMSNetSubscriberList mockSubscribers = mock(SMSNetSubscriberList.class);
        when(mockSubscribers.getSubscribers()).thenReturn(subscribersSet);
        SMSNetDictionary mockDictionary = mock(SMSNetDictionary.class);
        SMSJoinableNetManager mockManager = mock(SMSJoinableNetManager.class);
        when(mockManager.getNetSubscriberList()).thenReturn(mockSubscribers);
        when(mockManager.getNetDictionary()).thenReturn(mockDictionary);
        PowerMockito.mockStatic(SMSJoinableNetManager.class);
        when(SMSJoinableNetManager.getInstance()).thenReturn(mockManager);

        instance.onMessageReceived(garbageMessage);
        verify(mockDictionary, never()).addResourceFromSMS(any(), any());
        verify(mockDictionary, never()).addResourceFromSMS(any(), any());
    }

    @Test
    public void onMessageReceived_correctAddResource() {
        BroadcastReceiver instance = new BroadcastReceiver();
        SMSPeer sender = new SMSPeer("+393492794133");
        String correctText = RequestType.AddResource.asString() + FIELD_SEPARATOR + "the cat is " +
                "on" + FIELD_SEPARATOR + "the table" + FIELD_SEPARATOR + "the book is" +
                FIELD_SEPARATOR + "under the table";
        SMSMessage correctMessage = new SMSMessage(sender, correctText);
        Set<SMSPeer> subscribersSet = new HashSet<>();
        subscribersSet.add(sender);

        SMSNetSubscriberList mockSubscribers = mock(SMSNetSubscriberList.class);
        when(mockSubscribers.getSubscribers()).thenReturn(subscribersSet);
        SMSNetDictionary mockDictionary = mock(SMSNetDictionary.class);
        SMSJoinableNetManager mockManager = mock(SMSJoinableNetManager.class);
        when(mockManager.getNetSubscriberList()).thenReturn(mockSubscribers);
        when(mockManager.getNetDictionary()).thenReturn(mockDictionary);
        PowerMockito.mockStatic(SMSJoinableNetManager.class);
        when(SMSJoinableNetManager.getInstance()).thenReturn(mockManager);

        instance.onMessageReceived(correctMessage);
        verify(mockDictionary).addResourceFromSMS("the cat is on", "the table");
        verify(mockDictionary).addResourceFromSMS("the book is", "under the table");
    }

    @Test
    public void onMessageReceived_removeResourceWithNoResources_isIgnored() {
        BroadcastReceiver instance = new BroadcastReceiver();
        SMSPeer sender = new SMSPeer("+393492794133");
        String garbageText = RequestType.RemoveResource.asString();
        SMSMessage garbageMessage = new SMSMessage(sender, garbageText);
        Set<SMSPeer> subscribersSet = new HashSet<>();
        subscribersSet.add(sender);

        SMSNetSubscriberList mockSubscribers = mock(SMSNetSubscriberList.class);
        when(mockSubscribers.getSubscribers()).thenReturn(subscribersSet);
        SMSNetDictionary mockDictionary = mock(SMSNetDictionary.class);
        SMSJoinableNetManager mockManager = mock(SMSJoinableNetManager.class);
        when(mockManager.getNetSubscriberList()).thenReturn(mockSubscribers);
        when(mockManager.getNetDictionary()).thenReturn(mockDictionary);
        PowerMockito.mockStatic(SMSJoinableNetManager.class);
        when(SMSJoinableNetManager.getInstance()).thenReturn(mockManager);

        instance.onMessageReceived(garbageMessage);
        verify(mockDictionary, never()).removeResourceFromSMS(any());
        verify(mockDictionary, never()).removeResourceFromSMS(any());
    }

    @Test
    public void onMessageReceived_correctRemoveResource() {
        BroadcastReceiver instance = new BroadcastReceiver();
        SMSPeer sender = new SMSPeer("+393492794133");
        String correctText = RequestType.RemoveResource.asString() + FIELD_SEPARATOR + "the cat " +
                "is on" + FIELD_SEPARATOR + "the table";
        SMSMessage correctMessage = new SMSMessage(sender, correctText);
        Set<SMSPeer> subscribersSet = new HashSet<>();
        subscribersSet.add(sender);

        SMSNetSubscriberList mockSubscribers = mock(SMSNetSubscriberList.class);
        when(mockSubscribers.getSubscribers()).thenReturn(subscribersSet);
        SMSNetDictionary mockDictionary = mock(SMSNetDictionary.class);
        SMSJoinableNetManager mockManager = mock(SMSJoinableNetManager.class);
        when(mockManager.getNetSubscriberList()).thenReturn(mockSubscribers);
        when(mockManager.getNetDictionary()).thenReturn(mockDictionary);
        PowerMockito.mockStatic(SMSJoinableNetManager.class);
        when(SMSJoinableNetManager.getInstance()).thenReturn(mockManager);

        instance.onMessageReceived(correctMessage);
        verify(mockDictionary).removeResourceFromSMS("the cat is on");
        verify(mockDictionary).removeResourceFromSMS("the table");
    }
}