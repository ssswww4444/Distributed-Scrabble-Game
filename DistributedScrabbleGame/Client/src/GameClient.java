import org.eclipse.paho.client.mqttv3.MqttException;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.ArrayList;
import java.util.Random;

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
        } else {
            //clientID = MqttClient.generateClientId();  // not necessary
            this.username = username;
            mqttBroker = new MqttBroker(username, this);
            System.out.println("1");
            serverServantStub.addTOPlayerPool(username);
            System.out.println("2");
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
                players.add(new PlayerModel(s, "Available"));
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
            this.roomPlayerNames = new ArrayList<String>();  // empty
            renderRoomPage(true, roomNumber);
        } catch (RemoteException e) {
            e.printStackTrace();
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }

    /**
     * Receive notification that new player joined room
     */
    public void playerJoinedRoom(String username) {
        roomPlayerNames.add(username); // update list
        roomController.joinRoom(username);  // update UI
    }


    /**
     * Create a new room, room ID is assigned by server.
     */
    public void startGame() {
        try {
            serverServantStub.startNewGame(roomPlayerNames, roomNumber);

        } catch (RemoteException e) {
            e.printStackTrace();
        }
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

    public void receiveInvitation(String username, int roomNumber){
        System.out.println("received invitation from " + username + " to room " + roomNumber);
        this.menuController.invitationMsg(username, roomNumber);
    }

    public void acceptInvitation(int roomNumber){
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
    public void renderRoomPage(boolean isHost, int roomNumber){
        if(this.menuController!=null){
            this.roomNumber = roomNumber;
            this.roomPlayerNames.add(this.username);  // add himself
            this.menuController.loadRoom(isHost, roomPlayerNames);  // render GUI to room
            try {
                mqttBroker.getMqttClient().subscribe("mqtt/room/" + Integer.toString(roomNumber));  // subscribe
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


    public void removeMenuController(){
        this.menuController = null;
    }

    public void sendVoteRequest(String word) {

        /*if (word.equals("HAPPY")) {
            this.gameController.voteResponse(false);
        } else {
            this.gameController.voteResponse(true);
        }*/
    }

    public void pass() {

    }

    public void noWord() {

    }

    public void vote() {

    }


}
