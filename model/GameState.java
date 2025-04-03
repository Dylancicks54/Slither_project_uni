package model;

import controller.GameController;
import view.GameWindow;

import java.awt.*;
import java.util.*;
import java.util.List;
import java.util.Timer;
public class GameState {
    private Snake snake;
    private List<AISnake> aiSnakes;
    private List<AISnake> snakes;
    private Timer gameTimer;
    private Timer foodTimer;
    private ArrayList<Food> foods;
    private GameController controller;
    private Timer countdownTimer;  // Nouveau chronomètre pour le mode solo
    private int remainingTime;
    private int countdownSeconds;
    private int score;
    private static final double ATTRACT_DISTANCE = 50.0;
    private static final double ATTRACT_SPEED = 2.0;

    public GameState(GameController controller){
        this.snake=new Snake(110,Direction.RIGHT);
        this.score = 0;
        this.gameTimer=new Timer();
        this.foodTimer=new Timer();
        this.countdownSeconds = 60;
        this.remainingTime = countdownSeconds *40;
        this.countdownTimer = new Timer();
        this.foods = new ArrayList<>();
        this.controller=controller;
        this.aiSnakes=new ArrayList<>();


            for (int i = 0; i < 100; i++) {
                generateFood(1500,1500);
            }
            for (int i = 0; i < 5; i++) {
                aiSnakes.add(new AISnake((int)(Math.random() * GameWindow.getWindowWidth()), Direction.DOWN, foods));
            }

        gameStart();
    }

