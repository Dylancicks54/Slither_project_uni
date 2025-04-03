package controller;
import model.GameState;
import model.Direction;
import view.GameViewer.GameView;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;

public class GameController extends AbstractGameController implements KeyListener {

    private GameState gameState;
    private GameView gameView;

    public GameController(GameView gameView){
        this.gameView=gameView;
        this.gameState=new GameState(this);
    }

    public GameState getGameState() {
        return this.gameState;
    }

    @Override
    public void keyTyped(KeyEvent e) {
    }

    @Override
    public void keyPressed(KeyEvent e) {
            if(KeyEvent.VK_SPACE == e.getKeyCode()){
                gameState.getSnake().setAccelerating(true);
            }

        if (KeyEvent.VK_B == e.getKeyCode()) {
             gameState.resetGame();
        }
    }
    @Override
    public void keyReleased(KeyEvent e) {
        switch (e.getKeyCode()) {
            case KeyEvent.VK_SPACE:
                gameState.getSnake().setAccelerating(false);
                break;
        }
    }
    public void mouseMoved(MouseEvent e) {
            int mouseX = e.getX();
            int mouseY = e.getY();

            gameState.getSnake().setMouseX(mouseX);
            gameState.getSnake().setMouseY(mouseY);
    }

    public GameView getGv() {
        return this.gameView;
    }
}
