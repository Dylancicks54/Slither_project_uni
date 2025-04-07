package controller;
import model.GameState;
import view.GameViewer.GameView;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;

/**
 * Controller per il gioco single player
 */

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

    public GameView getGameView() {
        return this.gameView;
    }

    //EVENTI
    @Override
    public void keyTyped(KeyEvent e) {
    }

    @Override
    public void keyPressed(KeyEvent e) {
        //SPAZIO = ACCELERARE
        if(KeyEvent.VK_SPACE == e.getKeyCode()){
            gameState.getSnake().setAccelerating(true);
        }

        //B = RESETTARE LA PARTITA
        //@TODO non funziona il resetGame()
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

}
