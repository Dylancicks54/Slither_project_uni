import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.util.ArrayList;

import java.util.List;

public class GameWindow extends JPanel implements KeyListener, MouseMotionListener {
    private OnlineGameController onlineGameController;
    private GameController gameController;
    private GameClient gameClient;
    private JFrame preLobbyFrame;
    private boolean onlineMode = false;
    // Costruttore per modalità offline (già esistente)
    public GameWindow(GameClient gameClient){
        this.gameClient = gameClient;
        this.gameController = new GameController(this);
        initialize();
    }

    // Costruttore per modalità online: viene passato anche il Client
    public GameWindow(GameClient gameClient, OnlineGameController onlineGameController) {
        this.onlineGameController = onlineGameController;
        this.gameClient = gameClient;
        this.onlineMode = true;
        this.gameController = new GameController(this);

        initialize();
    }
    private void initialize() {
        setPreferredSize(new Dimension(1920, 1080));
        setFocusable(true);
        requestFocusInWindow();
        addKeyListener(this);
        addMouseMotionListener(this);
        Timer timer = new Timer(20, e -> {
            if (onlineMode) {
                processNetworkUpdates();
            } else {
                gameController.updateGameState();
            }
            //repaint();
        });
        timer.start();
    }

    private void processNetworkUpdates() {
        String message = gameClient.getMessageFromServer(); // Metodo che legge un messaggio dal server
        if (message != null && !message.isEmpty()) {
            gameClient.getMessageFromServer();
        }
        repaint();
    }

