package dev.portero.atlas.util;

import dev.portero.atlas.util.font.FontInfo;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.Style;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;

/**
 * Utility class for text message formatting, centering, and decoration in chat interfaces.
 * <p>
 * This class provides methods to format text with legacy color codes, center messages
 * in the chat window, and create decorative text with strikethrough lines.
 */
public final class MessageUtil {

    private static final int CHAT_WIDTH_PX = 320;
    private static final int SPACE_WIDTH_PX = 4;
    private static final LegacyComponentSerializer LEGACY_SERIALIZER = LegacyComponentSerializer.legacyAmpersand();
    private static final PlainTextComponentSerializer PLAIN_TEXT_SERIALIZER = PlainTextComponentSerializer.plainText();

    /**
     * Converts a string with legacy color codes (using &) into a Component.
     *
     * @param text The text containing legacy color codes to format
     * @return A formatted Component with applied colors and styles
     */
    public static Component format(String text) {
        return LEGACY_SERIALIZER.deserialize(text);
    }

    /**
     * Formats a string with legacy color codes and replaces format specifiers with provided arguments.
     *
     * @param text The text containing legacy color codes and format specifiers
     * @param args The arguments to insert into format specifiers
     * @return A formatted Component with arguments inserted and colors applied
     */
    public static Component format(String text, Object... args) {
        return LEGACY_SERIALIZER.deserialize(String.format(text, args));
    }

    /**
     * Centers a Component in the chat window by adding spaces before it.
     * <p>
     * The method calculates the pixel width of the message and adds enough
     * spaces before it to position it in the center of the chat window.
     *
     * @param message The Component to center
     * @return A new Component with spaces prepended to center the original message
     */
    public static Component center(Component message) {
        String plainText = PLAIN_TEXT_SERIALIZER.serialize(message);
        int spaceCount = calculatePaddingSpaces(plainText);

        return Component.text(" ".repeat(spaceCount)).append(message);
    }

    /**
     * Centers a string message in the chat window after formatting it.
     * <p>
     * This convenience method first formats the string with legacy color codes
     * and then centers the resulting Component.
     *
     * @param message The message with legacy color codes to center
     * @return A new Component with the formatted message centered
     */
    public static Component center(String message) {
        return center(format(message));
    }

    /**
     * Creates a centered component with decorative strikethrough lines on both sides.
     * <p>
     * This method produces output that looks like: {@code ----------------[ Message ]------------------}
     * where the dashes are strikethrough lines of the specified color and the message is centered.
     *
     * @param lineColor The color to apply to the decorative strikethrough lines
     * @param component The Component to place between decorative lines
     * @return A new Component with the original content surrounded by colored strikethrough lines
     */
    public static Component centerDecorated(TextColor lineColor, Component component) {
        String plainText = PLAIN_TEXT_SERIALIZER.serialize(component);
        int spaceCount = calculatePaddingSpaces(plainText);

        // Handle empty text case - fill the entire width with decoration
        if (plainText.isEmpty()) {
            spaceCount = CHAT_WIDTH_PX / SPACE_WIDTH_PX;
        }

        Style decorationStyle = Style.style(lineColor, TextDecoration.STRIKETHROUGH);
        TextComponent decoration = Component.text(" ".repeat(spaceCount), decorationStyle);

        if (!plainText.isEmpty()) {
            return Component.empty()
                    .append(decoration)
                    .append(component)
                    .append(decoration);
        } else {
            return Component.empty().append(decoration);
        }
    }

    /**
     * Creates a centered decorated text from a string with legacy color codes.
     * <p>
     * This convenience method formats the string before surrounding it with
     * decorative strikethrough lines. The result looks like: {@code ----------------[ Message ]------------------}
     *
     * @param lineColor The color to apply to the decorative strikethrough lines
     * @param message   The message with legacy color codes to center between decorative lines
     * @return A new Component with the formatted message surrounded by colored strikethrough lines
     */
    public static Component centerDecorated(TextColor lineColor, String message) {
        return centerDecorated(lineColor, format(message));
    }

    /**
     * Calculates the number of spaces needed to center text in the chat window.
     *
     * @param text The text to center
     * @return The number of spaces needed for padding
     */
    private static int calculatePaddingSpaces(String text) {
        int messagePxSize = calculateMessageWidth(text);
        int paddingPx = (CHAT_WIDTH_PX - messagePxSize) / 2;
        return Math.max(0, paddingPx / SPACE_WIDTH_PX);
    }

    /**
     * Calculates the pixel width of a message based on the font characteristics.
     * <p>
     * This method iterates through each character in the text and sums their widths
     * plus a 1-pixel spacing between characters, then subtracts 1 pixel to account for
     * no spacing after the last character.
     *
     * @param text The text to calculate width for
     * @return The width of the text in pixels
     */
    private static int calculateMessageWidth(String text) {
        int width = 0;
        for (char c : text.toCharArray()) {
            width += FontInfo.fromChar(c).getWidth() + 1;
        }
        return Math.max(0, width - 1);
    }
}