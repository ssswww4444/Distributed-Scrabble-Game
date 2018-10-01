import org.eclipse.paho.client.mqttv3.MqttClient;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

public class GameClient {
    private static GameInterface gameServantStub;

    public static final String serverTopic = "mqtt/server";
    public static final String testTopic = "mqtt/room1";

    private static String content = "First test content!";
    private static String clientID;


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
        } catch (Exception e) {
            System.err.println("Client exception: " + e.toString());
            e.printStackTrace();
        }
    }
}
