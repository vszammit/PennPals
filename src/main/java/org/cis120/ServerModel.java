package org.cis120;

import java.util.*;

/*
 * Make sure to write your own tests in ServerModelTest.java.
 * The tests we provide for each task are NOT comprehensive!
 */

/**
 * The {@code ServerModel} is the class responsible for tracking the
 * state of the server, including its current users and the channels
 * they are in.
 * This class is used by subclasses of {@link Command} to:
 * 1. handle commands from clients, and
 * 2. handle commands from {@link ServerBackend} to coordinate
 * client connection/disconnection.
 */

public final class ServerModel {

    /**
     * Constructs a {@code ServerModel}. Make sure to initialize any collections
     * used to model the server state here.
     */

    private Map<Integer, String> registeredClients;
    private Map<String, Channel> channels;

    public ServerModel() {
        registeredClients = new TreeMap<>();
        channels = new TreeMap<>();

    }

    // =========================================================================
    // == Task 2: Basic Server model queries
    // == These functions provide helpful ways to test the state of your model.
    // == You may also use them in later tasks.
    // =========================================================================

    /**
     * Gets the user ID currently associated with the given
     * nickname. The returned ID is -1 if the nickname is not
     * currently in use.
     *
     * @param nickname The nickname for which to get the associated user ID
     * @return The user ID of the user with the argued nickname if
     *         such a user exists, otherwise -1
     */
    public int getUserId(String nickname) {
        if (registeredClients.containsValue(nickname)) {
            Set<Map.Entry<Integer, String>> mapEntry = registeredClients.entrySet();
            for (Map.Entry<Integer, String> user : mapEntry) {
                if (user.getValue().equals(nickname)) {
                    return user.getKey();
                }
            }
        }
        return -1;
    }


    /**
     * Gets the nickname currently associated with the given user
     * ID. The returned nickname is null if the user ID is not
     * currently in use.
     *
     * @param userId The user ID for which to get the associated
     *               nickname
     * @return The nickname of the user with the argued user ID if
     *         such a user exists, otherwise null
     */
    public String getNickname(int userId) {
        for (Map.Entry<Integer, String> mapEntry: registeredClients.entrySet()) {
            if (userId == mapEntry.getKey()) {
                return mapEntry.getValue();
            }
        }
        return null;
    }

    /**
     * Gets a collection of the nicknames of all users who are
     * registered with the server. Changes to the returned collection
     * should not affect the server state.
     * 
     * This method is provided for testing.
     *
     * @return The collection of registered user nicknames
     */
    public Collection<String> getRegisteredUsers() {
        Collection<String> registeredUser = new TreeSet<>(registeredClients.values());
        return registeredUser;
    }

    /**
     * Gets a collection of the names of all the channels that are
     * present on the server. Changes to the returned collection
     * should not affect the server state.
     * 
     * This method is provided for testing.
     *
     * @return The collection of channel names
     */
    public Collection<String> getChannels() {
        Collection<String> channel = new TreeSet<>(channels.keySet());
        return channel;
    }

    /**
     * Gets a collection of the nicknames of all the users in a given
     * channel. The collection is empty if no channel with the given
     * name exists. Modifications to the returned collection should
     * not affect the server state.
     *
     * This method is provided for testing.
     *
     * @param channelName The channel for which to get member nicknames
     * @return A collection of all user nicknames in the channel
     */
    public Collection<String> getUsersInChannel(String channelName) {
        if (channels.containsKey(channelName)) {
            TreeSet<String> usersInChannel = channels.get(channelName).getUsers();
            TreeSet<String> copy = (TreeSet<String>) usersInChannel.clone();
            return copy;
        }
        return new TreeSet<>();
    }

    /**
     * Gets the nickname of the owner of the given channel. The result
     * is {@code null} if no channel with the given name exists.
     *
     * This method is provided for testing.
     *
     * @param channelName The channel for which to get the owner nickname
     * @return The nickname of the channel owner if such a channel
     *         exists; otherwise, return null
     */
    public String getOwner(String channelName) {
        if (channels.containsKey(channelName)) {
            return channels.get(channelName).getOwner();
        }
        return null;
    }

