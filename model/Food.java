package model;

/**
 * Classe con la gestione logica del cibo
 */
public class Food implements Point{

    private int x;
    private int y;
    //Il colore è un numero tra 1 e 4 e viene usato per prendere uno dei diversi PNG del cibo
    private int colore;

    public final static int SIZE = 10;

    public Food(int x,int y,int color){
        this.x = x;
        this.y = y;
        this.colore = color;
    }

    public int getX() {
        return x;
    }
    public void setX(int x) {
        this.x = x;
    }
    public int getY() {
        return y;
    }
    public void setY(int y) {
        this.y = y;
    }
    public int getColore() {
        return colore;
    }

    public void setColore(int colore) {
        this.colore = colore;
    }

    @Override
    public String toString(){
        return x+":"+y;
    }
}