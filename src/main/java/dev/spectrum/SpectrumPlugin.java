package dev.spectrum;

import dev.spectrum.command.SpectrumCommand;
import dev.spectrum.command.StyleCommand;
import dev.spectrum.hook.Hooks;
import dev.spectrum.style.StyleKind;
import org.bukkit.Bukkit;
import org.bukkit.command.PluginCommand;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * Spectrum: colours for chat messages and player names, defined in chatcolors.yml and namegradients.yml.
 */
public class SpectrumPlugin extends JavaPlugin {

    private Settings settings;
    private Messages messages;
    private StyleService styles;
    private Selections selections;
    private Hooks hooks;

    @Override
    public void onEnable() {
        messages = new Messages(this);
        styles = new StyleService(this);
        selections = new Selections(this);
        hooks = new Hooks(this);

        reloadAll();

        for (StyleKind kind : StyleKind.values()) {
            StyleCommand command = new StyleCommand(this, kind);
            PluginCommand registered = getCommand(kind.command());
            if (registered != null) {
                registered.setExecutor(command);
                registered.setTabCompleter(command);
            }
        }
        SpectrumCommand admin = new SpectrumCommand(this);
        PluginCommand spectrum = getCommand("spectrum");
        if (spectrum != null) {
            spectrum.setExecutor(admin);
            spectrum.setTabCompleter(admin);
        }

        Bukkit.getPluginManager().registerEvents(new SpectrumListener(this), this);
        hooks.registerPlaceholders();

        // Players who are already online (after a reload of the plugin).
        for (Player player : Bukkit.getOnlinePlayers()) selections.load(player);
    }

    /** Reloads config.yml, messages.yml and the two style files. */
    public void reloadAll() {
        saveDefaultConfig();
        reloadConfig();
        settings = new Settings(getConfig());
        messages.load();
        styles.load();
        hooks.load();
    }

    public Settings settings() {
        return settings;
    }

    public Messages messages() {
        return messages;
    }

    public StyleService styles() {
        return styles;
    }

    public Selections selections() {
        return selections;
    }

    public Hooks hooks() {
        return hooks;
    }
}
