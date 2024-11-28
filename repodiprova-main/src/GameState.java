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
        // Liste temporanee per aggiungere o rimuovere in modo sicuro
        List<Bot> botsToAdd = new ArrayList<>();
        List<Bot> botsToRemove = new ArrayList<>();
        List<Player> playersToRemove = new ArrayList<>();

        // Controlla collisioni tra giocatori e cibo
        Iterator<Player> playerIterator = players.iterator();
        while (playerIterator.hasNext()) {
            Player player = playerIterator.next();
            Iterator<Food> foodIterator = foodItems.iterator();
            while (foodIterator.hasNext()) {
                Food food = foodIterator.next();
                if (player.collidesWith(food)) {
                    player.grow();
                    foodIterator.remove(); // Rimuovi il cibo mangiato
                }
            }
        }

        // Controlla collisioni tra bot e cibo
        Iterator<Bot> botIterator = bots.iterator();
        while (botIterator.hasNext()) {
            Bot bot = botIterator.next();
            Iterator<Food> foodIterator = foodItems.iterator();
            while (foodIterator.hasNext()) {
                Food food = foodIterator.next();
                if (bot.collidesWith(food)) {
                    bot.grow();
                    foodIterator.remove(); // Rimuovi il cibo mangiato
                }
            }

            // Controlla collisioni tra bot e altri bot
            boolean isRemoved = false;
            Iterator<Bot> otherBotIterator = bots.iterator();
            while (otherBotIterator.hasNext()) {
                Bot otherBot = otherBotIterator.next();
                if (bot != otherBot && bot.collidesWith(otherBot)) {
                    isRemoved = true;
                    break;

                }
            }

            // Se il bot è rimosso, aggiungilo alla lista di rimozione
            if (isRemoved) {
                botsToRemove.add(bot);
                botsToAdd.add(new Bot(new Vector2D(Math.random() * 800, Math.random() * 600), this, null)); // Aggiungi un nuovo bot
            }

            // Controlla collisioni tra bot e segmenti dei giocatori
            for (Player player : players) {
                for (Segment segment : player.getBodySegments()) {
                    if (bot.getPosition().distanceTo(segment.getPosition()) < (bot.getSize() / 2 + (double) player.getSegmentSize() / 2)) {
                        botsToRemove.add(bot); // Rimuovi il bot dalla lista
                        break;
                    }
                }
            }
        }

        // Applicare le modifiche a bots: rimuovere i bot e aggiungere quelli nuovi
        bots.removeAll(botsToRemove); // Rimuove tutti i bot che sono stati segnati per la rimozione
        bots.addAll(botsToAdd);
        // Aggiungi i nuovi bot

        // Controlla collisioni tra giocatori e bot
        Iterator<Player> playerIterator2 = players.iterator();
        while (playerIterator2.hasNext()) {
            Player player = playerIterator2.next();
            Iterator<Bot> botIterator2 = bots.iterator();
            while (botIterator2.hasNext()) {
                Bot bot = botIterator2.next();
                if (player.collidesWith(bot)) {
                    playersToRemove.add(player); // Segna il giocatore per la rimozione
                    break; // Esci dal ciclo una volta che il giocatore è stato rimosso
                }
            }
        }

        // Rimuovi i giocatori segnati per la rimozione
        players.removeAll(playersToRemove);




    // Controlla collisioni tra giocatori e bot
        playerIterator = players.iterator(); // Nuovo iteratore per i giocatori
        while (playerIterator.hasNext()) {
            Player player = playerIterator.next();
            Iterator<Bot> botIterator2 = bots.iterator(); // Iteratore per i bot
            while (botIterator2.hasNext()) {
                Bot bot = botIterator2.next();
                if (player.collidesWith(bot)) {
                    playerIterator.remove(); // Rimuovi il giocatore
                    return; // Esci dal metodo se il giocatore è morto
                }
            }
        }
        }
    public void respawnFood () {
        while (foodItems.size() < 10) { // Assicurati di avere sempre almeno 10 cibi
            foodItems.add(new Food(new Vector2D(Math.random() * 800, Math.random() * 600), 10));
        }
    }
    public void render(Graphics g) {
        // Disegna il cibo
        g.setColor(Color.GREEN);
        for (Food food : foodItems) {
            g.fillOval((int) food.getPosition().x, (int) food.getPosition().y, (int) food.getSize(), (int) food.getSize());
        }

        // Disegna il giocatore
        for (Player player : players) {
            for (int i = 0; i < player.segments.size(); i++) {
                Segment segment = player.segments.get(i);
                if (i == 0) {
                    g.setColor(Color.YELLOW);
                } else {
                    // Disegna il corpo del serpente con colore diverso (blu)
                    g.setColor(Color.BLUE);
                }

                g.fillOval((int) segment.getPosition().x, (int) segment.getPosition().y, (int) segment.getSize(), (int) segment.getSize());
            }
        }


        // Disegna i bot
        g.setColor(Color.RED);
        for (Bot bot : bots) {
            for (int i = 0; i < bot.segments.size(); i++) {
                Segment segment = bot.segments.get(i);

                g.fillOval((int) segment.getPosition().x, (int) segment.getPosition().y, (int) segment.getSize(), (int) segment.getSize());
            }
            g.fillOval((int) bot.getPosition().x, (int) bot.getPosition().y, (int) bot.getSize(), (int) bot.getSize());
        }
    }
        public List<Food> getFoodItems () {
            return foodItems;
        }

        public List<Bot> getBots () {
            return bots;
        }

    }

