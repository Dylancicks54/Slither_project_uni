import javax.swing.*;
import java.awt.*;
import java.util.List;

public class GameWindow extends JPanel {
    private List<Entity> entities;
    private GraphicsEngine graphicsEngine;

    public GameWindow(List<Entity> entities) {
        this.entities = entities;
        this.graphicsEngine = new GraphicsEngine(this, entities); // Integrazione con GraphicsEngine
        setPreferredSize(new Dimension(800, 600));
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        synchronized (entities) {
            if (entities.isEmpty()) {
                g.setColor(Color.RED);
                g.drawString("Nessuna entità da disegnare.", 50, 50);
                return;
            }

            for (Entity entity : entities) {
                if (entity instanceof Player) {
                    g.setColor(Color.BLUE);
                } else if (entity instanceof Bot) {
                    g.setColor(Color.RED);
                } else if (entity instanceof Food) {
                    g.setColor(Color.GREEN);
                }
                g.fillOval((int) entity.getPosition().x, (int) entity.getPosition().y, (int) entity.getSize(), (int) entity.getSize());
            }
        }
    }


    public void startRendering() {
        graphicsEngine.startRendering();
    }

    public void stopRendering() {
        graphicsEngine.stopRendering();
    }
}
