package seedu.address.logic.commands.group;

import static java.util.Objects.requireNonNull;

import seedu.address.logic.CommandHistory;
import seedu.address.logic.commands.Command;
import seedu.address.logic.commands.CommandResult;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.Model;

/**
 * Deletes a group request the user received..
 */
public class DeleteGroupRequestCommand extends Command {
    public static final String COMMAND_WORD = "deleteGroupRequest";

    // TODO
    public static final String MESSAGE_USAGE = null;

    // TODO
    public static final String MESSAGE_SUCCESS = null;

    // TODO
    public static final String MESSAGE_DUPLICATE_REVIEW = null;

    // TODO
    private final Integer group;

    /**
     * Creates a DeleteGroupRequestCommand that will delete an
     * invitation to join the specified {@code Integer} group.
     */
    public DeleteGroupRequestCommand(Integer group) {
        requireNonNull(group);
        this.group = group;
    }

    @Override
    public CommandResult execute(Model model, CommandHistory history) throws CommandException {
        // TODO
        requireNonNull(model);

        return null;
    }

    @Override
    public boolean equals(Object other) {
        return other == this // short circuit if same object
                || (other instanceof DeleteGroupRequestCommand // instanceof handles nulls
                && group.equals(((DeleteGroupRequestCommand) other).group));
    }
}