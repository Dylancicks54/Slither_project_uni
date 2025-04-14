package view;

import Net.*;
import view.GameViewer.*;
import javax.swing.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

/**
 * Vista generica che gestisce entrambe le modalità di gioco
 */
public class GameWindow extends JFrame {

    /**
     * Costruttore per la modalità multiplayer
     * @param client
     */
    public GameWindow (Client client){
        setTitle("Slither.io");
        setSize(getWindowWidth(),getWindowHeight());
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(false);

        ServerGameView g = new ServerGameView(client);
        addMouseListener(g.getGc());
        addMouseMotionListener(g.getGc());
        setContentPane(g);
        setVisible(true);

        addWindowListener(new WindowAdapter()
        {
            @Override
            public void windowClosing(WindowEvent e)
            {
                    SwingUtilities.invokeLater(ShowPreLobby::new);
                    client.close();
                    e.getWindow().dispose();
            }

        });
    }

    /**
     * Costruttore per la modalità singleplayer
     */
    public GameWindow(){

        setTitle("Slither.io");
        setSize(getWindowWidth(),getWindowHeight());
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(false);

        SoloGameView g = new SoloGameView();
        setContentPane(g);

        addKeyListener(g.getGc());
        addMouseListener(g.getGc());
        addMouseMotionListener(g.getGc());

        setVisible(true);

    }

    public static int  getWindowWidth(){
        return  1100;
    }
    public static int  getWindowHeight(){
        return  600;
    }


}