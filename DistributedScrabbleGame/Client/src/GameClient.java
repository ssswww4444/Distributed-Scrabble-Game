import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

public class GameClient {
    private static GameInterface gameServantStub;
    public static void main(String[] args) {
        getServerRegistry();

        try {
            gameServantStub.vote("p1", true);
        } catch (RemoteException e) {
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
