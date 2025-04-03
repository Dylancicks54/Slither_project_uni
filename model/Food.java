package model;

public class Food implements Point{
    private int x;
    private int y;
    private int colore;
    private int size;
    public Food(int x,int y,int color, int size){
        this.x = x;
        this.y = y;
        this.colore = color;
        this.size = size;
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
    public int getSize() {
        return size;
    }
    public void setSize(int size) {
        this.size = size;
    }
    @Override
    public String toString(){
        return x+":"+y;
    }
}