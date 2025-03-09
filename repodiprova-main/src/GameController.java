import java.awt.*;
import java.awt.event.*;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class GameController implements Serializable {
    private static final long serialVersionUID = 1L;
    private GameState gameState;
    private List<Player> players;
    private List<Bot> bots;
    private List<Food> foodItems;
    private List<Entity> entities;

    public GameController(GameState gameState){
        this.gameState = gameState;
        this.players = gameState.getPlayers();
        this.bots = gameState.getBots();
        this.foodItems = gameState.getFoodItems();
        this.entities = gameState.getEntities();
    }

    public void applyGameState(GameState state) {
        Iterator<Player> playerIterator = players.iterator();
        while (playerIterator.hasNext()) {
            Player player = playerIterator.next();
            Player updatedPlayer = state.getPlayerById(player.getId());
            if (updatedPlayer != null) {
                player.setPosition(updatedPlayer.getPosition());
            }
        }

        Iterator<Bot> botIterator = bots.iterator();
        while (botIterator.hasNext()) {
            Bot bot = botIterator.next();
            for (Bot updatedBot : state.getBots()) {
                if (bot.equals(updatedBot)) {
                    bot.setPosition(updatedBot.getPosition());
                    break;
                }
            }
        }

        Iterator<Food> foodIterator = foodItems.iterator();
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
        return foodItems;
    }

    public List<Bot> getBots() {
        return bots;
    }

    public List<Player> getPlayers() {
        return players;
    }


}
