import java.util.ArrayList;

/**
 * This is a class for a Field, which represents the cards currently in play
 * (excluding the current card and the deck. It has a middle row, two second
 * rows (one above and one below), and two third rows (one above the upper
 * second, one below the lower second). The middle row is initially the only set
 * of cards active; to flip inactive cards, all cards touching it that are above
 * (one row above) it must be set to null, which means they are out of play.
 * 
 * @author Austin
 *
 */
public class Field {

	// The initial middle row (9 cards)
	private ArrayList<Card> set1 = new ArrayList<Card>();
	// The upper second row (8 cards)
	private ArrayList<Card> set2a = new ArrayList<Card>();
	// The upper third row (4 cards
	private ArrayList<Card> set3a = new ArrayList<Card>();
	// The lower second row (8 cards)
	private ArrayList<Card> set2b = new ArrayList<Card>();
	// The lower third row (4 cards)
	private ArrayList<Card> set3b = new ArrayList<Card>();

	/**
	 * Creates a new Field, where the middle row is set to active
	 * 
	 * @param field
	 *            , a non-null ArrayList<Card> that contains the 33 cards that
	 *            will be placed into the field
	 * @pre: field is non-null, contains 33 Card objects
	 * @post: Cards will be removed one by one, placed in correct set/row
	 */
	public Field(ArrayList<Card> field) {
		// 9 cards in the initial active row
		for (int i = 0; i < 9; i++) {
			Card starter = field.get(0);
			starter.makeActive();
			starter.setPosition(10 + i * 90, 300);
			set1.add(starter);
			setFirstLevelSubcards(i);
			field.remove(0);
		}
		// 8 cards in the upper second row
		for (int i = 0; i < 8; i++) {
			Card c = field.get(0);
			c.setPosition(65 + i * 90, 220);
			set2a.add(c);
			field.remove(0);
		}
		// 8 cards in the lower second row
		for (int i = 0; i < 8; i++) {
			Card c = field.get(0);
			c.setPosition(65 + i * 90, 380);
			set2b.add(c);
			setSecondLevelSubcards(i);
			field.remove(0);
		}
		// 4 cards in the upper third row
		for (int i = 0; i < 4; i++) {
			Card c = field.get(0);
			c.setPosition(100 + i * 180, 140);
			set3a.add(c);
			field.remove(0);
		}
		// 4 cards in lower third row
		for (int i = 0; i < 4; i++) {
			Card c = field.get(0);
			c.setPosition(100 + i * 180, 460);
			set3b.add(c);
			field.remove(0);
		}

	}

	/**
	 * Will take a Card in the first row's index, and then add the indexes of
	 * the subcards it has to the Card. For each subcard index added, it will
	 * correspond to both the upper and lower second row sets. Ex: A subcard
	 * index of 2 refers to index 2 in set2a and index two in set2b
	 * 
	 * @param index
	 *            , a non-null Integer between 0 and 8 representing one of the
	 *            indexes in set1
	 * @pre: index is between 0-8
	 * @post: adds correct subcard indexes to the card in set1 at the given
	 *        index
	 */
	public void setFirstLevelSubcards(Integer index) {
		// First card only touches first card of 2a/2b
		if (index.equals(0)) {
			set1.get(index).addSubcardIndex(0);
			// Last card only touches last card of 2a/2b
		} else if (index.equals(8)) {
			set1.get(index).addSubcardIndex(7);
			// all others touch 4 cards, two from 2a and 2 from 2b
			// (same indexes for both rows)
		} else {
			set1.get(index).addSubcardIndex(index - 1);
			set1.get(index).addSubcardIndex(index);
		}
	}

	/**
	 * Will take a Card in the second row's index, and then add the indexes of
	 * the subcards it has to the Card at that index in both set2a and 2b. The
	 * subcard added to 2a refers to the card in 3a, the subcard added to 2b
	 * refers to the card in 3b.
	 * 
	 * @param index
	 *            , a non-null Integer between 0 and 7 representing one of the
	 *            indexes either set2
	 * @pre: index is between 0-7
	 * @post: adds correct subcard indexes to the card in set2a and set2b at the
	 *        given index
	 */
	public void setSecondLevelSubcards(Integer index) {
		// first two touch first card on 3rd row
		if (index.equals(0) || index.equals(1)) {
			set2a.get(index).addSubcardIndex(0);
			set2b.get(index).addSubcardIndex(0);
			// second two touch 2nd card on 3rd row
		} else if (index.equals(2) || index.equals(3)) {
			set2a.get(index).addSubcardIndex(1);
			set2b.get(index).addSubcardIndex(1);
			// third two touch 3rd card on 3rd row
		} else if (index.equals(4) || index.equals(5)) {
			set2a.get(index).addSubcardIndex(2);
			set2b.get(index).addSubcardIndex(2);
			// last two touch last card on third row
		} else {
			set2a.get(index).addSubcardIndex(3);
			set2b.get(index).addSubcardIndex(3);
		}
	}

	/**
	 * Will return the set of Card specified by setNumber, if invalid setNumber
	 * given, will throw an exception
	 * 
	 * @param setNumber
	 *            , a non-null String representing the set
	 * @pre: a valid setNumber is one of: -"1" -"2a" -"2b" -"3a" -"3b"
	 * @post: returns back the ArrayList that matches with the given setNumber
	 * @return the specified set of Card in the field, or throws exception if
	 *         setNumber is invalid
	 */
	public ArrayList<Card> getSet(String setNumber) {
		if (setNumber.equals("1")) {
			return set1;
		} else if (setNumber.equals("2a")) {
			return set2a;
		} else if (setNumber.equals("2b")) {
			return set2b;
		} else if (setNumber.equals("3a")) {
			return set3a;
		} else if (setNumber.equals("3b")) {
			return set3b;
		} else {
			throw new RuntimeException("Invalid set number");
		}
	}

