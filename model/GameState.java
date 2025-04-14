package model;

import controller.*;

import java.util.*;
import java.util.List;
import java.util.Timer;

/**
 * Modello che gestisce la logica della partita
 */
public class GameState {
    private Snake snake;
    private GameController controller;

    //Elenco elementi di gioco
    private List<AISnake> aiSnakes;
    private List<AISnake> snakes;
    private ArrayList<Food> foods;

    //Timer per l'esecuzione dei thread dei vari processi della partita
    private Timer gameTimer;
    private Timer foodTimer;
    private Timer countdownTimer;

    //Contatori
    private int remainingTime;
    //private int countdownSeconds;
    private int score;

    //@TODO valutare se tenere il magnetize visto che non funziona
    //Costanti
    private static final int ATTRACT_DISTANCE = 50;
    private static final int ATTRACT_SPEED = 2;

    public static final int BORDER_X = 1550;
    public static final int BORDER_Y = 1550;
    public static final int OFFSET_MAP_X = 50;
    public static final int OFFSET_MAP_Y = 50;
    private static final int MATCH_DURATION = 60;
    private static final int MAX_NUMBER_FOOD = 100;
    private static final int MAX_NUMBER_BOT = 5;

    public GameState(GameController controller){
        this.controller=controller;

        //Inizializzo i contatori
        this.score = 0;
        this.remainingTime = MATCH_DURATION * 40;

        //Inizializzo i timer
        this.gameTimer=new Timer();
        this.foodTimer=new Timer();
        this.countdownTimer = new Timer();

        //Inizializzo gli elenchi
        this.foods = new ArrayList<>();
        this.aiSnakes=new ArrayList<>();

        //Spawno in posti randomici gli oggetti di gioco
        Random random = new Random();
        this.snake =new Snake(random.nextInt(getSpawnAreaX()), random.nextInt(getSpawnAreaY()), Direction.RIGHT);

        for (int i = 0; i < MAX_NUMBER_FOOD; i++) {
            generateFood(getSpawnAreaX(),getSpawnAreaY());
        }
        for (int i = 0; i < MAX_NUMBER_BOT; i++) {
            aiSnakes.add(new AISnake(random.nextInt(getSpawnAreaX()), random.nextInt(getSpawnAreaX()),Direction.DOWN, foods));
        }

        //Inizio il loop di gioco
        gameStart();
    }

