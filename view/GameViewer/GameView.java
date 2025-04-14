package view.GameViewer;
import view.*;
import javax.swing.*;
import java.awt.*;
/**
 * Classe astratta per riportare i metodi in comuni che le viste delle varie modalità devono avere in comune
 * */
public abstract class GameView extends JPanel{
    /**
     * Motodo per visualizzare immagini sulla finestra di gioco
     */
    @Override
    public void paintComponent(Graphics g) {
    }

    /**
     * Metodo per aggiornare il timer di gioco
     */
    public abstract void updateTimerLabel();

    /**
     * Metodo per mostrare il menù principale quando si chiude la finestra di gioco
     */
    public void showMenu() {
        SwingUtilities.invokeLater(() -> {
            new ShowPreLobby().setVisible(true);
            closeCurrentGameWindow();
        });
    }

    /**
     * Metodo per chiudere la finestra di gioco correntemente aperta
     */
    public void closeCurrentGameWindow() {
        Window window = SwingUtilities.getWindowAncestor(this);
        if (window != null) {
            window.dispose();
        }
    }

    /**
     * Metodo che mostra un messaggio a video quando il tempo è finito
     */
    public void showTimeUpDialog() {
        JOptionPane.showMessageDialog(this, "Tempo scaduto!", "Game Over", JOptionPane.INFORMATION_MESSAGE);
    }

    /**
     * Metodo che mostra un messaggio a video quando si perde
     */
    public void showLoseDialog() {
        JOptionPane.showMessageDialog(this, "Hai perso!", "Game Over", JOptionPane.INFORMATION_MESSAGE);
    }

}
