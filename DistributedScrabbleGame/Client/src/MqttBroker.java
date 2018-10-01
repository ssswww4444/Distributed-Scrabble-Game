import org.eclipse.paho.client.mqttv3.*;


/**
 * Connection callback will be triggered by mqttClient.connect()
 * */
public class MqttBroker implements MqttCallback {

    public static final String broker_addr = "tcp://127.0.0.1:1883";

    private MqttClient mqttClient;

    private Game game;

    /**
     * Constructor used in GameClient
     * */
    public MqttBroker(String topic, String clientID) {
        try {
            mqttClient = new MqttClient(broker_addr, clientID);
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
     * Constructor used in GameServer
     * */
    public MqttBroker(String topic, String clientID, Game g) {
        this.game = g;

        try {
            mqttClient = new MqttClient(broker_addr, clientID);
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
     *  Send message to subscriber based on topic
     * */
    public void notify(String topic, String s) {
        MqttMessage message = new MqttMessage();
        message.setPayload(s.getBytes());
        try {
            mqttClient.publish(topic, message);
        } catch (MqttPersistenceException e) {
            e.printStackTrace();
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }




    @Override
    public void messageArrived(String topic, MqttMessage message) throws Exception {
        System.out.println("The topic is : " + topic);
        System.out.println("The content is : " + new String(message.getPayload()) + "\n");
        if(topic.equals("mqtt/server")){
            System.out.println(message.toString().length());
            if((message.toString().length() != 0) && message.toString().contains(";")){
                String[] cmd = message.toString().split(";");

                System.out.println(cmd[0]);
                if(cmd[1].equals("Login")){
                    System.out.println("Newly added client: " + cmd[2]);
                    GameServer.login(cmd[2]);
                    GameServer.showPlayerPool();

                }
                if(cmd[1].equals("Vote")){
//                game.startVote();
                }
            }
        }
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
