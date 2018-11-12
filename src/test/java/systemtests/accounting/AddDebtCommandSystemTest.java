package systemtests.accounting;

import static seedu.address.commons.core.Messages.MESSAGE_INVALID_COMMAND_FORMAT;
import static seedu.address.commons.core.Messages.MESSAGE_USER_NOT_LOGGED_IN_FOR_COMMAND;
import static seedu.address.logic.parser.CliSyntax.PREFIX_AMOUNT;
import static seedu.address.logic.parser.CliSyntax.PREFIX_USERNAME;

import org.junit.Test;

import seedu.address.commons.core.Messages;
import seedu.address.commons.core.index.Index;
import seedu.address.logic.commands.accounting.AddDebtCommand;
import seedu.address.model.Model;
import seedu.address.model.accounting.Amount;
import seedu.address.model.user.User;
import seedu.address.model.user.Username;
import seedu.address.testutil.TypicalUsers;
import seedu.address.testutil.UserBuilder;
import systemtests.AddressBookSystemTest;

public class AddDebtCommandSystemTest extends AddressBookSystemTest {

    private static final String CURRENT_USER = TypicalUsers.getTypicalUsers().get(0).getUsername().toString();
    private static final String VALID_USER_A = TypicalUsers.getTypicalUsers().get(1).getUsername().toString();
    private static final String VALID_USER_B = TypicalUsers.getTypicalUsers().get(2).getUsername().toString();
    private static final String INVALID_USER_C = "Kate";
    private static final String INVALID_USER_A = "test a";
    private static final String VALID_AMOUNT = "101";
    private static final String INVALID_AMOUNT_FORMAT = "11.11111111";
    private static final String INVALID_AMOUNT_FORMAT_NEGATIVE = "-17";
    private static final String INVALID_AMOUNT_ZERO = "0";
    private static final String INVALID_AMOUNT_TOO_LAREG = "9999999999999";

    private User currentUser = TypicalUsers.getTypicalUsers().get(0);
    private User otherUserA = TypicalUsers.getTypicalUsers().get(1);
    private User otherUserB = TypicalUsers.getTypicalUsers().get(2);

    private Username userA = TypicalUsers.getTypicalUsers().get(1).getUsername();
    private Username userB = TypicalUsers.getTypicalUsers().get(2).getUsername();

    private Amount amount = new Amount("101");

