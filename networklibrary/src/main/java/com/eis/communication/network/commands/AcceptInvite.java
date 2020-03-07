package com.eis.communication.network.commands;

import androidx.annotation.NonNull;

import com.eis.communication.network.Invitation;

/**
 * Command to accept an Invitation to the network.
 *
 * @author Marco Cognolato
 * @author Giovanni Velludo
 */
public abstract class AcceptInvite<I extends Invitation> extends Command {

    protected final I invitation;

    /**
     * Constructor for AcceptInvite command, requires data to work.
     *
     * @param invitation The Invitation to a network.
     * @throws IllegalArgumentException If the parameter is null.
     */
    public AcceptInvite(@NonNull I invitation) {
        if (invitation == null) throw new IllegalArgumentException();
        this.invitation = invitation;
    }

    /**
     * Invites a peer to join a network.
     */
    protected abstract void execute();
}
