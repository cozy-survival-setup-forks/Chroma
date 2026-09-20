package dev.chroma;

import dev.chroma.style.Style;
import dev.chroma.style.StyleKind;
import io.papermc.paper.event.player.AsyncChatEvent;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

/**
 * Colours chat messages, and keeps track of what players picked.
 */
public final class ChromaListener implements Listener {

    private final ChromaPlugin plugin;

    ChromaListener(ChromaPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onJoin(PlayerJoinEvent event) {
        plugin.selections().load(event.getPlayer());
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        plugin.selections().forget(event.getPlayer().getUniqueId());
    }

    /**
     * Runs late, so the text is coloured after other plugins changed it. What other plugins put around the
     * message (the format of the chat) is not touched.
     */
    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onChat(AsyncChatEvent event) {
        Player player = event.getPlayer();
        Style style = plugin.styles().equipped(player, StyleKind.CHAT);
        if (style == null) return;

        String text = PlainTextComponentSerializer.plainText().serialize(event.message());
        if (text.isBlank()) return;

        boolean tags = plugin.settings().allowColorCodes() && player.hasPermission("chroma.chat.codes");
        if (tags) text = Messages.convertLegacy(text);
        event.message(style.render(text, tags));
    }
}
