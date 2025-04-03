package model;
import view.GameWindow;

import java.awt.*;
import java.util.LinkedList;
public class Snake {
    private LinkedList<SnakeBodyPart> body;
    private int position;
    private int startSize=5;
    private Direction direction;
    private boolean isAccelerating;
    private int speed = 5;
    private   int initialSpeed = 5;
    private int mouseX, mouseY;

    private int slow;

    public Snake(int position,Direction d){
        this.slow=0;
        this.direction=d;
        this.position=position;
        this.mouseX=0;
        this.mouseY=0;
        this.isAccelerating=false;
        this.body=new LinkedList<>();
        for(int i=startSize;i>0;i--){
            body.add(new SnakeBodyPart(i*position, Toolkit.getDefaultToolkit().getScreenSize().height/2));
        }
    }
    public void move(int mouseX, int mouseY) {

        for (int i = body.size() - 1; i > 0; i--) {
            body.get(i).setX(body.get(i - 1).getX());
            body.get(i).setY(body.get(i - 1).getY());
        }
        double angle = Math.atan2(mouseY -GameWindow.getWindowHeight()/2, mouseX - GameWindow.getWindowWidth()/2);

        int newX = (int) (body.get(0).getX() + speed * Math.cos(angle));
        int newY = (int) (body.get(0).getY() + speed * Math.sin(angle));

        body.get(0).setX(newX);
        body.get(0).setY(newY);

        // Augmentation linéaire de la vitesse lorsque l'accélération est activée
        if (isAccelerating && body.size()>5 && speed<8) {
            speed++;
            restringiPlayer();
        } else if(speed>initialSpeed){
            speed--;
        }
    }

    public void grow() {
        SnakeBodyPart bodyLast = body.getLast();
        SnakeBodyPart newBodyLast = new SnakeBodyPart(bodyLast.getX(), bodyLast.getY());
        body.addLast(newBodyLast);
    }

    public void restringiPlayer(){
        if(slow==25) {
            body.removeLast();
            slow=0;
        }
        else
            slow++;
    }

    public boolean collisionsWithFood(Point food) {
        return body.getFirst().getX() < food.getX() + 10 &&
                body.getFirst().getX() + 10 > food.getX() &&
                body.getFirst().getY() < food.getY() + 10 &&
                body.getFirst().getY() + 10 > food.getY();
    }

    /**
     * collisionsWithBody check if the head of the snake in parameter touch any part of the snake
     * @param snakes LinkedList of all snakeBodyPart of all player's snake
     * @return true if the snake touch another snake body, false if it doesn't
     */
    public boolean collisionsWithBody(LinkedList<SnakeBodyPart> snakes) {
        SnakeBodyPart head = body.get(0);
        for (int i=0; i < snakes.size(); i++) {
            if (distance(head.getX(), snakes.get(i).getX(), head.getY(), snakes.get(i).getY())<15) {
                return true;
            }
        }
        return false;
    }
    /**
     * distance calculate and return the distance from two point
     * @return distance form two point as int
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

    public int getPosition() {
        return position;
    }
}