    // ===============================================
    // == Task 3: Connections and Setting Nicknames ==
    // ===============================================

    /**
     * This method is automatically called by the backend when a new client
     * connects to the server. It should generate a default nickname with
     * {@link #generateUniqueNickname()}, store the new user's ID and username
     * in your data structures for {@link ServerModel} state, and construct
     * and return a {@link Broadcast} object using
     * {@link Broadcast#connected(String)}}.
     *
     * @param userId The new user's unique ID (automatically created by the
     *               backend)
     * @return The {@link Broadcast} object generated by calling
     *         {@link Broadcast#connected(String)} with the proper parameter
     */
    public Broadcast registerUser(int userId) {
        String nickname = generateUniqueNickname();
        registeredClients.put(userId, nickname);
        // We have taken care of generating the nickname and returning
        // the Broadcast for you. You need to modify this method to
        // store the new user's ID and username in this model's internal state.
        return Broadcast.connected(nickname);
    }

    /**
     * Helper for {@link #registerUser(int)}. (Nothing to do here.)
     *
     * Generates a unique nickname of the form "UserX", where X is the
     * smallest non-negative integer that yields a unique nickname for a user.
     * 
     * @return The generated nickname
     */
    private String generateUniqueNickname() {
        int suffix = 0;
        String nickname;
        Collection<String> existingUsers = getRegisteredUsers();
        do {
            nickname = "User" + suffix++;
        } while (existingUsers.contains(nickname));
        return nickname;
    }

    /**
     * This method is automatically called by the backend when a client
     * disconnects from the server. This method should take the following
     * actions, not necessarily in this order:
     *
     * (1) All users who shared a channel with the disconnected user should be
     * notified that they left
     * (2) All channels owned by the disconnected user should be deleted
     * (3) The disconnected user's information should be removed from
     * {@link ServerModel}'s internal state
     * (4) Construct and return a {@link Broadcast} object using
     * {@link Broadcast#disconnected(String, Collection)}.
     *
     * @param userId The unique ID of the user to deregister
     * @return The {@link Broadcast} object generated by calling
     *         {@link Broadcast#disconnected(String, Collection)} with the proper
     *         parameters
     */
    public Broadcast deregisterUser(int userId) {
        String nickname = getNickname(userId);
        Collection<String> current = new TreeSet<>();

        for (Map.Entry<String, Channel> mapEntry: channels.entrySet()) {
            Channel chan = mapEntry.getValue();
            if (chan.getUsers().contains(nickname)) {
                current.addAll(getUsersInChannel(mapEntry.getKey()));
                if (chan.getOwner().equals(nickname)) {
                    chan.removeAllUsers();
                    channels.remove(mapEntry.getKey());
                } else {
                    mapEntry.getValue().removeUser(nickname);
                }
            }
        }
        current.remove(nickname);
        registeredClients.remove(userId);
        return Broadcast.disconnected(nickname, current);
    }

    public Collection<String> getCommon(String nickname) {
        Collection<String> current = new TreeSet<>();
        for (Map.Entry<String, Channel> mapEntry: channels.entrySet()) {
            if (mapEntry.getValue().getUsers().contains(nickname)) {
                current.addAll(mapEntry.getValue().getUsers());
            }
        }
        return current;
    }


