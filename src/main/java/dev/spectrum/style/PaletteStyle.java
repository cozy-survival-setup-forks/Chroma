package dev.spectrum.style;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

/**
 * A style made of a list of colours and a mode (single colour, gradient, linear, random, rainbow), plus
 * optional bold, italic, underline, strikethrough and obfuscated.
 */
public final class PaletteStyle extends Style {

    /** Which decorations the text has. */
    public record Decorations(boolean bold, boolean italic, boolean underlined, boolean strikethrough, boolean obfuscated) {
        public static final Decorations NONE = new Decorations(false, false, false, false, false);

    }

    private final Palette.Mode mode;
    private final List<TextColor> colors;
    private final boolean ignoreSpaces;
    private final Decorations decorations;

    public PaletteStyle(StyleKind kind, String id, String display, String permission, Palette.Mode mode,
                        List<TextColor> colors, boolean ignoreSpaces, Decorations decorations) {
        super(kind, id, display, permission);
        this.mode = mode;
        this.colors = List.copyOf(colors);
        this.ignoreSpaces = ignoreSpaces;
        this.decorations = decorations;
    }

    private List<Palette.Run> runs(String text, Random random) {
        return Palette.paint(text, mode, colors, ignoreSpaces, random);
    }

    @Override
    public Component render(String text, boolean allowTags) {
        // Tags in the text do nothing here, the colours of the style decide how it looks.
        TextComponent.Builder builder = Component.text();
        if (decorations.bold()) builder.decoration(TextDecoration.BOLD, true);
        if (decorations.italic()) builder.decoration(TextDecoration.ITALIC, true);
        if (decorations.underlined()) builder.decoration(TextDecoration.UNDERLINED, true);
        if (decorations.strikethrough()) builder.decoration(TextDecoration.STRIKETHROUGH, true);
        if (decorations.obfuscated()) builder.decoration(TextDecoration.OBFUSCATED, true);

        for (Palette.Run run : runs(text, ThreadLocalRandom.current())) {
            builder.append(Component.text(run.text(), run.color()));
        }
        return builder.build();
    }

    @Override
    public String miniMessage(String text) {
        List<String> open = new ArrayList<>();
        if (decorations.bold()) open.add("bold");
        if (decorations.italic()) open.add("italic");
        if (decorations.underlined()) open.add("underlined");
        if (decorations.strikethrough()) open.add("strikethrough");
        if (decorations.obfuscated()) open.add("obfuscated");

        StringBuilder out = new StringBuilder();
        for (String tag : open) out.append('<').append(tag).append('>');
        for (Palette.Run run : runs(text, ThreadLocalRandom.current())) {
            String hex = run.color().asHexString();
            out.append('<').append(hex).append('>').append(MINI.escapeTags(run.text())).append("</").append(hex).append('>');
        }
        for (int i = open.size() - 1; i >= 0; i--) out.append("</").append(open.get(i)).append('>');
        return out.toString();
    }
}
