import java.awt.*;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class GameState {
    private List<Entity> entities;
    private List<Player> players;
    private List<Bot> bots;
    private List<Food> foodItems;
    public static final int MAP_WIDTH = 5000;
    public static final int MAP_HEIGHT = 5000;



    public GameState() {
        this.players = new ArrayList<>();
        this.bots = new ArrayList<>();
        this.foodItems = new ArrayList<>();
        this.entities = new ArrayList<>();
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

    public void checkCollisions() {
        List<Bot> botsToAdd = new ArrayList<>();
        List<Bot> botsToRemove = new ArrayList<>();
        List<Player> playersToKill = new ArrayList<>();
        entities.addAll(players);
        entities.addAll(bots);
        entities.addAll(foodItems);

        for (Player player : players) {
            Iterator<Food> foodIterator = foodItems.iterator();
            while (foodIterator.hasNext()) {
                Food food = foodIterator.next();
                if (player.collidesWith(food)) {
                    player.grow();
                    foodIterator.remove();
                }
            }
        }

        // Collisioni tra bot e cibo
        for (Bot bot : bots) {
            Iterator<Food> foodIterator = foodItems.iterator();
            while (foodIterator.hasNext()) {
                Food food = foodIterator.next();
                if (bot.collidesWith(food)) {
                    bot.grow();
                    foodIterator.remove();
                }
            }
        }

        // Collisioni tra bot e altri bot
        for (Bot bot : bots) {
            for (Bot otherBot : bots) {
                if (bot != otherBot && bot.collidesWith(otherBot)) {
                    botsToRemove.add(bot);
                    botsToAdd.add(Bot.createBot(entities, this)); // Genera un nuovo bot
                    break;
                }
            }
        }

        // Collisioni tra bot e segmenti dei giocatori
        for (Bot bot : bots) {
            for (Player player : players) {
                for (Segment segment : player.getBodySegments()) {
                    if (checkCollisionSegmentBot(bot, segment)) {
                        botsToRemove.add(bot);
                        botsToAdd.add(Bot.createBot(entities, this)); // Genera un nuovo bot
                        break;
                    }
                }
            }
        }

        // Collisioni tra giocatori e segmenti di altri giocatori
        for (Player player : players) {
            for (Player otherPlayer : players) {
                if (player != otherPlayer) { // Evita il confronto con se stesso
                    for (Segment segment : otherPlayer.getBodySegments()) {
                        if (checkCollisionSegmentPlayer(player, segment)) {
                            playersToKill.add(player);
                            break;
                        }
                    }
                }
            }
        }

        // Collisioni tra giocatori e bot
        for (Player player : players) {
            for (Bot bot : bots) {
                if (player.collidesWith(bot)) {
                    playersToKill.add(player);
                    break;
                }
            }
        }

        // Collisioni tra giocatori e segmenti dei bot
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

        // Segna i giocatori come morti
        for (Player player : playersToKill) {
            player.setAlive(false); // Segna il giocatore come morto
            System.out.println("Player " + player.getId() + " is dead! Press 'R' to respawn.");
        }

        // Rimuove i bot morti e genera nuovi bot
        bots.removeAll(botsToRemove);
        bots.addAll(botsToAdd);
    }


    private boolean checkCollisionSegmentBot(Bot bot, Segment segment) {
        double distance = bot.getPosition().distanceTo(segment.getPosition());
        double collisionDistance = bot.getSize() / 2.0 + segment.getSize() / 2.0;
        return distance < collisionDistance;
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

}