    public void paintComponent2(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;

        // Calcola l'offset in base alla posizione del giocatore locale
        int deltaX = (int) gameClient.getPlayer().getPosition().getX() - getWidth() / 2;
        int deltaY = (int) gameClient.getPlayer().getPosition().getY() - getHeight() / 2;

        // Disegna lo sfondo
        drawBackground(g2d);

        // Prepariamo le liste per salvare le informazioni parsate dal messaggio
        List<PlayerInfo> playersList = new ArrayList<>();
        List<BotInfo> botsList = new ArrayList<>();
        List<FoodInfo> foodsList = new ArrayList<>();

        // Ottieni il messaggio aggiornato dal server
        String message = gameClient.getMessageFromServer();
        if (message != null && !message.isEmpty() && message.startsWith("GAME_STATE_UPDATE ")) {
            String data = message.substring("GAME_STATE_UPDATE ".length());
            String[] segments = data.split(";");

            for (String segment : segments) {
                segment = segment.trim();
                if (segment.startsWith("PLAYER")) {
                    String[] parts = segment.split(" ");
                    if (parts.length >= 4) {
                        String pid = parts[1];
                        double x = 0, y = 0;
                        try {
                            x = Double.parseDouble(parts[2]);
                            y = Double.parseDouble(parts[3]);
                        } catch (NumberFormatException e) {
                            System.err.println("Errore nel parsing delle coordinate del giocatore: " + parts[2] + ", " + parts[3]);
                            continue; // Salta questo segmento se c'è un errore nel parsing
                        }

                        List<Segment> trail = new ArrayList<>();
                        // Trova la parte con il messaggio dei segmenti, separato dal simbolo '|'
                        if (segment.contains("SEGMENT")) {
                            String segmentsData = segment.split("SEGMENT")[1].trim();
                            String[] segmentPositions = segmentsData.split("\\|"); // Usa '|' come separatore
                            for (String segmentPos : segmentPositions) {
                                String[] pos = segmentPos.trim().split(" ");
                                if (pos.length == 2) {
                                    try {
                                        double posX = Double.parseDouble(pos[0]);
                                        double posY = Double.parseDouble(pos[1]);
                                        trail.add(new Segment(new Vector2D(posX, posY), 17));
                                    } catch (NumberFormatException e) {
                                        System.err.println("Errore nel parsing del segmento: " + pos[0] + ", " + pos[1]);
                                    }
                                }
                            }
                        }
                        playersList.add(new PlayerInfo(pid, x, y, trail));
                    }
                } else if (segment.startsWith("BOT")) {
                    String[] parts = segment.split(" ");
                    if (parts.length >= 4) {
                        String bid = parts[1];
                        double x = 0, y = 0;
                        try {
                            x = Double.parseDouble(parts[2]);
                            y = Double.parseDouble(parts[3]);
                        } catch (NumberFormatException e) {
                            System.err.println("Errore nel parsing delle coordinate del bot: " + parts[2] + ", " + parts[3]);
                            continue; // Salta questo segmento se c'è un errore nel parsing
                        }

                        List<Segment> trail = new ArrayList<>();
                        // Trova la parte con il messaggio dei segmenti, separato dal simbolo '|'
                        if (segment.contains("SEGMENT")) {
                            String segmentsData = segment.split("SEGMENT")[1].trim();
                            String[] segmentPositions = segmentsData.split("\\|"); // Usa '|' come separatore
                            for (String segmentPos : segmentPositions) {
                                String[] pos = segmentPos.trim().split(" ");
                                if (pos.length == 2) {
                                    try {
                                        double posX = Double.parseDouble(pos[0]);
                                        double posY = Double.parseDouble(pos[1]);
                                        trail.add(new Segment(new Vector2D(posX, posY), 17));
                                    } catch (NumberFormatException e) {
                                        System.err.println("Errore nel parsing del segmento del bot: " + pos[0] + ", " + pos[1]);
                                    }
                                }
                            }
                        }
                        botsList.add(new BotInfo(bid, x, y, trail));
                    }
                } else if (segment.startsWith("FOOD")) {
                    String[] parts = segment.split(" ");
                    if (parts.length >= 4) {
                        String fid = parts[1];
                        double x = 0, y = 0;
                        try {
                            x = Double.parseDouble(parts[2]);
                            y = Double.parseDouble(parts[3]);
                        } catch (NumberFormatException e) {
                            System.err.println("Errore nel parsing del cibo: " + parts[2] + ", " + parts[3]);
                            continue; // Salta questo segmento se c'è un errore nel parsing
                        }
                        foodsList.add(new FoodInfo(fid, x, y));
                    }
                }
            }
        }

        // Disegna i segmenti e i giocatori
        for (PlayerInfo pInfo : playersList) {
            // Se il giocatore è quello locale, usa un colore diverso
            if (pInfo.playerId.equals(gameClient.getPlayer().getId())) {
                g2d.setColor(Color.BLUE);
            } else {
                g2d.setColor(Color.MAGENTA);
            }
            int screenX = (int) pInfo.x - deltaX;
            int screenY = (int) pInfo.y - deltaY;

            // Disegna i segmenti del giocatore
            for (int i = 0; i < pInfo.getSegments().size(); i++) {
                Segment segment = pInfo.getSegments().get(i);
                int x = (int) segment.getPosition().getX() - deltaX;
                int y = (int) segment.getPosition().getY() - deltaY;
                int size = (int) segment.getSize();

                // Disegna ogni segmento come un cerchio (puoi anche usare un rettangolo se preferisci)
                g2d.fillOval(x - size / 2, y - size / 2, size, size);
            }



            g2d.fillOval(screenX - 10, screenY - 10, 20, 20);
            g2d.setColor(Color.WHITE);
            g2d.drawString(pInfo.playerId, screenX - (pInfo.playerId.length() * 3), screenY - 12);
        }

        // Disegna i bot
        for (BotInfo bInfo : botsList) {
            int screenX = (int) bInfo.x - deltaX;
            int screenY = (int) bInfo.y - deltaY;

            // Disegna i segmenti dei bot
            g2d.setColor(Color.RED);
            for (int i = 0; i < bInfo.getSegments().size(); i++) {
                Segment segment = bInfo.getSegments().get(i);
                int x = (int) segment.getPosition().getX() - deltaX;
                int y = (int) segment.getPosition().getY() - deltaY;
                int size = (int) segment.getSize();

                // Disegna ogni segmento come un cerchio (puoi anche usare un rettangolo se preferisci)
                g2d.fillOval(x - size / 2, y - size / 2, size, size);
            }


            g2d.setColor(Color.RED);
            g2d.fillOval(screenX - 10, screenY - 10, 20, 20);
        }

        // Disegna il cibo
        for (FoodInfo fInfo : foodsList) {
            int screenX = (int) fInfo.x - deltaX;
            int screenY = (int) fInfo.y - deltaY;
            g2d.setColor(Color.GREEN);
            g2d.fillOval(screenX - 5, screenY - 5, 10, 10);
        }

        // Disegna il rettangolo di confine e la zona rossa
        g2d.setColor(Color.RED);
        g2d.setStroke(new BasicStroke(9));
        g2d.drawRect(-deltaX, -deltaY, GameState.MAP_WIDTH, GameState.MAP_HEIGHT);
        zonaRossa(g2d, deltaX, deltaY);

        // Se il giocatore è morto, mostra la schermata di morte
        schermataMorte(gameClient.getPlayer(), g2d);
    }








