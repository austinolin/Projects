import java.util.ArrayList;

/**
 * This is a Card class, representing a playing card from a typical deck of 52.
 * 
 * @author Austin
 */
public class Card {

	// Four suits corresponding with an integer value
	public final static int SPADES = 0;
	public final static int HEARTS = 1;
	public final static int DIAMONDS = 2;
	public final static int CLUBS = 3;

	// Face cards represented as integer values (2-10 already represented by
	// their respective integers)
	public final static int ACE = 1;
	public final static int JACK = 11;
	public final static int QUEEN = 12;
	public final static int KING = 13;

	// The suit of this card, one of the constants
	// SPADES, HEARTS, DIAMONDS, CLUBS.
	private int suit;

	// The value of this card, from 1 to 13.
	private int value;

	// True if the card is face up, else false
	private boolean active;

	// The countUntilActive shows how many Cards are "above" this Card and must
	// be removed before this Card can be switched to active.
	private int countUntilActive;

	// List of indexes for all subcards (if card is in the Field)
	private ArrayList<Integer> indexOfSubcards;

	private int x; // Top left corner x position
	private int y; // Top left corner y position
	private int cardWidth = 80; // Width of the card
	private int cardLength = 100; // Length of the card

	/**
	 * Creates a new Card with specified value and suit and either active or
	 * inactive, with no subcards. Starts x and y positions to be 0 until set.
	 * 
	 * @param value
	 *            , an int between 1 and 13.
	 * @param suit
	 *            , an int between 0 and 3
	 * @param active
	 *            , boolean that is true if the card is face up, else false
	 * @pre: value and suit are non-null and correspond with defined suit and
	 *       value variables
	 */
	public Card(int value, int suit, boolean active) {
		this.value = value;
		this.suit = suit;
		this.active = active;
		this.x = 0;
		this.y = 0;
		indexOfSubcards = new ArrayList<Integer>();
		// If inactive, then there are going to be two cards above it
		// in the field
		if (active) {
			this.countUntilActive = 0;
		} else {
			this.countUntilActive = 2;
		}
	}

	/**
	 * Will put the given subcard index into the list of subcard indexes, if not
	 * already contained in it
	 * 
	 * @param index
	 *            , a non-null Integer representing a subcard's index in its
	 *            set/row
	 */
	public void addSubcardIndex(Integer index) {
		if (!indexOfSubcards.contains(index)) {
			indexOfSubcards.add(index);
		}
	}

	/**
	 * @return the Integer value representing the Suit of the card
	 */
	public int getSuit() {
		return suit;
	}

	/**
	 * @return the Integer value representing the Value of the card
	 */
	public int getValue() {
		return value;
	}

	/**
	 * @return true if Card is active/faceup, else false
	 */
	public boolean isActive() {
		return active;
	}

	/**
	 * Will reduce the count until active of the card. If countUntilActive falls
	 * below 0, sets Card to active
	 */
	public void reduceCount() {
		countUntilActive--;
		if (countUntilActive <= 0) {
			makeActive();
		}
	}

	/**
	 * Sets boolean active to true, making card active/face up
	 */
	public void makeActive() {
		active = true;
	}

	/**
	 * Sets the position on the coordinate plane of the Card
	 * 
	 * @param x
	 *            , a non-null positive int representing the x-coordinate of the
	 *            top left corner of the card
	 * @param y
	 *            , a non-null positive int representing the y-coordinate of the
	 *            top left corner of the card
	 */
	public void setPosition(int x, int y) {
		this.x = x;
		this.y = y;
	}

	/**
	 * @return the x coordinate value of the top left corner of the Card
	 */
	public int getX() {
		return x;
	}

	/**
	 * @return the y coordinate value of the top left corner of the Card
	 */
	public int getY() {
		return y;
	}

	/**
	 * Will convert the integer value of suit to the String value representing
	 * the Suit of the Card
	 * 
	 * @return the String value of the Card's suit
	 */
	public String suitToString() {
		if (suit == SPADES) {
			return "Spades";
		} else if (suit == HEARTS) {
			return "Hearts";
		} else if (suit == DIAMONDS) {
			return "Diamonds";
		} else {
			return "Clubs";
		}
	}

	/**
	 * Will convert the integer value of Card's value to the String value
	 * representing the value of the Card
	 * 
	 * @return the String value of the Card's value
	 */
	public String valueToString() {
		if (value == 1) {
			return "Ace";
		} else if (value == 11) {
			return "Jack";
		} else if (value == 12) {
			return "Queen";
		} else if (value == 13) {
			return "King";
		} else {
			return "" + value;
		}
	}

	/**
	 * @return: the String representation of a Card in the format:
	 *          "{Value} of {Suit}"
	 */
	public String toString() {
		return valueToString() + " of " + suitToString();
	}

	/**
	 * Will return true if the card has been clicked on
	 * 
	 * @param x
	 *            , a non-null positive int representing the x-coordinate of the
	 *            mouse click
	 * @param y
	 *            , a non-null positive int representing the y-coordinate of the
	 *            mouse click
	 * @return true if the mouse clicked on the Card, else false
	 */
	public boolean isClicked(int x, int y) {
		return ((x >= this.x) && (x <= (this.x + cardWidth)) && (y >= this.y) && (y <= (this.y + cardLength)));
	}

	/**
	 * Will return true if the Card is allowed to be removed from the game with
	 * regards to the "Current" card in the hand. The rule is, for a
	 * currentValue of 13, this card's value must be either 1 or 12. For
	 * currentValue 1, this card's value must be either 13 or 2. For all other
	 * currentValues, this card's value must be currentValue-- or currentValue++
	 * 
	 * @param currentValue
	 *            , a non-null int between 1 and 13
	 * @return true if this Card is valid, else false
	 */
	public boolean isValid(int currentValue) {
		if (currentValue == 13) {
			return (value == 1 || value == 12);
		}
		if (currentValue == 1) {
			return (value == 13 || value == 2);
		}
		return ((currentValue - 1 == this.value) || (currentValue + 1 == this.value));

	}

	/**
	 * @return the list of Subcard indexes
	 */
	public ArrayList<Integer> getSubcards() {
		return indexOfSubcards;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + suit;
		result = prime * result + value;
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
		Card other = (Card) obj;
		if (suit != other.suit)
			return false;
		if (value != other.value)
			return false;
		return true;
	}

}