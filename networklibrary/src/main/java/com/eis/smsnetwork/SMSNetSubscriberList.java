package com.eis.smsnetwork;

import androidx.annotation.NonNull;

import com.eis.communication.network.NetSubscriberList;
import com.eis.smslibrary.SMSPeer;

import java.util.HashSet;
import java.util.Set;

/**
 * Concrete implementation of a {@link NetSubscriberList} interface
 *
 * @author Marco Cognolato
 * @author Giovanni Velludo
 */
public class SMSNetSubscriberList implements NetSubscriberList<SMSPeer> {

    private final Set<SMSPeer> subscribers = new HashSet<>();

    /**
     * Adds a subscriber to this network
     *
     * @param subscriber The subscriber to add to the net
     * @throws IllegalArgumentException If subscriber is null
     */
    public void addSubscriber(@NonNull final SMSPeer subscriber) {
        //noinspection ConstantConditions
        if (subscriber == null)
            throw new IllegalArgumentException("Cannot add a null peer!");
        subscribers.add(subscriber);
    }

    /**
     * @return Returns the {@link Set} containing all subscribers. Adding and removing subscribers
     * to and from this {@link Set} won't change the Set of subscribers contained in instances of
     * this class. In order to do that you must call methods {@link #addSubscriber(SMSPeer)} and
     * {@link #removeSubscriber(SMSPeer)}.
     */
    public Set<SMSPeer> getSubscribers() {
        return new HashSet<>(subscribers);
    }

    /**
     * Removes a given subscriber from the subscribers
     *
     * @param subscriber The subscriber to remove
     * @throws IllegalArgumentException If a non present subscriber is removed
     */
    public void removeSubscriber(@NonNull final SMSPeer subscriber) {
        if (!subscribers.contains(subscriber))
            throw new IllegalArgumentException("The subscriber you're trying to remove is not " +
                    "present!");
        subscribers.remove(subscriber);
    }

    /**
     * @return {@code true} if there are no subscribers.
     */
    public boolean isEmpty() {
        return subscribers.isEmpty();
    }

    /**
     * Removes all subscribers.
     */
    public void clear() {
        subscribers.clear();
    }
}
