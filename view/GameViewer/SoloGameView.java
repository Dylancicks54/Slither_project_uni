package view.GameViewer;

import controller.GameController;
import model.AISnake;
import model.Food;
import model.GameState;
import model.Snake;

import java.awt.*;
import javax.swing.*;
import java.util.ArrayList;
import java.util.List;
public class SoloGameView extends GameView {
    private GameController gc;

    //Dove vengono salvati le immagini degli oggetti di gioco
    private Image[] foodImage;
    private Image snakeImage;
    private Image aisnakeImage;

    private JLabel timerLabel;

    public SoloGameView() {

        this.gc=new GameController(this);

        snakeImage=new ImageIcon(this.getClass().getResource("/resources/serpent.png")).getImage();
        aisnakeImage=new ImageIcon(this.getClass().getResource("/resources/serpent2.png")).getImage();

        foodImage=new Image[4];
        foodImage[0] = new ImageIcon(this.getClass().getResource("/resources/food.png")).getImage();
        foodImage[1] = new ImageIcon(this.getClass().getResource("/resources/food1.png")).getImage();
        foodImage[2] = new ImageIcon(this.getClass().getResource("/resources/food2.png")).getImage();
        foodImage[3] = new ImageIcon(this.getClass().getResource("/resources/food3.png")).getImage();

        timerLabel = new JLabel();
        timerLabel.setFont(new Font("Arial", Font.BOLD, 20));
        timerLabel.setForeground(Color.WHITE);
        add(timerLabel);
    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);

        //Disegno lo sfondo
        Graphics2D g2d = (Graphics2D) g;
        drawBackground(g2d);

        //Calcolo la distanza del serpente dal centro dello schermo
        int deltaX = getWidth() / 2 - gc.getGameState().getSnake().getBody().get(0).getX();
        int deltaY = getHeight() / 2 - gc.getGameState().getSnake().getBody().get(0).getY();

        //Coloro l'area esterna di gioco
        zonaRossa(g2d, deltaX, deltaY);

        //Disegno il giocatore
        for (int i = 0; i < gc.getGameState().getSnake().getBody().size(); i++) {
            int x = gc.getGameState().getSnake().getBody().get(i).getX() + deltaX;
            int y = gc.getGameState().getSnake().getBody().get(i).getY() + deltaY;

            g.drawImage(snakeImage, x, y, Snake.SEGMENT_SIZE, Snake.SEGMENT_SIZE, this);
        }

        //PARTE BOT
        g2d.setColor(Color.RED);
        g2d.setStroke(new BasicStroke(3));
        List<AISnake> snakes = new ArrayList<>(gc.getGameState().getAiSnakes());

        for (AISnake snake : snakes) {
            //Evidenzio il bersaglio del bot
            g2d.drawOval(snake.getLookingTo().getX() + deltaX, snake.getLookingTo().getY() + deltaY, 15, 15);

            //Disegno i bot
            for (int i = 0; i < snake.getBody().size(); i++) {
                int x = snake.getBody().get(i).getX() + deltaX;
                int y = snake.getBody().get(i).getY() + deltaY;

                g.drawImage(aisnakeImage, x, y, Snake.SEGMENT_SIZE, Snake.SEGMENT_SIZE, this);
            }
        }

        //Disegno minimappa
        drawMiniMap(g2d);

        g2d.setColor(Color.RED);
        g2d.setStroke(new BasicStroke(3));
        //BORDO SINISTRA
        g2d.drawLine(deltaX, deltaY, deltaX, deltaY + GameState.BORDER_Y);
        //BORDO ALTO
        g2d.drawLine(deltaX, deltaY, deltaX + GameState.BORDER_X, deltaY);
        //BORDO DESTRO
        g2d.drawLine(deltaX + GameState.BORDER_X, deltaY, deltaX + GameState.BORDER_X, deltaY + GameState.BORDER_Y);
        //BORDO BASSO
        g2d.drawLine(deltaX, deltaY + GameState.BORDER_Y, deltaX + GameState.BORDER_X, deltaY + GameState.BORDER_Y);

        //Metto il tempo ed il punteggio
        timerLabel.setText("Tempo: " + gc.getGameState().getRemainingTime() + "s");
        g.setColor(Color.WHITE);
        g.setFont(new Font("Arial", Font.PLAIN, 16));
        g.drawString("Punteggio: " + gc.getGameState().getScore(), 10, 20);

