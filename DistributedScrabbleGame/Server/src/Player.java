public class Player {

    private String username;
    private String status;  // possible values: "available" / "room"
    private int roomNum;


    public Player(String username) {
        this.username = username;
        this.status = Constants.STATUS_AVAILABLE;
        roomNum = -1;  // initially not in any room
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
}
