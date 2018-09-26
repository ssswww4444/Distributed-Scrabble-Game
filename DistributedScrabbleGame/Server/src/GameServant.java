import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

public class GameServant extends UnicastRemoteObject implements GameInterface {

    /**
     * Constructor
     */
    public GameServant() throws RemoteException {

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

    }

    /**
     * Insert a letter to the board
     */
    public void insertLetter() {

    }

    /**
     * Vote for the word highlighted
     */
    public void vote(boolean agree) {

    }


}
