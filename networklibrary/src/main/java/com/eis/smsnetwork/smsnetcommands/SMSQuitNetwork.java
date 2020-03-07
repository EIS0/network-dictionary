package com.eis.smsnetwork.smsnetcommands;

import androidx.annotation.NonNull;

import com.eis.communication.network.NetSubscriberList;
import com.eis.communication.network.commands.QuitNetwork;
import com.eis.smslibrary.SMSPeer;
import com.eis.smsnetwork.RequestType;
import com.eis.smsnetwork.SMSJoinableNetManager;
import com.eis.smsnetwork.broadcast.BroadcastSender;

/**
 * Command to quit the current network.
 *
 * @author Edoardo Raimondi
 * @author Marco Cognolato
 * @author Giovanni Velludo
 */
public class SMSQuitNetwork extends QuitNetwork<SMSPeer> {

    /**
     * Constructor for the SMSQuitNetwork command.
     */
    SMSQuitNetwork() {
        super();
    }

    /**
     * Removes myself from the subscribers list and broadcasts it to the net.
     */
    protected void execute() {
        String quitNetworkMessage = RequestType.QuitNetwork.asString();
        BroadcastSender.broadcastMessage(SMSJoinableNetManager.getInstance().getNetSubscriberList()
                .getSubscribers(), quitNetworkMessage);
        SMSJoinableNetManager.getInstance().clear();
    }
}
