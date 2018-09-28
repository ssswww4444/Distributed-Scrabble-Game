import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.MqttPersistenceException;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

public class GameClient {
    private static GameInterface gameServantStub;

    public static final String broker = "tcp://localhost:1883";
    public static final String topic1 = "mqtt/test";
    private String content = "Let's vote!";


    private static MqttClient client;

    public static void main(String[] args) {
        GameClient gc = new GameClient();
        gc.start();


//        getServerRegistry();
//        try {
//            gameServantStub.vote("p1", true);
//        } catch (RemoteException e) {
//            e.printStackTrace();
//        }
    }




    private void start() {
        try{

            // create Mqtt client
            client = new MqttClient("tcp://localhost:1883", MqttClient.generateClientId());


            // set up callback
            client.setCallback( new SimpleMqttCallback() );

            // connect to Mqtt broker
            client.connect();

            // subscribe from a topic
            client.subscribe(topic1);

            System.out.println("Client connected?: " + client.isConnected());


            // publish to a topic
            sendMessage(topic1, content);


        }catch (Exception e){
            System.err.println("Mqtt Client exception: " + e.toString());
            e.printStackTrace();
        }
    }

    /**
     *  Send message to subscriber based on topic
     * */
    private static void sendMessage(String topic, String s) {

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
