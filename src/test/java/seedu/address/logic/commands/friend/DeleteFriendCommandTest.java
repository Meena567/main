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
import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.UserPrefs;
import seedu.address.model.friend.Friendship;
import seedu.address.model.friend.FriendshipStatus;
import seedu.address.model.user.User;
import seedu.address.model.user.Username;
import seedu.address.testutil.TypicalUsers;
import seedu.address.testutil.UserBuilder;

public class DeleteFriendCommandTest {
    private static final CommandHistory EMPTY_COMMAND_HISTORY = new CommandHistory();

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    private Model model = new ModelManager(getTypicalAddressBook(), new UserPrefs(),
            TypicalUsers.getTypicalUserData(), new UserBuilder().build());
    private CommandHistory commandHistory = new CommandHistory();
    private Username validUsernameA = TypicalUsers.getTypicalUsers().get(0).getUsername();
    private Username currentUsername = TypicalUsers.getTypicalUsers().get(1).getUsername();
    private User currentUser = TypicalUsers.getTypicalUsers().get(1);

    /**
     * If null username is passed, throws a null pointer exception
     */
    @Test
    public void constructor_nullDeleteFriend_throwsNullPointerException() {
        thrown.expect(NullPointerException.class);
        new DeleteFriendCommand(null);
    }

    @Test
    public void execute_deleteFriendAcceptedByModel_addSuccessful() throws Exception {
        DeleteFriendCommandTest.ModelStubforDeleteFriend modelStub = new DeleteFriendCommandTest
                .ModelStubforDeleteFriend();

        modelStub.addFriendship(validUsernameA);

        // size is 1 as new friendship has been added
        assertEquals(1, modelStub.friendsAdded.size());

        CommandResult commandResult = new DeleteFriendCommand(validUsernameA)
                .execute(modelStub, commandHistory);

        // assert that the feedback message is the same
        assertEquals(String.format(DeleteFriendCommand.MESSAGE_SUCCESS, validUsernameA),
                commandResult.feedbackToUser);

        // assert that the size of friendsAdded is 0 as friendship has been deleted
        assertEquals(0, modelStub.friendsAdded.size());

        assertEquals(EMPTY_COMMAND_HISTORY, commandHistory);
    }

    /**
     * A Model stub that always deletes friend
     */
    private class ModelStubforDeleteFriend extends ModelStub {

        private final ArrayList<Friendship> friendsAdded = new ArrayList<>();
        private boolean loggedIn = true;

        @Override
        public boolean isCurrentlyLoggedIn() {
            return loggedIn;
        }

        /**
         * adds a pending friendship
         * @param friendUsername
         */
        public void addFriendship(Username friendUsername) {
            requireNonNull(friendUsername);
            Friendship f = new Friendship(TypicalUsers.getTypicalUserData().getUser(friendUsername),
                    TypicalUsers.getTypicalUserData().getUser(friendUsername),
                    currentUser, FriendshipStatus.ACCEPTED);
            friendsAdded.add(f);
        }

        @Override
        public void deleteFriend(Username friendUsername) {
            requireNonNull(friendUsername);
            User friend = TypicalUsers.getTypicalUserData().getUser(friendUsername);
            Friendship toRemove = new Friendship(friend, friend, currentUser, FriendshipStatus.ACCEPTED);
            for (Friendship f: friendsAdded) {
                if (toRemove.equals(f)) {
                    friendsAdded.remove(f);
                    break;
                }
            }
        }

        @Override
        public boolean hasUsernameFriend(Username friendUsername) {
            for (Friendship f: friendsAdded) {
                if (f.getFriendUsername().equals(friendUsername)
                        && f.getMyUsername().equals(currentUsername)) {
                    return true;
                }
            }
            return false;
        }

    }
}
