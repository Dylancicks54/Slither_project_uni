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

        drawBackground(g2d);
        //drawGrid(g2d, deltaX, deltaY);
        drawEntities(g2d, deltaX, deltaY);

        // Disegna i confini della mappa
        g2d.setColor(Color.RED);
        g2d.setStroke(new BasicStroke(9));
        g2d.drawRect(-deltaX, -deltaY, GameState.MAP_WIDTH, GameState.MAP_HEIGHT);
        zonaRossa(g2d, deltaX, deltaY);


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

    private void zonaRossa(Graphics2D g2d, int deltaX, int deltaY) {
        int mapX = -deltaX;
        int mapY = -deltaY;
        int mapWidth = GameState.MAP_WIDTH;
        int mapHeight = GameState.MAP_HEIGHT;

        Color coloreZona = new Color(115, 1, 1, 100);

        g2d.setColor(coloreZona);

        g2d.fillRect(0, 0, getWidth(), mapY);
        g2d.fillRect(0, mapY + mapHeight, getWidth(), getHeight() - (mapY + mapHeight));
        g2d.fillRect(0, mapY, mapX, mapHeight);
        g2d.fillRect(mapX + mapWidth, mapY, getWidth() - (mapX + mapWidth), mapHeight);
    }

    private void drawBackground(Graphics2D g2d) {
        Color baseColor = new Color(15, 15, 20);
        Color coloreOttagono = new Color(30, 30, 40);

        g2d.setColor(baseColor);
        g2d.fillRect(0, 0, getWidth(), getHeight());

        int size = 50;
        int spacing = size + 10;
        int spazioInMezzo = (int) (spacing * Math.sqrt(2));

        int deltaX = (int) (-player.getPosition().getX() % spazioInMezzo);
        int deltaY = (int) (-player.getPosition().getY() % spazioInMezzo);

        for (int x = deltaX - spazioInMezzo; x < getWidth() + spazioInMezzo; x += spazioInMezzo) {
            for (int y = deltaY - spazioInMezzo; y < getHeight() + spazioInMezzo; y += spazioInMezzo) {
                disegnaOttagono(g2d, x, y, size, coloreOttagono);
            }
        }
    }

    private void disegnaOttagono(Graphics2D g2d, int x, int y, int size, Color color) {
        int s = size / 3;

        int[] puntiX = { x, x + s, x + size - s, x + size, x + size, x + size - s, x + s, x };
        int[] puntiY = { y + s, y, y, y + s, y + size - s, y + size, y + size, y + size - s };

        g2d.setColor(color);
        g2d.fillPolygon(puntiX, puntiY, 8);
    }



    private Color getRainbowColor() {
        float hue;
        do {
            hue = (float) Math.random();
        } while ((hue >= 0.9 || hue <= 0.1) || (hue >= 0.55 && hue <= 0.7));

        return Color.getHSBColor(hue, 1.0f, 1.0f);
    }

    private void drawEntities(Graphics2D g2d, int offsetX, int offsetY) {
        List<Player> players = gameState.getPlayers();
        List<Bot> bots = gameState.getBots();
        List<Food> foodItems = gameState.getFoodItems();

        g2d.setColor(getRainbowColor());
        for (Food food : foodItems) {
            int screenX = (int) food.getPosition().getX() - offsetX;
            int screenY = (int) food.getPosition().getY() - offsetY;
            g2d.fillOval(screenX - 5, screenY - 5, 10, 10);
        }

        for (Bot bot : bots) {
            g2d.setColor(Color.RED);
            int screenX = (int) bot.getPosition().getX() - offsetX;
            int screenY = (int) bot.getPosition().getY() - offsetY;
            g2d.fillOval(screenX - 5, screenY - 5, 15, 15);

            g2d.setColor(Color.RED);
            for (Segment segment : bot.getBodySegments()) {
                int segX = (int) segment.getPosition().getX() - offsetX;
                int segY = (int) segment.getPosition().getY() - offsetY;
                g2d.fillOval(segX - 5, segY - 5, 10, 10);
            }
        }

        for (Player p : players) {
            g2d.setColor(Color.BLUE);
            int screenX = (int) p.getPosition().getX() - offsetX;
            int screenY = (int) p.getPosition().getY() - offsetY;
            g2d.fillOval(screenX - 10, screenY - 10, 15, 15);

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
