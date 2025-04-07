package model;

/**
 * Modello che identifica una coordinata nella mappa di gioco
 */
public class Pair implements Point{
    public int x;
    public int y;

    public Pair(int x, int y) {
        this.y = y;
        this.x = x;
    }

    public int getX() {
        return x;
    }
    public int getY() {
        return y;
    }
    @Override
    public String toString() {
        return x+":"+y;
    }
}