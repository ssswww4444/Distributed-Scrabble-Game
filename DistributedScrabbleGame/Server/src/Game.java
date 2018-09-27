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
    private int voteAgreeNum;
    private int voteTotalNum;
    private int currWordLength;  // current voting word
    private enum GameStatus {
        INSERTING, VOTING
    }
    private GameStatus currStatus;
    private boolean hasInserted;  // insertion made in this turn
    private int passCount;

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

        // init pass count
        passCount = 0;

        // init turn 1
        turn = 1;
        hasInserted = false;

    }

    /**
     * Notify all players that game has started
     */
    private void notifyGameStart() {
        for (Player player: players) {
            ClientInterface clientServant = player.getClientServant();
            Player currPlayer = players.get(turn - 1);  // will be p1
            try {
                clientServant.notifyGameStart();
                clientServant.notifyTurn(currPlayer.getUsername());  // starts with turn = 1 (p1)
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
        currStatus = GameStatus.INSERTING; // p1 inserting letter
    }

    /**
     * Current player passed this turn
     */
    public void passTurn() {
        // notify
        for (Player player: players) {
            ClientInterface clientServant = player.getClientServant();
            Player currPlayer = players.get(turn - 1);
            try {
                clientServant.notifyTurnPassed(currPlayer.getUsername());
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }

        // switch turn
        nextTurn();
    }

    /**
     * Switch turn and notify all players
     */
    public void nextTurn() {

        if (!hasInserted) {   // currPlayer passed
            passCount += 1;
        } else {
            passCount = 0;  // currPlayer not passed
        }

        if (passCount == players.size()) {  // all players passed
            endGame();
        }

        currStatus = GameStatus.INSERTING;
        hasInserted = false;  // reset boolean

        // switch turn
        turn += 1;
        if (turn > players.size()) {
            turn = 1;
        }

        // notify
        for (Player player: players) {
            ClientInterface clientServant = player.getClientServant();
            Player currPlayer = players.get(turn - 1);
            try {
                clientServant.notifyTurn(currPlayer.getUsername());
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

        hasInserted = true;

        // get cell
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

        // init vote
        currStatus = GameStatus.VOTING;
        voteAgreeNum = 0;
        voteTotalNum = 0;

        // notify
        for (Player player: players) {
            ClientInterface clientServant = player.getClientServant();
            currWordLength = length;
            try {
                clientServant.notifyStartVote(startI, startJ, length, horizontal);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Get voting response from a player, and notify all, if all voted, notify result: success,fail
     */
    public void vote(String username, boolean agree) {

        // update vote response
        if (agree) {
            voteAgreeNum++;
        }
        voteTotalNum++;

        // check if all voted
        if (voteTotalNum == players.size()) {
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

    /**
     * Notify all clients the voting results
     * @param success
     */
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

    /**
     * If success, gives score to current player
     */
    private void voteSuccess() {
        // notify voting result
        notifyVoteResult(true);

        // increment score
        Player currPlayer = players.get(turn - 1);
        Integer newScore = scores.get(currPlayer) + currWordLength;
        scores.put(currPlayer, newScore);

        // notify score change
        for (Player player: players) {
            ClientInterface clientServant = player.getClientServant();
            try {
                clientServant.notifyScoreChange(currPlayer.getUsername(), newScore);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }

        nextTurn();

    }

    /**
     * If failed, notify and switch turn
     */
    private void voteFail() {
        // notify
        notifyVoteResult(false);

        nextTurn();
    }

    public void leaveGame(String username) {
        // notify who left
        for (Player player: players) {
            ClientInterface clientServant = player.getClientServant();
            try {
                clientServant.notifyLeaveGame(username);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }

        // end game
        endGame();
    }

    /**
     * End the game
     */
    private void endGame() {
        // notify game has ended
        for (Player player: players) {
            ClientInterface clientServant = player.getClientServant();
            try {
                clientServant.notifyEndGame();
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    }

}
