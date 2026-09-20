package dev.chroma;

import dev.chroma.style.StyleKind;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * What each player picked. It is saved on the player, so it needs no file or database and survives restarts.
 * The choices of online players are also kept in memory, because chat is handled off the main thread.
 */
public final class Selections {

    private final ChromaPlugin plugin;
    private final Map<UUID, Map<StyleKind, String>> online = new ConcurrentHashMap<>();

    Selections(ChromaPlugin plugin) {
        this.plugin = plugin;
    }

    private NamespacedKey key(StyleKind kind) {
        return new NamespacedKey(plugin, kind.key() + "_style");
    }

    /** Reads what a player picked. Call it when they join. */
    void load(Player player) {
        PersistentDataContainer data = player.getPersistentDataContainer();
        Map<StyleKind, String> picked = new ConcurrentHashMap<>();
        for (StyleKind kind : StyleKind.values()) {
            String id = data.get(key(kind), PersistentDataType.STRING);

            // Name gradients picked with the NameGradient plugin are taken over.
            if (id == null && kind == StyleKind.NAME) {
                id = data.get(new NamespacedKey("namegradient", "equipped_gradient"), PersistentDataType.STRING);
                if (id != null) data.set(key(kind), PersistentDataType.STRING, id);
            }
            if (id != null) picked.put(kind, id);
        }
        online.put(player.getUniqueId(), picked);
    }

    void forget(UUID player) {
        online.remove(player);
    }

    /** The id the player picked, or null. */
    public @Nullable String get(UUID player, StyleKind kind) {
        Map<StyleKind, String> picked = online.get(player);
        return picked == null ? null : picked.get(kind);
    }

    /** Saves the pick of a player. A null id removes it. */
    public void set(Player player, StyleKind kind, @Nullable String id) {
        PersistentDataContainer data = player.getPersistentDataContainer();
        Map<StyleKind, String> picked = online.computeIfAbsent(player.getUniqueId(), uuid -> new ConcurrentHashMap<>());
        if (id == null) {
            data.remove(key(kind));
            picked.remove(kind);
        } else {
            data.set(key(kind), PersistentDataType.STRING, id);
            picked.put(kind, id);
        }
    }
}
