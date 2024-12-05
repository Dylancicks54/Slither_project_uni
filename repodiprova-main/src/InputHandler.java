import java.awt.*;
import java.awt.event.*;

public class InputHandler implements KeyListener, MouseMotionListener {
    private Player player;
    private Canvas canvas;

    public InputHandler(Player player, Canvas canvas) {
        this.player = player;
        this.canvas = canvas;
        canvas.addKeyListener(this);
        canvas.addMouseMotionListener(this);
    }

    @Override
    public void keyPressed(KeyEvent e) {
        if (player == null) return;

        int keyCode = e.getKeyCode();
        switch (keyCode) {
            case KeyEvent.VK_UP:
                player.setVelocity(new Vector2D(player.getVelocity().x, -player.getSpeed()));
                break;
            case KeyEvent.VK_DOWN:
                player.setVelocity(new Vector2D(player.getVelocity().x, player.getSpeed()));
                break;
            case KeyEvent.VK_LEFT:
                player.setVelocity(new Vector2D(-player.getSpeed(), player.getVelocity().y));
                break;
            case KeyEvent.VK_RIGHT:
                player.setVelocity(new Vector2D(player.getSpeed(), player.getVelocity().y));
                break;
            case KeyEvent.VK_SPACE:
                player.activateBoost();
                break;
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {
        if (player == null) return;

        int keyCode = e.getKeyCode();
        switch (keyCode) {
            case KeyEvent.VK_UP:
            case KeyEvent.VK_DOWN:
                player.setVelocity(new Vector2D(player.getVelocity().x, 0));
                break;
            case KeyEvent.VK_LEFT:
            case KeyEvent.VK_RIGHT:
                player.setVelocity(new Vector2D(0, player.getVelocity().y));
                break;
            case KeyEvent.VK_SPACE:
                player.deactivateBoost();
                break;
        }
    }

    @Override
    public void keyTyped(KeyEvent e) {
        // Non utilizzato
    }

    @Override
    public void mouseMoved(MouseEvent e) {
        if (player == null) return;

        // Calcola l'angolo tra il centro del canvas e la posizione del mouse
        Point mousePosition = e.getPoint();
        Point canvasCenter = new Point(canvas.getWidth() / 2, canvas.getHeight() / 2);

        double angle = Math.atan2(mousePosition.y - canvasCenter.y, mousePosition.x - canvasCenter.x);
        player.setAngle(Math.toDegrees(angle));  // Imposta l'angolo del giocatore
    }

    @Override
    public void mouseDragged(MouseEvent e) {
        mouseMoved(e);  // Gestisce anche il movimento del mouse durante il drag
    }
}
