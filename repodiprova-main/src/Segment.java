import java.awt.*;

public class Segment {
    private Vector2D position;
    private double size;
    private Image texture;

    public Segment(Vector2D position, double size, Image texture) {
        this.position = position;
        this.size = size;
        this.texture = texture;
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

        if (distance > size) {
            position.x += dx / distance * size;
            position.y += dy / distance * size;
        }

    }
    public boolean collidesWith(Segment other) {
        double distance = this.position.distanceTo(other.getPosition());
        return distance < (this.size + other.getSize());
    }


//    public void render(Graphics g) {
//        if (texture != null) {
//            g.drawImage(texture, (int) position.x, (int) position.y, (int) size, (int) size, null);
//        } else {
//            g.setColor(Color.GREEN);
//            g.fillOval((int) position.x, (int) position.y, (int) size, (int) size);
//        }
//    }
}
/*public class Segment {
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

        if (distance > size) {
            position.x += dx / distance * size;
            position.y += dy / distance * size;
        }
    }
}
*/