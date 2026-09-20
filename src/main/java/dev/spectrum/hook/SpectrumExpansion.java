package dev.spectrum.hook;

import dev.spectrum.SpectrumPlugin;
import dev.spectrum.style.Style;
import dev.spectrum.style.StyleKind;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Locale;

/**
 * PlaceholderAPI values. {@code <kind>} is {@code name} or {@code chat}.
 * <pre>
 * %spectrum_name%                        the player's name in their name gradient, as MiniMessage
 * %spectrum_name_legacy%                 the same with § codes,  %spectrum_name_amp% with &amp;#rrggbb codes
 * %spectrum_&lt;kind&gt;_id%                   the id of the style the player has active, or "none"
 * %spectrum_&lt;kind&gt;_display%              its name, coloured, as MiniMessage
 * %spectrum_&lt;kind&gt;_equipped_&lt;id&gt;        true if that style is active
 * %spectrum_&lt;kind&gt;_owned_&lt;id&gt;           true if the player may use that style
 * %spectrum_&lt;kind&gt;_preview_&lt;id&gt;         the player's name (name) or the preview text (chat) in that style, as MiniMessage
 * %spectrum_&lt;kind&gt;_preview_legacy_&lt;id&gt;  the same with § codes,  preview_amp_&lt;id&gt; with &amp;#rrggbb codes
 * </pre>
 * Only loaded when PlaceholderAPI is installed.
 */
final class SpectrumExpansion extends PlaceholderExpansion {

    private final SpectrumPlugin plugin;

    SpectrumExpansion(SpectrumPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public @NotNull String getIdentifier() {
        return "spectrum";
    }

    @Override
    public @NotNull String getAuthor() {
        return "Spectrum";
    }

    @Override
    public @NotNull String getVersion() {
        return plugin.getPluginMeta().getVersion();
    }

    @Override
    public boolean persist() {
        return true;
    }

    @Override
    public @Nullable String onPlaceholderRequest(Player player, @NotNull String params) {
        if (player == null) return "";
        String request = params.toLowerCase(Locale.ROOT);

        if (request.equals("name") || request.equals("name_legacy") || request.equals("name_amp")) {
            String name = plugin.hooks().nameOf(player);
            Style style = plugin.styles().equipped(player, StyleKind.NAME);
            if (style == null) return name;
            return switch (request) {
                case "name_legacy" -> style.legacy(name);
                case "name_amp" -> style.ampersand(name);
                default -> style.miniMessage(name);
            };
        }

        for (StyleKind kind : StyleKind.values()) {
            String prefix = kind.key() + "_";
            if (request.startsWith(prefix)) return kindValue(player, kind, request.substring(prefix.length()));
        }
        return null;
    }

    private @Nullable String kindValue(Player player, StyleKind kind, String request) {
        Style active = plugin.styles().equipped(player, kind);
        switch (request) {
            case "id" -> {
                return active == null ? "none" : active.id();
            }
            case "display" -> {
                return active == null ? "None" : active.miniMessage(active.display());
            }
            default -> {
                // fall through to the ones that take an id
            }
        }

        String preview = kind == StyleKind.NAME ? plugin.hooks().nameOf(player) : plugin.settings().chatPreviewText();
        if (request.startsWith("equipped_")) {
            return String.valueOf(active != null && active.id().equals(request.substring(9)));
        }
        if (request.startsWith("owned_")) {
            Style style = plugin.styles().library(kind).get(request.substring(6));
            return String.valueOf(style != null && plugin.styles().canUse(player, style));
        }
        if (request.startsWith("preview_legacy_")) return previewOf(kind, request.substring(15), style -> style.legacy(preview));
        if (request.startsWith("preview_amp_")) return previewOf(kind, request.substring(12), style -> style.ampersand(preview));
        if (request.startsWith("preview_")) return previewOf(kind, request.substring(8), style -> style.miniMessage(preview));
        return null;
    }

    private String previewOf(StyleKind kind, String id, java.util.function.Function<Style, String> format) {
        Style style = plugin.styles().library(kind).get(id);
        return style == null ? "" : format.apply(style);
    }
}
