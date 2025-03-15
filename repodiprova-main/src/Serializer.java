import java.util.ArrayList;
import java.util.List;

public class Serializer {

    /**
     * Serializza un oggetto Player.
     * Formato:
     * PLAYER <playerId> <x> <y> [SEG <seg1_x>,<seg1_y>|<seg2_x>,<seg2_y>|...]
     */
    public static String serializePlayer(Player p) {
        StringBuilder sb = new StringBuilder();
        sb.append("PLAYER ")
                .append(p.getId()).append(" ")
                .append(p.getPosition().getX()).append(" ")
                .append(p.getPosition().getY());

        List<Segment> segments = p.getBodySegments();
        if (segments != null && !segments.isEmpty()) {
            sb.append(" SEG ");
            for (int i = 0; i < segments.size(); i++) {
                Segment seg = segments.get(i);
                sb.append((int) seg.getPosition().getX())
                        .append(",")
                        .append((int) seg.getPosition().getY());
                if (i < segments.size() - 1) {
                    sb.append("|");
                }
            }
        }
        return sb.toString();
    }

    /**
     * Serializza un oggetto Bot.
     * Formato:
     * BOT <botId> <x> <y> [SEG <seg1_x>,<seg1_y>|<seg2_x>,<seg2_y>|...]
     */
    public static String serializeBot(Bot b) {
        StringBuilder sb = new StringBuilder();
        sb.append("BOT ")
                .append(b.getId()).append(" ")
                .append(b.getPosition().getX()).append(" ")
                .append(b.getPosition().getY());

        List<Segment> segments = b.getBodySegments();
        if (segments != null && !segments.isEmpty()) {
            sb.append(" SEG ");
            for (int i = 0; i < segments.size(); i++) {
                Segment seg = segments.get(i);
                sb.append((int) seg.getPosition().getX())
                        .append(",")
                        .append((int) seg.getPosition().getY());
                if (i < segments.size() - 1) {
                    sb.append("|");
                }
            }
        }
        return sb.toString();
    }

    /**
     * Serializza un oggetto Food.
     * Formato:
     * FOOD <foodId> <x> <y>
     */
    public static String serializeFood(Food f) {
        StringBuilder sb = new StringBuilder();
        sb.append("FOOD ")
                .append(f.getIdentifier()).append(" ")
                .append(f.getPosition().getX()).append(" ")
                .append(f.getPosition().getY());
        return sb.toString();
    }

    /**
     * Deserializza una stringa contenente i dati di un Player.
     * Formato atteso:
     * PLAYER <playerId> <x> <y> [SEG <seg1_x>,<seg1_y>|<seg2_x>,<seg2_y>|...]
     */
    public static List<Player> deserializePlayers(String segment) {
        List<Player> players = new ArrayList<>();
        if (segment == null || segment.trim().isEmpty()) return players;

        try {
            segment = segment.trim();
            if (segment.startsWith("&")) segment = segment.substring(1).trim();
            if (segment.endsWith(";")) segment = segment.substring(0, segment.length() - 1).trim();

            String[] playerSegments = segment.split("&PLAYER");
            for (String playerSegment : playerSegments) {
                playerSegment = playerSegment.trim();
                if (playerSegment.isEmpty()) continue;

                String[] mainParts = playerSegment.split(" SEG ");
                String[] parts = mainParts[0].split(" ");
                if (parts.length < 4) continue;

                String id = parts[1];
                double x = Double.parseDouble(parts[2]);
                double y = Double.parseDouble(parts[3]);

                Player p = new Player(id);
                p.setPosition(new Vector2D(x, y));

                if (mainParts.length > 1 && !mainParts[1].trim().isEmpty()) {
                    String[] segTokens = mainParts[1].trim().split("\\|");
                    for (String token : segTokens) {
                        String[] coords = token.split(",");
                        if (coords.length == 2) {
                            int segX = Integer.parseInt(coords[0].trim());
                            int segY = Integer.parseInt(coords[1].trim());
                            p.addBodySegment(new Segment(new Vector2D(segX, segY), 17));
                        }
                    }
                }
                players.add(p);
            }
        } catch (Exception e) {
            System.err.println("Errore nella deserializzazione dei PLAYER: " + e.getMessage());
        }
        return players;
    }


    /**
     * Deserializza una stringa contenente i dati di un Bot.
     * Formato atteso:
     * BOT <botId> <x> <y> [SEG <seg1_x>,<seg1_y>|<seg2_x>,<seg2_y>|...]
     */
    public static List<Bot> deserializeBots(String segment) {
        List<Bot> bots = new ArrayList<>();
        try {
            segment = segment.trim();

            // Suddivide in più segmenti se ci sono più BOT
            String[] botSegments = segment.split(";");

            for (String botSegment : botSegments) {
                botSegment = botSegment.trim();
                if (botSegment.isEmpty()) continue;

                if (botSegment.startsWith("&")) {
                    botSegment = botSegment.substring(1).trim();
                }

                String[] mainParts = botSegment.split(" SEG ");
                String[] parts = mainParts[0].split(" ");

                if (parts.length < 4) {
                    throw new IllegalArgumentException("Segmento BOT non valido: " + botSegment);
                }

                String id = parts[1];
                double x = parseDoubleSafe(parts[2], "Coordinata X non valida");
                double y = parseDoubleSafe(parts[3], "Coordinata Y non valida");

                Bot b = new Bot(id, new Vector2D(x, y));

                if (mainParts.length > 1 && !mainParts[1].trim().isEmpty()) {
                    String[] segTokens = mainParts[1].trim().split("\\|");
                    for (String token : segTokens) {
                        token = token.trim();
                        if (token.isEmpty()) continue;

                        String[] coords = token.split(",");
                        if (coords.length == 2) {
                            double segX = parseDoubleSafe(coords[0], "Segmento X non valido");
                            double segY = parseDoubleSafe(coords[1], "Segmento Y non valido");

                            b.addBodySegment(new Segment(new Vector2D(segX, segY), 17));
                        }
                    }
                }
                bots.add(b);
            }
        } catch (Exception e) {
            System.err.println("Errore nella deserializzazione di BOT: " + segment + " -> " + e.getMessage());
        }
        return bots;
    }

    public static List<Food> deserializeFoods(String segment) {
        List<Food> foods = new ArrayList<>();
        try {
            segment = segment.trim();

            // Suddivide in più segmenti se ci sono più FOOD
            String[] foodSegments = segment.split(";");

            for (String foodSegment : foodSegments) {
                foodSegment = foodSegment.trim();
                if (foodSegment.isEmpty()) continue;

                if (foodSegment.startsWith("&")) {
                    foodSegment = foodSegment.substring(1).trim();
                }

                String[] parts = foodSegment.split(" ");
                if (parts.length < 4) {
                    throw new IllegalArgumentException("Segmento FOOD non valido: " + foodSegment);
                }

                String foodId = parts[1];
                double x = parseDoubleSafe(parts[2], "Coordinata X non valida");
                double y = parseDoubleSafe(parts[3], "Coordinata Y non valida");

                foods.add(new Food(new Vector2D(x, y), 10, foodId));
            }
        } catch (Exception e) {
            System.err.println("Errore nella deserializzazione di FOOD: " + segment + " -> " + e.getMessage());
        }
        return foods;
    }


    // Metodo di supporto per il parsing sicuro dei numeri
    private static double parseDoubleSafe(String value, String errorMessage) throws NumberFormatException {
        try {
            return Double.parseDouble(value.trim());
        } catch (NumberFormatException e) {
            throw new NumberFormatException(errorMessage + ": " + value);
        }
    }



}
