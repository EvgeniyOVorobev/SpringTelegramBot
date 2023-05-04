package ru.ev.SpringTelegramBot.entity;

import org.glassfish.grizzly.http.util.TimeStamp;

import javax.persistence.Entity;
import javax.persistence.Id;
import java.sql.Timestamp;

@Entity(name="usersDataTable")
public class User {
    @Id
    private Long chadId;

    private String firsName;

    private String lastName;

    private String userName;

    private Timestamp registeredAt;

    public Long getChadId() {
        return chadId;
    }

    public void setChadId(Long chadId) {
        this.chadId = chadId;
    }

    public String getFirsName() {
        return firsName;
    }

    public void setFirsName(String firsName) {
        this.firsName = firsName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public Timestamp getRegisteredAt() {
        return registeredAt;
    }

    public void setRegisteredAt(Timestamp registeredAt) {
        this.registeredAt = registeredAt;
    }

    @Override
    public String toString() {
        return "User{" +
                "chadId=" + chadId +
                ", firsName='" + firsName + '\'' +
                ", lastName='" + lastName + '\'' +
                ", userName='" + userName + '\'' +
                ", registeredAt=" + registeredAt +
                '}';
    }
}
