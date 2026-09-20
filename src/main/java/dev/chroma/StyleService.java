package dev.chroma;

import dev.chroma.style.Style;
import dev.chroma.style.StyleKind;
import dev.chroma.style.StyleLibrary;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.EnumMap;
import java.util.Map;

/**
 * Loads the style files and answers what a player is allowed to use and what they have picked.
 */
public final class StyleService {

    private final ChromaPlugin plugin;
    private volatile Map<StyleKind, StyleLibrary> libraries = new EnumMap<>(StyleKind.class);

    StyleService(ChromaPlugin plugin) {
        this.plugin = plugin;
    }

    void load() {
        Map<StyleKind, StyleLibrary> loaded = new EnumMap<>(StyleKind.class);
        for (StyleKind kind : StyleKind.values()) {
            File file = new File(plugin.getDataFolder(), kind.fileName());
            if (!file.exists()) plugin.saveResource(kind.fileName(), false);
            YamlConfiguration yaml = YamlConfiguration.loadConfiguration(file);
            loaded.put(kind, StyleLibrary.load(kind, yaml, plugin.getLogger()));
        }
        libraries = loaded;
    }

    public StyleLibrary library(StyleKind kind) {
        return libraries.get(kind);
    }

    /**
     * Can the player use this style? Everyone can when permissions are turned off. Players with the wildcard
     * of the kind (chroma.chat.* or chroma.name.*) can use all of them.
     */
    public boolean canUse(Player player, Style style) {
        if (!plugin.settings().usePermissions()) return true;
        return player.hasPermission(style.permission())
                || player.hasPermission(style.kind().defaultPermission().replace("{id}", "*"));
    }

    /**
     * The style a player has active: the one they picked, or the default of the file. Null if they have none, or
     * lost the permission for it.
     */
    public @Nullable Style equipped(Player player, StyleKind kind) {
        StyleLibrary library = library(kind);
        Style style = library.get(plugin.selections().get(player.getUniqueId(), kind));
        if (style == null && !library.defaultId().isEmpty()) {
            style = library.get(library.defaultId());
        }
        return style != null && canUse(player, style) ? style : null;
    }
}
