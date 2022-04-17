package org.cis120;
import java.util.*;

public class Channel {
    private String owner;
    private boolean isPrivate;
    private TreeSet<String> usersInChannel;

    public Channel(String own) {
        usersInChannel = new TreeSet<>();
        owner = own;
        usersInChannel.add(owner);
        isPrivate = false;
    }

    public Channel(String own, Boolean invite) {
        usersInChannel = new TreeSet<>();
        owner = own;
        usersInChannel.add(owner);
        isPrivate = invite;
    }

    public TreeSet<String> getUsers() {
        TreeSet<String> newUsers = usersInChannel;
        return newUsers;
    } //returns the users in the Channel

    public String getOwner() {
        return owner;
    } //returns the owner of the Channel

    public void changeOwnerName(String name) {
        owner = name;
    } //changes the name of the owner

    public boolean privateState() {
        return isPrivate;
    } //returns the state of the Channel (public or private)

    public void addUser(String user) {
        usersInChannel.add(user);
    } //adds user to the Channel

    public void removeUser(String user) {
        usersInChannel.remove(user);
    } //removes user from the Channel

    public void removeAllUsers() {
        usersInChannel = new TreeSet<>();
    } //removes all users from the Channel
}