    /**
     * This method is called when a user wants to change their nickname.
     * 
     * @param nickCommand The {@link NicknameCommand} object containing
     *                    all information needed to attempt a nickname change
     * @return The {@link Broadcast} object generated by
     *         {@link Broadcast#okay(Command, Collection)} if the nickname
     *         change is successful. The command should be the original nickCommand
     *         and the collection of recipients should be any clients who
     *         share at least one channel with the sender, including the sender.
     *
     *         If an error occurs, use
     *         {@link Broadcast#error(Command, ServerResponse)} with either:
     *         (1) {@link ServerResponse#INVALID_NAME} if the proposed nickname
     *         is not valid according to
     *         {@link ServerModel#isValidName(String)}
     *         (2) {@link ServerResponse#NAME_ALREADY_IN_USE} if there is
     *         already a user with the proposed nickname
     */
    public Broadcast changeNickname(NicknameCommand nickCommand) {
        String oldName = nickCommand.getSender();
        String newName = nickCommand.getNewNickname();
        int id = getUserId(nickCommand.getSender());

        if (isValidName(newName)) {
            if (registeredClients.containsKey(newName)) {
                return Broadcast.error(nickCommand, ServerResponse.NAME_ALREADY_IN_USE);
            }
            for (Channel userChan : channels.values()) {
                if (userChan.getUsers().contains(oldName)) {
                    userChan.removeUser(oldName);
                    userChan.addUser(newName);
                    String owner = userChan.getOwner();
                    if (owner.equals(oldName)) {
                        userChan.changeOwnerName(newName);
                    }
                }
            }
            Collection<String> current = getCommon(newName);
            registeredClients.put(id, newName);
            return Broadcast.okay(nickCommand, current);
        } else {
            return Broadcast.error(nickCommand, ServerResponse.INVALID_NAME);
        }
    }

    /**
     * Determines if a given nickname is valid or invalid (contains at least
     * one alphanumeric character, and no non-alphanumeric characters).
     * (Nothing to do here.)
     * 
     * @param name The channel or nickname string to validate
     * @return true if the string is a valid name
     */
    public static boolean isValidName(String name) {
        if (name == null || name.isEmpty()) {
            return false;
        }
        for (char c : name.toCharArray()) {
            if (!Character.isLetterOrDigit(c)) {
                return false;
            }
        }
        return true;
    }

    // ===================================
    // == Task 4: Channels and Messages ==
    // ===================================

    /**
     * This method is called when a user wants to create a channel.
     * You can ignore the privacy aspect of this method for task 4, but
     * make sure you come back and implement it in task 5.
     * 
     * @param createCommand The {@link CreateCommand} object containing all
     *                      information needed to attempt channel creation
     * @return The {@link Broadcast} object generated by
     *         {@link Broadcast#okay(Command, Collection)} if the channel
     *         creation is successful. The only recipient should be the new
     *         channel's owner.
     *
     *         If an error occurs, use
     *         {@link Broadcast#error(Command, ServerResponse)} with either:
     *         (1) {@link ServerResponse#INVALID_NAME} if the proposed
     *         channel name is not valid according to
     *         {@link ServerModel#isValidName(String)}
     *         (2) {@link ServerResponse#CHANNEL_ALREADY_EXISTS} if there is
     *         already a channel with the proposed name
     */
    public Broadcast createChannel(CreateCommand createCommand) {
        String ownerName = createCommand.getSender();
        String channelName = createCommand.getChannel();
        boolean privacyChannel = createCommand.isInviteOnly();
        Collection<String> current;

        if (isValidName(channelName)) {
            if (channels.containsValue(channelName)) {
                return Broadcast.error(createCommand, ServerResponse.NAME_ALREADY_IN_USE);
            }
            Channel newChan = new Channel(ownerName, privacyChannel);
            current = newChan.getUsers();
            channels.put(channelName, newChan);
            return Broadcast.okay(createCommand, current);
        } else {
            return Broadcast.error(createCommand, ServerResponse.INVALID_NAME);
        }
    }

    /**
     * This method is called when a user wants to join a channel.
     * You can ignore the privacy aspect of this method for task 4, but
     * make sure you come back and implement it in task 5.
     * 
     * @param joinCommand The {@link JoinCommand} object containing all
     *                    information needed for the user's join attempt
     * @return The {@link Broadcast} object generated by
     *         {@link Broadcast#names(Command, Collection, String)} if the user
     *         joins the channel successfully. The recipients should be all
     *         people in the joined channel (including the sender).
     *
     *         If an error occurs, use
     *         {@link Broadcast#error(Command, ServerResponse)} with either:
     *         (1) {@link ServerResponse#NO_SUCH_CHANNEL} if there is no
     *         channel with the specified name
     *         (2) (after Task 5) {@link ServerResponse#JOIN_PRIVATE_CHANNEL} if
     *         the sender is attempting to join a private channel
     */
    public Broadcast joinChannel(JoinCommand joinCommand) {
        String user = joinCommand.getSender();
        String channelName = joinCommand.getChannel();
        if (channels.containsKey(channelName)) {
            Channel join = channels.get(channelName);
            boolean privacy = join.privateState();
            if (privacy) {
                return Broadcast.error(joinCommand, ServerResponse.JOIN_PRIVATE_CHANNEL);
            } else {
                String ownerChan = join.getOwner();
                join.addUser(user);
                Collection<String> current = getUsersInChannel(channelName);
                return Broadcast.names(joinCommand, current, ownerChan);
            }

        } else {
            return Broadcast.error(joinCommand, ServerResponse.NO_SUCH_CHANNEL);
        }
    }


