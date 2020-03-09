package com.eis.smsnetwork;

import android.util.Log;

import androidx.annotation.NonNull;

import com.eis.communication.network.Invitation;
import com.eis.communication.network.JoinableNetworkManager;
import com.eis.communication.network.commands.CommandExecutor;
import com.eis.communication.network.listeners.JoinInvitationListener;
import com.eis.smslibrary.SMSPeer;
import com.eis.smsnetwork.smsnetcommands.SMSAcceptInvite;

/**
 * Concrete JoinableNetwork for SMS Messages. If a listener is NOT set (using {@link
 * #setJoinInvitationListener(JoinInvitationListener)}) the method {@link
 * #acceptJoinInvitation(Invitation)} will be called automatically, else you should call that from
 * the listener if you want to accept an invitation
 *
 * @author Marco Cognolato
 * @author Giovanni Velludo
 */
public class SMSJoinableNetManager extends SMSNetworkManager implements JoinableNetworkManager<
        String, String, SMSPeer, SMSFailReason, Invitation<SMSPeer>> {

    private static SMSJoinableNetManager instance;

    private JoinInvitationListener<Invitation<SMSPeer>> invitationListener = null;

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
        if (instance == null)
            instance = new SMSJoinableNetManager();
        return instance;
    }

    /**
     * Accepts a given join invitation. If a listener is NOT set (using {@link
     * #setJoinInvitationListener(JoinInvitationListener)}) this method will be called
     * automatically, else you should call this from the listener if you want to accept an
     * invitation
     *
     * @param invitation The invitation previously received.
     * @throws IllegalArgumentException If the parameter is null.
     */
    @Override
    public void acceptJoinInvitation(@NonNull Invitation invitation) {
        CommandExecutor.execute(new SMSAcceptInvite((SMSInvitation) invitation));
    }

    /**
     * Sets a listener waiting for network invitations
     *
     * @param joinInvitationListener Listener called upon invitation received. It cannot be null.
     * @throws IllegalArgumentException If the parameter is null.
     */
    @Override
    public void setJoinInvitationListener(
            @NonNull JoinInvitationListener<Invitation<SMSPeer>> joinInvitationListener) {
        //noinspection ConstantConditions
        if (joinInvitationListener == null) throw new IllegalArgumentException();
        invitationListener = joinInvitationListener;
    }

    /**
     * When an invitation in received separates between auto accepting and forwarding it to the user
     * depending if a listener was set by the user
     *
     * @param invitation The invitation received
     */
    public void checkInvitation(Invitation<SMSPeer> invitation) {
        if (invitationListener == null) {
            Log.d("JOINABLE_NET", "No listener is set, accepting automatically");
            acceptJoinInvitation(invitation);
        } else {
            Log.d("JOINABLE_NET", "Listener IS set, accepting manually");
            invitationListener.onJoinInvitationReceived(invitation);
        }
    }
}
