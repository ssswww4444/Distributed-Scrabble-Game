import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicInteger;

public interface ServerInterface extends Remote {

    void addTOPlayerPool(String username) throws RemoteException;

    ArrayList<String> getPlayerPool() throws RemoteException;

    int createRoom(String username) throws RemoteException;

    void leaveRoom() throws RemoteException;

    void inviteAll(String inviter, int roomNum) throws RemoteException;

    void invite(String inviter, String targetUser, int roomNum) throws RemoteException;

    ArrayList<String> getUserInRoom(int roomNum) throws RemoteException;

    void startNewGame(ArrayList<String> players, int roomNum) throws RemoteException;

    void insertLetter(int x, int y, char letter, int roomNum) throws RemoteException;  // ps: should check if cell empty at client GUI

    void startVote(int startI, int startJ, int length, boolean horizontal, int roomNum) throws RemoteException;

    void passTurn(int roomNum) throws RemoteException;

    void vote(String username, boolean agree, int roomNum) throws RemoteException;

    void leaveGame(String username, int roomNum) throws RemoteException;

    boolean canJoinRoom(String username, int roomNum) throws  RemoteException;

}