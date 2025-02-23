/*import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;

public class InputHandler implements KeyListener, MouseMotionListener {
    private GameController gameController;
    private Canvas canvas;

    public InputHandler(GameController gameController, Canvas canvas) {
        this.gameController = gameController;
        this.canvas = canvas;
        canvas.addKeyListener(this);
        canvas.addMouseMotionListener(this);
    }

    @Override
    public void keyPressed(KeyEvent e) {
        gameController.handleKeyPress(e.getKeyCode());
    }

    @Override
    public void keyReleased(KeyEvent e) {
        gameController.handleKeyRelease(e.getKeyCode());
    }

    @Override
    public void keyTyped(KeyEvent e) {}

    @Override
    public void mouseMoved(MouseEvent e) {
        gameController.handleMouseMove(e.getPoint(), canvas.getWidth(), canvas.getHeight());
    }

    @Override
    public void mouseDragged(MouseEvent e) {
        mouseMoved(e);
    }
}
*/