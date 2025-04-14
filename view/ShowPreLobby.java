package view;

import controller.*;
import javax.swing.*;
import java.awt.*;

public class ShowPreLobby extends JFrame {
    private GameController gameController;
    private ImageIcon background;
    private Image backgroundImage;


    public ShowPreLobby() {
        // Configurazione della finestra (this)
        setTitle("SLITHER.IO");
        setSize(700, 500);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // Caricamento e ridimensionamento dell'immagine di background
        background = new ImageIcon(getClass().getResource("/resources/slitherionew.jpeg"));
        backgroundImage = background.getImage().getScaledInstance(getWidth(), getHeight(), Image.SCALE_SMOOTH);
        background = new ImageIcon(backgroundImage);
        JLabel backgroundLabel = new JLabel(background);
        backgroundLabel.setLayout(new BorderLayout());

        // Creazione del pannello centrale (opzionale)
        JPanel panel = new JPanel();
        panel.setOpaque(false);
        panel.setLayout(new GridBagLayout());

        // Configurazione del pannello dei bottoni
        JPanel buttonPanel = new JPanel();
        buttonPanel.setOpaque(false);
        buttonPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 20, 10));

        // Bottone Singleplayer
        JButton singlePlayerButton = new JButton("Singleplayer");
        stileBottoni(singlePlayerButton);
        buttonPanel.add(singlePlayerButton);

        // Bottone Multiplayer
        JButton multiPlayerButton = new JButton("Multiplayer");
        stileBottoni(multiPlayerButton);
        buttonPanel.add(multiPlayerButton);

        // Aggiunge il pannello dei bottoni in basso
        backgroundLabel.add(buttonPanel, BorderLayout.SOUTH);

        // Azioni dei bottoni
        singlePlayerButton.addActionListener(e -> {
            dispose(); // Chiude la finestra corrente
            new GameWindow();
        });

        multiPlayerButton.addActionListener(e -> {
            dispose();
            new ConnectionMenu();
        });

        // Aggiunge il pannello centrale se necessario
        backgroundLabel.add(panel, BorderLayout.CENTER);

        // Imposta il content pane e rende la finestra visibile
        setContentPane(backgroundLabel);
    }

    private void stileBottoni(JButton button) {
        button.setFont(new Font("SansSerif", Font.BOLD, 22));
        button.setPreferredSize(new Dimension(200, 60));
        button.setBackground(new Color(50, 205, 50));
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createLineBorder(new Color(34, 139, 34), 2));
        button.setOpaque(true);

        button.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                button.setBackground(new Color(34, 139, 34));
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                button.setBackground(new Color(50, 205, 50));
            }
        });

        button.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Color.WHITE, 2),
                BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED)
        ));
    }

    public static void main(String[] args) {
        // Istanzia e mostra la finestra correttamente configurata
        new ShowPreLobby().setVisible(true);
    }
}