    /**
     * This method is called when a user wants to send a message to a channel.
     * 
     * @param messageCommand The {@link MessageCommand} object containing all
     *                       information needed for the messaging attempt
     * @return The {@link Broadcast} object generated by
     *         {@link Broadcast#okay(Command, Collection)} if the message
     *         attempt is successful. The recipients should be all clients
     *         in the channel.
     *
     *         If an error occurs, use
     *         {@link Broadcast#error(Command, ServerResponse)} with either:
     *         (1) {@link ServerResponse#NO_SUCH_CHANNEL} if there is no
     *         channel with the specified name
     *         (2) {@link ServerResponse#USER_NOT_IN_CHANNEL} if the sender is
     *         not in the channel they are trying to send the message to
     */
    public Broadcast sendMessage(MessageCommand messageCommand) {
        String client = messageCommand.getSender();
        String channelName = messageCommand.getChannel();
        if (channels.containsKey(channelName)) {
            if (channels.get(channelName).getUsers().contains(client)) {
                Collection<String> current = getUsersInChannel(channelName);
                return Broadcast.okay(messageCommand, current);
            } else {
                return Broadcast.error(messageCommand, ServerResponse.USER_NOT_IN_CHANNEL);
            }
        }
        return Broadcast.error(messageCommand, ServerResponse.NO_SUCH_CHANNEL);

    }


    /**
     * This method is called when a user wants to leave a channel.
     * 
     * @param leaveCommand The {@link LeaveCommand} object containing all
     *                     information about the user's leave attempt
     * @return The {@link Broadcast} object generated by
     *         {@link Broadcast#okay(Command, Collection)} if the user leaves
     *         the channel successfully. The recipients should be all clients
     *         who were in the channel, including the user who left.
     * 
     *         If an error occurs, use
     *         {@link Broadcast#error(Command, ServerResponse)} with either:
     *         (1) {@link ServerResponse#NO_SUCH_CHANNEL} if there is no
     *         channel with the specified name
     *         (2) {@link ServerResponse#USER_NOT_IN_CHANNEL} if the sender is
     *         not in the channel they are trying to leave
     */
    public Broadcast leaveChannel(LeaveCommand leaveCommand) {
        String client = leaveCommand.getSender();
        String channelName = leaveCommand.getChannel();
        Collection<String> current = getUsersInChannel(channelName);
        Collection<String> updated;
        if (channels.containsKey(channelName)) {
            Channel leave = channels.get(channelName);
            if (leave.getUsers().contains(client)) {
                if (leave.getOwner().equals(client)) {
                    leave.removeAllUsers();
                    channels.remove(channelName);
                } else {
                    leave.removeUser(client);
                }
                updated = current;
                updated.add(client);
                return Broadcast.okay(leaveCommand, updated);
            } else {
                return Broadcast.error(leaveCommand, ServerResponse.USER_NOT_IN_CHANNEL);
            }

        }
        return Broadcast.error(leaveCommand, ServerResponse.NO_SUCH_CHANNEL);
    }


    // =============================
    // == Task 5: Channel Privacy ==
    // =============================


