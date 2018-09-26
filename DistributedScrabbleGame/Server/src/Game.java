import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.ArrayList;
import java.util.HashMap;

public class Game {

    private int turn;
    private ArrayList<ArrayList<Cell>> board;
    private static final int boardHeight = 20;
    private static final int boardWidth = 20;
    private ArrayList<Player> players;
    private HashMap<Player, Integer> scores;

    /**
     * Constructor
     */
    public Game(ArrayList<Player> players, int roomID) {
        this.players = players;
        initGame();
        registerGame(roomID);   // create & bind game servant
        notifyGameStart();
    }

    /**
     * Bind the game to registry for clients lookup
     */
    private void registerGame(int roomID) {
        try {
            Registry registry = LocateRegistry.getRegistry();  // get registry of the server
            registry.rebind(Integer.toString(roomID), new GameServant(this));   // one servant for each game
        } catch(RemoteException e) {
            e.printStackTrace();
        }
    }

    /**
     * Initialise a game
     */
    private void initGame() {
        // init board
        for (int i=0; i < boardHeight; i++) {
            ArrayList<Cell> row = new ArrayList<>();
            for (int j=0; j < boardWidth; j++) {
                Cell cell = new Cell();
                row.add(cell);
            }
            board.add(row);
        }

        // init scores
        for (Player player: players) {
            scores.put(player, 0);
        }
    }

    /**
     * Notify all players that game has started
     */
    private void notifyGameStart() {
        for (Player player: players) {
            ClientInterface clientServant = player.getClientServant();
            try {
                clientServant.notifyGameStart();
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Switch turn and notify all players
     */
    public void nextTurn() {
        // switch turn
        turn += 1;
        if (turn > players.size()) {
            turn = 1;
        }

        // notify
        for (Player player: players) {
            ClientInterface clientServant = player.getClientServant();
            try {
                clientServant.notifyTurn(turn);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     *
     * @param i
     * @param j
     * @param letter
     * @return success or failed
     */
    public boolean insertLetter(int i, int j, Character letter) {
        Cell targetCell = board.get(i).get(j);

        if (targetCell.getLetter() != null) {  // non-empty cell
            return false;  // failed
        }

        targetCell.setLetter(letter);
        return true;
    }


}
