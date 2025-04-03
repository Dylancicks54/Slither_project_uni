package view;

import Net.Client;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;

public class ConnectionMenu extends JFrame {
    private Client client;

    public ConnectionMenu() {
        setTitle("Slither.io Menu");
        setSize(1100, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(false);
        setLayout(null);

        // Dimensioni della finestra
        int frameWidth = 1100;
        int frameHeight = 600;

        // Carica e scala l'immagine di background
        ImageIcon backgroundImageIcon = new ImageIcon("resources/background_menu.jpg");
        Image backgroundImage = backgroundImageIcon.getImage();
        Image scaledBackground = backgroundImage.getScaledInstance(frameWidth, frameHeight, Image.SCALE_SMOOTH);
        ImageIcon scaledBackgroundIcon = new ImageIcon(scaledBackground);
        JLabel backgroundLabel = new JLabel(scaledBackgroundIcon);
        backgroundLabel.setBounds(0, 0, frameWidth, frameHeight);

        // Titolo (spostato leggermente più in alto)
        int titleWidth = 400;
        int titleHeight = 60;
        JLabel titleLabel = new JLabel("SLITHER.IO GAME", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 34));
        titleLabel.setForeground(Color.RED);
        titleLabel.setBounds((frameWidth - titleWidth) / 2, 20, titleWidth, titleHeight);

        // Host Address Field
        int hostWidth = 200;
        int hostHeight = 40;
        JTextField hostAddress = new JTextField("localhost");
        hostAddress.setBounds((frameWidth - hostWidth) / 2 , 150, hostWidth, hostHeight);
        hostAddress.setBackground(Color.BLACK);
        hostAddress.setForeground(Color.RED);
        hostAddress.setFont(new Font("Arial", Font.BOLD, 18));
        hostAddress.setHorizontalAlignment(JTextField.CENTER);
        hostAddress.setBorder(BorderFactory.createLineBorder(Color.RED, 2));
        hostAddress.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                hostAddress.setText("");
            }
        });

        // Username Field (ingrandito)
        int usernameWidth = 250;
        int usernameHeight = 60;
        JTextField usernameTextBox = new JTextField("Your Username");
        usernameTextBox.setBounds((frameWidth - usernameWidth) / 2, 250, usernameWidth, usernameHeight);
        usernameTextBox.setBackground(Color.BLACK);
        usernameTextBox.setForeground(Color.RED);
        usernameTextBox.setFont(new Font("Arial", Font.BOLD, 22));
        usernameTextBox.setHorizontalAlignment(JTextField.CENTER);
        usernameTextBox.setBorder(BorderFactory.createLineBorder(Color.RED, 2));
        usernameTextBox.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                usernameTextBox.setText("");
            }
        });

        // Join Button (ingrandito e abbassato leggermente)
        int buttonWidth = 250;
        int buttonHeight = 60;
        JButton join = new JButton("JOIN");
        join.setBounds((frameWidth - buttonWidth) / 2, 350, buttonWidth, buttonHeight);
        join.setBackground(Color.RED);
        join.setForeground(Color.BLACK);
        join.setFont(new Font("Arial", Font.BOLD, 24));
        join.setBorder(BorderFactory.createLineBorder(Color.BLACK, 3));
        join.setFocusPainted(false);
        join.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                join.setBackground(Color.BLACK);
                join.setForeground(Color.RED);
            }
            @Override
            public void mouseExited(MouseEvent e) {
                join.setBackground(Color.RED);
                join.setForeground(Color.BLACK);
            }
        });

        join.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                dispose();
                String username = usernameTextBox.getText();
                String hostAdressString = hostAddress.getText();
                try {
                    Socket socket = new Socket(InetAddress.getByName(hostAdressString), 1234);
                    Client client = new Client(socket, username);
                    client.confirmConnection();
                    new Thread(client::listenForMessage).start();
                    new GameWindow(client);
                } catch (IOException ignore) {
                    System.out.println("CLIENT: connection error");
                    new ShowPreLobby().setVisible(true);
                }
            }
        });

        // Aggiungi i componenti
        add(titleLabel);
        add(hostAddress);
        add(usernameTextBox);
        add(join);
        add(backgroundLabel);

        getContentPane().setBackground(Color.BLACK);
        setLocationRelativeTo(null);
        setVisible(true);
    }
}
