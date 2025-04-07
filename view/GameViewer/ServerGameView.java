package view.GameViewer;

import Net.Client;
import Net.GameServer;
import controller.OnlineGameController;
import model.Food;
import model.Pair;
import Net.Serialize;
import model.Snake;
import view.ShowPreLobby;
import javax.swing.*;
import java.awt.*;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Vista per la partita multiplayer
 */
public class ServerGameView extends GameView {

    private final OnlineGameController gc;
    private final Client client;

    //Immagini del cibo
    private final Image[] foodImage;
    //Immagini del corpo dello snake
    private final Image snakeImage;

    public ServerGameView(Client client) {
        this.client=client;
        this.gc = new OnlineGameController(client);

        snakeImage=new ImageIcon(Objects.requireNonNull(this.getClass().getResource("/resources/serpent.png"))).getImage();

        foodImage=new Image[4];
        foodImage[0] = new ImageIcon(Objects.requireNonNull(this.getClass().getResource("/resources/food.png"))).getImage();
        foodImage[1] = new ImageIcon(Objects.requireNonNull(this.getClass().getResource("/resources/food1.png"))).getImage();
        foodImage[2] = new ImageIcon(Objects.requireNonNull(this.getClass().getResource("/resources/food2.png"))).getImage();
        foodImage[3] = new ImageIcon(Objects.requireNonNull(this.getClass().getResource("/resources/food3.png"))).getImage();
    }


    public void paintComponent(Graphics g) {

            //Disconnetto se il cliente è chiuso
            if(client.isClosed()){
                showLoseDialog();
                new ShowPreLobby().setVisible(true);
                closeCurrentGameWindow();
            }

            Graphics2D g2d = (Graphics2D) g;

            //Disegno lo sfondo
            drawBackground(g2d);

            //Incomincio a disegnare gli oggetti di gioco se ho ricevuto il messaggio dal server
            if(client.getMessageFromServer()!=null) {

                //Recupero gli snake
                Map<String, List<Pair>> snakes = Serialize.deserializeSnakes(client.getSnakes());
                //Recupero i foods
                List<Pair> foods = Serialize.deserializeFoods(client.getFoods());

                int offsetX = 0;
                int offsetY = 0;

                //Cambio l'offset di ogni giocatore in modo tale che ogni giocatore vede lo snake al centro della finestra
                offsetX = getWidth() / 2 - snakes.get(client.getUserName()).get(0).getX();
                offsetY = getHeight() / 2 - snakes.get(client.getUserName()).get(0).getY();

                //Disegno la zona rossa
                zonaRossa(g2d, offsetX, offsetY);

                //Disegno ogni serpente
                for (Map.Entry<String, List<Pair>> entry : snakes.entrySet()) {
                    List<Pair> snakePos = entry.getValue();
                    g.drawString(entry.getKey(), snakePos.get(0).getX() + offsetX - (entry.getKey().length() * 3), snakePos.get(0).getY() + offsetY);

                    for (int i = 0; i < snakePos.size(); i++) {
                        int x = snakePos.get(i).getX() + offsetX;
                        int y = snakePos.get(i).getY() + offsetY;
                        g.drawImage(snakeImage, x, y, Snake.SEGMENT_SIZE, Snake.SEGMENT_SIZE, this);
                    }
                }

                //Disengo il punteggio nell'angolo in alto a sinistra
                g.setColor(Color.WHITE);
                g.setFont(new Font("Arial", Font.PLAIN, 16));
                g.drawString("Punteggio: " + (snakes.get(client.getUserName()).size() - 5), 10, 20);

                //Disegno il cibo
                for (Pair food : foods) {
                    //Cambio il colore dipendente al valore di x, dando l'impressione di casualità nei colori
                    //change color depending on x value, gives the impression of randomness in color
                    g.drawImage(foodImage[food.getX() % 4], food.getX() + offsetX, food.getY() + offsetY, Food.SIZE, Food.SIZE, this);
                }

            }
        repaint();
    }

