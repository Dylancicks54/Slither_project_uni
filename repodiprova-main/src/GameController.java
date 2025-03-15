import java.awt.*;
import java.awt.event.*;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class GameController implements Serializable {
    private static final long serialVersionUID = 1L;
    //private GameClient gameClient;
    private GameState gameState;
    private GameWindow gameWindow;

    public GameController(GameWindow gameWindow){
        this.gameState = GameState.getInstance();
        gameState.setController(this);
        this.gameWindow = gameWindow;
        gameState.addBot();

    }

//    public boolean isServerAvailable() {
//        return gameClient.isServerAvailable(); // Controlla se il server è attivo
//    }

    public void applyGameState(GameState state) {
        Iterator<Player> playerIterator = state.getPlayers().iterator();
        while (playerIterator.hasNext()) {
            Player player = playerIterator.next();
            Player updatedPlayer = state.getPlayerById(player.getId());
            if (updatedPlayer != null) {
                player.setPosition(updatedPlayer.getPosition());
            }
        }

        Iterator<Bot> botIterator = state.getBots().iterator();
        while (botIterator.hasNext()) {
            Bot bot = botIterator.next();
            for (Bot updatedBot : state.getBots()) {
                if (bot.equals(updatedBot)) {
                    bot.setPosition(updatedBot.getPosition());
                    break;
                }
            }
        }

        Iterator<Food> foodIterator = state.getFoodItems().iterator();
        while (foodIterator.hasNext()) {
            Food food = foodIterator.next();
            for (Food updatedFood : state.getFoodItems()) {
                if (food.equals(updatedFood)) {
                    food.setPosition(updatedFood.getPosition());
                    break;
                }
            }
        }
    }


    public void updateGameState() {
        gameState.updateGameState();
    }

    public List<Food> getFoodItems() {
        return gameState.getFoodItems();
    }

    public List<Bot> getBots() {
        return gameState.getBots();
    }

    public List<Player> getPlayers() {
        return gameState.getPlayers();
    }
    public GameState getGameState() {
        return gameState;
    }


}
