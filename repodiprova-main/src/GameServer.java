import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

public class GameServer {
    private static final int PORT = 1234;
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
                broadcastInitialGameState();

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

    private static void broadcastInitialGameState() {
        StringBuilder gameStateMessage = new StringBuilder("GAME_STATE ");

        // Aggiungi tutti i giocatori
        for (Player player : gameState.getPlayers()) {
            gameStateMessage.append("NEW_PLAYER ")
                    .append(player.getId()).append(" ")
                    .append(player.getPosition().getX()).append(" ")
                    .append(player.getPosition().getY());
            if(!player.isAlive()){
                gameStateMessage.append("DEAD ");
            }
        }

        // Aggiungi tutti i bot
        for (Bot bot : gameState.getBots()) {
            gameStateMessage.append("NEW_BOT ")
                    .append(bot.getPosition().getX()).append(" ")
                    .append(bot.getPosition().getY());
        }

        // Aggiungi tutto il cibo
        for (Food food : gameState.getFoodItems()) {
            gameStateMessage.append("NEW_FOOD ")
                    .append(food.getPosition().getX()).append(" ")
                    .append(food.getPosition().getY());
        }
        StringBuilder gameStateMessageUpdate = new StringBuilder("UPDATE ");
        gameStateMessageUpdate.append("UPDATE_PLAYERS ");
        gameStateMessageUpdate.append("UPDATE_BOTS ");
        gameStateMessageUpdate.append("UPDATE_FOOD ");

        // Invia il messaggio completo a tutti i client
        String fullMessage = gameStateMessage.toString();
        for (ClientHandler client : clients) {
            client.sendMessage(fullMessage);
            client.sendMessage(gameStateMessageUpdate.toString());
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
            System.out.println("Invio stato di gioco al client...");

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
                        client.sendMessage("NEW_PLAYER" + player.getId() + " " +
                                player.getPosition().getX() + " " +
                                player.getPosition().getY());
                    }
                }
            } else if (message.startsWith("MOVE")) {
                String[] parts = message.split(" ");
                String playerId = parts[1];
                double newX = Double.parseDouble(parts[2]);
                double newY = Double.parseDouble(parts[3]);

                // Trova il giocatore e aggiorna la posizione
                Player player = gameState.getPlayerById(playerId);
                if (player != null) {
                    player.setPosition(new Vector2D(newX, newY));

                    // Invia la nuova posizione a tutti i client
                    broadcastInitialGameState();

                }
            }

        }


    }

    public static void broadcast(String message) {
        for (ClientHandler client : clients) {
            client.sendMessage(message);
        }
    }
}