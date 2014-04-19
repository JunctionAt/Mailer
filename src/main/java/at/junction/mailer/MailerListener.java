package at.junction.mailer;

import org.bukkit.ChatColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerJoinEvent;

public class MailerListener {
    Mailer plugin;

    public MailerListener(Mailer plugin){
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event){
        int mailCount = plugin.mailbox.getInboxCount(event.getPlayer().getName());
        if (mailCount > 0){
            event.getPlayer().sendMessage(String.format("%sYou have mail! Type /mail to view it.", ChatColor.GREEN));
        }

    }
}
