import java.awt.*;
import java.util.List;
import javax.swing.*;

public class GraphicsEngine {
    private JPanel panel;
    private List<Entity> entities;
    private Timer renderTimer;

    public GraphicsEngine(JPanel panel, List<Entity> entities) {
        this.panel = panel;
        this.entities = entities;

        // Timer per aggiornare il rendering ~60 FPS
        renderTimer = new Timer(16, e -> panel.repaint());
    }

    public void startRendering() {
        renderTimer.start();
    }

    public void stopRendering() {
        renderTimer.stop();
    }

    public void drawScene(Graphics g) {
        synchronized (entities) {
            g.clearRect(0, 0, panel.getWidth(), panel.getHeight());

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
}
