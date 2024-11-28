import java.awt.*;
import java.awt.event.*;

public class InputHandler implements KeyListener, MouseListener, MouseMotionListener {
    private Player player;

    public InputHandler(Player player, Canvas canvas) {
        this.player = player;
        canvas.addMouseMotionListener(this);
        canvas.addKeyListener(this);
    }

    @Override
    public void mouseDragged(MouseEvent e) {
        updateAngle(e);
    }

    @Override
    public void mouseMoved(MouseEvent e) {
        updateAngle(e);
    }

    private void updateAngle(MouseEvent e) {
        Point mousePosition = e.getPoint();
        double angle = Math.atan2(mousePosition.y - player.getPosition().y, mousePosition.x - player.getPosition().x);
        player.setAngle(Math.toDegrees(angle));
    }

    @Override
    public void keyTyped(KeyEvent e) {

    }

    @Override
    public void keyPressed(KeyEvent e) {
        int keyCode = e.getKeyCode();
        if (keyCode == KeyEvent.VK_UP) {
            player.getVelocity().y = -1; // Muove il giocatore su
        } else if (keyCode == KeyEvent.VK_DOWN) {
            player.getVelocity().y = 1; // Muove il giocatore giù
        } else if (keyCode == KeyEvent.VK_LEFT) {
            player.getVelocity().x = -1; // Muove il giocatore a sinistra
        } else if (keyCode == KeyEvent.VK_RIGHT) {
            player.getVelocity().x = 1; // Muove il giocatore a destra
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {
        // Quando il tasto viene rilasciato, fermiamo il movimento
        int keyCode = e.getKeyCode();
        if (keyCode == KeyEvent.VK_UP || keyCode == KeyEvent.VK_DOWN) {
            player.getVelocity().y = 0;
        } else if (keyCode == KeyEvent.VK_LEFT || keyCode == KeyEvent.VK_RIGHT) {
            player.getVelocity().x = 0;
        }
    }

    @Override
    public void mouseClicked(MouseEvent e) {

    }

    @Override
    public void mousePressed(MouseEvent e) {

    }

    @Override
    public void mouseReleased(MouseEvent e) {

    }

    @Override
    public void mouseEntered(MouseEvent e) {

    }

    @Override
    public void mouseExited(MouseEvent e) {

    }
}
