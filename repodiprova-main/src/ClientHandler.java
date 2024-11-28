import java.io.*;
import java.net.Socket;

public class ClientHandler implements Runnable {
    private Socket clientSocket;
    private Player player;
    private Game game;
    private BufferedReader input;
    private PrintWriter output;

    public ClientHandler(Socket clientSocket, Game game) {
        this.clientSocket = clientSocket;
        this.game = game;
        this.player = new Player("Player_" + clientSocket.getPort(), null);
        game.addPlayer(player);

        try {
            input = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            output = new PrintWriter(clientSocket.getOutputStream(), true);
        } catch (IOException e) {
            System.err.println("Error setting up client handler: " + e.getMessage());
        }
    }

    @Override
    public void run() {
        try {
            String command;
            while ((command = input.readLine()) != null) {
                processCommand(command);
            }
        } catch (IOException e) {
            System.err.println("Client disconnected: " + e.getMessage());
        } finally {
            game.removePlayer(player);
        }
    }

    public void handleInput() {
        // Placeholder for handling input
    }

    public void sendUpdate() {
        // Send game state to the client
        output.println("Game State Update");
    }

    public void processCommand(String command) {
        // Process player commands (e.g., move)
        if (command.startsWith("MOVE")) {
            String[] parts = command.split(" ");
            double angle = Double.parseDouble(parts[1]);
            player.setAngle(angle);
            player.move();
        }
    }
}
