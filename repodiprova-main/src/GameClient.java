import javax.swing.*;
import javax.swing.Timer;
import java.awt.*;
import java.io.*;
import java.net.*;
import java.util.*;
import java.util.List;

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

    public GameClient() {
        showStartMenu();
    }

    private void showStartMenu() {
        JFrame startFrame = new JFrame("Slither.io - Start Menu");
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
        player.setPosition(new Vector2D(400, 300));  // Assicura che il player sia al centro
        gameState.addPlayer(player);
        gameController = new GameController(player);

        entities = new ArrayList<>(gameState.getPlayers());
        entities.addAll(gameState.getBots());
        entities.addAll(gameState.getFoodItems());

        if (entities.isEmpty()) {
            System.err.println("ERRORE: entities è vuoto, nessun oggetto da disegnare!");
        } else {
            for (Entity e : entities) {
                System.out.println("Entità presente: " + e.getClass().getSimpleName() + " Posizione: " + e.getPosition());
            }
        }

        createGameWindow();
    }

    private void startMultiplayer() {
        this.entities = new ArrayList<>();
        try {
            player = new Player("Player_" + UUID.randomUUID().toString().substring(0, 8));
            socket = new Socket(SERVER_IP, SERVER_PORT);
            out = new PrintWriter(socket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            System.out.println("Connesso al server come " + player.getId());
            out.println("JOIN " + player.getId()); // Invia il messaggio di join al server
            new Thread(this::listenForMessages).start();
            gameController = new GameController(player);
            createGameWindow();
        } catch (IOException e) {
            System.err.println("Errore di connessione al server.");
            System.exit(0);
        }
    }

    private void listenForMessages() {
        try {
            String message;
            while ((message = in.readLine()) != null) {
                processServerMessage(message);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void processServerMessage(String message) {
        String[] parts = message.split(" ");
        if (message.startsWith("NEW_PLAYER")) {
            String playerId = parts[1];
            Player newPlayer = new Player(playerId);
            entities.add(newPlayer);
        } else if (message.startsWith("UPDATE")) {
            String playerId = parts[1];
            double x = Double.parseDouble(parts[2]);
            double y = Double.parseDouble(parts[3]);
            updatePlayerPosition(playerId, x, y);
        }
    }

    private void updatePlayerPosition(String playerId, double x, double y) {
        for (Entity entity : entities) {
            if (entity instanceof Player && ((Player) entity).getId().equals(playerId)) {
                entity.setPosition(new Vector2D(x, y));
                break;
            }
        }
    }

    private void createGameWindow() {
        if (entities == null) {
            entities = new ArrayList<>();
        }

        gameFrame = new JFrame("Game Client");
        gameFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        gameFrame.setSize(800, 600);

        gameWindow = new GameWindow(entities, gameController, player);
        gameFrame.add(gameWindow);
        gameFrame.pack();
        gameFrame.setVisible(true);

        Timer renderTimer = new Timer(16, e -> gameWindow.repaint());
        renderTimer.start();
    }

    public static void main(String[] args) {
        new GameClient();
    }
}