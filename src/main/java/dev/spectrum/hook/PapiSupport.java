package dev.spectrum.hook;

import dev.spectrum.SpectrumPlugin;
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

    static void install(SpectrumPlugin plugin) {
        PlayerNameOverride.install(plugin);
    }

    static void uninstall() {
        PlayerNameOverride.uninstall();
    }

    static void register(SpectrumPlugin plugin) {
        new SpectrumExpansion(plugin).register();
    }
}
