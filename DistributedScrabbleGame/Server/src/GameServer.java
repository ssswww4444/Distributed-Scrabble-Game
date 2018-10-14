import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

public class GameServer {
    private static final String PLACEHOLDER = " ";
    private static String clientID = "gameServer";
    private static ServerServant serverServant;

    public static void main(String[] args) {

        // There is no need for server to subscribe any topic at current stage.
        MqttBroker mqttBroker = new MqttBroker(PLACEHOLDER, clientID);
        bindServerRegistry(mqttBroker);
        Runtime.getRuntime().addShutdownHook(new Thread() {
            public void run() {
                serverServant.serverDown();
            }
        });
    }


    /**
     * Initiate a game servant and bind the ServerInterface to registry.
     */
    private static void bindServerRegistry(MqttBroker mqttBroker) {
        try {
            serverServant = new ServerServant(mqttBroker);
            //ServerInterface stub = (ServerInterface) UnicastRemoteObject.exportObject(gs, 0);
            Registry registry = LocateRegistry.getRegistry();
            registry.rebind("ServerInterface", serverServant);
            System.err.println("Game Server ready");
        } catch (Exception e) {
            System.err.println("Server exception: " + e.toString());
            e.printStackTrace();
        }
    }
}
