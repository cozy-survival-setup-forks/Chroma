package dev.spectrum.hook;

import dev.spectrum.SpectrumPlugin;
import me.clip.placeholderapi.PlaceholderAPIPlugin;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.logging.Level;

/**
 * Takes the place of PlaceholderAPI's player expansion so %player_name% gives the coloured name for players who
 * have a name gradient or a coloured nickname. Every other %player_...% is answered by the expansion it replaced.
 * Only loaded when PlaceholderAPI is installed.
 */
final class PlayerNameOverride extends PlaceholderExpansion {

    private static PlayerNameOverride installed;
    private static final ThreadLocal<Boolean> BUSY = ThreadLocal.withInitial(() -> false);

    private final SpectrumPlugin plugin;
    private final PlaceholderExpansion original;

    private PlayerNameOverride(SpectrumPlugin plugin, PlaceholderExpansion original) {
        this.plugin = plugin;
        this.original = original;
    }

    /** Swaps it in. Called a little after startup, when PlaceholderAPI has loaded its expansions. */
    static void install(SpectrumPlugin plugin) {
        if (installed != null) return;
        try {
            var manager = PlaceholderAPIPlugin.getInstance().getLocalExpansionManager();
            PlaceholderExpansion original = manager.getExpansion("player");
            if (original == null) {
                plugin.getLogger().warning("override-player-name is on, but the PlaceholderAPI player expansion is not installed. Run /papi ecloud download Player and reload Spectrum.");
                return;
            }
            manager.unregister(original);
            PlayerNameOverride override = new PlayerNameOverride(plugin, original);
            if (override.register()) installed = override;
            else manager.register(original);
        } catch (RuntimeException | LinkageError e) {
            plugin.getLogger().log(Level.WARNING, "Could not take over %player_name%: " + e);
        }
    }

    /** Puts the original back. */
    static void uninstall() {
        if (installed == null) return;
        PlayerNameOverride override = installed;
        installed = null;
        try {
            var manager = PlaceholderAPIPlugin.getInstance().getLocalExpansionManager();
            manager.unregister(override);
            manager.register(override.original);
        } catch (RuntimeException | LinkageError ignored) {
            // PlaceholderAPI is shutting down as well
        }
    }

    @Override
    public @NotNull String getIdentifier() {
        return "player";
    }

    @Override
    public @NotNull String getAuthor() {
        return original.getAuthor();
    }

    @Override
    public @NotNull String getVersion() {
        return original.getVersion();
    }

    @Override
    public boolean persist() {
        return true;
    }

    @Override
    public @Nullable String onRequest(OfflinePlayer offline, @NotNull String params) {
        if (params.equals("name") && !BUSY.get() && offline instanceof Player player && plugin.settings().overridePlayerName()) {
            // name-source may itself be %player_name%, which must not come back here
            BUSY.set(true);
            try {
                String styled = plugin.hooks().styledName(player);
                if (styled != null) return styled;
            } finally {
                BUSY.set(false);
            }
        }
        return original.onRequest(offline, params);
    }
}
