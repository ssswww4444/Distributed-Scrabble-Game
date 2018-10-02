import org.eclipse.paho.client.mqttv3.*;


/**
 * Connection callback will be triggered by mqttClient.connect()
 */
public class MqttBroker implements MqttCallback {

    public static final String BROKER_ADDR = "tcp://127.0.0.1:1883";

    private MqttClient mqttClient;
    private Game game;
    private GameClient gc;

    public MqttClient getMqttClient() { return mqttClient; }

    /**
     * Constructor used in Game Server (Broker at Server)
     */
    public MqttBroker(String topic, String mqttClientID) {   // mqttClientID = "gameServer"
        try {
            mqttClient = new MqttClient(BROKER_ADDR, mqttClientID);
            mqttClient.setCallback(this);
            mqttClient.connect();
            System.out.println("Client connected?: " + mqttClient.isConnected());

            mqttClient.subscribe(topic);   // game server currently subscribe to nothing: PLACEHOLDER " "
        } catch (MqttException e) {
            System.err.println("Mqtt Client exception: " + e.toString());
            e.getCause();
            e.printStackTrace();
        }
    }


    /**
     * Constructor used in Game Client (Broker at Client)
     */
    public MqttBroker(String mqttClientID, GameClient gc) {  // mqttClientID = username
        try {
            mqttClient = new MqttClient(BROKER_ADDR, mqttClientID);
            mqttClient.setCallback(this);
            mqttClient.connect();
            this.gc = gc;
            System.out.println("Client connected?: " + mqttClient.isConnected());

            // all clients must subscribe to SERVER_TOPIC
            mqttClient.subscribe(Constants.MQTT_TOPIC + "/" + Constants.SERVER_TOPIC);

            // subscribe to its client topic (username) for one-to-one messages
            mqttClient.subscribe(Constants.MQTT_TOPIC + "/" + Constants.CLIENT_TOPIC + mqttClientID);

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
     * topic types:
     * 1. MQTT/server
     * 2. MQTT/room/roomID
     * 3. MQTT/client/username
     */
    @Override
    public void messageArrived(String topic, MqttMessage message) {
        System.out.println("Start of Message: ---> ");
        System.out.println("The topic is : " + topic);
        System.out.println("The content is : " + new String(message.getPayload()));

        String[] topics = topic.split("/") ; // always starts with mqtt

        switch(topics[1]) {
            case Constants.SERVER_TOPIC:
                serverMessageHandler(message);
                break;
            case Constants.ROOM_TOPIC:
                roomMessageHandler(Integer.parseInt(topics[2]), message);
                break;
            case Constants.CLIENT_TOPIC:
                clientMessageHandler(topics[2], message);
                break;
        }

        System.out.println("End of Message: <---" + "\n");
    }

    /**
     * Handle the message directly from server (all clients receive this type of message)
     */
    private void serverMessageHandler(MqttMessage message) {
        String[] cmd = message.toString().split(";");
        switch(cmd[0]) {
            case Constants.LOGIN:  // new username login -> update list
                System.out.println(cmd[1] + " logged in. ");
                gc.renderPlayerList();
                break;
            case Constants.INVITATION: // receive invitation (invitation type: all clients)
                System.out.println(cmd[1] + "invite you to join Room " + cmd[3]);
//                gc.renderRoomPage(Integer.parseInt(cmd[2]));
                break;
        }
    }

    /**
     * Handle the message which all people in this room received
     */
    private void roomMessageHandler(int roomNumber, MqttMessage message) {
        String[] cmd = message.toString().split(";");
        switch (cmd[0]){  // action types
            case Constants.GAME_START:
                gc.startGame();
                break;
            case Constants.VOTE:
                gc.vote();
                break;
            case Constants.GAME_OVER:
                gc.renderResultPage();
                break;
            case Constants.JOIN_ROOM:  // new player joined room
                gc.playerJoinedRoom(cmd[1]);
        }
    }

    /**
     * Direct message (only this client received)
     */
    private void clientMessageHandler(String username, MqttMessage message) {
        String[] cmd = message.toString().split(";");
        switch (cmd[0]){  // action types
            case Constants.INVITATION:
                System.out.println(cmd[1] + "invite you to join Room " + cmd[2]);
                gc.receiveInvitation(cmd[1], Integer.parseInt(cmd[2]));
                break;
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