    @Override
    protected void paintComponent(Graphics g) {
        if(onlineMode){
            paintComponent2(g);
        }else {
            super.paintComponent(g);
            Graphics2D g2d = (Graphics2D) g;

            int deltaX = (int) gameClient.getPlayer().getPosition().getX() - getWidth() / 2;
            int deltaY = (int) gameClient.getPlayer().getPosition().getY() - getHeight() / 2;

            drawBackground(g2d);
            //drawGrid(g2d, deltaX, deltaY);
            drawEntities(g2d, deltaX, deltaY);

            g2d.setColor(Color.RED);
            g2d.setStroke(new BasicStroke(9));
            g2d.drawRect(-deltaX, -deltaY, GameState.MAP_WIDTH, GameState.MAP_HEIGHT);
            zonaRossa(g2d, deltaX, deltaY);

            schermataMorte(gameClient.getPlayer(), g2d);
        }
    }
    private void schermataMorte(Player p, Graphics2D g2d){
        if (!gameClient.getPlayer().isAlive()) {
            g2d.setColor(new Color(255, 0, 0, 150)); // Sfondo semi-trasparente
            g2d.fillRect(getWidth() / 2 - 200, getHeight() / 2 - 100, 400, 200); // Rettangolo menu

            g2d.setColor(Color.WHITE);
            g2d.setFont(new Font("Arial", Font.BOLD, 24));
            g2d.drawString("YOU DIED", getWidth() / 2 - 50, getHeight() / 2 - 50);
            g2d.drawString("Press R to Respawn", getWidth() / 2 - 100, getHeight() / 2);
            g2d.drawString("Press Q to Quit", getWidth() / 2 - 80, getHeight() / 2 + 40);
        }
    }
    private void zonaRossa(Graphics2D g2d, int deltaX, int deltaY) {
        int mapX = -deltaX;
        int mapY = -deltaY;
        int mapWidth = GameState.MAP_WIDTH;
        int mapHeight = GameState.MAP_HEIGHT;

        Color coloreZona = new Color(115, 1, 1, 100);

        g2d.setColor(coloreZona);

        g2d.fillRect(0, 0, getWidth(), mapY);
        g2d.fillRect(0, mapY + mapHeight, getWidth(), getHeight() - (mapY + mapHeight));
        g2d.fillRect(0, mapY, mapX, mapHeight);
        g2d.fillRect(mapX + mapWidth, mapY, getWidth() - (mapX + mapWidth), mapHeight);
    }

    private void drawBackground(Graphics2D g2d) {
        Color baseColor = new Color(15, 15, 20);
        Color coloreOttagono = new Color(30, 30, 40);

        g2d.setColor(baseColor);
        g2d.fillRect(0, 0, getWidth(), getHeight());

        int size = 50;
        int spacing = size + 10;
        int spazioInMezzo = (int) (spacing * Math.sqrt(2));

        int deltaX = (int) (-gameClient.getPlayer().getPosition().getX() % spazioInMezzo);
        int deltaY = (int) (-gameClient.getPlayer().getPosition().getY() % spazioInMezzo);

        for (int x = deltaX - spazioInMezzo; x < getWidth() + spazioInMezzo; x += spazioInMezzo) {
            for (int y = deltaY - spazioInMezzo; y < getHeight() + spazioInMezzo; y += spazioInMezzo) {
                disegnaOttagono(g2d, x, y, size, coloreOttagono);
            }
        }
    }

