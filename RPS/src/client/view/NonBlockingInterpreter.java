package client.view;

import java.net.InetSocketAddress;
import client.net.ServerConnection;
import java.util.Scanner;
import client.net.CommunicationListener;
import common.MessageException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Reads and interprets user commands. The command interpreter will run in a separate thread, which
 * is started by calling the <code>start</code> method. Commands are executed in a thread pool, a
 * new prompt will be displayed as soon as a command is submitted to the pool, without waiting for
 * command execution to complete.
 */
public class NonBlockingInterpreter implements Runnable {
    private static final String PROMPT = "> ";
    private final Scanner console = new Scanner(System.in);
    private boolean receivingCmds = false;
    private ServerConnection server;
    private final ThreadSafeStdOut outMgr = new ThreadSafeStdOut();

    /**
     * Starts the interpreter. The interpreter will be waiting for user input when this method
     * returns. Calling <code>start</code> on an interpreter that is already started has no effect.
     */
    public void start() {
        if (receivingCmds) {
            return;
        }
        receivingCmds = true;
        server = new ServerConnection();
        new Thread(this).start();
    }

    /**
     * Interprets and performs user commands.
     */
    @Override
    public void run() {
        while (receivingCmds) {
            try {
                CmdLine cmdLine = new CmdLine(readNextLine());
                switch (cmdLine.getCmd()) {
                    case QUIT:
                        receivingCmds = false;
                        server.disconnect();
                        break;
                    case CONNECT:
                        server.addCommunicationListener(new ConsoleOutput());
                        server.connect(cmdLine.getParameter(0),
                                      Integer.parseInt(cmdLine.getParameter(1)));
                        break;
                    case USER:
                        server.sendUsername(cmdLine.getParameter(0));
                        break;
                    case JOIN:
                        server.sendJoin();
                        break;
                    case PLAY:
                        String choice = cmdLine.getParameter(0);
                        if(choice.equalsIgnoreCase("rock") || choice.equalsIgnoreCase("paper") || choice.equalsIgnoreCase("scissor"))
                            server.sendPlay(choice);
                        else 
                           outMgr.println("> Not a valid choice, play again!");
                        break;
                    default:
                        outMgr.println("> No such command!");
                }
            } catch (NullPointerException npe) {
                outMgr.println("> To few arguments in request");
            } catch (Exception e) {
                outMgr.println("> Operation failed, try again!");
            }
        }
    }

    private String readNextLine() {
        outMgr.print(PROMPT);
        String s = null;
        return console.nextLine();
    }

    private class ConsoleOutput implements CommunicationListener {
        
        @Override 
        public void recvdMsg(String msg){
            printToConsole(msg);
        }
        
        @Override
        public void connected(InetSocketAddress serverAddress){
            printToConsoleConnect("Connected to " + serverAddress.getHostName() + ":" + serverAddress.getPort());
        }
        
        @Override
        public void disconnected(){
            printToConsoleConnect("Disconnected from server.");
        }
        
        private void printToConsole(String msg) throws MessageException {
            String[] info = msg.split("##");
            try {
                if(info[1].compareTo("WAITCONNECT") == 0)
                    outMgr.println("Waiting for players to join game..");
                else if(info[1].compareTo("WAITROUND") == 0){
                    try {
                        Thread.sleep(400);
                    } catch (InterruptedException ex) {
                        Logger.getLogger(NonBlockingInterpreter.class.getName()).log(Level.SEVERE, null, ex);
                    }
                    outMgr.println("Waiting for other players to make their choice..");    
                }

                else if(info[1].compareTo("ALREADYJOINED") == 0)
                    outMgr.println("Already joined game, you cannot join again!");
                else if(info[1].compareTo("WAITJOIN") == 0)
                    outMgr.println("Game already strarted, wait to join next round, be fast!");
                else if(info[1].compareTo("PLAYERCHOICE") == 0)
                    outMgr.println(info[2] + " has made a choice.");
                else if(info[1].compareTo("PLAYBLOCKED") == 0)
                    outMgr.println("You have already made a choice.");
                else if(info[1].compareTo("PLAYERBLOCKED") == 0)
                    outMgr.println("You are not in the game and can not play, join or wait for next round.");
                else if(info[1].compareTo("CHOICES") == 0)
                    outMgr.println("\n****************CHOICES**************** \n> " +
                                   info[2] + " picked: " + info[3] + "\n> " +
                                   info[4] + " picked: " + info[5] + "\n> " +
                                   info[6] + " picked: " + info[7] + "\n");
                else if(info[1].compareTo("RESULT") == 0)
                    outMgr.println("*************SCOREBOARD************** \n" +
                                   "> The result for: " + info[2] + " round score: " + info[3] + " total score: " + info[4] + "\n" +
                                   "> The result for: " + info[5] + " round score: " + info[6] + " total score: " + info[7] + "\n" +
                                   "> The result for: " + info[8] + " round score: " + info[9] + " total score: " + info[10] +"\n" + 
                                   "> You can join another round. \n");
                else if (info[1].compareTo("USER") == 0)
                    outMgr.println(info[2] + " has joined the server.");

                else if(info[1].compareTo("DISCONNECT") == 0)
                     outMgr.println(info[2] + " has left the game.");
                else if(info[1].compareTo("NEWGAME") == 0)
                    outMgr.println("New game has started, Please choose to play rock, paper or scissor");
                else if(info[1].compareTo("LOSTDATA") == 0)
                    outMgr.println("Message from server was lost.");
            } catch(MessageException me) {
                outMgr.println("Message from server could not be handled properly.");
            }
            outMgr.print(PROMPT);       
        }
        
        private void printToConsoleConnect(String msg) {
           outMgr.println(msg);
           outMgr.print(PROMPT);
        }
    }
}
