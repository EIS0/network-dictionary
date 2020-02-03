package com.eis.smsnetwork.smsnetcommands;

import android.util.Log;

import androidx.annotation.NonNull;

import com.eis.smslibrary.SMSManager;
import com.eis.smslibrary.SMSMessage;
import com.eis.smslibrary.SMSPeer;
import com.eis.smsnetwork.RequestType;
import com.eis.smsnetwork.SMSJoinableNetManager;

/**
 * @author Marco Cognolato
 * @author Giovanni Velludo
 */
public class SMSInvitePeer extends com.eis.communication.network.commands.InvitePeer<SMSPeer> {

    SMSJoinableNetManager netManager;

    /**
     * Constructor for the SMSInvitePeer command, requires data to work
     *
     * @param peerToInvite The SMSPeer to invite to the network
     * @param netManager A valid SMSJoinableNetManager, used by the command
     */
    public SMSInvitePeer(@NonNull SMSPeer peerToInvite, @NonNull SMSJoinableNetManager netManager) {
        super(peerToInvite);
        this.netManager = netManager;
    }

    /**
     * Execute the SMSInvitePeer logic: sends a request to join a network
     */
    protected void execute() {
        String message = RequestType.Invite.asString();
        SMSMessage messageToSend = new SMSMessage(peerToInvite, message);
        SMSManager.getInstance().sendMessage(messageToSend);
        Log.d("SMSINVITE_COMMAND", "Invitation Sent to: " + peerToInvite);
        netManager.getInvitedPeers().addSubscriber(peerToInvite);
    }
}
