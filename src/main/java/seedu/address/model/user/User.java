package seedu.address.model.user;

import static java.util.Objects.requireNonNull;
import static seedu.address.commons.util.CollectionUtil.requireAllNonNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import javafx.collections.ObservableList;
import seedu.address.model.Name;
import seedu.address.model.accounting.Amount;
import seedu.address.model.accounting.Debt;
import seedu.address.model.accounting.DebtId;
import seedu.address.model.accounting.DebtStatus;
import seedu.address.model.accounting.UniqueDebtList;
import seedu.address.model.friend.Friendship;
import seedu.address.model.friend.FriendshipStatus;
import seedu.address.model.friend.UniqueFriendList;
import seedu.address.model.group.Group;
import seedu.address.model.timetable.Date;
import seedu.address.model.timetable.UniqueSchedule;
import seedu.address.model.timetable.Week;
import seedu.address.model.timetable.exceptions.DateNotFoundException;
import seedu.address.model.timetable.exceptions.DuplicateDateException;

/**
 * Represents a User in the address book.
 * Guarantees: details are present and not null, field values are validated, immutable.
 */
public class User {
    // Identity fields
    private final Username username;
    private final Password password;
    private final Name name;
    private final Phone phone;
    private final Email email;

    private final List<Group> groupRequests = new ArrayList<>();
    private final List<Group> groups = new ArrayList<>();
    private final UniqueFriendList friendRequests = new UniqueFriendList();
    private final UniqueFriendList friends = new UniqueFriendList();
    private final UniqueDebtList debts = new UniqueDebtList();
    private final UniqueSchedule busySchedule;
    private final List<RestaurantReview> restaurantReviews = new ArrayList<>();

    /**
     * Every field must be present and not null.
     */
    public User(Username username, Password password, Name name, Phone phone, Email email) {
        requireAllNonNull(username, password, name, phone, email);
        this.username = username;
        this.password = password;
        this.name = name;
        this.phone = phone;
        this.email = email;
        this.busySchedule = new UniqueSchedule(this.username);
    }

    public Username getUsername() {
        return username;
    }

    public Password getPassword() {
        return password;
    }

    public Name getName() {
        return name;
    }

    public Phone getPhone() {
        return phone;
    }

    public Email getEmail() {
        return email;
    }

    public List<Group> getGroupRequests() {
        return groupRequests;
    }

    public List<Group> getGroups() {
        return groups;
    }

    public UniqueSchedule getBusySchedule() {
        return busySchedule;
    }

    public List<RestaurantReview> getRestaurantReviews() {
        return restaurantReviews;
    }

    /**
     * Returns a string of the list of friends that the User has
     * @return String of list of friends
     */
    public String listFriends() {
        return friends.toString();
    }

    /**
     * Returns true if both users of the same name have at least one other identity field that is the same.
     * This defines a weaker notion of equality between two users.
     */
    public boolean isSameUser(User otherUser) {
        if (otherUser == this) {
            return true;
        }

        return otherUser != null
                && otherUser.getName().equals(getName())
                && (otherUser.getPhone().equals(getPhone()) || otherUser.getEmail().equals(getEmail()));
    }

