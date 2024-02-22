package parent;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import javax.swing.SwingUtilities;

/**
 * Connection defines object that...
 * @author USER | 30/01/2024
 */
public class Connection
{
    // Attributes תכונות
    private CustomImagePanel imagePanel;
    private Socket photoSocket;
    private Socket mouseSocket;
    private Socket keyboardSocket;
    private Socket actionSocket;
    private String ip;
    private int port;
    // Methoods פעולות

    public Connection(int port)
    {
        this.port = port;
        this.ip = "192.168.211.29";
        openActionSocket();
    }

    public void openActionSocket()
    {
        try
        {
            actionSocket = new Socket(ip, port);
            System.out.println("action socket has opened");

        } catch (IOException ex)
        {
            Logger.getLogger(Connection.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void handleActionCommand(String action)
    {

        switch (action)
        {
            case "start screen share":
                sendActionOverActionSocket(action);
                openPhotoConnection();
                break;
            case "give control":
                sendActionOverActionSocket(action);
                openKeyboardConnection();
                openMouseConnection();
                break;

            case "stop screen share":
                closePhotoConnection();
                break;
            case "stop give control":
                closeKeyboardConnection();
                closeMouseConnection();
                break;
            case "close action socket":
                closeActionConnection();
                break;
            // additional cases as needed
            // default:
            // code to be executed if none of the cases match

        }

    }

    public void sendActionOverActionSocket(String action)
    {
        Thread thread = new Thread(new Runnable()
        {
            @Override
            public void run()
            {
                DataOutputStream dataOutputStream;
                try
                {
                    dataOutputStream = new DataOutputStream(actionSocket.getOutputStream());

                    // Send a string to the server
                    dataOutputStream.writeUTF(action);
                } catch (IOException ex)
                {
                    Logger.getLogger(Connection.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        });
        thread.start();

    }

    public void closeActionConnection()
    {
        try
        {
            if (actionSocket != null && !actionSocket.isClosed())
            {
                actionSocket.close();
                System.out.println("Closed the actionSocket");
            }

        } catch (IOException ex)
        {
            Logger.getLogger(Connection.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void closeKeyboardConnection()
    {
        try
        {
            if (keyboardSocket != null && !keyboardSocket.isClosed())
            {
                keyboardSocket.close();
                System.out.println("Closed the keyboardSocket");
            }

        } catch (IOException ex)
        {
            Logger.getLogger(Connection.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void closeMouseConnection()
    {
        try
        {
            if (mouseSocket != null && !mouseSocket.isClosed())
            {
                mouseSocket.close();
                System.out.println("Closed the mouseSocket");
            }

        } catch (IOException ex)
        {
            Logger.getLogger(Connection.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void closePhotoConnection()
    {
        try
        {
            if (photoSocket != null && !photoSocket.isClosed())
            {
                photoSocket.close();
                System.out.println("Closed the photoSocket");
            }
        } catch (IOException ex)
        {
            Logger.getLogger(Connection.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void openPhotoConnection()
    {
        try
        {

            photoSocket = new Socket(ip, port);
            System.out.println("Client connected to photo socket: " + photoSocket.getInetAddress());

            Thread thread = new Thread(new Runnable()
            {
                @Override
                public void run()
                {
                    handlePhotoConnection();
                }
            });
            thread.start();

        } catch (IOException e)
        {
            System.out.println("Parent exception: " + e.getMessage());
        }
    }

    private void handlePhotoConnection()
    {
        try
        {

            DataInputStream dataInputStream = new DataInputStream(photoSocket.getInputStream());
            BufferedImage image;
            int imageSize;
            while (photoSocket != null && !photoSocket.isClosed())
            {
                // Read the size of the incoming image
                imageSize = dataInputStream.readInt();

                // Read the image data
                byte[] imageData = new byte[imageSize];
                dataInputStream.readFully(imageData);

                // Convert the byte array to BufferedImage
                image = ImageIO.read(new ByteArrayInputStream(imageData));

                if (image != null)
                {
                    imagePanel.setImage(image);
                    imagePanel.repaint();
                }
            }

        } catch (IOException e)
        {
            System.out.println("Parent exception: " + e.getMessage());
        } finally
        {
            closePhotoConnection();
        }
    }

    public void openKeyboardConnection()
    {
        try
        {

            keyboardSocket = new Socket(ip, port);
            System.out.println("parent connected to keyboard socket: " + keyboardSocket.getInetAddress());

        } catch (IOException e)
        {
            System.out.println("child exception: " + e.getMessage());
        }
    }

    public void openMouseConnection()
    {
        try
        {

            mouseSocket = new Socket(ip, port);
            System.out.println("parent connected to mouse socket: " + mouseSocket.getInetAddress());

        } catch (IOException e)
        {
            System.out.println("child exception: " + e.getMessage());
        }
    }

    public void setImagePanel(CustomImagePanel imagePanel)
    {
        this.imagePanel = imagePanel;
    }

    public void sendKeyboardCommandOverSocket(KeyboardButton key)
    {
        Thread thread = new Thread(new Runnable()
        {
            @Override
            public void run()
            {
                try
                {
                    ObjectOutputStream objectOutputStream = new ObjectOutputStream(keyboardSocket.getOutputStream());
                    System.out.println(key.toString());
                    objectOutputStream.writeObject(key);
                } catch (IOException ex)
                {
                    Logger.getLogger(Connection.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        });
        thread.start();
    }

    public void sendMouseCommandOverSocket(MouseOpetions key)
    {
        Thread thread = new Thread(new Runnable()
        {
            @Override
            public void run()
            {
                try
                {
                    ObjectOutputStream objectOutputStream = new ObjectOutputStream(mouseSocket.getOutputStream());
                    System.out.println(key.toString());
                    objectOutputStream.writeObject(key);
                    System.out.println("only-------------------------");
                } catch (IOException ex)
                {
                    Logger.getLogger(Connection.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        });
        thread.start();
    }

}
