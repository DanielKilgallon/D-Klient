package project7;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;

/**
 * Talks to a server and uses Javax Swing to create a simple GUI.
 * 
 * @author Daniel Kilgallon
 */
public class Client extends JFrame
{

    private final Socket c;
    private final Scanner in;
    private final PrintWriter out;

    private final JTextArea textArea;

    public Client(Socket c) throws IOException
    {
        this.c = c;
        in = new Scanner(c.getInputStream());
        out = new PrintWriter(c.getOutputStream(), true);
        //thread that runs server input
        OutputThread ot = new OutputThread();

        textArea = new JTextArea("");
        textArea.setEditable(false);
        textArea.setLineWrap(true);
        textArea.setBorder(new LineBorder(Color.BLACK, 4));
        textArea.setPreferredSize(new Dimension(1000, 400));

        JScrollPane scroll = new JScrollPane(textArea);
        scroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);

        JTextField textfield = new JTextField("");
        textfield.setPreferredSize(new Dimension(400, 40));

        JButton send = new JButton("Send");
        send.setFocusable(false);
        send.setPreferredSize(new Dimension(80, 40));

        //will activate when "enter" is pressed or JButton
        ActionListener al = (ActionEvent ae)-> 
        {
                    String s = textfield.getText();
                    textfield.setText("");
                    out.println(s);
                    textArea.append("You: " + s + "\n");
        };
        send.addActionListener(al);
        textfield.addActionListener(al);

        this.setLayout(new GridLayout(2, 0));
        this.setTitle("JMessager");
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        this.setFocusable(false);

        this.setSize(new Dimension(600, 400));
        this.setLocationRelativeTo(null);
        this.setResizable(true);

        this.add(scroll);

        //JPanel for formatting
        JPanel inputPanel = new JPanel();
        inputPanel.setBorder(new EmptyBorder(60, 35, 10, 35));
        inputPanel.add(textfield);
        inputPanel.add(send);

        this.add(inputPanel);

        this.setVisible(true);
        this.setAlwaysOnTop(true);

        ot.start();
    }

    public static void main(String[] args) throws IOException
    {
        Client client = new Client(new Socket("csllc.bloomu.edu", 9001));
    }

    /**
     * Takes server input and adds to textArea
     */
    class OutputThread extends Thread
    {

        @Override
        public void run()
        {
            while (true)
            {
                String s = in.nextLine();
                //line to stop client remotely from server
                //no client can send this line to another
                if (s.equals("/"))
                    System.exit(0);
                textArea.append(s + "\n");
            }
        }
    }
}
