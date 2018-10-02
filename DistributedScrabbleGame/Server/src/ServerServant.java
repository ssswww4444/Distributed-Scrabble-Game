import org.eclipse.paho.client.mqttv3.MqttClient;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class ServerServant extends UnicastRemoteObject implements ServerInterface {

    private MqttBroker mqttBroker;
    private ArrayList<Player> playerPool;
    private AtomicInteger roomCount;
    private ArrayList<Game> games;
    private HashMap<String, Player> usernamePlayerMap;

    public ServerServant(MqttBroker broker) throws RemoteException {
        mqttBroker = broker;
        playerPool = new ArrayList<>();
        roomCount = new AtomicInteger(0);
        games = new ArrayList<>();
        usernamePlayerMap = new HashMap<>();
    }


    /**
     * Add the login user to playerPool
     */
    @Override
    public void addTOPlayerPool(String username) {
        System.out.println("11");
        Player player = new Player(username);
        playerPool.add(player);
        System.out.println("12");
        usernamePlayerMap.put(username, player);  // add to hashmap
        mqttBroker.notify(Constants.MQTT_TOPIC + "/" + Constants.SERVER_TOPIC, Constants.LOGIN + ";" + username);
        System.out.println("13");
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
    public int createRoom(String username) {
        // get roomID
        int roomID = roomCount.incrementAndGet();

        // update player status
        Player player = usernamePlayerMap.get(username);
        player.setStatus(Constants.STATUS_ROOM);
        player.setRoomNum(roomID);

        return roomID;
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
            mqttBroker.notify(Constants.MQTT_TOPIC + "/" + Constants.SERVER_TOPIC, Constants.INVITATION + ";" + inviter + ";" + roomNum);
    }


    /**
     * One to One invitation.
     */
    @Override
    public void invite(String inviter, String targetUser, int roomNum) throws RemoteException {
        mqttBroker.notify(Constants.MQTT_TOPIC + "/" + Constants.CLIENT_TOPIC + targetUser, Constants.INVITATION + ";" + inviter + ";" + roomNum);
    }

    /**
     * Return all the users given the room number.
     */
    @Override
    public ArrayList<String> getUserInRoom(int roomNum) throws RemoteException {
        ArrayList<String> userNames = new ArrayList<>();
        for(Player player : playerPool){
            if(player.getRoomNum() == roomNum) {
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
        mqttBroker.notify(Constants.MQTT_TOPIC + "/" + Constants.ROOM_TOPIC + "/" + roomNum, Constants.GAME_START);
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

    @Override
    public boolean canJoinRoom(String username, int roomNum) throws  RemoteException {
        ArrayList<String> players = getUserInRoom(roomNum);
        if (players.size() < Constants.ROOM_MAX_PLAYER) {
            // notify all players currently in the room that new player joined
            mqttBroker.notify(Constants.MQTT_TOPIC + "/" + Constants.ROOM_TOPIC + "/" + roomNum, Constants.JOIN_ROOM + ";" + username);

            // update player status
            Player player = usernamePlayerMap.get(username);
            player.setStatus(Constants.STATUS_ROOM);
            player.setRoomNum(roomNum);

            return true;
        }
        return false;
    }
}
