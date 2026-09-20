package dev.spectrum.style;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.ShadowColor;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;

import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

/**
 * A style made of a list of colours and a mode (single colour, gradient, linear, random, rainbow), plus
 * optional bold, italic, underline, strikethrough and obfuscated. With a glitch text colour the letters have
 * that colour and the colours of the style become their shadow.
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
    private final @Nullable TextColor glitchText;

    public PaletteStyle(StyleKind kind, String id, String display, String permission, Palette.Mode mode,
                        List<TextColor> colors, boolean ignoreSpaces, Decorations decorations, @Nullable TextColor glitchText) {
        super(kind, id, display, permission);
        this.mode = mode;
        this.colors = List.copyOf(colors);
        this.ignoreSpaces = ignoreSpaces;
        this.decorations = decorations;
        this.glitchText = glitchText;
    }

    public boolean glitch() {
        return glitchText != null;
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
            if (glitchText == null) {
                builder.append(Component.text(run.text(), run.color()));
            } else {
                builder.append(Component.text(run.text(), glitchText).shadowColor(ShadowColor.shadowColor(run.color(), 255)));
            }
        }
        return builder.build();
    }

    /** Legacy codes cannot carry a shadow, so a glitch style is written as MiniMessage. */
    @Override
    public String ampersand(String text) {
        return glitchText == null ? super.ampersand(text) : MINI.serialize(render(text));
    }
}
