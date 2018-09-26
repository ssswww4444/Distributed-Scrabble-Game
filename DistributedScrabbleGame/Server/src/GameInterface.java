import java.rmi.Remote;
import java.rmi.RemoteException;

public interface GameInterface extends Remote {

    public void startVote() throws RemoteException;
    public void pass() throws RemoteException;
    public boolean insertLetter(int x, int y, char letter) throws RemoteException;
    public void vote(String username, boolean agree) throws RemoteException;

}
