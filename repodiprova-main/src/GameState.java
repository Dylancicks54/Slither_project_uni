import java.awt.*;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class GameState {
    private List<Entity> entities;
    private List<Player> players;
    private List<Bot> bots;
    private List<Food> foodItems;
    public static final int MAP_WIDTH = 5000;
    public static final int MAP_HEIGHT = 5000;



    public GameState() {
        this.players = new CopyOnWriteArrayList<>();  // Usa CopyOnWriteArrayList
        this.bots = new CopyOnWriteArrayList<>();     // Usa CopyOnWriteArrayList
        this.foodItems = new CopyOnWriteArrayList<>(); // Usa CopyOnWriteArrayList
        this.entities = new CopyOnWriteArrayList<>(); // Usa CopyOnWriteArrayList
    }

    public void addPlayer(Player player) {
        players.add(player);
    }

    public void addBot() {
        for(int i=0; i<10; i++) {
            bots.add(Bot.createBot(entities,this));
        }
    }
    public void removeFood(Food food) {
        foodItems.remove(food);
    }


    public void updateGameState() {
        for (Player player : players) {
            player.update();
        }
        for (Bot bot : bots) {
            bot.update();
        }
        checkCollisions();
        respawnFood();
    }

    public void aggiornaLista(){

        entities.addAll(players);
        entities.addAll(bots);
        entities.addAll(foodItems);
    }

    public void checkCollisions() {
        List<Bot> botsToAdd = new ArrayList<>();
        List<Bot> botsToRemove = new ArrayList<>();
        List<Player> playersToKill = new ArrayList<>();
        List<Food> foodToRemove = new ArrayList<>(); // Lista per raccogliere il cibo da rimuovere

        aggiornaLista();

        // Gestione delle collisioni tra player e cibo
        for (Player player : players) {
            for (Food food : foodItems) {
                if (player.collidesWith(food)) {
                    player.grow();
                    foodToRemove.add(food);
                }
            }
        }

        // Gestione delle collisioni tra bot e cibo
        for (Bot bot : bots) {
            for (Food food : foodItems) {
                if (bot.collidesWith(food)) {
                    bot.grow();
                    foodToRemove.add(food);
                }
            }
        }

        // Gestione delle collisioni tra bot e altri bot
        for (Bot bot : bots) {
            for (Bot otherBot : bots) {
                if (bot != otherBot && bot.collidesWith(otherBot)) {
                    // Trasformiamo ogni segmento del bot morto in cibo
                    for (Segment segment : bot.getBodySegments()) {
                        foodItems.add(createFoodFromSegment(segment)); // Crea cibo da ogni segmento
                    }
                    botsToRemove.add(bot);
                    botsToAdd.add(Bot.createBot(entities, this));
                    break;
                }
            }
        }

        // Gestione delle collisioni tra bot e segmenti del corpo dei player
        for (Bot bot : bots) {
            for (Player player : players) {
                for (Segment segment : player.getBodySegments()) {
                    if (checkCollisionSegmentBot(bot, segment)) {
                        // Trasformiamo ogni segmento del bot morto in cibo
                        for (Segment segmentBot : bot.getBodySegments()) {
                            foodItems.add(createFoodFromSegment(segmentBot)); // Crea cibo da ogni segmento
                        }
                        botsToRemove.add(bot);
                        botsToAdd.add(Bot.createBot(entities, this));
                        break;
                    }
                }
            }
        }

        // Gestione delle collisioni tra player e altri player
        for (Player player : players) {
            for (Player otherPlayer : players) {
                if (player != otherPlayer && player.collidesWith(otherPlayer)) {
                    playersToKill.add(player);
                    player.setAlive(false);
                }
            }
        }

        // Gestione delle collisioni tra player e segmenti del corpo dei player
        for (Player player : players) {
            for (Player otherPlayer : players) {
                if (player != otherPlayer) {
                    for (Segment segment : otherPlayer.getBodySegments()) {
                        if (checkCollisionSegmentPlayer(player, segment)) {
                            playersToKill.add(player);
                            player.setAlive(false);
                            break;
                        }
                    }
                }
            }
        }

        // Gestione delle collisioni tra player e bot
        for (Player player : players) {
            for (Bot bot : bots) {
                if (player.collidesWith(bot)) {
                    playersToKill.add(player);
                    break;
                }
            }
        }
        for (Bot bot : bots) {
            List<Bot> otherBots = new ArrayList<>(bots);
            for (Bot otherBot : otherBots) {
                for (Segment segment : otherBot.getBodySegments()) {
                    if (bot!= otherBot && checkCollisionSegmentBot(bot, segment)) {
                        for (Segment segmentBot : bot.getBodySegments()) {
                            foodItems.add(createFoodFromSegment(segmentBot)); // Crea cibo da ogni segmento
                        }
                        botsToRemove.add(bot);
                        botsToAdd.add(Bot.createBot(entities, this));
                        break;
                    }
                }
            }
        }

        // Gestione delle collisioni tra player e segmenti del corpo dei bot
        for (Player player : players) {
            for (Bot bot : bots) {
                for (Segment segment : bot.getBodySegments()) {
                    if (checkCollisionSegmentPlayer(player, segment)) {
                        playersToKill.add(player);
                        break;
                    }
                }
            }
        }

        // Eliminazione dei player morti
        for (Player player : playersToKill) {
            player.setAlive(false);
            System.out.println("Player " + player.getId() + " is dead! Press 'R' to respawn.");
        }

        // Rimuoviamo i bot morti e aggiungiamo quelli nuovi
        bots.removeAll(botsToRemove);
        bots.addAll(botsToAdd);

        // Rimuoviamo il cibo mangiato
        foodItems.removeAll(foodToRemove);
    }

    // Metodo per creare cibo da un segmento
    private Food createFoodFromSegment(Segment segment) {
        // Crea un oggetto Food basato sulla posizione del segmento
        Food food = new Food(segment.getPosition(), 10); // Usa la posizione del segmento per il cibo
        return food;
    }


    private boolean checkCollisionSegmentBot(Bot bot, Segment segment) {
        double distance = bot.getPosition().distanceTo(segment.getPosition());
        double collisionDistance = bot.getSize() / 2.0 + segment.getSize() / 2.0;
        return distance < collisionDistance;
    }

    public void setPlayers(List<Player> players) {
        this.players = players;
    }

    public void setFoodItems(List<Food> foodItems) {
        this.foodItems = foodItems;
    }

    public void setBots(List<Bot> bots) {
        this.bots = bots;
    }

    private boolean checkCollisionSegmentPlayer(Player player, Segment segment) {
        double distance = player.getPosition().distanceTo(segment.getPosition());
        double collisionDistance = player.getSize() / 2.0 + segment.getSize() / 2.0;
        return distance < collisionDistance;
    }

    private void respawnFood() {
        final int MAX_FOOD_ITEMS = 500;
        while (foodItems.size() < MAX_FOOD_ITEMS) {
            foodItems.add(new Food(new Vector2D(Math.random() * 5000, Math.random() * 5000), 10));
        }
    }

    public List<Player> getPlayers() { return players; }
    public List<Bot> getBots() { return bots; }
    public List<Food> getFoodItems() { return foodItems; }

    public List<Entity> getEntities() {
        List<Entity> allEntities = new ArrayList<>();
        allEntities.addAll(players);
        allEntities.addAll(bots);
        allEntities.addAll(foodItems);
        return allEntities;
    }

    public Player getPlayerById(String playerId) {
        for (Player player : players) {
            if (player.getId().equals(playerId)) {
                return player;
            }
        }
        return null; // Se non trovato, ritorna null
    }
}