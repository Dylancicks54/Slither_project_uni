import java.io.*;
import java.net.*;
import java.util.*;

public class GameServer {
    private static final int PORT = 12345;
    private static GameState gameState;
    private static final List<ClientHandler> clients = new ArrayList<>();

    public static void main(String[] args) {
        gameState = new GameState();

        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("Server avviato...");
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

    public static void broadcast(String message) {
        synchronized (clients) {
            for (ClientHandler client : clients) {
                client.sendMessage(message);
            }
        }
    }

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
                    handleClientMessage(inputLine);
                }
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                try {
                    clientSocket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        private void handleClientMessage(String message) {
            if (message.startsWith("JOIN")) {
                String playerId = message.split(" ")[1];
                Player newPlayer = new Player(playerId);
                gameState.addPlayer(newPlayer);
                broadcast("NEW_PLAYER " + newPlayer.getId());
            }
        }
    }
}