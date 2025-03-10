import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.util.List;

public class GameWindow extends JPanel implements KeyListener, MouseMotionListener {
    private GameController gameController;
    private GameClient gameClient;
    private JFrame preLobbyFrame;

    public GameWindow(GameController gameController, GameClient gameClient) {
        this.gameController = gameController;
        this.gameClient = gameClient;
        setPreferredSize(new Dimension(1920, 1080));
        this.setFocusable(true);
        this.requestFocusInWindow();
        this.addKeyListener(this);
        this.addMouseMotionListener(this);
        // Avvia il game loop
        Timer timer = new Timer(20, e -> {
            gameController.updateGameState();
            repaint();
        });
        timer.start();

    }


    @Override
    protected void paintComponent(Graphics g) {
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

            if (gameController.isServerAvailable()) {
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

        List<Player> players = gameController.getPlayers();
        List<Bot> bots = gameController.getBots();
        List<Food> foodItems = gameController.getFoodItems();

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
}
