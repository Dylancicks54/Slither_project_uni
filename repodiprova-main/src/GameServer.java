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

        private void sendFullState() {
            synchronized (gameState) {
                for (Player player : players) {
                    out.println("FULL_STATE PLAYER " + player.getId() + " " + player.getPosition().x + " " + player.getPosition().y);
                }

            }
        }


        @Override
        public void run() {
            try {
                in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                out = new PrintWriter(clientSocket.getOutputStream(), true);

                sendFullState(); // Invia lo stato completo al client connesso

                String inputLine;
                while ((inputLine = in.readLine()) != null) {
                    System.out.println("Ricevuto dal client: " + inputLine);

                    if (inputLine.startsWith("JOIN")) {
                        String playerName = inputLine.split(" ")[1]; // Nome univoco inviato dal client
                        Player newPlayer = new Player(playerName, null);
                        newPlayer.setPosition(new Vector2D(Math.random() * 1000, Math.random() * 1000)); // Posizione iniziale randomica
                        synchronized (players) {
                            players.add(newPlayer);
                        }
                        out.println("INIT PLAYER " + newPlayer.getId() + " " + newPlayer.getPosition().x + " " + newPlayer.getPosition().y);
                        broadcast("UPDATE PLAYER " + newPlayer.getId() + " " + newPlayer.getPosition().x + " " + newPlayer.getPosition().y);
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
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
