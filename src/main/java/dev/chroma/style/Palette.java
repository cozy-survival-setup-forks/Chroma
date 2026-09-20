package dev.chroma.style;

import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.util.HSVLike;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Random;

/**
 * Colours for text: reading them from the config and spreading a list of them over some text.
 */
public final class Palette {

    /** How the colours of a palette are spread over the characters of a text. */
    public enum Mode {
        /** Every character has the first colour. */
        SINGLE,
        /** A smooth fade through all the colours, from the first character to the last. */
        GRADIENT,
        /** The colours one after the other, over and over: red, blue, red, blue... */
        LINEAR,
        /** A random colour from the list for every character. */
        RANDOM,
        /** All the colours of the rainbow from the first character to the last. The colour list is not used. */
        RAINBOW
    }

    /** A piece of text that has one colour. */
    public record Run(String text, TextColor color) {
    }

    private Palette() {
    }

    /**
     * Reads a colour: {@code #ff8800}, {@code &#ff8800}, a colour name such as {@code dark_red}, or an old style
     * code such as {@code 4} or {@code &c}.
     */
    public static @Nullable TextColor parseColor(@Nullable String input) {
        if (input == null) return null;
        String text = input.trim().toLowerCase(Locale.ROOT);
        if (text.startsWith("&#")) text = text.substring(1);
        if (text.startsWith("&") && text.length() == 2) text = text.substring(1);
        if (text.isEmpty()) return null;

        if (text.startsWith("#")) {
            return text.length() == 7 ? TextColor.fromHexString(text) : null;
        }
        if (text.length() == 1) {
            NamedTextColor legacy = legacyCode(text.charAt(0));
            if (legacy != null) return legacy;
        }
        return NamedTextColor.NAMES.value(text);
    }

    private static @Nullable NamedTextColor legacyCode(char code) {
        return switch (code) {
            case '0' -> NamedTextColor.BLACK;
            case '1' -> NamedTextColor.DARK_BLUE;
            case '2' -> NamedTextColor.DARK_GREEN;
            case '3' -> NamedTextColor.DARK_AQUA;
            case '4' -> NamedTextColor.DARK_RED;
            case '5' -> NamedTextColor.DARK_PURPLE;
            case '6' -> NamedTextColor.GOLD;
            case '7' -> NamedTextColor.GRAY;
            case '8' -> NamedTextColor.DARK_GRAY;
            case '9' -> NamedTextColor.BLUE;
            case 'a' -> NamedTextColor.GREEN;
            case 'b' -> NamedTextColor.AQUA;
            case 'c' -> NamedTextColor.RED;
            case 'd' -> NamedTextColor.LIGHT_PURPLE;
            case 'e' -> NamedTextColor.YELLOW;
            case 'f' -> NamedTextColor.WHITE;
            default -> null;
        };
    }

    /**
     * The text split into runs of one colour each.
     *
     * @param ignoreSpaces for {@link Mode#LINEAR}: spaces do not use up a colour
     */
    public static List<Run> paint(String text, Mode mode, List<TextColor> colors, boolean ignoreSpaces, Random random) {
        int[] chars = text.codePoints().toArray();
        List<Run> runs = new ArrayList<>();
        if (chars.length == 0 || (colors.isEmpty() && mode != Mode.RAINBOW)) {
            return runs;
        }

        StringBuilder current = new StringBuilder();
        TextColor currentColor = null;
        int linearIndex = 0;
        for (int i = 0; i < chars.length; i++) {
            TextColor color = switch (mode) {
                case SINGLE -> colors.get(0);
                case GRADIENT -> gradientAt(colors, i, chars.length);
                case RANDOM -> colors.get(random.nextInt(colors.size()));
                case RAINBOW -> TextColor.color(HSVLike.hsvLike(chars.length == 1 ? 0f : (float) i / chars.length, 1f, 1f));
                case LINEAR -> {
                    TextColor picked = colors.get(linearIndex % colors.size());
                    if (!(ignoreSpaces && Character.isWhitespace(chars[i]))) linearIndex++;
                    yield picked;
                }
            };

            if (currentColor != null && !currentColor.equals(color)) {
                runs.add(new Run(current.toString(), currentColor));
                current.setLength(0);
            }
            currentColor = color;
            current.appendCodePoint(chars[i]);
        }
        runs.add(new Run(current.toString(), currentColor));
        return runs;
    }

    /** The colour of character {@code index} out of {@code length}, fading through all the colours. */
    static TextColor gradientAt(List<TextColor> colors, int index, int length) {
        if (colors.size() == 1 || length == 1) return colors.get(0);

        float position = (float) index / (length - 1) * (colors.size() - 1);
        int from = Math.min((int) position, colors.size() - 2);
        return TextColor.lerp(position - from, colors.get(from), colors.get(from + 1));
    }
}