    /**
     * Returns true if both users have the same identity and data fields.
     * This defines a stronger notion of equality between two users.
     */
    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }

        if (!(other instanceof User)) {
            return false;
        }

        User otherUser = (User) other;
        return otherUser.getUsername().equals(getUsername());
    }

    @Override
    public int hashCode() {
        // use this method for custom fields hashing instead of implementing your own
        return Objects.hash(username, password, name, phone, email, restaurantReviews);
    }

    @Override
    public String toString() {
        final StringBuilder builder = new StringBuilder();
        builder.append(getUsername())
                .append(" Password: ")
                .append(getPassword())
                .append(" Name: ")
                .append(getName())
                .append(" Phone: ")
                .append(getPhone())
                .append(" Email: ")
                .append(getEmail());
        return builder.toString();
    }

    /**
     * Allows a user to add other users as friends.
     * Exception handling is done in addFriendCommand.
     */
    public void addFriend(User user) {
        Friendship friendship = new Friendship(this, this, user);
        user.friendRequests.add(friendship);
    }

    /**
     * Allows user to add a friend or friendRequest by taking in
     * @param friendship
     * @return the updated User
     */
    public User addFriendship(Friendship friendship) {
        FriendshipStatus friendshipStatus = friendship.getFriendshipStatus();
        if (friendshipStatus.equals(FriendshipStatus.ACCEPTED)) {
            friends.add(friendship);
        } else {
            friendRequests.add(friendship);
        }
        return this;
    }

    /**
     * Returns an unmodifiable list of friends
     */
    public ObservableList<Friendship> getFriends() {
        UniqueFriendList toReturn = new UniqueFriendList();
        for (Friendship f: this.friends) {
            toReturn.add(f);
        }
        return toReturn.asUnmodifiableObservableList();
    }

    /**
     * Returns an unmodifiable list of friend requests
     */
    public ObservableList<Friendship> getFriendRequests() {
        UniqueFriendList toReturn = new UniqueFriendList();
        for (Friendship f: this.friendRequests) {
            toReturn.add(f);
        }
        return toReturn.asUnmodifiableObservableList();
    }

    /**
     * Ensures that the friendship can only be accepted by the party who did not initiate the request.
     * Changes friendship status to ACCEPTED.
     * Deletes friendship from friendRequests for the accepting party.
     * Adds friendship to friends list for both parties.
     * @param user Username of the friend to accept.
     */
    public void acceptFriendRequest(User user) {
        Friendship friendship = findFriendshipInList(friendRequests, user);
        User friend = friendship.getFriendUser();
        // changes friendship to accepted
        friendship.changeFriendshipStatus();

        // deletes from friendRequests for both parties
        friendRequests.remove(friendship);

        // adds to friends for both parties
        friends.add(friendship);
        Friendship friendship2 = new Friendship(this, friend, friend, FriendshipStatus.ACCEPTED);
        friend.friends.add(friendship2);
    }

    /**
     * Deletes the friendship request of the user with name username
     * @param user Username of the user that you want to delete friendRequest of
     */
    public void deleteFriendRequest(User user) {
        Friendship friendship = findFriendshipInList(friendRequests, user);
        friendRequests.remove(friendship);
    }

    /**
     * Deletes the friendship for both parties when delete is initiated by one party.
     * Deletes friendship only if friendship exists.
     * @param user Username of the friend you want to delete
     */
    public void deleteFriend(User user) {
        // gets the friendship for both parties
        Friendship friendship = findFriendshipInList(friends, user);
        Friendship friendship2 = findFriendshipInList(user.friends, this);
        friends.remove(friendship);
        user.friends.remove(friendship2);
    }

    /**
     * Helper method to find friendship in list.
     * @param list List in which you would like to search in.
     * @param user Username of the friend that you would like to find.
     * @return Friendship between this user and user with Name username.
     */
    public Friendship findFriendshipInList(UniqueFriendList list, User user) {
        for (Friendship friendship: list) {
            if (friendship.getFriendUser().equals(user)) {
                return friendship;
            }
        }
        return null;
    }

    /**
     * Allows a user to just create a group with the group name.
     * User creating will automatically be added into the group.
     * @param name name of the group
     * @return group Group created
     */
    public Group createGroup(String name) {
        Name groupName = new Name(name);
        Group group = new Group(groupName, this);
        this.groups.add(group);
        return group;
    }

    /**
     * Adds group to list of groups
     * @param group
     */
    public void addGroup(Group group) {
        this.groups.add(group);
    }

    /**
     * Adds group to list of groupRequests
     * @param group
     */
    public void addGroupPending(Group group) {
        this.groupRequests.add(group);
    }

    /**
     * Adds a group request to this User
     * @param group
     */
    public void addGroupRequest(Group group) {
        this.groupRequests.add(group);
    }

    /**
     * group is removed from the groupRequests list.
     * group is added to the groups list.
     * status is changed in the group.
     * @param group
     */
    public void acceptGroupRequest(Group group) {
        this.groupRequests.remove(group);
        this.groups.add(group);
        group.changeMemberStatus(this);
    }

    /**
     * group is removed from the groupRequests list.
     * User is removed from group's pendingUsers list
     * @param group
     */
    public void deleteGroupRequest(Group group) {
        this.groupRequests.remove(group);
        group.removePendingUser(this);
    }

    /**
     * group is removed from the groups list.
     * User is removed from group's acceptedUsers list
     * @param group
     */
    public void deleteGroup(Group group) {
        this.groups.remove(group);
        group.removeAcceptedUser(this);
    }

    /**
     * Helper function to list either groups or groupRequests
     * @param list
     * @return String of the group names
     */
    public String listHelperGroup(List<Group> list) {
        String toReturn = "";
        for (Group group: list) {
            toReturn += group.getGroupName() + "\n";
        }
        return toReturn;
    }

    /**
     * Returns list of groups
     * @return String accepted group names that have been accepted
     */
    public String listGroups() {
        return listHelperGroup(groups);
    }

    /**
     * Returns list of groupRequests
     * @return String of group names that have yet to be accepted
     */
    public String listGroupRequests() {
        return listHelperGroup(groupRequests);
    }

    /**
     * User can add new members after the creation of the group
     * @param group Group that members should be added to
     * @param users Users to be added
     */
    public void addGroupMembers(Group group, User... users) {
        group.addMembers(users);
    }

    //================== Debt ==========================
    /**
     * Method to add a debts to a user.
     * @param debt a debt to add.
     * @return the user who the debt added to.
     */
    public User addDebt(Debt debt) {
        debts.add(debt);
        return this;
    }

    /**
     * Method for the creditor to create and add a debt.
     * @param debtor the debtor of the adding debt
     * @param amount the amount of the adding debt
     */
    public void addDebt(User debtor, Amount amount) {
        Debt d = new Debt(this, debtor, amount);
        this.debts.add(d);
        debtor.debts.add(d);
    }

    /**
     * Method for the creditor to create and add a debt(with Pending status) to other user.
     * @param debtor the debtor of the adding debt
     * @param amount the amount of the adding debt
     * @param status the status of the adding debt
     */
    public void addDebt(User debtor, Amount amount, DebtStatus status) {
        Debt d = new Debt(this, debtor, amount, status);
        this.debts.add(d);
        debtor.debts.add(d);
    }

    /**
     * Method for the creditor to create and add debts(with Pending status) to other users in a group.
     * @param group the group of debtor of the adding debt
     * @param amount the total amount of the adding debt
     */
    public void addGroupDebt(User currentUser, Group group, Amount amount) {
        Amount amt = new Amount(String.valueOf
                ((Math.round((amount.toDouble()) / (group.getAcceptedUsers().size())) * 100) / 100));
        for (User u: group.getAcceptedUsers()) {
            if (!u.equals(currentUser)) {
                addDebt(u, amt);
            }
        }
    }

    /**
     * Method for the creditor to clear an amount of the debtor.
     * The amount will be deducted from the original debt amount,
     * and create a debt with balanced amount between two user.
     * Old debt will change to a Cleared status.
     * @param debtor the debtor of the clearing debt.
     * @param amount the amount of the clearing debt.
     */
    public void clearDebt(User debtor, Amount amount) {
        double balAmount = (amount.toDouble()) * (-1);
        int count = 0;
        for (Debt d: this.debts) {
            if ((d.getCreditor().equals(this))
                    && (d.getDebtor().equals(debtor))
                    && (d.getDebtStatus().equals(DebtStatus.ACCEPTED))) {
                balAmount += d.getAmount().toDouble();
                count = this.debts.asUnmodifiableObservableList().indexOf(d);
            }
        }
        if (balAmount == 0) {
            this.debts.asUnmodifiableObservableList().get(count).changeDebtStatus(DebtStatus.CLEARED);
            debtor.debts.asUnmodifiableObservableList().get(count).changeDebtStatus(DebtStatus.CLEARED);
        }
        else if (balAmount > 0) {
            Amount amt = new Amount(String.valueOf(Math.round(balAmount * 100) / 100));
            Debt toAdd = new Debt(this, debtor, amount, DebtStatus.CLEARED);
            this.debts.add(toAdd);
            debtor.debts.add(toAdd);
            this.debts.asUnmodifiableObservableList().get(count).changeDebtAmount(amt);
            debtor.debts.asUnmodifiableObservableList().get(count).changeDebtAmount(amt);
        }
    }

    /**
     * Method for debtor to accepted a debt request received.
     * It auto balanced the debt between user.
     * The original debt amount will be balanced or added to the new
     * accepted debt amount and create a new debt with total amount between two users.
     * Old debt will change to a Balanced status.
     * @param creditor the creditor of the accepting debt.
     * @param amount the amount of the accepting debt.
     * @param debtId the debtId of the accepting debt.
     */
    public void acceptedDebtRequest(User creditor, Amount amount, DebtId debtId) {
        double balAmount = amount.toDouble();
        boolean exist = false;
        for (Debt d: this.debts) {
            if ((d.getCreditor().equals(creditor))
                    && (d.getDebtor().equals(this))
                    && (d.getDebtStatus().equals(DebtStatus.ACCEPTED))) {
                balAmount += d.getAmount().toDouble();
                d.changeDebtStatus(DebtStatus.BALANCED);
                exist = true;
            }
            if ((d.getCreditor().equals(this))
                    && (d.getDebtor().equals(creditor))
                    && (d.getDebtStatus().equals(DebtStatus.ACCEPTED))) {
                balAmount -= d.getAmount().toDouble();
                d.changeDebtStatus(DebtStatus.BALANCED);
                exist = true;
            }
        }
        for (Debt d: creditor.debts) {
            if ((d.getCreditor().equals(creditor))
                    && (d.getDebtor().equals(this))
                    && (d.getDebtStatus().equals(DebtStatus.ACCEPTED))) {
                d.changeDebtStatus(DebtStatus.BALANCED);
            }
            if ((d.getCreditor().equals(this))
                    && (d.getDebtor().equals(creditor))
                    && (d.getDebtStatus().equals(DebtStatus.ACCEPTED))) {
                d.changeDebtStatus(DebtStatus.BALANCED);
            }
        }
        Debt toFind = new Debt(creditor, this, amount, debtId, DebtStatus.PENDING);
        if (!exist) {
            //this.debts.get(pos).changeDebtStatus(DebtStatus.ACCEPTED);
            Debt toAdd = new Debt(creditor, this, amount, debtId, DebtStatus.ACCEPTED);
            this.debts.remove(toFind);
            this.debts.add(toAdd);
            //creditor.debts.get(pos).changeDebtStatus(DebtStatus.ACCEPTED);
            creditor.debts.remove(toFind);
            creditor.debts.add(toAdd);
        } else {
            if (balAmount == 0) {
                //this.debts.get(pos).changeDebtStatus(DebtStatus.BALANCED);
                Debt toAdd = new Debt(creditor, this, amount, debtId, DebtStatus.BALANCED);
                this.debts.add(toAdd);
                this.debts.remove(toFind);
                //creditor.debts.get(pos).changeDebtStatus(DebtStatus.BALANCED);
                creditor.debts.remove(toFind);
                creditor.debts.add(toAdd);
            }
            if (balAmount > 0) {
                //this.debts.add(new Debt(creditor, this, amount, DebtStatus.BALANCED));
                Amount amt = new Amount(String.valueOf(Math.round(balAmount * 100) / 100));
                Debt toAdd = new Debt(creditor, this, amt, DebtStatus.ACCEPTED);
                Debt record = new Debt(creditor, this, amount, DebtStatus.BALANCED);
                this.debts.remove(toFind);
                this.debts.add(record);
                this.debts.add(toAdd);
                creditor.debts.remove(toFind);
                creditor.debts.add(record);
                creditor.debts.add(toAdd);
            }
            if (balAmount < 0) {
                //this.debts.add(new Debt(creditor, this, amount, DebtStatus.BALANCED));
                Amount amt = new Amount(String.valueOf(Math.round(balAmount * (-100)) / 100));
                Debt toAdd = new Debt(this, creditor, amt, DebtStatus.ACCEPTED);
                Debt record = new Debt(creditor, this, amount, DebtStatus.BALANCED);
                this.debts.remove(toFind);
                this.debts.add(record);
                this.debts.add(toAdd);
                creditor.debts.remove(toFind);
                creditor.debts.add(record);
                creditor.debts.add(toAdd);
            }
        }
    }

    /**
     * Method for debtor to reject and delete a debt request received.
     * @param creditor the creditor of the deleting debt.
     * @param amount the amount of the deleting debt.
     * @param debtId the debtId of the deleting debt.
     */
    public void deleteDebtRequest(User creditor, Amount amount, DebtId debtId) {
        Debt toFind = new Debt(creditor, this, amount, debtId, DebtStatus.PENDING);
        this.debts.remove(toFind);
        creditor.debts.remove(toFind);
    }

    /**
     * Method for getting all the Debts(regardless the status) of the login user
     */
    public ObservableList<Debt> getDebts() {
        return debts.asUnmodifiableObservableList();
    }

    /**
     * Method for getting all the Debts which login user is the debtor and the status is Accepted.
     */
    public ObservableList<Debt> getCreditor() {
        UniqueDebtList creditor = new UniqueDebtList();
        for (Debt d: this.debts) {
            if (d.getDebtor().equals(this) && d.getDebtStatus().equals(DebtStatus.ACCEPTED)) {
                creditor.add(d);
            }
        }
        return creditor.asUnmodifiableObservableList();
    }

    /**
     * Method for getting all the Debts which login user is the creditor and the status is Accepted.
     */
    public ObservableList<Debt> getDebtor() {
        UniqueDebtList debtor = new UniqueDebtList();
        for (Debt d: this.debts) {
            if (d.getCreditor().equals(this) && d.getDebtStatus().equals(DebtStatus.ACCEPTED)) {
                debtor.add(d);
            }
        }
        return debtor.asUnmodifiableObservableList();
    }

    /**
     * Method for getting all the Debts which login user is the debtor and the status is Pending.
     */
    public ObservableList<Debt> getDebtRequestReceived() {
        UniqueDebtList toReturn = new UniqueDebtList();
        for (Debt d: this.debts) {
            if (d.getDebtor().equals(this) && d.getDebtStatus().equals(DebtStatus.PENDING)) {
                toReturn.add(d);
            }
        }
        return toReturn.asUnmodifiableObservableList();
    }

    /**
     * Method for getting all the Debts which login user is the creditor and the status is Pending.
     */
    public ObservableList<Debt> getDebtRequestSent() {
        UniqueDebtList toReturn = new UniqueDebtList();
        for (Debt d: this.debts) {
            if (d.getCreditor().equals(this) && d.getDebtStatus().equals(DebtStatus.PENDING)) {
                toReturn.add(d);
            }
        }
        return toReturn.asUnmodifiableObservableList();
    }

    // ==================== TIMETABLE COMMANDS ======================= //
    /**
     * Adds the constructed model UniqueSchedule to the user.
     */
    public void addUniqueBusySchedule(UniqueSchedule schedule) {
        busySchedule.addAll(schedule);
    }

    /**
     * Blocks out a time on the user's schedule.
     */
    public void blockDateOnSchedule(Date date) {
        requireNonNull(date);
        if (busySchedule.contains(date)) {
            throw new DuplicateDateException();
        }
        busySchedule.add(date);
    }

    /**
     * Frees up a time on the user's schedule.
     */
    public void freeDateOnSchedule(Date date) {
        requireNonNull(date);
        if (!busySchedule.contains(date)) {
            throw new DateNotFoundException();
        }
        busySchedule.remove(date);

    }

    /**
     * Gets the user's blocked dates schedule for a week.
     */
    public List<Date> getFreeDatesForWeek(Week week) {
        return Collections.unmodifiableList(this.busySchedule.getFreeDatesForWeek(week));
    }

    /**
     * Checks if the date is contained in the user's schedule.
     */
    public boolean hasDateOnSchedule(Date date) {
        requireNonNull(date);
        return busySchedule.contains(date);
    }

    /**
     * Adds a restaurantReview to the user's list of restaurantReviews.
     */
    public User addRestaurantReviewToUser(RestaurantReview newRestaurantReview) {
        requireNonNull(newRestaurantReview);
        restaurantReviews.add(newRestaurantReview);
        return this;
    }
}
