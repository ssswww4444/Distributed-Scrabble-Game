import java.rmi.Remote;
import java.rmi.RemoteException;

public interface GameInterface extends Remote {
    void startVote(int startI, int startJ, int length, boolean horizontal) throws RemoteException;
    void passTurn() throws RemoteException;
    void insertLetter(int x, int y, char letter) throws RemoteException;  // ps: should check if cell empty at client GUI
    void vote(String username, boolean agree) throws RemoteException;
    void leaveGame(String username) throws RemoteException;
}