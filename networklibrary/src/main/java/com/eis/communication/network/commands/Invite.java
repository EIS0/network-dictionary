package com.eis.communication.network.commands;

import androidx.annotation.NonNull;

import com.eis.communication.Peer;

/**
 * Command to invite a {@link Peer} to join the network.
 *
 * @author Marco Cognolato
 * @author Giovanni Velludo
 */
public abstract class Invite<P extends Peer> extends Command {

    protected P invitedPeer;

    /**
     * Constructor for the Invite command, requires data to work.
     *
     * @param invitedPeer The {@link Peer} to invite to the network.
     */
    public Invite(@NonNull P invitedPeer) {
        this.invitedPeer = invitedPeer;
    }

    /**
     * Execute the Invite logic: sends a request to join a network.
     */
    protected abstract void execute();
}