    private void disegnaOttagono(Graphics2D g2d, int x, int y, int size, Color color) {
        int s = size / 3;

        int[] puntiX = { x, x + s, x + size - s, x + size, x + size, x + size - s, x + s, x };
        int[] puntiY = { y + s, y, y, y + s, y + size - s, y + size, y + size, y + size - s };

        g2d.setColor(color);
        g2d.fillPolygon(puntiX, puntiY, 8);
    }
    public void showPreLobby() {
        preLobbyFrame = new JFrame("SLITHER.IO");
        preLobbyFrame.setSize(700, 500);
        preLobbyFrame.setLocationRelativeTo(null);
        preLobbyFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // Carica l'immagine di sfondo e ridimensiona
        ImageIcon background = new ImageIcon("repodiprova-main/src/slitherionew.jpeg");
        Image backgroundImage = background.getImage().getScaledInstance(preLobbyFrame.getWidth(), preLobbyFrame.getHeight(), Image.SCALE_SMOOTH);
        background = new ImageIcon(backgroundImage);
        JLabel backgroundLabel = new JLabel(background);
        backgroundLabel.setLayout(new BorderLayout()); // Usa BorderLayout per la finestra

        JPanel panel = new JPanel();
        panel.setOpaque(false); // Rende il pannello trasparente
        panel.setLayout(new GridBagLayout()); // Layout per il contenuto
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.gridx = 0;

        // Pannello per i bottoni
        JPanel buttonPanel = new JPanel();
        buttonPanel.setOpaque(false); // Pannello trasparente
        buttonPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 20, 10)); // Flusso centrato con spazio tra i bottoni

        // Bottone Singleplayer
        JButton singlePlayerButton = new JButton("Singleplayer");
        styleButton(singlePlayerButton);
        buttonPanel.add(singlePlayerButton);

        // Bottone Multiplayer
        JButton multiPlayerButton = new JButton("Multiplayer");
        styleButton(multiPlayerButton);
        buttonPanel.add(multiPlayerButton);

        // Aggiungi il pannello dei bottoni alla parte inferiore della finestra
        backgroundLabel.add(buttonPanel, BorderLayout.SOUTH); // Aggiunge i bottoni nella parte inferiore della finestra

        // Azioni dei bottoni
        singlePlayerButton.addActionListener(e -> {
            gameClient.startSinglePlayer();
            preLobbyFrame.dispose();
        });

        multiPlayerButton.addActionListener(e -> {

            if (gameClient.isServerAvailable()) {
                gameClient.startMultiplayer();
                preLobbyFrame.dispose();
            } else {
                JOptionPane.showMessageDialog(preLobbyFrame, "Server non disponibile!", "Errore", JOptionPane.ERROR_MESSAGE);
            }
        });

        // Aggiungi il pannello principale con il contenuto
        backgroundLabel.add(panel, BorderLayout.CENTER);

        preLobbyFrame.setContentPane(backgroundLabel);
        preLobbyFrame.setVisible(true);
    }


    private void styleButton(JButton button) {
        button.setFont(new Font("SansSerif", Font.BOLD, 22));  // Ingrandito il font per il testo del bottone
        button.setPreferredSize(new Dimension(200, 60)); // Impostato una dimensione fissa (larghezza x altezza)
        button.setBackground(new Color(50, 205, 50)); // Colore verde brillante
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createLineBorder(new Color(34, 139, 34), 2)); // Aggiungiamo un bordo verde scuro
        button.setOpaque(true);

        button.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                button.setBackground(new Color(34, 139, 34)); // Colore verde scuro al passaggio del mouse
            }

            public void mouseExited(java.awt.event.MouseEvent evt) {
                button.setBackground(new Color(50, 205, 50)); // Torna al colore originale
            }
        });

        // Aggiungi un'ombra
        button.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Color.WHITE, 2),
                BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED)
        ));
    }




    private Color getRainbowColor() {
        float hue;
        do {
            hue = (float) Math.random();
        } while ((hue >= 0.9 || hue <= 0.1) || (hue >= 0.55 && hue <= 0.7));

        return Color.getHSBColor(hue, 1.0f, 1.0f);
    }

    private void drawEntities(Graphics2D g2d, int offsetX, int offsetY) {

        List<Player> players = gameController.getGameState().getPlayers();
        List<Bot> bots = gameController.getGameState().getBots();
        List<Food> foodItems = gameController.getGameState().getFoodItems();

        g2d.setColor(getRainbowColor());
        for (Food food : foodItems) {
            int screenX = (int) food.getPosition().getX() - offsetX;
            int screenY = (int) food.getPosition().getY() - offsetY;
            g2d.fillOval(screenX - 5, screenY - 5, 10, 10);
        }

        for (Bot bot : bots) {
            g2d.setColor(Color.RED);
            int screenX = (int) bot.getPosition().getX() - offsetX;
            int screenY = (int) bot.getPosition().getY() - offsetY;
            g2d.fillOval(screenX - 5, screenY - 5, 20, 20);

            g2d.setColor(Color.RED);
            for (Segment segment : bot.getBodySegments()) {
                int segX = (int) segment.getPosition().getX() - offsetX;
                int segY = (int) segment.getPosition().getY() - offsetY;
                g2d.fillOval(segX - 5, segY - 5, 16, 16);
            }
        }

        for (Player p : players) {
            g2d.setColor(Color.BLUE);
            int screenX = (int) p.getPosition().getX() - offsetX;
            int screenY = (int) p.getPosition().getY() - offsetY;
            g2d.fillOval(screenX - 10, screenY - 10, 20, 20);

            g2d.setColor(Color.BLUE);
            for (Segment segment : p.getBodySegments()) {
                int segX = (int) segment.getPosition().getX() - offsetX;
                int segY = (int) segment.getPosition().getY() - offsetY;
                g2d.fillOval(segX - 5, segY - 5, 16, 16);
            }
        }
    }
    class PlayerInfo {
        String playerId;
        double x, y;
        List<Segment> segments = new ArrayList<>();
        PlayerInfo(String playerId, double x, double y, List<Segment> segments) {
            this.playerId = playerId;
            this.x = x;
            this.y = y;
            this.segments.addAll(segments);
        }
        // Aggiungi un segmento alla lista
        public void addSegment(Segment segment) {
            segments.add(segment);
        }

        // Ottieni i segmenti
        public List<Segment> getSegments() {
            return segments;
        }
    }
    class BotInfo {
        String botId;
        double x, y;
        List<Segment> segments = new ArrayList<>();
        BotInfo(String botId,double x, double y, List<Segment> segments) {
            this.botId = botId;
            this.x = x;
            this.y = y;
            this.segments.addAll(segments);
        }
        // Aggiungi un segmento alla lista
        public void addSegment(Segment segment) {
            segments.add(segment);
        }

        // Ottieni i segmenti
        public List<Segment> getSegments() {
            return segments;
        }

    }
    class FoodInfo {
        String foodId;
        double x, y;
        FoodInfo(String foodId,double x, double y) {
            this.foodId = foodId;
            this.x = x;
            this.y = y;
        }
    }


    @Override
    public void keyPressed(KeyEvent e) {
        if (gameClient.getPlayer() == null) return;

        int keyCode = e.getKeyCode();
        switch (keyCode) {

            case KeyEvent.VK_SPACE:
                gameClient.getPlayer().activateBoost();
                break;
            case KeyEvent.VK_R:
                if (!gameClient.getPlayer().isAlive()) {
                    gameClient.getPlayer().respawn();
                    System.out.println("Player " + gameClient.getPlayer().getId() + " has respawned!");
                }
                break;
            case KeyEvent.VK_Q: // Esci dal gioco
                System.out.println("Exiting game...");
                System.exit(0);
                break;
        }
    }



    @Override
    public void keyReleased(KeyEvent e) {
        if (gameClient.getPlayer() == null) return;

        int keyCode = e.getKeyCode();
        switch (keyCode) {
            case KeyEvent.VK_SPACE:
                gameClient.getPlayer().deactivateBoost();
                break;
        }
    }

    @Override
    public void keyTyped(KeyEvent e) {
        // Non utilizzato
    }

    @Override
    public void mouseMoved(MouseEvent e) {
        if (gameClient.getPlayer() == null) return;

        Point mousePosition = e.getPoint();
        Point canvasCenter = new Point(this.getWidth() / 2, this.getHeight() / 2);

        double angle = Math.atan2(mousePosition.y - canvasCenter.y, mousePosition.x - canvasCenter.x);
        gameClient.getPlayer().setAngle(Math.toDegrees(angle));  // Imposta l'angolo del giocatore
    }

    @Override
    public void mouseDragged(MouseEvent e) {
        mouseMoved(e);
    }

    public GameController getGameController() {
        return gameController;
    }
}
