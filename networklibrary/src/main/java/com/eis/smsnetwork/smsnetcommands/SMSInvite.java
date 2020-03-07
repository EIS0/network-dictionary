package com.eis.smsnetwork.smsnetcommands;

import android.util.Log;

import androidx.annotation.NonNull;

import com.eis.communication.network.commands.Invite;
import com.eis.smslibrary.SMSManager;
import com.eis.smslibrary.SMSMessage;
import com.eis.smslibrary.SMSPeer;
import com.eis.smsnetwork.RequestType;
import com.eis.smsnetwork.SMSNetworkManager;

/**
 * @author Marco Cognolato
 * @author Giovanni Velludo
 */
public class SMSInvite extends Invite<SMSPeer> {

    private SMSNetworkManager netManager;

    /**
     * Constructor for the SMSInvite command, requires data to work.
     *
     * @param invitedPeer The {@link SMSPeer} to invite to the network.
     * @param netManager A valid SMSJoinableNetManager, used by the command.
     */
    public SMSInvite(@NonNull SMSPeer invitedPeer, @NonNull SMSNetworkManager netManager) {
        super(invitedPeer);
        this.netManager = netManager;
    }

    /**
     * Execute the SMSInvite logic: sends a request to join a network.
     */
    protected void execute() {
        String message = RequestType.Invite.asString();
        SMSMessage messageToSend = new SMSMessage(invitedPeer, message);
        SMSManager.getInstance().sendMessage(messageToSend);
        Log.d("SMSINVITE_COMMAND", "Invitation Sent to: " + invitedPeer);
        netManager.getInvitedPeers().add(invitedPeer);
    }
}
