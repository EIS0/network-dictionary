package com.eis.smsnetwork.smsnetcommands;

import android.util.Log;

import androidx.annotation.NonNull;

import com.eis.communication.network.commands.Invite;
import com.eis.smslibrary.SMSManager;
import com.eis.smslibrary.SMSMessage;
import com.eis.smslibrary.SMSPeer;
import com.eis.smsnetwork.RequestType;
import com.eis.smsnetwork.SMSJoinableNetManager;

/**
 * @author Marco Cognolato
 * @author Giovanni Velludo
 */
public class SMSInvite extends Invite<SMSPeer> {

    /**
     * Constructor for the SMSInvite command, requires data to work.
     *
     * @param invitedPeer The {@link SMSPeer} to invite to the network.
     * @throws IllegalArgumentException if the parameter is null.
     */
    public SMSInvite(@NonNull SMSPeer invitedPeer) {
        super(invitedPeer);
    }

    /**
     * Execute the SMSInvite logic: sends a request to join a network.
     */
    protected void execute() {
        String message = RequestType.Invite.asString();
        SMSMessage messageToSend = new SMSMessage(invitedPeer, message);
        SMSManager.getInstance().sendMessage(messageToSend);
        Log.d("SMSINVITE_COMMAND", "Invitation Sent to: " + invitedPeer);
        SMSJoinableNetManager.getInstance().getInvitedPeers().add(invitedPeer);
    }
}
