import org.eclipse.paho.client.mqttv3.MqttClient;

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

    //private static String clientID;


    /**
     * Create game client.
     * 1. look up the registry
     * 1. Subscribe Server topic and wait for messages
     * 2. Add the current player to server playerPool via RMI
     */
    public GameClient(String username) throws Exception {

        // get the RMI stub.
        getServerRegistry();

        if (username.equals("ERROR")) {
            throw new Exception("Error");
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

    public String getUsername() {
        return this.username;
    }

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

        /*Random r = new Random();
        int n = r.nextInt(9);
        if(n>=0);
        if(n>=1) players.add(new PlayerModel("dumb_user1", "Room102"));
        if(n>=2) players.add(new PlayerModel("dumb_user2", "Available"));
        if(n>=3) players.add(new PlayerModel("Kuang Laoshi", "Available"));
        if(n>=4) players.add(new PlayerModel("Man Laoshi", "Room102"));
        if(n>=5) players.add(new PlayerModel("dumb_user3", "Room219"));
        if(n>=6) players.add(new PlayerModel("dumb_user4", "Available"));
        if(n>=7) players.add(new PlayerModel("Will Laoshi", "Room219"));
        if(n>=8) players.add(new PlayerModel("dumb_user5", "Room102"));*/
        return players;
    }

    public void renderPlayerList() {
        System.out.println("try to render. ");
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
        this.roomNumber = 666;
    }

    public int getRoomNumber() {
        return this.roomNumber;
    }

    public ArrayList<String> getAvailablePlayers() {
        ArrayList<String> players = new ArrayList<>();

        Random r = new Random();
        int n = r.nextInt(6);
        if (n >= 0) ;
        if (n >= 1) players.add("dumb_user2");
        if (n >= 2) players.add("Kuang Laoshi");
        if (n >= 3) players.add("dumb_user4");
        if (n >= 4) players.add("Will Laoshi");
        if (n >= 5) players.add("dumb_user5");
        return players;
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
