/*import java.util.ArrayList;
import java.util.List;

public class Game {
    private List<Player> players = new ArrayList<>();
    private List<Food> foodItems = new ArrayList<>();
    private List<Bot> bots = new ArrayList<>();
    private GameState gameState;

    public Game() {
        gameState = new GameState(players, bots, foodItems);
    }

    public void startGame() {
        System.out.println("Game started!");
        // Populate initial food items
        for (int i = 0; i < 100; i++) {
            foodItems.add(new Food(new Vector2D(Math.random() * 500, Math.random() * 500), (int) (Math.random() * 5)));
        }
    }

    public void stopGame() {
        System.out.println("Game stopped!");
    }

    public void updateGameState() {
        gameState.updateGameState();
    }

    public void addPlayer(Player player) {
        players.add(player);
    }

    public void removePlayer(Player player) {
        players.remove(player);
    }
}
*/