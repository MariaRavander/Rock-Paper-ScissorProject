package server.controller;

import server.model.Game;
import server.net.PlayerHandler;

public class Controller {
    private final Game game = new Game();
  
    public void appendToHistory(String msg) {
       game.appendEntry(msg);
    }
    
    public String[] getGameStatus() {
        return game.getGameStatus();
    }
    
    public boolean prepareGame() {
        return game.prepareGame();
    }
    
    public boolean sendChoice(String choice, PlayerHandler handler) {
        return game.playGame(choice, handler);
    }
    public boolean gameRunning() {
        return game.running();
    }
    
    public void endGame() {
        game.endGame();
    }
    
    public String getChoices() {
        return game.getChoices();
    }
    public String getResult() {
        return game.gameResult();
    }
}
