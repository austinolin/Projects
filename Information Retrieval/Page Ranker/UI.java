import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.io.FileWriter;
import java.io.IOException;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;

import org.elasticsearch.search.SearchHit;

public class UI extends JPanel implements ActionListener {

    private static SearchHit[] hits;
    private static String assessorId;
    private static String queryId;

    private static JFrame frame;
    private URLLabel url;
    private JLabel numberAssessed;
    private JRadioButton zero, one, two;
    private static int pagesAssessed = 0;
    private static final int YES = 0;

    public UI() {
        super(new BorderLayout());

        String urlString = hits[pagesAssessed].getId();

        // Create the URL Label
        url = new URLLabel(urlString, urlString);
        url.setFont(new Font("Serif", Font.PLAIN, 20));
        url.setPreferredSize(new Dimension(500, 100));
        url.setBorder(BorderFactory.createLineBorder(Color.black));

        // Create the buttons
        zero = new JRadioButton("0 - Non-Relevant");
        zero.setSelected(true);

        one = new JRadioButton("1 - Relevant");

        two = new JRadioButton("2 - Very Relevant");

        JButton submit = new JButton("Submit Assessment");
        submit.addActionListener(this);

        // Group the buttons
        ButtonGroup group = new ButtonGroup();
        group.add(zero);
        group.add(one);
        group.add(two);
        group.add(submit);

        // Place buttons in a panel
        JPanel radioPanel = new JPanel(new GridLayout(0, 1));
        radioPanel.add(zero);
        radioPanel.add(one);
        radioPanel.add(two);
        radioPanel.add(submit);
        radioPanel.setBorder(BorderFactory.createLineBorder(Color.black));

        // Create documents assessed label
        numberAssessed = new JLabel("Pages assessed: " + pagesAssessed);
        numberAssessed.setFont(new Font("Serif", Font.BOLD, 20));
        numberAssessed.setPreferredSize(new Dimension(500, 100));
        numberAssessed.setBorder(BorderFactory.createLineBorder(Color.black));

        // add components to pane
        setBackground(Color.white);
        add(url, BorderLayout.PAGE_START);
        add(radioPanel, BorderLayout.CENTER);
        add(numberAssessed, BorderLayout.PAGE_END);

    }

    @Override
    public void actionPerformed(ActionEvent e) {
        // write line to file
        String assessment = "";
        if (zero.isSelected()) {
            assessment = "0";
        } else if (one.isSelected()) {
            assessment = "1";
        } else {
            assessment = "2";
        }

        String line = queryId + " " + assessorId + " " + url.getText() + " "
                + assessment + "\n";
        try (FileWriter fw = new FileWriter("qrel.txt", true)) {
            fw.write(line);
        } catch (IOException e1) {
            e1.printStackTrace();
        }

        // update number of pages assessed
        pagesAssessed++;
        numberAssessed.setText("Pages assessed: " + pagesAssessed);
        numberAssessed.repaint();

        if (pagesAssessed == hits.length) {
            int response = JOptionPane
                    .showConfirmDialog(
                            frame,
                            "There are no more pages to assess.\nWould you like to assess pages for another query?",
                            "Continue?", JOptionPane.YES_NO_OPTION);

            if (response == YES) {
                restartAssessment();

            } else {
                frame.dispatchEvent(new WindowEvent(frame,
                        WindowEvent.WINDOW_CLOSING));
            }
        }

        // update URL label
        String urlString = hits[pagesAssessed].getId();
        url.setText(urlString);
        url.setUrl(urlString);
        url.repaint();

    }

    private void restartAssessment() {
        queryId = JOptionPane.showInputDialog("What is the query id?");
        String query = JOptionPane.showInputDialog("What is the query?");

        hits = Search.query(query);
        pagesAssessed = 0;

        // update URL label
        String urlString = hits[pagesAssessed].getId();
        url.setText(urlString);
        url.setUrl(urlString);

        // update pages assessed label
        numberAssessed.setText("Pages assessed: " + pagesAssessed);

        // repaint
        numberAssessed.repaint();
        url.repaint();
    }

    private static void createAndShowGui() {
        assessorId = JOptionPane.showInputDialog("What is your assessor id?");
        queryId = JOptionPane.showInputDialog("What is the query id?");
        String query = JOptionPane.showInputDialog("What is the query?");

        hits = Search.query(query);

        // Create and set up the window
        frame = new JFrame("Assessment Iterface");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // Set up content page
        JComponent newContentPane = new UI();
        newContentPane.setOpaque(true);
        frame.setContentPane(newContentPane);

        // Display the window
        frame.pack();
        frame.setVisible(true);

    }

    public static void main(String[] args) {
        javax.swing.SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                createAndShowGui();
            }
        });
    }
}