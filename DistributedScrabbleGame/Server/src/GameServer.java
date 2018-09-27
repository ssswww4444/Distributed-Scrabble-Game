import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;

public class GameServer{
    public static void main(String[] args){
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


    private static ArrayList<Player> mockPlayers(){
        ArrayList<Player> players = new ArrayList<>();
        Player p1 = new Player("p1");
        Player p2 = new Player("p2");
        Player p3 = new Player("p3");
        players.add(p1);
        players.add(p2);
        players.add(p3);
        return players;
    }
}
