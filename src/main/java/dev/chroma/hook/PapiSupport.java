package dev.chroma.hook;

import dev.chroma.ChromaPlugin;
import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.entity.Player;

/**
 * The parts that use PlaceholderAPI classes. Only loaded when PlaceholderAPI is installed.
 */
final class PapiSupport {

    private PapiSupport() {
    }

    static String parse(Player player, String text) {
        return PlaceholderAPI.setPlaceholders(player, text);
    }

    static void register(ChromaPlugin plugin) {
        new ChromaExpansion(plugin).register();
    }
}
