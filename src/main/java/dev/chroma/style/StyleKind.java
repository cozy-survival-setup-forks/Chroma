package dev.chroma.style;

/**
 * The two things a player can style: the text of their chat messages and their name.
 */
public enum StyleKind {

    CHAT("chatcolors.yml", "chat", "chatcolor", "chroma.chat.{id}"),
    NAME("namegradients.yml", "name", "namegradient", "chroma.name.{id}");

    private final String fileName;
    private final String key;
    private final String command;
    private final String defaultPermission;

    StyleKind(String fileName, String key, String command, String defaultPermission) {
        this.fileName = fileName;
        this.key = key;
        this.command = command;
        this.defaultPermission = defaultPermission;
    }

    /** The file the styles of this kind are defined in. */
    public String fileName() {
        return fileName;
    }

    /** Used in placeholders (%chroma_name_id%) and to save what a player picked. */
    public String key() {
        return key;
    }

    /** The command that manages this kind. */
    public String command() {
        return command;
    }

    /** The permission of a style that does not set its own. {id} is the id of the style. */
    public String defaultPermission() {
        return defaultPermission;
    }
}
