package seedu.address.logic.commands.friend;

import static java.util.Objects.requireNonNull;
import static org.junit.Assert.assertEquals;
import static seedu.address.testutil.TypicalRestaurants.getTypicalAddressBook;

import java.util.ArrayList;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import seedu.address.logic.CommandHistory;
import seedu.address.logic.commands.CommandResult;
import seedu.address.logic.commands.ModelStub;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.UserPrefs;
import seedu.address.model.friend.Friendship;
import seedu.address.model.friend.FriendshipStatus;
import seedu.address.model.user.User;
import seedu.address.model.user.Username;
import seedu.address.testutil.TypicalFriendships;
import seedu.address.testutil.TypicalUsers;
import seedu.address.testutil.UserBuilder;

public class AcceptFriendCommandTest {
    private static final CommandHistory EMPTY_COMMAND_HISTORY = new CommandHistory();

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    private Model model = new ModelManager(getTypicalAddressBook(), new UserPrefs(),
            TypicalUsers.getTypicalUserData(), new UserBuilder().build());
    private CommandHistory commandHistory = new CommandHistory();
    private Username validUsernameA = TypicalUsers.getTypicalUsers().get(0).getUsername();
    private User validUserA = TypicalUsers.getTypicalUsers().get(0);
    private Username validUsernameB = TypicalUsers.getTypicalUsers().get(2).getUsername();
    private Username invalidUser = new Username("NOTAUSER");
    private Username currentUsername = TypicalUsers.getTypicalUsers().get(1).getUsername();
    private User currentUser = TypicalUsers.getTypicalUsers().get(1);

    /**
     * If null username is passed, throws a null pointer exception
     */
    @Test
    public void constructor_nullAcceptFriend_throwsNullPointerException() {
        thrown.expect(NullPointerException.class);
        new AcceptFriendCommand(null);
    }

    @Test
    public void execute_acceptFriendAcceptedByModel_addSuccessful() throws Exception {
        AcceptFriendCommandTest.ModelStubforAcceptFriend modelStub = new AcceptFriendCommandTest
                .ModelStubforAcceptFriend();

        modelStub.addFriendship(validUsernameA);

        CommandResult commandResult = new AcceptFriendCommand(validUsernameA)
                .execute(modelStub, commandHistory);

        // assert that the feedback message is the same
        assertEquals(String.format(AcceptFriendCommand.MESSAGE_SUCCESS, validUsernameA),
                commandResult.feedbackToUser);

        // assert that the size of friendsAdded is 1 as only one friendship
        assertEquals(1, modelStub.friendsAdded.size());

        // assert that every field in the friendship is the same
        assertEquals(validUsernameA, modelStub.friendsAdded.get(0).getFriendUsername());
        assertEquals(validUsernameA, modelStub.friendsAdded.get(0).getInitiatedBy().getUsername());
        assertEquals(currentUsername, modelStub.friendsAdded.get(0).getMyUsername());
        assertEquals(FriendshipStatus.ACCEPTED, modelStub.friendsAdded.get(0).getFriendshipStatus());

        assertEquals(EMPTY_COMMAND_HISTORY, commandHistory);
    }

    /**
     * Throws exception if no user is currently logged in
     * @throws Exception
     */
    @Test
    public void execute_notLoggedIn_throwsCommandException() throws Exception {

        AcceptFriendCommand acceptFriendCommand = new AcceptFriendCommand(validUsernameA);
        AcceptFriendCommandTest.ModelStubforAcceptFriend modelStub = new AcceptFriendCommandTest
                .ModelStubforAcceptFriend();
        modelStub.loggedIn = false;

        thrown.expect(CommandException.class);
        thrown.expectMessage(String.format(
                AcceptFriendCommand.MESSAGE_NOT_LOGGED_IN, AcceptFriendCommand.COMMAND_WORD));
        acceptFriendCommand.execute(modelStub, commandHistory);
    }

    /**
     * Throw exception if no such request is received
     * @throws Exception
     */
    @Test
    public void execute_noFriendToAccept() throws Exception {
        AcceptFriendCommandTest.ModelStubforAcceptFriend modelStub = new AcceptFriendCommandTest
                .ModelStubforAcceptFriend();
        AcceptFriendCommand acceptFriendCommand = new AcceptFriendCommand(validUsernameA);
        thrown.expect(CommandException.class);
        thrown.expectMessage(String.format(
                AcceptFriendCommand.MESSAGE_NO_REQUEST, AcceptFriendCommand.COMMAND_WORD));
        acceptFriendCommand.execute(modelStub, commandHistory);

    }

    /**
     * A Model stub that always adds friend being added.
     */
    private class ModelStubforAcceptFriend extends ModelStub {

        private final ArrayList<Friendship> friendsAdded = new ArrayList<>();
        private boolean loggedIn = true;

        @Override
        public boolean isCurrentlyLoggedIn() {
            return loggedIn;
        }

        public void addFriendship(Username friendUsername) {
            requireNonNull(friendUsername);
            Friendship f = new Friendship(TypicalUsers.getTypicalUserData().getUser(friendUsername),
                    TypicalUsers.getTypicalUserData().getUser(friendUsername),
                    currentUser);
            friendsAdded.add(f);
        }

        @Override
        public void acceptFriend(Username friendUsername) {
            requireNonNull(friendUsername);
            User friend = TypicalUsers.getTypicalUserData().getUser(friendUsername);
            Friendship toChange = new Friendship(friend, friend, currentUser);
            for (Friendship f: friendsAdded) {
                if(toChange.equals(f)) {
                    f.changeFriendshipStatus();
                }
            }
        }

        @Override
        public boolean hasUsernameFriendRequest(Username friendUsername) {
            for (Friendship f: friendsAdded) {
                System.out.println(f.getFriendUsername() + " " + friendUsername);
                System.out.println(f.getMyUsername());
                if (f.getFriendUsername().equals(friendUsername)
                        && f.getMyUsername().equals(currentUsername)) {
                    return true;
                }
            }
            return false;
        }

    }
}