    /**
     * Metodo che inizia il loop di gioco.
     * Il metodo esegue 3 thread "in parallelo" che sono responsabili per:
     * - la logica di gioco (collissione tra oggetti di gioco)
     * - gestione della durata della partita
     * - respawn del cibo
     */
    public void gameStart() {
        //THREAD CON LA LOGICA DI GIOCO
        gameTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                //Muovo il player
                snake.move(snake.getMouseX(), snake.getMouseY());

                //Muovo i bot
                //Lavoro su una copia di foods per evitare il problema della concorrenza
                for (AISnake aiSnake : aiSnakes) {
                    aiSnake.moveAI((ArrayList<Food>) foods.clone());
                }

                // Controlla le collisioni:
                // 1. Collisioni con il cibo
                checkFoodCollision();

                // 2. Se la testa del player collide con un segmento di un bot, termina il gioco
                if (checkPlayerCollisionWithBots()) {
                    gameTimer.cancel();
                    foodTimer.cancel();
                    countdownTimer.cancel();
                    controller.getGameView().showLoseDialog();
                    Timer returnToMenuTimer = new Timer();
                    returnToMenuTimer.schedule(new TimerTask() {
                        @Override
                        public void run() {
                            controller.getGameView().showMenu();
                            controller.getGameView().closeCurrentGameWindow();
                        }
                    }, 1000);
                    return;
                }

                // 3. Se la testa di un bot collide con un segmento del player, quel bot "muore" (viene respawnato)
                checkBotCollisionWithPlayer();

                // 4. Se due bot collidono tra loro, il bot coinvolto viene respawnato
                checkAIBotCollisions();

                // 5. Controllo se si scontra con i bordi della mappa
                if (checkBodyCollision()) {
                    gameTimer.cancel();
                    foodTimer.cancel();
                    countdownTimer.cancel();
                    controller.getGameView().showLoseDialog();
                    Timer returnToMenuTimer = new Timer();
                    returnToMenuTimer.schedule(new TimerTask() {
                        @Override
                        public void run() {
                            controller.getGameView().showMenu();
                            controller.getGameView().closeCurrentGameWindow();
                        }
                    }, 1000);
                    return;
                }
            }
        }, 0, 25);  // Aggiorna ogni 25ms

        //THREAD TIMER DELLA PARTITA
        countdownTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                remainingTime--;
                //Quando il tempo è scaduto, la partita finisce
                if (remainingTime <= 0) {
                    gameTimer.cancel();
                    foodTimer.cancel();
                    countdownTimer.cancel();
                    controller.getGameView().showTimeUpDialog();
                    Timer returnToMenuTimer = new Timer();
                    returnToMenuTimer.schedule(new TimerTask() {
                        @Override
                        public void run() {
                            controller.getGameView().showMenu();
                            controller.getGameView().closeCurrentGameWindow();
                        }
                    }, 1000);
                } else {
                    controller.getGameView().updateTimerLabel();
                }
            }
        }, 0, 25);

        //THREAD SPAWN CIBO
        foodTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                generateFood(getSpawnAreaX(), getSpawnAreaY());
            }
        }, 0, 200);
    }

    /**
     * Metodo per lo spawn del cibo.
     * Il cibo viene generato in una coordinata causale, associato un colore casuale ed inserito nell'elenco dei cibi
     * @param mapX ascissa massima dove il cibo può comparire
     * @param mapY ordinata massima dove il cibo può comparire
     */
    public void generateFood(int mapX,int mapY) {
        Random random = new Random();

        int x = random.nextInt(mapX);
        int y = random.nextInt(mapY);
        // Prendo un numero causale tra 1 e 4 (escluso)
        int randomNumber = random.nextInt(4);

        Food food = new Food(x, y, randomNumber);
        foods.add(food);
    }

    /**
     * Metodo che gestisce la collisione con il cibo.
     * Quando uno snake (player o bot) "cattura" un cibo, aumenta la sua lunghezza ed aumenta anche il suo punteggio.
     */
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

    /**
     * Metodo che controlla la collisione del player contro il bordo della mappa.
     * Se il player si scontra con esso, muore.
     * @return true se ha toccato il bordo, false in tutti gli altri casi
     */
    public boolean checkBodyCollision(){
        //Correzione perchè il riferimento sulla vista non è al centro della testa
        //ma è nell'angolo in alto a sinistra del quadrato che inscrive la circonferenza.
        //Quindi per il lato destro e basso devo considerare anche la grandezza del pallino che costituisce il corpo dello snake

        return snake.getBody().get(0).getX() <= (0 - Snake.SEGMENT_SIZE) || //LATO SINISTRO
                snake.getBody().get(0).getX() >= (BORDER_X - Snake.SEGMENT_SIZE) || //lATO DESTRO
                snake.getBody().get(0).getY() >= (BORDER_Y - Snake.SEGMENT_SIZE) || //LATO BASSO
                snake.getBody().get(0).getY() <= (0 - Snake.SEGMENT_SIZE); //LATO ALTO
    }

    /**
     * Metodo che gestisce la collisione del giocatore con un bot.
     *
     * Quando avviene l'urto, il player muore ed al suo posto compare del cibo.
     *
     * @return true se il player colpisce il corpo di un bot, false in tutti gli altri casi
     */
    public boolean checkPlayerCollisionWithBots() {
        int threshold = Snake.SEGMENT_SIZE;

        int playerX = snake.getBody().get(0).getX();
        int playerY = snake.getBody().get(0).getY();

        for (AISnake aiSnake : aiSnakes) {
            for (SnakeBodyPart segment : aiSnake.getBody()) {
                int segX = segment.getX();
                int segY = segment.getY();
                //Controllo collisione
                if (Math.abs(playerX - segX) < threshold && Math.abs(playerY - segY) < threshold) {
                    //Se si scontra, lo snake viene sostituico con del cibo
                    generateFoodFromSnake(snake);
                    return true;
                }
            }
        }
        return false;
    }


    /**
     * Metodo che gestisce la collisione del bot con il giocatore.
     *
     * Quando avviene l'urto, il bot muore ed al suo posto compare del cibo.
     *
     * Inoltre, il bot viene prontamente sostituito con uno nuovo
     */
    public void checkBotCollisionWithPlayer() {
        int threshold = Snake.SEGMENT_SIZE;
        List<AISnake> botsToRespawn = new ArrayList<>();

        // Itera su una copia della lista per evitare la ConcurrentModificationException
        for (AISnake aiSnake : new ArrayList<>(aiSnakes)) {
            int botHeadX = aiSnake.getBody().get(0).getX();
            int botHeadY = aiSnake.getBody().get(0).getY();
            for (Object segmentObj : snake.getBody()) {
                SnakeBodyPart segment = (SnakeBodyPart) segmentObj;
                int segX = segment.getX();
                int segY = segment.getY();
                //Controllo collisione
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

    /**
     * Metodo che gestisce la collisione tra bot.
     * Quando avviene l'urto (testa di uno con il corpo dell'altro), quello che ha colpito con la testa muore e spawna cibo
     *
     */
    public void checkAIBotCollisions() {
        int threshold = Snake.SEGMENT_SIZE;
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
                for (SnakeBodyPart segmentB : botB.getBody()) {
                    int segX = segmentB.getX();
                    int segY = segmentB.getY();
                    //Controllo collisione
                    if (Math.abs(botAHeadX - segX) < threshold && Math.abs(botAHeadY - segY) < threshold) {
                        if (!botsToRespawn.contains(botA)) {
                            botsToRespawn.add(botA);
                            generateFoodFromSnake(botA);
                        }
                    }
                }
                // Verifica se la testa di botB colpisce un segmento di botA
                for (SnakeBodyPart segmentA : botA.getBody()) {

                    int segX = segmentA.getX();
                    int segY = segmentA.getY();
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

    /**
     * Metodo che gestisce il respawn dei bot.
     * I bot vengono respownati in una posizione casuale dentro l'area di gioco
     * @return AISnake l'istanza di bot da inserire nel mondo di gioco
     */
    private AISnake spawnAISnake() {
        Random random = new Random();

        int spawnX, spawnY;
        boolean validPosition;

        do {
            spawnX = random.nextInt(getSpawnAreaX());
            spawnY = random.nextInt(getSpawnAreaY());
            validPosition = true;

            // Controllo se la posizione nuova collisione con il giocatore
            if (checkCollisionWithSnake(snake, spawnX, spawnY)) {
                validPosition = false;
            }

            // Controllo se la posizione nuova collisione con altri bot
            for (AISnake aiSnake : aiSnakes) {
                if (checkCollisionWithSnake(aiSnake, spawnX, spawnY)) {
                    validPosition = false;
                    break;
                }
            }

        } while (!validPosition); // Continua a generare finché non trovi una posizione libera

        return new AISnake(spawnX, spawnY,Direction.DOWN, foods);
    }

    /**
     * Metodo helper per controllare se le coordinate in input (x,y) fanno parte di uno snake
     * @param snake Snake preso in cosiderazione
     * @param x ascissa in esame
     * @param y ordinata in esame
     * @return true se le coordinate fanno parte dello snake, false tin tutti gli altri casi
     */
    private boolean checkCollisionWithSnake(Snake snake, int x, int y) {
        for (SnakeBodyPart part : snake.getBody()) {
            if (Math.abs(part.getX() - x) < Snake.SEGMENT_SIZE && Math.abs(part.getY() - y) < Snake.SEGMENT_SIZE) {
                return true; // Collisione trovata
            }
        }
        return false; // Nessuna collisione
    }

    /**
     * Metodo per la generazione di cibo alla morte di uno snake
     * @param snake snake da scomporre in cibo
     */
    private void generateFoodFromSnake(Snake snake) {
        for (SnakeBodyPart part : snake.getBody()) {
            int x = part.getX();
            int y = part.getY();
            int foodType = new Random().nextInt(4); // Scegliamo un tipo di cibo casuale
            foods.add(new Food(x, y, foodType));
        }
    }

    /**
     * Restitusce la lunghezza dell'area di gioco
     */
    public static int getSpawnAreaX(){
        return BORDER_X - OFFSET_MAP_X;
    }

    /**
     * Restituisce l'altrezza dell'area di gioco
     * @return
     */
    public static int getSpawnAreaY(){
        return BORDER_Y - OFFSET_MAP_Y;
    }

    /**
     * Metodo che gestisce la chiusura della sessione di gioco.
     * Chiude tutti i timer ed apre il menu della prelobby
     */
    public void resetGame() {
        gameTimer.cancel();
        foodTimer.cancel();
        countdownTimer.cancel();

        //Faccio comparire il menu
        controller.getGameView().showMenu();
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
     //si divide per 40 per mantenere uniforme l'unità temporale confrontata con l'update dello stato di gioco
    public int getRemainingTime() {
        return (int)remainingTime/40;
    }
    public int getScore() {
        return score;
    }
}
