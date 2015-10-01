import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import javax.swing.AbstractAction;
import javax.swing.JApplet;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.Timer;
import javax.swing.border.LineBorder;

/**
 * This is a TriPeaksGUI class that creates a JFrame for a TriPeaks Solitaire
 * game.
 * 
 * @author Austin
 *
 */
public class TriPeaksGUI extends JApplet {
	// If false, create Main Menu page. Else, generate game page
	private boolean gameStarted = false;
	// The background color
	private Color background = Color.green.darker().darker();

	/**
	 * Generates the Main Menu page if gameStarted is false, otherwise 
	 * generates the Game
	 */
	public void init() {
		// Generate Main Menu
		if (!gameStarted) {
			JPanel buttonPanel = new JPanel();
			buttonPanel.setBackground(background);
			buttonPanel.add(new JButton(new AbstractAction("Start Game") {
				public void actionPerformed(ActionEvent e) {
					// When clicked on, start the game
					gameStarted = true;
					init();
				}
			}));

			JFrame frame = new JFrame("Tripeaks Solitaire");
			JPanel title = new JPanel();
			title.setSize(500, 500);
			title.setBackground(background);
			JLabel jlabel = new JLabel("TRIPEAKS SOLITAIRE");
			jlabel.setFont(new Font("Verdana", 1, 50));
			title.add(jlabel);
			title.setBorder(new LineBorder(Color.BLACK)); // make it easy to see
			frame.setSize(400, 400);
			frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
			frame.getContentPane().add(title, BorderLayout.NORTH);
			frame.getContentPane().setPreferredSize(new Dimension(2000, 1000));
			frame.getContentPane().add(buttonPanel, BorderLayout.SOUTH);
			frame.pack();
			frame.setVisible(true);
		} else {
			// Generate the Game panel
			JFrame frame = new JFrame("Tripeaks Solitaire");
			frame.setSize(900, 800);
			frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
			JPanel panel = new TriPeaks();
			panel.addMouseListener(new MouseAdapter() {
				@Override
				public void mouseClicked(MouseEvent e) {
					// Check all cards to see if any are clicked, then handle
					// appropriately
					((TriPeaks) panel).mouseClicked(e.getX(), e.getY());
				}
			});
			panel.setBackground(background);
			frame.getContentPane().add(panel, BorderLayout.CENTER);
			frame.setVisible(true);
		}

	}

	/**
	 * This is a nested class for the TriPeaks game. It has a timer, 
	 * lays the cards out, and allows the player to click on cards and play.
	 * A player can draw a card by clicking on the deck, or remove a faceup card
	 * from play if it is valid based on the current card. When a card is
	 * removed or drawn, it replaces current. The game ends when either all
	 * cards are removed, or the time runs out. Tripeaks Solitaire
	 * 
	 * @author Austin
	 *
	 */
	class TriPeaks extends JPanel {

		// The deck
		Deck deck;
		// The current card in your hand
		Card current;
		// The field of cards in play that are not in the deck or
		// in the hand
		Field field;
		// Remaining minutes
		int minutes;
		// Remaining seconds
		int seconds;
		// True if the mouse/timer is enabled, else false
		boolean enabled;
		// The timer of the game
		Timer time;
		// True if the game has been won, else false
		boolean won;

		/**
		 * Creates a new TriPeaks(). Creates a deck and timer, and deal the
		 * cards into the field and current.
		 */
		public TriPeaks() {
			// Timer ticks every second
			int delay = 1000;
			ActionListener taskPerformer = new ActionListener() {
				public void actionPerformed(ActionEvent evt) {
					// Game is won if field is empty
					if (field.isEmpty()) {
						won = true;
						endGame();
					} else if (seconds == 0 && minutes == 0) {
						// Game is lost if time runs out
						endGame();
					} else if (seconds == 0) {
						minutes--;
						seconds = 59;
					} else {
						seconds--;
					}
					repaint();
				}
			};
			time = new Timer(delay, taskPerformer);
			time.start();
			setBackground(background);

			// Create and shuffle deck
			deck = new Deck();
			deck.shuffle();
			ArrayList<Card> fieldCards = new ArrayList<Card>();

			// 33 cards drawn to create the Field
			for (int i = 1; i <= 33; i++) {
				fieldCards.add(deck.draw());
			}
			field = new Field(fieldCards);
			// Draw current card in hand
			current = deck.draw();
			current.makeActive();
			// Game timer set to 5 minutes initially
			minutes = 5;
			seconds = 0;
			// Mouse and timer enabled
			enabled = true;
			won = false;
		}

		/**
		 * Ends the game by disabling the mouse clicks and stopping the
		 * timer.
		 */
		private void endGame() {
			enabled = false;
			time.stop();
		}

