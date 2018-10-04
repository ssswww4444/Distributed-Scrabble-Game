/*
    COMP90015 Project 2 ScoreModel
    Group Name: Distributed Otaku
    Tutor: Alisha Aneja
 */

import javafx.beans.property.SimpleStringProperty;

/* A model used for the Score TableView UI element */
public class ScoreModel {
    private final SimpleStringProperty username;
    private final SimpleStringProperty score;

    public ScoreModel(String username, String score){
        this.username = new SimpleStringProperty(username);
        this.score = new SimpleStringProperty(score);
    }

    public String getUsername(){
        return username.get();
    }

    public void setUsername(String username){
        this.username.set(username);
    }

    public String getScore(){
        return score.get();
    }

    public void setScore(String score){
        this.score.set(score);
    }
}
