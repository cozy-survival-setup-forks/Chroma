package dev.chroma.style;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.standard.StandardTags;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;

/**
 * A style written as MiniMessage, with {@code {text}} (or {@code {name}}) where the text goes. Anything MiniMessage
 * can do works: gradients, rainbows, obfuscated "glitch" letters, hover text and so on.
 */
public final class TemplateStyle extends Style {

    private static final String TAG = "chroma_text";

    /**
     * What a player may write in chat when colour codes are allowed: colours, decorations, gradients and rainbows,
     * and nothing that clicks, hovers, inserts text or reads the server.
     */
    private static final MiniMessage SAFE = MiniMessage.builder()
            .tags(net.kyori.adventure.text.minimessage.tag.resolver.TagResolver.builder()
                    .resolvers(StandardTags.color(), StandardTags.decorations(), StandardTags.gradient(),
                            StandardTags.rainbow(), StandardTags.reset())
                    .build())
            .build();

    private final String template;

    public TemplateStyle(StyleKind kind, String id, String display, String permission, String template) {
        super(kind, id, display, permission);
        this.template = withPlaceholder(template);
    }

    /** Does the template have a place for the text? If it does not, the text goes at the end. */
    public static boolean hasPlaceholder(String template) {
        return template.contains("{text}") || template.contains("{name}");
    }

    private static String withPlaceholder(String template) {
        String result = hasPlaceholder(template) ? template : template + "{text}";
        return result.replace("{text}", "{name}");
    }

    @Override
    public Component render(String text, boolean allowTags) {
        TagResolver value = allowTags ? Placeholder.component(TAG, SAFE.deserialize(text)) : Placeholder.unparsed(TAG, text);
        return MINI.deserialize(template.replace("{name}", "<" + TAG + ">"), value);
    }

    @Override
    public String miniMessage(String text) {
        return template.replace("{name}", MINI.escapeTags(text));
    }
}
