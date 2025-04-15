package model;

import java.util.ArrayList;
/**
 * Classe per la gestione della logica dei bot per il gioco singleplayer
 */
public class AISnake extends Snake{

    /**
     * Coordinate della destinazione del bot
     */
    private Pair lookingTo;

    private static int BOT_SPEED = 5;

    public AISnake(int x, int y, Direction direction, ArrayList<Food> foods) {
        super(x, y, direction);
        lookingTo=closestFood(foods);
    }

    /**
     * Metodo che gestisce il movimento del bot.
     * Per ora il bot si muove verso il cibo più vicino
     *
     * @param foods ArrayList di tutti i cibi presenti nella partita
     */
    public void moveAI(ArrayList<Food> foods){
        SnakeBodyPart testa = getBody().get(0);

        lookingTo = closestFood(foods); //Trovo il cibo più vicino

        for (int i = getBody().size() - 1; i > 0; i--) {
            getBody().get(i).setX(getBody().get(i - 1).getX());
            getBody().get(i).setY(getBody().get(i - 1).getY());
        }

        //Imposto l'angolo che lo snake deve fare per muoversi verso il cibo individuato
        double angle = Math.atan2( lookingTo.getY()- testa.getY(), lookingTo.getX()-testa.getX() );

        //Muovo la testa del bot verso l'angolo individuato
        int newX = (int) (getBody().get(0).getX() + BOT_SPEED * Math.cos(angle));
        int newY = (int) (getBody().get(0).getY() + BOT_SPEED * Math.sin(angle));
        getBody().get(0).setX(newX);
        getBody().get(0).setY(newY);
    }

    /**
     * Metodo che restituisce il cibo più vicino al bot snake
     * @param foods ArrayList di tutti i cibi presenti nella partita
     * @return istanza di Pair con le coordinate del cibo più vicino
     */
    private Pair closestFood(ArrayList<Food> foods) {
        Food closestFood = foods.get(0);
        SnakeBodyPart testa = getBody().get(0);
        int closestFoodDistance =distance(closestFood.getX(),testa.getX(),closestFood.getY(), testa.getY());

        //Prendo i cibi due a due, vedo guale dei due è più vicino e mi tengo da parte quello più vicino
        for(int i =0;i<foods.size()-2;i++){
            int foodDistance1 =distance(foods.get(i).getX(),testa.getX(),foods.get(i).getY(), testa.getY());
            int foodDistance2=distance(foods.get(i+1).getX(),testa.getX(),foods.get(i+1).getY(), testa.getY());
            if(foodDistance1>=foodDistance2&&foodDistance2<closestFoodDistance){
                closestFood=foods.get(i+1);
                closestFoodDistance = foodDistance2;
            }
            else if(foodDistance1<closestFoodDistance){
                closestFood=foods.get(i);
                closestFoodDistance = foodDistance1;
            }
        }
        return new Pair(closestFood.getX(),closestFood.getY());
    }

    /**
     * Getter di lookingTo
     */
    public Pair getLookingTo(){
        return lookingTo;
    }
}