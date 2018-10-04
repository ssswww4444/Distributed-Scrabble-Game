import javafx.application.Platform;
import org.eclipse.paho.client.mqttv3.MqttException;

import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.ArrayList;

public class GameClient {
    private String username;
    private int roomNumber;
    private MenuController menuController;
    private RoomController roomController;
    private GameController gameController;
    private ArrayList<String> roomPlayerNames;
    private MqttBroker mqttBroker;

    private ServerInterface serverServantStub;

    public String getUsername() {
        return this.username;
    }

    public int getRoomNumber() {
        return this.roomNumber;
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

    /**
     * Create game client.
     * 1. look up the registry
     * 1. Subscribe Server topic and wait for messages
     * 2. Add the current player to server playerPool via RMI
     */
    public GameClient(String username) throws Exception {

        // get the RMI stub.
        getServerRegistry();

        if (serverServantStub.getPlayerPool().contains(username)) {
            throw new Exception("Existing user");
        } else if (username.equals("gameServer")) {  // username cannot be "gameServer" (for mqtt id purpose)
            throw new Exception("Username cannot be 'gameServer'");
        } else if (username.equals("")) {  // empty string
            throw new Exception("Username cannot be empty");
        } else {
            this.username = username;
            mqttBroker = new MqttBroker(username, this);
            serverServantStub.addTOPlayerPool(username);
        }
    }


    /**
     * Get the game server remote servant.
     */
    private void getServerRegistry() {
        try {
            Registry registry = LocateRegistry.getRegistry("localhost");
            serverServantStub = (ServerInterface) registry.lookup("ServerInterface");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    /**
     * Get all current online users.
     */
    public ArrayList<PlayerModel> getPlayerList() {
        ArrayList<PlayerModel> players = new ArrayList<>();
        try {
            ArrayList<String> playerObjects = serverServantStub.getPlayerPool();
            for (String s : playerObjects) {
                players.add(new PlayerModel(s, Constants.STATUS_AVAILABLE));
            }
        } catch (RemoteException e) {
            e.printStackTrace();
        }

        return players;
    }


    /**
     * Get all current available users. (Current implementation is just "Return all users")
     */
    public ArrayList<String> getAvailablePlayers() {
        ArrayList<String> players = new ArrayList<>();
        try {
            players = serverServantStub.getPlayerPool();
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
        try {
            players = serverServantStub.getPlayerPool();
            if (this.menuController != null) {
                ArrayList<PlayerModel> playerModels = new ArrayList<>();

                for (String player : players) {
                    if (!player.equals(this.username)) {
                        playerModels.add(new PlayerModel(player, Constants.STATUS_AVAILABLE));
                    }
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
            mqttBroker.getMqttClient().subscribe("mqtt/room/" + Integer.toString(roomNumber));  // subscribe
            this.roomPlayerNames = new ArrayList<>();  // empty
            renderRoomPage(true, roomNumber);
        } catch (RemoteException | MqttException e) {
            e.printStackTrace();
        }
    }


    /**
     * Receive notification that new player joined room
     */
    public void playerJoinedRoom(String username) {
        roomPlayerNames.add(username); // update list
        Platform.runLater(() -> {
            GameClient.this.roomController.joinRoom(username);  // update UI
        });
    }


    /**
     * Create a new room, room ID is assigned by server.
     * Host start a game and server then send broadcasts to others in the room
     */
    public void startGame() {
        try {
            serverServantStub.startNewGame(roomPlayerNames, roomNumber);

        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }


    /**
     * Note!! This is used by guests in the room, be notified to update UI to "game".
     */
    public void renderGamePage() {
        roomController.fadeOut();
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
                roomPlayerNames = serverServantStub.getUserInRoom(roomNumber);  // not including himself
                renderRoomPage(false, roomNumber);
            } else {  // failed
                menuController.displayMsg();
            }
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }


    /**
     * Join room successful
     */
    public void renderRoomPage(boolean isHost, int roomNumber) {
        if (this.menuController != null) {
            try {
                this.roomNumber = roomNumber;
                this.roomPlayerNames.add(this.username);  // add himself
                this.menuController.loadRoom(isHost, roomPlayerNames);  // render GUI to room
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


    public void removeMenuController() {
        this.menuController = null;
    }

    public void sendVoteRequest(String word) {

        /*if (word.equals("HAPPY")) {
            this.gameController.voteResponse(false);
        } else {
            this.gameController.voteResponse(true);
        }*/

        try {
            serverServantStub.startVote(0, 0, word.length(), true, roomNumber, word);
        } catch (RemoteException e) {
            e.printStackTrace();
        }

    }

    public void pass() {

    }

    public void noWord() {

    }



    public void updatePlayerScore(int score){
        try {
            serverServantStub.notifyVoteResult(this.username, score, this.roomNumber);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    public Boolean isValidWord(Boolean isWord) {
        return true;
    }


    public void vote(String word) {
        System.out.println("Lets vote: " + word);

        // remove this after connect to Voting UI
        try {
            Thread.sleep(3000);
        }catch(Exception e){
        }

        Boolean result = isValidWord(true);

        try {
            serverServantStub.vote(this.username, result, this.roomNumber);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
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
}
