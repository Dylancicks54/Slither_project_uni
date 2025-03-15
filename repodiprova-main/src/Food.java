import java.awt.Color;

public class Food extends Entity {
    private int nutritionalValue;
    private Color color;
    private Bot claimedBy;
    private String identifier;

    public Food(Vector2D position, int nutritionalValue, String identifier) {
        this.position = position;
        this.size = 8;
        this.nutritionalValue = nutritionalValue;
        this.color = Color.GREEN;
        this.identifier = identifier;
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

    public String getIdentifier() {
        return identifier;
    }

    @Override
    public String toString() {
        return "Food{position=" + position + ", size=" + size + '}';
    }
}
