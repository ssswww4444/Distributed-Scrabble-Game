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
    private HashMap<String, String> usernameStatusMap;
    private HashMap<Integer, Game> roomNumGameMap;

    // room -> username -> info(ArrayList)
    // info: pos in room, ready(0 for not ready, 1 for ready)
    private HashMap<Integer, HashMap<String, ArrayList<Integer>>> roomUserInfoMap;  // room -> username -> info(ArrayList)

    public ServerServant(MqttBroker broker) throws RemoteException {
        mqttBroker = broker;
        playerPool = new ArrayList<>();
        roomCount = new AtomicInteger(0);
        games = new ArrayList<>();
        usernamePlayerMap = new HashMap<>();
        roomNumGameMap = new HashMap<>();
        usernameStatusMap = new HashMap<>();
        roomUserInfoMap = new HashMap();
    }


    /**
     * Add the login user to playerPool
     */
    @Override
    public void addTOPlayerPool(String username) {
        Player player = new Player(username);
        playerPool.add(player);
        usernamePlayerMap.put(username, player);  // add to hashmap
        usernameStatusMap.put(username, Constants.STATUS_AVAILABLE);

        mqttBroker.notify(Constants.MQTT_TOPIC + "/" + Constants.SERVER_TOPIC, Constants.PLAYER_LIST_UPDATE);

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

    @Override
    public void ready(String username, int roomNum) throws RemoteException {
        // update map
        roomUserInfoMap.get(roomNum).get(username).set(1,1);  // set to ready
        mqttBroker.notify(Constants.MQTT_TOPIC + "/" + Constants.ROOM_TOPIC + "/" + roomNum, Constants.READY + ";" + username);
    }


    /**
     * remove the user from playerPool, when closing the client program.
     */
    @Override
    public void removeFromPlayerPool(String username) {
        playerPool.removeIf(player -> player.getUsername().equals(username));
        usernamePlayerMap.remove(username);
        usernameStatusMap.remove(username);

        mqttBroker.notify(Constants.MQTT_TOPIC + "/" + Constants.SERVER_TOPIC, Constants.PLAYER_LIST_UPDATE);

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
     * Get usernameStatus hashmap
     */
    @Override
    public HashMap<String, String> getUsernameStatusMap() throws RemoteException {
        return usernameStatusMap;
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
        player.setIsHost(true);
        usernameStatusMap.put(username, Constants.STATUS_ROOM + " " + roomID);

        ArrayList<Integer> infoList = new ArrayList<>();
        infoList.add(1);  // position
        infoList.add(1);  // host always ready
        HashMap<String, ArrayList<Integer>> userInfoMap = new HashMap<>();
        userInfoMap.put(username, infoList);
        roomUserInfoMap.put(roomID, userInfoMap);

        mqttBroker.notify(Constants.MQTT_TOPIC + "/" + Constants.SERVER_TOPIC, Constants.PLAYER_LIST_UPDATE);

        return roomID;
    }


    /**
     * Decrement the room counter when a room is dismissed.
     */
    @Override
    public void leaveRoom(String username, boolean isHost, int roomNum) throws RemoteException {
        if (isHost) {  // dismiss room
            for (String user: roomUserInfoMap.get(roomNum).keySet()) {
                playerLeaveRoom(user);
                usernameStatusMap.put(user, Constants.STATUS_AVAILABLE);
            }
            roomUserInfoMap.remove(roomNum);
            usernamePlayerMap.get(username).setIsHost(false);  // not host anymore
            mqttBroker.notify(Constants.MQTT_TOPIC + "/" + Constants.ROOM_TOPIC + "/" + roomNum, Constants.DISMISS_ROOM + ";" + username);
        } else {   // leave room
            playerLeaveRoom(username);
            usernameStatusMap.put(username, Constants.STATUS_AVAILABLE);
            roomUserInfoMap.get(roomNum).remove(username);
            mqttBroker.notify(Constants.MQTT_TOPIC + "/" + Constants.ROOM_TOPIC + "/" + roomNum, Constants.LEAVE_ROOM + ";" + username);
        }

        mqttBroker.notify(Constants.MQTT_TOPIC + "/" + Constants.SERVER_TOPIC, Constants.PLAYER_LIST_UPDATE);
    }

    /**
     * Get available players
     */
    @Override
    public ArrayList<String> getAvailablePlayers() throws  RemoteException {
        ArrayList<String> players = new ArrayList<String>();
        for (Player player: playerPool) {
            if (player.getStatus().equals(Constants.STATUS_AVAILABLE)) {
                players.add(player.getUsername());
            }
        }
        return players;
    }

    /**
     * Update player status when leave room
     */
    private void playerLeaveRoom(String username) {
        Player player = usernamePlayerMap.get(username);
        player.setRoomNum(Constants.NOT_IN_ROOM_ID);
        player.setStatus(Constants.STATUS_AVAILABLE);
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
    public HashMap<String, ArrayList<Integer>> getUserInRoom(int roomNum) throws RemoteException {
        return roomUserInfoMap.get(roomNum);
    }


    /**
     * Initialize a game and enter the game interface.
     * Synchronize all players' gui when they are in the same room and the game starts.
     */
    @Override
    public void startNewGame(ArrayList<String> players, int roomNum) {
        // set all to "not ready" except host
        for (String name: roomUserInfoMap.get(roomNum).keySet()) {
            ArrayList<Integer> infoList = roomUserInfoMap.get(roomNum).get(name);
            if (infoList.get(0) == 1) {  // isHost
                continue;
            }
            infoList.set(1,0); // set to not ready
            roomUserInfoMap.get(roomNum).put(name, infoList);
        }

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

    /**
     * No word is found by player
     */
    @Override
    public void noWord(int roomNum) throws RemoteException{
        mqttBroker.notify(Constants.MQTT_TOPIC + "/" + Constants.ROOM_TOPIC + "/" + roomNum,
                Constants.NO_WORD);
    }


    @Override
    public void startVote(int startI, int startJ, int length, boolean horizontal, int roomNum, String word) throws RemoteException {
        Game currGame = roomNumGameMap.get(roomNum);
        currGame.startVote(startI, startJ, length, horizontal);
        mqttBroker.notify(Constants.MQTT_TOPIC + "/" + Constants.ROOM_TOPIC + "/" + roomNum,
                Constants.VOTE + ";" + startI + ";" + startJ + ";" + length + ";" + horizontal + ";" + word);
    }

    @Override
    public void placeLetter(int insertedI, int insertedJ, String insertLetter, int roomNum) throws RemoteException {
        Game currGame = roomNumGameMap.get(roomNum);
        currGame.placeLetter(insertedI, insertedJ, insertLetter);
        mqttBroker.notify(Constants.MQTT_TOPIC + "/" + Constants.ROOM_TOPIC + "/" + roomNum,
                Constants.PLACE_LETTER + ";" + insertedI + ";" + insertedJ + ";"  + insertLetter);
    }

    @Override
    public void passTurn(int roomNum) throws RemoteException {
        Game currGame = roomNumGameMap.get(roomNum);
        boolean endGame = currGame.passTurn();
        if(endGame){
            mqttBroker.notify(Constants.MQTT_TOPIC + "/" + Constants.ROOM_TOPIC + "/" + roomNum,
                    Constants.END_GAME);
        }else{
            mqttBroker.notify(Constants.MQTT_TOPIC + "/" + Constants.ROOM_TOPIC + "/" + roomNum,
                    Constants.PASS);
        }
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
    public void leaveGame(String username, int roomNum) throws RemoteException {

    }

    @Override
    public boolean canJoinRoom(String username, int roomNum) throws  RemoteException {
//        ArrayList<String> players = getUserInRoom(roomNum);
        HashMap<String, ArrayList<Integer>> currentUserInfoMap = roomUserInfoMap.get(roomNum);

        if (currentUserInfoMap.keySet().size() < Constants.ROOM_MAX_PLAYER) {
            // update player status
            Player player = usernamePlayerMap.get(username);
            player.setStatus(Constants.STATUS_ROOM);
            player.setRoomNum(roomNum);
            usernameStatusMap.put(username, Constants.STATUS_ROOM + " " + roomNum);

            ArrayList<Integer> infoList = new ArrayList<>();
            int pos = 2;
            while(pos < 5) {
                for (ArrayList<Integer> playerInfo: currentUserInfoMap.values()) {
                    if (playerInfo.get(0) == pos) {
                        pos++;
                        continue;  // pos not available
                    }
                }
                break;  // found a pos available
            }
            infoList.add(pos);  // position
            infoList.add(0);  // initially not ready
            currentUserInfoMap.put(username, infoList);
            roomUserInfoMap.put(roomNum, currentUserInfoMap);

            // notify all players currently in the room that new player joined at the pos
            mqttBroker.notify(Constants.MQTT_TOPIC + "/" + Constants.ROOM_TOPIC + "/" + roomNum, Constants.JOIN_ROOM + ";" + username + ";" + pos);

            mqttBroker.notify(Constants.MQTT_TOPIC + "/" + Constants.SERVER_TOPIC, Constants.PLAYER_LIST_UPDATE);

            return true;
        }
        return false;
    }
}
