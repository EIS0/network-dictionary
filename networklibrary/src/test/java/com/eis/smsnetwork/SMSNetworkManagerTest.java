package com.eis.smsnetwork;

import android.util.Log;

import com.eis.communication.network.listeners.GetResourceListener;
import com.eis.communication.network.listeners.InviteListener;
import com.eis.communication.network.listeners.RemoveResourceListener;
import com.eis.communication.network.listeners.SetResourceListener;
import com.eis.smslibrary.SMSManager;
import com.eis.smslibrary.SMSPeer;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import static org.junit.Assert.*;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.powermock.api.mockito.PowerMockito.mockStatic;
import static org.powermock.api.mockito.PowerMockito.when;

@RunWith(PowerMockRunner.class)
@PrepareForTest({SMSManager.class, Log.class})
public class SMSNetworkManagerTest {

    private SMSJoinableNetManager networkManager;
    private SMSNetDictionary localDictionary;

    private final String KEY1 = "Key1";
    private final String RES1 = "Res1";
    private final String RES2 = "Res2";
    private final SMSPeer VALID_PEER = new SMSPeer("+393479281192");

    @Mock
    private GetResourceListener<String, String, SMSFailReason> getListenerMock;
    @Mock
    private SetResourceListener<String, String, SMSFailReason> setListenerMock;
    @Mock
    private RemoveResourceListener<String, SMSFailReason> removeListenerMock;
    @Mock
    private InviteListener<SMSPeer, SMSFailReason> inviteListenerMock;

    @Before
    public void setup() {
        PowerMockito.mockStatic(Log.class);
        when(Log.d(anyString(), anyString())).thenReturn(0);
        when(Log.e(anyString(), anyString())).thenReturn(0);
        networkManager = SMSJoinableNetManager.getInstance();
        networkManager.clear();
        localDictionary = (SMSNetDictionary) networkManager.getNetDictionary();
    }

    @Test
    public void getNetSubscribers() {
        assertEquals(networkManager.getNetSubscriberList(), networkManager.getNetSubscriberList());
    }

    @Test
    public void getNetDictionary() {
        assertEquals(networkManager.getNetDictionary(), networkManager.getNetDictionary());
    }

    @Test
    public void getResource_available() {
        localDictionary.addResource(KEY1, RES1);

        networkManager.getResource(KEY1, getListenerMock);
        verify(getListenerMock, times(1)).onGetResource(KEY1, RES1);
    }

    @Test
    public void getResource_notAvailable() {
        networkManager.getResource(KEY1, getListenerMock);
        verify(getListenerMock).onGetResourceFailed(KEY1, SMSFailReason.NO_RESOURCE);
    }

    @Test
    public void setResource_available() {
        localDictionary.addResource(KEY1, RES1);
        networkManager.setResource(KEY1, RES2, setListenerMock);
        verify(setListenerMock).onResourceSet(KEY1, RES2);
    }

    @Test
    /*
     * Tested the modification of a given <key, resource> pair; the value is modified even if it has
     * never been added to the Dictionary, but there is no error since, if the pair is not found, is
     * added to the Dictionary as a new pair.
     * It's basically an addResource()
     */
    public void setResource_notAvailable() {
        networkManager.setResource(KEY1, RES2, setListenerMock);
        verify(setListenerMock, times(0)).onResourceSetFail(KEY1, RES2, SMSFailReason.MESSAGE_SEND_ERROR);
    }

    @Test
    public void removeResource_available() {
        localDictionary.addResource(KEY1, RES1);
        networkManager.removeResource(KEY1, removeListenerMock);
        verify(removeListenerMock).onResourceRemoved(KEY1);
    }

    @Test
    /*
     * The system is not able to send the message
     */
    public void invite_failed() {
        networkManager.invite(VALID_PEER, inviteListenerMock);
        verify(inviteListenerMock).onInvitationNotSent(VALID_PEER, SMSFailReason.MESSAGE_SEND_ERROR);
    }

    @Test
    public void invite_succeeded() {
        SMSManager mockManager = mock(SMSManager.class);
        mockStatic(SMSManager.class);
        when(SMSManager.getInstance()).thenReturn(mockManager);
        networkManager.invite(VALID_PEER, inviteListenerMock);
        verify(inviteListenerMock).onInvitationSent(VALID_PEER);
    }

    @Test
    public void acceptJoinInvitation() {
        SMSManager mockManager = mock(SMSManager.class);
        mockStatic(SMSManager.class);
        when(SMSManager.getInstance()).thenReturn(mockManager);
        SMSInvitation invitation = new SMSInvitation(VALID_PEER);
        networkManager.acceptJoinInvitation(invitation);
    }

}