    @Test
    public void add() {
        //Add and login user to test model
        addUser(otherUserA);
        addUser(otherUserB);
        addUser(currentUser);
        login(currentUser);
        //Create model(expected model)
        Model model = getModel();

        User userACopy = new UserBuilder().withUsername(otherUserA.getUsername().toString())
                .withPhone(otherUserA.getPhone().toString())
                .withPassword(otherUserA.getPassword().toString())
                .withName(otherUserA.getName().toString())
                .withEmail(otherUserA.getEmail().toString())
                .build();
        User userBCopy = new UserBuilder().withUsername(otherUserB.getUsername().toString())
                .withPhone(otherUserB.getPhone().toString())
                .withPassword(otherUserB.getPassword().toString())
                .withName(otherUserB.getName().toString())
                .withEmail(otherUserB.getEmail().toString())
                .build();
        User currentUserCopy = new UserBuilder().withUsername(currentUser.getUsername().toString())
                .withPhone(currentUser.getPhone().toString())
                .withPassword(currentUser.getPassword().toString())
                .withName(currentUser.getName().toString())
                .withEmail(currentUser.getEmail().toString())
                .build();
        //Add and login user to expected model
        model.addUser(userACopy);
        model.addUser(userBCopy);
        model.addUser(currentUserCopy);
        model.loginUser(currentUserCopy);

        //Test success for AddDebtCommand with standard command format, valid input and model
        String command = AddDebtCommand.COMMAND_WORD + " " + PREFIX_USERNAME + VALID_USER_A
                + " " + PREFIX_AMOUNT + VALID_AMOUNT;
        addSleep();
        assertCommandSuccess(command, model, userA, amount);

        // Test success for AddDebtCommand if execute the same command as Debt Id is generate automatically and
        // should not be repeated, so not a duplicated Debt.
        // This allow user create multiple debts with same amount and debtor.

        addSleep();
        assertCommandSuccess(command, model, userA, amount);

        //Test success for AddDebtCommand with standard command format, valid input and model with different user
        command = AddDebtCommand.COMMAND_WORD + " " + PREFIX_USERNAME + VALID_USER_B
                + " " + PREFIX_AMOUNT + VALID_AMOUNT;
        addSleep();
        assertCommandSuccess(command, model, userB, amount);

        //Test success for AddDebtCommand when selecting other type of item
        selectRestaurant(Index.fromOneBased(3));
        addSleep();
        assertCommandSuccess(command, model, userB, amount);

        //Test success for AddDebtCommand with other login user(creditor)
        logout();
        model.logoutUser();
        login(otherUserA);
        model.loginUser(userA);
        addSleep();
        assertCommandSuccess(command, model, userB, amount);

        //Test failure for AddDebtCommand with missing username prefix
        command = AddDebtCommand.COMMAND_WORD + " " + VALID_USER_B
                + " " + PREFIX_AMOUNT + VALID_AMOUNT;
        addSleep();
        assertCommandFailure(command, model, String.format(MESSAGE_INVALID_COMMAND_FORMAT,
                AddDebtCommand.MESSAGE_USAGE));

        //Test failure for AddDebtCommand with missing amount prefix
        command = AddDebtCommand.COMMAND_WORD + " " + PREFIX_USERNAME + VALID_USER_B
                + " " + VALID_AMOUNT;
        addSleep();
        assertCommandFailure(command, model, String.format(MESSAGE_INVALID_COMMAND_FORMAT,
                AddDebtCommand.MESSAGE_USAGE));

        //Test failure for AddDebtCommand with invalid username
        command = AddDebtCommand.COMMAND_WORD + " " + PREFIX_USERNAME + INVALID_USER_A
                + " " + PREFIX_AMOUNT + VALID_AMOUNT;
        addSleep();
        assertCommandFailure(command, model, Username.MESSAGE_USERNAME_CONSTRAINTS);

        //Test failure for AddDebtCommand with invalid amount(more than 2 decimal places)
        command = AddDebtCommand.COMMAND_WORD + " " + PREFIX_USERNAME + VALID_USER_B
                + " " + PREFIX_AMOUNT + INVALID_AMOUNT_FORMAT;
        addSleep();
        assertCommandFailure(command, model, Amount.MESSAGE_AMOUNT_CONSTRAINTS);

        //Test failure for AddDebtCommand with invalid amount(negative)
        command = AddDebtCommand.COMMAND_WORD + " " + PREFIX_USERNAME + VALID_USER_B
                + " " + PREFIX_AMOUNT + INVALID_AMOUNT_FORMAT_NEGATIVE;
        addSleep();
        assertCommandFailure(command, model, Amount.MESSAGE_AMOUNT_CONSTRAINTS);

        //Test failure for AddDebtCommand with invalid command word
        command = "AddDebt" + " " + PREFIX_USERNAME + VALID_USER_A
                + " " + PREFIX_AMOUNT + VALID_AMOUNT;
        addSleep();
        assertCommandFailure(command, model, Messages.MESSAGE_UNKNOWN_COMMAND);

        //Test failure for AddDebtCommand with no login user in the model
        logout();
        model.logoutUser();
        command = AddDebtCommand.COMMAND_WORD + " " + PREFIX_USERNAME + VALID_USER_B
                + " " + PREFIX_AMOUNT + VALID_AMOUNT;
        addSleep();
        assertCommandFailure(command, model,
                String.format(MESSAGE_USER_NOT_LOGGED_IN_FOR_COMMAND, AddDebtCommand.COMMAND_WORD));

        //Login current user for following test
        login(currentUser);
        model.loginUser(currentUser);

        //Test failure for AddDebtCommand with input debtor same as login user
        command = AddDebtCommand.COMMAND_WORD + " " + PREFIX_USERNAME + CURRENT_USER
                + " " + PREFIX_AMOUNT + VALID_AMOUNT;
        addSleep();
        assertCommandFailure(command, model, AddDebtCommand.MESSAGE_CANNOT_ADD_DEBT_TO_ONESELF);

        //Test failure for AddDebtCommand with invalid amount(0).
        command = AddDebtCommand.COMMAND_WORD + " " + PREFIX_USERNAME + VALID_USER_A
                + " " + PREFIX_AMOUNT + INVALID_AMOUNT_ZERO;
        addSleep();
        assertCommandFailure(command, model, AddDebtCommand.MESSAGE_INVALID_AMOUNT);

        //Test failure for AddDebtCommand with invalid amount(larger than 100000000).
        command = AddDebtCommand.COMMAND_WORD + " " + PREFIX_USERNAME + VALID_USER_A
                + " " + PREFIX_AMOUNT + INVALID_AMOUNT_TOO_LAREG;
        addSleep();
        assertCommandFailure(command, model, AddDebtCommand.MESSAGE_INVALID_AMOUNT);

        //Test failure for AddDebtCommand with input debtor not in model
        command = AddDebtCommand.COMMAND_WORD + " " + PREFIX_USERNAME + INVALID_USER_C
                + " " + PREFIX_AMOUNT + VALID_AMOUNT;
        addSleep();
        assertCommandFailure(command, model, AddDebtCommand.MESSAGE_NO_SUCH_USER);
    }

    /**
     * Performs verification.
     */
    private void assertCommandSuccess(String command, Model expectedModel, Username debtor, Amount amount) {
        expectedModel.addDebt(debtor, amount);
        String expectedResultMessage = String.format(AddDebtCommand.MESSAGE_SUCCESS, debtor, amount.toDouble());
        assertCommandSuccess(command, expectedModel, expectedResultMessage);
    }

    /**
     * Performs the same verification as
     * {@code assertCommandSuccess(String, Model, Username, Amount)} except asserts that the,<br>
     * 1. Result display box displays {@code expectedResultMessage}.<br>
     * 2. {@code Storage} equal to the corresponding components in {@code expectedModel}.<br>
     */
    private void assertCommandSuccess(String command, Model expectedModel, String expectedResultMessage) {
        executeCommand(command);
        assertApplicationDisplaysExpected("", expectedResultMessage, expectedModel);
        assertSelectedCardUnchanged();
        assertCommandBoxShowsDefaultStyle();
    }

    /**
     * Executes {@code command} and asserts that the,<br>
     * 1. Command box displays {@code command}.<br>
     * 2. Command box has the error style class.<br>
     * 3. Result display box displays {@code expectedResultMessage}.<br>
     * 4. {@code Storage} remain unchanged.
     */
    private void assertCommandFailure(String command, Model expectedModel, String expectedResultMessage) {
        executeCommand(command);
        assertApplicationDisplaysExpected(command, expectedResultMessage, expectedModel);
        assertSelectedCardUnchanged();
        assertCommandBoxShowsErrorStyle();
    }
}
