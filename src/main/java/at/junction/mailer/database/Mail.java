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
    public int id;

    @NotNull
    public String playerFrom;

    @NotNull
    public String playerTo;

    @NotEmpty
    public String mail;

    @NotNull
    public Date mailTime;

    @NotNull
    public MailStatus status;

    @NotNull
    public boolean deleted;

    public String humanizeDate(){
        int time = (int) (new Date().getTime() - mailTime.getTime()) / 1000 / 3600;

        if (time == 1){
            return "1 hour ago";
        } else {
            return String.format("%d hours ago", time);
        }

    }
}