package Net;

import model.*;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
/**
 * questa classe gestisce il gioco serverside, responsabile per la logica e aggiorna ogni client collegato
 */
public class GameServer {
    private volatile Map<ClientHandler,Snake> players;
    private final List<Pair> foods;
    private final Server server;

    //Costanti
    public static final int BORDER_X = 5000;
    public static final int BORDER_Y = 5000;
    private static final int OFFSET_MAP_X = 0;
    private static final int OFFSET_MAP_Y = 0;
    public static final int MAX_FOOD = 1000;


    /**
     * Costruttore.
     * Inizializza la map dei Player e la lista dei Food
     * @param server
     * */
    public GameServer(Server server){
        players= new ConcurrentHashMap<>();
        foods=new ArrayList<>();
        this.server = server;

        addFood();
    }

    /**
     * Metodo che aggiunge un giocatore al mondo di gioco
     * @param clientHandler ClientHandler connesso al client
     * @param snake istanza di Snake associata al player
     */
    public void addPlayer(ClientHandler clientHandler, Snake snake){
        players.put(clientHandler,snake);
    }

    /**
     * Metodo che gestisce il loop di gioco.
     * Gestisce la posizione del giocatore, controlla le collisioni e gestice la crescita degli snack ad esecuzione
     */
    public void update(){
        if(!players.isEmpty()){

            Iterator<Map.Entry<ClientHandler,Snake>> iterator =players.entrySet().iterator();
            Map.Entry<ClientHandler,Snake> entry=null;

            while (iterator.hasNext()){
                entry = iterator.next();

                //Controllo se il giocatore è ancora vivo
                if(!entry.getKey().isAlive()){
                    players.remove(entry.getKey(),entry.getValue());
                    continue;
                }

                // Recupera il comando corrente; se è null, usa l'ultimo movimento conosciuto
                String command = entry.getKey().getNewPos();
                if (command == null) {
                    command = entry.getKey().getLastMove();
                    // Se anche lastMove risulta null, saltiamo l'iterazione
                    if (command == null)
                        continue;
                }

                //Aggiorno la posizione
                Pair newPos = stringToPos(entry.getKey().getNewPos(),entry.getKey().getClientUserName());
                entry.getValue().move(newPos.getX(),newPos.getY());
                //CONTROLLO COLLISIONI
                checkFoodCollision(entry.getValue());
                playerCollided(entry);
                borderCollision(entry);
            }
            sendPackage();
        }
    }
    /**
     * Metodo che aggiunge il cibo nel mondo
     */
    public void addFood(){
        for (int i=0;i < MAX_FOOD;i++){
            generateFood();
        }
    }

    /**
     * Metodo che genera un'istanza di cibo in una coordinata causale nella mappa del mondo
     */
    private void generateFood(){
        Random random = new Random();
        int foodX=random.nextInt(BORDER_X);
        int foodY=random.nextInt(BORDER_Y);

        foods.add(new Pair(foodX,foodY));
    }

    /**
     * Metodo che controlla la collisione con il cibo.
     * Quando uno snake cattura del cibo, aggiungo 3 nuove istanze di cibo al suo posto
     * @param snake snake preso in esame
     */
    public void checkFoodCollision(Snake snake){
        for(int i=0;i<foods.size();i++){
            if(snake.collisionsWithFood(foods.get(i))){
                snake.grow();
                foods.remove(i--);
                for(int j=0;j<3;j++){
                    generateFood();
                }
            }
        }
    }

    /**
     * Metodo che gestisce la morte di un giocatore.
     * Quando un giocatore muore, chiudo il suo socket e sostituisco il suo corpo in cibo catturabile
     * @param player Map ClientHandler-Snake dei giocatori presenti nella partita
     */
    public void die(Map.Entry<ClientHandler,Snake> player){
        player.getKey().write("SERVER: you died!");
        player.getKey().close();
        explode(player.getValue());
        players.remove(player.getKey(),player.getValue());
        server.removeUser(player.getKey().getClientUserName());
    }

    /**
     * Metodo che gestisce la sostituzione del corpo dello snake con del cibo catturabile
     * @param snake l'istanza di snake da sostituire
     */
    public void explode(Snake snake){
        for(SnakeBodyPart snakeBodyPart : snake.getBody()){
            foods.add(new Pair(snakeBodyPart.getX(),snakeBodyPart.getY()));
        }
    }

    /**
     * Metodo che controlla la collisione tra player.
     * Quando avviene una collisione, uccide il player che ha generato la collisione.
     *
     * @param playerEntry Map ClientHandler-Snake dei giocatori presenti nella partita
     * @return true se è avvenuta una collisione, false altrimenti
     */
    public boolean playerCollided(Map.Entry<ClientHandler,Snake> playerEntry){
        //Snake del giocatore preso in analisi
        Snake snake = playerEntry.getValue();

        //Elenco di tutti gli altri snake
        LinkedList<SnakeBodyPart> snakes = new LinkedList<>();

        //Riempio la lista snakes
        for(Map.Entry<ClientHandler,Snake> entry : players.entrySet()){
            if(!entry.getKey().equals(playerEntry.getKey()))
                snakes.addAll(entry.getValue().getBody());
        }

        //Controllo la collisione con ogni entry della lista
        if(snake.collisionsWithBody(snakes)){
            die(playerEntry);
            return true;
        }
        return false;
    }

    /**
     * Metodo che controlla la collisione con il bordo
     * @param playerEntry Entry della map ClientHandler-Snake
     * @return true se lo snake ha colliso con il bordo, false altrimenti
     */

    public boolean borderCollision(Map.Entry<ClientHandler, Snake> playerEntry) {
        SnakeBodyPart head = playerEntry.getValue().getBody().get(0);

        // Il serpente deve stare entro (0, 0) e (5000 - SEGMENT_SIZE, 5000 - SEGMENT_SIZE)
        if (head.getX() < 0 || head.getY() < 0 ||
                head.getX() > (BORDER_X - Snake.SEGMENT_SIZE) ||
                head.getY() > (BORDER_Y - Snake.SEGMENT_SIZE)) {
            die(playerEntry);
            return true;
        }
        return false;
    }

    /**
     * Metodo che manda il pacchetto contenente tutte le informazioni necessarie per la creeazione della vista per ogni client connesso
     */
    public void sendPackage(){
        StringBuilder stringBuilder = new StringBuilder();

        //SERIALIZE COORDINATE DEL CORPO DELLO SNAKE
        for(Map.Entry<ClientHandler,Snake> entry: players.entrySet()){
            stringBuilder.append(Serialize.serializePlayerSnake(entry));
            stringBuilder.append(";");
        }
        //DIVISORE
        stringBuilder.append("&");
        //SERIALIZE COORDINATE DEL CIBO
        for(Pair pair : foods){
            stringBuilder.append(pair.toString());
            stringBuilder.append(",");
        }
        System.out.println(stringBuilder.toString());
        server.sendMessage(stringBuilder.toString());
    }

    /**
     * Metodo che converte una stringa in un coordinata Pair
     * @param newPos stringa con l'intero messaggio ricevuto
     * @param username username del client che ha inviato il messaggio
     */
    private Pair stringToPos(String newPos,String username) {
        String temp = Serialize.removeUsername(newPos,username);
        return Serialize.deserializePlayerPos(temp);
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

    public Pair getBorders(){
        return  new Pair(BORDER_X, BORDER_Y);
    }
}