		/**
		 * Paints the game on the board, including the deck, current card,
		 * field, timer, and any text
		 */
		public void paintComponent(Graphics g) {
			super.paintComponent(g);
			g.setFont(new Font("SansSerif", Font.PLAIN, 12));

			// Iterate through each set and draw the card on the
			// board. If a card is null, it is out of play and not
			// drawn on the board.
			drawRow(g, field.getSet("3a"));
			drawRow(g, field.getSet("3b"));
			drawRow(g, field.getSet("2a"));
			drawRow(g, field.getSet("2b"));
			drawRow(g, field.getSet("1"));
			

			// Draw facedown card for deck, or nothing if deck is empty
			if (!deck.isEmpty()) {
				drawCard(g, new Card(1, 1, false), 200, 600, true);
			}

			// Draw the current card
			drawCard(g, current, 400, 600, false);
			g.setFont(new Font("Serif", Font.BOLD, 24));

			String secondString;
			if (seconds > 9) {
				secondString = ((Integer) seconds).toString();
			} else {
				secondString = "0" + seconds;
			}

			// Write out the time remaining if game is still going
			if (enabled) {
				g.drawString("Time Remaining: " + minutes + ":" + secondString,
						10, 23);
			} else {
				// Write either the winner message, or the loser message
				if (won) {
					g.drawString("You won! Time Left: " + minutes + ":"
							+ secondString, 10, 23);
				} else {
					g.drawString("You lose! Time ran out: 0:00", 10, 23);
				}
			}
		}

		/**
		 * Draws a row of Cards from the field onto the frame.
		 * @param g, the non-null Graphics
		 * @param row, a non-null ArrayList of Cards
		 */
		private void drawRow(Graphics g, ArrayList<Card> row) {
			for (int i = 0; i < row.size(); i++) {
				Card c = row.get(i);
				if (c != null) {
					drawCard(g, c, c.getX(), c.getY(), false);
				}
			}
		}
		
		
		/**
		 * Draws a Card on the board at the given coordinates if the Card is not
		 * null
		 * 
		 * @param g
		 *            , the Graphics used to draw (non-null)
		 * @param card
		 *            , a Card (can be null) that is to be drawn
		 * @param x
		 *            , a positive non-null int representing the x-coordinate of
		 *            the topleft corner of the card
		 * @param y
		 *            , a positive non-null int representing the y-coordinate of
		 *            the topleft corner of the card
		 * @param isDeck
		 *            , non-null boolean that is true if the card is just the
		 *            deck card
		 */
		void drawCard(Graphics g, Card card, int x, int y, boolean isDeck) {
			if (!card.isActive()) {
				// Draw a face-down card
				g.setColor(Color.red);
				g.fillRect(x, y, 80, 100);
				g.setColor(Color.white);
				g.drawRect(x + 3, y + 3, 73, 93);
				g.drawRect(x + 4, y + 4, 71, 91);
				// if card is for the Deck, draw the number of cards
				// remaining in the deck on the card
				if (isDeck) {
					g.drawString(deck.getCount() + "", x + 30, y + 50);
				}
			} else {
				// Draw a face-up card
				g.setColor(Color.white);
				g.fillRect(x, y, 80, 100);
				g.setColor(Color.gray);
				g.drawRect(x, y, 79, 99);
				g.drawRect(x + 1, y + 1, 77, 97);
				// Set color depending on suit
				if (card.getSuit() == Card.DIAMONDS
						|| card.getSuit() == Card.HEARTS) {
					g.setColor(Color.red);
				} else {
					g.setColor(Color.black);
				}
				// Draw String version of Card on card
				g.drawString(card.valueToString(), x + 10, y + 30);
				g.drawString("of", x + 10, y + 50);
				g.drawString(card.suitToString(), x + 10, y + 70);
			}
		}

		/**
		 * If mouse is clicked and enabled, then checks all cards in game to
		 * see if they've been clicked. If deck is clicked, draws a new
		 * current card. If a field card is clicked/active/valid, removes it
		 * from play and make it new current card. Else, does nothing.
		 * 
		 * @param x
		 *            , non-null positive int representing the x-coordinate of
		 *            the mouse click
		 * @param y
		 *            , non-null positive int representing the y-coordinate of
		 *            the mouse click
		 */
		// @Override
		public void mouseClicked(int x, int y) {
			if (enabled) {
				if (deck.onClick(x, y)) {
					current = deck.draw();
					current.makeActive();
				}
				Card removed = field.onClick(x, y, current.getValue());
				if (removed != null) {
					current = removed;
				}
				repaint();
			}

		}
	}
}
