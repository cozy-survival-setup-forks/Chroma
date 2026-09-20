package dev.spectrum.style;

import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;

/**
 * The glitch look: white letters with a shadow in the colour of the style.
 *
 * @param all  every chat colour made of colours becomes a glitch one, unless it says {@code glitch: false}
 * @param text the colour of the letters
 */
public record GlitchOptions(boolean all, TextColor text) {

    public static final GlitchOptions OFF = new GlitchOptions(false, NamedTextColor.WHITE);
}
