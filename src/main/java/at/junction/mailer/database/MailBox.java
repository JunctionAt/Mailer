package at.junction.mailer.database;

import at.junction.mailer.Mailer;
import com.avaje.ebean.Query;

import java.util.ArrayList;
import java.util.List;

public class MailBox {
    Mailer plugin;

    public MailBox(Mailer plugin) {
        this.plugin = plugin;
    }

    public List<Mail> getUserMails(String username) {
        Query<Mail> query = plugin.getDatabase().find(Mail.class).where().ieq("playerTo", username).eq("deleted", false).order("id DESC");
        if (query != null)
            return query.findList();
        else
            return new ArrayList<Mail>();
    }

    public List<Mail> getUnreadMails(String username) {
        Query<Mail> query = plugin.getDatabase().find(Mail.class).where().ieq("playerTo", username).eq("status", Mail.MailStatus.UNREAD).eq("deleted", false).order("id DESC");
        if (query != null)
            return query.findList();
        else
            return new ArrayList<Mail>();
    }

    public List<Mail> getMailsFrom(String username) {
        Query<Mail> query = plugin.getDatabase().find(Mail.class).where().ieq("playerFrom", username).order("id DESC");
        if (query != null)
            return query.findList();
        else
            return new ArrayList<Mail>();
    }

    public int getInboxCount(String username) {
        Query<Mail> query = plugin.getDatabase().find(Mail.class).where().ieq("playerTo", username).in("status", Mail.MailStatus.UNREAD).query();
        if (query != null)
            return query.findRowCount();
        else
            return 0;
    }

    public Mail getMail(int id) {
        Query<Mail> query = plugin.getDatabase().find(Mail.class).where().eq("id", id).query();

        if (query != null)
            return query.findUnique();
        else
            return null;
    }

    public void clear() {
    }

    public void save(Mail Mail) {
        plugin.getDatabase().save(Mail);
    }

}
