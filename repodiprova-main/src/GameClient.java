import javax.swing.*;
import javax.swing.Timer;
import java.awt.*;
import java.io.*;
import java.net.*;
import java.util.*;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.locks.ReentrantLock;

public class GameClient {
    private static final String SERVER_IP = "localhost";
    private static final int SERVER_PORT = 1234;
    private Socket socket;
    private PrintWriter out;
    private BufferedReader in;
    private Player player;
    private final List<Entity> entities = Collections.synchronizedList(new ArrayList<>());
    private JFrame gameFrame;
    private GameWindow gameWindow;
    private GameController gameController;
    private ExecutorService executorService;
    private boolean connected = false;
    private Timer updateTimer;
    private final Map<String, Player> playerMap = new ConcurrentHashMap<>();
    private final Map<String, Bot> botMap = new ConcurrentHashMap<>();
    private final Map<String, Food> foodMap = new ConcurrentHashMap<>();
    private final ReentrantLock lock = new ReentrantLock();
    private OnlineGameController onlineGameController;
    private GameState gameState;
    private String messageFromServer;


    public GameClient() {
        gameWindow = new GameWindow(this);
        gameWindow.showPreLobby();


    }
    private void sendUsernameToServer() {
        out.write(player.getId());
        out.flush();
    }




