package model;
/**
 * Interfaccia per uniformare Pair e Food insieme per la serializzazione e per i metodi di Snake
 */
public interface Point {

    public int getX();
    public int getY();
    @Override
    public String toString();
}
