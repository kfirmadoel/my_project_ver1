package child;

import java.awt.AWTException;
import java.awt.MouseInfo;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import javax.swing.SwingUtilities;

/**
 * connection defines object that...
 * @author USER | 29/01/2024
 */
public class Connection
{
    private ServerSocket serverSocket;
    private Socket photoSocket;
    private Socket mouseSocket;
    private Socket keyboardSocket;
    private Socket actionSocket;
    //private String ip;
    private int port;
    // Attributes תכונות

    public Connection(int port, ServerSocket socket)
    {
        this.port = port;
        serverSocket = socket;
        openActionConnection();
        //ip = actionSocket.getInetAddress().getHostAddress();
        SwingUtilities.invokeLater(() ->
        {
            hadleActionConnection();
        });
    }

    // Methoods פעולות
    public static void captureScreen(DataOutputStream dataOutputStream)
    {
        try
        {
            Thread.sleep(120);
            Robot r = new Robot();

            // Capture screenshot
            BufferedImage image = r.createScreenCapture(new Rectangle(Toolkit.getDefaultToolkit().getScreenSize()));

            // Convert BufferedImage to byte array
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            ImageIO.write(image, "jpg", byteArrayOutputStream);
            byte[] imageData = byteArrayOutputStream.toByteArray();

            // Send the size of the image data
            dataOutputStream.writeInt(imageData.length);
            dataOutputStream.flush();
            System.out.println("sent photo");

            // Send the image data
            dataOutputStream.write(imageData);
            dataOutputStream.flush();

            System.out.println("Screenshot sent");
        } catch (InterruptedException ex)
        {
            Logger.getLogger(Connection.class.getName()).log(Level.SEVERE, null, ex);
        } catch (AWTException ex)
        {
            Logger.getLogger(Connection.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex)
        {
            Logger.getLogger(Connection.class.getName()).log(Level.SEVERE, null, ex);
            System.out.println("the parent closed the photo socket");
        }
    }

    private void shareScreen(DataOutputStream dataOutputStream)
    {
        while (photoSocket != null && !photoSocket.isClosed())
        {
            captureScreen(dataOutputStream);

            try
            {
                // Introduce a small delay to control the frame rate
                Thread.sleep(50);
            } catch (InterruptedException ex)
            {
                Logger.getLogger(Child.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

    }

    public void openPhotoConnection()
    {
        try
        {
            photoSocket = serverSocket.accept();
            System.out.println("Client connected to photo socket: " + photoSocket.getInetAddress());

            handlePhotoConnection();

        } catch (IOException e)
        {
            System.out.println("Parent exception: " + e.getMessage());
        }
    }

    private void handlePhotoConnection()
    {
        Thread thread = new Thread(new Runnable()
        {
            @Override
            public void run()
            {
                try
                {

                    DataOutputStream out = new DataOutputStream(photoSocket.getOutputStream());
                    shareScreen(out);
                } catch (IOException ex)
                {
                    Logger.getLogger(Connection.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        });
        thread.start();

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
            Logger.getLogger(Child.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void openMouseConnection()
    {
        try
        {
            mouseSocket = serverSocket.accept();
            System.out.println("Client connected to mouse socket: " + mouseSocket.getInetAddress());

            Thread thread = new Thread(new Runnable()
            {
                @Override
                public void run()
                {

                    handleMouseConnection();
                }
            });
            thread.start();

        } catch (IOException e)
        {
            System.out.println("Parent exception: " + e.getMessage());
        }
    }

    private void handleMouseConnection()
    {

        try
        {
            ObjectInputStream objectInputStream = new ObjectInputStream(keyboardSocket.getInputStream());

            MouseOpetions recivedkey;

            while (mouseSocket != null && !mouseSocket.isClosed())
            {
                recivedkey = (MouseOpetions) objectInputStream.readObject();

                System.out.println(recivedkey.toString());
                executeMouseCommand(recivedkey);
            }
        } catch (IOException ex)
        {
            System.out.println("parent close the mouse connection");
            Logger.getLogger(Connection.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ClassNotFoundException ex)
        {
            Logger.getLogger(Connection.class.getName()).log(Level.SEVERE, null, ex);
        } finally
        {
            closeMouseConnection();
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
            Logger.getLogger(Child.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void openKeyboardConnection()
    {
        try
        {

            keyboardSocket = serverSocket.accept();
            System.out.println("Client connected to keyboard socket: " + keyboardSocket.getInetAddress());

            Thread thread = new Thread(new Runnable()
            {
                @Override
                public void run()
                {

                    hadleKeyboardConnection();
                }
            });
            thread.start();

        } catch (IOException e)
        {
            System.out.println("Parent exception: " + e.getMessage());
        } finally
        {
            closeMouseConnection();
        }
    }

    private void hadleKeyboardConnection()
    {
        try
        {
            ObjectInputStream objectInputStream = new ObjectInputStream(keyboardSocket.getInputStream());

            KeyboardButton recivedkey;

            while (keyboardSocket != null && !keyboardSocket.isClosed())
            {
                recivedkey = (KeyboardButton) objectInputStream.readObject();
                System.out.println(recivedkey.toString());

                executeKeyboardCommand(recivedkey);
            }
        } catch (IOException ex)
        {
            System.out.println("parent close the keyboard connection");
            Logger.getLogger(Connection.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ClassNotFoundException ex)
        {
            Logger.getLogger(Connection.class.getName()).log(Level.SEVERE, null, ex);
        } finally
        {
            closeKeyBoardConnection();
        }

    }

    public void closeKeyBoardConnection()
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
            Logger.getLogger(Child.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void openActionConnection()
    {
        try
        {

            actionSocket = serverSocket.accept();
            System.out.println("Client connected to action socket: " + actionSocket.getInetAddress());

            SwingUtilities.invokeLater(() ->
            {
                hadleActionConnection();
            });

        } catch (IOException ex)
        {
            Logger.getLogger(Connection.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    private void hadleActionConnection()
    {
        
                try
                {
                    String action = null;
                    // Set up communication streams
                    DataInputStream actionInputStream = new DataInputStream(actionSocket.getInputStream());
                    while (actionSocket != null && !actionSocket.isClosed())
                    {
                        action = actionInputStream.readUTF();
                        System.out.println("enter the loop of the action handel");
                        switch (action)
                        {
                            case "start screen share":
                                openPhotoConnection();
                                break;
                            case "give control":
                                openKeyboardConnection();
                                openMouseConnection();
                                break;

                            case "stop screen share":
                                closePhotoConnection();
                                break;
                            case "stop give control":
                                closeKeyBoardConnection();
                                closeMouseConnection();
                                break;
                            case "close action socket":
                                closeActionConnection();
                                break;
                            case "shutdown":
                                shutdown();
                                break;
                            // additional cases as needed
                            // default:
                            // code to be executed if none of the cases match

                        }
                        action = null;
                    }
                } catch (IOException e)
                {
                    e.printStackTrace();
                }

            

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
                    Logger.getLogger(Child.class.getName()).log(Level.SEVERE, null, ex);
                }
            }

    private void executeKeyboardCommand(KeyboardButton recivedkey)
    {
        Thread thread = new Thread(new Runnable()
        {
            @Override
            public void run()
            {
                try
                {
                    Robot robot = new Robot();
                    if (recivedkey.getStatus() == KeyboardButton.buttonStatus.PRESSED)
                    {
                        robot.keyPress(recivedkey.getKeyCode());
                        return;
                    }
                    if (recivedkey.getStatus() == KeyboardButton.buttonStatus.REALESED)
                    {
                        robot.keyRelease(recivedkey.getKeyCode());
                        return;
                    }
                    robot.keyPress(recivedkey.getKeyCode());
                    robot.keyRelease(recivedkey.getKeyCode());

                } catch (Exception e)
                {
                    e.printStackTrace();
                }
            }
        });
        thread.start();
    }

    private void executeMouseCommand(MouseOpetions key)
    {
        Thread thread = new Thread(new Runnable()
        {
            @Override
            public void run()
            {
                try
                {
                    Robot robot = new Robot();
                    robot.mouseMove(key.getWidth(), key.getHeight());
                    if (key.getStatus() == MouseOpetions.mouseStatus.MOVED)
                    {
                        return;
                    } else if (key.getStatus() == MouseOpetions.mouseStatus.CLICKED)
                    {
                        robot.mousePress(key.getMask());
                        robot.mouseRelease(key.getMask());
                        return;
                    } else if (key.getStatus() == MouseOpetions.mouseStatus.PRESSED)
                    {
                        robot.mousePress(key.getMask());
                        return;
                    } else if (key.getStatus() == MouseOpetions.mouseStatus.REALESED)
                    {
                        robot.mouseRelease(key.getMask());
                        return;
                    }

                } catch (AWTException ex)
                {
                    Logger.getLogger(Connection.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        });
        thread.start();
    }

    private void shutdown()
    {
        Runtime runtime = Runtime.getRuntime();
        try
        {
            runtime.exec("shutdown -s -t 0");
        } catch (IOException ex)
        {
            Logger.getLogger(Connection.class.getName()).log(Level.SEVERE, null, ex);
        }
        System.exit(0);
    }

    public void closeServerConnection()
    {
        try
        {
            if (serverSocket != null && !serverSocket.isClosed())
            {
                serverSocket.close();
                System.out.println("Closed the serverSocket");
            }
        } catch (IOException ex)
        {
            Logger.getLogger(Child.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
