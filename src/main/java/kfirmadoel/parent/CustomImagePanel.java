package parent;

import java.awt.Graphics;
import java.awt.image.BufferedImage;
import javax.swing.JPanel;


/**
 * CustomImagePanel defines object that...
 * @author USER | 15/01/2024
 */
class CustomImagePanel extends JPanel {
        private BufferedImage image;
        
        
        public void setImage(BufferedImage image) {
            this.image = image;
            repaint();
        }

        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            if (image != null) {
                g.drawImage(image, 0, 0, getWidth(), getHeight(), this);
            }
        }
    }
