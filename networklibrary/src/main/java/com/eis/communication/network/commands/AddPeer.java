package com.eis.communication.network.commands;

import androidx.annotation.NonNull;

import com.eis.communication.Peer;
import com.eis.communication.network.NetSubscriberList;

/**
 * Command to add a peer to the Subscribers list
 *
 * @author Edoardo Raimondi
 * @author Marco Cognolato
 * @author Giovanni Velludo
 */
public abstract class AddPeer<T extends Peer> extends Command {

    protected final T peer;

    /**
     * AddPeer command constructor, receives the data it needs to operate on.
     *
     * @param peer           The Peer to add to the network
     * @throws IllegalArgumentException If the parameter is null
     */
    public AddPeer(@NonNull T peer) {
        if (peer == null) throw new IllegalArgumentException();
        this.peer = peer;
    }

    /**
     * Adds the peer to the subscribers list and broadcasts it to the net
     */
    protected abstract void execute();
}
