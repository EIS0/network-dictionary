package com.eis.smsnetwork.broadcast;

import android.util.Log;

import com.eis.communication.network.commands.CommandExecutor;
import com.eis.smslibrary.SMSManager;
import com.eis.smsnetwork.RequestType;
import com.eis.smsnetwork.SMSInvitation;
import com.eis.smsnetwork.SMSJoinableNetManager;
import com.eis.smslibrary.SMSMessage;
import com.eis.smslibrary.SMSPeer;
import com.eis.smslibrary.exceptions.InvalidTelephoneNumberException;
import com.eis.smslibrary.listeners.SMSReceivedServiceListener;
import com.eis.smsnetwork.SMSNetDictionary;
import com.eis.smsnetwork.SMSNetSubscriberList;
import com.eis.smsnetwork.smsnetcommands.SMSAddPeer;

import java.util.Set;

/**
 * @author Marco Cognolato, Giovanni Velludo, Enrico Cestaro
 */
public class BroadcastReceiver extends SMSReceivedServiceListener {

    private static final int REQUEST_FIELD_END_INDEX = 1;
    public static final String FIELD_SEPARATOR = "¤";
    // To allow FIELD_SEPARATOR in keys and resources, we escape it with a backslash when
    // sending those keys and resources through an SMS. Therefore we only split the message
    // when FIELD_SEPARATOR is not preceded by a backslash.
    static final String SEPARATOR_REGEX = "(?<!\\\\)" + FIELD_SEPARATOR;
    private static final String LOG_TAG = "BroadcastReceiver";


