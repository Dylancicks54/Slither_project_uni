package model;
import view.GameWindow;

import java.awt.*;
import java.util.LinkedList;

/**
 * Modello con la gestione della logica del giocatore
 */
public class Snake {

    private LinkedList<SnakeBodyPart> body;
    private Direction direction;
    private boolean isAccelerating;
    private int speed;
    private int mouseX, mouseY;

    private int slow;

    private static final int START_SIZE = 5;
    private static final int START_SPEED= 5;
    //Tempo in accelerazione dopo il quale il corpo "perde" un elemento
    private static final int SHRINK_THRESHOLD = 25;

    public static final int SEGMENT_SIZE = 15;

    public Snake(int x,int y,Direction d){
        this.slow=0;
        this.direction=d;

        this.mouseX=0;
        this.mouseY=0;
        this.speed = START_SPEED;
        this.isAccelerating=false;
        this.body=new LinkedList<>();

        for(int i=START_SIZE;i>0;i--){
            body.add(new SnakeBodyPart(x, y));
        }
    }

    /**
     * Metodo che gestisce il movimento dello snake.
     * Si muove verso le coordinate del mouse che vengono passate in input
     * @param mouseX ascissa posizione mouse player
     * @param mouseY ordinata posizione mouse player
     */
    public void move(int mouseX, int mouseY) {

        //Gli elementi del corpo seguono la posizioni degli elementi precedenti
        for (int i = body.size() - 1; i > 0; i--) {
            body.get(i).setX(body.get(i - 1).getX());
            body.get(i).setY(body.get(i - 1).getY());
        }

        //Calcolo l'angolo che si deve muove per raggiungere la destinazione indicata dal mouse
        double angle = Math.atan2(mouseY -GameWindow.getWindowHeight()/2, mouseX - GameWindow.getWindowWidth()/2);

        int newX = (int) (body.get(0).getX() + speed * Math.cos(angle));
        int newY = (int) (body.get(0).getY() + speed * Math.sin(angle));

        //Sposto la testa
        body.get(0).setX(newX);
        body.get(0).setY(newY);

        // Se è attiva l'accelerazione, aumento la velocità e restringo il corpo
        if (isAccelerating && body.size()>5 && speed<8) {
            speed++;
            restringiPlayer();
        } else if(speed>START_SPEED){
            speed--;
        }
    }

    /**
     * Metodo che aumenta la dimensione dello snake.
     *
     * Aggiungo un nuovo elemento in coda alla lista di SnakeBodyPart
     */
    public void grow() {
        SnakeBodyPart bodyLast = body.getLast();
        SnakeBodyPart newBodyLast = new SnakeBodyPart(bodyLast.getX(), bodyLast.getY());
        body.addLast(newBodyLast);
    }

    /**
     * Metodo che gestisce il restrigimento dovuto all'accelerazione.
     *
     * Dopo che accelero per tot tempo, restingo il corpo
     */
    public void restringiPlayer(){
        if(slow== SHRINK_THRESHOLD) {
            body.removeLast();
            slow=0;
        }
        else
            slow++;
    }

    /**
     * Metodo che controlla la collisione con un cibo
     *
     * @param food cibo preso in esame
     * @return true se collidono, false in tutti gli altri casi
     */
    public boolean collisionsWithFood(Point food) {
        return body.getFirst().getX() < food.getX() + SEGMENT_SIZE &&
                body.getFirst().getX() + SEGMENT_SIZE > food.getX() &&
                body.getFirst().getY() < food.getY() + SEGMENT_SIZE &&
                body.getFirst().getY() + SEGMENT_SIZE > food.getY();
    }

    /**
     * Metodo che controlla se la testa dello snake in input tocca qualsiasi parte del serpente
     * @param snakes LinkedList di tutti i snakeBodyPart degli snake di tutti i giocatori
     * @return true se il serpente tocca un'altro corpo, false altrimenti
     */
    public boolean collisionsWithBody(LinkedList<SnakeBodyPart> snakes) {
        SnakeBodyPart head = body.get(0);
        for (int i=0; i < snakes.size(); i++) {
            if (distance(head.getX(), snakes.get(i).getX(), head.getY(), snakes.get(i).getY())< SEGMENT_SIZE) {
                return true;
            }
        }
        return false;
    }
    /**
     * Metodo che calcola la distanza tra due punti e la restituisce
     * @return la distanza tra due punto come intero
     */
    public static int distance(int x1,int x2, int y1, int y2){
        double x=Math.pow(x1-x2,2);
        double y =Math.pow(y1-y2,2);
        return (int)Math.round(Math.sqrt(x+y));
    }

    public boolean isAccelerating() {
        return isAccelerating;
    }

    public void setAccelerating(boolean accelerating) {
        isAccelerating = accelerating;
    }

    public LinkedList<SnakeBodyPart> getBody(){
        return this.body;
    }

    public Direction getDirection() {
        return direction;
    }

    public void setDirection(Direction direction) {
        this.direction = direction;
    }
    public int getMouseX() {
        return mouseX;
    }
    public void setMouseX(int mouseX) {
        this.mouseX = mouseX;
    }

    public int getMouseY() {
        return mouseY;
    }
    public void setMouseY(int mouseY) {
        this.mouseY = mouseY;
    }

}