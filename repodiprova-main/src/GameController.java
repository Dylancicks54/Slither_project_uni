import java.awt.*;
import java.awt.event.*;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class GameController implements Serializable {
    private static final long serialVersionUID = 1L;
    private List<Player> players;
    private List<Bot> bots;
    private List<Food> foodItems;

    public GameController(GameState gameState){
        this.players = new ArrayList<>(gameState.getPlayers());
        this.bots = new ArrayList<>(gameState.getBots());
        this.foodItems = new ArrayList<>(gameState.getFoodItems());
    }

    public void applyGameState(GameState state) {
        for (Player player : players) {
            Player updatedPlayer = state.getPlayerById(player.getId());
            if (updatedPlayer != null) {
                player.setPosition(updatedPlayer.getPosition());
            }
        }
        for (Bot bot : bots) {
            for (Bot updatedBot : state.getBots()) {
                if (bot.equals(updatedBot)) {
                    bot.setPosition(updatedBot.getPosition());
                    break;
                }
            }
        }
        for (Food food : foodItems) {
            for (Food updatedFood : state.getFoodItems()) {
                if (food.equals(updatedFood)) {
                    food.setPosition(updatedFood.getPosition());
                    break;
                }
            }
        }
    }
}
