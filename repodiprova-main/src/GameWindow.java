import javax.swing.*;
import java.awt.*;
import java.util.List;

public class GameWindow extends JPanel {
    private GameState gameState;
    private GameController gameController;
    private Player player;

    public GameWindow(GameState gameState, GameController gameController, Player player) {
        this.gameState = gameState;
        this.gameController = gameController;
        this.player = player;
        setPreferredSize(new Dimension(1920, 1080));
        setFocusable(true);
        requestFocusInWindow();
        // Avvia il game loop
        Timer timer = new Timer(16, e -> updateGame());
        timer.start();

    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;

        int deltaX = (int) player.getPosition().getX() - getWidth() / 2;
        int deltaY = (int) player.getPosition().getY() - getHeight() / 2;

        drawGrid(g2d, deltaX, deltaY);
        drawEntities(g2d, deltaX, deltaY);

        // Disegna i confini della mappa
        g2d.setColor(Color.RED);
        g2d.setStroke(new BasicStroke(9));
        g2d.drawRect(-deltaX, -deltaY, GameState.MAP_WIDTH, GameState.MAP_HEIGHT);

        // Mostra la schermata di morte
        if (!player.isAlive()) {
            g2d.setColor(new Color(255, 0, 0, 150)); // Sfondo semi-trasparente
            g2d.fillRect(getWidth() / 2 - 200, getHeight() / 2 - 100, 400, 200); // Rettangolo menu

            g2d.setColor(Color.WHITE);
            g2d.setFont(new Font("Arial", Font.BOLD, 24));
            g2d.drawString("YOU DIED", getWidth() / 2 - 50, getHeight() / 2 - 50);
            g2d.drawString("Press R to Respawn", getWidth() / 2 - 100, getHeight() / 2);
            g2d.drawString("Press Q to Quit", getWidth() / 2 - 80, getHeight() / 2 + 40);
        }
    }


    private void drawGrid(Graphics2D g2d, int deltaX, int deltaY) {
        g2d.setColor(Color.BLACK);
        g2d.fillRect(0, 0, getWidth(), getHeight());
        g2d.setColor(Color.GRAY);

        int startX = (deltaX / 50) * 50;
        int startY = (deltaY / 50) * 50;

        for (int x = startX; x < deltaX + getWidth(); x += 50) {
            int screenX = x - deltaX;
            g2d.drawLine(screenX, 0, screenX, getHeight());
        }

        for (int y = startY; y < deltaY + getHeight(); y += 50) {
            int screenY = y - deltaY;
            g2d.drawLine(0, screenY, getWidth(), screenY);
        }
    }
    private Color getRainbowColor() {
        float hue;
        do {
            hue = (float) Math.random(); // Genera un valore casuale tra 0.0 e 1.0
        } while ((hue >= 0.9 || hue <= 0.1) || (hue >= 0.55 && hue <= 0.7));
        // Evita tonalità di ROSSO (0.9-1.0 e 0.0-0.1) e BLU (0.55-0.7)

        return Color.getHSBColor(hue, 1.0f, 1.0f);
    }

    private void drawEntities(Graphics2D g2d, int offsetX, int offsetY) {
        List<Player> players = gameState.getPlayers();
        List<Bot> bots = gameState.getBots();
        List<Food> foodItems = gameState.getFoodItems();

        // Disegna il cibo
        g2d.setColor(getRainbowColor());
        for (Food food : foodItems) {
            int screenX = (int) food.getPosition().getX() - offsetX;
            int screenY = (int) food.getPosition().getY() - offsetY;
            g2d.fillOval(screenX - 5, screenY - 5, 10, 10);
        }

        // Disegna i bot e i loro segmenti
        for (Bot bot : bots) {
            g2d.setColor(Color.RED);
            int screenX = (int) bot.getPosition().getX() - offsetX;
            int screenY = (int) bot.getPosition().getY() - offsetY;
            g2d.fillOval(screenX - 5, screenY - 5, 15, 15);

            // Disegna i segmenti del bot
            g2d.setColor(Color.RED);
            for (Segment segment : bot.getBodySegments()) {
                int segX = (int) segment.getPosition().getX() - offsetX;
                int segY = (int) segment.getPosition().getY() - offsetY;
                g2d.fillOval(segX - 5, segY - 5, 10, 10);
            }
        }

        // Disegna il player e i suoi segmenti
        for (Player p : players) {
            g2d.setColor(Color.BLUE);
            int screenX = (int) p.getPosition().getX() - offsetX;
            int screenY = (int) p.getPosition().getY() - offsetY;
            g2d.fillOval(screenX - 10, screenY - 10, 15, 15);

            // Disegna i segmenti del player
            g2d.setColor(Color.BLUE);
            for (Segment segment : p.getBodySegments()) {
                int segX = (int) segment.getPosition().getX() - offsetX;
                int segY = (int) segment.getPosition().getY() - offsetY;
                g2d.fillOval(segX - 5, segY - 5, 10, 10);
            }
        }
    }

    /*private void drawEntityWithSegments(Graphics2D g2d, Entity entity, Color color, int offsetX, int offsetY) {
        g2d.setColor(color);

        int headSize = 20;  // Dimensione della testa
        int segmentSize = (int) (headSize * 0.8);  // I segmenti sono più piccoli dell'80%

        if (entity instanceof Bot) {
            Bot bot = (Bot) entity;
            for (Segment segment : bot.getBodySegments()) {
                drawSegment(g2d, segment, segmentSize, offsetX, offsetY);
            }
        } else if (entity instanceof Player) {
            Player player = (Player) entity;
            for (Segment segment : player.getBodySegments()) {
                drawSegment(g2d, segment, segmentSize, offsetX, offsetY);
            }
        }

        // Disegna la testa dell'entità
        int screenX = (int) entity.getPosition().getX() - offsetX;
        int screenY = (int) entity.getPosition().getY() - offsetY;
        g2d.fillOval(screenX - headSize / 2, screenY - headSize / 2, headSize, headSize);
    }*/

    private void drawSegment(Graphics2D g2d, Segment segment, int segmentSize, int offsetX, int offsetY) {
        int screenX = (int) segment.getPosition().getX() - offsetX;
        int screenY = (int) segment.getPosition().getY() - offsetY;
        g2d.fillOval(screenX - segmentSize / 2, screenY - segmentSize / 2, segmentSize, segmentSize);
    }

    private void updateGame() {
        gameState.updateGameState(); // Aggiorna la logica del gioco
        repaint();  // Ridisegna lo schermo
    }
}
