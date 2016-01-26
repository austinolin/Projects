
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Desktop;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.net.URI;

import javax.swing.JLabel;

/**
 * Code found at:
 * http://insidecoding.com/2011/09/05/creating-a-url-jlabel-in-swing/
 *
 * @author ludovicianul
 */
public class URLLabel extends JLabel {

    private String url;

    public URLLabel() {
        this("", "");
    }

    public URLLabel(String label, String url) {
        super(label);

        this.url = url;
        setForeground(Color.BLUE.darker());
        setCursor(new Cursor(Cursor.HAND_CURSOR));
        addMouseListener(new URLOpenAdapter());
    }

    public void setUrl(String url) {
        this.url = url;
    }

    private class URLOpenAdapter extends MouseAdapter {

        @Override
        public void mouseClicked(MouseEvent e) {
            if (Desktop.isDesktopSupported()) {
                try {
                    Desktop.getDesktop().browse(new URI(url));
                } catch (Throwable t) {
                    //
                }
            }
        }
    }
}