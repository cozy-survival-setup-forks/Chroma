package dev.spectrum.style;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;

/**
 * A way to colour text, defined in chatcolors.yml or namegradients.yml. It is either a MiniMessage template
 * ({@link TemplateStyle}) or a list of colours with a mode ({@link PaletteStyle}).
 */
public abstract sealed class Style permits TemplateStyle, PaletteStyle {

    protected static final MiniMessage MINI = MiniMessage.miniMessage();

    private static final LegacyComponentSerializer LEGACY = LegacyComponentSerializer.builder()
            .character(LegacyComponentSerializer.SECTION_CHAR).hexColors().useUnusualXRepeatedCharacterHexFormat().build();
    private static final LegacyComponentSerializer AMPERSAND = LegacyComponentSerializer.builder()
            .character(LegacyComponentSerializer.AMPERSAND_CHAR).hexColors().build();

    private final StyleKind kind;
    private final String id;
    private final String display;
    private final String permission;

    protected Style(StyleKind kind, String id, String display, String permission) {
        this.kind = kind;
        this.id = id;
        this.display = display;
        this.permission = permission;
    }

    public StyleKind kind() {
        return kind;
    }

    public String id() {
        return id;
    }

    /** The name of the style as players see it. It is coloured with the style itself. */
    public String display() {
        return display;
    }

    public String permission() {
        return permission;
    }

    /**
     * The text coloured with this style.
     *
     * @param allowTags whether the text may contain MiniMessage tags. Only for text of players who are allowed to.
     */
    public abstract Component render(String text, boolean allowTags);

    /** The text coloured with this style, written as MiniMessage. Tags in the text itself are escaped. */
    public abstract String miniMessage(String text);

    public Component render(String text) {
        return render(text, false);
    }

    /** The name of the style, coloured. */
    public Component displayComponent() {
        return render(display);
    }

    /** The text with § colour codes, for plugins that do not know MiniMessage. */
    public String legacy(String text) {
        return LEGACY.serialize(render(text));
    }

    /** The text with &amp;#rrggbb colour codes, which most menu plugins understand. */
    public String ampersand(String text) {
        return AMPERSAND.serialize(render(text));
    }
}
