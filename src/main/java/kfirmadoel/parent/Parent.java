package parent;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.io.DataInputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import parent.MouseOpetions.mouseStatus;

/**
 * Parent defines object that...
 * @author USER | 31/01/2024
 */
public class Parent
{
    private boolean isScreenShared;
    private boolean isUnderControl;
    private String IP;
    private CustomImagePanel imagePanel;
    private Boolean isMouseInPanel;
    private int widthResolutionOfChild;
    private int heightResolutionOfChild;

    // Attributes תכונות
    // Methoods פעולות
    public Parent()
    {
        this.IP = "192.168.211.24";
        int port=0;
        try
        {
            Socket connectionSock = new Socket(IP, 12345);
            DataInputStream dataInputStream = new DataInputStream(connectionSock.getInputStream());

            // Read an integer from the client
            port = dataInputStream.readInt();
            System.out.println("Received port: " + port);
            this.widthResolutionOfChild = dataInputStream.readInt();
            System.out.println("width="+widthResolutionOfChild);
            this.heightResolutionOfChild = dataInputStream.readInt();
            System.out.println("height="+heightResolutionOfChild);
            connectionSock.close();
        } catch (IOException ex)
        {
            Logger.getLogger(Parent.class.getName()).log(Level.SEVERE, null, ex);
        }
        this.isScreenShared = false;
        this.isUnderControl = false;
        Connection con = new Connection(port);
        Thread thread=new Thread(new Runnable()
        {
            @Override
            public void run()
            {
        
            JFrame frame = new JFrame();
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            imagePanel = new CustomImagePanel();
            // Create buttons
            JButton screenShareButton = new JButton("start/stop screen share");
            screenShareButton.addActionListener(new ActionListener()
            {
                @Override
                public void actionPerformed(ActionEvent e)
                {
                    if (isScreenShared == false)
                    {
                        isScreenShared = true;
                        con.sendActionOverActionSocket("start screen share");
                        con.openPhotoConnection();
                        isScreenShared = true;
                        System.out.println("start screen share");
                    } else
                    {
                        isScreenShared = false;
                        con.sendActionOverActionSocket("stop screen share");
                        con.closePhotoConnection();
                        System.out.println("stop screen share");
                    }
                }
            });
            JButton takeControlButton = new JButton("take control/stop");
            takeControlButton.addActionListener(new ActionListener()
            {
                @Override
                public void actionPerformed(ActionEvent e)
                {
                    if (isUnderControl == false)
                    {
                        isUnderControl = true;
                        con.sendActionOverActionSocket("give control");
                        con.openKeyboardConnection();
                        con.openMouseConnection();
                        isUnderControl = true;
                        System.out.println("give control");
                    } else
                    {
                        isUnderControl = false;
                        con.sendActionOverActionSocket("stop give control");
                        con.closeKeyboardConnection();
                        con.closeMouseConnection();
                        System.out.println("stop give control");
                    }
                }
            });
            JButton shutdownButton = new JButton("shoutdown");
            shutdownButton.addActionListener(new ActionListener()
            {
                @Override
                public void actionPerformed(ActionEvent e)
                {
                    con.sendActionOverActionSocket("shutdown");
                    con.closePhotoConnection();
                    con.closeMouseConnection();
                    con.closeKeyboardConnection();
                    con.closeActionConnection();
                }
            });
            imagePanel.addKeyListener(new KeyListener()
            {
                @Override
                public void keyTyped(KeyEvent e)
                {
//                    if (isUnderControl == true)
//                    {
//                        KeyboardButton key = new KeyboardButton(e.getKeyCode(), KeyboardButton.buttonStatus.CLICKED);
//                        System.out.println(key.toString());
//                        con.sendKeyboardCommandOverSocket(key);
//                    }
                }

                @Override
                public void keyPressed(KeyEvent e)
                {
                    if (isUnderControl == true)
                    {
                        KeyboardButton key = new KeyboardButton(e.getKeyCode(), KeyboardButton.buttonStatus.PRESSED);
                        System.out.println(key.toString());
                        con.sendKeyboardCommandOverSocket(key);
                    }
                }

                @Override
                public void keyReleased(KeyEvent e)
                {
                    if (isUnderControl == true)
                    {
                        KeyboardButton key = new KeyboardButton(e.getKeyCode(), KeyboardButton.buttonStatus.REALESED);
                        System.out.println(key.toString());
                        con.sendKeyboardCommandOverSocket(key);
                    }
                }
            });

            // Create a panel for the buttons with a horizontal BoxLayout
            JPanel buttonPanel = new JPanel();
            buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.X_AXIS));
            buttonPanel.add(screenShareButton);
            buttonPanel.add(takeControlButton);
            buttonPanel.add(shutdownButton);

            // Create your CustomImagePanel
            
            imagePanel.addMouseMotionListener(new MouseMotionListener()
            {
                @Override
                public void mouseDragged(MouseEvent e)
                {

                }

                @Override
                public void mouseMoved(MouseEvent e)
                {
                    if (isMouseInPanel == true&&isUnderControl==true)
                    {

                        // Get the screen size in pixels
                        Dimension screenSize = imagePanel.getSize();
                        
                        int atitudeHeight = heightResolutionOfChild/screenSize.height;
                        int atitudewidth = widthResolutionOfChild/screenSize.width;
                        MouseOpetions key = new MouseOpetions(atitudeHeight * e.getY(), atitudewidth * e.getX(), mouseStatus.MOVED, e.getModifiersEx());
                        System.out.println("check");
                        System.out.println(key.toString());
                        con.sendMouseCommandOverSocket(key);
                    };
                }
            });
            imagePanel.addMouseListener(new MouseListener()
            {
                @Override
                public void mouseClicked(MouseEvent e)
                {
//                    if (isMouseInPanel == true&&isUnderControl==true)
//                    {
//                        Toolkit toolkit = Toolkit.getDefaultToolkit();
//
//                        // Get the screen size in pixels
//                        Dimension screenSize = toolkit.getScreenSize();
//
//                        int atitudeHeight = screenSize.height / heightResolutionOfChild;
//                        int atitudewidth = screenSize.width / widthResolutionOfChild;
//                        MouseOpetions key = new MouseOpetions(atitudeHeight * e.getY(), atitudewidth * e.getX(), mouseStatus.CLICKED, e.getModifiersEx());
//                        con.sendMouseCommandOverSocket(key);
//                    };
                }

                @Override
                public void mousePressed(MouseEvent e)
                {
                    if (isMouseInPanel == true&&isUnderControl==true)
                    {
                        Toolkit toolkit = Toolkit.getDefaultToolkit();

                        // Get the screen size in pixels
                        Dimension screenSize = toolkit.getScreenSize();

                        int atitudeHeight = screenSize.height / heightResolutionOfChild;
                        int atitudewidth = screenSize.width / widthResolutionOfChild;
                        MouseOpetions key = new MouseOpetions(atitudeHeight * e.getY(), atitudewidth * e.getX(), mouseStatus.PRESSED, e.getModifiersEx());
                        con.sendMouseCommandOverSocket(key);
                    };
                }

                @Override
                public void mouseReleased(MouseEvent e)
                {
                    if (isMouseInPanel == true&&isUnderControl==true)
                    {
                        Toolkit toolkit = Toolkit.getDefaultToolkit();

                        // Get the screen size in pixels
                        Dimension screenSize = toolkit.getScreenSize();

                        int atitudeHeight = screenSize.height / heightResolutionOfChild;
                        int atitudewidth = screenSize.width / widthResolutionOfChild;
                        MouseOpetions key = new MouseOpetions(atitudeHeight * e.getY(), atitudewidth * e.getX(), mouseStatus.REALESED, e.getModifiersEx());
                        con.sendMouseCommandOverSocket(key);
                    };
                }

                @Override
                public void mouseEntered(MouseEvent e)
                {
                    System.out.println("mouse entered");
                    isMouseInPanel = true;
                }

                @Override
                public void mouseExited(MouseEvent e)
                {
                    System.out.println("mouse exited");
                    isMouseInPanel = false;
                }
            });

            // Create a main panel with a BorderLayout
            JPanel mainPanel = new JPanel(new BorderLayout());
            mainPanel.add(buttonPanel, BorderLayout.NORTH);
            mainPanel.add(imagePanel, BorderLayout.CENTER);

            frame.add(mainPanel);
            frame.setSize(800, 600);
            frame.setVisible(true);
            con.setImagePanel(imagePanel);
            }
        });
        thread.start();
        
    }

}
