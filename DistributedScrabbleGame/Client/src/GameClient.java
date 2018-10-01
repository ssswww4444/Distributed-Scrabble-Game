import org.eclipse.paho.client.mqttv3.MqttClient;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.ArrayList;
import java.util.Random;

public class GameClient {


    private String username;
    private int roomNumber;
    private RoomController roomController;
    private GameController gameController;


    private static GameInterface gameServantStub;

    public static final String serverTopic = "mqtt/server";
    public static final String testTopic = "mqtt/room1";

    private static String content = "First test content!";
    private static String clientID;


    public GameClient(String username) throws Exception{

        if(username.equals("ERROR")){
            throw new Exception("Error");
        }else{
            this.username = username;
        }
        /*try {
            Registry registry = LocateRegistry.getRegistry(null);
            ServerInterface stub = (ServerInterface) registry.lookup("ServerInterface");
            stub.printMsg();
            } catch (Exception e) {
            System.err.println("Client exception: " + e.toString());
            e.printStackTrace();
        }*/
    }


    /**
     * Constructor
     * */
    public GameClient() {
        clientID   = MqttClient.generateClientId().toString();;
    }


    public static void main(String[] args) {
        GameClient gameClient = new GameClient();

        MqttBroker mqttBroker = new MqttBroker(testTopic, clientID);

        mqttBroker.notify(serverTopic, serverTopic + ";" + "Login" + ";" + clientID);
        mqttBroker.notify(testTopic, "Hello room mates, I am client: " + clientID);

//        getServerRegistry();
//        try {
//            gameServantStub.vote("p1", true);
//        } catch (RemoteException e) {
//            e.printStackTrace();
//        }
    }

    /**
     * Get the game server remote servant.
     * */
    private static void getServerRegistry() {
        try {
            Registry registry = LocateRegistry.getRegistry("localhost");
            gameServantStub = (GameInterface) registry.lookup("GameInterface");


    public String getUsername(){
        return this.username;
    }

    public ArrayList<PlayerModel> getPlayerList(){
        ArrayList<PlayerModel> players = new ArrayList<>();

        Random r = new Random();
        int n = r.nextInt(9);
        if(n>=0);
        if(n>=1) players.add(new PlayerModel("dumb_user1", "Room102"));
        if(n>=2) players.add(new PlayerModel("dumb_user2", "Available"));
        if(n>=3) players.add(new PlayerModel("Kuang Laoshi", "Available"));
        if(n>=4) players.add(new PlayerModel("Man Laoshi", "Room102"));
        if(n>=5) players.add(new PlayerModel("dumb_user3", "Room219"));
        if(n>=6) players.add(new PlayerModel("dumb_user4", "Available"));
        if(n>=7) players.add(new PlayerModel("Will Laoshi", "Room219"));
        if(n>=8) players.add(new PlayerModel("dumb_user5", "Room102"));
        return players;
    }

    public void createRoom(){
        this.roomNumber = 666;
    }

    public int getRoomNumber(){
        return this.roomNumber;
    }

    public ArrayList<String> getAvailablePlayers(){
        ArrayList<String> players = new ArrayList<>();

        Random r = new Random();
        int n = r.nextInt(6);
        if(n>=0);
        if(n>=1) players.add("dumb_user2");
        if(n>=2) players.add("Kuang Laoshi");
        if(n>=3) players.add("dumb_user4");
        if(n>=4) players.add("Will Laoshi");
        if(n>=5) players.add("dumb_user5");
        return players;
    }

    public void newGame(){}

    public void invite(String username){
        Random r = new Random();
        int n = r.nextInt(10);
        if(n > 7){
            roomController.replyInvitation(username, false);
        }else{
            roomController.replyInvitation(username, true);
        }
    }

    public void setRoomController(RoomController controller){
        this.roomController = controller;
    }

    public void setGameController(GameController controller){
        this.gameController = controller;
    }

    public void sendVoteRequest(String word){

        if(word.equals("HAPPY")){
            this.gameController.voteResponse(false);
        }else{
            this.gameController.voteResponse(true);
        }
    }

    public void pass(){

    }

    public void noWord(){

    }

}
