import javax.swing.*;
import java.io.*;
import java.net.*;
import java.util.*;

import java.util.concurrent.*;

public class GameServer {
    private static final int PORT = 1234;
    private static GameStateServer gameStateServer;
    private static final List<ClientHandler> clients = new CopyOnWriteArrayList<>();
    private static final Random random = new Random();
    private List<Bot> bots = new CopyOnWriteArrayList<>();
    private List<Food> foods = new CopyOnWriteArrayList<>();
    private ServerSocket serverSocket;
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);


    public GameServer(ServerSocket server) {
        gameStateServer = new GameStateServer(this);
        this.serverSocket = server;
        startGameLoop(); // Avvia il game loop a 60 tick al secondo
    }

    private void startGameLoop() {
        // Pianifica l'aggiornamento del gameState e la broadcast degli aggiornamenti a 60 tick al secondo
        scheduler.scheduleAtFixedRate(() -> {
            gameStateServer.update();         // Aggiorna lo stato del gioco
        }, 0, 20, TimeUnit.MILLISECONDS);      // 17ms ~ 60 FPS
    }

    public void startServer(){
        try {
            System.out.println("Server avviato sulla porta " + PORT);
            while (!serverSocket.isClosed()) {
                Socket clientSocket = serverSocket.accept();
                ClientHandler clientHandler = new ClientHandler(clientSocket);
                System.out.println("Nuovo client connesso!");
                clients.add(clientHandler);
                new Thread(clientHandler).start();
            }
        } catch (IOException e) {
            closeServerSocket();
        }
    }
    public void closeServerSocket() {
        try {
            if (serverSocket != null) {
                serverSocket.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public void respond() {
        System.out.println("SERVER: game started");
        while (!serverSocket.isClosed()) {
            try {
                long start = System.currentTimeMillis();
                Thread t = new Thread(gameStateServer::update);
                t.start();
                t.join();
                long finish = System.currentTimeMillis() - start;
                long sleepTime = 17 - finish; // aiming for 60 ticks per second (16.67ms per tick)
                if (sleepTime > 0) {
                    Thread.sleep(sleepTime); // Sleep for the remaining time to maintain the target FPS
                }
            } catch (InterruptedException ignore) {
            }
        }
    }


    public static void main(String[] args) {
        try{
            ServerSocket serverSocket1 = new ServerSocket(PORT);
            GameServer server = new GameServer(serverSocket1);
            new Thread(server::respond);
            new Thread(server::startServer).start();
        }catch(Exception e){
            e.printStackTrace();
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


        public void sendMessage(String message) {
            if (out != null && isAlive) {
                out.println(message);
                out.flush();
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
                gameStateServer.getPlayers().remove(player);
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

                // Crea un nuovo player
                player = new Player(playerId);
                gameStateServer.addPlayer(player);
                // Invia lo stato aggiornato al nuovo client
                //sendGameStateToClient();
                System.out.println("Giocatore aggiunto: " + player.getId());
                System.out.println("Numero totale di giocatori nel server: " + gameStateServer.getPlayers().size());

                // Invia l'aggiornamento ai client esistenti
                for (ClientHandler client : clients) {
                    if (client != this) {
                        client.sendMessage("NEW_PLAYER " + player.getId() + " " +
                                player.getPosition().getX() + " " + player.getPosition().getY());
                    }
                }



                // Conferma il join al nuovo client
                sendMessage("JOIN_OK");

                // Invia un aggiornamento globale
                //broadcastGameStateUpdates();
                gameStateServer.update();
            }


            else if (message.startsWith("MOVE ")) {
                String[] parts = message.split(" ");
                String playerId = parts[1];
                double newX = Double.parseDouble(parts[2]);
                double newY = Double.parseDouble(parts[3]);

                // Trova il giocatore e aggiorna la posizione
                Player player = gameStateServer.getPlayerById(playerId);
                    player.setPosition(new Vector2D(newX, newY));
                    // Invia la nuova posizione a tutti i client
                    //broadcastGameStateUpdates();
                    gameStateServer.update();
            }
        }



    }

    public static void broadcast(String message) {
        for (ClientHandler client : clients) {
            client.sendMessage(message);
        }
    }
}