package project7;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Scanner;
import java.util.Set;

/**
 * A server that reads from a client and lets them chat together
 *
 * @author Daniel Kilgallon
 */
public class Server
{

    //contains all connected clients
    private static final HashMap LIST = new HashMap();

    public static void main(String[] args) throws IOException
    {
        ServerSocket ss = new ServerSocket(1234);

        //continually connects clients and starts a thread for each
        while (true)
        {
            Socket client = ss.accept();
            new ClientThread(client).start();
        }
    }

    /**
     * Lets user message other users through a static hash map
     */
    private static class ClientThread extends Thread
    {

        private final Socket client;
        private final Scanner in;
        private final PrintWriter out;
        private String name;

        /**
         * Explicit Constructor
         *
         * @param c Client to start thread with
         */
        public ClientThread(Socket c) throws IOException
        {
            client = c;
            in = new Scanner(c.getInputStream());
            out = new PrintWriter(c.getOutputStream(), true);
            name = null;
        }

        /**
         * Returns a Socket of this thread's client.
         *
         * @return a Socket Object
         */
        public Socket getClient()
        {
            return client;
        }

        /**
         * Thread method, called when program uses thread.start()
         */
        @Override
        public void run()
        {
            //everytime method is called, checks to make sure client has name set
            if (name == null)
            {
                while (true)
                {
                    try
                    {
                        out.println("Please input your name: ");
                        name = in.nextLine();
                        //will catch if stream closes
                    } catch (NoSuchElementException e)
                    {
                        System.out.println(name + "'s client closed unexpectedly");
                        //untypable name
                        name = "//";
                        return;
                    }
                    //checks if name is in correct parameters
                    if (name.length() > 10 || name.length() <= 2)
                        out.println("Incorrect name length (3-10)");
                    else
                        break;
                }
                LIST.put(name, this.getClient());
                out.println("Welcome! Use 'help/' for more info.");
            }
            //server output to show when someone connected.
            System.out.println(client.getInetAddress() + " connected as " + name);
            while (true)
            {
                String s = "";
                try
                {
                    //gets input, outputs to server console
                    s = in.nextLine();
                    System.out.println(name + ": " + s);
                    //will catch if stream closes
                } catch (NoSuchElementException e)
                {
                    System.out.println(name + "'s client closed unexpectedly");
                }
                //checks if user inputted correct format
                if (s.contains("/"))
                {
                    int spliter = s.indexOf("/");
                    String recipient = s.substring(0, spliter);
                    String message = s.substring(spliter + 1);
                    //all known commands
                    switch (recipient)
                    {
                        case "help":
                            String msg = "Welcome to Danny's JMessage Server!\n"
                                    + "A few commands to remember:\n"
                                    + "   help/ --shows this prompt(obviously)\n"
                                    + "   users/ --shows all currently connected users\n"
                                    + "   <Username>/ --sends message to connected user\n"
                                    + "   all/ --sends message to all connected users\n";
                            out.println(msg);
                            break;
                        case "users":
                            Set set = LIST.keySet();
                            out.println(set.toString());
                            break;
                        case "all":
                            MessageAll(name, message);
                            break;
                        //default: tries to send message
                        default:
                            try
                            {
                                Socket partnerClient = (Socket) LIST.get(recipient);
                                PrintWriter partnerOut = new PrintWriter(partnerClient.getOutputStream(), true);
                                partnerOut.println(this.name + ": " + message);
                                //will catch if user not found or io problem
                            } catch (IOException | NullPointerException ex)
                            {
                                out.println("That is not a current user or a command.");
                            }
                    }

                }
                if (!in.hasNextLine())
                    break;
            }
            //after while loop ends, messages all users who left
            MessageAll(name, "has left");
            in.close();
            out.close();
        }

        /**
         * Sends message to all users.
         *
         * @param name who is sending message
         * @param msg message to be sent
         */
        private void MessageAll(String name, String msg)
        {
            Set allUsers = LIST.keySet();
            Iterator<Socket> i = allUsers.iterator();
            while (i.hasNext())
            {
                Socket temp = null;
                try
                {
                    temp = i.next();
                    Socket aClient = (Socket) LIST.get(temp);
                    PrintWriter aClientOut = new PrintWriter(aClient.getOutputStream(), true);
                    //formats outputted message
                    aClientOut.println(name + "@all: " + msg);
                //will catch if user not found or io problem
                } catch (IOException | NullPointerException ex)
                {
                    //removes user since there is an io problem or they disconnected
                    LIST.remove(temp);
                }
            }
        }
    }
}
