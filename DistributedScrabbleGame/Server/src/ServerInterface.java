import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.ArrayList;

public interface ServerInterface extends Remote {
    void startVote(int startI, int startJ, int length, boolean horizontal) throws RemoteException;
    void passTurn() throws RemoteException;
    void insertLetter(int x, int y, char letter) throws RemoteException;  // ps: should check if cell empty at client GUI
    void vote(String username, boolean agree) throws RemoteException;
    void leaveGame(String username) throws RemoteException;
    void addTOPlayerPool(String username) throws RemoteException;
    ArrayList<String> getPlayerPool()throws RemoteException;
}