    /**
     * Receives messages from other peers in the network and acts according to the content of those
     * messages. Messages are composed of different fields, separated by
     * {@link BroadcastReceiver#FIELD_SEPARATOR}. {@link BroadcastReceiver#FIELD_SEPARATOR}s
     * preceded by a backslash do not separate fields.
     * Field 0 contains the {@link RequestType} of the request contained in this message.
     * The rest of the message varies depending on the {@link RequestType}:
     * <ul>
     * <li>Invite: there are no other fields</li>
     * <li>AcceptInvitation: there are no other fields</li>
     * <li>AddPeer: fields from 1 to the last one contain the phone numbers of each
     * {@link com.eis.smslibrary.SMSPeer} we have to add to our network</li>
     * <li>QuitNetwork: there are no other fields, because this request can only be sent by the
     * {@link com.eis.smslibrary.SMSPeer} who wants to be removed</li>
     * <li>AddResource: starting from 1, fields with odd numbers contain keys, their following
     * (even) field contains the corresponding value</li>
     * <li>RemoveResource: fields from 1 to the last one contain the keys to remove</li>
     * </ul>
     *
     * @param message The message passed by {@link com.eis.smslibrary.SMSReceivedBroadcastReceiver}.
     */
    @Override
    public void onMessageReceived(SMSMessage message) {
        Log.d(LOG_TAG, "Message received: " + message.getPeer() + " " + message.getData());
        String[] fields = message.getData().split(SEPARATOR_REGEX);
        RequestType request;
        try {
            request = RequestType.get(fields[0]);
        } catch (ArrayIndexOutOfBoundsException e) {
            Log.e(LOG_TAG, "Message has no fields");
            return;
        } catch (NullPointerException e) {
            Log.e(LOG_TAG, "Message has invalid RequestType");
            return;
        }
        if (request == null) {
            Log.e(LOG_TAG, "Message has empty field 0");
            return;
        }
        SMSPeer sender = message.getPeer();
        SMSNetSubscriberList subscribers =
                (SMSNetSubscriberList) SMSJoinableNetManager.getInstance().getNetSubscriberList();
        SMSNetDictionary dictionary =
                (SMSNetDictionary) SMSJoinableNetManager.getInstance().getNetDictionary();
        boolean senderIsNotSubscriber = !subscribers.getSubscribers().contains(sender);

        switch (request) {
            case Invite: {
                if (fields.length > 1) {
                    Log.e(LOG_TAG, "Message has " + fields.length + " fields, but there should " +
                            "have been only 1");
                    return;
                }
                SMSJoinableNetManager.getInstance().checkInvitation(new SMSInvitation(sender));
                break;
            }
            case AcceptInvitation: {
                if (fields.length > 1) {
                    Log.e(LOG_TAG, "Message has " + fields.length + " fields, but there should " +
                            "have been only 1");
                    return;
                }
                // Verifying if the sender has been invited to join the network
                Set<SMSPeer> invitedPeers = SMSJoinableNetManager.getInstance().getInvitedPeers();
                if (!invitedPeers.contains(sender)) {
                    Log.e(LOG_TAG, "AcceptInvitation received by peer who wasn't invited");
                    return;
                }
                invitedPeers.remove(sender);

                // Sending to the invited peer my subscribers list
                StringBuilder mySubscribers = new StringBuilder(RequestType.AddPeer.asString() +
                        FIELD_SEPARATOR);
                for (SMSPeer peerToAdd : subscribers.getSubscribers())
                    mySubscribers.append(peerToAdd).append(FIELD_SEPARATOR);
                SMSMessage mySubscribersMessage = new SMSMessage(
                        // we remove the last character in mySubscribers because it's a
                        // FIELD_SEPARATOR, and there's no need for it since there can't be any
                        // more fields after the last character of the message
                        sender, mySubscribers.deleteCharAt(mySubscribers.length() - 1).toString());
                SMSManager.getInstance().sendMessage(mySubscribersMessage);

                // Sending my dictionary to the invited peer
                String myDictionary = RequestType.AddResource.asString() + FIELD_SEPARATOR +
                        dictionary.getAllKeyResourcePairsForSMS();
                SMSMessage myDictionaryMessage = new SMSMessage(sender, myDictionary);
                SMSManager.getInstance().sendMessage(myDictionaryMessage);

                // Broadcasting to the previous subscribers the new subscriber
                CommandExecutor.execute(new SMSAddPeer(sender, subscribers));
                // Updating my local subscribers list
                subscribers.addSubscriber(sender);
            }
            case AddPeer: {
                if (senderIsNotSubscriber) {
                    Log.e(LOG_TAG, "AddPeer received by peer who's not part of our network");
                    return;
                }
                SMSPeer[] peersToAdd;
                try {
                    peersToAdd = new SMSPeer[fields.length - REQUEST_FIELD_END_INDEX];
                } catch (NegativeArraySizeException e) {
                    Log.e(LOG_TAG, "RequestType is AddPeer, but the message doesn't contain any " +
                            "peers to be added");
                    return;
                }
                try {
                    for (int i = REQUEST_FIELD_END_INDEX; i < fields.length; i++)
                        peersToAdd[i - REQUEST_FIELD_END_INDEX] = new SMSPeer(fields[i]);
                } catch (InvalidTelephoneNumberException e) {
                    Log.e(LOG_TAG, "Peers to be added have an invalid phone number");
                    return;
                } catch (ArrayIndexOutOfBoundsException e) {
                    Log.e(LOG_TAG, "RequestType is AddPeer, but the message doesn't contain any " +
                            "peers to be added");
                    return;
                }
                for (SMSPeer peer : peersToAdd)
                    subscribers.addSubscriber(peer);
                break;
            }
            case QuitNetwork: {
                if (fields.length > 1) {
                    Log.e(LOG_TAG, "Message has " + fields.length + " fields, there should have " +
                            "been only 1");
                    return;
                }
                try {
                    subscribers.removeSubscriber(sender);
                } catch (IllegalArgumentException e) {
                    Log.e(LOG_TAG, "The subscriber asking to be removed from the network is not " +
                            "part of the network");
                    return;
                }
                break;
            }
            case AddResource: {
                if (senderIsNotSubscriber) {
                    Log.e(LOG_TAG, "AddResource received by peer who's not part of our network");
                    return;
                }
                // if the number of fields is even, that means not every key will have a
                // corresponding value, so the message we received is garbage. For example, with 4
                // fields we'll have: requestType, key, value, key
                if (fields.length % 2 == 0) {
                    Log.e(LOG_TAG, "AddResource message contains a key with no corresponding " +
                            "resource");
                    return;
                }
                // the last field is the only one which can possibly contain a backslash as its last
                // character, if it does then the message we received is garbage because keys and
                // resources cannot have a backslash as their last character
                String lastField = fields[fields.length - 1];
                if (lastField.charAt(lastField.length() - 1) == '\\') {
                    Log.e(LOG_TAG, "AddResource message contains an invalid resource");
                    return;
                }
                String[] keys;
                String[] values;
                try {
                    keys = new String[(fields.length - REQUEST_FIELD_END_INDEX) / 2];
                    values = new String[keys.length];
                } catch (NegativeArraySizeException e) {
                    Log.e(LOG_TAG, "RequestType is AddResource, but the message doesn't contain " +
                            "any keys nor resources");
                    return;
                }
                try {
                    for (int i = 0, j = REQUEST_FIELD_END_INDEX; j < fields.length; i++) {
                        keys[i] = fields[j++];
                        values[i] = fields[j++];
                    }
                } catch (ArrayIndexOutOfBoundsException e) {
                    Log.e(LOG_TAG, "RequestType is AddResource, but the message doesn't contain " +
                            "any keys nor resources");
                    return;
                }
                for (int i = 0; i < keys.length; i++) {
                    dictionary.addResourceFromSMS(keys[i], values[i]);
                }
                break;
            }
            case RemoveResource: {
                if (senderIsNotSubscriber) {
                    Log.e(LOG_TAG, "RemoveResource received by peer who's not part of our network");
                    return;
                }
                // the last field is the only one which can possibly contain a backslash as its last
                // character, if it does then the message we received is garbage because keys and
                // resources cannot have a backslash as their last character
                String lastField = fields[fields.length - 1];
                if (lastField.charAt(lastField.length() - 1) == '\\') {
                    Log.e(LOG_TAG, "RemoveResource message contains an invalid resource");
                    return;
                }
                try {
                    for (int i = REQUEST_FIELD_END_INDEX; i < fields.length; i++)
                        dictionary.removeResourceFromSMS(fields[i]);
                } catch (ArrayIndexOutOfBoundsException e) {
                    Log.e(LOG_TAG, "RequestType is RemoveResource, but the message doesn't " +
                            "contain any keys of resources to be removed");
                    return;
                }
                break;
            }
        }
    }
}
