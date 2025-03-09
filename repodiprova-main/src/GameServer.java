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
                broadcastGameStateUpdates();

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
        for (ClientHandler client : clients) {
            // Stato iniziale completo
            client.sendMessage("INIT_STATE");

            for (Player player : gameState.getPlayers()) {
                if(player.isAlive()){
                    client.sendMessage("NEW_PLAYER " + player.getId() + " " + player.getPosition().getX() + " " + player.getPosition().getY() + (player.isAlive() ? "" : " DEAD"));
                }else{
                client.sendMessage("NEW_PLAYER " + player.getId() + " " + player.getPosition().getX() + " " + player.getPosition().getY());  }
          }
            for (Bot bot : gameState.getBots()) {
                client.sendMessage("NEW_BOT " + bot.getPosition().getX() + " " + bot.getPosition().getY());
            }

            for (Food food : gameState.getFoodItems()) {
                client.sendMessage("NEW_FOOD " + food.getPosition().getX() + " " + food.getPosition().getY());
            }

            client.sendMessage("INIT_COMPLETE");
        }
    }
    private static void broadcastGameStateUpdates() {
        StringBuilder playerUpdates = new StringBuilder("UPDATE_PLAYERS");
        for (Player player : gameState.getPlayers()) {
            playerUpdates.append(" ")
                    .append(player.getId()).append(" ")
                    .append(player.getPosition().getX()).append(" ")
                    .append(player.getPosition().getY());
            if (!player.isAlive()) {
                playerUpdates.append(" DEAD"); // Aggiungi uno spazio prima di DEAD
            }
        }

        StringBuilder botUpdates = new StringBuilder("UPDATE_BOTS");
        for (Bot bot : gameState.getBots()) {
            botUpdates.append(" ")
                    .append(bot.getPosition().getX()).append(" ")
                    .append(bot.getPosition().getY());
        }

        StringBuilder foodUpdates = new StringBuilder("UPDATE_FOOD");
        for (Food food : gameState.getFoodItems()) {
            foodUpdates.append(" ")
                    .append(food.getPosition().getX()).append(" ")
                    .append(food.getPosition().getY());
        }

        StringBuilder gameStateUpdate = new StringBuilder("GAME_STATE_UPDATE ");
        gameStateUpdate.append(playerUpdates).append(" ")
                .append(botUpdates).append(" ")
                .append(foodUpdates);

        broadcast(gameStateUpdate.toString());
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

                // Conferma il join al nuovo giocatore
                sendMessage("JOIN_OK");

                // 📌 INVIA LA LISTA DI TUTTI I GIOCATORI ESISTENTI AL NUOVO CLIENT
                for (Player existingPlayer : gameState.getPlayers()) {
                    if (!existingPlayer.getId().equals(playerId)) {
                        sendMessage("NEW_PLAYER " + existingPlayer.getId() + " " +
                                existingPlayer.getPosition().getX() + " " +
                                existingPlayer.getPosition().getY());
                    }
                }

                // Invia lo stato attuale del gioco
                sendGameStateToClient();

                // Notifica gli altri client del nuovo giocatore
                for (ClientHandler client : clients) {
                    if (client != this) {
                        client.sendMessage("NEW_PLAYER " + player.getId() + " " +
                                player.getPosition().getX() + " " +
                                player.getPosition().getY());
                    }
                }

                // Dopo l'aggiunta di un nuovo player, invia un aggiornamento globale
                broadcastGameStateUpdates();
            }

            else if (message.startsWith("MOVE ")) {
                String[] parts = message.split(" ");
                String playerId = parts[1];
                double newX = Double.parseDouble(parts[2]);
                double newY = Double.parseDouble(parts[3]);

                // Trova il giocatore e aggiorna la posizione
                Player player = gameState.getPlayerById(playerId);
                    player.setPosition(new Vector2D(newX, newY));
                    // Invia la nuova posizione a tutti i client
                    broadcastGameStateUpdates();
            }
        }



    }

    public static void broadcast(String message) {
        System.out.println("Broadcasting to " + clients.size() + " clients: " + message);
        for (ClientHandler client : clients) {
            client.sendMessage(message);
        }
    }
}