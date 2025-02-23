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


    public Bot(Vector2D startPosition, List<Entity> entities, GameState gameState) {
        this.position = startPosition;
        this.size = 17;
        this.velocity = new Vector2D(0, 0);
        this.currentState = State.IDLE;
        this.controller = new AIController(this, entities); // AIController si basa sulle entità
        this.speed = 2.5;
        this.segments = new ArrayList<>();
        this.gameState = gameState;  //  ASSEGNA GAMESTATE
    }


    public static Bot createBot(List<Entity> entities, GameState gameState) {
        Vector2D newPosition;
        boolean positionValid;
        do {
            newPosition = new Vector2D(Math.random() * 800, Math.random() * 600);
            positionValid = true;
            for (Entity entity : entities) {
                if (newPosition.distanceTo(entity.getPosition()) < 50) {
                    positionValid = false;
                    break;
                }
            }
        } while (!positionValid);

        return new Bot(newPosition, entities, gameState);
    }



    @Override
    public void update() {
        controller.decideBehavior();
        calculateNextMove();
        move();
    }

    public void move() {
        position.x += velocity.x;
        position.y += velocity.y;

        if (!segments.isEmpty()) {
            for (int i = segments.size() - 1; i > 0; i--) {
                segments.get(i).follow(segments.get(i - 1).getPosition());
            }
            segments.getFirst().follow(position);
        }
    }


    public void grow() {
        if (!segments.isEmpty()) {
            Segment lastSegment = segments.get(segments.size() - 1);
            Vector2D newSegmentPosition = lastSegment.getPosition().subtract(velocity.normalize().scale(size));
            segments.add(new Segment(newSegmentPosition, size));
        } else {
            // Se non ha segmenti, aggiungi il primo segmento dietro alla testa
            Vector2D newSegmentPosition = position.subtract(velocity.normalize().scale(size));
            segments.add(new Segment(newSegmentPosition, size));
        }

        // Rallenta leggermente quando cresce
        if (speed > 2) speed -= 0.03;
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
        this.velocity = direction.scale(this.speed);

        // Simula il prossimo passo
        double newX = position.x + velocity.x;
        double newY = position.y + velocity.y;

        // Controlla se il bot sta per uscire
        if (newX < 0 || newX > GameState.MAP_WIDTH || newY < 0 || newY > GameState.MAP_HEIGHT) {
            // Se sta per uscire, scegli una nuova direzione casuale all'interno della mappa
            Vector2D newTarget = getRandomPositionWithinBounds();
            moveTowards(newTarget); // Ricalcola la nuova direzione
        } else {
            // Movimento normale
            this.position.x = newX;
            this.position.y = newY;
        }
    }

    // Metodo per ottenere una posizione casuale all'interno dei limiti della mappa
    private Vector2D getRandomPositionWithinBounds() {
        double x = Math.random() * GameState.MAP_WIDTH;
        double y = Math.random() * GameState.MAP_HEIGHT;
        return new Vector2D(x, y);
    }



    public List<Segment> getBodySegments() {
        return segments;
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
