public class Vector2D {
    public double x, y;

    public Vector2D(double x, double y) {
        this.x = x;
        this.y = y;
    }

    public Vector2D subtract(Vector2D other) {
        return new Vector2D(this.x - other.x, this.y - other.y);
    }

    public double distanceTo(Vector2D other) {
        return Math.sqrt(Math.pow(this.x - other.x, 2) + Math.pow(this.y - other.y, 2));
    }

    public Vector2D normalize() {
        double magnitude = length();  // Usa il metodo length() per normalizzare
        if (magnitude == 0) return new Vector2D(0, 0);
        return new Vector2D(x / magnitude, y / magnitude);
    }

    public Vector2D scale(double scalar) {
        return new Vector2D(this.x * scalar, this.y * scalar);
    }

    // Aggiungi il metodo length() che calcola la lunghezza del vettore
    public double length() {
        return Math.sqrt(x * x + y * y);
    }

    @Override
    public String toString() {
        return "Vector2D{" + "x=" + x + ", y=" + y + '}';
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    public Vector2D set(double x, double y) {
        return new Vector2D(x, y);
    }

    public void setY(double y) {
        this.y = y;
    }
    public Vector2D add(Vector2D other) {
        return new Vector2D(this.x + other.x, this.y + other.y);
    }

}


/*public class Vector2D {
    public double x;
    public double y;

    // Costruttore
    public Vector2D(double x, double y) {
        this.x = x;
        this.y = y;
    }

    // Metodo per sottrarre due vettori
    public Vector2D subtract(Vector2D other) {
        return new Vector2D(this.x - other.x, this.y - other.y);
    }

    // Metodo per moltiplicare un vettore per uno scalare
    public Vector2D scale(double scalar) {
        return new Vector2D(this.x * scalar, this.y * scalar);
    }

    // Metodo per normalizzare il vettore (per renderlo di lunghezza 1)
    public Vector2D normalize() {
        double magnitude = Math.sqrt(this.x * this.x + this.y * this.y);
        if (magnitude != 0) {
            return new Vector2D(this.x / magnitude, this.y / magnitude);
        }
        return new Vector2D(0, 0); // Evita la divisione per zero, restituisce un vettore nullo
    }

    // Metodo per calcolare la distanza tra due vettori
    public double distanceTo(Vector2D other) {
        double dx = this.x - other.x;
        double dy = this.y - other.y;
        return Math.sqrt(dx * dx + dy * dy);
    }

    // Metodo per stampare il vettore
 /*   @Override
   public String toString() {
        return "(" + x + ", " + y + ")";
    }
}
*/