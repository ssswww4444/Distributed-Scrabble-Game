import org.eclipse.paho.client.mqttv3.MqttClient;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicInteger;

public class ServerServant extends UnicastRemoteObject implements ServerInterface {

    private MqttBroker mqttBroker;
    private ArrayList<Player> playerPool;
    private AtomicInteger roomCount;
    private ArrayList<Game> games;

    public ServerServant(MqttBroker broker) throws RemoteException {
        mqttBroker = broker;
        playerPool = new ArrayList<>();
        roomCount = new AtomicInteger(0);
        games = new ArrayList<>();
    }


    /**
     * Add the login user to playerPool
     */
    @Override
    public void addTOPlayerPool(String username) {
        playerPool.add(new Player(username));
        mqttBroker.notify(Constants.SERVER_TOPIC, Constants.SERVER_TOPIC + ";" + "Login" + ";" + username);
        System.out.println("new player added. ");
    }


    /**
     * return a list of current users' names
     */
    @Override
    public ArrayList<String> getPlayerPool() {
        ArrayList<String> playerNames = new ArrayList<>();
        for (Player p : playerPool) {
            playerNames.add(p.getUsername());
        }
        return playerNames;
    }


    /**
     * Increment the room counter when a room is created.
     */
    @Override
    public int addRoom() {
        return roomCount.incrementAndGet();
    }


    /**
     * Decrement the room counter when a room is dismissed.
     */
    @Override
    public void leaveRoom() {
//        roomCount.decrementAndGet();
    }


    /**
     * Invite all available online users.
     */
    @Override
    public void inviteAll(String inviter, int roomNum) {
            mqttBroker.notify(Constants.SERVER_TOPIC, Constants.SERVER_TOPIC + ";" + Constants.INVITATION + ";" + inviter + ";" + roomNum);
    }


    /**
     * One to One invitation.
     */
    @Override
    public void invite(String inviter, String targetUser, int roomNum) throws RemoteException {
        mqttBroker.notify(Constants.SERVER_TOPIC, Constants.SERVER_TOPIC + ";" + Constants.INVITATION + ";" + targetUser + ";" + roomNum);
    }

    /**
     * Return all the users given the room number.
     */
    @Override
    public ArrayList<String> getUserInRoom(int roomNum) throws RemoteException {
        ArrayList<String> userNames = new ArrayList<>();
        for(Player player : playerPool){
            if(Integer.parseInt(player.getStatus().split(" ")[1]) == roomNum){
                userNames.add(player.getUsername());
            }
        }
        return userNames;
    }


    /**
     * Initialize a game and enter the game interface.
     * Synchronize all players' gui when they are in the same room and the game starts.
     */
    @Override
    public void startNewGame(ArrayList<String> players, int roomNum) {
        ArrayList<Player> playerObjects = new ArrayList<>();
        for (String playerName : players) {
            playerObjects.add(new Player(playerName));
        }
        games.add(new Game(playerObjects, roomNum));
        mqttBroker.notify(Constants.ROOM + " " + roomNum, Constants.GAME_START);
    }


    /**
     * Insert a letter to the board at coordinate (i,j): row i, col j
     */
    public void insertLetter(int i, int j, char letter, int roomNum) {
        //game.insertLetter(i, j, letter);
    }


    @Override
    public void startVote(int startI, int startJ, int length, boolean horizontal, int roomNum) throws RemoteException {

    }


    @Override
    public void passTurn(int roomNum) throws RemoteException {

    }


    @Override
    public void vote(String username, boolean agree, int roomNum) throws RemoteException {

    }


    @Override
    public void leaveGame(String username, int roomNum) throws RemoteException {

    }
}
