import javax.swing.*;
import javax.swing.Timer;
import java.awt.*;
import java.io.*;
import java.net.*;
import java.util.*;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.locks.ReentrantLock;

public class GameClient {
    private static final String SERVER_IP = "localhost";
    private static final int SERVER_PORT = 1234;
    private Socket socket;
    private PrintWriter out;
    private BufferedReader in;
    private GameState gameState;
    private Player player;
    private final List<Entity> entities = Collections.synchronizedList(new ArrayList<>());
    private JFrame gameFrame;
    private GameWindow gameWindow;
    private GameController gameController;
    private ExecutorService executorService;
    private boolean connected = false;
    private Timer updateTimer;
    private final Map<String, Player> playerMap = new ConcurrentHashMap<>();
    private final ReentrantLock lock = new ReentrantLock();

    public GameClient() {
        showStartMenu();
    }

    private void showStartMenu() {
        JFrame startFrame = new JFrame("VERMONI - Start Menu");
        startFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        startFrame.setSize(400, 300);
        JPanel panel = new JPanel(new GridLayout(3, 1));
        JLabel titleLabel = new JLabel("Scegli una modalità di gioco", SwingConstants.CENTER);
        JButton singlePlayerButton = new JButton("Single Player");
        JButton multiPlayerButton = new JButton("Multiplayer");
        singlePlayerButton.addActionListener(e -> {
            startFrame.dispose();
            startSinglePlayer();
        });
        multiPlayerButton.addActionListener(e -> {
            if (isServerAvailable()) {
                startFrame.dispose();
                startMultiplayer();
            } else {
                JOptionPane.showMessageDialog(startFrame, "Il server non è disponibile! Avvia il server e riprova.", "Errore", JOptionPane.ERROR_MESSAGE);
            }
        });
        panel.add(titleLabel);
        panel.add(singlePlayerButton);
        panel.add(multiPlayerButton);
        startFrame.add(panel);
        startFrame.setVisible(true);
    }
    private boolean isServerAvailable() {
        try (Socket testSocket = new Socket(SERVER_IP, SERVER_PORT)) {
            return true;
        } catch (IOException e) {
            return false;
        }
    }
    private void startSinglePlayer() {
        gameState = new GameState();
        player = new Player("SinglePlayer");
        gameState.addPlayer(player);
        gameState.addBot();

//        entities = new ArrayList<>(gameState.getPlayers());
        entities.addAll(gameState.getPlayers());
        entities.addAll(gameState.getBots());
        entities.addAll(gameState.getFoodItems());

        createGameWindow();
    }
    private void startMultiplayer() {
//        this.entities = new ArrayList<>();
        executorService = Executors.newSingleThreadExecutor();

        try {
            player = new Player("Player_" + UUID.randomUUID().toString().substring(0, 8));
            socket = new Socket(SERVER_IP, SERVER_PORT);
            out = new PrintWriter(socket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            System.out.println("Connesso al server come " + player.getId());
            out.println("JOIN " + player.getId());

            Future<Boolean> joinResponse = executorService.submit(this::waitForServerResponse);

            if (joinResponse.get()) {
                System.out.println("Server ha confermato il join.");
                connected = true;

                gameState = new GameState();
                receiveInitialGameStateFromServer(); // Questo riempirà tutto lo stato
                gameState.addPlayer(player);
                playerMap.put(player.getId(), player);
                createGameWindow();
                // Avvia thread per ascoltare messaggi dal server
                new Thread(this::listenForMessages).start();

                // Inizia a inviare aggiornamenti del player al server
                startPlayerUpdates();
            } else {
                System.err.println("Errore: il server non ha accettato il join.");
                System.exit(1);
            }

        } catch (IOException | InterruptedException | ExecutionException e) {
            System.err.println("Errore di connessione al server.");
            e.printStackTrace();
            System.exit(0);
        }
    }
    private void receiveInitialGameStateFromServer() {
        try {
            String message;
            boolean initComplete = false;

            while (!initComplete && (message = in.readLine()) != null) {
                System.out.println("Stato iniziale ricevuto: " + message);
                String[] parts = message.trim().split("\\s+");

                if (message.startsWith("NEW_PLAYER ")) {
                    if (parts.length < 4) continue;
                    String playerId = parts[1];
                    double x = Double.parseDouble(parts[2]);
                    double y = Double.parseDouble(parts[3]);

                    // Se è il nostro stesso player, aggiorniamo la posizione
                    if (player.getId().equals(playerId)) {
                        player.setPosition(new Vector2D(x, y));
                    }
                    // Se è un altro player, aggiungiamolo
                    else if (!playerMap.containsKey(playerId)) {
                        Player newPlayer = new Player(playerId);
                        newPlayer.setPosition(new Vector2D(x, y));
                        gameState.addPlayer(newPlayer);
                        entities.add(newPlayer);
                        playerMap.put(playerId, newPlayer);
                    }
                    gameState.updateGameState();
                }
                else if (message.startsWith("NEW_BOT ")) {
                    if (parts.length < 3) continue;
                    double x = Double.parseDouble(parts[1]);
                    double y = Double.parseDouble(parts[2]);

                    // Evita di aggiungere duplicati controllando se la posizione è già registrata
                    boolean exists = gameState.getBots().stream()
                            .anyMatch(bot -> bot.getPosition().getX() == x && bot.getPosition().getY() == y);

                    if (!exists) {
                        Bot bot = new Bot(new Vector2D(x, y), entities, gameState);
                        gameState.getBots().add(bot);
                        entities.add(bot);
                    }
                    gameState.updateGameState();
                }
                else if (message.startsWith("NEW_FOOD ")) {
                    if (parts.length < 3) continue;
                    double x = Double.parseDouble(parts[1]);
                    double y = Double.parseDouble(parts[2]);

                    // Evita duplicati nel cibo
                    boolean exists = gameState.getFoodItems().stream()
                            .anyMatch(food -> food.getPosition().getX() == x && food.getPosition().getY() == y);

                    if (!exists) {
                        Food food = new Food(new Vector2D(x, y), 10);
                        gameState.getFoodItems().add(food);
                        entities.add(food);
                    }
                    gameState.updateGameState();
                }
                else if (message.startsWith("INIT_COMPLETE")) {
                    System.out.println("Inizializzazione completata.");
                    initComplete = true;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private boolean waitForServerResponse() {
        try {
            String response = in.readLine();
            System.out.println("Risposta del server: " + response);
            return "JOIN_OK".equals(response);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    private void listenForMessages() {
        try {
            String message;
            while (connected && (message = in.readLine()) != null) {
                processServerMessageUpdate(message);
            }
        } catch (IOException e) {
            System.err.println("Errore nella ricezione dei messaggi dal server");
            e.printStackTrace();
            connected = false;
        }
    }

    private void processServerMessageUpdate(String message) {
        System.out.println("Messaggio ricevuto: " + message);
        String[] parts = message.split(" ");

        if (message.startsWith("UPDATE_PLAYERS ")) {
            for (int i = 1; i < parts.length; i++) {
                String playerId = parts[i];
                if (i + 2 >= parts.length) break;

                double x = Double.parseDouble(parts[i + 1]);
                double y = Double.parseDouble(parts[i + 2]);
                boolean isDead = false;
                if (i + 3 < parts.length && "DEAD".equals(parts[i + 3])) {
                    isDead = true;
                    i++; // Avanza l'indice per saltare "DEAD"
                }
                updatePlayerPosition(playerId, x, y);

                Player playerObj = playerMap.get(playerId);
                if (playerObj == null) {
                    playerObj = new Player(playerId);
                    gameState.addPlayer(playerObj);
                    entities.add(playerObj);
                    playerMap.put(playerId, playerObj);
                }

                playerObj.setPosition(new Vector2D(x, y));
                playerObj.setAlive(!isDead);
                gameState.updateGameState();

                // Assicurati che il player locale venga aggiornato correttamente
                if (player != null && player.getId().equals(playerId)) {
                    player.setPosition(new Vector2D(x, y));
                    player.setAlive(!isDead);
                }

                if (isDead) {
                    i++; // Salta il flag DEAD
                }
            }
            SwingUtilities.invokeLater(() -> gameWindow.repaint());
        }
        else if (message.startsWith("UPDATE_BOTS ")) {
            lock.lock();
            try {
                List<Entity> newEntities = new ArrayList<>();
                entities.removeIf(entity -> entity instanceof Bot);  // Rimuovi solo bot

                gameState.getBots().clear();
                for (int i = 1; i + 1 < parts.length; i += 2) {  // Controlla che ci siano abbastanza valori
                    double x = Double.parseDouble(parts[i]);
                    double y = Double.parseDouble(parts[i + 1]);

                    Bot bot = new Bot(new Vector2D(x, y), entities, gameState);
                    bot.setPosition(new Vector2D(x, y));
                    gameState.getBots().add(bot);
                    newEntities.add(bot);
                }
                entities.addAll(newEntities);
                gameState.updateGameState();
            } finally {
                lock.unlock();
            }
            SwingUtilities.invokeLater(() -> gameWindow.repaint());
        }
        else if (message.startsWith("UPDATE_FOOD ")) {
            lock.lock();
            try {
                List<Entity> newEntities = new ArrayList<>();
                entities.removeIf(entity -> entity instanceof Food);  // Rimuovi solo il cibo

                gameState.getFoodItems().clear();
                for (int i = 1; i + 1 < parts.length; i += 2) {  // Controlla che ci siano abbastanza valori
                    double x = Double.parseDouble(parts[i]);
                    double y = Double.parseDouble(parts[i + 1]);

                    Food food = new Food(new Vector2D(x, y), 10);
                    gameState.getFoodItems().add(food);
                    newEntities.add(food);
                }
                entities.addAll(newEntities);
                gameState.updateGameState();
            } finally {
                lock.unlock();
            }
            SwingUtilities.invokeLater(() -> gameWindow.repaint());
        }

        else if (message.startsWith("REMOVE_PLAYER ")) {
            String playerId = parts[1];
            removePlayer(playerId);
            gameState.updateGameState();
            SwingUtilities.invokeLater(() -> gameWindow.repaint());
        }
    }
    private void updatePlayerPosition(String playerId, double x, double y) {
        Player existingPlayer = playerMap.get(playerId);
        lock.lock();
        try {
            // Usa una copia della lista o CopyOnWriteArrayList per evitare ConcurrentModificationException

                boolean found = false;

                // Usa una copia per l'iterazione
                List<Entity> entitiesCopy = new ArrayList<>(entities);

                for (Entity entity : entitiesCopy) {
                    if (entity instanceof Player && ((Player) entity).getId().equals(playerId)) {
                        // Aggiorna la posizione del giocatore
                        entity.setPosition(new Vector2D(x, y));
                        found = true;
                        break;
                    }
                }

                // Se il giocatore non è stato trovato, lo aggiungi
                if (!found) {
                    if (existingPlayer != null) {
                        existingPlayer.setPosition(new Vector2D(x, y));
                    } else if (!playerId.equals(player.getId())) {
                        Player newPlayer = new Player(playerId);
                        newPlayer.setPosition(new Vector2D(x, y));
                        playerMap.put(playerId, newPlayer);
                        gameState.addPlayer(newPlayer);
                        entities.add(newPlayer);
                    }
                }

        }finally{
            lock.unlock();
        }
        // Dopo aver modificato la lista, aggiorna la UI
        SwingUtilities.invokeLater(() -> gameWindow.repaint());

    }

    private void removePlayer(String playerId) {
        synchronized (entities) {
            entities.removeIf(entity -> entity instanceof Player && ((Player) entity).getId().equals(playerId));
        }
        synchronized (gameState.getPlayers()) {
            gameState.getPlayers().removeIf(p -> p.getId().equals(playerId));
        }

        SwingUtilities.invokeLater(gameWindow::repaint); // Aggiorna la UI in modo sicuro
    }



    private void startPlayerUpdates() {
        updateTimer = new Timer(50, e -> {
            if (connected && player.isAlive()) {
                sendPlayerUpdate();
            }
        });
        updateTimer.start();
    }

    private void sendPlayerUpdate() {
        new Thread(() -> {
            while (connected) {
                try {
                    Thread.sleep(50); // Invia posizione ogni 50ms
                    updatePlayerPosition(player.getId(), player.getPosition().getX(), player.getPosition().getY());
                    out.println("MOVE " + player.getId() + " " + player.getPosition().getX() + " " + player.getPosition().getY());
                    System.out.println("Posizione inviata al server: " + player.getPosition().getX() + ", " + player.getPosition().getY());

                    // Aggiungi qui l'aggiornamento della posizione nel client

                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }


    private void createGameWindow() {

        gameFrame = new JFrame("VERMONI - Game Client");
        gameFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        gameFrame.setSize(800, 600);

        gameWindow = new GameWindow(gameState, gameController, player);
        gameController = new GameController(player, gameWindow);

        gameFrame.add(gameWindow);
        gameFrame.pack();
        gameFrame.setVisible(true);

        Timer renderTimer = new Timer(16, e -> gameWindow.repaint());
        renderTimer.start();
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(GameClient::new);
    }
}