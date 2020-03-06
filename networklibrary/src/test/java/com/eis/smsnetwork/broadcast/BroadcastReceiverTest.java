package com.eis.smsnetwork.broadcast;

import android.util.Log;

import com.eis.smslibrary.SMSManager;
import com.eis.smslibrary.SMSMessage;
import com.eis.smslibrary.SMSPeer;
import com.eis.smsnetwork.RequestType;
import com.eis.smsnetwork.SMSInvitation;
import com.eis.smsnetwork.SMSJoinableNetManager;
import com.eis.smsnetwork.SMSNetDictionary;
import com.eis.smsnetwork.SMSNetSubscriberList;

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
        when(Log.e(anyString(), anyString())).thenReturn(0);
    }

    /**
     * Tests whether {@link BroadcastReceiver#SEPARATOR_REGEX} correctly splits fields in received
     * messages, that is whenever a non escaped {@link BroadcastReceiver#FIELD_SEPARATOR} is found.
     */
    @Test
    public void separatorRegex() {
        String message =
                "è" + FIELD_SEPARATOR + "greeting" + FIELD_SEPARATOR + "howdily\\" + FIELD_SEPARATOR
                        + "doodily" + FIELD_SEPARATOR + "tree" + FIELD_SEPARATOR + "pinus\\" +
                        FIELD_SEPARATOR + "pinaster\\" + FIELD_SEPARATOR;
        String[] expectedFields = {"è", "greeting", "howdily\\" + FIELD_SEPARATOR + "doodily",
                "tree", "pinus" + "\\" + FIELD_SEPARATOR + "pinaster\\" + FIELD_SEPARATOR};
        String[] fields = message.split(BroadcastReceiver.SEPARATOR_REGEX);
        Assert.assertArrayEquals(expectedFields, fields);

        message = "\\" + FIELD_SEPARATOR + "\\" + FIELD_SEPARATOR + FIELD_SEPARATOR + "\\hello";
        expectedFields = new String[]{"\\" + FIELD_SEPARATOR + "\\" + FIELD_SEPARATOR, "\\hello"};
        fields = message.split(BroadcastReceiver.SEPARATOR_REGEX);
        Assert.assertArrayEquals(expectedFields, fields);

        message =
                "word" + FIELD_SEPARATOR + "\\\\" + FIELD_SEPARATOR + "word" + FIELD_SEPARATOR +
                        "\\" + FIELD_SEPARATOR + "word";
        expectedFields = new String[]{"word", "\\\\" + FIELD_SEPARATOR + "word",
                "\\" + FIELD_SEPARATOR + "word"};
        fields = message.split(BroadcastReceiver.SEPARATOR_REGEX);
        Assert.assertArrayEquals(expectedFields, fields);
    }

    /**
     * Tests whether a message not meant for our dictionary is ignored.
     */
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

    /**
     * Tests whether an empty message is ignored.
     */
    @Test
    public void onMessageReceived_emptyMessage_isIgnored() {
        BroadcastReceiver instance = new BroadcastReceiver();
        SMSPeer sender = new SMSPeer("+393492794133");
        SMSMessage garbage = new SMSMessage(sender, "");

        SMSJoinableNetManager mockManager = mock(SMSJoinableNetManager.class);
        when(mockManager.getNetSubscriberList()).thenReturn(new SMSNetSubscriberList());
        when(mockManager.getNetDictionary()).thenReturn(new SMSNetDictionary());
        PowerMockito.mockStatic(SMSJoinableNetManager.class);
        when(SMSJoinableNetManager.getInstance()).thenReturn(mockManager);

        instance.onMessageReceived(garbage);
        PowerMockito.verifyStatic(never());
        SMSJoinableNetManager.getInstance();
    }

    /**
     * Tests whether a message with {@link RequestType#Invite} containing some unneeded additional
     * characters is ignored.
     */
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

    /**
     * Tests whether a message with {@link RequestType#Invite} containing unneeded additional fields
     * is ignored.
     */
    @Test
    public void onMessageReceived_inviteWithUnneededFields_isIgnored() {
        BroadcastReceiver instance = new BroadcastReceiver();
        SMSPeer sender = new SMSPeer("+393492794133");
        String garbageText = RequestType.Invite.asString() + FIELD_SEPARATOR + ">";
        SMSMessage garbageMessage = new SMSMessage(sender, garbageText);

        SMSJoinableNetManager mockManager = mock(SMSJoinableNetManager.class);
        when(mockManager.getNetSubscriberList()).thenReturn(new SMSNetSubscriberList());
        when(mockManager.getNetDictionary()).thenReturn(new SMSNetDictionary());
        PowerMockito.mockStatic(SMSJoinableNetManager.class);
        when(SMSJoinableNetManager.getInstance()).thenReturn(mockManager);

        instance.onMessageReceived(garbageMessage);
        verify(mockManager, never()).checkInvitation(any());
    }

    /**
     * Tests whether a message with {@link RequestType#Invite} and no additional fields is correctly
     * processed.
     */
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

    /**
     * Tests whether a message with {@link RequestType#AcceptInvitation} received by a peer who
     * wasn't invited is ignored.
     */
    @Test
    public void onMessageReceived_acceptInvitationFromUninvited_isIgnored() {
        BroadcastReceiver instance = new BroadcastReceiver();
        SMSPeer sender = new SMSPeer("+393492794133");
        SMSPeer subscriber = new SMSPeer("+393332734121");
        String correctText = RequestType.AcceptInvitation.asString();
        SMSMessage correctMessage = new SMSMessage(sender, correctText);
        Set<SMSPeer> subscribersSet = new HashSet<>();
        subscribersSet.add(subscriber);
        Set<SMSPeer> invitedPeers = new HashSet<>();

        SMSManager mockSMSManager = mock(SMSManager.class);
        SMSNetSubscriberList mockSubscribers = mock(SMSNetSubscriberList.class);
        when(mockSubscribers.getSubscribers()).thenReturn(subscribersSet);
        SMSJoinableNetManager mockNetworkManager = mock(SMSJoinableNetManager.class);
        when(mockNetworkManager.getNetSubscriberList()).thenReturn(mockSubscribers);
        when(mockNetworkManager.getNetDictionary()).thenReturn(new SMSNetDictionary());
        when(mockNetworkManager.getInvitedPeers()).thenReturn(invitedPeers);
        PowerMockito.mockStatic(SMSJoinableNetManager.class);
        when(SMSJoinableNetManager.getInstance()).thenReturn(mockNetworkManager);
        PowerMockito.mockStatic(SMSManager.class);
        when(SMSManager.getInstance()).thenReturn(mockSMSManager);

        instance.onMessageReceived(correctMessage);
        verify(mockSMSManager, never()).sendMessage(any());
        verify(mockSubscribers, never()).addSubscriber(any());
    }

    /**
     * Tests whether a message with {@link RequestType#AcceptInvitation} containing unneeded
     * additional characters is ignored.
     */
    @Test
    public void onMessageReceived_acceptInvitationWithGarbage_isIgnored() {
        BroadcastReceiver instance = new BroadcastReceiver();
        SMSPeer sender = new SMSPeer("+393492794133");
        SMSPeer subscriber = new SMSPeer("+393332734121");
        String garbageText = RequestType.AcceptInvitation.asString() + "P";
        SMSMessage garbageMessage = new SMSMessage(sender, garbageText);
        Set<SMSPeer> subscribersSet = new HashSet<>();
        subscribersSet.add(subscriber);
        Set<SMSPeer> invitedPeers = new HashSet<>();
        invitedPeers.add(sender);

        SMSManager mockSMSManager = mock(SMSManager.class);
        SMSNetSubscriberList mockSubscribers = mock(SMSNetSubscriberList.class);
        when(mockSubscribers.getSubscribers()).thenReturn(subscribersSet);
        SMSJoinableNetManager mockNetworkManager = mock(SMSJoinableNetManager.class);
        when(mockNetworkManager.getNetSubscriberList()).thenReturn(mockSubscribers);
        when(mockNetworkManager.getNetDictionary()).thenReturn(new SMSNetDictionary());
        when(mockNetworkManager.getInvitedPeers()).thenReturn(invitedPeers);
        PowerMockito.mockStatic(SMSJoinableNetManager.class);
        when(SMSJoinableNetManager.getInstance()).thenReturn(mockNetworkManager);
        PowerMockito.mockStatic(SMSManager.class);
        when(SMSManager.getInstance()).thenReturn(mockSMSManager);

        instance.onMessageReceived(garbageMessage);
        verify(mockSMSManager, never()).sendMessage(any());
        verify(mockSubscribers, never()).addSubscriber(any());
    }

    /**
     * Tests whether a message with {@link RequestType#AcceptInvitation} containing unneeded
     * additional fields is ignored.
     */
    @Test
    public void onMessageReceived_acceptInvitationWithUnneededFields_isIgnored() {
        BroadcastReceiver instance = new BroadcastReceiver();
        SMSPeer sender = new SMSPeer("+393492794133");
        SMSPeer subscriber = new SMSPeer("+393332734121");
        String garbageText = RequestType.AcceptInvitation.asString() + FIELD_SEPARATOR + "P";
        SMSMessage garbageMessage = new SMSMessage(sender, garbageText);
        Set<SMSPeer> subscribersSet = new HashSet<>();
        subscribersSet.add(subscriber);
        Set<SMSPeer> invitedPeers = new HashSet<>();
        invitedPeers.add(sender);

        SMSManager mockSMSManager = mock(SMSManager.class);
        SMSNetSubscriberList mockSubscribers = mock(SMSNetSubscriberList.class);
        when(mockSubscribers.getSubscribers()).thenReturn(subscribersSet);
        SMSJoinableNetManager mockNetworkManager = mock(SMSJoinableNetManager.class);
        when(mockNetworkManager.getNetSubscriberList()).thenReturn(mockSubscribers);
        when(mockNetworkManager.getNetDictionary()).thenReturn(new SMSNetDictionary());
        when(mockNetworkManager.getInvitedPeers()).thenReturn(invitedPeers);
        PowerMockito.mockStatic(SMSJoinableNetManager.class);
        when(SMSJoinableNetManager.getInstance()).thenReturn(mockNetworkManager);
        PowerMockito.mockStatic(SMSManager.class);
        when(SMSManager.getInstance()).thenReturn(mockSMSManager);

        instance.onMessageReceived(garbageMessage);
        verify(mockSMSManager, never()).sendMessage(any());
        verify(mockSubscribers, never()).addSubscriber(any());
    }

    /**
     * Tests whether a message with {@link RequestType#AcceptInvitation} and no additional fields
     * from a peer who was invited is correctly processed.
     */
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
        String expectedMySubscribersText =
                RequestType.AddPeer.asString() + FIELD_SEPARATOR + subscriber;
        String expectedMyDictionaryText = RequestType.AddResource.asString() + FIELD_SEPARATOR +
                "Key" + FIELD_SEPARATOR + "This is a valid resource" + FIELD_SEPARATOR + "OtherKey"
                + FIELD_SEPARATOR + "This is another valid resource";
        String expectedAddPeerTextForSubscribers =
                RequestType.AddPeer.asString() + FIELD_SEPARATOR + sender;

        SMSManager mockSMSManager = mock(SMSManager.class);
        SMSNetSubscriberList mockSubscribers = mock(SMSNetSubscriberList.class);
        when(mockSubscribers.getSubscribers()).thenReturn(subscribersSet);
        SMSNetDictionary mockDictionary = mock(SMSNetDictionary.class);
        when(mockDictionary.getAllKeyResourcePairsForSMS()).thenReturn("Key" + FIELD_SEPARATOR +
                "This is a valid resource" + FIELD_SEPARATOR + "OtherKey" + FIELD_SEPARATOR +
                "This is another valid resource");
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
        Assert.assertEquals(expectedMySubscribersText,
                messageCaptor.getAllValues().get(0).getData());
        Assert.assertEquals(sender, messageCaptor.getAllValues().get(1).getPeer());
        Assert.assertEquals(expectedMyDictionaryText,
                messageCaptor.getAllValues().get(1).getData());
        Assert.assertEquals(subscriber, messageCaptor.getAllValues().get(2).getPeer());
        Assert.assertEquals(expectedAddPeerTextForSubscribers,
                messageCaptor.getAllValues().get(2).getData());
        verify(mockSubscribers, times(2)).addSubscriber(sender);
    }

    /**
     * Tests whether a message with {@link RequestType#QuitNetwork} and unneeded additional
     * characters is ignored.
     */
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

    /**
     * Tests whether a message with {@link RequestType#QuitNetwork} and unneeded additional fields
     * is ignored.
     */
    @Test
    public void onMessageReceived_quitNetworkWithUnneededFields_isIgnored() {
        BroadcastReceiver instance = new BroadcastReceiver();
        SMSPeer sender = new SMSPeer("+393492794133");
        String garbageText = RequestType.QuitNetwork.asString() + FIELD_SEPARATOR + ">";
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

    /**
     * Tests whether a message with {@link RequestType#QuitNetwork} coming from somebody who's not a
     * subscriber is ignored.
     */
    @Test
    public void onMessageReceived_quitNetworkFromNonSubscriber_isIgnored() {
        BroadcastReceiver instance = new BroadcastReceiver();
        SMSPeer sender = new SMSPeer("+393492794133");
        String garbageText = RequestType.QuitNetwork.asString() + FIELD_SEPARATOR + ">";
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
        verify(mockSubscribers, never()).removeSubscriber(any());
    }

    /**
     * Tests whether a message with {@link RequestType#QuitNetwork} and no additional fields is
     * correctly processed.
     */
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

    /**
     * Tests whether a message with {@link RequestType#AddPeer} from a peer who's not part of our
     * network is ignored.
     */
    @Test
    public void onMessageReceived_addPeerFromNonSubscriber_isIgnored() {
        BroadcastReceiver instance = new BroadcastReceiver();
        SMSPeer sender = new SMSPeer("+393492794133");
        String garbageText =
                RequestType.AddPeer.asString() + FIELD_SEPARATOR + "+393478512584" + FIELD_SEPARATOR
                        + "+393338512123";
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

    /**
     * Tests whether a message with {@link RequestType#AddPeer} containing an invalid phone number
     * is ignored.
     */
    @Test
    public void onMessageReceived_addPeerWithWrongNumber_isIgnored() {
        BroadcastReceiver instance = new BroadcastReceiver();
        SMSPeer sender = new SMSPeer("+393492794133");
        String garbageText =
                RequestType.AddPeer.asString() + FIELD_SEPARATOR + "+393478512584" + FIELD_SEPARATOR
                        + "+0000333812123";
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

    /**
     * Tests whether a message with {@link RequestType#AddPeer} containing no peers to be added is
     * ignored.
     */
    @Test
    public void onMessageReceived_addPeerWithNoPeers_isIgnored() {
        BroadcastReceiver instance = new BroadcastReceiver();
        SMSPeer sender = new SMSPeer("+393492794133");
        String garbageText =
                RequestType.AddPeer.asString() + FIELD_SEPARATOR + "" + FIELD_SEPARATOR;
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

    /**
     * Tests whether a message with {@link RequestType#AddPeer} and proper formatting is correctly
     * processed.
     */
    @Test
    public void onMessageReceived_correctAddPeer() {
        BroadcastReceiver instance = new BroadcastReceiver();
        SMSPeer sender = new SMSPeer("+393492794133");
        String correctText =
                RequestType.AddPeer.asString() + FIELD_SEPARATOR + "+393478512584" + FIELD_SEPARATOR
                        + "+39333812123";
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

    /**
     * Tests whether a message with {@link RequestType#AddResource} and containing no key-resource
     * pairs is ignored.
     */
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
    }

    /**
     * Tests whether a message with {@link RequestType#AddResource} coming from somebody who's not a
     * subscriber is ignored.
     */
    @Test
    public void onMessageReceived_addResourceFromNonSubscriber_isIgnored() {
        BroadcastReceiver instance = new BroadcastReceiver();
        SMSPeer sender = new SMSPeer("+393492794133");
        String correctText =
                RequestType.AddResource.asString() + FIELD_SEPARATOR + "the cat is on" +
                FIELD_SEPARATOR + "the table" + FIELD_SEPARATOR + "the book is" +
                FIELD_SEPARATOR + "under the table";
        SMSMessage garbageMessage = new SMSMessage(sender, correctText);
        Set<SMSPeer> subscribersSet = new HashSet<>();

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
    }

    /**
     * Tests whether a message with {@link RequestType#AddResource} and containing a key with no
     * associated resource is ignored.
     */
    @Test
    public void onMessageReceived_addResourceWithKeyButNoAssociatedResource_isIgnored() {
        BroadcastReceiver instance = new BroadcastReceiver();
        SMSPeer sender = new SMSPeer("+393492794133");
        String garbageText =
                RequestType.AddResource.asString() + FIELD_SEPARATOR + "the cat is on" +
                        FIELD_SEPARATOR + "the table" + FIELD_SEPARATOR + "the book is";
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
    }

    /**
     * Tests whether a message with {@link RequestType#AddResource} and containing an invalid
     * resource is ignored.
     */
    @Test
    public void onMessageReceived_addResourceWithInvalidResource_isIgnored() {
        BroadcastReceiver instance = new BroadcastReceiver();
        SMSPeer sender = new SMSPeer("+393492794133");
        String garbageText =
                RequestType.AddResource.asString() + FIELD_SEPARATOR + "the cat is on" +
                        FIELD_SEPARATOR + "the table" + FIELD_SEPARATOR + "the book is" +
                        FIELD_SEPARATOR + "under the table\\";
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
    }

    /**
     * Tests whether a message with {@link RequestType#AddResource} and proper formatting is
     * correctly processed.
     */
    @Test
    public void onMessageReceived_correctAddResource() {
        BroadcastReceiver instance = new BroadcastReceiver();
        SMSPeer sender = new SMSPeer("+393492794133");
        String correctText =
                RequestType.AddResource.asString() + FIELD_SEPARATOR + "the cat is on" +
                        FIELD_SEPARATOR + "the table" + FIELD_SEPARATOR + "the book is" +
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

    /**
     * Tests whether a message with {@link RequestType#RemoveResource} and no keys of the resources
     * to remove is ignored.
     */
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
    }

    /**
     * Tests whether a message with {@link RequestType#RemoveResource} from somebody who's not a
     * subscriber is ignored.
     */
    @Test
    public void onMessageReceived_removeResourceFromNonSubscriber_isIgnored() {
        BroadcastReceiver instance = new BroadcastReceiver();
        SMSPeer sender = new SMSPeer("+393492794133");
        String correctText =
                RequestType.RemoveResource.asString() + FIELD_SEPARATOR + "the cat is on" +
                FIELD_SEPARATOR + "the table";
        SMSMessage garbageMessage = new SMSMessage(sender, correctText);
        Set<SMSPeer> subscribersSet = new HashSet<>();

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
    }

    /**
     * Tests whether a message with {@link RequestType#RemoveResource} containing an invalid key is
     * ignored.
     */
    @Test
    public void onMessageReceived_removeResourceWithInvalidKey_isIgnored() {
        BroadcastReceiver instance = new BroadcastReceiver();
        SMSPeer sender = new SMSPeer("+393492794133");
        String garbageText =
                RequestType.RemoveResource.asString() + FIELD_SEPARATOR + "the cat is on" +
                        FIELD_SEPARATOR + "the table\\";
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
    }

    /**
     * Tests whether a message with {@link RequestType#RemoveResource} and proper formatting is
     * correctly processed.
     */
    @Test
    public void onMessageReceived_correctRemoveResource() {
        BroadcastReceiver instance = new BroadcastReceiver();
        SMSPeer sender = new SMSPeer("+393492794133");
        String correctText =
                RequestType.RemoveResource.asString() + FIELD_SEPARATOR + "the cat is on" +
                        FIELD_SEPARATOR + "the table";
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