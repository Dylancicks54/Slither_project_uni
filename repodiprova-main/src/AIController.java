import java.util.ArrayList;
import java.util.List;

public class AIController {
    private Bot bot;
    private List<Entity> nearbyEntities; // Lista delle entità vicine

    // Modifica del costruttore per inizializzare la lista
    public AIController(Bot bot,List<Entity> nearbyEntities) {
        this.bot = bot;
        this.nearbyEntities = nearbyEntities;  // Inizializziamo la lista
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
            if (distance < 9000) {  // Raggio di rilevamento maggiore
                nearbyEntities.add(food);
            }
        }

        // Controlla ogni bot
        for (Entity otherBot : bot.getGameState().getBots()) {
            if (bot != otherBot) {
                double distance = bot.getPosition().distanceTo(otherBot.getPosition());
                if (distance < 9000) {
                    nearbyEntities.add(otherBot);
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
        Food closestFood = null;
        double minDistance = Double.MAX_VALUE;

        for (Entity entity : nearbyEntities) {
            if (entity instanceof Food) {
                Food food = (Food) entity;

                // Se il cibo è già stato reclamato da un altro bot, lo ignoriamo
                if (food.getClaimedBy() != null && food.getClaimedBy() != bot) continue;

                double distance = bot.getPosition().distanceTo(food.getPosition());
                if (distance < minDistance) {
                    minDistance = distance;
                    closestFood = food;
                }
            }
        }

        if (closestFood != null) {
            closestFood.setClaimedBy(bot); // Ora il bot reclama il cibo
            bot.moveTowards(closestFood.getPosition());

            // Controlliamo se il bot ha raggiunto il cibo
            if (bot.collidesWith(closestFood)) {
                bot.grow();
                bot.getGameState().removeFood(closestFood);
            }
        }
    }





    // Metodo per evitare una collisione
    public void avoidCollision() {
        Vector2D oppositeDirection = bot.getVelocity().scale(-1);
        bot.setVelocity(oppositeDirection);
    }
}