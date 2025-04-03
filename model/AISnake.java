package model;

import java.util.ArrayList;
/**
 * this class extend snake and is used to make a bot snake to play whit
 */
public class AISnake extends Snake{
    private Pair lookingTo;
    /**
     * constructor initialize a snake and the first point where the snake is looking to*/
    //VECCHIO COSTRUTTORE (TENUTO PER SICUREZZA) - DA CANCELLARE SE RISULTA STABILE IL CODICE NUOVO
    /*
    public AISnake(int position, Direction direction,ArrayList<Food> foods) {
        super(position, direction);
        lookingTo=closestFood(foods);
    }
     */

    public AISnake(int x, int y, Direction direction,ArrayList<Food> foods) {
        super(x, y, direction);
        lookingTo=closestFood(foods);
    }

    /**
     *this method moves the bot snake towards the closest food
     * @param foods ArrayList of all foods in the game
     */
    public void moveAI(ArrayList<Food> foods){
        SnakeBodyPart testa = getBody().get(0);
        lookingTo = closestFood(foods);//finds the closest food
        for (int i = getBody().size() - 1; i > 0; i--) {
            getBody().get(i).setX(getBody().get(i - 1).getX());
            getBody().get(i).setY(getBody().get(i - 1).getY());
        }

        //set the angle at which the snake need to move to go straight for the food
        double angle = Math.atan2( lookingTo.getY()- testa.getY(), lookingTo.getX()-testa.getX() );

        //moves the head of the bot-snake
        int newX = (int) (getBody().get(0).getX() + 5 * Math.cos(angle));
        int newY = (int) (getBody().get(0).getY() + 5 * Math.sin(angle));
        getBody().get(0).setX(newX);
        getBody().get(0).setY(newY);
    }

    /**
     * this method return the closest food to the bot snake
     * @param foods ArrayList of all foods in the game
     * @return Pair object with the coordinate of the closest food
     */
    private Pair closestFood(ArrayList<Food> foods) {
        Food closestFood = foods.get(0);
        SnakeBodyPart testa = getBody().get(0);
        int closestFoodDistance =distance(closestFood.getX(),testa.getX(),closestFood.getY(), testa.getY());

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
     * this method return the point where the snake is looking
     */

    public Pair getLookingTo(){
        return lookingTo;
    }
}