    /**
     * Metodo per disegnare lo sfondo del mondo di gioco
     * @param g2d istanza di Graphics2D
     */
    private void drawBackground(Graphics2D g2d) {
        Color baseColor = new Color(15, 15, 20);
        Color coloreOttagono = new Color(30, 30, 40);

        // Disegna lo sfondo di base
        g2d.setColor(baseColor);
        g2d.fillRect(0, 0, getWidth(), getHeight());

        int size = 50;
        int spacing = size + 10;
        int gridSpacing = (int)(spacing * Math.sqrt(2)); // spaziatura per la griglia

        int[] playerPos = gc.getPlayerPosition();

        // Consideriamo il giocatore al centro della view
        int centerX = getWidth() / 2;
        int centerY = getHeight() / 2;

        // Calcola le coordinate del punto in alto a sinistra del mondo visibile
        int worldX0 = playerPos[0] - centerX;
        int worldY0 = playerPos[1] - centerY;

        // Calcola l'offset relativo alla griglia usando il modulo
        int offsetX = -(worldX0 % gridSpacing);
        int offsetY = -(worldY0 % gridSpacing);
        // In caso di modulo negativo, aggiusta l'offset
        if (offsetX > 0) offsetX -= gridSpacing;
        if (offsetY > 0) offsetY -= gridSpacing;

        // Disegna la griglia di ottagoni coprendo l'intera area visibile
        for (int x = offsetX; x < getWidth(); x += gridSpacing) {
            for (int y = offsetY; y < getHeight(); y += gridSpacing) {
                disegnaOttagono(g2d, x, y, size, coloreOttagono);
            }
        }
    }

    /**
     * Metodo per disegnare un ottagono
     * @param g2d Graphics2D
     * @param x ascissa punto di partenza
     * @param y ordinata punto di partenza
     * @param size grandezza ottagono
     * @param color colore ottagono
     */
    private void disegnaOttagono(Graphics2D g2d, int x, int y, int size, Color color) {
        int s = size / 3;

        int[] puntiX = { x, x + s, x + size - s, x + size, x + size, x + size - s, x + s, x };
        int[] puntiY = { y + s, y, y, y + s, y + size - s, y + size, y + size, y + size - s };

        g2d.setColor(color);
        g2d.fillPolygon(puntiX, puntiY, 8);
    }

    /**
     * Metodo che disenga la zona esterna alla mappa
     * @param g2d
     * @param deltaX
     * @param deltaY
     */
    private void zonaRossa(Graphics2D g2d, int deltaX, int deltaY) {
        // Colore rosso semitrasparente per le aree fuori dalla mappa
        Color coloreZona = new Color(115, 1, 1, 150);
        g2d.setColor(coloreZona);

        // La mappa va da 0 a 5000 in X e in Y.
        // Convertiamo le coordinate della mappa in coordinate schermo aggiungendo l'offset.
        int mapLeft   = deltaX;
        int mapTop    = deltaY;
        int mapRight  = deltaX + GameServer.BORDER_X;
        int mapBottom = deltaY + GameServer.BORDER_Y;

        int screenWidth  = getWidth();
        int screenHeight = getHeight();

        // Zona sopra la mappa: se il bordo superiore della mappa è sotto il top dello schermo
        if (mapTop > 0) {
            g2d.fillRect(0, 0, screenWidth, mapTop);
        }
        // Zona sotto la mappa: se il bordo inferiore della mappa è sopra il fondo dello schermo
        if (mapBottom < screenHeight) {
            g2d.fillRect(0, mapBottom, screenWidth, screenHeight - mapBottom);
        }
        // Zona a sinistra della mappa: se il bordo sinistro della mappa è a destra del bordo sinistro dello schermo
        if (mapLeft > 0) {
            g2d.fillRect(0, 0, mapLeft, screenHeight);
        }
        // Zona a destra della mappa: se il bordo destro della mappa è a sinistra del bordo destro dello schermo
        if (mapRight < screenWidth) {
            g2d.fillRect(mapRight, 0, screenWidth - mapRight, screenHeight);
        }

        // Disegna il bordo rosso all'esterno della mappa
        g2d.setColor(Color.RED);
        g2d.setStroke(new BasicStroke(9));
        // Il bordo viene disegnato attorno alla mappa usando le sue coordinate schermo.
        // Poiché la mappa ha dimensioni 5000 x 5000, il rettangolo va da mapLeft, mapTop
        // e si estende per 5000 pixel in larghezza e altezza.
        g2d.drawRect(mapLeft, mapTop, GameServer.BORDER_X, GameServer.BORDER_Y);
    }

    public OnlineGameController getGc() {
        return gc;
    }

    @Override
    public void updateTimerLabel() {}
}
