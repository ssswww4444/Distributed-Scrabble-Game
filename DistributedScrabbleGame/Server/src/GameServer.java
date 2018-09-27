import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;

public class GameServer{
    private ArrayList<ClientInterface> clientList;

    public static void main(String[] args){

        //getClientRegistry();

        ArrayList<Player> players = mockPlayers();
        int roomId = 1;
        Game game = new Game(players, roomId);
        bindServerRegistry(game);


    }


    /**
     * Initiate a game servant and bind the GameInterface to registry.
     * */
    private static void bindServerRegistry(Game game) {
        try {
            GameServerServant gs = new GameServerServant(game);
            GameInterface stub = (GameInterface) UnicastRemoteObject.exportObject(gs, 0);
            Registry registry = LocateRegistry.getRegistry();
            registry.bind("GameInterface", stub);
            System.err.println("Game Server ready");
        } catch (Exception e) {
            System.err.println("Server exception: " + e.toString());
            e.printStackTrace();
        }
    }


    /**
     * Get the remote client servant.
     * */
//    private static void getClientRegistry() {
//        try {
//            Registry registry = LocateRegistry.getRegistry("localhost");
//            clientServantStub = (ClientInterface) registry.lookup("ClientInterface");
//        } catch (Exception e) {
//            System.err.println("Client exception: " + e.toString());
//            e.printStackTrace();
//        }
//    }


    private static ArrayList<Player> mockPlayers(){
        ArrayList<Player> players = new ArrayList<>();
        Player p1 = new Player("p1", new GameClientServant());
        Player p2 = new Player("p2", new GameClientServant());
        Player p3 = new Player("p3", new GameClientServant());
        players.add(p1);
        players.add(p2);
        players.add(p3);
        return players;
    }
}
