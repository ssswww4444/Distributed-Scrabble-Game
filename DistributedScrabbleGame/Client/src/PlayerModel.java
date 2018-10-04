/*
    COMP90015 Project 2 PlayerModel
    Group Name: Distributed Otaku
    Tutor: Alisha Aneja
 */

import javafx.beans.property.SimpleStringProperty;

/* A model used for the Player TableView UI element */
public class PlayerModel {
    private final SimpleStringProperty username;
    private final SimpleStringProperty status;

    public PlayerModel(String username, String status){
        this.username = new SimpleStringProperty(username);
        this.status = new SimpleStringProperty(status);
    }

    public String getUsername(){
        return username.get();
    }

    public void setUsername(String username){
        this.username.set(username);
    }

    public String getStatus(){
        return status.get();
    }

    public void setStatus(String status){
        this.status.set(status);
    }

}