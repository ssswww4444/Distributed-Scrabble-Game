import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

public class GameServer {
    private static final String PLACEHOLDER = " ";
    private static String clientID = "gameServer";

    public static void main(String[] args) {

        // there is no need for server to subscribe any topic at current stage.
        MqttBroker mqttBroker = new MqttBroker(PLACEHOLDER, clientID);

        bindServerRegistry(mqttBroker);
    }


    /**
     * Initiate a game servant and bind the ServerInterface to registry.
     */
    private static void bindServerRegistry(MqttBroker mqttBroker) {
        try {
            ServerServant gs = new ServerServant(mqttBroker);
            //ServerInterface stub = (ServerInterface) UnicastRemoteObject.exportObject(gs, 0);
            Registry registry = LocateRegistry.getRegistry();
            registry.rebind("ServerInterface", gs);
            System.err.println("Game Server ready");
        } catch (Exception e) {
            System.err.println("Server exception: " + e.toString());
            e.printStackTrace();
        }
    }
}
