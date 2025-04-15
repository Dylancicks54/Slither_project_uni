package Net;

import model.*;
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;

/**
 * Classe che gestisce l'avvio del server.
 * Una volta avviato, parte il loop di gioco, accetta le connessioni di Client e riceve/manda messaggi da/a loro
 */
public class Server {
    private final ServerSocket serverSocket;
    private final List<ClientHandler> clientHandlers;
    private final GameServer gameServer;

    private final Set<String> users;
    /**
     * Costruttore.
     * Inizializza la lista dei clientHandlers e la classe GameServer
     * @param serverSocket ServerSocket
     */
    public Server(ServerSocket serverSocket) {
        clientHandlers = new ArrayList<>();
        users = new HashSet<>();
        this.serverSocket = serverSocket;
        this.gameServer=new GameServer(this);
    }
    /**
     * Metodo resposabile per accettare le connesione dei client che lo richiedono.
     * E' un metodo bloccante quindi deve girare su un'altro thread e creare un ClientHandler per ciascun utente in un thread separato
     */
    public void startServer() {
        System.out.println("SERVER: avviato\nSERVER: In attesa di connessioni...");
        try {
            while (!serverSocket.isClosed()) {
                Socket socket = serverSocket.accept();

                //Accetto la connessione, lo aggiungo nell'elenco dei ClientHandler e lo aggiungo nel mondo di gioco
                ClientHandler clientHandler = new ClientHandler(socket,users);
                System.out.println("SERVER: Nuovo giocatore connesso: "+clientHandler.getClientUserName());
                clientHandlers.add(clientHandler);
                Pair spawnPoint = getSpawnPoint();
                gameServer.addPlayer(clientHandler,new Snake(spawnPoint.getX(), spawnPoint.getY(),Direction.RIGHT));

                Thread thread = new Thread(clientHandler);
                thread.start();
            }
        }
        catch(IOException ignore){
            closeServerSocket();
        }
    }

    /**
     * Metodo che genere delle coordinate per lo spawn del client
     * @return coordinate di spawn
     */
    public Pair getSpawnPoint(){
        Random random = new Random();

        int x = random.nextInt(GameServer.getSpawnAreaX());
        int y = random.nextInt(GameServer.getSpawnAreaY());

        return new Pair(x,y);
    }
    /**
     * Rimuove un utente dal set dei nomi
     * @param userName nome utente da rimuovere
     */
    public void removeUser(String userName) {
        users.remove(userName);
    }

    /**
     * Metodo che chiede il socket lato server
     */
    public void closeServerSocket() {
        try {
            if (serverSocket != null) {
                serverSocket.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    /**
     * Metodo che manda un messaggio ad ogni client presente nella lista.
     * @param str messaggio da inviarte
     */
    public void sendMessage(String str){
        for(ClientHandler clientHandler: clientHandlers){
            clientHandler.write(str);
        }
    }
    /**
     * Metodo per l'esecuzione del loop di gioco.
     * Inotre cerca di mantere 60 tick stabili
     */
    public void respond(){
        System.out.println("SERVER: Partita iniziata");
        while(!serverSocket.isClosed()){
            try {
                long start = System.currentTimeMillis();
                Thread t= new Thread(gameServer::update);
                t.start();
                t.join();
                long finish = System.currentTimeMillis()-start;

                if(finish<17) //1000/60 = circa 17
                    t.sleep(17-(finish));
            }catch (InterruptedException ignora){
            }
        }
    }

    public static void main(String [] args)  {
        try {
            ServerSocket serversocket = new ServerSocket(1234);
            Server server = new Server(serversocket);
            //Avvio loop di gioco
            new Thread(server::respond).start();
            //Avvio processo per l'ascolto dei messaggi per il server
            new Thread(server::startServer).start();
        }catch (IOException ignore){}
    }
}