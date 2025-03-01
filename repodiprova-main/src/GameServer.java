import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

public class GameServer {
    private static final int PORT = 12345;
    private static GameState gameState;
    private static final List<ClientHandler> clients = new CopyOnWriteArrayList<>();
    private static final Random random = new Random();

    public static void main(String[] args) {
        gameState = new GameState();
        gameState.addBot();

        // Timer per aggiornare lo stato di gioco
        Timer gameUpdateTimer = new Timer();
        gameUpdateTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                gameState.updateGameState();
                broadcastGameState();
            }
        }, 0, 50);

        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("Server avviato sulla porta " + PORT);

            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("Nuovo client connesso!");

                ClientHandler clientHandler = new ClientHandler(clientSocket);
                clients.add(clientHandler);
                new Thread(clientHandler).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void broadcastGameState() {
        StringBuilder stateMessage = new StringBuilder("UPDATE ");

        for (Player player : gameState.getPlayers()) {
            stateMessage.append(player.getId()).append(" ")
                    .append(player.getPosition().getX()).append(" ")
                    .append(player.getPosition().getY()).append(" ");
        }

        for (ClientHandler client : clients) {
            client.sendMessage(stateMessage.toString());
        }
    }

    static class ClientHandler implements Runnable {
        private final Socket clientSocket;
        private PrintWriter out;
        private BufferedReader in;
        private Player player;
        private boolean isAlive = true;

        public ClientHandler(Socket clientSocket) {
            this.clientSocket = clientSocket;
        }

        private void sendGameStateToClient() {
            // Invia tutti i player
            for (Player p : gameState.getPlayers()) {
                sendMessage("NEW_PLAYER " + p.getId() + " " +
                        p.getPosition().getX() + " " +
                        p.getPosition().getY());
            }

            // Invia tutti i bot
            for (Bot b : gameState.getBots()) {
                sendMessage("NEW_BOT " + b.getPosition().getX() + " " +
                        b.getPosition().getY());
            }

            // Invia tutto il cibo
            for (Food f : gameState.getFoodItems()) {
                sendMessage("NEW_FOOD " + f.getPosition().getX() + " " +
                        f.getPosition().getY());
            }

            // Segnala fine dell'inizializzazione
            sendMessage("INIT_COMPLETE");
        }

        public void sendMessage(String message) {
            if (out != null && isAlive) {
                out.println(message);
            }
        }

        @Override
        public void run() {
            try {
                in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                out = new PrintWriter(clientSocket.getOutputStream(), true);

                String inputLine;
                while (isAlive && (inputLine = in.readLine()) != null) {
                    handleClientMessage(inputLine);
                }
            } catch (IOException e) {
                System.out.println("Un client si è disconnesso.");
            } finally {
                disconnect();
            }
        }

        private void disconnect() {
            isAlive = false;

            if (player != null) {
                gameState.getPlayers().remove(player);
                broadcast("REMOVE_PLAYER " + player.getId());
            }

            clients.remove(this);

            try {
                if (clientSocket != null && !clientSocket.isClosed()) {
                    clientSocket.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        public void handleClientMessage(String message) {
            System.out.println("Messaggio dal client: " + message);

            if (message.startsWith("JOIN")) {
                String playerId = message.split(" ")[1];

                player = new Player(playerId);
                // Posizione iniziale casuale
                double x = random.nextDouble() * (GameState.MAP_WIDTH - 100) + 50;
                double y = random.nextDouble() * (GameState.MAP_HEIGHT - 100) + 50;
                player.setPosition(new Vector2D(x, y));

                gameState.addPlayer(player);
                System.out.println("Giocatori nel server dopo JOIN: " + gameState.getPlayers().size());

                sendMessage("JOIN_OK");

                // Invia l'intero stato di gioco al client
                sendGameStateToClient();

                // Notifica gli altri client del nuovo giocatore
                for (ClientHandler client : clients) {
                    if (client != this) {
                        client.sendMessage("NEW_PLAYER " + player.getId() + " " +
                                player.getPosition().getX() + " " +
                                player.getPosition().getY());
                    }
                }
            } else if (message.startsWith("MOVE")) {
                player.move();
            }
        }


    }

    public static void broadcast(String message) {
        for (ClientHandler client : clients) {
            client.sendMessage(message);
        }
    }
}