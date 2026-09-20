package dev.spectrum.style;

import net.kyori.adventure.text.format.TextColor;
import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.logging.Logger;

/**
 * Turns an entry of chatcolors.yml or namegradients.yml into a {@link Style}. Both files use the same entries:
 * <pre>
 * glitch:
 *   display: "Glitch"
 *   format: "&lt;gradient:#00ffff:#ff00ff&gt;{text}"      MiniMessage, or
 *
 * ocean:
 *   mode: GRADIENT                                    colours and a mode
 *   colors: ["#0077ff", "#00ffd5"]
 *   bold: true
 * </pre>
 */
public final class StyleParser {

    private StyleParser() {
    }

    /** The id used in commands and permissions: lower case letters, numbers, - and _. */
    public static String cleanId(String key) {
        return key.toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9_\\-]", "");
    }

    /** "dark_red" becomes "Dark Red". */
    public static String prettyName(String id) {
        StringBuilder out = new StringBuilder();
        for (String word : id.split("[_\\-]+")) {
            if (word.isEmpty()) continue;
            if (out.length() > 0) out.append(' ');
            out.append(Character.toUpperCase(word.charAt(0))).append(word.substring(1));
        }
        return out.toString();
    }

    /**
     * @return the style, or null if the entry has neither a format nor colours (the reason is logged)
     */
    public static @Nullable Style parse(StyleKind kind, String key, ConfigurationSection section, Logger logger,
                                        GlitchOptions glitch) {
        String id = cleanId(key);
        if (id.isEmpty()) {
            logger.warning(kind.fileName() + ": '" + key + "' is not a usable id (use letters, numbers, - and _).");
            return null;
        }

        String display = section.getString("display", prettyName(id));
        String permission = section.getString("permission", kind.defaultPermission());
        permission = permission.replace("{id}", id);

        String format = section.getString("format", "");
        if (!format.isBlank()) {
            if (!TemplateStyle.hasPlaceholder(format)) {
                logger.warning(kind.fileName() + ": " + id + " has no {text} in its format, the text is added at the end.");
            }
            if (section.getBoolean("glitch", false)) {
                logger.warning(kind.fileName() + ": " + id + " has a format, glitch only works on styles made of colours.");
            }
            return new TemplateStyle(kind, id, display, permission, format);
        }

        List<TextColor> colors = new ArrayList<>();
        List<String> names = section.isList("colors") ? section.getStringList("colors")
                : section.isString("colors") ? List.of(section.getString("colors", "")) : List.of();
        for (String name : names) {
            TextColor color = Palette.parseColor(name);
            if (color == null) {
                logger.warning(kind.fileName() + ": " + id + " has an unknown colour '" + name + "', skipping it.");
            } else {
                colors.add(color);
            }
        }

        Palette.Mode mode = parseMode(section.getString("mode", colors.size() > 1 ? "GRADIENT" : "SINGLE"), kind, id, logger);
        if (colors.isEmpty() && mode != Palette.Mode.RAINBOW) {
            logger.warning(kind.fileName() + ": " + id + " needs a 'format' or a list of 'colors', skipping it.");
            return null;
        }

        PaletteStyle.Decorations decorations = new PaletteStyle.Decorations(
                section.getBoolean("bold", false),
                section.getBoolean("italic", false),
                section.getBoolean("underline", section.getBoolean("underlined", false)),
                section.getBoolean("strikethrough", false),
                section.getBoolean("obfuscated", section.getBoolean("magic", false)));

        TextColor glitchText = glitchText(kind, id, section, glitch, logger);
        return new PaletteStyle(kind, id, display, permission, mode, colors, section.getBoolean("ignore-spaces", false), decorations, glitchText);
    }

    /** The letter colour of a glitch style, or null when the style is not one. Only chat colours can glitch. */
    private static @Nullable TextColor glitchText(StyleKind kind, String id, ConfigurationSection section, GlitchOptions glitch,
                                                   Logger logger) {
        boolean own = section.contains("glitch");
        boolean wanted = own ? section.getBoolean("glitch") : glitch.all();
        if (!wanted) return null;
        if (kind != StyleKind.CHAT) {
            if (own) logger.warning(kind.fileName() + ": " + id + " has glitch, which only works for chat colours.");
            return null;
        }
        return glitch.text();
    }

    private static Palette.Mode parseMode(String text, StyleKind kind, String id, Logger logger) {
        try {
            return Palette.Mode.valueOf(text.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException ex) {
            logger.warning(kind.fileName() + ": " + id + " has an unknown mode '" + text
                    + "' (use SINGLE, GRADIENT, LINEAR, RANDOM or RAINBOW), using GRADIENT.");
            return Palette.Mode.GRADIENT;
        }
    }
}
