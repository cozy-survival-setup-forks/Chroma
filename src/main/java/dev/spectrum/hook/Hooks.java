package dev.spectrum.hook;

import dev.spectrum.SpectrumPlugin;
import dev.spectrum.style.Style;
import dev.spectrum.style.StyleKind;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.regex.Pattern;

/**
 * Optional plugins. Spectrum works without them.
 */
public final class Hooks {

    private static final Pattern OLD_CODES = Pattern.compile("(?i)[&§](#[0-9a-f]{6}|x(?:[&§][0-9a-f]){6}|[0-9a-fk-or])");
    private static final Pattern HAS_COLOURS = Pattern.compile("(?i)[&§](#[0-9a-f]{6}|x(?:[&§][0-9a-f]){6}|[0-9a-fk-o])|<(?:#[0-9a-f]{6}|gradient|rainbow)[:>]");
    private static final Pattern X_HEX = Pattern.compile("(?i)&x((?:&[0-9a-f]){6})");

    private final SpectrumPlugin plugin;
    private volatile boolean placeholderApi = false;

    public Hooks(SpectrumPlugin plugin) {
        this.plugin = plugin;
    }

    public void load() {
        placeholderApi = Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI");
    }

    /** %player_name% is taken over a little later, when PlaceholderAPI has loaded its own expansions. */
    public void takeOverPlayerName() {
        if (placeholderApi && plugin.settings().overridePlayerName()) PapiSupport.install(plugin);
    }

    public void giveBackPlayerName() {
        if (placeholderApi) PapiSupport.uninstall();
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


    /**
     * The name with its colours, or null when there is nothing to colour: the nickname as it was typed if it has
     * colours of its own (and that is allowed), else the name in the gradient the player has picked.
     */
    public String styledName(Player player) {
        String source = plugin.settings().nameSource();
        if (plugin.settings().nicknameColorsWin() && !source.isBlank() && placeholderApi) {
            String raw = PapiSupport.parse(player, source).trim();
            if (!raw.isBlank() && HAS_COLOURS.matcher(raw).find()) return ampersand(raw);
        }
        Style style = plugin.styles().equipped(player, StyleKind.NAME);
        return style == null ? null : style.ampersand(nameOf(player));
    }

    /** Old colour codes in any form as &amp; codes with &amp;#rrggbb, so one format is left. */
    static String ampersand(String text) {
        String result = text.replace('§', '&');
        return X_HEX.matcher(result).replaceAll(match -> "&#" + match.group(1).replace("&", ""));
    }

    /** The text without colour codes and MiniMessage tags. */
    static String plainText(String text) {
        String withoutCodes = OLD_CODES.matcher(text).replaceAll("");
        return MiniMessage.miniMessage().stripTags(withoutCodes).trim();
    }
}
