import java.awt.*;
import java.awt.event.KeyEvent;

public class GameController {
    private Player player;

    public GameController(Player player) {
        this.player = player;
    }

    public void handleKeyPress(int keyCode) {
        switch (keyCode) {
            case KeyEvent.VK_UP -> player.setVelocity(new Vector2D(player.getVelocity().x, -player.getSpeed()));
            case KeyEvent.VK_DOWN -> player.setVelocity(new Vector2D(player.getVelocity().x, player.getSpeed()));
            case KeyEvent.VK_LEFT -> player.setVelocity(new Vector2D(-player.getSpeed(), player.getVelocity().y));
            case KeyEvent.VK_RIGHT -> player.setVelocity(new Vector2D(player.getSpeed(), player.getVelocity().y));
            case KeyEvent.VK_SPACE -> player.activateBoost();
        }
    }

    public void handleKeyRelease(int keyCode) {
        switch (keyCode) {
            case KeyEvent.VK_UP, KeyEvent.VK_DOWN -> player.setVelocity(new Vector2D(player.getVelocity().x, 0));
            case KeyEvent.VK_LEFT, KeyEvent.VK_RIGHT -> player.setVelocity(new Vector2D(0, player.getVelocity().y));
            case KeyEvent.VK_SPACE -> player.deactivateBoost();
        }
    }

    public void handleMouseMove(Point mousePosition, int canvasWidth, int canvasHeight) {
        Point canvasCenter = new Point(canvasWidth / 2, canvasHeight / 2);
        double angle = Math.atan2(mousePosition.y - canvasCenter.y, mousePosition.x - canvasCenter.x);
        player.setAngle(Math.toDegrees(angle));
    }
}
