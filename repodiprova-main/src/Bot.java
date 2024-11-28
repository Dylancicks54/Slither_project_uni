import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class Bot extends Entity {
    private Image segmentTexture;
    private AIController controller;
    public State currentState;
    GameState gameState;
    double speed;
    List<Segment> segments = new ArrayList<>();


    public Bot(Vector2D startPosition,GameState gameState, Image segmentTexture) {
        this.position = startPosition;
        this.size = 10;
        this.velocity = new Vector2D(0, 0);
        this.segmentTexture = segmentTexture;
        this.currentState = State.IDLE;
        this.controller = new AIController(this);
        this.gameState = gameState;
        this.speed = 2.5;
        this.segments.add(new Segment(position, 10, segmentTexture));
    }

//    public Bot creaBot(){
//
//    }
    @Override
    public void update() {
        // Logica per il movimento del bot, rilevamento del cibo e altre azioni
        //this.move();
        controller.decideBehavior();
        calculateNextMove();
        move();
    }

    public void move() {
        // Esempio di movimento: aggiorna la posizione del bot (questa è una logica di base)
        position.x += velocity.x;
        position.y += velocity.y;

        for (int i = segments.size() - 1; i > 0; i--) {
            segments.get(i).follow(segments.get(i - 1).getPosition());
        }
        segments.get(0).follow(position);
    }

    public void grow() {
        Segment lastSegment = segments.get(segments.size() - 1);
        segments.add(new Segment(new Vector2D(lastSegment.getPosition().x, lastSegment.getPosition().y), lastSegment.getSize(), segmentTexture));
        if(speed > 2) speed -= 0.03;

    }


    @Override
    public boolean collidesWith(Entity other) {
        double distance = this.position.distanceTo(other.position);
        return distance < (this.size + other.size);
    }

    // Aggiungi un metodo per ottenere il GameState (se non è già presente)
    public GameState getGameState() {
        return gameState; // Assicurati che il GameState sia definito nella classe Bot
    }
    public void moveTowards(Vector2D targetPosition) {
        Vector2D direction = targetPosition.subtract(this.position).normalize();
        this.velocity = direction.scale(this.speed); // Imposta la velocità basandosi sulla direzione
    }


    public Vector2D getVelocity() {
        return velocity;
    }

    // Setter per la velocità
    public void setVelocity(Vector2D velocity) {
        this.velocity = velocity;
    }
//    public void render(Graphics g) {
//        g.drawImage(segmentTexture, (int) position.x, (int) position.y, (int) size, (int) size, null);
//    }
    public void calculateNextMove() {
        switch (currentState) {
            case IDLE:

                currentState = State.MOVING_TO_FOOD;
                break;

            case MOVING_TO_FOOD:

                controller.seekFood(); // Spostati verso il cibo
                break;

            case AVOIDING_COLLISION:

                controller.avoidCollision(); // Evita ostacoli
                currentState = State.MOVING_TO_FOOD;
                break;
        }
    }
    public void setSpeed(double speed) {
        this.speed = speed; // Metodo per impostare la velocità
    }

    public double getSpeed() {
        return speed; // Metodo per ottenere la velocità
    }

}
