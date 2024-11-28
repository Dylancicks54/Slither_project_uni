import javax.swing.*;
import javax.swing.Timer;
import java.awt.*;
import java.util.*;
import java.util.List;

public class GameWindow extends JPanel {
    private GameState gameState;

    public GameWindow(GameState gameState) {
        this.gameState = gameState;
        setPreferredSize(new Dimension(800, 600));
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        gameState.render(g);  // Assicurati che il GameState gestisca il rendering
    }

    public static void main(String[] args) {
        // Crea entità
        List<Player> players = new ArrayList<>();
        List<Bot> bots = new ArrayList<>();
        List<Food> foodItems = new ArrayList<>();

        //Aggiungi player
        Player player = new Player("Player1", null);
        players.add(player);

        //Aggiugi bot
        bots.add(new Bot(new Vector2D(100, 100), new GameState(players, bots, foodItems), null));

        // Crea il GameState
        GameState gameState = new GameState(players, bots, foodItems);

        // Crea la finestra e mostra il gioco
        JFrame frame = new JFrame("Slither.io - Semplice");
        //GameWindow window = new GameWindow(gameState);
        frame.setSize(1920, 1080);
        //frame.pack();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        Canvas canvas = new Canvas() {
            @Override
            public void paint(Graphics g) {
                super.paint(g);
                gameState.render(g);
                // Disegna lo stato del gioco
            }
        };

        canvas.setSize(800, 600);
        frame.add(canvas);
        frame.setVisible(true);

        InputHandler inputHandler = new InputHandler(player,canvas);

        canvas.addKeyListener(inputHandler);
        canvas.setFocusable(true);

        // Avvia il loop del gioco
        /*
        new Timer(100, e -> {
            gameState.updateGameState();
            window.repaint();  // Rendi la finestra per aggiornare il gioco
        }).start();
         */

        // Game loop usando un thread
        Thread gameThread = new Thread(() -> {
            final int FPS = 60;
            final long frameTime = 1000 / FPS;

            while (true) {
                long startTime = System.currentTimeMillis();

                // Aggiorna lo stato del gioco
                gameState.updateGameState();

                // Ridisegna la scena nel thread AWT
                SwingUtilities.invokeLater(() -> canvas.repaint());

                // Calcola il tempo rimanente per rispettare l'FPS
                long elapsedTime = System.currentTimeMillis() - startTime;
                long sleepTime = frameTime - elapsedTime;

                // Attendi il tempo rimanente (se necessario)
                if (sleepTime > 0) {
                    try {
                        Thread.sleep(sleepTime);
                    } catch (InterruptedException ex) {
                        ex.printStackTrace();
                    }
                }
            }
        });

        gameThread.start(); // Avvia il thread del gioco

        System.out.println("Inizio del gioco...");
    }
}
