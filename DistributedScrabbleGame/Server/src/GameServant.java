import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

public class GameServant extends UnicastRemoteObject implements GameInterface {

    private Game game;

    /**
     * Constructor
     */
    public GameServant(Game game) throws RemoteException {
        this.game = game;
    }

    /**
     * Start voting
     */
    public void startVote() {

    }

    /**
     * Pass the turn
     */
    public void pass() {
        game.nextTurn();
    }

    /**
     * Insert a letter to the board at coordinate (i,j): row i, col j
     */
    public boolean insertLetter(int i, int j, char letter) {
        return game.insertLetter(i, j, letter);
    }

    /**
     * Vote for the word highlighted
     */
    public void vote(boolean agree) {

    }


}
