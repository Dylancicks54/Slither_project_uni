package view;

import Net.Client;
import view.GameViewer.ServerGameView;
import view.GameViewer.SoloGameView;
import javax.swing.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;


public class GameWindow extends JFrame {
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