import java.awt.*;

public class Segment {
    private Vector2D position;
    private double size;


    public Segment(Vector2D position, double size) {
        this.position = position;
        this.size = size;

    }

    public Vector2D getPosition() {
        return position;
    }

    public double getSize() {
        return size;
    }

    public void follow(Vector2D target) {
        double dx = target.x - position.x;
        double dy = target.y - position.y;
        double distance = Math.sqrt(dx * dx + dy * dy);

        double followDistance = size; // Usa la dimensione del segmento per un follow preciso

        if (distance > followDistance) {
            position.x += dx / distance * followDistance;
            position.y += dy / distance * followDistance;
        }
    }


    public boolean collidesWith(Entity other) {
        double distance = this.position.distanceTo(other.getPosition());
        return distance < (this.size + other.getSize()) /2;
    }
    public void setPosition(Vector2D newPosition) {
        this.position.x = newPosition.x;
        this.position.y = newPosition.y;
    }

}