        //DISEGNO IL CIBO
        //uso un loop for per risolvere ConcurrentModificationException
        for (int i = 0; i < gc.getGameState().getFoods().size(); i++) {
            Food food = gc.getGameState().getFoods().get(i);
            int x = food.getX() + deltaX;
            int y = food.getY() + deltaY;
            g.drawImage(foodImage[food.getColore()], x, y, 10, 10, this);
        }
    }

    /**
     * Metodo per disegnare il background della mappa
     * @param g2d istanza di Graphics2D
     */
    private void drawBackground(Graphics2D g2d) {
        Color baseColor = new Color(15, 15, 20);
        Color coloreOttagono = new Color(30, 30, 40);

        g2d.setColor(baseColor);
        g2d.fillRect(0, 0, getWidth(), getHeight());

        int size = 50;
        int spacing = size + 10;
        int spazioInMezzo = (int) (spacing * Math.sqrt(2));
        int deltaX;
        int deltaY;

        deltaX =  (-gc.getGameState().getSnake().getBody().getFirst().getX() % spazioInMezzo);
        deltaY =  (-gc.getGameState().getSnake().getBody().getFirst().getY() % spazioInMezzo);

        for (int x = deltaX - spazioInMezzo; x < getWidth() + spazioInMezzo; x += spazioInMezzo) {
            for (int y = deltaY - spazioInMezzo; y < getHeight() + spazioInMezzo; y += spazioInMezzo) {
                disegnaOttagono(g2d, x, y, size, coloreOttagono);
            }
        }
    }

    /**
     * Metodo per disegna un ottagono
     * @param g2d istanza di Graphics2D
     * @param x ascissa del punto di partenza
     * @param y ordinata del punto di partenza
     * @param size grandezza dell'ottagono
     * @param color colore dell'ottagono
     */
    private void disegnaOttagono(Graphics2D g2d, int x, int y, int size, Color color) {
        int s = size / 3;

        int[] puntiX = { x, x + s, x + size - s, x + size, x + size, x + size - s, x + s, x };
        int[] puntiY = { y + s, y, y, y + s, y + size - s, y + size, y + size, y + size - s };

        g2d.setColor(color);
        g2d.fillPolygon(puntiX, puntiY, 8);
    }

    /**
     * Metodo per disegnare la zona fuori dalla mappa
     * @param g2d istanza di Graphics2D
     * @param deltaX ascissa riferimento mappa
     * @param deltaY ordinata riferimento mappa
     */
    private void zonaRossa(Graphics2D g2d, int deltaX, int deltaY) {
        Color zoneColor = new Color(115, 1, 1, 150);
        g2d.setColor(zoneColor);

        int borderLeft   = deltaX;
        int borderTop    = deltaY;

        int borderRight  = deltaX + 1550;
        int borderBottom = deltaY + 1550;

        int screenWidth  = getWidth();
        int screenHeight = getHeight();

        // Zona sopra: se il bordo superiore è maggiore di 0, riempie l'area in alto
        if (borderTop > 0) {
            g2d.fillRect(0, 0, screenWidth, borderTop);
        }

        // Zona sotto: se il bordo inferiore è minore dell'altezza dello schermo
        if (borderBottom < screenHeight) {
            g2d.fillRect(0, borderBottom, screenWidth, screenHeight - borderBottom);
        }

        // Zona a sinistra: riempie l'area a sinistra del bordo
        if (borderLeft > 0) {
            g2d.fillRect(0, borderTop, borderLeft, borderBottom - borderTop);
        }

        // Zona a destra: riempie l'area a destra del bordo
        if (borderRight < screenWidth) {
            g2d.fillRect(borderRight, borderTop, screenWidth - borderRight, borderBottom - borderTop);
        }
    }

    /**
     * Metodo per disegnare la minimappa
     * @param g2d istanza di Graphics2D
     */
    public void drawMiniMap(Graphics2D g2d) {
        int size = 150;
        int spazio = 20;
        int raggio = size / 2;

        int centroX = getWidth() - raggio - spazio;
        int centroY = getHeight() - raggio - spazio;

        g2d.setColor(new Color(0, 0, 0, 150));
        g2d.fillOval(centroX - raggio, centroY - raggio, size, size);
        g2d.setColor(Color.DARK_GRAY);
        g2d.setStroke(new BasicStroke(1));
        g2d.drawOval(centroX - raggio, centroY - raggio, size, size);

        int mapWidth = getWidth();
        int mapHeight = getHeight();

        double scaleX = (double) size / mapWidth;
        double scaleY = (double) size / mapHeight;

        int playerX = (int) (gc.getGameState().getSnake().getBody().getFirst().getX() * scaleX);
        int playerY = (int) (gc.getGameState().getSnake().getBody().getFirst().getY() * scaleY);

        int offsetX = playerX - (size / 2);
        int offsetY = playerY - (size / 2);
        double distanza = Math.sqrt(offsetX * offsetX + offsetY * offsetY);

        // questo serve per non far uscire il pallino fuori dalla mini mappa
        if (distanza > raggio - 5) {
            double controlloBordo = (raggio - 5) / distanza;
            playerX = (int) (size / 2 + offsetX * controlloBordo);
            playerY = (int) (size / 2 + offsetY * controlloBordo);
        }
        g2d.setColor(Color.BLUE);
        g2d.fillOval(centroX - raggio + playerX - 5, centroY - raggio + playerY - 5, 10, 10);
    }

    /**
     * Metodo per aggiornare il timer di gioco
     */
    public void updateTimerLabel() {
        if(!(timerLabel==null))
            timerLabel.setText("Tempo: " + gc.getGameState().getRemainingTime() + "s");
        repaint();
    }

    public GameController getGc() {
        return gc;
    }

    /**
     * Metodo per mostrare il messaggio allo scadere del tempo
     */
    public void showTimeUpDialog() {
        String winningMessage = "";

        // Trovo il bot con il punteggio più alto
        int highestAIScore = Integer.MIN_VALUE;
        for (AISnake aiSnake : getGc().getGameState().getAiSnakes()) {
            // Il punteggio è basato sulla lungezza dello snake, sottraendo 5 perchè è la lunghezza iniziale
            int aiScore = aiSnake.getBody().size() - 5;
            highestAIScore = Math.max(highestAIScore, aiScore);
        }

        // Confronto il punteggio del giocatore con quello del bot col punteggio più alto
        if (getGc().getGameState().getScore() > highestAIScore) {
            winningMessage = "Hai vinto! Il tuo punteggio è: " + getGc().getGameState().getScore();
        } else {
            winningMessage = "Hai perso! Il tuo punteggio è " + getGc().getGameState().getScore() +
                    " ma il bot col punteggio più alto è: " + highestAIScore;
        }

        // Mostro il messaggio con l'esito
        JOptionPane.showMessageDialog(this, winningMessage);
    }

}
