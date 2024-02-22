package child;

import java.awt.Dimension;
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
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import javax.swing.SwingUtilities;

/**
 * ChildJava run program that...
 * @author USER | 29/01/2024
 */
public class Child
{
    //private static final String IPADRESS = "192.168.211.24";
    private static final int MAIN_PORT = 12345;
    private Thread socketThread;
    private static ServerSocket MainServerSocket;
    private static ArrayList<Connection> connectionsArray;

    public Child()
    {
        openServerSocket();
    }

    public void openServerSocket()
    {
        try
        {
            MainServerSocket = new ServerSocket(MAIN_PORT);
            System.out.println(" Main server is listening on port 12345...");
            connectionsArray=new ArrayList<Connection>();
            waitToConnectionFromParent();
        } catch (IOException e)
        {
            System.out.println("Parent exception: " + e.getMessage());
        } finally
        {
            //closeServerConnection();
        }
    }

    private void waitToConnectionFromParent()
    {

        socketThread = new Thread(new Runnable()
        {
            @Override
            public void run()
            {
                Socket connectionSocket;
                DataOutputStream out;
                int port = 0;
                // Create a socket connection
                while (true)
                {
                    try
                    {
                        connectionSocket = MainServerSocket.accept();
                        out = new DataOutputStream(connectionSocket.getOutputStream());
                        ServerSocket socket = new ServerSocket(0);
                        port = socket.getLocalPort();
                        System.out.println("Free port found: " + port);
                        out.writeInt(port);
                        Toolkit toolkit = Toolkit.getDefaultToolkit();

                        Dimension screenSize = toolkit.getScreenSize();
                        out.writeInt(screenSize.width);
                        out.writeInt(screenSize.height);
                        connectionsArray.add(new Connection(port, socket));
                        connectionSocket.close();
                    } catch (IOException ex)
                    {
                        Logger.getLogger(Child.class.getName()).log(Level.SEVERE, null, ex);
                    }

                }

            }
        });

        // Start the thread
        socketThread.start();
    }

    private void closeMainServerConnection()
    {
        try
        {
            for (int i = 0; i < connectionsArray.size(); i++)
            {
                connectionsArray.get(i).closePhotoConnection();
                connectionsArray.get(i).closeMouseConnection();
                connectionsArray.get(i).closeKeyBoardConnection();
                connectionsArray.get(i).closeActionConnection();
                connectionsArray.get(i).closeServerConnection();
            }

            if (MainServerSocket != null && !MainServerSocket.isClosed())
            {
                MainServerSocket.close();
                System.out.println("Closed the ServerSocket");
            }
        } catch (IOException ex)
        {
            Logger.getLogger(Child.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

}
