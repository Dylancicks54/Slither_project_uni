package view.GameViewer;

import Net.Client;
import controller.OnlineGameController;
import model.Pair;
import Net.Serialize;
import view.ShowPreLobby;
import javax.swing.*;
import java.awt.*;
import java.util.List;
import java.util.Map;
import java.util.Objects;
public class ServerGameView extends GameView {
    private final OnlineGameController gc;
    //private final Image background;
    private final Image[] foodImage;
    private final Image snakeImage;
    private final Client client;
    public ServerGameView(Client client) {
        this.client=client;
        this.gc = new OnlineGameController(client);
        //@TODO commentato perchè noi lo disegnamo lo sfondo
        //background = new ImageIcon(Objects.requireNonNull(this.getClass().getResource("/resources/background.PNG"))).getImage();
        snakeImage=new ImageIcon(Objects.requireNonNull(this.getClass().getResource("/resources/serpent.png"))).getImage();
        foodImage=new Image[4];
        foodImage[0] = new ImageIcon(Objects.requireNonNull(this.getClass().getResource("/resources/food.png"))).getImage();
        foodImage[1] = new ImageIcon(Objects.requireNonNull(this.getClass().getResource("/resources/food1.png"))).getImage();
        foodImage[2] = new ImageIcon(Objects.requireNonNull(this.getClass().getResource("/resources/food2.png"))).getImage();
        foodImage[3] = new ImageIcon(Objects.requireNonNull(this.getClass().getResource("/resources/food3.png"))).getImage();
    }

    public void paintComponent(Graphics g) {

            //disconnect if the client is closed
            if(client.isClosed()){
                JOptionPane.showMessageDialog(this,"You lost,Game over!");
                new ShowPreLobby().setVisible(true);
                closeCurrentGameWindow();
            }
            //super.paintComponent(g);
            //g.drawImage(background, 0, 0, 1100, 600, this);
            Graphics2D g2d = (Graphics2D) g;
            drawBackground(g2d);




            //drawMiniMapServer(g2d, gc);

            if(client.getMessageFromServer()!=null) {
                /*Initialization of list and variable depending on message received form the server*/
                Map<String, List<Pair>> snakes = Serialize.deserializeSnakes(client.getSnakes());
                List<Pair> foods = Serialize.deserializeSnake(client.getFoods());
                int offsetX = 0;
                int offsetY = 0;

                //change the offset for every player to let them view their snake at the center of the screen
                offsetX = getWidth() / 2 - snakes.get(client.getUserName()).get(0).getX();
                offsetY = getHeight() / 2 - snakes.get(client.getUserName()).get(0).getY();
                zonaRossa(g2d, offsetX, offsetY);

                //paint every snake
                for (Map.Entry<String, List<Pair>> entry : snakes.entrySet()) {
                    List<Pair> snakePos = entry.getValue();
                    g.drawString(entry.getKey(), snakePos.get(0).getX() + offsetX - (entry.getKey().length() * 3), snakePos.get(0).getY() + offsetY);

                    for (int i = 0; i < snakePos.size(); i++) {
                        int x = snakePos.get(i).getX() + offsetX;
                        int y = snakePos.get(i).getY() + offsetY;
                        g.drawImage(snakeImage, x, y, 15, 15, this);
                    }
                }

                //paint the score in the top right corner
                g.setColor(Color.WHITE);
                g.setFont(new Font("Arial", Font.PLAIN, 16));
                g.drawString("Score: " + (snakes.get(client.getUserName()).size() - 5), 10, 20);

                //paint the foods
                for (Pair food : foods) {
                    //change color depending on x value, gives the impression of randomness in color
                    g.drawImage(foodImage[food.getX() % 4], food.getX() + offsetX, food.getY() + offsetY, 10, 10, this);
                }
                //borders

                g2d.setColor(Color.WHITE);
                g2d.setStroke(new BasicStroke(3));
                g2d.drawLine( 10000+offsetX, -10000 + offsetY, 10000 + offsetX, 10000 + offsetY);
                g2d.drawLine(10000 + offsetX, -10000 + offsetY, -10000 + offsetX, -10000 + offsetY);
                g2d.drawLine(-10000+offsetX, -10000 + offsetY,  -10000+offsetX, 10000 + offsetY);
                g2d.drawLine( -10000+offsetX, 10000 + offsetY, -10000 + offsetX, 10000 + offsetY);
            }
        repaint();
    }
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

    private void disegnaOttagono(Graphics2D g2d, int x, int y, int size, Color color) {
        int s = size / 3;

        int[] puntiX = { x, x + s, x + size - s, x + size, x + size, x + size - s, x + s, x };
        int[] puntiY = { y + s, y, y, y + s, y + size - s, y + size, y + size, y + size - s };

        g2d.setColor(color);
        g2d.fillPolygon(puntiX, puntiY, 8);
    }
    private void zonaRossa(Graphics2D g2d, int deltaX, int deltaY) {
        // Colore rosso semitrasparente per le aree fuori dalla mappa
        Color coloreZona = new Color(115, 1, 1, 150);
        g2d.setColor(coloreZona);

        // La mappa va da -5000 a 5000 in X e in Y, quindi ha dimensioni 10000 x 10000.
        // Convertiamo le coordinate della mappa in coordinate schermo aggiungendo l'offset.
        int mapLeft   = -5000 + deltaX;
        int mapTop    = -5000 + deltaY;
        int mapRight  = 5000 + deltaX;
        int mapBottom = 5000 + deltaY;

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
        // Poiché la mappa ha dimensioni 10000 x 10000, il rettangolo va da mapLeft, mapTop
        // e si estende per 10000 pixel in larghezza e altezza.
        g2d.drawRect(mapLeft, mapTop, 10000, 10000);
    }




//    public void drawMiniMapServer(Graphics2D g2d, OnlineGameController controller) {
//        int size = 150;
//        int spazio = 20;
//        int raggio = size / 2;
//
//        int centroX = getWidth() - raggio - spazio;
//        int centroY = getHeight() - raggio - spazio;
//
//        g2d.setColor(new Color(0, 0, 0, 150));
//        g2d.fillOval(centroX - raggio, centroY - raggio, size, size);
//        g2d.setColor(Color.DARK_GRAY);
//        g2d.setStroke(new BasicStroke(1));
//        g2d.drawOval(centroX - raggio, centroY - raggio, size, size);
//
//        int mapWidth = getWidth();
//        int mapHeight = getHeight();
//
//        double scaleX = (double) size / mapWidth;
//        double scaleY = (double) size / mapHeight;
//
//        // Ottieni la posizione del giocatore dal controller
//        int[] playerPos = controller.getPlayerPosition();
//        int playerX = (int) (playerPos[0] * scaleX);
//        int playerY = (int) (playerPos[1] * scaleY);
//
//        int offsetX = playerX - (size / 2);
//        int offsetY = playerY - (size / 2);
//        double distanza = Math.sqrt(offsetX * offsetX + offsetY * offsetY);
//
//        // Evita che il pallino esca dalla minimappa
//        if (distanza > raggio - 5) {
//            double controlloBordo = (raggio - 5) / distanza;
//            playerX = (int) (size / 2 + offsetX * controlloBordo);
//            playerY = (int) (size / 2 + offsetY * controlloBordo);
//        }
//
//        g2d.setColor(Color.BLUE);
//        g2d.fillOval(centroX - raggio + playerX - 5, centroY - raggio + playerY - 5, 10, 10);
//    }

    public OnlineGameController getGc() {
        return gc;
    }
    @Override
    public void updateTimerLabel() {}
}
