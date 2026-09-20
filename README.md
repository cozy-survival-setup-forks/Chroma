# Spectrum

Chat colours and name gradients for Paper 1.21+, each defined in its own simple file. There is no built-in menu:
build one with DeluxeMenus (or any menu plugin) using the commands and placeholders below.

## Files

| File | What it holds |
| --- | --- |
| `chatcolors.yml` | The colours of chat messages |
| `namegradients.yml` | The gradients of player names |
| `config.yml` | Permissions, name source, permission commands |
| `messages.yml` | Every message the plugin sends |

Both style files work the same way. A style is written in one of two ways.

**With colours**

```yaml
ocean:
  display: "Ocean"
  mode: GRADIENT          # SINGLE, GRADIENT, LINEAR, RANDOM or RAINBOW
  colors: ["#00c6ff", "#0072ff"]
  bold: true              # also italic, underline, strikethrough, obfuscated
```

**With a MiniMessage format** (`{text}` in chatcolors.yml, `{name}` in namegradients.yml)

```yaml
glitch:
  display: "Glitch"
  format: "<gradient:#00ffff:#ff00ff><obfuscated>||</obfuscated> {text} <obfuscated>||</obfuscated></gradient>"
```

`<obfuscated>` makes the letters inside it flicker, which is what gives the glitch look.

Both files come with examples: gradients, single colours, rainbow, alternating and random colours, and glitch,
matrix, frost, lava and neon formats.

## Commands

| Command | Use |
| --- | --- |
| `/chatcolor equip <id>`, `unequip`, `list` | Pick a chat colour |
| `/namegradient equip <id>`, `unequip`, `list` | Pick a name gradient |
| `/namegradient admin give\|remove <player> <id>` | Give or take a style (runs the commands set in `config.yml`) |
| `/spectrum reload` | Reload all files |
| `/spectrum preview <chat\|name> <id>` | Preview a style |

## Permissions

- `spectrum.chat.<id>` and `spectrum.name.<id>`: use one style (or set your own with `permission:` on the style)
- `spectrum.chat.*` and `spectrum.name.*`: use all of them (op by default)
- `spectrum.admin`: reload, preview, give and remove
- `spectrum.chat.codes`: write colour codes and MiniMessage in chat (needs `chat.allow-color-codes: true`)

Set `use-permissions: false` in `config.yml` to let everybody use every style.

## Placeholders (PlaceholderAPI)

Every placeholder gives colours as `&#rrggbb` codes, which DeluxeMenus and most chat, tab and scoreboard plugins understand.

**Chat colours** need no placeholder: a player's message is coloured as it is sent, whatever chat plugin you use.

**Name gradients** are not applied by Spectrum itself. Put `%spectrum_name%` where the name should go in your chat, tab or scoreboard plugin. A player without a gradient just gets their plain name.

**For menus**, replace `<kind>` with `chat` or `name`:

- `%spectrum_<kind>_id%` and `%spectrum_<kind>_display%`: what the player has equipped
- `%spectrum_<kind>_equipped_<id>%` and `%spectrum_<kind>_owned_<id>%`: `true` or `false`
- `%spectrum_<kind>_preview_<id>%`: a preview of a style, using the player's name for `name` and the sample text from `config.yml` for `chat`

## DeluxeMenus example

```yaml
items:
  ocean:
    material: LIGHT_BLUE_DYE
    slot: 10
    display_name: "%spectrum_chat_preview_ocean%"
    left_click_commands:
      - "[player] chatcolor equip ocean"
```

## Building

```
./gradlew build
```

The jar is in `build/libs`. Licensed under MIT.
