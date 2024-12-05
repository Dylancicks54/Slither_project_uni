import java.awt.*;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class GameState {
    private List<Player> players;
    private List<Bot> bots;
    private List<Food> foodItems;

    public GameState(List<Player> players, List<Bot> bots, List<Food> foodItems) {
        this.players = players;
        this.bots = bots;
        this.foodItems = foodItems;
    }

    public void updateGameState() {
        long startTime = System.nanoTime();  // Inizio temporale
        System.out.println("Inizio aggiornamento stato del gioco.");

        // Aggiornamento della posizione dei giocatori
        for (Player player : players) {
            player.update();  // Aggiorna la posizione, velocità, ecc.
        }

        // Aggiornamento dei bot
        for (Bot bot : bots) {
            bot.update();
        }

        // Gestione delle collisioni (se c'è)
        checkCollisions();

        // Respawn del cibo
        respawnFood();

        long elapsedTime = System.nanoTime() - startTime;
        System.out.println("Tempo aggiornamento stato di gioco (ns): " + elapsedTime);  // Visualizza in nanosecondi
        System.out.println("Tempo aggiornamento stato di gioco (ms): " + elapsedTime / 1_000_000);  // Converte in millisecondi
    }




    public void checkCollisions() {
        List<Bot> botsToAdd = new ArrayList<>();
        List<Bot> botsToRemove = new ArrayList<>();
        List<Player> playersToRemove = new ArrayList<>();

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
                    botsToAdd.add(Bot.createBot(this, null)); // Genera un nuovo bot
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
                        botsToAdd.add(Bot.createBot(this, null)); // Genera un nuovo bot
                        break;
                    }
                }
            }
        }

        // Collisioni tra bot e segmenti di altri bot
        for (Bot bot : bots) {
            for (Bot otherBot : bots) {
                if (bot != otherBot) { // Evita il confronto con se stesso
                    for (Segment segment : otherBot.getBodySegments()) {
                        if (checkCollisionSegmentBot(bot, segment)) {
                            botsToRemove.add(bot);
                            botsToAdd.add(Bot.createBot(this, null)); // Genera un nuovo bot
                            break;
                        }
                    }
                }
            }
        }
        for (Player player : players) {
            for (Player otherPlayer : players) {
                if (player != otherPlayer) { // Evita il confronto con se stesso
                    for (Segment segment : otherPlayer.getBodySegments()) {
                        if (checkCollisionSegmentPlayer(player, segment)) {
                            playersToRemove.add(player);
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
                    playersToRemove.add(player);
                    break;
                }
            }
        }
        // Collisioni tra bot e segmenti di altri bot
        for (Player player : players) {
            for (Bot bot : bots) {
                    for (Segment segment : bot.getBodySegments()) {
                        if (checkCollisionSegmentPlayer(player,segment)) {
                            playersToRemove.add(player);
                            break;
                        }
                    }

            }
        }

        // Applica le modifiche
        bots.removeAll(botsToRemove);
        bots.addAll(botsToAdd);
        players.removeAll(playersToRemove);
    }

    /**
     * Metodo per verificare la collisione tra un bot e un segmento. Necessaria se non voglio creare una classe EntitywithMovement o altra classe segment
     *
     * @param bot     Il bot da controllare.
     * @param segment Il segmento da controllare.
     * @return true se il bot collide con il segmento, false altrimenti.
     */
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


    public void respawnFood() {
        final int MAX_FOOD_ITEMS = 10;

        while (foodItems.size() < MAX_FOOD_ITEMS) {
            foodItems.add(new Food(new Vector2D(Math.random() * 800, Math.random() * 600), 10));
        }
    }

    public void render(Graphics g, Player player) {
        Graphics2D g2 = (Graphics2D) g;

        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        if (player == null) {
            g.setColor(Color.RED);
            Font font = new Font("Arial", Font.PLAIN, 70);  // Font, stile e dimensione
            g2.setFont(font);
            g.drawString("SEI MORTO", 100, 100);
            return;
        }

        Vector2D playerPosition = player.getPosition();

        // Dimensioni della finestra
        int screenWidth = 1920;
        int screenHeight = 1080;

        // Calcola l'offset per centrare il giocatore del client
        int offsetX = (int) playerPosition.x - screenWidth / 2;
        int offsetY = (int) playerPosition.y - screenHeight / 2;

        // Disegna lo sfondo
        g2.setColor(Color.DARK_GRAY);
        g2.fillRect(0, 0, 8000, 8000);

        // Disegna la griglia dei pentagoni
        g2.setColor(Color.WHITE);
        double angleIncrement = Math.PI * 2 / 5;
        double radius = 100 / 2.0;

        double xOffset = 75 * 1.5;
        double yOffset = 75 * Math.sqrt(3);

        for (int row = -30; row < 8000 / yOffset; row++) {
            for (int col = -30; col < 8000 / xOffset; col++) {
                double xShift = (row % 2 == 0) ? 0 : xOffset / 2;
                double centerX = col * xOffset + xShift;
                double centerY = row * yOffset;

                Polygon pentagon = new Polygon();
                for (int i = 0; i < 5; i++) {
                    double angle = i * angleIncrement - Math.PI / 2;
                    int x = (int) (centerX + Math.cos(angle) * radius) - offsetX;
                    int y = (int) (centerY + Math.sin(angle) * radius) - offsetY;
                    pentagon.addPoint(x, y);
                }
                g2.drawPolygon(pentagon);
            }
        }

        // Disegna il cibo
        g2.setColor(Color.GREEN);
        for (Food food : foodItems) {
            int foodX = (int) food.getPosition().x - offsetX;
            int foodY = (int) food.getPosition().y - offsetY;
            g2.fillOval(foodX, foodY, (int) food.getSize(), (int) food.getSize());
        }

        // Disegna il giocatore
        for (Player players : players) {
            for (int i = 0; i < players.segments.size(); i++) {
                Segment segment = players.segments.get(i);
                int segmentX = (int) segment.getPosition().x - offsetX;
                int segmentY = (int) segment.getPosition().y - offsetY;
                if (i == 0) {
                    g2.setColor(Color.YELLOW);
                } else {
                    g2.setColor(Color.BLUE);
                }
                g2.fillOval(segmentX, segmentY, (int) segment.getSize(), (int) segment.getSize());
            }
        }

        // Disegna i bot
        g2.setColor(Color.RED);
        for (Bot bot : bots) {
            for (int i = 0; i < bot.segments.size(); i++) {
                Segment segment = bot.segments.get(i);
                int segmentX = (int) segment.getPosition().x - offsetX;
                int segmentY = (int) segment.getPosition().y - offsetY;
                g2.fillOval(segmentX, segmentY, (int) segment.getSize(), (int) segment.getSize());
            }
            int botX = (int) bot.getPosition().x - offsetX;
            int botY = (int) bot.getPosition().y - offsetY;
            g2.fillOval(botX, botY, (int) bot.getSize(), (int) bot.getSize());
        }
    }


    public List<Food> getFoodItems () {
            return foodItems;
        }

        public List<Bot> getBots () {
            return bots;
        }

    public List<Entity> getAllEntities() {
        List<Entity> allEntities = new ArrayList<>();
        allEntities.addAll(players);
        allEntities.addAll(bots);
        allEntities.addAll(foodItems);
        return allEntities;
    }

    public List<Player> getPlayers() {
        return players;
    }
}

