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
    private HashMap<Integer, Game> roomNumGameMap;

    public ServerServant(MqttBroker broker) throws RemoteException {
        mqttBroker = broker;
        playerPool = new ArrayList<>();
        roomCount = new AtomicInteger(0);
        games = new ArrayList<>();
        usernamePlayerMap = new HashMap<>();
        roomNumGameMap = new HashMap<>();
    }


    /**
     * Add the login user to playerPool
     */
    @Override
    public void addTOPlayerPool(String username) {
        Player player = new Player(username);
        playerPool.add(player);
        usernamePlayerMap.put(username, player);  // add to hashmap
        mqttBroker.notify(Constants.MQTT_TOPIC + "/" + Constants.SERVER_TOPIC, Constants.LOGIN + ";" + username);

        // debug printings
        System.out.println("Login info start ---> ");
        System.out.println("new user logged in, " + username);
        System.out.print("Current users are : ");
        for(Player p : playerPool){
            System.out.print(p.getUsername() + " ");
        }
        System.out.println();
        System.out.println("Login info end <---" + "\n");
    }


    /**
     * remove the user from playerPool, when closing the client program.
     */
    @Override
    public void removeFromPlayerPool(String username) {
        playerPool.removeIf(player -> player.getUsername().equals(username));
        usernamePlayerMap.remove(username);
        mqttBroker.notify(Constants.MQTT_TOPIC + "/" + Constants.SERVER_TOPIC, Constants.LOGOUT + ";" + username);

        // debug printings
        System.out.println("Logout info start ---> ");
        System.out.println("user logged out, " + username);
        System.out.print("Current users are : ");
        for(Player p : playerPool){
            System.out.print(p.getUsername() + " ");
        }
        System.out.println();
        System.out.println("Logout info end <---" + "\n");
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
        System.out.println("server receive invitation request");
        System.out.println("inviter: " + inviter);
        System.out.println("target: " + targetUser);
        System.out.println("roomNum: " + roomNum);
        mqttBroker.notify(Constants.MQTT_TOPIC + "/" + Constants.CLIENT_TOPIC + "/" + targetUser, Constants.INVITATION + ";" + inviter + ";" + roomNum);
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
        Game g = new Game(playerObjects, roomNum);
        games.add(g);
        roomNumGameMap.put(roomNum, g);
        mqttBroker.notify(Constants.MQTT_TOPIC + "/" + Constants.ROOM_TOPIC + "/" + roomNum, Constants.GAME_START);
    }


    /**
     * Insert a letter to the board at coordinate (i,j): row i, col j
     */
    public void insertLetter(int i, int j, char letter, int roomNum) {
        //game.insertLetter(i, j, letter);
    }


    @Override
    public void startVote(int startI, int startJ, int length, boolean horizontal, int roomNum, String word) throws RemoteException {
        Game currGame = roomNumGameMap.get(roomNum);
        currGame.startVote(startI, startJ, length, horizontal);
        mqttBroker.notify(Constants.MQTT_TOPIC + "/" + Constants.ROOM_TOPIC + "/" + roomNum,
                Constants.VOTE + ";" + startI + ";" + startJ + ";" + length + ";" + horizontal + ";" + word);
    }


    @Override
    public void passTurn(int roomNum) throws RemoteException {

    }


    @Override
    public void vote(String username, boolean agree, int roomNum) throws RemoteException {
        Game currGame = roomNumGameMap.get(roomNum);
        String playername = currGame.getCurrPlayer();
        int result = currGame.vote(playername, agree);

        HashMap<String, Integer> scores = currGame.getScores();

        System.out.println("score get username: " + scores.get(playername));
        if(result == 1){    //if all voted yes
            mqttBroker.notify(Constants.MQTT_TOPIC + "/" + Constants.ROOM_TOPIC + "/" + roomNum,
                    Constants.VOTE_RESULT + ";" + playername + ";" +
                            scores.get(playername) + ";" + "true");
        }else if(result == 0){
            mqttBroker.notify(Constants.MQTT_TOPIC + "/" + Constants.ROOM_TOPIC + "/" + roomNum,
                    Constants.VOTE_RESULT + ";" + playername + ";" +
                            scores.get(playername) + ";" + "false");
        }
        else{  //if somebody has not voted
                System.out.println("Waiting for voting");
        }
    }



    @Override
    public void notifyVoteResult(String username, int score, int roomNum) throws RemoteException {
        Game currGame = roomNumGameMap.get(roomNum);
        mqttBroker.notify(Constants.MQTT_TOPIC + "/" + Constants.ROOM_TOPIC + "/" + roomNum,
                username + ";" + score);
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
