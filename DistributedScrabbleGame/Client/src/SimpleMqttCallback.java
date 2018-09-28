import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttMessage;


public class SimpleMqttCallback implements MqttCallback {

    @Override
    public void connectionLost(Throwable cause) {
        System.err.println("MQTT connection broken");
    }

    @Override
    public void messageArrived(String topic, MqttMessage message) throws Exception {
        System.out.println("The topic is : " + topic);
        System.out.println("The content is : " + new String(message.getPayload()) + "\n");
    }

    @Override
    public void deliveryComplete(IMqttDeliveryToken token) {
//       System.out.println("deliveryComplete---------" + token.isComplete());
    }

}