package seedu.address.logic.commands.friend;

import static java.util.Objects.requireNonNull;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
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
import seedu.address.testutil.TypicalUsers;
import seedu.address.testutil.UserBuilder;

public class AddFriendCommandTest {
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
    public void constructor_nullAddFriend_throwsNullPointerException() {
        thrown.expect(NullPointerException.class);
        new AddFriendCommand(null);
    }

    @Test
    public void execute_addFriendAcceptedByModel_addSuccessful() throws Exception {
        AddFriendCommandTest.ModelStubforAddFriend modelStub = new AddFriendCommandTest.ModelStubforAddFriend();

        CommandResult commandResult = new AddFriendCommand(validUsernameA).execute(modelStub, commandHistory);

        // assert that the feedback message is the same
        assertEquals(String.format(AddFriendCommand.MESSAGE_SUCCESS, validUsernameA),
                commandResult.feedbackToUser);

        // assert that the size of friendsAdd is 1 as only one friendship is created
        assertEquals(1, modelStub.friendsAdded.size());

        // assert that every field in the friendship is the same
        assertEquals(validUsernameA, modelStub.friendsAdded.get(0).getFriendUsername());
        assertEquals(validUsernameA, modelStub.friendsAdded.get(0).getInitiatedBy().getUsername());
        assertEquals(currentUsername, modelStub.friendsAdded.get(0).getMyUsername());
        assertEquals(FriendshipStatus.PENDING, modelStub.friendsAdded.get(0).getFriendshipStatus());

        assertEquals(EMPTY_COMMAND_HISTORY, commandHistory);
    }

    /**
     * Throws exception if no user is currently logged in
     * @throws Exception
     */
    @Test
    public void execute_notLoggedIn_throwsCommandException() throws Exception {

        AddFriendCommand addFriendCommand = new AddFriendCommand(validUsernameA);
        AddFriendCommandTest.ModelStubforAddFriend modelStub = new AddFriendCommandTest.ModelStubforAddFriend();
        modelStub.loggedIn = false;

        thrown.expect(CommandException.class);
        thrown.expectMessage(String.format(
                AddFriendCommand.MESSAGE_NOT_LOGGED_IN, AddFriendCommand.COMMAND_WORD));
        addFriendCommand.execute(modelStub, commandHistory);
    }

    /**
     * Throws exception if the username specified to add is not a valid username in the model
     * @throws Exception
     */
    @Test
    public void execute_userNotInModel_throwsCommandException() throws Exception {
        AddFriendCommand addFriendCommand = new AddFriendCommand(invalidUser);
        ModelStub modelStub = new AddFriendCommandTest.ModelStubforAddFriend();

        thrown.expect(CommandException.class);
        thrown.expectMessage(AddFriendCommand.MESSAGE_NO_SUCH_USER);
        addFriendCommand.execute(modelStub, commandHistory);
    }

    /**
     * Throws exception if the user tries to add oneself as a friend
     * @throws Exception
     */
    @Test
    public void execute_isSameUser_throwsCommandException() throws Exception {
        AddFriendCommand addFriendCommand = new AddFriendCommand(currentUsername);
        ModelStub modelStub = new AddFriendCommandTest.ModelStubforAddFriend();

        thrown.expect(CommandException.class);
        thrown.expectMessage(AddFriendCommand.MESSAGE_CANNOT_ADD_ONESELF);
        addFriendCommand.execute(modelStub, commandHistory);
    }

    /**
     * Throws exception if more than one friend request is sent to the same user
     * by the currently logged in user
     * @throws Exception
     */
    @Test
    public void execute_addSameUserAgain_throwsCommandException() throws Exception {
        AddFriendCommand addFriendCommand1 = new AddFriendCommand(validUsernameA);
        AddFriendCommand addFriendCommand2 = new AddFriendCommand(validUsernameA);
        ModelStub modelStub = new AddFriendCommandTest.ModelStubforAddFriend();

        addFriendCommand1.execute(modelStub, commandHistory);
        thrown.expect(CommandException.class);
        thrown.expectMessage(AddFriendCommand.MESSAGE_DUPLICATE_FRIEND_REQUEST);
        addFriendCommand2.execute(modelStub, commandHistory);
    }

