package com.eis.communication.network.commands;

import androidx.annotation.NonNull;

/**
 * Command to add a resource to the net dictionary
 *
 * @author Edoardo Raimondi
 * @author Marco Cognolato
 * @author Giovanni Velludo
 */
public abstract class AddResource<K, R> extends Command {

    protected final K key;
    protected final R value;

    /**
     * Constructor for the AddResource command, needs the data to operate
     *
     * @param key           The key of the resource to add
     * @param value         The value of the resource to add
     */
    protected AddResource(@NonNull K key, @NonNull R value) {
        this.key = key;
        this.value = value;
    }

    /**
     * Adds the key-resource pair to the dictionary, then broadcasts the message
     */
    protected abstract void execute();
}
