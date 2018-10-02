import org.eclipse.paho.client.mqttv3.*;


/**
 * Connection callback will be triggered by mqttClient.connect()
 */
public class MqttBroker implements MqttCallback {

    public static final String BROKER_ADDR = "tcp://127.0.0.1:1883";

    private MqttClient mqttClient;
    private Game game;
    private GameClient gc;


    /**
     * Constructor used in Game Server
     */
    public MqttBroker(String topic, String clientID) {
        try {
            mqttClient = new MqttClient(BROKER_ADDR, clientID);
            mqttClient.setCallback(this);
            mqttClient.connect();
            System.out.println("Client connected?: " + mqttClient.isConnected());

            mqttClient.subscribe(topic);
        } catch (MqttException e) {
            System.err.println("Mqtt Client exception: " + e.toString());
            e.getCause();
            e.printStackTrace();
        }
    }


    /**
     * Constructor used in Game Server
     */
    public MqttBroker(String topic, String clientID, GameClient gc) {
        try {
            mqttClient = new MqttClient(BROKER_ADDR, clientID);
            mqttClient.setCallback(this);
            mqttClient.connect();
            this.gc = gc;
            System.out.println("Client connected?: " + mqttClient.isConnected());

            mqttClient.subscribe(topic);
        } catch (MqttException e) {
            System.err.println("Mqtt Client exception: " + e.toString());
            e.getCause();
            e.printStackTrace();
        }
    }


    /**
     * Send message to subscriber based on topic
     */
    public void notify(String topic, String s) {
        MqttMessage message = new MqttMessage();
        message.setPayload(s.getBytes());
        try {
            mqttClient.publish(topic, message);
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }


    /**
     * Note: This method is ONLY used by GameClient.
     * Client -> Server is using RMI.
     */
    @Override
    public void messageArrived(String topic, MqttMessage message) {
        System.out.println("Start of Message: ---> ");
        System.out.println("The topic is : " + topic);
        System.out.println("The content is : " + new String(message.getPayload()));

        if (topic.equals("mqtt/server")) {
            if ((message.toString().length() != 0) && message.toString().contains(";")) {
                String[] cmd = message.toString().split(";");

                System.out.println(cmd[0]);
                if (cmd[1].equals("Login")) {
                    System.out.println("Newly added client: " + cmd[2]);
                    gc.renderPlayerList();
                }
                if (cmd[1].equals("Vote")) {
//                game.startVote();
                }


            }
        }
        System.out.println("End of Message: <---" + "\n");
    }


    @Override
    public void connectionLost(Throwable e) {
        System.err.println("MQTT connection broken");
        e.getCause();
        e.printStackTrace();
    }


    @Override
    public void deliveryComplete(IMqttDeliveryToken token) {
//       System.out.println("deliveryComplete---------" + token.isComplete());
    }
}
