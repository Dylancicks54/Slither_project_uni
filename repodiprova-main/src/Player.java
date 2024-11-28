import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class Player extends Entity {
    private String id;
    List<Segment> segments = new ArrayList<>();
    private double speed = 2.5;
    private double angle = 0.0;
    private Image segmentTexture;

    public Player(String id, Image segmentTexture) {
        this.id = id;
        this.segmentTexture = segmentTexture;
        this.position = new Vector2D(250, 250); // Posizione iniziale
        this.segments.add(new Segment(position, 10, segmentTexture)); // Segmento iniziale
    }
    public Segment getFirstseg(){
        return new Segment(new Vector2D(250,250), 10, segmentTexture);
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
        if(speed > 2) speed -= 0.03;

    }

//    public void die() {
//        System.out.println(id + " has died.");
//    }

    public void setAngle(double angle) {
        this.angle = angle;
    }

    @Override
    public void update() {
        move();
    }

    @Override
    public boolean collidesWith(Entity other) {
        double distance = this.position.distanceTo(other.position);
        return distance < (this.size + other.size);
    }

//    public void render(Graphics g) {
//        for (Segment segment : segments) {
//            segment.render(g);
//        }
//    }
    public Vector2D getVelocity() {
        return velocity;
    }

    // Aggiungi il setter per la velocità se necessario
    public void setVelocity(Vector2D velocity) {
        this.velocity = velocity;
    }

    public List<Segment> getBodySegments() {
        return segments;
    }

    public int getSegmentSize() {
        return segments.size();
    }
}
/*import java.util.ArrayList;
import java.util.List;

public class Player extends Entity {
    private String id;
    List<Segment> segments = new ArrayList<>();
    private double speed = 5.0;
    private double angle = 0.0;

    public Player(String id) {
        this.id = id;
        this.position = new Vector2D(250, 250); // Posizione iniziale
        this.segments.add(new Segment(position, 10)); // Segmento iniziale
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
        segments.add(new Segment(new Vector2D(lastSegment.getPosition().x, lastSegment.getPosition().y), lastSegment.getSize()));
        speed += 0.1; // Incrementa leggermente la velocità
        System.out.println("Giocatore " + id + " è cresciuto! Lunghezza: " + segments.size());
    }


    public void die() {
        System.out.println(id + " has died.");
    }

    public void setAngle(double angle) {
        this.angle = angle;
    }

    @Override
    public void update() {
        move();
    }

    @Override
    public boolean collidesWith(Entity other) {
        double distance = this.position.distanceTo(other.position);
        return distance < (this.size + other.size);
    }
    // Aggiungi il getter per la velocità
    public Vector2D getVelocity() {
        return velocity;
    }

    // Aggiungi il setter per la velocità se necessario
    public void setVelocity(Vector2D velocity) {
        this.velocity = velocity;
    }

}
*/