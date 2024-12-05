import java.io.*;
import java.net.*;
import java.util.*;
public class GameServer {
    private static final int PORT = 12345;
    private static final List<Player> players = new ArrayList<>();
    private static final List<Bot> bots = new ArrayList<>();
    private static final List<Food> foodItems = new ArrayList<>();
    private static GameState gameState;
    private static final List<ClientHandler> clients = new ArrayList<>();

    public static void main(String[] args) {
        gameState = new GameState(players, bots, foodItems);

        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("Server avviato...");

            // Aggiungi qualche bot e cibo al gioco
            for (int i = 0; i < 4; i++) {
                bots.add(new Bot(new Vector2D(Math.random() * 3000, Math.random() * 3000), gameState, null));
            }
            for (int i = 0; i < 10; i++) {
                foodItems.add(new Food(new Vector2D(Math.random() * 1000, Math.random() * 1000), (int) (Math.random() * 10)));
            }

            // Accetta connessioni dai client
            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("Nuovo client connesso!");

                // Crea un thread per ogni client
                ClientHandler clientHandler = new ClientHandler(clientSocket);
                clients.add(clientHandler);
                new Thread(clientHandler).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Metodo per inviare messaggi a tutti i client
    public static void broadcast(String message) {
        synchronized (clients) {
            for (ClientHandler client : clients) {
                client.sendMessage(message);
            }
        }
    }

    // Gestore del client
    static class ClientHandler implements Runnable {
        private final Socket clientSocket;
        private PrintWriter out;
        private BufferedReader in;

        public ClientHandler(Socket clientSocket) {
            this.clientSocket = clientSocket;
        }

        public void sendMessage(String message) {
            if (out != null) {
                out.println(message);
            }
        }

        @Override
        public void run() {
            try {
                in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                out = new PrintWriter(clientSocket.getOutputStream(), true);

                String inputLine;
                while ((inputLine = in.readLine()) != null) {
                    System.out.println("Ricevuto dal client: " + inputLine);

                    // Gestisci le comunicazioni con il client
                    if (inputLine.startsWith("JOIN")) {
                        String playerName = inputLine.split(" ")[1];
                        Player newPlayer = new Player(playerName, null); // Crea un giocatore solo quando il client si connette
                        players.add(newPlayer);

                        // Invia l'aggiornamento del giocatore appena creato
                        out.println("INIT PLAYER " + newPlayer.getId() + " " + newPlayer.getPosition().x + " " + newPlayer.getPosition().y);
                    }

                    // Aggiorna tutti i client con lo stato del gioco
                    synchronized (players) {
                        for (Player player : players) {
                            out.println("UPDATE PLAYER " + player.getId() + " " + player.getPosition().x + " " + player.getPosition().y);
                        }
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                // Rimuovi il client dalla lista quando si disconnette
                synchronized (clients) {
                    clients.remove(this);
                }
                try {
                    clientSocket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
