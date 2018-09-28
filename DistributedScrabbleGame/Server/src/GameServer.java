import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.MqttPersistenceException;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;

public class GameServer{
    public static final String broker = "tcp://localhost:1883";
    public static final String topic1 = "mqtt/test";
    private static final String clientID = "game server";

    private static MqttClient client;
    private String message;
    /**
     * Constructor
     * */
    public GameServer() throws MqttException {

        client = new MqttClient("tcp://localhost:1883", clientID);
    }



    public static void main(String[] args){
        ArrayList<Player> players = mockPlayers();
        int roomId = 1;

        GameServer server = null;

        try {
            server = new GameServer();
            server.connect();
        } catch (MqttException e) {
            e.printStackTrace();
        }

        server.message = "Hello Client";
        server.sendMessage(topic1, server.message);


        Game game = new Game(players, roomId);
//        bindServerRegistry(game);
    }




    /**
     * Connect server to the Mqtt broker
     * */
    private void connect() {
        try {
            client.setCallback( new SimpleMqttCallback() );
            client.connect();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }


    /**
     *  Send message to subscriber based on topic
     * */
    private void sendMessage(String topic, String s) {

        MqttMessage message = new MqttMessage();
        message.setPayload(s.getBytes());
        try {
            client.publish(topic, message);
        } catch (MqttPersistenceException e) {
            e.printStackTrace();
        } catch (MqttException e) {
            e.printStackTrace();
        }

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
