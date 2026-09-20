package dev.chroma.command;

import dev.chroma.ChromaPlugin;
import dev.chroma.style.Style;
import dev.chroma.style.StyleKind;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Locale;

/**
 * /chroma reload, and /chroma preview &lt;chat|name&gt; &lt;id&gt; to see a style without owning it.
 */
public final class ChromaCommand implements TabExecutor {

    private final ChromaPlugin plugin;

    public ChromaCommand(ChromaPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!sender.hasPermission("chroma.admin")) {
            plugin.messages().send(sender, "no-permission");
            return true;
        }

        String sub = args.length == 0 ? "" : args[0].toLowerCase(Locale.ROOT);
        switch (sub) {
            case "reload" -> {
                plugin.reloadAll();
                plugin.messages().send(sender, "reloaded");
            }
            case "preview" -> preview(sender, args);
            default -> plugin.messages().send(sender, "usage-chroma");
        }
        return true;
    }

    private void preview(CommandSender sender, String[] args) {
        if (args.length < 3) {
            plugin.messages().send(sender, "usage-chroma");
            return;
        }
        StyleKind kind = args[1].equalsIgnoreCase("name") ? StyleKind.NAME : StyleKind.CHAT;
        Style style = plugin.styles().library(kind).get(args[2]);
        if (style == null) {
            plugin.messages().send(sender, "invalid-style");
            return;
        }
        String text = kind == StyleKind.NAME && sender instanceof Player player
                ? plugin.hooks().nameOf(player) : plugin.settings().chatPreviewText();
        plugin.messages().send(sender, "preview", Placeholder.component("text", style.render(text)),
                Placeholder.unparsed("id", style.id()));
    }

    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args) {
        if (!sender.hasPermission("chroma.admin")) return List.of();
        List<String> options = new java.util.ArrayList<>();
        if (args.length == 1) options.addAll(List.of("reload", "preview"));
        if (args.length == 2 && args[0].equalsIgnoreCase("preview")) options.addAll(List.of("chat", "name"));
        if (args.length == 3 && args[0].equalsIgnoreCase("preview")) {
            StyleKind kind = args[1].equalsIgnoreCase("name") ? StyleKind.NAME : StyleKind.CHAT;
            plugin.styles().library(kind).all().forEach(style -> options.add(style.id()));
        }
        String typed = args[args.length - 1].toLowerCase(Locale.ROOT);
        options.removeIf(option -> !option.toLowerCase(Locale.ROOT).startsWith(typed));
        return options;
    }
}
