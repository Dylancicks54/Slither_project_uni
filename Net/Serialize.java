package Net;

import model.*;
import java.util.*;
/**
 * Classe di supporto per la serializzazione e deserializzazione dei messaggi in entrata ed in uscita dal server
 */
public class Serialize {
    /**
     * Metodo per la serializzazione della posizione del cursore del giocare in una stringa.
     * @param mouseX ascissa della posizione del mouse
     * @param mouseY ordinata della posizione del mouse
     * @return String coordinate mouse serializzate
     */
    public static String serializePlayerPos(int mouseX,int mouseY){
        return "x:"+mouseX+",y:"+mouseY;
    }

    /**
     * Metodo per la deserializzazione della stringa in posizione del cursore del giocatore.
     * @param string messaggio serializzato
     * @return Pair coordinate del mouse
     */
    public static Pair deserializePlayerPos(String string){
        int x;
        int y;

        String[] tokens =string.split(",");

        //Reperisco la X
        String[] t = tokens[0].split(":");
        x=Integer.parseInt(t[1]);

        //Reperisco la Y
        t=tokens[1].split(":");
        y=Integer.parseInt(t[1]);
        return new Pair(x,y);

    }

    /**
     * Metodo per rimuovere l'username da una stringa
     * @param str corpo messaggio
     * @param username username del client
     * @return stringa senza username
     * */
    public static String  removeUsername(String str,String username){
        return str.replace(username+"-","");
    }

    /**
     * Metodo per serializzare uno snake includendo l'username del client
     * @param player entry della mappa contenente ClientHandler-Snake
     * @return stringa con le coordinate dello snake
     * es: <USERNAME>,730:400,584:400,438:400,292:400,146:400; size of 5
     * */
    public static String serializePlayerSnake(Map.Entry<ClientHandler, Snake> player){
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(player.getKey().getClientUserName());
        stringBuilder.append(",");
        for(int i=0;i<player.getValue().getBody().size()-1;i++){
            stringBuilder.append(player.getValue().getBody().get(i).toString());
            stringBuilder.append(",");
        }
        stringBuilder.append(player.getValue().getBody().get(player.getValue().getBody().size()-1));
        return stringBuilder.toString();
    }

    /**
     * Metodo per deserializzare ogni snake a partire da una stringa
     * @param snakes stringa contenente gli snake serializzati
     * @return map con gli username ed una lista di coordinate che descrivono la posizione di ogni SnakeBodyPart
     */
    public static  Map<String,List<Pair>> deserializeSnakes(String snakes){
        String [] tokens=snakes.split(";");
        Map<String,List<Pair>> snakeMap = new HashMap<>();
        for(int i=0;i<tokens.length;i++){
            snakeMap.put(tokens[i].split(",")[0],deserializeSnake(tokens[i]));
        }
        return snakeMap;
    }

    /**
     * Metodo per deserializzare un singolo serpente a partire da una stringa.
     * @param str stringa contenente le coordinate di una SnakeBodyPart
     * @return List<Pair> che descrivono le coordinate di ogni snakeBodyPart
     */
    public static List<Pair> deserializeSnake(String str){
        List<Pair> snakeCoordinate=new ArrayList<>();

        String []tokens = str.split(",");
        for(int i =1;i<tokens.length;i++){
            String []coordinates=tokens[i].split(":");
            snakeCoordinate.add(new Pair(Integer.parseInt(coordinates[0]),Integer.parseInt(coordinates[1])));
        }
        return snakeCoordinate;
    }

    /**
     * Metodo per deserializzare le posizioni dei cibi a partire da una stringa.
     * @param foods stringa contenente le coordinate dei Food
     * @return List<Pair> che descrivono le coordinate di ogni Food
     */
    public static List<Pair> deserializeFoods(String foods){
        //Usato lo stesso metodo di uno snake solo
        return deserializeSnake(foods);
    }

}
