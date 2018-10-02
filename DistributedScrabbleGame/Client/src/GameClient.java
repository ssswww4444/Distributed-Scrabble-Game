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

    private ServerInterface serverServantStub;

    public String getUsername() {
        return this.username;
    }

    public int getRoomNumber() {
        return this.roomNumber;
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
        } else {
            //clientID = MqttClient.generateClientId();
            this.username = username;
            MqttBroker mqttBroker = new MqttBroker(Constants.SERVER_TOPIC, username, this);
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
                        playerModels.add(new PlayerModel(player, "Available"));
                    }
                }
                menuController.updatePlayerList(playerModels);
            }
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }


    public void createRoom() {
        try {
            this.roomNumber = serverServantStub.addRoom();
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }


    public void newGame() {
    }

    public void invite(String username) {
        Random r = new Random();
        int n = r.nextInt(10);
        if (n > 7) {
            roomController.replyInvitation(username, false);
        } else {
            roomController.replyInvitation(username, true);
        }
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

    public void sendVoteRequest(String word) {

        if (word.equals("HAPPY")) {
            this.gameController.voteResponse(false);
        } else {
            this.gameController.voteResponse(true);
        }
    }

    public void pass() {

    }

    public void noWord() {

    }

}
