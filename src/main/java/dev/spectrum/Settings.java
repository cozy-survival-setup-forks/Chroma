package dev.spectrum;

import org.bukkit.configuration.file.FileConfiguration;

/**
 * The options in config.yml.
 */
public final class Settings {

    private final FileConfiguration config;

    Settings(FileConfiguration config) {
        this.config = config;
    }

    /** When off, every player can use every style. */
    public boolean usePermissions() {
        return config.getBoolean("use-permissions", true);
    }

    /** The placeholder that gives the name to colour, for example a nickname. Empty for the player name. */
    public String nameSource() {
        return config.getString("name-source", "");
    }

    /** Whether players with spectrum.chat.codes can write colour codes and MiniMessage in their messages. */
    public boolean allowColorCodes() {
        return config.getBoolean("chat.allow-color-codes", false);
    }

    /** The text shown in the preview placeholders of chat styles. */
    public String chatPreviewText() {
        return config.getString("chat.preview-text", "The quick brown fox");
    }

    /** The command that gives a player the permission of a style. {player} and {permission} are filled in. */
    public String permissionSetCommand() {
        return config.getString("commands.permission-set", "");
    }

    public String permissionUnsetCommand() {
        return config.getString("commands.permission-unset", "");
    }
}
