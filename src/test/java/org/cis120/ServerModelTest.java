package org.cis120;

import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

import java.rmi.ServerError;
import java.util.Collection;
import java.util.Collections;

public class ServerModelTest {
    private ServerModel model;

    /**
     * Before each test, we initialize model to be
     * a new ServerModel (with all new, empty state)
     */
    @BeforeEach
    public void setUp() {
        // We initialize a fresh ServerModel for each test
        model = new ServerModel();
    }

    /**
     * Here is an example test that checks the functionality of your
     * changeNickname error handling. Each line has commentary directly above
     * it which you can use as a framework for the remainder of your tests.
     */
    @Test
    public void testInvalidNickname() {
        // A user must be registered before their nickname can be changed,
        // so we first register a user with an arbitrarily chosen id of 0.
        model.registerUser(0);

        // We manually create a Command that appropriately tests the case
        // we are checking. In this case, we create a NicknameCommand whose
        // new Nickname is invalid.
        Command command = new NicknameCommand(0, "User0", "!nv@l!d!");

        // We manually create the expected Broadcast using the Broadcast
        // factory methods. In this case, we create an error Broadcast with
        // our command and an INVALID_NAME error.
        Broadcast expected = Broadcast.error(
                command, ServerResponse.INVALID_NAME
        );

        // We then get the actual Broadcast returned by the method we are
        // trying to test. In this case, we use the updateServerModel method
        // of the NicknameCommand.
        Broadcast actual = command.updateServerModel(model);

        // The first assertEquals call tests whether the method returns
        // the appropriate Broadcast.
        assertEquals(expected, actual, "Broadcast");

        // We also want to test whether the state has been correctly
        // changed.In this case, the state that would be affected is
        // the user's Collection.
        Collection<String> users = model.getRegisteredUsers();

        // We now check to see if our command updated the state
        // appropriately. In this case, we first ensure that no
        // additional users have been added.
        assertEquals(1, users.size(), "Number of registered users");

        // We then check if the username was updated to an invalid value
        // (it should not have been).
        assertTrue(users.contains("User0"), "Old nickname still registered");

        // Finally, we check that the id 0 is still associated with the old,
        // unchanged nickname.
        assertEquals(
                "User0", model.getNickname(0),
                "User with id 0 nickname unchanged"
        );
    }

    /*
     * Your TAs will be manually grading the tests that you write below this
     * comment block. Don't forget to test the public methods you have added to
     * your ServerModel class, as well as the behavior of the server in
     * different scenarios.
     * You might find it helpful to take a look at the tests we have already
     * provided you with in Task4Test, Task3Test, and Task5Test.
     */

    @Test
    public void testDeregisterUser() {
        model.registerUser(0);
        model.registerUser(1);
        Command create = new CreateCommand(0, "User0", "vic", false);
        create.updateServerModel(model);
        Command join = new JoinCommand(1, "User1", "vic");
        join.updateServerModel(model);

        Broadcast expected = Broadcast.disconnected("User1", Collections.singleton("User0"));
        assertEquals(expected, model.deregisterUser(1), "broadcast");

        assertTrue(
                model.getChannels().contains("vic"),
                "channel exists"
        );
        assertEquals(
                1, model.getUsersInChannel("vic").size(),
                "number of users in channel"
        );
        assertTrue(
                model.getUsersInChannel("vic").contains("User0"),
                "unaffected user is still in channel"
        );
    }

    @Test
    public void testJoinPrivateNoSuchUser() {
        model.registerUser(0);

        Command create = new CreateCommand(0, "User0", "vic", true);
        create.updateServerModel(model);

        Command invite = new InviteCommand(0, "User0", "vic", "User4");

        Broadcast expected = Broadcast.error(invite, ServerResponse.NO_SUCH_USER);
        assertEquals(expected, invite.updateServerModel(model), "broadcast");

        assertTrue(
                model.getUsersInChannel("vic").contains("User0"),
                "User0 in channel"
        );
        assertEquals(
                1, model.getUsersInChannel("vic").size(),
                "number of users in channel"
        );
    }


