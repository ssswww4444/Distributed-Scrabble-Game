import org.eclipse.paho.client.mqttv3.MqttClient;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;

public class GameServer{
    private static final String serverTopic = "mqtt/server";
    private static final String testTopic = "mqtt/room1";
    private static String clientID  = null;
    public  static ArrayList<Player> playerPool;

    private static MqttClient client;
    private String message;


    /**
     * Constructor
     * */
    public GameServer() {
        playerPool = mockPlayers();
        clientID   = "gameServer";
    }



    public static void main(String[] args){
        GameServer server = new GameServer();
        int roomId = 1;
        Game game = new Game(playerPool, roomId);

        GameServer gameServer = new GameServer();
        MqttBroker mqttBroker = new MqttBroker(serverTopic, clientID, game);


        mqttBroker.notify(serverTopic, "Server Ready");

        showPlayerPool();

//        bindServerRegistry(game);

    }





    /**
     * Initiate a game servant and bind the GameInterface to registry.
     * */
    private static void bindServerRegistry(Game game) {
        try {
            GameServerServant gs = new GameServerServant(game);
            GameInterface stub = (GameInterface) UnicastRemoteObject.exportObject(gs, 0);
            Registry registry = LocateRegistry.getRegistry();
            registry.bind("GameInterface", stub);
            System.err.println("Game Server ready");
        } catch (Exception e) {
            System.err.println("Server exception: " + e.toString());
            e.printStackTrace();
        }
    }

    /**
     * A player can login with its username
     */
    public static Boolean login(String username) {
        boolean result = true; // change later based on the allowance from GUI
        if(result){
            Player p = new Player(username);
            playerPool.add(p);
        }
        return result;
    }


    public static void showPlayerPool(){
        for(int i=0; i<playerPool.size(); i++){
            System.out.println(playerPool.get(i).getUsername());
        }

    }

    private static ArrayList<Player> mockPlayers(){
        ArrayList<Player> players = new ArrayList<>();
        Player p1 = new Player("p1");
        Player p2 = new Player("p2");
        Player p3 = new Player("p3");
        players.add(p1);
        players.add(p2);
        players.add(p3);
        return players;
    }


}
