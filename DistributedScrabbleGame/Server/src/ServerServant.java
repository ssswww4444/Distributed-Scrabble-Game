import org.eclipse.paho.client.mqttv3.MqttClient;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicInteger;

public class ServerServant extends UnicastRemoteObject implements ServerInterface {

    private ArrayList<Player> playerPool;
    private AtomicInteger roomCount;
    private MqttBroker mqttBroker;

    /**
     * Constructor
     */
    public ServerServant(MqttBroker broker) throws RemoteException {
        playerPool = new ArrayList<>();
        mqttBroker = broker;
        roomCount = new AtomicInteger(0);
    }


    /**
     * Start voting
     */
    public void startVote(int startI, int startJ, int length, boolean horizontal) {  // horizontal = false --> vertical
        //game.startVote(startI, startJ, length, horizontal);
    }


    /**
     * Pass the turn either before inserting letter or before voting
     */
    public void passTurn() {
        //game.passTurn();
    }


    /**
     * Insert a letter to the board at coordinate (i,j): row i, col j
     */
    public void insertLetter(int i, int j, char letter) {
        //game.insertLetter(i, j, letter);
    }


    /**
     * Vote for the word highlighted
     */
    public void vote(String username, boolean agree) {
        //game.vote(username, agree);

        System.out.println(username + " " + agree);
    }


    /**
     * A player with this username has left the game
     */
    public void leaveGame(String username) {
        //game.leaveGame(username);
    }

// wenqiangk ===> added on Tuesday.
    @Override
    public void addTOPlayerPool(String username) {
        playerPool.add(new Player(username));
        mqttBroker.notify(Constants.SERVER_TOPIC, Constants.SERVER_TOPIC + ";" + "Login" + ";" + username);
        System.out.println("new player added. ");
    }

    @Override
    public ArrayList<String> getPlayerPool() {
        ArrayList<String> playerNames = new ArrayList<>();
        for(Player p: playerPool){
            playerNames.add(p.getUsername());
        }
        return playerNames;
    }

    @Override
    public int addRoom() {
        return roomCount.incrementAndGet();
    }
// wenqiangk <===
}
