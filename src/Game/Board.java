package Game;

import java.awt.*;
import Game.SLGame.*;
import javax.print.DocFlavor;
import javax.swing.*;
import javax.swing.text.BadLocationException;
import java.awt.event.*;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class Board extends JPanel implements Runnable, ActionListener
{
    static Scanner scan = new Scanner(System.in);
    private static JFrame frame = new JFrame("Snakes And Ladders");
    private int XMARGIN = 20;
    private int YMARGIN = 20;
    private Player players[];
    private SnakePlayer sPlayers[];
    private int pCount;

    private Snake[] ss = new Snake[10];
    private Ladder[]ls = new Ladder[10];
    int snakesCount = 0;
    int laddersCount = 0;

    private JButton Login = new JButton("Login");
    private JButton Logout = new JButton("Logout");
    public static JTextField inGameTerminalInput;
    public static JTextArea inGameTerminalOutput;
    private JScrollPane terminalOutputScroll;

    public final static String newline = "\n";
    public static String textEntered = "";

    public static InputStream inputStream = null;

    public int getSnakePlayers(){
        return sPlayers.length;
    }

    // the standard positions of snakes and ladders
    public void setup()
    {
        /*
        add(new Ladder(12,49));
        add(new Ladder(34,51));
        add(new Ladder(53,79));

        add(new Snake(75,42));
        add(new Snake(39,8));
        add(new Snake(95,21));
        add(new Snake(42,19));*/
    }

    // allows the number and positions of snakes and ladders to be customized
    public void customize()
    {

        snakesCount = 0;
        laddersCount = 0;
        repaint();
        int numSnakes;
        int numLadders;

        do {
            System.out.print("Enter number of snakes : 1..10 : ");
            numSnakes = scan.nextInt();
            System.out.print("Enter number of ladders : 1..10 : ");
            numLadders = scan.nextInt();
        } while ( numSnakes < 1 || numSnakes > 10 || numLadders < 1 && numLadders > 10);
        for (int i=0; i<numSnakes; i++)
        {
            int head,tail;
            do {
                System.out.print("Enter Head pos of snake " + (i+1) + " : ");
                head = scan.nextInt();
                System.out.print("Enter Tail pos of snake " + (i+1) + " : ");
                tail = scan.nextInt();
                if ( tail >= head)
                    System.out.println("Head must be higher than the tail. ReEnter");
            } while ( head <= tail);
            add(new Snake(head,tail));
        }
        for (int i=0; i<numLadders; i++)
        {
            int bottom,top;
            do {
                System.out.print("Enter Bottom pos of ladder " + (i+1) + " : ");
                bottom = scan.nextInt();
                System.out.print("Enter Top pos of ladder " + (i+1) + " : ");
                top = scan.nextInt();
                if ( bottom >= top)
                    System.out.println("Top must be higher than the Bottom. ReEnter");
            } while ( top <= bottom);
            add(new Ladder(bottom,top));

        }
    }

    // Computes the new position taking into account the posiiions of the snakes and ladders
    public int newPos(int pos, int cPlayer)
    {
        int val = pos;

        for (int i=0; i<laddersCount; i++)
            if ( pos == ls[i].getBottom() )
                val = ls[i].getTop();

        for (int i=0; i<snakesCount; i++)
            if ( pos == ss[i].getHead() )
                val = ss[i].getTail();

        if ( val < pos)
        {
            System.out.println("You are bitten by a snake. Press 1 to continue");
            if(players[cPlayer].getSnakeEscapePoints() != 0) {
                players[cPlayer].decrementPoints();
            }
            else if(players[cPlayer].getSnakeEscapePoints() == 0){
                sPlayers[getSnakePlayers()] = new SnakePlayer(sPlayers.length);
                add(sPlayers);
            }
            scan.nextInt();
        }
        else if ( val > pos)
        {
            System.out.println("You are going up the ladder. Press 1 to continue");
            if(players[cPlayer].getSnakeEscapePoints() < 4) {
                players[cPlayer].incrementPoints();
            }
            scan.nextInt();
        }
        return val;
    }

    //Used to add a snake
    public void add(Snake s)
    {
        if ( snakesCount < 10)
        {
            ss[snakesCount] = s;
            snakesCount++;
        }
    }

    //Used to add a ladder
    public void add(Ladder l)
    {
        if ( laddersCount < 10)
        {
            ls[laddersCount] = l;
            laddersCount++;
        }
    }

    public static void terminalOutput(String outputString){
        final int maxLineLength = 26;
        int numLinesToRemove = inGameTerminalOutput.getLineCount() - maxLineLength;

        inGameTerminalOutput.append(outputString + newline);
        if (numLinesToRemove > 0) {
            try {
                int posOfLastLineToRemove = inGameTerminalOutput.getLineEndOffset(numLinesToRemove - 1);
                inGameTerminalOutput.replaceRange("", 0, posOfLastLineToRemove);
            } catch (BadLocationException ex) {
                ex.printStackTrace();
            }
        }

    }

    public void actionPerformed(ActionEvent e) {
        final int maxLineLength = 26;
        int inPtr = 0;
        char ch;
        int numLinesToRemove = inGameTerminalOutput.getLineCount() - maxLineLength;
        textEntered = inGameTerminalInput.getText();

        if(!textEntered.equals("") && !textEntered.equals(" ")) {
            inGameTerminalOutput.append(textEntered + newline);

            if(numLinesToRemove > 0)
            {
                try
                {
                    int posOfLastLineToRemove= inGameTerminalOutput.getLineEndOffset(numLinesToRemove - 1);
                    inGameTerminalOutput.replaceRange("",0,posOfLastLineToRemove);
                } catch (BadLocationException ex) {
                    ex.printStackTrace();
                }
            }

            inGameTerminalInput.setText("");
            inGameTerminalOutput.setCaretPosition(inGameTerminalOutput.getDocument().getLength());
        }
        else{
            terminalOutput("> Please Enter Text");
        }

    }

    //Creates new frame called board.
    public Board()
    {
        Container contentPane = frame.getContentPane();
        contentPane.setLayout(new BorderLayout());

        frame.getContentPane().add(this,BorderLayout.CENTER);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(700,600);
        frame.setVisible(true);
        frame.setResizable(false);
        setup();
        new Thread(this).start();

        //Used for attempt at in game input -> didnt work, but code may still be relevant
        /*inGameTerminalInput = new JTextField(20);
        inGameTerminalInput.addActionListener(this);
        inGameTerminalInput.setBounds(427,60,266,20);
        this.add(inGameTerminalInput);
        frame.addWindowListener( new WindowAdapter() {
            public void windowOpened( WindowEvent e ){
                inGameTerminalInput.requestFocus();
            }
        });*/

        inGameTerminalOutput = new JTextArea(5,20);
        inGameTerminalOutput.setEditable(false);
        inGameTerminalOutput.setFont(new Font("Menlo",Font.PLAIN,12));

        terminalOutputScroll = new JScrollPane(inGameTerminalOutput);
        terminalOutputScroll.setBounds(430,60,260,400);
        this.add(terminalOutputScroll);

        Login.setBounds(430,10,120,45);
        this.add(Login);

        Logout.setBounds(570,10,120,45);
        this.add(Logout);

        Login.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (Login.isEnabled()) {
                    LoginScreen throwLogin = new LoginScreen();
                }
            }
        });
    }


    public void add(Player players[], int pCount)
    {
        this.players = players;
        this.pCount = pCount;
    }

    public void add(SnakePlayer snakePlayer[]){
        this.sPlayers = snakePlayer;
    }

    // This method is used to wiggle the snake
    // The timing can be changed by varying the sleep time
    public void run()
    {
        double inc = 0.05;
        while (true)
        {
            try {
                Thread.sleep(200); //THIS NUMBER NEEDS TO BE ABOVE 200 TO KEEP DICE ONBOARD!!!!!!
            }
            catch (Exception e) {}
            Draw.factor += inc;
            if (Draw.factor > 0.5 || Draw.factor < -0.5)
                inc = -inc;
            repaint();
        }
    }

    // this method is called in response to repaint
    public void paintComponent(Graphics g) {

        super.paintComponent(g);
        for (int i=0; i<10; i++)
            for (int j=0; j<10; j++){
                if ((i+j)%2 == 0)
                    g.setColor(Color.YELLOW);
                else
                    g.setColor(Color.ORANGE);
                g.fillRect(XMARGIN + 40*i,YMARGIN+40*j, 40,40);
            }

        g.setColor(Color.GRAY);
        g.fillRect(20,440,400,160);

        g.setColor(Color.BLACK);
        for ( int i=0; i<100; i++)
            if ( (i/10) % 2 == 0 )
                g.drawString("" + (i+1),XMARGIN + 5 + i%10 * 40 ,YMARGIN -5 + 400 - i/10 * 40);
            else
                g.drawString("" + (i+1),XMARGIN  + 370 - (i%10 * 40) ,YMARGIN - 5 + 400 - i/10 * 40);

        for (int i=0; i<snakesCount; i++)
            ss[i].draw(g);
        for (int i=0; i<laddersCount; i++)
            ls[i].draw(g);
        for (int i=0; i<pCount; i++)
            players[i].draw(g);
    }
}




