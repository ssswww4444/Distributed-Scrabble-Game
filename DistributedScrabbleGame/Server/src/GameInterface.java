import java.rmi.Remote;
import java.rmi.RemoteException;

public interface GameInterface extends Remote {

    public void startVote() throws RemoteException;
    public void pass() throws RemoteException;
    public void insertLetter() throws RemoteException;
    public void vote(boolean agree) throws RemoteException;

}
