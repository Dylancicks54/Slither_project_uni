package Net;

import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Classe che gestisce la comunicazione con un singolo client dal lato del server.
 * E' responsabile per inviare e ricevere messarri dal client
 */

public class ClientHandler implements Runnable{
    public static List<ClientHandler> clientHandlers = new ArrayList<>();

    private Socket socket;
    private BufferedReader bufferedReader;
    private BufferedWriter bufferedWriter;

    private String clientUserName;

    //Dove viene conservato il messaggio serializzato della nuova posizione di dove deve andare il client
    private String newPos;

    private boolean isAlive;

    /**
     * Costruttore.
     * Inizializza un BufferedReader e un BufferedWriter per la comunicazione con il client tramite il socket e resta in attesa di un username dal client.
     * @param socket il socket connesso al client
     */
    public ClientHandler(Socket socket, Set<String> users) {
        try {
            this.isAlive=true;
            this.socket = socket;
            this.bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            this.bufferedWriter = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
            this.clientUserName = bufferedReader.readLine();
            while (users.contains(this.clientUserName)){
                this.clientUserName +=1;
            }
            clientHandlers.add(this);
            users.add(this.clientUserName);
        }catch (IOException e){
            closeEverything(socket,bufferedWriter,bufferedReader);
        }
    }

    @Override
    public void run() {
        receiveMessage();
    }
    /**
     * Metodo che riceve i messaggi dal client mentre il socket è connesso
     */
    public void receiveMessage(){
        String messageFromClient;

        while (socket.isConnected()){
            try{
                messageFromClient = bufferedReader.readLine();
                if(messageFromClient==null) {
                    System.out.println("SERVER: \""+ clientUserName +"\" disconnected");
                    closeEverything(socket, bufferedWriter, bufferedReader);
                }
                newPos = messageFromClient;
                System.out.println(newPos);
            }catch (IOException e){
                closeEverything(socket,bufferedWriter,bufferedReader);

                break;
            }
        }
    }

    /**
     * Metdo che gestisce la chiusura del ClientHandler
     */
    public void close(){
        closeEverything(socket,bufferedWriter,bufferedReader);
    }

    /**
     * Metodo che gestisce l'invio in broadcast di un messaggio (messo in input del metodo) proventiente da un client a tutti gli altri
     * broadcastMessage broadcast a message as parameter from the client to every other client
     * @param messageToSend messaggio da inoltrare
     */
    public void broadcastMessage(String messageToSend){
        for (ClientHandler clientHandler : clientHandlers){
            try{
                if(!clientHandler.clientUserName.equals(clientUserName)){
                    clientHandler.bufferedWriter.write(messageToSend);
                    clientHandler.bufferedWriter.newLine();
                    clientHandler.bufferedWriter.flush();
                }
            }catch (IOException e){
                closeEverything(socket,bufferedWriter,bufferedReader);
            }
        }
    }

    /**
     * Metodo che gestisce la scrittura di un messaggio dal server al client collegato a questo socket
     * @param mes messaggio da inviare
     */
    public void write (String mes){
        try {
            bufferedWriter.write(mes);
            bufferedWriter.newLine();
            bufferedWriter.flush();
        }catch (IOException ignore){
        }
    }

    /**
     * Metodo che rimuove un ClientHandler dalla lista dei client attivi
     */
    public void removeClientHandler(){
        clientHandlers.remove(this);
        broadcastMessage("SERVER: \"" + clientUserName + "\" has left");
    }

    /**
     * Metodo che gestisce la chiusura di tutte le componenti necessarie per la communicazione con il client
     * @param socket
     * @param bufferedWriter
     * @param bufferedReader
     */
    public void closeEverything(Socket socket, BufferedWriter bufferedWriter, BufferedReader bufferedReader) {
        removeClientHandler();
        this.isAlive=false;
        try {
            if (socket != null)
                socket.close();
            if (bufferedReader != null)
                bufferedReader.close();
            if (bufferedWriter != null)
                bufferedWriter.close();
        }catch (IOException e){
            e.printStackTrace();
        }
    }
    public boolean isAlive(){
        return isAlive;
    }

    public String getClientUserName() {
        return clientUserName;
    }

    public String getNewPos() {
        return newPos;
    }
}
