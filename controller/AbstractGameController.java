package controller;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;


/**
 * Classe astratta per tutti i controller
 */
public abstract class AbstractGameController extends MouseAdapter {

    public abstract void mouseMoved(MouseEvent e);
}
