import javax.swing.*;
import javax.swing.Timer;
import java.awt.*;
import java.io.*;
import java.net.*;
import java.util.*;
import java.util.List;
import java.util.concurrent.*;

public class GameClient {
    private static final String SERVER_IP = "localhost";
    private static final int SERVER_PORT = 12345;
    private Socket socket;
    private PrintWriter out;
    private BufferedReader in;
    private GameState gameState;
    private Player player;
    private List<Entity> entities;
    private JFrame gameFrame;
    private GameWindow gameWindow;
    private GameController gameController;
    private ExecutorService executorService;
    private boolean connected = false;
    private Timer updateTimer;

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
                gameState.addPlayer(player); // Aggiungi il player locale al gameState
                receiveInitialGameStateFromServer();

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
            while ((message = in.readLine()) != null) {
                System.out.println("Stato iniziale ricevuto: " + message);
                String[] parts = message.split(" ");

                if (message.startsWith("NEW_PLAYER")) {
                    String playerId = parts[1];
                    double x = Double.parseDouble(parts[2]);
                    double y = Double.parseDouble(parts[3]);

                    // Non aggiungere il player locale di nuovo
                    if (!playerId.equals(player.getId())) {
                        Player newPlayer = new Player(playerId);
                        newPlayer.setPosition(new Vector2D(x, y));
                        gameState.addPlayer(newPlayer);
                        entities.add(newPlayer);
                    }
                } else if (message.startsWith("INIT_COMPLETE")) {
                    break; // Fine dell'inizializzazione
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

        if (message.startsWith("UPDATE")) {
            for (int i = 1; i < parts.length; i += 3) {
                String playerId = parts[i];
                double x = Double.parseDouble(parts[i + 1]);
                double y = Double.parseDouble(parts[i + 2]);

                // Non aggiorniamo il player locale con le informazioni dal server
                if (!playerId.equals(player.getId())) {
                    updatePlayerPosition(playerId, x, y);
                }
            }
        } else if (message.startsWith("REMOVE_PLAYER")) {
            String playerId = parts[1];
            removePlayer(playerId);
        }
    }

    private void updatePlayerPosition(String playerId, double x, double y) {
        for (Entity entity : entities) {
            if (entity instanceof Player && ((Player) entity).getId().equals(playerId)) {
                entity.setPosition(new Vector2D(x, y));
                return;
            }
        }

        // Se il player non esiste, crealo
        if (!playerId.equals(player.getId())) {
            Player newPlayer = new Player(playerId);
            newPlayer.setPosition(new Vector2D(x, y));
            gameState.addPlayer(newPlayer);
            entities.add(newPlayer);
        }
    }

    private void removePlayer(String playerId) {
        entities.removeIf(entity -> entity instanceof Player && ((Player) entity).getId().equals(playerId));
        gameState.getPlayers().removeIf(p -> p.getId().equals(playerId));
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
