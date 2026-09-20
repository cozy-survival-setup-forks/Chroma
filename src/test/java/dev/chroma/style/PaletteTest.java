package dev.chroma.style;

import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PaletteTest {

    private static final TextColor RED = TextColor.color(255, 0, 0);
    private static final TextColor BLUE = TextColor.color(0, 0, 255);

    private static String joined(List<Palette.Run> runs) {
        StringBuilder text = new StringBuilder();
        runs.forEach(run -> text.append(run.text()));
        return text.toString();
    }

    @Test
    void coloursCanBeWrittenInAllTheWaysPlayersKnow() {
        assertEquals(TextColor.color(0xff8800), Palette.parseColor("#ff8800"));
        assertEquals(TextColor.color(0xff8800), Palette.parseColor("&#FF8800"));
        assertEquals(NamedTextColor.DARK_RED, Palette.parseColor("4"));
        assertEquals(NamedTextColor.RED, Palette.parseColor("&c"));
        assertEquals(NamedTextColor.LIGHT_PURPLE, Palette.parseColor("light_purple"));
        assertNull(Palette.parseColor("banana"));
        assertNull(Palette.parseColor("#12"));
        assertNull(Palette.parseColor(""));
        assertNull(Palette.parseColor(null));
    }

    @Test
    void singleGivesEveryLetterTheFirstColour() {
        List<Palette.Run> runs = Palette.paint("hello", Palette.Mode.SINGLE, List.of(RED, BLUE), false, new Random(1));

        assertEquals(1, runs.size());
        assertEquals("hello", runs.get(0).text());
        assertEquals(RED, runs.get(0).color());
    }

    @Test
    void gradientStartsAndEndsOnTheOuterColours() {
        List<Palette.Run> runs = Palette.paint("abcdef", Palette.Mode.GRADIENT, List.of(RED, BLUE), false, new Random(1));

        assertEquals("abcdef", joined(runs));
        assertEquals(RED, runs.get(0).color());
        assertEquals(BLUE, runs.get(runs.size() - 1).color());
        assertEquals(6, runs.size()); // every letter is a little different
    }

    @Test
    void gradientThroughThreeColoursHitsTheMiddleColour() {
        TextColor green = TextColor.color(0, 255, 0);
        List<Palette.Run> runs = Palette.paint("abcde", Palette.Mode.GRADIENT, List.of(RED, green, BLUE), false, new Random(1));

        assertEquals(green, runs.get(2).color());
    }

    @Test
    void oneLetterOrOneColourStillWorks() {
        assertEquals(RED, Palette.paint("a", Palette.Mode.GRADIENT, List.of(RED, BLUE), false, new Random(1)).get(0).color());
        assertEquals(1, Palette.paint("abc", Palette.Mode.GRADIENT, List.of(RED), false, new Random(1)).size());
    }

    @Test
    void linearRepeatsTheColours() {
        List<Palette.Run> runs = Palette.paint("abcd", Palette.Mode.LINEAR, List.of(RED, BLUE), false, new Random(1));

        assertEquals(4, runs.size());
        assertEquals(List.of(RED, BLUE, RED, BLUE), runs.stream().map(Palette.Run::color).toList());
    }

    /** The colour of each character of the text. */
    private static List<TextColor> perCharacter(List<Palette.Run> runs) {
        List<TextColor> colors = new java.util.ArrayList<>();
        for (Palette.Run run : runs) {
            run.text().codePoints().forEach(ignored -> colors.add(run.color()));
        }
        return colors;
    }

    @Test
    void linearCanSkipSpaces() {
        List<TextColor> normal = perCharacter(Palette.paint("a b c", Palette.Mode.LINEAR, List.of(RED, BLUE), false, new Random(1)));
        List<TextColor> skipping = perCharacter(Palette.paint("a b c", Palette.Mode.LINEAR, List.of(RED, BLUE), true, new Random(1)));

        // The spaces use up a colour: a, space, b, space, c = RED, BLUE, RED, BLUE, RED
        assertEquals(List.of(RED, BLUE, RED, BLUE, RED), normal);
        // Skipping them the letters take turns: a = RED, b = BLUE, c = RED
        assertEquals(RED, skipping.get(0));
        assertEquals(BLUE, skipping.get(2));
        assertEquals(RED, skipping.get(4));
    }

    @Test
    void randomOnlyUsesTheGivenColours() {
        List<Palette.Run> runs = Palette.paint("some longer text here", Palette.Mode.RANDOM, List.of(RED, BLUE), false, new Random(7));

        assertEquals("some longer text here", joined(runs));
        assertTrue(runs.stream().allMatch(run -> run.color().equals(RED) || run.color().equals(BLUE)));
        assertTrue(runs.size() > 1);
    }

    @Test
    void rainbowNeedsNoColours() {
        List<Palette.Run> runs = Palette.paint("rainbow", Palette.Mode.RAINBOW, List.of(), false, new Random(1));

        assertEquals("rainbow", joined(runs));
        assertEquals(7, runs.size());
    }

    @Test
    void nothingToPaintGivesNothing() {
        assertTrue(Palette.paint("", Palette.Mode.GRADIENT, List.of(RED, BLUE), false, new Random(1)).isEmpty());
        assertTrue(Palette.paint("text", Palette.Mode.GRADIENT, List.of(), false, new Random(1)).isEmpty());
    }

    @Test
    void emojiAreNotSplitInHalf() {
        List<Palette.Run> runs = Palette.paint("a😀b", Palette.Mode.LINEAR, List.of(RED, BLUE), false, new Random(1));

        assertEquals("a😀b", joined(runs));
        assertEquals(3, runs.size());
    }
}
