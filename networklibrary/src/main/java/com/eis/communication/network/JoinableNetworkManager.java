package com.eis.communication.network;

import androidx.annotation.NonNull;

import com.eis.communication.Peer;
import com.eis.communication.network.listeners.JoinInvitationListener;

/**
 * Manager for networks where the join has to be done manually and is not performed automatically.
 *
 * @param <I> The type of invitation used by the network.
 * @author Luca Crema, suggested by Enrico Cestaro
 */
public interface JoinableNetworkManager<RK, RV, P extends Peer, FR extends FailReason, I extends Invitation<P>> extends NetworkManager<RK, RV, P, FR> {

    /**
     * Method used to join the network after an invitation in received.
     *
     * @param invitation The invitation previously received.
     * @throws IllegalArgumentException If the parameter is null.
     */
    void acceptJoinInvitation(@NonNull I invitation);

    /**
     * Sets the listener used to wait for invitations to join the network.
     *
     * @param joinInvitationListener Listener called upon invitation received.
     */
    void setJoinInvitationListener(JoinInvitationListener<I> joinInvitationListener);

}
