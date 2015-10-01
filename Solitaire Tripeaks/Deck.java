import java.util.ArrayList;

/**
 * This is a Deck class, that represents the deck of cards you can use for the
 * TriPeaks card game. It can be generated, shuffled, and drawn from.
 * 
 * @author Austin
 */
public class Deck {

	// The list of cards currently in the deck
	private ArrayList<Card> deck;
	private int x = 200; // Top left corner x position
	private int y = 600; // Top left corner y position
	private int cardWidth = 80; // Width of card
	private int cardLength = 100; // Length of card

	/**
	 * Creates a new Deck containing 52 cards, 2-10/Jack/Queen/King/Ace for each
	 * Suit. Each card is initially inactive.
	 */
	public Deck() {
		deck = new ArrayList<Card>();
		for (int suit = 0; suit <= 3; suit++) {
			for (int value = 1; value <= 13; value++) {
				deck.add(new Card(value, suit, false));
			}
		}
	}

	/**
	 * Will shuffle the deck of cards into a random order.
	 */
	public void shuffle() {
		for (int i = 51; i > 0; i--) {
			int rand = (int) (Math.random() * (i + 1));
			Card temp = deck.get(i);
			deck.set(i, deck.get(rand));
			deck.set(rand, temp);
		}
	}

	/**
	 * Will draw the top card (front of list) from the deck and return it. If
	 * deck is empty, return null;
	 * 
	 * @return the top Card from the deck or null if deck is empty
	 */
	public Card draw() {
		Card drawn = null;
		if (!deck.isEmpty()) {
			drawn = deck.get(0);
			deck.remove(0);
		}
		return drawn;
	}

	/**
	 * Will check to see if deck is empty
	 * 
	 * @return true if empty, else false
	 */
	public boolean isEmpty() {
		return deck.isEmpty();
	}

	/**
	 * Will return true if the Deck has been clicked on
	 * 
	 * @param x
	 *            , a non-null positive int representing the x-coordinate of the
	 *            mouse click
	 * @param y
	 *            , a non-null positive int representing the y-coordinate of the
	 *            mouse click
	 * @return true if the Deck was clicked on, else false. False if deck is
	 *         empty
	 */
	public boolean onClick(int x, int y) {
		if (!isEmpty()) {
			return ((x >= this.x) && (x <= this.x + cardWidth) && (y >= this.y) && (y <= this.y
					+ cardLength));
		}
		return false;
	}

	/**
	 * @return the int number of Card in the deck currently
	 */
	public int getCount() {
		return deck.size();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((deck == null) ? 0 : deck.hashCode());
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
		Deck other = (Deck) obj;
		if (deck == null) {
			if (other.deck != null)
				return false;
		} else if (!deck.equals(other.deck))
			return false;
		return true;
	}
}
