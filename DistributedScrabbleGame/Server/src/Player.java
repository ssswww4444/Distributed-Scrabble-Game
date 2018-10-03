public class Player {

    private String username;
    private String status;


    public Player(String username) {
        this.username = username;
        this.status  = Constants.STATUS_AVAILABLE;
    }

    public String getUsername() {
        return username;
    }

    public String getStatus() { return status; }

    public void setStatus(int roomNum) {
        this.status = Constants.ROOM + " " + roomNum;
    }
}
