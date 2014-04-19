package at.junction.mailer;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;

import at.junction.mailer.database.Mail;
import at.junction.mailer.database.MailBox;

import javax.persistence.PersistenceException;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;

public class Mailer extends JavaPlugin {

    public MailBox mailbox;

    void setupDatabase() {
        try {
            getDatabase().find(Mail.class).findRowCount();
        } catch (PersistenceException ex) {
            getLogger().log(Level.INFO, "First run, initializing database.");
            installDDL();
        }
    }


    @Override
    public void onEnable() {
        //This plugin has no configuration
        mailbox = new MailBox(this);

    }

    @Override
    public void onDisable() {

    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String name, String[] args) {
        /*
        * Usage
        * /mail - view inbox
        * /mail read id - read mail
        * /mail send player message - send message
        *
         */
        if (command.getName().equalsIgnoreCase("mail")) {
            if (args.length == 0) {
                //Show user their inbox
                sendMessages(sender, sender.getName(), mailbox.getUnreadMails(sender.getName()));
            } else if (args.length == 2 && args[0].equalsIgnoreCase("read")) {
                //Send user a single message, id = args[1]
                sendMessage(sender, sender.getName(), Integer.parseInt(args[1]));
            } else if (args.length > 2 && args[0].equalsIgnoreCase("send")) {
                StringBuilder message = new StringBuilder();
                String sendTo = args[1];
                for (int i = 2; i < args.length; i++)
                    message.append(args[i]).append(' ');
                message.substring(0, message.length() - 1);
                Mail mail = new Mail();
                mail.deleted = false;
                mail.mailTime = new Date();
                mail.playerFrom = sender.getName();
                mail.playerTo = sendTo;
                mail.status = Mail.MailStatus.UNREAD;
                mailbox.save(mail);
                sender.sendMessage(String.format("%sMail Sent!", ChatColor.GREEN));
            } else if (args.length == 2 && args[0].equalsIgnoreCase("delete")){
                Mail mail = mailbox.getMail(Integer.parseInt(args[1]));
                if (!mail.playerTo.equalsIgnoreCase(sender.getName())){
                    sender.sendMessage(String.format("%sYou can not delete another player's mail!", ChatColor.RED));
                    return true;
                }
                mail.deleted = true;
                mailbox.save(mail);
            }
        } else if (command.getName().equalsIgnoreCase("mail-override")){
            if (args.length <= 1){
                return false;
            } else if (args.length == 2 && args[0].equalsIgnoreCase("!from")) {
                String playerName = args[1];
                sendMessages(sender, playerName, mailbox.getMailsFrom(playerName));
            } else if (args.length == 2 && args[0].equalsIgnoreCase("!to")){
                String playerName = args[1];
                sendMessages(sender, playerName, mailbox.getUserMails(playerName));
            } else if (args.length == 3 && args[0].equalsIgnoreCase("!read")){
                //0: !read 1: playername 2: id
                String playerName = args[1];
                sendMessage(sender, playerName, Integer.parseInt(args[2]));
            }
        }
        return true;
    }

    public void sendMessages(CommandSender sender, String owner, List<Mail> mailList) {
        if (mailList.size() == 0) {
            sender.sendMessage(String.format("%sYou have no mail.", ChatColor.RED));
        } else {
            String messageFormat = "%s[%d] (%s) %s: %s";

            for (Mail m : mailList) {
                if (m.status == Mail.MailStatus.UNREAD) {
                    if (m.mail.length() < 50) {
                        sender.sendMessage(String.format(messageFormat, ChatColor.GREEN, m.id, m.humanizeDate(), m.playerFrom, m.mail));
                    } else {
                        sender.sendMessage(String.format(messageFormat, ChatColor.GREEN, m.id, m.humanizeDate(), m.playerFrom, String.format("%s...", m.mail.substring(0, 47))));
                    }
                } else if (m.status == Mail.MailStatus.READ) {
                    if (m.mail.length() < 50) {
                        sender.sendMessage(String.format(messageFormat, ChatColor.DARK_GREEN, m.id, m.humanizeDate(), m.playerFrom, m.mail));
                    } else {
                        sender.sendMessage(String.format(messageFormat, ChatColor.DARK_GREEN, m.id, m.humanizeDate(), m.playerFrom, String.format("%s...", m.mail.substring(0, 47))));
                    }
                }
            }
        }
    }

    public void sendMessage(CommandSender sender, String owner, int id) {
        Mail mail = mailbox.getMail(id);
        if (mail == null) {
            sender.sendMessage(String.format("%sThis mail doesn't exist.", ChatColor.RED));
        } else {
            if (mail.playerTo.equalsIgnoreCase(owner)) {
                mail.status = Mail.MailStatus.READ;
                mailbox.save(mail);
            } else {
                //If the player doesn't own the message AND they don't have permission, print error and return
                if (!sender.hasPermission("mailer.staff")){
                    sender.sendMessage(String.format("%sAll you see is gibberish...(You don't have permission to read other's mail)", ChatColor.RED));
                    return;
                }
                sender.sendMessage(String.format("%s Mail sent from %s %s to %s. Status: %s", ChatColor.GOLD, mail.playerFrom, mail.humanizeDate(), mail.playerTo, (mail.status == Mail.MailStatus.READ ? "read" : "unread")));
            }
            sender.sendMessage(String.format("%s[%d] (%s) %s: %s", ChatColor.GOLD, mail.id, mail.humanizeDate(), mail.playerFrom, mail.mail));
        }
    }
}
