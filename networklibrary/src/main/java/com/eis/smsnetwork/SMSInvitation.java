package com.eis.smsnetwork;

import androidx.annotation.NonNull;

import com.eis.communication.network.Invitation;
import com.eis.smslibrary.SMSPeer;

/**
 * Represents a received invitation to join a network.
 */
public class SMSInvitation implements Invitation<SMSPeer> {
    private SMSPeer inviter;

    /**
     * @param inviter The {@link SMSPeer} who sent the invitation.
     * @throws IllegalArgumentException If the parameter is null.
     */
    public SMSInvitation(@NonNull SMSPeer inviter) {
        //noinspection ConstantConditions
        if (inviter == null) throw new IllegalArgumentException();
        this.inviter = inviter;
    }

    /**
     * Returns the {@link SMSPeer} who sent the Invitation to join a new network
     */
    @Override
    public SMSPeer getInviterPeer() {
        return inviter;
    }
}
