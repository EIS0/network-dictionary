package com.eis.communication.network.commands;

import com.eis.communication.Peer;

/**
 * Command to quit a network, which is, removing myself from the network I'm in
 *
 * @author Edoardo Raimondi
 * @author Marco Cognolato
 * @author Giovanni Velludo
 */
public abstract class QuitNetwork<T extends Peer> extends Command {

    /**
     * Constructor for the QuitNetwork command.
     */
    protected QuitNetwork() {
    }

    /**
     * Removes myself from the subscribers list and broadcasts it to the net.
     */
    protected abstract void execute();

}
