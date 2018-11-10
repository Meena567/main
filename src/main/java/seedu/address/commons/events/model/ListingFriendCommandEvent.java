package seedu.address.commons.events.model;

import javafx.collections.ObservableList;
import seedu.address.commons.events.BaseEvent;
import seedu.address.model.group.Friendship;

/** Listing all the friends.*/
public class ListingFriendCommandEvent extends BaseEvent {
    public final ObservableList<Friendship> listingItem;

    public ListingFriendCommandEvent(ObservableList<Friendship> listingItem) {
        this.listingItem = listingItem;
    }

    @Override
    public String toString() {
        return "Listing Friendships";
    }
}
