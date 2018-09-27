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
    private int voteAgreeNum = 0;
    private int voteTotalNum = 0;
    private enum GameStatus {
        INSERTING, VOTING
    }
    private GameStatus currStatus;

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

        // init turn
        turn = 1;
    }

    /**
     * Notify all players that game has started
     */
    private void notifyGameStart() {
        for (Player player: players) {
            ClientInterface clientServant = player.getClientServant();
            try {
                clientServant.notifyGameStart();
                clientServant.notifyTurn(turn);  // starts with turn = 1 (p1)
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
        currStatus = GameStatus.INSERTING; // p1 inserting letter
    }

    /**
     * Switch turn and notify all players
     */
    public void nextTurn() {
        currStatus = GameStatus.INSERTING;

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
     * Insert letter to the board at (i,j)
     * @return success or fail
     */
    public boolean insertLetter(int i, int j, Character letter) {
        Cell targetCell = board.get(i).get(j);

        if (targetCell.getLetter() != null) {  // non-empty cell
            return false;  // fail
        }

        targetCell.setLetter(letter);

        // notify
        for (Player player: players) {
            ClientInterface clientServant = player.getClientServant();
            try {
                clientServant.notifyInsertLetter(i, j, letter);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }

        return true;
    }

    /**
     * Initialise a vote and notify all clients
     */
    public void startVote(int startI, int startJ, int length, boolean horizontal) {
        currStatus = GameStatus.VOTING;
        // notify
        for (Player player: players) {
            ClientInterface clientServant = player.getClientServant();
            try {
                clientServant.notifyStartVote(startI, startJ, length, horizontal);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Get voting response from a player, and notify all, if all voted, give result: success,fail
     */
    public void vote(String username, boolean agree) {
        if (agree) {
            voteAgreeNum++;
        }
        voteTotalNum++;
        if (voteTotalNum == players.size()) {  // all voted
            if (voteAgreeNum == voteTotalNum) {  // all agree
                voteSuccess();
            } else {
                voteFail();
            }
        }
        // notify
        for (Player player: players) {
            ClientInterface clientServant = player.getClientServant();
            try {
                clientServant.notifyVote(username, agree);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    }

    private void notifyVoteResult(boolean success) {
        for (Player player: players) {
            ClientInterface clientServant = player.getClientServant();
            try {
                clientServant.notifyVoteResult(success);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    }

    private void voteSuccess() {
        // notify
        notifyVoteResult(true);

        nextTurn();

    }

    private void voteFail() {
        // notify
        notifyVoteResult(false);

        // check if game ends

        nextTurn();
    }

}