    /**
     * Throws exception if currently logged in user tries to add a user with whom
     * he or she is already friends with
     * @throws Exception
     */
    @Test
    public void execute_addExistingFriend_throwsCommandException() throws Exception {
        Friendship alreadyFriends1 = new Friendship(currentUser, currentUser, validUserA,
                FriendshipStatus.ACCEPTED);
        Friendship alreadyFriends2 = new Friendship(validUserA, currentUser, currentUser,
                FriendshipStatus.ACCEPTED);
        AddFriendCommand addFriendCommand = new AddFriendCommand(validUsernameA);
        ModelStubforAddFriend modelStub = new AddFriendCommandTest.ModelStubforAddFriend();
        modelStub.addFriendtoFriendsAlready(alreadyFriends1);
        modelStub.addFriendtoFriendsAlready(alreadyFriends2);

        thrown.expect(CommandException.class);
        thrown.expectMessage(AddFriendCommand.MESSAGE_FRIEND_ALREADY);
        addFriendCommand.execute(modelStub, commandHistory);
    }

    /**
     * Throws an exception if currently logged in user tries to send another user
     * a friend request if the other user had previously sent the currently logged in
     * user a friend request. Should accept the existing friend request instead of sending
     * a friend request.
     * @throws Exception
     */
    @Test
    public void execute_acceptExistingRequest_throwsCommandException() throws Exception {
        Friendship acceptFriendRequest = new Friendship(currentUser, currentUser, validUserA);
        AddFriendCommand addFriendCommand = new AddFriendCommand(validUsernameA);
        ModelStubforAddFriend modelStub = new AddFriendCommandTest.ModelStubforAddFriend();
        modelStub.addFriendtoFriendRequests(acceptFriendRequest);

        thrown.expect(CommandException.class);
        thrown.expectMessage(AddFriendCommand.MESSAGE_ACCEPT_EXISTING_REQUEST);
        addFriendCommand.execute(modelStub, commandHistory);
    }

    @Test
    public void equals() {
        AddFriendCommand test1 = new AddFriendCommand(validUsernameA);
        AddFriendCommand test2 = new AddFriendCommand(validUsernameB);
        AddFriendCommand test3 = new AddFriendCommand(validUsernameA);

        assertTrue(test1.equals(test1));

        assertTrue(test1.equals(test3));

        assertFalse(test1.equals("test1"));

        assertFalse(test1.equals(null));

        assertFalse(test1.equals(test2));
    }

    /**
     * A Model stub that always adds friend being added.
     */
    private class ModelStubforAddFriend extends ModelStub {

        private final ArrayList<Friendship> friendsAdded = new ArrayList<>();
        private final ArrayList<Friendship> friendsAlready = new ArrayList<>();
        private boolean loggedIn = true;

        @Override
        public boolean isCurrentlyLoggedIn() {
            return loggedIn;
        }

        @Override
        public boolean hasUser(Username username) {
            return TypicalUsers.getTypicalUserData().hasUser(username);
        }

        @Override
        public boolean isSameAsCurrentUser(Username username) {
            return currentUser.equals(TypicalUsers.getTypicalUserData().getUser(username));
        }

        @Override
        public void addFriend(Username friendUsername) {
            requireNonNull(friendUsername);
            User friend = TypicalUsers.getTypicalUserData().getUser(friendUsername);
            Friendship toAdd = new Friendship(friend, friend, currentUser);
            friendsAdded.add(toAdd);
        }

        @Override
        public boolean hasUsernameSentRequest(Username friendUsername) {
            User friend = TypicalUsers.getTypicalUserData().getUser(friendUsername);
            Friendship friendshipToAdd = new Friendship(friend, friend, currentUser);
            for (Friendship f: friendsAdded) {
                if (f.getMyUsername().equals(friendshipToAdd.getMyUsername())
                        && f.getFriendUsername().equals(friendshipToAdd.getFriendUsername())) {
                    return true;
                }
            }
            return false;
        }

        @Override
        public boolean hasUsernameFriend(Username friendUsername) {
            for (Friendship f: friendsAlready) {
                if (f.getFriendUsername().equals(friendUsername)) {
                    return true;
                }
            }
            return false;
        }

        public void addFriendtoFriendsAlready(Friendship friendship) {
            friendsAlready.add(friendship);
        }

        public void addFriendtoFriendRequests(Friendship friendship) {
            friendsAdded.add(friendship);
        }

        @Override
        public boolean hasUsernameFriendRequest(Username friendUsername) {
            for (Friendship f: friendsAdded) {
                if (f.getFriendUsername().equals(currentUsername)
                        && f.getMyUsername().equals(friendUsername)) {
                    return true;
                }
            }
            return false;
        }

    }
}
