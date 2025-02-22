/*import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferStrategy;
import java.util.ArrayList;
import java.util.List;

public class Main {
    public static void main(String[] args) {
        // Crea una lista di giocatori, bot e cibo
        List<Player> players = new ArrayList<>();
        List<Bot> bots = new ArrayList<>();
        List<Food> foodItems = new ArrayList<>();


        // Aggiungi un giocatore
        Player player = new Player("Player1", null);
        players.add(player);

        // Aggiungi qualche bot
        GameState gameState = new GameState(players, bots, foodItems);
        for(int i = 0; i< 3; i++){
            bots.add(new Bot(new Vector2D(Math.random() * 800, Math.random() * 800), gameState, null));
        }
        for(int i = 0; i< 50; i++){
            foodItems.add(new Food(new Vector2D(Math.random() * 1000, Math.random() * 1000), 10));
        }


        // Crea la finestra del gioco
        JFrame gameWindow = new JFrame("Slither Game");
        gameWindow.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);



        // Crea un canvas per disegnare il gioco
        Canvas canvas = new Canvas() {

            @Override
            public void paint(Graphics g) {

                super.paint(g);
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                gameState.render(g2,player);
                // Disegna lo stato del gioco
                if(!player.isAlive()){
                    g2.drawString("Sei Morto",150,150);
                }
            }

        };

        canvas.setSize(1920, 1080);

        gameWindow.add(canvas);
        gameWindow.setResizable(false);
        gameWindow.pack(); // Adatta la finestra al contenuto
        gameWindow.setVisible(true);

        canvas.setFocusable(true);
        canvas.requestFocus();
        InputHandler inputHandler = new InputHandler(player, canvas);
        canvas.addKeyListener(inputHandler);

        canvas.createBufferStrategy(3);

        Thread gameThread = new Thread(() -> {
            final int FPS = 60;
            final long frameTime = 1000 / FPS;

            // Ottieni il BufferStrategy dal Canvas
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
                    delta--;
                }

                // Rendering
                Graphics g = bufferStrategy.getDrawGraphics();
                try {
                    Graphics2D g2 = (Graphics2D) g;
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    g2.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());
                    gameState.render(g2, player); // Disegna lo stato del gioco
                } finally {
                    g.dispose();
                }

                // Mostra il frame
                if (!bufferStrategy.contentsLost()) {
                    bufferStrategy.show();
                }

                // Calcola il tempo rimanente per rispettare l'FPS
                long elapsedTime = System.nanoTime() - now;
                long sleepTime = frameTime - elapsedTime / 1_000_000;

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

        gameThread.setDaemon(true); // Chiudi il thread quando il programma termina
        gameThread.start(); // Avvia il thread del gioco

        System.out.println("Inizio del gioco...");
    }
}

 */