    /**
     * This method is called when a channel's owner adds a user to that channel.
     * 
     * @param inviteCommand The {@link InviteCommand} object containing all
     *                      information needed for the invite attempt
     * @return The {@link Broadcast} object generated by
     *         {@link Broadcast#names(Command, Collection, String)} if the user
     *         joins the channel successfully as a result of the invite.
     *         The recipients should be all people in the joined channel
     *         (including the new user).
     *
     *         If an error occurs, use
     *         {@link Broadcast#error(Command, ServerResponse)} with either:
     *         (1) {@link ServerResponse#NO_SUCH_USER} if the invited user
     *         does not exist
     *         (2) {@link ServerResponse#NO_SUCH_CHANNEL} if there is no channel
     *         with the specified name
     *         (3) {@link ServerResponse#INVITE_TO_PUBLIC_CHANNEL} if the
     *         invite refers to a public channel
     *         (4) {@link ServerResponse#USER_NOT_OWNER} if the sender is not
     *         the owner of the channel
     */
    public Broadcast inviteUser(InviteCommand inviteCommand) {
        String invited = inviteCommand.getUserToInvite();
        String channelName = inviteCommand.getChannel();
        String send = inviteCommand.getSender();
        Channel chan = channels.get(channelName);
        int invitedID = getUserId(invited);

        if (channels.containsKey(channelName)) {
            if (chan.privateState()) {
                if (chan.getOwner().equals(send)) {
                    if (registeredClients.containsKey(invitedID)) {
                        chan.addUser(invited);
                        Collection<String> current = getUsersInChannel(channelName);
                        return Broadcast.names(inviteCommand, current, send);
                    } else {
                        return Broadcast.error(inviteCommand, ServerResponse.NO_SUCH_USER);
                    }

                } else {
                    return Broadcast.error(inviteCommand, ServerResponse.USER_NOT_OWNER);
                }
            } else {
                return Broadcast.error(inviteCommand, ServerResponse.INVITE_TO_PUBLIC_CHANNEL);
            }
        } else {
            return Broadcast.error(inviteCommand, ServerResponse.NO_SUCH_CHANNEL);
        }
    }


    /**
     * This method is called when a channel's owner removes a user from
     * that channel.
     * 
     * @param kickCommand The {@link KickCommand} object containing all
     *                    information needed for the kick attempt
     * @return The {@link Broadcast} object generated by
     *         {@link Broadcast#okay(Command, Collection)} if the user is
     *         successfully kicked from the channel. The recipients should be
     *         all clients who were in the channel, including the user
     *         who was kicked.
     *
     *         If an error occurs, use
     *         {@link Broadcast#error(Command, ServerResponse)} with either:
     *         (1) {@link ServerResponse#NO_SUCH_USER} if the user being kicked
     *         does not exist
     *         (2) {@link ServerResponse#NO_SUCH_CHANNEL} if there is no channel
     *         with the specified name
     *         (3) {@link ServerResponse#USER_NOT_IN_CHANNEL} if the
     *         user being kicked is not a member of the channel
     *         (4) {@link ServerResponse#USER_NOT_OWNER} if the sender is not
     *         the owner of the channel
     */
    public Broadcast kickUser(KickCommand kickCommand) {
        String kicked = kickCommand.getUserToKick();
        String channelName = kickCommand.getChannel();
        Channel chan = channels.get(channelName);
        String send = kickCommand.getSender();
        int kickedID = getUserId(kicked);

        if (channels.containsKey(channelName)) {
            if (registeredClients.containsKey(kickedID)) {
                if (chan.getOwner().equals(send)) {
                    if (chan.getUsers().contains(kicked)) {
                        chan.removeUser(kicked);
                        Collection<String> current = getUsersInChannel(channelName);
                        current.add(kicked);
                        channels.remove(chan);
                        return Broadcast.okay(kickCommand, current);
                    } else {
                        return Broadcast.error(kickCommand, ServerResponse.USER_NOT_IN_CHANNEL);
                    }
                } else {
                    return Broadcast.error(kickCommand, ServerResponse.USER_NOT_OWNER);
                }
            } else {
                return Broadcast.error(kickCommand, ServerResponse.NO_SUCH_USER);
            }
        } else {
            return Broadcast.error(kickCommand, ServerResponse.NO_SUCH_CHANNEL);
        }
    }

}
