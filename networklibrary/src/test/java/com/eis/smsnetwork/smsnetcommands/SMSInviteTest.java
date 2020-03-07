package com.eis.smsnetwork.smsnetcommands;

import android.util.Log;

import com.eis.communication.network.commands.CommandExecutor;
import com.eis.smslibrary.SMSManager;
import com.eis.smslibrary.SMSMessage;
import com.eis.smslibrary.SMSPeer;
import com.eis.smsnetwork.RequestType;
import com.eis.smsnetwork.SMSJoinableNetManager;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import static org.junit.Assert.*;
import static org.mockito.Matchers.anyString;
import static org.powermock.api.mockito.PowerMockito.when;

@RunWith(PowerMockRunner.class)
@PrepareForTest({SMSManager.class, Log.class})
public class SMSInviteTest {

    @Captor
    ArgumentCaptor<SMSMessage> messageCaptor;

    @Before
    public void setUp() {
        PowerMockito.mockStatic(Log.class);
        when(Log.d(anyString(), anyString())).thenReturn(0);
    }

    @Test
    public void invitePeer() {
        SMSJoinableNetManager netManager = SMSJoinableNetManager.getInstance();
        String expectedMessage = RequestType.Invite.asString();
        SMSPeer invitedPeer = new SMSPeer("+393475349954");
        SMSManager mockManager = Mockito.mock(SMSManager.class);
        PowerMockito.mockStatic(SMSManager.class);
        when(SMSManager.getInstance()).thenReturn(mockManager);

        CommandExecutor.execute(new SMSInvite(invitedPeer));

        assertTrue(netManager.getInvitedPeers().contains(invitedPeer));
        Mockito.verify(mockManager).sendMessage(messageCaptor.capture());
        assertEquals(invitedPeer, messageCaptor.getValue().getPeer());
        assertEquals(expectedMessage, messageCaptor.getValue().getData());
    }

    @Test(expected = IllegalArgumentException.class)
    public void inviteNullPeer() {
        CommandExecutor.execute(new SMSInvite(null));
    }
}