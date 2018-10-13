import javafx.application.Platform;
import org.eclipse.paho.client.mqttv3.MqttException;

import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.ArrayList;
import java.util.HashMap;

public class GameClient {
    private String username;
    private int roomNumber;
    private boolean isHost;
    private int currTurn;
    private MenuController menuController;
    private RoomController roomController;
    private GameController gameController;
    private HashMap<String, ArrayList<Integer>> roomPlayerInfoMap;
    private MqttBroker mqttBroker;

    private ServerInterface serverServantStub;

    public String getUsername() {
        return this.username;
    }

    public int getRoomNumber() {
        return this.roomNumber;
    }

    public boolean isHost() {
        return isHost;
    }

    public void setMenuController(MenuController controller) {
        this.menuController = controller;
    }

    public void setRoomController(RoomController controller) {
        this.roomController = controller;
    }

    public void setGameController(GameController controller) {
        this.gameController = controller;
    }

//    public void setIsHost(boolean isHost){
//        this.isHost = isHost;
//    }

    /**
     * Create game client.
     * 1. look up the registry
     * 1. Subscribe Server topic and wait for messages
     * 2. Add the current player to server playerPool via RMI
     */
    public GameClient(String username) throws Exception {

        // get the RMI stub.
        getServerRegistry();

        ArrayList<String> playerPool;

        try {
            playerPool = serverServantStub.getPlayerPool();
        } catch (Exception e) {
            throw new Exception("Cannot find server.");
        }

        if (playerPool.contains(username)) {
            throw new Exception("Username not available. Please choose another one.");
        } else if (username.equals("gameServer")) {  // username cannot be "gameServer" (for mqtt id purpose)
            throw new Exception("Username cannot be 'gameServer'. Please choose another one.");
        } else if (username.equals("")) {  // empty string
            throw new Exception("Username cannot be empty.");
        } else {
            this.username = username;
            mqttBroker = new MqttBroker(username, this);
            serverServantStub.addTOPlayerPool(username);
        }

        // init
        isHost = false;
        roomNumber = Constants.NOT_IN_ROOM_ID;
        roomPlayerInfoMap = new HashMap<>();
    }


