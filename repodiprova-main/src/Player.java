import java.awt.*;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class Player extends Entity {
    private String id;
    List<Segment> segments = new ArrayList<>();
    private double speed = 2.5;
    private double angle = 0.0;
    private boolean isBoosting = false; // Flag per verificare se il boost è attivo
    private boolean isCooldown = false; // Flag per verificare se l'abilità è in cooldown
    private long boostStartTime = 0; // Timestamp dell'inizio del boost
    private long cooldownEndTime = 0;
    private boolean isAlive = true;// Timestamp della fine del cooldown
    private LinkedList<Vector2D> trail = new LinkedList<>();

    public Player(String id) {
        this.id = id;
        this.position = new Vector2D(
                Math.random() * GameState.MAP_WIDTH,
                Math.random() * GameState.MAP_HEIGHT
        ); // Genera una posizione casuale nella mappa

        this.segments.add(new Segment(new Vector2D(this.position.x - 15, this.position.y - 15), 17));
    }


    public String getId() {
        return id;
    }
    public void respawn() {
        this.position = new Vector2D(
                Math.random() * GameState.MAP_WIDTH,
                Math.random() * GameState.MAP_HEIGHT
        ); // Nuova posizione casuale nella mappa

        this.segments.clear(); // Rimuove i vecchi segmenti
        this.segments.add(new Segment(new Vector2D(this.position.x - 15, this.position.y - 15), 17));

        this.speed = 2.5; // Resetta la velocità
        this.isAlive = true; // Il player torna in vita
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
        if (!isAlive) return; // Se il player è morto, non aggiorna lo stato

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

        // Blocca il player nei confini della mappa
        position.x = Math.max(0, Math.min(position.x, GameState.MAP_WIDTH));
        position.y = Math.max(0, Math.min(position.y, GameState.MAP_HEIGHT));

        // Salva la posizione attuale della testa nella trail
        trail.addFirst(new Vector2D(position.x, position.y));

        // Assicura che la lunghezza della trail non cresca all'infinito
        if (trail.size() > segments.size() + 5) {
            trail.removeLast();
        }

        // Ora facciamo seguire i segmenti
        for (int i = 0; i < segments.size(); i++) {
            int index = Math.min(i * 5, trail.size() - 1);
            segments.get(i).setPosition(trail.get(index));
        }
    }


    public void grow() {
        Segment lastSegment = segments.getLast();
        segments.add(new Segment(new Vector2D(lastSegment.getPosition().x, lastSegment.getPosition().y), lastSegment.getSize()));
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

    public void setId(String id) {
        this.id = id;
    }

    public boolean isAlive() {
        return isAlive;
    }

    public void setAlive(boolean alive) {
        isAlive = alive;
    }
}