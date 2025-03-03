import javax.swing.*;
import javax.swing.Timer;
import java.awt.*;
import java.io.*;
import java.net.*;
import java.util.*;
import java.util.List;
import java.util.concurrent.*;

public class GameClient {
    private static final String SERVER_IP = "10.227.219.95";
    private static final int SERVER_PORT = 1234;
    private Socket socket;
    private PrintWriter out;
    private BufferedReader in;
    private GameState gameState;
    private Player player;
    private List<Entity> entities = new CopyOnWriteArrayList<>();
    private JFrame gameFrame;
    private GameWindow gameWindow;
    private GameController gameController;
    private ExecutorService executorService;
    private boolean connected = false;
    private Timer updateTimer;
    private final Map<String, Player> playerMap = new ConcurrentHashMap<>();

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

        entities = new ArrayList<>(gameState.getPlayers());
        entities.addAll(gameState.getBots());
        entities.addAll(gameState.getFoodItems());

        createGameWindow();
    }
    private void startMultiplayer() {
        this.entities = new ArrayList<>();
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
                String[] parts = message.split(" ");

                if (parts.length == 0) {
                    System.err.println("Messaggio vuoto ricevuto dal server.");
                    continue;
                }

                if (message.startsWith("INIT_STATE")) {
                    gameState = new GameState(); // Inizializza lo stato del gioco
                }

                if (message.startsWith("NEW_PLAYER ")) {
                    if (parts.length < 4) {
                        System.err.println("Messaggio NEW_PLAYER incompleto: " + message);
                        continue;
                    }
                    String playerId = parts[1];
                    double x = Double.parseDouble(parts[2]);
                    double y = Double.parseDouble(parts[3]);

                    Player newPlayer = new Player(playerId);
                    newPlayer.setPosition(new Vector2D(x, y));

                    if (playerId.equals(player.getId())) {
                        player.setPosition(new Vector2D(x, y));
                        gameState.addPlayer(player);
                        entities.add(player);
                    } else {
                        gameState.addPlayer(newPlayer);
                        entities.add(newPlayer);
                        playerMap.put(playerId, newPlayer);
                    }
                } else if (message.startsWith("NEW_BOT ")) {
                    if (parts.length < 3) {
                        System.err.println("Messaggio NEW_BOT incompleto: " + message);
                        continue;
                    }
                    double x = Double.parseDouble(parts[1]);
                    double y = Double.parseDouble(parts[2]);

                    Bot bot = new Bot(new Vector2D(x, y), entities, gameState);
                    bot.setPosition(new Vector2D(x, y));
                    gameState.getBots().add(bot);
                    entities.add(bot);
                } else if (message.startsWith("NEW_FOOD ")) {
                    if (parts.length < 3) {
                        System.err.println("Messaggio NEW_FOOD incompleto: " + message);
                        continue;
                    }
                    double x = Double.parseDouble(parts[1]);
                    double y = Double.parseDouble(parts[2]);

                    Food food = new Food(new Vector2D(x, y), 10);
                    gameState.getFoodItems().add(food);
                    entities.add(food);
                } else if (message.startsWith("INIT_COMPLETE")) {
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
                processServerMessage(message);
            }
        } catch (IOException e) {
            System.err.println("Errore nella ricezione dei messaggi dal server");
            e.printStackTrace();
            connected = false;
        }
    }

    private void processServerMessage(String message) {
        System.out.println("Messaggio ricevuto: " + message);
        String[] parts = message.split(" ");

        if (message.startsWith(" UPDATE_PLAYERS ")) {
            // Aggiorna la posizione dei giocatori
            for (int i = 1; i < parts.length; i += 3) {
                String playerId = parts[i];
                double x = Double.parseDouble(parts[i + 1]);
                double y = Double.parseDouble(parts[i + 2]);
                if (i + 4 < parts.length && parts[i + 4].equals(" DEAD ")) {
                    player.setAlive(false);
                }
                    // Assicurati che il giocatore giusto venga aggiornato
                if (playerId.equals(player.getId())) {
                    player.setPosition(new Vector2D(x, y));
                } else {
                    updatePlayerPosition(playerId, x, y);
                }

            }
        }
        else if (message.startsWith(" UPDATE_BOTS ")) {
            synchronized (entities) {
                // Rimuovi tutti i bot esistenti
                entities.removeIf(entity -> entity instanceof Bot);
                gameState.getBots().clear();

                // Aggiungi i bot aggiornati
                for (int i = 1; i < parts.length; i += 2) {
                    double x = Double.parseDouble(parts[i]);
                    double y = Double.parseDouble(parts[i + 1]);

                    Bot bot = new Bot(new Vector2D(x,y), entities, gameState);
                    bot.setPosition(new Vector2D(x, y));
                    gameState.getBots().add(bot);
                    entities.add(bot);
                }
            }
        }
        else if (message.startsWith(" UPDATE_FOOD ")) {
            synchronized (entities) {
                // Rimuovi tutto il cibo esistente
                entities.removeIf(entity -> entity instanceof Food);
                gameState.getFoodItems().clear();

                // Aggiungi il cibo aggiornato
                for (int i = 1; i < parts.length; i += 2) {
                    double x = Double.parseDouble(parts[i]);
                    double y = Double.parseDouble(parts[i + 1]);

                    Food food = new Food(new Vector2D(x, y), 10);
                    gameState.getFoodItems().add(food);
                    entities.add(food);
                }
            }
        }
        else if (message.startsWith(" REMOVE_PLAYER ")) {
            String playerId = parts[1];
            removePlayer(playerId);
        }
        else if (message.startsWith(" NEW_FOOD ")) {
            double x = Double.parseDouble(parts[1]);
            double y = Double.parseDouble(parts[2]);
            Food food = new Food(new Vector2D(x, y), 10);

            synchronized (entities) {
                gameState.getFoodItems().add(food);
                entities.add(food);
            }

        }


    }
    private void updatePlayerPosition(String playerId, double x, double y) {
        Player existingPlayer = playerMap.get(playerId);

        // Usa una copia della lista o CopyOnWriteArrayList per evitare ConcurrentModificationException
        synchronized (entities) {
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
        }

        // Dopo aver modificato la lista, aggiorna la UI
        SwingUtilities.invokeLater(gameWindow::repaint);
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
        if (out != null) {
            String updateMessage = "MOVE " + player.getId() + " " +
                    player.getPosition().getX() + " " +
                    player.getPosition().getY();
            out.println(updateMessage);
        }
    }

    private void createGameWindow() {
        if (entities == null) {
            entities = new ArrayList<>();
        }

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
        SwingUtilities.invokeLater(() -> new GameClient());
    }
}