import java.awt.Color;

public class Food extends Entity {
    private int nutritionalValue;
    private Color color;
    private Bot claimedBy;

    public Food(Vector2D position, int nutritionalValue) {
        this.position = position;
        this.size = 8;
        this.nutritionalValue = nutritionalValue;
        this.color = Color.GREEN;
    }

    public Bot getClaimedBy() {
        return claimedBy;
    }

    public void setClaimedBy(Bot claimedBy) {
        this.claimedBy = claimedBy;
    }
    @Override
    public void update() {
        // Il cibo non si muove
    }

    @Override
    public boolean collidesWith(Entity other) {
        double distance = this.position.distanceTo(other.position);
        return distance < (this.size + other.size);
    }

    public int getNutritionalValue() {
        return nutritionalValue;
    }

    @Override
    public String toString() {
        return "Food{position=" + position + ", size=" + size + '}';
    }
}