    @Test
    public void testInviteUserNoSuchChannel() {
        model.registerUser(2);
        model.registerUser(3);
        Command create = new CreateCommand(2, "User2", "vic", true);
        create.updateServerModel(model);

        Command invite = new InviteCommand(2, "User2", "lol", "User3");
        Broadcast expected = Broadcast.error(invite, ServerResponse.NO_SUCH_CHANNEL);
        assertEquals(expected, invite.updateServerModel(model), "broadcast");
    }

    @Test
    public void testInviteUserPublicChannel() {
        model.registerUser(5);
        model.registerUser(9);
        Command create = new CreateCommand(5, "User5", "vic", false);
        create.updateServerModel(model);

        Command invite = new InviteCommand(5, "User5", "vic", "User9");
        Broadcast expected = Broadcast.error(invite, ServerResponse.INVITE_TO_PUBLIC_CHANNEL);
        assertEquals(expected, invite.updateServerModel(model), "broadcast");
    }

    @Test
    public void testInviteUserUserNotOwner() {
        model.registerUser(4);
        model.registerUser(7);
        Command create = new CreateCommand(4, "User4", "vic", true);
        create.updateServerModel(model);

        Command invite = new InviteCommand(4, "User7", "vic", "User4");
        Broadcast expected = Broadcast.error(invite, ServerResponse.USER_NOT_OWNER);
        assertEquals(expected, invite.updateServerModel(model), "broadcast");
    }


    @Test
    public void testInviteUserAlreadyMember() {
        model.registerUser(10);
        Command create = new CreateCommand(10, "User10", "vic", true);
        create.updateServerModel(model);

        Command invite = new JoinCommand(10, "User10", "vic");
        invite.updateServerModel(model);
        assertEquals(1,
                model.getUsersInChannel("vic").size(),
                "User10 was not added");
    }


    @Test
    public void testKickNoSuchUser() {
        model.registerUser(10);
        Command create = new CreateCommand(10, "User10", "vic", true);
        create.updateServerModel(model);

        Command kick = new KickCommand(10, "User10", "vic", "User1");
        Broadcast expected = Broadcast.error(kick, ServerResponse.NO_SUCH_USER);
        assertEquals(expected, kick.updateServerModel(model), "broadcast");
    }

    @Test
    public void testKickNoSuchChannel() {
        model.registerUser(10);
        model.registerUser(11);
        Command create = new CreateCommand(10, "User10", "vic", true);
        create.updateServerModel(model);

        Command kick = new KickCommand(10, "User10", "Allah", "User11");
        Broadcast expected = Broadcast.error(kick, ServerResponse.NO_SUCH_CHANNEL);
        assertEquals(expected, kick.updateServerModel(model), "broadcast");
    }
    public void testLeaveNoSuchChannel() {
        model.registerUser(0);

        Command leave = new LeaveCommand(0, "User0", "vic");
        Broadcast expected = Broadcast.error(leave, ServerResponse.NO_SUCH_CHANNEL);
        assertEquals(expected, leave.updateServerModel(model), "broadcast");
    }

    @Test
    public void testLeaveUserNotInChannel() {
        model.registerUser(9);
        model.registerUser(3);
        Command create = new CreateCommand(9, "User9", "vic", false);
        create.updateServerModel(model);

        Command leave = new LeaveCommand(3, "User3", "vic");
        Broadcast expected = Broadcast.error(leave, ServerResponse.USER_NOT_IN_CHANNEL);
        assertEquals(expected, leave.updateServerModel(model), "broadcast");
    }

    public void testMessageNoSuchChannel() {
        model.registerUser(23);
        Command message = new MessageCommand(23, "User23", "vic", "I love CIS");
        Broadcast expected = Broadcast.error(message, ServerResponse.NO_SUCH_CHANNEL);
        assertEquals(expected, message.updateServerModel(model), "broadcast");
    }

    @Test
    public void testMessageUserNotInChannel() {
        model.registerUser(23);
        model.registerUser(44);
        Command create = new CreateCommand(23, "User23", "vic", false);
        create.updateServerModel(model);

        Command message = new MessageCommand(44, "User44", "vic", "I love CIS");
        Broadcast expected = Broadcast.error(message, ServerResponse.USER_NOT_IN_CHANNEL);
        assertEquals(expected, message.updateServerModel(model), "broadcast");
    }
}


