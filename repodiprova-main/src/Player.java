import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class Player extends Entity {
    private String id;
    List<Segment> segments = new ArrayList<>();
    private double speed = 2.5;
    private double angle = 0.0;
    private Image segmentTexture;
    private boolean isBoosting = false; // Flag per verificare se il boost è attivo
    private boolean isCooldown = false; // Flag per verificare se l'abilità è in cooldown
    private long boostStartTime = 0; // Timestamp dell'inizio del boost
    private long cooldownEndTime = 0; // Timestamp della fine del cooldown

    public Player(String id, Image segmentTexture) {
        this.id = id;
        this.segmentTexture = segmentTexture;
        this.position = new Vector2D(Math.random() * 1000, Math.random() * 1000); // Posizione iniziale
        this.segments.add(new Segment(position, 10, segmentTexture)); // Segmento iniziale
    }

    public String getId() {
        return id;
    }

    public void activateBoost() {
        if (!isBoosting && !isCooldown) {
            isBoosting = true;
            boostStartTime = System.currentTimeMillis(); // Imposta l'inizio del boost
            speed *= 2; // Raddoppia la velocità
        }
    }
    public void deactivateBoost() {
        if (isBoosting) {
            isBoosting = false;
            isCooldown = false;
            cooldownEndTime = System.currentTimeMillis() + 3000;
            speed /= 2;
        }
    }

    public void updateBoostStatus() {
        long currentTime = System.currentTimeMillis();

        if (isBoosting && currentTime - boostStartTime >= 5000) { // 5 secondi di boost
            isBoosting = false;
            isCooldown = true; // Attiva il cooldown
            cooldownEndTime = currentTime + 3000; // Cooldown di 3 secondi
            speed /= 2; // Ripristina la velocità normale
        }

        if (isCooldown && currentTime >= cooldownEndTime) {
            isCooldown = false; // Fine del cooldown
        }
    }

    public boolean isBoostAvailable() {
        return !isBoosting && !isCooldown;
    }

    public boolean isBoosting() {
        return isBoosting;
    }

    @Override
    public void update() {
        updateBoostStatus(); // Aggiorna lo stato del boost
        move();
    }

    @Override
    public boolean collidesWith(Entity other) {
        double distance = this.position.distanceTo(other.position);
        return distance < (this.size + other.size);
    }

    public void move() {
        velocity.x = Math.cos(Math.toRadians(angle)) * speed;
        velocity.y = Math.sin(Math.toRadians(angle)) * speed;

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
        if (speed > 2) speed -= 0.03;
    }

    public void setAngle(double angle) {
        this.angle = angle;
    }

    public Vector2D getVelocity() {
        return velocity;
    }

    public void setVelocity(Vector2D velocity) {
        this.velocity = velocity;
    }

    public List<Segment> getBodySegments() {
        return segments;
    }

    public int getSegmentSize() {
        return segments.size();
    }

    public double getSpeed() {
        return speed;
    }
    public void setPosition(Vector2D position) {
        if (position == null) {
            throw new IllegalArgumentException("Position cannot be null");
        }
        this.position = new Vector2D(position.x, position.y);
    }

}