    /**
     * Get the game server remote servant.
     */
    private void getServerRegistry() throws Exception {
        try {
            Registry registry = LocateRegistry.getRegistry("localhost");
            serverServantStub = (ServerInterface) registry.lookup("ServerInterface");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Get all current available users. (Current implementation is just "Return all users")
     */
    public ArrayList<String> getAvailablePlayers() {
        ArrayList<String> players = new ArrayList<>();
        try {
            players = serverServantStub.getAvailablePlayers();
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        return players;
    }

    /**
     * Display all online users.
     */
    public void renderPlayerList() {
        ArrayList<String> players;
        HashMap<String, String> usernameStatusMap;
        try {
            players = serverServantStub.getPlayerPool();
            usernameStatusMap = serverServantStub.getUsernameStatusMap();

            if (this.menuController != null) {
                ArrayList<PlayerModel> playerModels = new ArrayList<>();

                for (String player : players) {
                    playerModels.add(new PlayerModel(player, usernameStatusMap.get(player)));
                }

                menuController.updatePlayerList(playerModels);
            }
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    /**
     * Create a new room
     * 1. get roomID from server
     * 2. subscribe to "mqtt/room/roomID"
     * 3. get player names
     */
    public void createRoom() {
        try {
            this.roomNumber = serverServantStub.createRoom(this.username);  // get roomID from server
            mqttBroker.getMqttClient().subscribe(Constants.MQTT_TOPIC + "/" + Constants.ROOM_TOPIC + "/" + Integer.toString(roomNumber));  // subscribe

            addToInfoMap(this.username, 1, 1);  // host always ready

            isHost = true;
            renderRoomPage();
        } catch (RemoteException | MqttException e) {
            e.printStackTrace();
        }
    }

    /**
     * Add new player to the roomPlayerInfoMap
     */
    private void addToInfoMap(String username, int pos, int ready) {
        ArrayList<Integer> infoList = new ArrayList<>();
        infoList.add(pos);
        infoList.add(ready);
        roomPlayerInfoMap.put(username, infoList);
    }


    /**
     * Receive notification that new player joined room
     */
    public void playerJoinedRoom(String username, int pos) {
        addToInfoMap(username, pos, 0);  // update map

        Platform.runLater(() -> {
            GameClient.this.roomController.joinRoom(username, pos);  // update UI
        });
    }

    /**
     * Send notification that the player is ready
     */
    public void ready() {
        try {
            serverServantStub.ready(username, roomNumber);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }


    /**
     * Receive notification that player left room
     */
    public void playerLeaveRoom(String username) {
        roomPlayerInfoMap.remove(username);

        int pos = roomPlayerInfoMap.get(username).get(0);

        if(this.roomController!=null){      // If user is in room scene
            Platform.runLater(() -> {
                GameClient.this.roomController.leaveRoom(pos, isHost);  // update UI
            });
        } else{      // If user is in game scene
            Platform.runLater(()->{
                GameClient.this.gameController.renderResultPage(username, false); // not
            });
        }
    }

    /**
     * Receive notification that host dismissed room
     */
    public void hostDismissRoom(String username) {
        if(this.roomController!=null){  // Room dismissed in Room scene
            Platform.runLater(() -> {
                GameClient.this.roomController.dismissRoom(username);  // update UI
            });
            try {
                mqttBroker.getMqttClient().unsubscribe(Constants.MQTT_TOPIC + "/" + Constants.ROOM_TOPIC + "/" + Integer.toString(roomNumber));
            } catch (MqttException e) {
                e.printStackTrace();
            }
            roomPlayerInfoMap = new HashMap<>();

            roomNumber = Constants.NOT_IN_ROOM_ID;
        }else{  // Room dismissed in Game scene
            Platform.runLater(()->{
                GameClient.this.gameController.renderResultPage(username, true);
            });
            try {
                mqttBroker.getMqttClient().unsubscribe(Constants.MQTT_TOPIC + "/" + Constants.ROOM_TOPIC + "/" + Integer.toString(roomNumber));;
            } catch (MqttException e) {
                e.printStackTrace();
            }
        }

    }

    /**
     * Get notification that a client is ready
     */
    public void playerReady(String username) {
        ArrayList<Integer> infoList = roomPlayerInfoMap.get(username);
        infoList.remove(1);
        infoList.add(1);
        roomPlayerInfoMap.put(username, infoList);  // ready

        if (roomController != null) {  // not at room scene
            Platform.runLater(() -> {
                this.roomController.playerReady(infoList.get(0));  // player at this pos get ready
            });
        }
    }


    /**
     * Create a new room, room ID is assigned by server.
     * Host start a game and server then send broadcasts to others in the room
     */
    public void startGame() {
        try {
            serverServantStub.startNewGame(new ArrayList<>(roomPlayerInfoMap.keySet()), roomNumber);

        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }


    /**
     * Note!! This is used by guests in the room, be notified to update UI to "game".
     */
    public void renderGamePage() {
        this.currTurn = 1;
        roomController.fadeOut("Game");
    }

    /**
     * Send invitation to target user via Server
     */
    public void invite(String username) {
        try {
            serverServantStub.invite(this.username, username, roomNumber);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }


    public void receiveInvitation(String username, int roomNumber) {
        Platform.runLater(() -> {   // avoid update directly from non-application thread
            GameClient.this.menuController.invitationMsg(username, roomNumber);  // update UI
        });
    }


    public void acceptInvitation(int roomNumber) {
        try {
            if (serverServantStub.canJoinRoom(this.username, roomNumber)) {  // check if can join
                roomPlayerInfoMap = serverServantStub.getUserInRoom(roomNumber);

                this.roomNumber = roomNumber;
                renderRoomPage();
            } else {  // failed
                menuController.displayMsg();
            }
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    /**
     * Update roomPlayerInfoMap from server
     */
    public void updateRoomInfoMap() {
        try {
            roomPlayerInfoMap = serverServantStub.getUserInRoom(roomNumber);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }


    /**
     * Join room successful
     */
    public void renderRoomPage() {
        if (this.menuController != null) {
            try {
                this.menuController.loadRoom(isHost, roomPlayerInfoMap);  // render GUI to room
                mqttBroker.getMqttClient().subscribe(Constants.MQTT_TOPIC + "/" + Constants.ROOM_TOPIC + "/" + roomNumber);  // subscribe
            } catch (MqttException e) {
                e.printStackTrace();
            }
        }
    }


    /**
     * Final ranking board.
     */
    public void renderResultPage() {
        //TODO
    }

    /**
     * Clear the menu controller when entering a new scene
     */
    public void removeMenuController() {
        this.menuController = null;
    }

    /**
     * Clear the game controller when entering a new scene
     */
    public void removeGameController() {
        this.gameController = null;
    }

    /**
     * Clear the room controller when entering a new scene
     */
    public void removeRoomController() {
        this.roomController = null;
    }

    /**
     * Notify the server servant to broadcast the voting request
     */
    public void sendVoteRequest(int startRow, int startCol, String word, boolean horizontal) {
        try {
            serverServantStub.startVote(startRow, startCol, word.length(),
                    horizontal, roomNumber, word);
        } catch (RemoteException e) {
            e.printStackTrace();
        }

    }

    public void sendPlacedLetter(int insertRow, int insertCol, String insertedLetter){
        try {
            serverServantStub.placeLetter(insertRow, insertCol, insertedLetter, this.roomNumber);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    /**
     * Send pass request to server
     */
    public void pass(){
        try {
            serverServantStub.passTurn(roomNumber);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    /**
     * Tell UI controller to render pass message if not current player and move to next turn
     */
    public void passResponse() {
        if(!isMyTurn()){
            this.gameController.passMsg(false);
        }
        this.nextTurn();
    }

    /**
     * End the game and display final result
     */
    public void endGame(){
        gameController.renderResultPage(null, isHost);
    }

    public void noWord() {
        try {
            serverServantStub.noWord(roomNumber);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    /**
     * Other players did not choose a word
     */
    public void noWordResponse(){
        if(!isMyTurn()){
            this.gameController.passMsg(true);
        }
        this.nextTurn();
    }


    public void nextTurn(){
        if(currTurn == roomPlayerInfoMap.keySet().size()){
            currTurn = 1;
        }else{
            currTurn++;
        }
        this.gameController.renderNext();
    }


    public Boolean isValidWord(Boolean isWord) {
        return true;
    }


    public void vote(String word) {
        System.out.println("Lets vote: " + word);


        this.gameController.voteMsg(word);
//
//
//        try {
//            serverServantStub.vote(this.username, result, this.roomNumber);
//        } catch (RemoteException e) {
//            e.printStackTrace();
//        }
    }

    /**
     * Render Vote Result Dialog on UI and update score if neccessary
     */
    public void renderVoteResult(boolean isWord, int score){
        this.gameController.voteResultMsg(isWord, score);
        if(isWord){
            this.gameController.updateScore(new ArrayList<>(roomPlayerInfoMap.keySet()).get(currTurn-1), score);
        }
        this.nextTurn();
    }

    /**
     * Synchronize selected word and modified cell
     * when the vote is passed by all players
     */
//    public void synchronizeGameBoard(int startRow, int startCol, int length, boolean horizontal, int insertRow,
//                                     int insertCol, String insertedLetter){
//        this.gameController.highlightChosenWord(startRow, startCol, length, horizontal);
//    }


    /**
     * Synchronize the letter placement by another player
     */
    public void synchronizePlacedLetter(int insertRow, int insertCol, String insertedLetter){
        this.gameController.renderPlacedLetter(insertRow, insertCol, insertedLetter);
    }


    public void yesVote(String word){
        try {
            serverServantStub.vote(this.username,true, this.roomNumber);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
//        renderVoteResult(true, 111);
    }

    public void noVote(){
        try {
            serverServantStub.vote(this.username,false, this.roomNumber);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
//        renderVoteResult(false, 0);
    }

    /**
     * Tell server to remove the user from playerPoll.
     */
    public void logout() {
        try {
            serverServantStub.removeFromPlayerPool(username);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    /**
     * Tell server that player left room
     */
    public void leaveRoom() {
        try {
            mqttBroker.getMqttClient().unsubscribe(Constants.MQTT_TOPIC + "/" + Constants.ROOM_TOPIC + "/" + Integer.toString(roomNumber));  // subscribe
            serverServantStub.leaveRoom(username, isHost, roomNumber);  // notify server
//            roomPlayerNames = new ArrayList<>();  // set to empty
            roomPlayerInfoMap = new HashMap<>();

            if(this.roomController!=null) {   // leave in room scene
                Platform.runLater(() -> {
                    this.roomController.fadeOut("Menu");
                });
            } else {
                Platform.runLater(()->{
                    this.gameController.fadeOut("Menu");
                });
            }
            isHost = false;
            roomNumber = Constants.NOT_IN_ROOM_ID;
        } catch (RemoteException | MqttException e) {
            e.printStackTrace();
        }
    }

    /**
     * Get room player info map
     */
    public HashMap<String, ArrayList<Integer>> getRoomInfoMap(){
        return this.roomPlayerInfoMap;
    }

    /**
     * Check if the players' turn
     */
    public boolean isMyTurn(){
        return (new ArrayList<>(roomPlayerInfoMap.keySet()).get(this.currTurn-1).equals(this.username));
    }

    /**
     * Get current turn player
     */
    public String getCurrTurnPlayer(){
        return this.currTurn + " - " + new ArrayList<>(roomPlayerInfoMap.keySet()).get(this.currTurn-1);
    }
}
