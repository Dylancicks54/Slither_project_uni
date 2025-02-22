import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class GameWindow extends JPanel {
    private List<Entity> entities;
    private GameController gameController;
    private Player player;

    public GameWindow(List<Entity> entities, GameController gameController, Player player) {
        this.entities = (entities != null) ? entities : new ArrayList<>();
        this.gameController = gameController;
        this.player = player;
        setPreferredSize(new Dimension(800, 600));
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Renderer.drawEntities(g, entities, player);
    }
}