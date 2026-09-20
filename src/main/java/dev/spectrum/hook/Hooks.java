package dev.spectrum.hook;

import dev.spectrum.SpectrumPlugin;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.regex.Pattern;

/**
 * Optional plugins. Spectrum works without them.
 */
public final class Hooks {

    private static final Pattern OLD_CODES = Pattern.compile("(?i)[&§](#[0-9a-f]{6}|x(?:[&§][0-9a-f]){6}|[0-9a-fk-or])");

    private final SpectrumPlugin plugin;
    private boolean placeholderApi = false;

    public Hooks(SpectrumPlugin plugin) {
        this.plugin = plugin;
    }

    public void load() {
        placeholderApi = Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI");
    }

    public void registerPlaceholders() {
        if (placeholderApi) PapiSupport.register(plugin);
    }

    /**
     * The name to colour: the name-source placeholder from config.yml (a nickname, for example) or the player's
     * name, without any colours it already has.
     */
    public String nameOf(Player player) {
        String source = plugin.settings().nameSource();
        if (source.isBlank() || !placeholderApi) return player.getName();

        String name = plainText(PapiSupport.parse(player, source));
        return name.isBlank() ? player.getName() : name;
    }

    /** The text without colour codes and MiniMessage tags. */
    static String plainText(String text) {
        String withoutCodes = OLD_CODES.matcher(text).replaceAll("");
        return MiniMessage.miniMessage().stripTags(withoutCodes).trim();
    }
}