    public boolean isServerAvailable() {
        try (Socket testSocket = new Socket(SERVER_IP, SERVER_PORT)) {
            return true;
        } catch (IOException e) {
            return false;
        }
    }
    public void startSinglePlayer() {
        player = new Player("solo");
        createGameWindowSingleplayer();

    }
    public void startMultiplayer() {
        executorService = Executors.newSingleThreadExecutor();

        try {
            player = new Player("Player_" + UUID.randomUUID().toString().substring(0, 8));
            socket = new Socket(SERVER_IP, SERVER_PORT);
            out = new PrintWriter(socket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            System.out.println("Connesso al server come " + player.getId());
            out.println("JOIN " + player.getId());
            Future<Boolean> joinResponse = executorService.submit(this::waitForServerResponse);
            onlineGameController = new OnlineGameController(this);
            sendUsernameToServer();


            if (joinResponse.get()) {
                System.out.println("Server ha confermato il join.");
                connected = true;
                playerMap.put(player.getId(), player);
                createOnlineGameWindow();
                gameController = gameWindow.getGameController();
                startListening(); // Avvia l'ascolto dei messaggi dal server
                startPlayerUpdates();
            } else {
                System.err.println("Errore: il server non ha accettato il join.");
                System.exit(1);
            }

        } catch (IOException | InterruptedException | ExecutionException e) {
            System.err.println("Errore di connessione al server.");
            e.printStackTrace();
            System.exit(0);
        }
    }

    public void write(String message) {
        if (socket.isConnected()) {
            out.write(player.getId() + "-" + message);
            out.flush();
        } else {
            System.out.println("server disconnected");
        }
    }
    public String readMessage() {
        try {
            if (in != null) {  // Assicurati che ci sia un messaggio pronto
                return in.readLine();  // Legge una riga dal buffer di input
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;  // Nessun messaggio ricevuto
    }


    private boolean waitForServerResponse() {
        try {
            String response = in.readLine();
            System.out.println("Risposta del server: " + response);
//            return "JOIN_OK".equals(response);
            return true;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    private void startListening() {
        new Thread(() -> listenForMessages()).start();
    }


    private void listenForMessages(){
        while(socket.isConnected()) {
            messageFromServer = readMessage();
            System.out.println(messageFromServer);
        }
    }
//    private void listenForMessages() {
//        while (socket.isConnected()) {
//            try {
//                messageFromServer = in.readLine();
//                if (messageFromServer == null) break;
//
//                // Se il messaggio contiene comandi SERVER, li gestiamo separatamente
//                if (messageFromServer.contains("SERVER")) {
//                    System.out.println(messageFromServer);
//                    if (messageFromServer.contains("SERVER: you died!"))
//                        closeEverything();
//                    continue;
//                }
//
//                // Il messaggio è composto da segmenti separati da ';'
//                // Esempio: "PLAYER Player_aef60d8e 3404.47 4283.80;BOT 748.30 895.23;BOT 555.19 705.08;BOT 187.81 1032.00"
//                String[] segments = messageFromServer.split(";");
//                for (String segment : segments) {
//                    segment = segment.trim();
//                    if (segment.startsWith("PLAYER ")) {
//                        // Formato: "PLAYER <playerId> <x> <y>"
//                        String[] parts = segment.split(" ");
//                        if (parts.length >= 4) {
//                            String playerId = parts[1];
//                            double x = Double.parseDouble(parts[2]);
//                            double y = Double.parseDouble(parts[3]);
//                            // Aggiorna (o aggiungi) il giocatore nel GameState
//                            updatePlayerPosition(playerId, x, y);
//                        }
//                    } else if (segment.startsWith("BOT ")) {
//                        // Formato: "BOT <x> <y>"
//                        String[] parts = segment.split(" ");
//                        if (parts.length >= 4) {
//                            String botId = parts[1];
//                            double x = Double.parseDouble(parts[2]);
//                            double y = Double.parseDouble(parts[3]);
//                            // Aggiorna (o aggiungi) il bot nel GameState
//                            updateBotPosition(botId,x, y);
//                        }
//                    } else if (segment.startsWith("FOOD ")) {
//                        String[] parts = segment.split(" ");
//                        if (parts.length >= 4) {
//                            String foodId = parts[1];
//                            double x = Double.parseDouble(parts[2]);
//                            double y = Double.parseDouble(parts[3]);
//                            // Aggiorna (o aggiungi) il bot nel GameState
//                            updateFoodPosition(foodId,x, y);
//                        }
//                    }
//                    // Puoi aggiungere altri casi, ad es. per FOOD, se necessario.
//                }
//            } catch (IOException e) {
//                closeEverything();
//                break;
//            }
//        }
//    }



    private void closeEverything() {
        try {
            if (socket != null) socket.close();
            if (in != null) in.close();
            if (out != null) out.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void updatePlayerPosition(String playerId, double x, double y) {
        lock.lock();
        try {
            Player existingPlayer = playerMap.get(playerId);
            if (existingPlayer != null) {
                existingPlayer.setPosition(new Vector2D(x, y));
            } else {
                Player newPlayer = new Player(playerId);
                newPlayer.setPosition(new Vector2D(x, y));
            }
        } finally {
            lock.unlock();
        }
    }
    private void updateBotPosition(String botId,double x, double y) {

            Bot existingBot = botMap.get(botId);
            if (existingBot != null) {
                existingBot.setPosition(new Vector2D(x, y));
            }else{
                Bot newBot = new Bot(new Vector2D(x,y), entities, gameController.getGameState(), botId);
                newBot.setPosition(new Vector2D(x, y));
            }

    }
    private void updateFoodPosition(String foodId,double x, double y) {
        Food existingFood = foodMap.get(foodId);
        if (existingFood != null) {
            existingFood.setPosition(new Vector2D(x, y));
        }else{
            Food newFood = new Food(new Vector2D(x,y),10,foodId);
            newFood.setPosition(new Vector2D(x, y));
        }
    }

    private void removePlayer(String playerId) {
        synchronized (entities) {
            gameController.getGameState().getEntities().removeIf(entity -> entity instanceof Player && ((Player) entity).getId().equals(playerId));
        }
        synchronized (gameController.getGameState().getPlayers()) {
            gameController.getGameState().getPlayers().removeIf(p -> p.getId().equals(playerId));
        }

    }



    private void startPlayerUpdates() {
        updateTimer = new Timer(50, e -> {
            if (connected && player.isAlive()) {
                sendPlayerUpdate();
            }
        });
        updateTimer.start();
    }

    private void sendPlayerUpdate() {
        new Thread(() -> {
            while (connected) {
                try {
                    Thread.sleep(20); // Invia posizione ogni 50ms
                    updatePlayerPosition(player.getId(), player.getPosition().getX(), player.getPosition().getY());
                    out.println("MOVE " + player.getId() + " " + player.getPosition().getX() + " " + player.getPosition().getY());
                    System.out.println("Posizione inviata al server: " + player.getPosition().getX() + ", " + player.getPosition().getY());

                    // Aggiungi qui l'aggiornamento della posizione nel client

                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }


    private void createGameWindowSingleplayer() {
        System.out.println("🔵 Creazione finestra di gioco...");
        gameFrame = new JFrame("VERMONI - Game Client");
        gameFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        gameFrame.setSize(800, 600);
        gameWindow.getGameController().getPlayers().add(player);
        gameFrame.add(gameWindow);
        gameFrame.pack();
        gameFrame.setVisible(true);
        Timer renderTimer = new Timer(20, e -> gameWindow.repaint());
        renderTimer.start();
        System.out.println("🔵 Creata finestra di gioco...");
    }
    public void createOnlineGameWindow() {
        JFrame gameFrame = new JFrame("Online Game");
        gameFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        GameWindow gameWindowOnline = new GameWindow(this, onlineGameController);
        gameWindowOnline.getGameController().getGameState().addPlayer(player);
        gameFrame.add(gameWindowOnline);
        gameFrame.pack();
        gameFrame.setLocationRelativeTo(null);
        gameFrame.setVisible(true);
    }


    public Player getPlayer() {
        return player;
    }
    public String getMessageFromServer(){
        return messageFromServer;
    }
    public boolean isClosed(){
        return socket.isClosed();
    }
    public static void main(String[] args) {
        SwingUtilities.invokeLater(GameClient::new);
    }
}