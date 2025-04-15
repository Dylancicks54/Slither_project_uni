package controller;

import Net.Client;
import Net.Serialize;

import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

/**
 * Controller per il gioco in multiplayer
 */
public class OnlineGameController extends AbstractGameController implements MouseListener {

    private Client client;

    public OnlineGameController(Client client) {

        this.client=client;
    }

    /**
     * Metodo per recuperare le coordinate del giocatore dal messaggio inviato dal server
     * @return array con le coordinate (x,y)
     */
    public int[] getPlayerPosition() {

        String snakesData = client.getSnakes(); // Estrae la posizione del giocatore dal messaggio del server
        if (snakesData != null && !snakesData.isEmpty()) {
            String[] snakeParts = snakesData.split(","); // usa la virgola come separatore
            int startIndex = 0;
            if (snakeParts.length > 0 && !snakeParts[0].contains(":")) { // Se il primo elemento non contiene ":" probabilmente è un identificatore (es. "g")
                startIndex = 1;
            }
            if (snakeParts.length > startIndex) {

                String[] coords = snakeParts[startIndex].split(":"); // Formato coordinate "x:y"
                if (coords.length >= 2) {
                    try {
                        int x = Integer.parseInt(coords[0].trim());
                        int y = Integer.parseInt(coords[1].trim());
                        return new int[]{x, y};
                    } catch (NumberFormatException e) {
                        e.printStackTrace();
                    }
                }
            }
        }

        return new int[]{0, 0}; // Valori di default se non ci sono dati validi
    }

    @Override
    public void mouseMoved(MouseEvent e) {
        int mouseX = e.getX();
        int mouseY = e.getY();

        client.write(Serialize.serializePlayerPos(mouseX, mouseY));
    }
}
