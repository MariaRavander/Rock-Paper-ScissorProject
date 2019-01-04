package server.model;

import java.util.*;
import server.net.PlayerHandler;

public class Game {
    private final List<String> entries = Collections.synchronizedList(new ArrayList<>());
    private int noOfPlayers = 0;
    private int noOfChoices = 0;
    public int score = 0;
    private boolean running = false;
    
    PlayerHandler player1;
    PlayerHandler player2;
    PlayerHandler player3;
    String choice1;
    String choice2;
    String choice3;

    public void appendEntry(String msg) {
        entries.add(msg);
    }

    public String[] getGameStatus() {
        return entries.toArray(new String[0]);
    }

    public synchronized boolean prepareGame() {
        boolean startGame = false;
        noOfPlayers++;
        if(noOfPlayers == 3) {
            startGame = true;
            running = true;
            noOfPlayers = 0;
        }
        return startGame;
    }
    
    public boolean running() {
        return running;
    }
    
    public void endGame(){
        running = false;
        player1.playing = false;
        player2.playing = false;
        player3.playing = false;
    }
    

    public synchronized boolean playGame(String choice, PlayerHandler handler) {
        noOfChoices++;
        if(noOfChoices == 1) {
            player1 = handler;
            choice1 = choice;
            player1.madeChoice = true;
        }
        else if(noOfChoices == 2) {
            player2 = handler;
            choice2 = choice;
            player2.madeChoice = true;
        } else {
            player3 = handler;
            choice3 = choice;
            player3.madeChoice = true;
        }
        if(noOfChoices == 3) {
            calculateScore();
            noOfChoices = 0;
            return true;
        } else
            return false;
    }
    
    public String getChoices() {
        StringJoiner join = new StringJoiner("##");
        join.add(player1.getUsername());
        join.add(choice1);
        join.add(player2.getUsername());
        join.add(choice2);
        join.add(player3.getUsername());
        join.add(choice3);
       
        return join.toString();
    }
    
    public String gameResult() {
        StringJoiner join = new StringJoiner("##");
        join.add(player1.getUsername());
        join.add(player1.roundScore + "");
        join.add(player1.totalScore + "");
        join.add(player2.getUsername());
        join.add(player2.roundScore + "");
        join.add(player2.totalScore + "");
        join.add(player3.getUsername());
        join.add(player3.roundScore + "");
        join.add(player3.totalScore + "");
        
        player1.roundScore = 0;
        player2.roundScore = 0;
        player3.roundScore = 0;
        player1.madeChoice = false;
        player2.madeChoice = false;
        player3.madeChoice = false;
        return join.toString();
    }
    
    private void calculateScore() {
        switch (choice1) {
            case "paper":
                if(choice2.equals("rock")) {
                    player1.roundScore++;
                }
                if(choice3.equals("rock"))
                    player1.roundScore++;
                break;
            case "rock":
                if(choice2.equals("scissor"))
                    player1.roundScore++;
                if(choice3.equals("scissor"))
                    player1.roundScore++;
                break;
            default:
                if(choice2.equals("paper"))
                    player1.roundScore++;
                if(choice3.equals("paper"))
                    player1.roundScore++;
                break;
        }
        
        switch (choice2) {
            case "paper":
                if(choice1.equals("rock"))
                    player2.roundScore++;
                if(choice3.equals("rock"))
                    player2.roundScore++;
                break;
            case "rock":
                if(choice1.equals("scissor"))
                    player2.roundScore++;
                if(choice3.equals("scissor"))
                    player2.roundScore++;
                break;
            default:
                if(choice1.equals("paper"))
                    player2.roundScore++;
                if(choice3.equals("paper"))
                    player2.roundScore++;
                break;
        }
        
        switch (choice3) {
            case "paper":
                if(choice1.equals("rock"))
                    player3.roundScore++;
                if(choice2.equals("rock"))
                    player3.roundScore++;
                break;
            case "rock":
                if(choice1.equals("scissor"))
                    player3.roundScore++;
                if(choice2.equals("scissor"))
                    player3.roundScore++;
                break;
            default:
                if(choice1.equals("paper"))
                    player3.roundScore++;
                if(choice2.equals("paper"))
                    player3.roundScore++;
                break;
        }
        player1.totalScore = player1.totalScore + player1.roundScore;
        player2.totalScore = player2.totalScore + player2.roundScore;
        player3.totalScore = player3.totalScore + player3.roundScore;
    }   
}
