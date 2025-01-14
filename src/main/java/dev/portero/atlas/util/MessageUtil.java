package dev.portero.atlas.util;

import dev.portero.atlas.text.FontInfo;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.Style;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;

public class MessageUtil {

    private static final int CHAT_WIDTH_PX = 320;
    private static final int SPACE_WIDTH_PX = 4;

    public static Component format(String key) {
        return LegacyComponentSerializer.legacyAmpersand().deserialize(key);
    }

    public static Component format(String key, Object... args) {
        return LegacyComponentSerializer.legacyAmpersand().deserialize(String.format(key, args));
    }

    public static Component center(Component message) {
        String plainText = PlainTextComponentSerializer.plainText().serialize(message);

        int messagePxSize = calculateMessageWidth(plainText);
        int paddingPx = (CHAT_WIDTH_PX - messagePxSize) / 2;
        int spaceCount = Math.max(0, paddingPx / SPACE_WIDTH_PX);

        return Component.text(" ".repeat(spaceCount)).append(message);
    }

    public static Component centerDecorated(TextColor color, String message) {
        Component textComponent = format(message);
        String plainText = PlainTextComponentSerializer.plainText().serialize(textComponent);

        int messagePxSize = calculateMessageWidth(plainText);
        int paddingPx = (CHAT_WIDTH_PX - messagePxSize) / 2;
        int spaceCount = Math.max(0, paddingPx / SPACE_WIDTH_PX);

        if (plainText.isEmpty()) {
            spaceCount = CHAT_WIDTH_PX / SPACE_WIDTH_PX;
        }

        Style style = Style.style(color, TextDecoration.STRIKETHROUGH);
        Style reset = Style.style(color);

        TextComponent decoration = Component.text(" ".repeat(spaceCount), style);

        return !plainText.isEmpty() ? Component.empty().append(decoration).append(textComponent).style(reset)
                .append(decoration).append(Component.empty()).style(reset) : Component.empty().append(decoration);
    }

    private static int calculateMessageWidth(String text) {
        int width = 0;
        for (char c : text.toCharArray()) {
            width += FontInfo.fromChar(c).getWidth() + 1;
        }
        return Math.max(0, width - 1);
    }
}
