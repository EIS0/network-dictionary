package com.eis.smsnetwork.smsnetcommands;

import android.util.Log;

import com.eis.communication.network.commands.CommandExecutor;
import com.eis.smslibrary.SMSManager;
import com.eis.smslibrary.SMSMessage;
import com.eis.smslibrary.SMSPeer;
import com.eis.smsnetwork.RequestType;
import com.eis.smsnetwork.SMSInvitation;
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
@PrepareForTest({Log.class, SMSManager.class})
public class SMSAcceptInviteTest {

    @Captor
    ArgumentCaptor<SMSMessage> messageCaptor;

    @Before
    public void setUp() {
        PowerMockito.mockStatic(Log.class);
        when(Log.d(anyString(), anyString())).thenReturn(0);
    }

    @Test
    public void acceptInvite() {
        SMSPeer inviter = new SMSPeer("+393471488293");
        SMSJoinableNetManager netManager = SMSJoinableNetManager.getInstance();
        SMSInvitation invitation = new SMSInvitation(inviter);
        SMSManager mockManager = Mockito.mock(SMSManager.class);
        PowerMockito.mockStatic(SMSManager.class);
        PowerMockito.when(SMSManager.getInstance()).thenReturn(mockManager);

        CommandExecutor.execute(new SMSAcceptInvite(invitation));

        //TODO: verify that QuitNetwork is called (if possible)
        assertTrue(netManager.getNetSubscriberList().getSubscribers().contains(inviter));
        Mockito.verify(mockManager).sendMessage(messageCaptor.capture());
        assertEquals(inviter, messageCaptor.getValue().getPeer());
        assertEquals(RequestType.AcceptInvitation.asString(), messageCaptor.getValue().getData());
    }

    @Test(expected = IllegalArgumentException.class)
    public void nullInvitation() {
        CommandExecutor.execute(new SMSAcceptInvite(null));
    }

    @Test(expected = IllegalArgumentException.class)
    public void nullInviter() {
        SMSInvitation invitation = new SMSInvitation(null);
        CommandExecutor.execute(new SMSAcceptInvite(invitation));
    }
}