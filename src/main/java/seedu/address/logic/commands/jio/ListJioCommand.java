package seedu.address.logic.commands.jio;

import static java.util.Objects.requireNonNull;

import javafx.collections.ObservableList;
import seedu.address.logic.CommandHistory;
import seedu.address.logic.commands.Command;
import seedu.address.logic.commands.CommandResult;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.Model;
import seedu.address.model.jio.Jio;
import seedu.address.model.restaurant.Restaurant;

import java.util.Iterator;
import java.util.function.Predicate;

/**
 * List the available jios.
 */
public class ListJioCommand extends Command {
    public static final String COMMAND_WORD = "listJio";

    public static final String MESSAGE_SUCCESS = "Listed all restaurants";

    @Override
    public CommandResult execute(Model model, CommandHistory history) {
        requireNonNull(model);
        
        /*
        String list = "All jios:";
        
        ObservableList<Jio> allJios = model.getJioList();
        Iterator<Jio> iterator = allJios.iterator();
        while(iterator.hasNext()) {
            list += ("\n" + iterator.next());
        }
        */
        
        return new CommandResult(MESSAGE_SUCCESS);
    }

}

