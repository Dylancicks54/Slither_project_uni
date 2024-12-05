import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferStrategy;
import java.io.*;
import java.net.*;
import java.util.*;
import java.util.List;

public class GameClient {
    private static final String SERVER_IP = "10.227.219.242";
    private static final int SERVER_PORT = 12345;
    private Socket socket;
    private PrintWriter out;
    private BufferedReader in;
    private GameState gameState;
    private Player player;
    private List<Entity> entities = new ArrayList<>();
    private Canvas canvas;

    public GameClient() {
        try {
            // Genera un nome unico per il giocatore
            player.setId("Player_" + UUID.randomUUID().toString().substring(0, 8)); // Nome unico basato su UUID
            socket = new Socket(SERVER_IP, SERVER_PORT);
            out = new PrintWriter(socket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            System.out.println("Connesso al server come " + player.getId());

            SwingUtilities.invokeLater(this::showPreLobby);
            new Thread(this::listenForMessages).start();
        } catch (IOException e) {
            JOptionPane.showMessageDialog(null, "Impossibile connettersi al server. Avvia il server e riprova.", "Errore di connessione", JOptionPane.ERROR_MESSAGE);
            System.exit(0);
        }

    }

    private void showPreLobby() {
        JFrame preLobbyFrame = new JFrame("Prelobby");
        preLobbyFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        preLobbyFrame.setSize(400, 300);

        JPanel panel = new JPanel(new GridLayout(3, 1));
        JLabel welcomeLabel = new JLabel("Benvenuto! Premi 'Join' per entrare nel gioco.", SwingConstants.CENTER);
        JButton joinButton = new JButton("Join");

        // Listener per il pulsante Join
        joinButton.addActionListener(e -> {
            out.println("JOIN " + player.getId()); // Invia il nome univoco al server
            player = new Player(player.getId(), null);
            player.setPosition(new Vector2D(Math.random() * 300, Math.random() * 300));
            entities.add(player);
            preLobbyFrame.dispose();
            SwingUtilities.invokeLater(this::createGameWindow);
        });

        panel.add(welcomeLabel);
        panel.add(joinButton);
        preLobbyFrame.add(panel);
        preLobbyFrame.setVisible(true);
    }

    private void listenForMessages() {
        try {
            String message;
            while ((message = in.readLine()) != null) {
                System.out.println("Messaggio ricevuto dal server: " + message);

                if (message.startsWith("FULL_STATE")) {
                    String[] parts = message.split(" ");
                    String entityType = parts[1];

                    switch (entityType) {
                        case "PLAYER":
                            updateOrCreatePlayer(parts[2], Double.parseDouble(parts[3]), Double.parseDouble(parts[4]));
                            break;
                    }
                } else if (message.startsWith("UPDATE")) {
                    String[] parts = message.split(" ");
                    if (parts[1].equals("PLAYER")) {
                        updatePlayerPosition(parts[2], Double.parseDouble(parts[3]), Double.parseDouble(parts[4]));
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void updateOrCreatePlayer(String playerId, double x, double y) {
        Player player = findPlayerById(playerId);
        if (player == null) {
            player = new Player(playerId, null);
            entities.add(player);
        }
        player.setPosition(new Vector2D(x, y));
    }

    private Player findPlayerById(String playerId) {
        for (Entity entity : entities) {
            if (entity instanceof Player && ((Player) entity).getId().equals(playerId)) {
                return (Player) entity;
            }
        }
        return null;
    }

    // Metodi simili per Bot e Food
    private void updatePlayerPosition(String playerId, double x, double y) {
        for (Entity entity : entities) {
            if (entity instanceof Player && ((Player) entity).getId().equals(playerId)) {
                entity.setPosition(new Vector2D(x, y));
                break;
            }
        }
    }

    private void createGameWindow() {
        JFrame frame = new JFrame("Game Client");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // Crea un Canvas per disegnare il gioco
        canvas = new Canvas() {
            @Override
            public void paint(Graphics g) {
                super.paint(g);
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                gameState.render(g2, player); // Disegna lo stato del gioco
            }
        };

        canvas.setSize(1920, 1080);
        frame.add(canvas);
        frame.pack();
        frame.setVisible(true);

        // Imposta il canvas come componente focusable
        canvas.setFocusable(true);
        canvas.requestFocus();

        // Aggiungi l'InputHandler per tastiera e mouse
        InputHandler inputHandler = new InputHandler(player, canvas);
        canvas.addKeyListener(inputHandler);
        canvas.addMouseMotionListener(inputHandler);

        // Ora crea il BufferStrategy (solo dopo che il Canvas è stato aggiunto al frame)
        canvas.createBufferStrategy(3);

        // Avvia il thread del gioco
        Thread gameThread = new Thread(() -> {
            final int FPS = 60;
            final long frameTime = 1000 / FPS;

            BufferStrategy bufferStrategy = canvas.getBufferStrategy();
            if (bufferStrategy == null) {
                throw new IllegalStateException("BufferStrategy non inizializzato correttamente.");
            }

            long lastTime = System.nanoTime();
            double delta = 0;
            double nsPerUpdate = 1e9 / FPS; // Nanosecondi per frame

            while (true) {
                long now = System.nanoTime();
                delta += (now - lastTime) / nsPerUpdate;
                lastTime = now;

                // Aggiorna lo stato del gioco per ogni frame
                while (delta >= 1) {
                    gameState.updateGameState();
                    delta--; // Reset delta ogni volta che è passata una "unità" di FPS
                }

                // Rendering
                Graphics g = bufferStrategy.getDrawGraphics();
                try {
                    Graphics2D g2 = (Graphics2D) g;
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    g2.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());
                    gameState.render(g2, player); // Disegna lo stato del gioco
                } finally {
                    g.dispose(); // Libera le risorse grafiche
                }

                if (!bufferStrategy.contentsLost()) {
                    bufferStrategy.show(); // Mostra il buffer aggiornato
                }

                // Calcola il tempo rimanente per rispettare l'FPS
                long elapsedTime = System.nanoTime() - now;
                long sleepTime = frameTime - elapsedTime / 1_000_000;

                // Attendi il tempo rimanente (se necessario)
                if (sleepTime > 0) {
                    try {
                        Thread.sleep(sleepTime);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                } else {
                    System.out.println("Frame ritardato: " + (-sleepTime) + "ms");
                }
            }
        });
        gameThread.setDaemon(true);
        gameThread.start();
    }




    public static void main(String[] args) {
        new GameClient();
    }
}
