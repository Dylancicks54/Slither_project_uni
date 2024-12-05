public abstract class Entity {
    protected Vector2D position;
    protected Vector2D velocity = new Vector2D(0, 0);
    protected double size;

    public abstract void update();

    public abstract boolean collidesWith(Entity other);

    public Vector2D getPosition() {
        return position;
    }

    public double getSize() {
        return size;
    }

    public void setPosition(Vector2D vector2D) {
        position = vector2D;
    }
}
/*public abstract class Entity {
    protected Vector2D position;
    protected Vector2D velocity = new Vector2D(0, 0);
    protected double size;

    public abstract void update();

    public abstract boolean collidesWith(Entity other);

    public Vector2D getPosition() {
        return position;
    }

    public double getSize() {
        return size;
    }
}
*/