	/**
	 * Will check to see if a card in the field has been clicked on, and returns
	 * that Card if it is active and a valid card relative to current. If
	 * inactive or no Card was clicked, returns null. If a card is clicked and
	 * active, and is set to null in the set to remove it from play.
	 * 
	 * @param x
	 *            , a non-null positive int representing the x-coordinate of the
	 *            Mouse click
	 * @param y
	 *            , a non-null positive int representing the y-coordinate of the
	 *            Mouse click
	 * @param currentValue
	 *            , a non-null int value of the "current" card
	 * @pre: currentValue must be a valid "value" (See Card)
	 * @post: Updates the field to either have a valid/active/clicked on card
	 * set to null (and returning that Card, updating its subcards accordingly),
	 * or returns null meaning no valid card was clicked and the field is unchanged
	 * @return a Card if clicked/active/valid, else null
	 */
	public Card onClick(int x, int y, int currentValue) {
		// List of subcard indexes will be stored
		ArrayList<Integer> subcards;
		// The card we clicked on, null if no active cards clicked
		Card removed = null;
		
		// Checks set 3a
		for (int i = 0; i < set3a.size(); i++) {
			Card c = set3a.get(i);
			// if card not removed already
			if (c != null) {
				if (c.isActive() && c.isClicked(x, y)
						&& c.isValid(currentValue)) {
					removed = c;
					// remove card from play
					set3a.set(i, null);
				}
			}
		}
		// Checks set 3b
		for (int i = 0; i < set3b.size(); i++) {
			Card c = set3b.get(i);
			// if card not removed already
			if (c != null) {
				if (c.isActive() && c.isClicked(x, y)
						&& c.isValid(currentValue)) {
					removed = c;
					// remove card from play
					set3b.set(i, null);
				}
			}
		}
		// Checks set 2a
		for (int i = 0; i < set2a.size(); i++) {
			Card c = set2a.get(i);
			// if card not removed already
			if (c != null) {
				if (c.isActive() && c.isClicked(x, y)
						&& c.isValid(currentValue)) {
					subcards = c.getSubcards();
					for (int subcardIndex : subcards) {
						set3a.get(subcardIndex).reduceCount();
					}
					removed = c;
					// remove card from play
					set2a.set(i, null);

				}
			}
		}
		// Checks set 2b
		for (int i = 0; i < set2b.size(); i++) {
			Card c = set2b.get(i);
			// if card not removed already
			if (c != null) {
				if (c.isActive() && c.isClicked(x, y)
						&& c.isValid(currentValue)) {
					subcards = c.getSubcards();
					for (int subcardIndex : subcards) {
						set3b.get(subcardIndex).reduceCount();
					}
					removed = c;
					// remove card from play
					set2b.set(i, null);
				}
			}
		}
		// Checks set 1
		for (int i = 0; i < set1.size(); i++) {
			Card c = set1.get(i);
			// if card not removed already
			if (c != null) {
				if (c.isActive() && c.isClicked(x, y)
						&& c.isValid(currentValue)) {
					subcards = c.getSubcards();
					for (int subcardIndex : subcards) {
						set2a.get(subcardIndex).reduceCount();
						set2b.get(subcardIndex).reduceCount();
					}
					removed = c;
					// remove card from play
					set1.set(i, null);
				}
			}
		}
		return removed;

	}

	/**
	 * Checks if the field (all sets) is empty or
	 * only populated with null Cards
	 * 
	 * @return true if empty or all null, else false
	 */
	public boolean isEmpty() {
		return allCardsAreNull(set1) && allCardsAreNull(set2a)
				&& allCardsAreNull(set2b) && allCardsAreNull(set3a)
				&& allCardsAreNull(set3b);
	}
	
	/**
	 * Checks if all of the Cards in the set are null, which
	 * means they are removed from the game
	 * @param set, the row of Cards we are checking (non-null)
	 * @return true if all Cards are null, else false
	 */
	private boolean allCardsAreNull(ArrayList<Card> set) {
		for (Card c : set) {
			if (c != null) {
				return false;
			}
		}
		return true;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((set1 == null) ? 0 : set1.hashCode());
		result = prime * result + ((set2a == null) ? 0 : set2a.hashCode());
		result = prime * result + ((set2b == null) ? 0 : set2b.hashCode());
		result = prime * result + ((set3a == null) ? 0 : set3a.hashCode());
		result = prime * result + ((set3b == null) ? 0 : set3b.hashCode());
		return result;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Field other = (Field) obj;
		if (set1 == null) {
			if (other.set1 != null)
				return false;
		} else if (!set1.equals(other.set1))
			return false;
		if (set2a == null) {
			if (other.set2a != null)
				return false;
		} else if (!set2a.equals(other.set2a))
			return false;
		if (set2b == null) {
			if (other.set2b != null)
				return false;
		} else if (!set2b.equals(other.set2b))
			return false;
		if (set3a == null) {
			if (other.set3a != null)
				return false;
		} else if (!set3a.equals(other.set3a))
			return false;
		if (set3b == null) {
			if (other.set3b != null)
				return false;
		} else if (!set3b.equals(other.set3b))
			return false;
		return true;
	}

}
