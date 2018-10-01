import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.ArrayList;

public interface GameInterface extends Remote {
    void startVote(int startI, int startJ, int length, boolean horizontal) throws RemoteException;
    void passTurn() throws RemoteException;
    void insertLetter(int x, int y, char letter) throws RemoteException;  // ps: should check if cell empty at client GUI
    void vote(String username, boolean agree) throws RemoteException;
    void leaveGame(String username) throws RemoteException;
    ArrayList<String> addPlayer(String username) throws RemoteException;
}