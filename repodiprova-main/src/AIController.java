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
        nearbyEntities.clear();

        // Copia la lista degli alimenti per evitare la modifica concorrente
        List<Entity> foodList = new ArrayList<>(bot.getGameState().getFoodItems());
        for (Entity food : foodList) {
            double distance = bot.getPosition().distanceTo(food.getPosition());
            if (distance < 2500) {
                nearbyEntities.add(food);
            }
        }

        // Copia la lista dei bot per evitare la modifica concorrente
        List<Entity> botList = new ArrayList<>(bot.getGameState().getBots());
        for (Entity otherBot : botList) {
            if (bot != otherBot) {
                double distance = bot.getPosition().distanceTo(otherBot.getPosition());
                if (distance < 2500) {
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

    public void avoidCollision() {
        Vector2D currentVelocity = bot.getVelocity();

        // Trova l'entità più vicina da evitare
        Entity closestEntityToAvoid = null;
        double minDistance = Double.MAX_VALUE;

        for (Entity entity : nearbyEntities) {
            double distance = bot.getPosition().distanceTo(entity.getPosition());
            if (distance < minDistance) {
                minDistance = distance;
                closestEntityToAvoid = entity;
            }
        }

        if (closestEntityToAvoid != null) {
            // Calcola una direzione per evitare la collisione
            Vector2D directionToAvoid = bot.getPosition().subtract(closestEntityToAvoid.getPosition());
            Vector2D newVelocity = currentVelocity.add(directionToAvoid.normalize().scale(bot.getSpeed()));

            // Imposta la nuova velocità (evitando la collisione)
            bot.setVelocity(newVelocity.normalize().scale(bot.getSpeed()));
        }
    }




//    // Metodo per evitare una collisione
//    public void avoidCollision() {
//        Vector2D currentVelocity = bot.getVelocity();
//        double avoidanceAngle = Math.toRadians(45); // Angolo di deviazione
//
//        // Ruota la direzione invece di invertirla completamente
//        double newX = currentVelocity.x * Math.cos(avoidanceAngle) - currentVelocity.y * Math.sin(avoidanceAngle);
//        double newY = currentVelocity.x * Math.sin(avoidanceAngle) + currentVelocity.y * Math.cos(avoidanceAngle);
//
//        bot.setVelocity(new Vector2D(newX, newY).normalize().scale(bot.getSpeed()));
//    }

}