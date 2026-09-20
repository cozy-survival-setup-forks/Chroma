package dev.chroma.style;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.configuration.file.YamlConfiguration;
import org.junit.jupiter.api.Test;

import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class StyleTest {

    private static final Logger LOG = Logger.getLogger("test");

    private static String plain(Component component) {
        return PlainTextComponentSerializer.plainText().serialize(component);
    }

    private static YamlConfiguration yaml(String text) throws Exception {
        YamlConfiguration yaml = new YamlConfiguration();
        yaml.loadFromString(text);
        return yaml;
    }

    private static StyleLibrary library(StyleKind kind, String text) throws Exception {
        return StyleLibrary.load(kind, yaml(text), LOG);
    }

    // ---- reading entries ----

    @Test
    void anEntryWithAFormatIsATemplate() throws Exception {
        StyleLibrary library = library(StyleKind.CHAT, "styles:\n  glitch:\n    format: '<red><obfuscated>|</obfuscated> {text}'\n");

        Style style = library.get("glitch");
        assertInstanceOf(TemplateStyle.class, style);
        assertEquals("Glitch", style.display());
        assertEquals("chroma.chat.glitch", style.permission());
    }

    @Test
    void anEntryWithColoursIsAPalette() throws Exception {
        StyleLibrary library = library(StyleKind.NAME, "styles:\n  ocean:\n    mode: GRADIENT\n    colors: ['#0000ff', '#00ffff']\n    bold: true\n");

        Style style = library.get("ocean");
        assertInstanceOf(PaletteStyle.class, style);
        assertEquals("chroma.name.ocean", style.permission());
        assertEquals(StyleKind.NAME, style.kind());
    }

    @Test
    void theOldNameGradientKeyStillWorks() throws Exception {
        StyleLibrary library = library(StyleKind.NAME, "styles:\n  groovy:\n    display: groovy\n    pattern: '<reset><#ff0000>{name}'\n");

        Style style = library.get("groovy");
        assertInstanceOf(TemplateStyle.class, style);
        assertEquals("Steve", plain(style.render("Steve")));
    }

    @Test
    void aModeWithoutColoursOrFormatIsSkipped() throws Exception {
        StyleLibrary library = library(StyleKind.CHAT, "styles:\n  broken:\n    mode: GRADIENT\n  fine:\n    colors: ['#ff0000']\n");

        assertNull(library.get("broken"));
        assertNotNull(library.get("fine"));
    }

    @Test
    void rainbowNeedsNoColours() throws Exception {
        assertNotNull(library(StyleKind.CHAT, "styles:\n  rainbow:\n    mode: RAINBOW\n").get("rainbow"));
    }

    @Test
    void aWrongColourIsSkippedButTheRestStay() throws Exception {
        Style style = library(StyleKind.CHAT, "styles:\n  mixed:\n    colors: ['nonsense', '#ff0000']\n").get("mixed");

        assertNotNull(style);
        assertEquals("hi", plain(style.render("hi")));
    }

    @Test
    void customPermissionsAndIdsAreCleaned() throws Exception {
        StyleLibrary library = library(StyleKind.CHAT, "styles:\n  'My Style!':\n    colors: ['#fff000']\n    permission: 'ranks.vip.{id}'\n");

        Style style = library.get("mystyle");
        assertNotNull(style);
        assertEquals("ranks.vip.mystyle", style.permission());
    }

    @Test
    void aDefaultThatDoesNotExistIsIgnored() throws Exception {
        assertEquals("ocean", library(StyleKind.CHAT, "default: Ocean\nstyles:\n  ocean:\n    colors: ['#0000ff']\n").defaultId());
        assertEquals("", library(StyleKind.CHAT, "default: nope\nstyles:\n  ocean:\n    colors: ['#0000ff']\n").defaultId());
    }

    @Test
    void theExampleFilesLoadWithoutProblems() {
        for (StyleKind kind : StyleKind.values()) {
            var stream = getClass().getResourceAsStream("/" + kind.fileName());
            assertNotNull(stream, kind.fileName());
            YamlConfiguration yaml = YamlConfiguration.loadConfiguration(new InputStreamReader(stream, StandardCharsets.UTF_8));

            StyleLibrary library = StyleLibrary.load(kind, yaml, new Logger("strict", null) {
                @Override
                public void warning(String msg) {
                    throw new AssertionError(kind.fileName() + " has a problem: " + msg);
                }
            });

            assertTrue(library.all().size() >= 10, kind.fileName() + " has " + library.all().size() + " styles");
            for (Style style : library.all()) {
                assertFalse(plain(style.render("Some Text")).isBlank(), style.id());
                assertFalse(plain(style.displayComponent()).isBlank(), style.id());
                assertFalse(style.display().contains("<"), style.id() + " display must be plain text");
            }
        }
    }

    // ---- rendering ----

    @Test
    void aTemplateColoursTheText() throws Exception {
        Style style = library(StyleKind.CHAT, "styles:\n  red:\n    format: '<red>{text}'\n").get("red");

        Component component = style.render("hello");

        assertEquals("hello", plain(component));
        assertEquals(net.kyori.adventure.text.format.NamedTextColor.RED, component.children().isEmpty() ? component.color() : component.children().get(0).color());
    }

    @Test
    void aTemplateWithoutAPlaceholderPutsTheTextAtTheEnd() throws Exception {
        Style style = library(StyleKind.CHAT, "styles:\n  red:\n    format: '<red>'\n").get("red");

        assertEquals("hello", plain(style.render("hello")));
    }

    @Test
    void textCannotSneakInTagsUnlessAllowed() throws Exception {
        Style style = library(StyleKind.CHAT, "styles:\n  red:\n    format: '<red>{text}'\n").get("red");

        assertEquals("<blue>hi</blue>", plain(style.render("<blue>hi</blue>", false)));
        assertEquals("hi", plain(style.render("<blue>hi</blue>", true)));
    }

    @Test
    void allowedTagsCannotClickHoverOrBreakOutOfTheStyle() throws Exception {
        Style style = library(StyleKind.CHAT, "styles:\n  red:\n    format: '<red>{text}</red> after'\n").get("red");

        Component component = style.render("<click:run_command:/op me>hi</click></red><hover:show_text:'x'>yo", true);

        assertFalse(hasInteraction(component));
        assertTrue(plain(component).endsWith(" after"));
        assertEquals("hi after", plain(style.render("<green>hi</green>", true)));
    }

    private static boolean hasInteraction(Component component) {
        if (component.clickEvent() != null || component.hoverEvent() != null || component.insertion() != null) return true;
        return component.children().stream().anyMatch(StyleTest::hasInteraction);
    }

    @Test
    void gradientAndObfuscatedTemplatesKeepTheText() throws Exception {
        Style style = library(StyleKind.CHAT,
                "styles:\n  glitch:\n    format: '<gradient:#00ffff:#ff00ff><obfuscated>||</obfuscated> {text} <obfuscated>||</obfuscated></gradient>'\n").get("glitch");

        assertEquals("|| hello ||", plain(style.render("hello")));
        assertTrue(style.legacy("hello").contains("§k"));
    }

    @Test
    void aPaletteKeepsTheTextAndIgnoresTags() throws Exception {
        Style style = library(StyleKind.NAME, "styles:\n  ocean:\n    mode: GRADIENT\n    colors: ['#0000ff', '#00ffff']\n    bold: true\n").get("ocean");

        assertEquals("Steve", plain(style.render("Steve")));
        assertEquals("<red>x", plain(style.render("<red>x", true)));
        assertTrue(style.legacy("Steve").contains("§l"));
    }

    @Test
    void miniMessageOutputRoundTrips() throws Exception {
        Style palette = library(StyleKind.NAME, "styles:\n  ocean:\n    mode: GRADIENT\n    colors: ['#0000ff', '#00ffff']\n    bold: true\n").get("ocean");
        Style template = library(StyleKind.NAME, "styles:\n  glitch:\n    format: '<gradient:#00ffff:#ff00ff><obfuscated>|</obfuscated>{name}</gradient>'\n").get("glitch");

        for (Style style : new Style[]{palette, template}) {
            String mini = style.miniMessage("Steve <3");
            assertEquals(plain(style.render("Steve <3")), plain(MiniMessage.miniMessage().deserialize(mini)), mini);
        }
    }

    @Test
    void miniMessageOutputClosesWhatItOpens() throws Exception {
        Style palette = library(StyleKind.NAME, "styles:\n  ocean:\n    mode: GRADIENT\n    colors: ['#0000ff', '#00ffff']\n    bold: true\n").get("ocean");

        Component followedByText = MiniMessage.miniMessage().deserialize(palette.miniMessage("Steve") + " says hi");

        // The words after the name are not bold and not coloured.
        Component last = followedByText.children().get(followedByText.children().size() - 1);
        assertEquals(" says hi", plain(last));
        assertNull(last.color());
        assertEquals(net.kyori.adventure.text.format.TextDecoration.State.NOT_SET, last.decoration(net.kyori.adventure.text.format.TextDecoration.BOLD));
    }

    @Test
    void ampersandOutputUsesHexCodes() throws Exception {
        Style style = library(StyleKind.NAME, "styles:\n  red:\n    colors: ['#ff0000']\n").get("red");

        assertEquals("&#ff0000Steve", style.ampersand("Steve"));
    }
}
