package com.eis.smsnetwork.smsnetcommands;

import android.util.Log;

import androidx.annotation.NonNull;

import com.eis.communication.network.commands.CommandExecutor;
import com.eis.smslibrary.SMSManager;
import com.eis.smslibrary.SMSMessage;
import com.eis.smslibrary.SMSPeer;
import com.eis.smsnetwork.RequestType;
import com.eis.smsnetwork.SMSInvitation;
import com.eis.smsnetwork.SMSJoinableNetManager;

/**
 * @author Marco Cognolato
 * @author Giovanni Velludo
 */
public class SMSAcceptInvite extends com.eis.communication.network.commands.AcceptInvite<SMSInvitation> {

    /**
     * Constructor for SMSAcceptInvite command, requires data to work.
     *
     * @param invitation The SMSInvitation to a network.
     * @throws IllegalArgumentException If the parameter is null.
     */
    public SMSAcceptInvite(@NonNull SMSInvitation invitation) {
        super(invitation);
    }

    /**
     * Quits the current network (if we're part of one), adds the inviter to our network and
     * notifies them of the fact that we accepted their invitation.
     */
    protected void execute() {
        SMSPeer inviter = invitation.getInviterPeer();
        SMSJoinableNetManager netManager = SMSJoinableNetManager.getInstance();
        CommandExecutor.execute(new SMSQuitNetwork());
        netManager.getNetSubscriberList().addSubscriber(inviter);
        if (inviter.getInvalidityReason() != null) return;
        SMSManager.getInstance().sendMessage(new SMSMessage(inviter, RequestType.AcceptInvitation.asString()));
        Log.d("ACCEPTINVITE_COMMAND", "Accepting invite from: " + inviter);
    }
}
