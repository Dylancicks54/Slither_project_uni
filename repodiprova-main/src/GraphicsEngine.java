import java.awt.*;
import java.util.List;

public class GraphicsEngine {
    private Canvas canvas;
    private Thread renderThread;
    private List<Entity> entities;

    public GraphicsEngine(Canvas canvas, List<Entity> entities) {
        this.canvas = canvas;
        this.entities = entities;
    }

    public void startRendering() {
        renderThread = new Thread(() -> {
            while (!Thread.currentThread().isInterrupted()) {
                drawScene();
                try {
                    Thread.sleep(16); // ~60 FPS
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        });
        renderThread.start();
    }

    public void stopRendering() {
        if (renderThread != null) {
            renderThread.interrupt();
        }
    }

    public void drawScene() {
        // Render all entities
        Graphics g = canvas.getGraphics();
        if (g == null) return;

        g.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());

        for (Entity entity : entities) {
            if (entity instanceof Player) {
                g.setColor(Color.BLUE);
            } else if (entity instanceof Bot) {
                g.setColor(Color.RED);
            } else if (entity instanceof Food) {
                g.setColor(Color.GREEN);
            }
            g.fillOval((int) entity.position.x, (int) entity.position.y, (int) entity.size, (int) entity.size);
        }
    }
}
