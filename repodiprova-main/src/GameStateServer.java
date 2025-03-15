import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

public class GameStateServer extends GameState {
    private final GameServer server;


    /**
     * Costruttore: inizializza lo stato di gioco e salva il riferimento al GameServer.
     * Utilizza il costruttore della superclasse GameState per inizializzare
     * le liste di players, bots e foodItems.
     *
     * @param server il GameServer che gestisce le connessioni e l'invio degli aggiornamenti ai client.
     */
    public GameStateServer(GameServer server) {
        super();
        this.server = server;
        addBot();
    }

    /**
     * Aggiorna lo stato del gioco lato server.
     * - Esegue l'aggiornamento base (movimenti, collisioni, magnet effect, respawn cibo)
     * - Controlla eventuali collisioni con i bordi
     * - Invia lo stato aggiornato a tutti i client
     */
    public void update() {
        // Aggiorna lo stato di gioco (movimenti, collisioni, ecc.) usando la logica definita in GameState
        super.updateGameState();
        // Invia lo stato aggiornato ai client
        sendGameStateToClients();
    }


    /**
     * Costruisce un pacchetto di stringhe contenente lo stato attuale del gioco,
     * includendo informazioni di players, bots e food, e lo invia a tutti i client
     * tramite il metodo broadcast del GameServer.
     */
    private void sendGameStateToClients() {
        StringBuilder updateMsg = new StringBuilder();
        updateMsg.append("GAME_STATE_UPDATE ");

        // Aggiungi lo stato dei players
        for (Player p : getPlayers()) {
            updateMsg.append("PLAYER ")
                    .append(p.getId()).append(" ")
                    .append(p.getPosition().getX()).append(" ")
                    .append(p.getPosition().getY());

            // Aggiungi un solo messaggio SEGMENT seguito dai segmenti separati da '|'
            StringBuilder segments = new StringBuilder();
            for (Segment previousPosition : p.getBodySegments()) {
                segments.append(previousPosition.getPosition().getX()).append(" ")
                        .append(previousPosition.getPosition().getY()).append("|"); // Usa '|' come separatore
            }

            // Aggiungi i segmenti
            if (segments.length() > 0) {
                segments.deleteCharAt(segments.length() - 1); // Rimuove l'ultimo '|' aggiunto
                updateMsg.append(" SEGMENT ").append(segments);
            }

            if (!p.isAlive()) {
                updateMsg.append(" DEAD");
            }
            updateMsg.append(";");
        }

        // Aggiungi lo stato dei bots
        for (Bot b : getBots()) {
            updateMsg.append("BOT ")
                    .append(b.getId()).append(" ")
                    .append(b.getPosition().getX()).append(" ")
                    .append(b.getPosition().getY());

            // Aggiungi un solo messaggio SEGMENT seguito dai segmenti separati da '|'
            StringBuilder segments = new StringBuilder();
            for (Segment previousPosition : b.getSegments()) {
                segments.append(previousPosition.getPosition().getX()).append(" ")
                        .append(previousPosition.getPosition().getY()).append("|"); // Usa '|' come separatore
            }

            // Aggiungi i segmenti
            if (segments.length() > 0) {
                segments.deleteCharAt(segments.length() - 1); // Rimuove l'ultimo '|' aggiunto
                updateMsg.append(" SEGMENT ").append(segments);
            }
            updateMsg.append(";");
        }

        // Aggiungi lo stato degli elementi di cibo
        for (Food f : getFoodItems()) {
            updateMsg.append("FOOD ")
                    .append(f.getIdentifier()).append(" ")
                    .append(f.getPosition().getX()).append(" ")
                    .append(f.getPosition().getY())
                    .append(";");
        }

        // Invia il pacchetto di aggiornamento a tutti i client connessi
        server.broadcast(updateMsg.toString());
        System.out.println(updateMsg.toString());
    }



    public void addEntities(){
        super.addBot();
    }
}
