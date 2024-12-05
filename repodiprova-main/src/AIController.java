import java.util.ArrayList;
import java.util.List;

public class AIController {
    private Bot bot;
    private List<Entity> nearbyEntities; // Lista delle entità vicine

    // Modifica del costruttore per inizializzare la lista
    public AIController(Bot bot) {
        this.bot = bot;
        this.nearbyEntities = new ArrayList<>();  // Inizializziamo la lista
    }

    // Metodo che decide il comportamento del bot
    public void decideBehavior() {
        // Popola nearbyEntities (aggiungi logica per trovare le entità vicine, come cibo o altri bot)
        updateNearbyEntities();

        // Controlla se c'è una collisione imminente
        if (isCollisionImminent()) {
            bot.currentState = State.AVOIDING_COLLISION;
        } else {
            bot.currentState = State.MOVING_TO_FOOD;
        }
    }

    // Metodo per aggiornare la lista delle entità vicine
    private void updateNearbyEntities() {
        nearbyEntities.clear(); // Pulisce la lista

        // Controlla ogni cibo
        for (Entity food : bot.getGameState().getFoodItems()) {
            double distance = bot.getPosition().distanceTo(food.getPosition());
            System.out.println("Food position: " + food.getPosition() + ", Bot position: " + bot.getPosition() + ", Distance: " + distance);
            if (distance < 50000) {  // Raggio di rilevamento maggiore
                nearbyEntities.add(food);
                System.out.println("Added food to nearbyEntities: " + food.getPosition());
            }
        }

        // Controlla ogni bot
        for (Entity otherBot : bot.getGameState().getBots()) {
            if (bot != otherBot) {
                double distance = bot.getPosition().distanceTo(otherBot.getPosition());
                System.out.println("Other bot position: " + otherBot.getPosition() + ", Distance: " + distance);
                if (distance < 30000) {
                    nearbyEntities.add(otherBot);
                    System.out.println("Added bot to nearbyEntities: " + otherBot.getPosition());
                }
            }
        }


    }




    // Metodo per verificare se una collisione è imminente
    private boolean isCollisionImminent() {
        for (Entity entity : nearbyEntities) {
            if (bot.collidesWith(entity)) {
                return true;
            }
        }
        return false;
    }

    // Metodo per cercare il cibo
    public void seekFood() {
        Entity closestFood = null;
        double minDistance = Double.MAX_VALUE;

        for (Entity entity : nearbyEntities) {
            if (entity instanceof Food) {
                Food food = (Food) entity;
                if (food.getClaimedBy() == null || food.getClaimedBy() == bot) {
                    double distance = bot.getPosition().distanceTo(food.getPosition());
                    if (distance < minDistance) {
                        minDistance = distance;
                        closestFood = food;
                    }
                }
            }
        }

        if (closestFood != null) {
            ((Food) closestFood).setClaimedBy(bot); // Rivendica il cibo
            bot.moveTowards(closestFood.getPosition());
        }
    }




    // Metodo per evitare una collisione
    public void avoidCollision() {
        Vector2D oppositeDirection = bot.getVelocity().scale(-1);
        bot.setVelocity(oppositeDirection);
    }
}