    public void gameStart() {
        gameTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                // Muove il player e i bot
                snake.move(snake.getMouseX(), snake.getMouseY());
                for (AISnake aiSnake : aiSnakes) {
                    aiSnake.moveAI((ArrayList<Food>) foods.clone());
                }

                // Applica l'effetto magnete e controlla il cibo
                applyMagnetEffect();
                checkFoodCollision();

                // Controlla le collisioni:
                // 1. Se la testa del player collide con un segmento di un bot, termina il gioco
                if (checkPlayerCollisionWithBots()) {
                    gameTimer.cancel();
                    foodTimer.cancel();
                    countdownTimer.cancel();
                    controller.getGv().showLoseDialog();
                    Timer returnToMenuTimer = new Timer();
                    returnToMenuTimer.schedule(new TimerTask() {
                        @Override
                        public void run() {
                            controller.getGv().showMenu();
                            controller.getGv().closeCurrentGameWindow();
                        }
                    }, 1000);
                    return;
                }

                // 2. Se la testa di un bot collide con un segmento del player, quel bot "muore" (viene respawnato)
                checkBotCollisionWithPlayer();

                // 3. Se due bot collidono tra loro, il bot coinvolto viene respawnato
                checkAIBotCollisions();

                // Controlla i bordi del campo
                if (checkBodyCollision()) {
                    gameTimer.cancel();
                    foodTimer.cancel();
                    countdownTimer.cancel();
                    controller.getGv().showLoseDialog();
                    Timer returnToMenuTimer = new Timer();
                    returnToMenuTimer.schedule(new TimerTask() {
                        @Override
                        public void run() {
                            controller.getGv().showMenu();
                            controller.getGv().closeCurrentGameWindow();
                        }
                    }, 1000);
                    return;
                }
            }
        }, 0, 25);  // Aggiorna ogni 25ms

        countdownTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                remainingTime--;
                if (remainingTime <= 0) {
                    gameTimer.cancel();
                    foodTimer.cancel();
                    countdownTimer.cancel();
                    controller.getGv().showTimeUpDialog();
                    Timer returnToMenuTimer = new Timer();
                    returnToMenuTimer.schedule(new TimerTask() {
                        @Override
                        public void run() {
                            controller.getGv().showMenu();
                            controller.getGv().closeCurrentGameWindow();
                        }
                    }, 1000);
                } else {
                    controller.getGv().updateTimerLabel();
                }
            }
        }, 0, 25);

        foodTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                generateFood(1500, 1500);
            }
        }, 0, 200);
    }



    public void generateFood(int a,int b) {
        Random random = new Random();
        int x = random.nextInt(a);
        int y = random.nextInt(b);
        Color color = Color.RED; // Couleur de la nourriture
        int size = 10; // Taille de la nourriture
        // Générer un nombre aléatoire entre 0 (inclus) et 4 (exclus)
        int randomNumber = random.nextInt(4);
        Food food = new Food(x, y, randomNumber, size);
        foods.add(food);
    }
    public void checkFoodCollision() {
        for (int i = foods.size() - 1; i >= 0; i--) {  // Iteriamo dalla fine per evitare l'errore di IndexOutOfBounds
            Food food = foods.get(i);

            // Controlla se il giocatore mangia il cibo
            if (snake.collisionsWithFood(food)) {
                snake.grow();  // Il giocatore cresce
                foods.remove(i);  // Rimuovi il cibo dalla lista
                score += 1;  // Aumenta il punteggio
            } else {
                // Controlla se uno dei bot mangia il cibo
                for (AISnake aiSnake : aiSnakes) {
                    if (aiSnake.collisionsWithFood(food)) {
                        aiSnake.grow();  // Il bot cresce
                        foods.remove(i);  // Rimuovi il cibo dalla lista
                        break;  // Una volta che un bot mangia il cibo, non dobbiamo continuare a cercare
                    }
                }
            }
        }
    }

    public boolean checkBodyCollision(){
        return snake.getBody().get(0).getX() <= 0 || snake.getBody().get(0).getX() >= 1533 || snake.getBody().get(0).getY() >= 1533 || snake.getBody().get(0).getY() <= -100;
    }

    private void applyMagnetEffect() {
        List<Food> foodToRemove = new ArrayList<>();
        List<Food> foodsCopy;

        // Creiamo una copia della lista foods per evitare modifiche concorrenti
        synchronized (foods) {
            foodsCopy = new ArrayList<>(foods);
        }

        for (Food food : foodsCopy) {
            Snake closestSnake = null;
            double closestDistance = ATTRACT_DISTANCE;

            // Controlliamo i giocatori
            double distance1 = Snake.distance(snake.getBody().getFirst().getX(), snake.getBody().getFirst().getY(), food.getX(), food.getY());
            if (distance1 < closestDistance) {
                closestDistance = distance1;
                closestSnake = snake;
            }

            // Controlliamo i bot
            for (AISnake aiSnake : aiSnakes) {
                double distance2 = Snake.distance(aiSnake.getBody().getFirst().getX(), aiSnake.getBody().getFirst().getY(), food.getX(), food.getY());
                if (distance2 < closestDistance) {
                    closestDistance = distance2;
                    closestSnake = aiSnake;
                }
            }

            // Se abbiamo trovato un serpente vicino al cibo
            if (closestSnake != null) {
                if (closestSnake.collisionsWithFood(food)) {
                    closestSnake.grow();  // Il metodo grow() dovrebbe esistere sia in Snake che in AISnake
                    foodToRemove.add(food);
                } else {
                    moveFoodTowards(food, closestSnake);
                }
            }
        }

        // Rimuoviamo il cibo mangiato in modo sincronizzato
        synchronized (foods) {
            foods.removeAll(foodToRemove);
        }
    }

    private void moveFoodTowards(Food food, Snake snake) {
        int foodPosX = food.getX();
        int foodPosY = food.getY();

        double dx = snake.getBody().getFirst().getX() - foodPosX;
        double dy = snake.getBody().getFirst().getY() - foodPosY;
        double distance = Math.sqrt(dx * dx + dy * dy);

        if (distance > 0) {
            int moveX = (int)((dx / distance) * ATTRACT_SPEED);
            int moveY = (int)((dy / distance) * ATTRACT_SPEED);
            food.setX(foodPosX + moveX);
            food.setY(foodPosY + moveY);
        }
    }
    // Se la testa del giocatore collide con un segmento di un bot, restituisce true
    public boolean checkPlayerCollisionWithBots() {
        int threshold = 10; // Soglia in pixel (modifica in base alla dimensione dei segmenti)
        int playerX = snake.getBody().get(0).getX();
        int playerY = snake.getBody().get(0).getY();
        for (AISnake aiSnake : aiSnakes) {
            for (Object segmentObj : aiSnake.getBody()) {
                SnakeBodyPart segment = (SnakeBodyPart) segmentObj;
                int segX = segment.getX();
                int segY = segment.getY();
                if (Math.abs(playerX - segX) < threshold && Math.abs(playerY - segY) < threshold) {
                    generateFoodFromSnake(snake);
                    return true;
                }
            }
        }
        return false;
    }



    // Se la testa di un bot collide con un segmento del giocatore, il bot viene respawnato
    public void checkBotCollisionWithPlayer() {
        int threshold = 10;
        List<AISnake> botsToRespawn = new ArrayList<>();

        // Itera su una copia della lista per evitare la ConcurrentModificationException
        for (AISnake aiSnake : new ArrayList<>(aiSnakes)) {
            int botHeadX = aiSnake.getBody().get(0).getX();
            int botHeadY = aiSnake.getBody().get(0).getY();
            for (Object segmentObj : snake.getBody()) {
                SnakeBodyPart segment = (SnakeBodyPart) segmentObj;
                int segX = segment.getX();
                int segY = segment.getY();
                if (Math.abs(botHeadX - segX) < threshold && Math.abs(botHeadY - segY) < threshold) {
                    botsToRespawn.add(aiSnake);
                    generateFoodFromSnake(aiSnake);
                    break;
                }
            }
        }

        // Per ogni bot che ha colliso, rimuovi dalla lista e aggiungi il nuovo bot (respawn)
        for (AISnake bot : botsToRespawn) {
            aiSnakes.remove(bot);
            AISnake newBot = spawnAISnake();
            aiSnakes.add(newBot);
        }
    }




    // Se due bot collidono tra loro, quello la cui testa tocca un segmento dell'altro viene respawnato
    public void checkAIBotCollisions() {
        int threshold = 10;
        List<AISnake> botsToRespawn = new ArrayList<>();
        for (int i = 0; i < aiSnakes.size(); i++) {
            AISnake botA = aiSnakes.get(i);
            int botAHeadX = botA.getBody().get(0).getX();
            int botAHeadY = botA.getBody().get(0).getY();
            for (int j = i + 1; j < aiSnakes.size(); j++) {
                AISnake botB = aiSnakes.get(j);
                int botBHeadX = botB.getBody().get(0).getX();
                int botBHeadY = botB.getBody().get(0).getY();
                // Verifica se la testa di botA colpisce un segmento di botB
                for (Object segmentObj : botB.getBody()) {
                    SnakeBodyPart segment = (SnakeBodyPart) segmentObj;
                    int segX = segment.getX();
                    int segY = segment.getY();
                    if (Math.abs(botAHeadX - segX) < threshold && Math.abs(botAHeadY - segY) < threshold) {
                        if (!botsToRespawn.contains(botA)) {
                            botsToRespawn.add(botA);
                            generateFoodFromSnake(botA);
                        }
                    }
                }
                // Verifica se la testa di botB colpisce un segmento di botA
                for (Object segmentObj : botA.getBody()) {
                    SnakeBodyPart segment = (SnakeBodyPart) segmentObj;
                    int segX = segment.getX();
                    int segY = segment.getY();
                    if (Math.abs(botBHeadX - segX) < threshold && Math.abs(botBHeadY - segY) < threshold) {
                        if (!botsToRespawn.contains(botB)) {
                            botsToRespawn.add(botB);
                            generateFoodFromSnake(botB);
                        }
                    }
                }
            }
        }
        for (AISnake bot : botsToRespawn) {
            aiSnakes.remove(bot);
            // Utilizza il metodo helper per creare il nuovo bot in una posizione valida
            AISnake newBot = spawnAISnake();
            aiSnakes.add(newBot);
        }
    }

    private AISnake spawnAISnake() {
        int botWidth = 20; // dimensione stimata del bot
        int windowWidth = GameWindow.getWindowWidth();
        int windowHeight = GameWindow.getWindowHeight();
        Random random = new Random();

        int spawnX, spawnY;
        boolean validPosition;

        do {
            spawnX = random.nextInt(windowWidth - botWidth);
            spawnY = random.nextInt(windowHeight - botWidth);
            validPosition = true;

            // Controllo collisione con il giocatore
            if (checkCollisionWithSnake(snake, spawnX, spawnY)) {
                validPosition = false;
            }

            // Controllo collisione con altri bot
            for (AISnake aiSnake : aiSnakes) {
                if (checkCollisionWithSnake(aiSnake, spawnX, spawnY)) {
                    validPosition = false;
                    break;
                }
            }

        } while (!validPosition); // Continua a generare finché non trovi una posizione libera

        return new AISnake(spawnX, Direction.DOWN, foods);
    }

    // Metodo helper per controllare se una posizione collide con un serpente
    private boolean checkCollisionWithSnake(Snake snake, int x, int y) {
        for (SnakeBodyPart part : snake.getBody()) {
            if (Math.abs(part.getX() - x) < 20 && Math.abs(part.getY() - y) < 20) {
                return true; // Collisione trovata
            }
        }
        return false; // Nessuna collisione
    }


    private void generateFoodFromSnake(Snake snake) {
        for (SnakeBodyPart part : snake.getBody()) {
            int x = part.getX();
            int y = part.getY();
            int foodType = new Random().nextInt(4); // Scegliamo un tipo di cibo casuale
            int size = 10;
            foods.add(new Food(x, y, foodType, size));
        }
    }





    public void resetGame() {
        gameTimer.cancel();
        foodTimer.cancel();
        countdownTimer.cancel();

        controller.getGv().showMenu();  // Affiche le menu après avoir réinitialisé le jeu
    }

    public ArrayList<Food> getFoods(){
        return this.foods;
    }
    public Snake getSnake() {
        return snake;
    }
    public List<AISnake> getAiSnakes(){
        return aiSnakes;
    }

    public int getRemainingTime() {
        return (int)remainingTime/40;
    }
    public int getScore() {
        return score;
    }
}
