package at.junction.mailer;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import at.junction.mailer.database.Mail;
import at.junction.mailer.database.MailBox;

import javax.persistence.PersistenceException;
import java.util.ArrayList;
import java.util.Arrays;
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
        setupDatabase();
        getServer().getPluginManager().registerEvents(new MailerListener(this), this);
    }
    @Override
    public ArrayList<Class<?>> getDatabaseClasses() {
        ArrayList<Class<?>> list = new ArrayList<>();
        list.add(Mail.class);
        return list;
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
        String sendTo;
        String[] messageArray;
        if (command.getName().equalsIgnoreCase("mail")) {
            if (args.length == 0) {
                //Show user their inbox
                sendMessages(sender, sender.getName(), mailbox.getUserMails(sender.getName()));
            } else if (args.length == 2 && args[0].equalsIgnoreCase("read")) {
                //Send user a single message, id = args[1]
                sendMessage(sender, sender.getName(), Integer.parseInt(args[1]));
            } else if (args.length > 2 && args[0].equalsIgnoreCase("send")) {
                sendTo = args[1];
                messageArray = Arrays.copyOfRange(args, 2, args.length);
                mailSend(sender.getName(), sendTo, messageArray);
                sender.sendMessage(String.format("%sMail Sent!", ChatColor.GREEN));

            } else if (args.length == 2 && args[0].equalsIgnoreCase("delete")){
                Mail mail = mailbox.getMail(Integer.parseInt(args[1]));
                if (!mail.getPlayerTo().equalsIgnoreCase(sender.getName())){
                    sender.sendMessage(String.format("%sYou can not delete another player's mail!", ChatColor.RED));
                    return true;
                }
                mail.setDeleted(true);
                mailbox.save(mail);
            } else if (args.length > 1) {
                // Default to send when no valid subcommand is given
                sendTo = args[0];
                messageArray = Arrays.copyOfRange(args, 1, args.length);
                mailSend(sender.getName(), sendTo, messageArray);
                sender.sendMessage(String.format("%sMail Sent!", ChatColor.GREEN));
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
        } else if (command.getName().equalsIgnoreCase("mailhelp")){

            sender.sendMessage(String.format("%sMailer Help", ChatColor.GRAY));
            sender.sendMessage(String.format("%s-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-", ChatColor.DARK_GRAY));
            sender.sendMessage(String.format("%s/mail - View message overview", ChatColor.GRAY));
            sender.sendMessage(String.format("%s/mail send <player> <message>- Send a message", ChatColor.GRAY));
            sender.sendMessage(String.format("%s/mail read <id> - Read a given message", ChatColor.GRAY));
            sender.sendMessage(String.format("%s/mail delete <id> - Delete a given message", ChatColor.GRAY));
            if (sender.hasPermission("mailer.staff")){
                sender.sendMessage(String.format("%s-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-", ChatColor.DARK_GRAY));
                sender.sendMessage(String.format("%s/mail-override - View other messages", ChatColor.GRAY));
                sender.sendMessage(String.format("%s/mail-override !to <player> - View messages to a user", ChatColor.GRAY));
                sender.sendMessage(String.format("%s/mail-override !from <player> - View messages from a player", ChatColor.GRAY));
                sender.sendMessage(String.format("%s/mail-override !read <player> <id> - Read a message to a player with a given ID", ChatColor.GRAY));
            }

        }
        return true;
    }

    public void sendMessages(CommandSender sender, String owner, List<Mail> mailList) {
        if (mailList.size() == 0) {
            sender.sendMessage(String.format("%sYou have no mail.", ChatColor.RED));
        } else {
            String messageFormat = "%s[%d] (%s) From: %s";
            sender.sendMessage(String.format("%sType /mail read <id> to read a message, or /mail delete <id> to remove it", ChatColor.GOLD));
            sender.sendMessage(String.format("%s[id] (time) from", ChatColor.GOLD));
            for (Mail m : mailList) {
                if (m.getStatus() == Mail.MailStatus.UNREAD) {
                    if (m.getMail().length() < 50) {
                        sender.sendMessage(String.format(messageFormat, ChatColor.GREEN, m.getId(), humanizeDate(m), m.getPlayerFrom()));
                    } else {
                        sender.sendMessage(String.format(messageFormat, ChatColor.GREEN, m.getId(), humanizeDate(m), m.getPlayerFrom()));
                    }
                } else if (m.getStatus() == Mail.MailStatus.READ) {
                    if (m.getMail().length() < 50) {
                        sender.sendMessage(String.format(messageFormat, ChatColor.DARK_GREEN, m.getId(), humanizeDate(m), m.getPlayerFrom()));
                    } else {
                        sender.sendMessage(String.format(messageFormat, ChatColor.DARK_GREEN, m.getId(), humanizeDate(m), m.getPlayerFrom()));
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
            if (mail.getPlayerTo().equalsIgnoreCase(owner)) {
                mail.setStatus(Mail.MailStatus.READ);
                mailbox.save(mail);
            } else {
                //If the player doesn't own the message AND they don't have permission, print error and return
                if (!sender.hasPermission("mailer.staff")){
                    sender.sendMessage(String.format("%sAll you see is gibberish...(You don't have permission to read other's mail)", ChatColor.RED));
                    return;
                }
                sender.sendMessage(String.format("%s Mail sent from %s %s to %s. Status: %s", ChatColor.GOLD, mail.getPlayerFrom(), humanizeDate(mail), mail.getPlayerTo(), (mail.getStatus() == Mail.MailStatus.READ ? "read" : "unread")));
            }
            sender.sendMessage(String.format("%s[%d] (%s) %s: %s", ChatColor.GOLD, mail.getId(), humanizeDate(mail), mail.getPlayerFrom(), mail.getMail()));
        }
    }
    private void mailSend(String sendFrom, String sendTo, String[] messageArray) {
        StringBuilder message = new StringBuilder();
        for (int i = 0; i < messageArray.length; i++)
            message.append(messageArray[i]).append(' ');
        message.substring(0, message.length() - 1);
        Mail mail = new Mail();
        mail.setMail(message.toString());
        mail.setDeleted(false);
        mail.setMailTime(new Date());
        mail.setPlayerFrom(sendFrom);
        mail.setPlayerTo(sendTo);
        mail.setStatus(Mail.MailStatus.UNREAD);
        mailbox.save(mail);


        Player alert = getServer().getPlayer(sendTo);
        if (alert != null){
            alert.sendMessage(String.format("%sYou have mail! Type /mail to read it", ChatColor.GREEN));
        }
    }
    public String humanizeDate(Mail mail) {
        int time = (int) (new Date().getTime() - mail.getMailTime().getTime()) / 1000 / 3600;

        if (time == 1) {
            return "1 hour ago";
        } else {
            return String.format("%d hours ago", time);
        }

    }

}
