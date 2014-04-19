package at.junction.mailer.database;

import com.avaje.ebean.validation.NotEmpty;
import com.avaje.ebean.validation.NotNull;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.util.Date;

@Entity()
@Table(name = "mailer")
public class Mail {

    public enum MailStatus {
        UNREAD, READ
    }

    @Id
    private int id;

    @NotNull
    private String playerFrom;

    @NotNull
    private String playerTo;

    @NotEmpty
    private String mail;

    @NotNull
    private Date mailTime;

    @NotNull
    private MailStatus status;

    @NotNull
    private boolean deleted;

    public void setId(int id) {
        this.id = id;
    }

    public int getId() {
        return this.id;
    }

    public void setPlayerFrom(String playerFrom) {
        this.playerFrom = playerFrom;
    }

    public String getPlayerFrom() {
        return this.playerFrom;
    }

    public void setPlayerTo(String playerTo) {
        this.playerTo = playerTo;
    }

    public String getPlayerTo() {
        return this.playerTo;
    }

    public void setMail(String mail) {
        this.mail = mail;
    }

    public String getMail() {
        return this.mail;
    }

    public void setMailTime(Date date) {
        this.mailTime = date;
    }

    public Date getMailTime() {
        return this.mailTime;
    }

    public void setStatus(MailStatus status) {
        this.status = status;
    }

    public MailStatus getStatus() {
        return this.status;
    }

    public boolean isDeleted() {
        return this.deleted;
    }

    public void setDeleted(boolean deleted) {
        this.deleted = deleted;
    }

}