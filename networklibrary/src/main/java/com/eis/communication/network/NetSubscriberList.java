package com.eis.communication.network;

import com.eis.communication.Peer;

import java.util.Set;

/**
 * Interface which defines common operations for Subscribers of a network. A subscriber is defined
 * as a Peer currently connected to the network.
 *
 * @param <T> The type of subscribers, must implement the {@link Peer} interface
 * @author Marco Cognolato
 * @author Giovanni Velludo
 */
public interface NetSubscriberList<T extends Peer> {

    /**
     * Adds a subscriber to this network
     *
     * @param subscriber The subscriber to add to the net
     */
    void addSubscriber(T subscriber);

    /**
     * @return Returns the {@link Set} containing all subscribers. Adding and removing subscribers
     * to and from this {@link Set} must not change the Set of subscribers contained in instances of
     * classes implementing this interface. Only methods {@link #addSubscriber(Peer)}, {@link
     * #removeSubscriber(Peer)} and {@link #clear()} should be able to modify the set of subscribers
     * contained in instances of classes implementing this interface.
     */
    Set<T> getSubscribers();

    /**
     * Removes a given subscriber from the subscribers
     *
     * @param subscriber The subscriber to remove
     */
    void removeSubscriber(T subscriber);

    /**
     * @return {@code true} if there are no subscribers.
     */
    boolean isEmpty();

    /**
     * Removes all subscribers.
     */
    void clear();
}
