package com.eis.smsnetwork;

import com.eis.communication.network.FailReason;
import com.eis.communication.network.Invitation;
import com.eis.communication.network.JoinableNetworkManager;
import com.eis.communication.network.listeners.JoinInvitationListener;
import com.eis.smslibrary.SMSPeer;

/**
 * Concrete JoinableNetwork for SMS Messages
 * If a listener is NOT set (using {@link #setJoinInvitationListener(JoinInvitationListener)})
 * the method {@link #acceptJoinInvitation(Invitation)} will be called automatically,
 * else you should call that from the listener if you want to accept an invitation
 *
 * @author Marco Cognolato, Giovanni Velludo
 */
public class SMSJoinableNetManager extends SMSNetworkManager
        implements JoinableNetworkManager<String, String, SMSPeer, FailReason, Invitation<SMSPeer>> {

    private static SMSJoinableNetManager instance;

    /**
     * Private constructor of the singleton.
     */
    private SMSJoinableNetManager() {
    }

    /**
     * Gets the only instance of this class.
     *
     * @return the only instance of SMSNetworkManager.
     */
    public static SMSJoinableNetManager getInstance() {
        return instance;
    }

    /**
     * Accepts a given join invitation.
     * If a listener is NOT set (using {@link #setJoinInvitationListener(JoinInvitationListener)})
     * this method will be called automatically, else you should call this from the listener
     * if you want to accept an invitation
     *
     * @param invitation The invitation previously received.
     */
    @Override
    public void acceptJoinInvitation(Invitation invitation) {
        //redirects the call to the acceptJoinInvitation in the parent class.
        super.acceptJoinInvitation(invitation);
    }

    /**
     * Sets a listener waiting for network invitations
     *
     * @param joinInvitationListener Listener called upon invitation received.
     */
    @Override
    public void setJoinInvitationListener(JoinInvitationListener<Invitation<SMSPeer>> joinInvitationListener) {

    }
}
