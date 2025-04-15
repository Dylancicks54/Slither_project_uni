package Net;

import view.*;
import java.io.*;
import java.net.Socket;
/**
 * Classe che gestisce la comunicazione con il server a cui il client è connesso
 */
public class Client {
    private Socket socket;
    private BufferedReader bufferedReader;
    private BufferedWriter bufferedWriter;
    private String userName;
    //Dove vengo conservati gli oggetti di gioco serializzati
    private String snakes;
    private String foods;
    //Dove viene conservato il messaggio da deserializzare dal server
    private String messageFromServer;

    /**
     * Costruttore.
     * Crea un istanza di Client, inizializza un BufferedReader e un BufferedWriter per I/O con il server collegato al socket
     * @param socket istanza di Socket connessa al server
     * @param userName stringa contentente l'username del client
     */
    public Client (Socket socket,String userName){
        System.out.println("Tentativo di connessione...");
        try{
            this.socket = socket;
            this.bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            this.bufferedWriter = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
            this.userName=userName;
            System.out.println("Connesso al server "+socket.getInetAddress());
        }catch (IOException e){
            closeEverything( socket, bufferedWriter, bufferedReader);
        }
    }
    /**
     * Metodo che per consolidare la connessione con il server.
     */
    public void confirmConnection(){
        try{
            bufferedWriter.write(userName);
            bufferedWriter.newLine();
            bufferedWriter.flush();
        }catch (IOException ignore){

        }
    }
    /**
     * Metodo che gestisce l'invio dei messaggi al server.
     * La struttura del messaggio deve essere la seguente: <USERNAME>-<CORPO_MESSAGGIO>.
     * Il corpo del messaggio contiene gli snake ed i cibi serializzati divisi da un "&".
     * @param message stringa che va inserita nel corpo del messaggio
     */
    public void write(String message){
        if (socket.isConnected()){
            try {
                bufferedWriter.write(userName + "-" + message);
                bufferedWriter.newLine();
                bufferedWriter.flush();
            }catch (IOException ignore){

            }
        }else {
            System.out.println("Server disconnesso");
        }
    }
    /**
     * Metodo che chiude tutte le strutture create per la comunicazione con il server
     */
    public void closeEverything(Socket socket, BufferedWriter bufferedWriter, BufferedReader bufferedReader) {
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
    /**
     * Metodo che legge i messaggi in arrivo dal server mentre il socket è attivo.
     * E' un metodo bloccante e quindi è necessario eseguire questo precesso su un thread separato per evitare di fermare il programma principale
     */
    public void listenForMessage() {
        while (socket.isConnected()) {
            try {
                messageFromServer = bufferedReader.readLine();
                if (messageFromServer == null) {
                    System.out.println("Connessione chiusa.");
                    closeEverything(socket, bufferedWriter, bufferedReader);
                    break;
                }

                // Gestione del messaggio per l'aggiornamento del nome utente
                if (messageFromServer.startsWith("SERVER:USERNAME_UPDATE-")) {
                    String updatedName = messageFromServer.split("-", 2)[1];
                    System.out.println("Il tuo nome è stato aggiornato in: " + updatedName);
                    userName = updatedName;
                    continue;
                }

                // DEBUG
                //System.out.println(messageFromServer);


                // Elaborazione degli snake e cibi
                String[] parts = messageFromServer.split("&");
                if (parts.length >= 2) {
                    snakes = parts[0];
                    foods = parts[1];
                }
            } catch (IOException e) {
                closeEverything(socket, bufferedWriter, bufferedReader);
            }
        }
    }


    /**
     * Metodo per la chiusura del client
     */
    public void close(){
        System.out.println("SERVER: bye!");
        closeEverything(socket,bufferedWriter,bufferedReader);
    }

    public String getMessageFromServer(){
        return messageFromServer;
    }
    public String getFoods(){
        return foods;
    }
    public String getSnakes(){
        return snakes;
    }
    public String getUserName() {
        return userName;
    }
    public boolean isClosed(){
        return socket.isClosed();
    }

    public static void main(String [] args)  {
        new ConnectionMenu().setVisible(true);
    }


}

