package controller;

import Net.Client;
import Net.Serialize;

import java.awt.event.MouseEvent;

/**
 * Controller per il gioco in multiplayer
 */
public class OnlineGameController extends AbstractGameController {

    private Client client;

    public OnlineGameController(Client client) {

        this.client=client;
    }

    /**
     * Metodo per recuperare le coordinate del giocatore dal messaggio inviato dal server
     * @return array con le coordinate (x,y)
     */
    public int[] getPlayerPosition() {
        // Estrai la posizione del giocatore dal messaggio del server
        String snakesData = client.getSnakes();
        if (snakesData != null && !snakesData.isEmpty()) {
            // Suddividi la stringa usando la virgola come separatore
            String[] snakeParts = snakesData.split(",");
            int startIndex = 0;
            // Se il primo elemento non contiene ":" probabilmente è un identificatore (es. "g")
            if (snakeParts.length > 0 && !snakeParts[0].contains(":")) {
                startIndex = 1;
            }
            if (snakeParts.length > startIndex) {
                // Formato coordinate "x:y"
                String[] coords = snakeParts[startIndex].split(":");
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
        // Valori di default se non ci sono dati validi
        return new int[]{0, 0};
    }

    @Override
    public void mouseMoved(MouseEvent e) {
        int mouseX = e.getX();
        int mouseY = e.getY();

        client.write(Serialize.serializePlayerPos(mouseX, mouseY));
    }


}
