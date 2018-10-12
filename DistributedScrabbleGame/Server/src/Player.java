public class Player {

    private String username;
    private String status;  // possible values: "available" / "room"
    private int roomNum;
    private boolean isHost;


    public Player(String username) {
        this.username = username;
        this.status = Constants.STATUS_AVAILABLE;
        this.roomNum = Constants.NOT_IN_ROOM_ID;  // initially not in any room
        this.isHost = false;
    }

    public String getUsername() {
        return username;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) { this.status = status; }

    public void setRoomNum(int roomNum) { this.roomNum = roomNum; }

    public int getRoomNum() {return roomNum; }

    public void setIsHost(boolean isHost) { this.isHost = isHost; }

    public boolean getIsHost() { return isHost; }
}
