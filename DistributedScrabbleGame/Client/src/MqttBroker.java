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
    public MqttBroker(String topic, String mqttClientID) {
        try {
            mqttClient = new MqttClient(BROKER_ADDR, mqttClientID);
            mqttClient.setCallback(this);
            mqttClient.connect();
            System.out.println("Client connected?: " + mqttClient.isConnected());

            mqttClient.subscribe(topic);   // client subscribe to its ID
        } catch (MqttException e) {
            System.err.println("Mqtt Client exception: " + e.toString());
            e.getCause();
            e.printStackTrace();
        }
    }


    /**
     * Constructor used in Game Client
     */
    public MqttBroker(String topic, String mqttClientID, GameClient gc) {
        try {
            mqttClient = new MqttClient(BROKER_ADDR, mqttClientID);
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
    public void notify(String topic, String message) {
        MqttMessage mqttMessage = new MqttMessage();
        mqttMessage.setPayload(message.getBytes());
        try {
            mqttClient.publish(topic, mqttMessage);
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

        if (topic.equals(Constants.SERVER_TOPIC)) {
            // Messages for server use
            if ((message.toString().length() != 0) && message.toString().contains(";")) {
                String[] cmd = message.toString().split(";");

                switch(cmd[1]){
                    case Constants.LOGIN:
                        System.out.println(cmd[2] + " logged in. ");
                        gc.renderPlayerList();
                        break;
                    case Constants.INVITATION: // inviteAll
                        System.out.println(cmd[2] + "invite you to join " + cmd[3]);
                        gc.renderRoomPage(Integer.parseInt(cmd[3]));
                }


            }
        } else if (topic.split(" ")[0].equals(Constants.ROOM)) {
            // Messages for a room
            int roomNum = Integer.parseInt(topic.split(" ")[1]);
            switch (message.toString()){
                case Constants.GAME_START:
                    gc.startGame();
                    break;
                case Constants.VOTE:
                    gc.vote();
                    break;
                case Constants.GAME_OVER:
                    gc.renderResultPage();
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
