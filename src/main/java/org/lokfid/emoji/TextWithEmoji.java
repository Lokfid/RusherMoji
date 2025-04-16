package org.lokfid.emoji;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.Component;
import org.lokfid.EmojiPlugin;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TextWithEmoji {
    private static final Pattern EMOJI_PATTERN = Pattern.compile(":[a-zA-Z0-9-_]+:");

    private final List<Component> component;
    private final List<Emoji> emojis;

    public TextWithEmoji(Component text) {
        this.component = new ArrayList<>();
        this.emojis = new ArrayList<>();

        text.visit((style, content) -> {
            parseEmoji(content, style);
            return Optional.empty();
        }, Style.EMPTY);
    }

    public int render(GuiGraphics context, Font textRenderer, int x, int y, int color) {
        int newX = x;

        int count = Math.min(component.size(), emojis.size());
        for (int i = 0; i < count; i++) {
            context.drawString(textRenderer, component.get(i)  , newX, y, color);
            newX += textRenderer.width(component.get(i));
            Emoji emoji = emojis.get(i);
            if (emoji != null) {
                emoji.render(context, textRenderer, newX, y, color);
                newX += textRenderer.lineHeight;
            }
        }
        return newX;
    }

    private void parseEmoji(String string, Style style) {
        Matcher matcher = EMOJI_PATTERN.matcher(string);

        int end = 0;
        while (matcher.find()) {
            String before = string.substring(end, matcher.start());
            String emoji = matcher.group();
            if (EmojiPlugin.EMOJIS.containsKey(emoji)) {
                this.component.add(Component.literal(before).setStyle(style));
                this.emojis.add(EmojiPlugin.EMOJIS.get(emoji));
            } else { // Invalid emoji, put back in the text
                this.component.add(Component.literal(before + emoji).setStyle(style));
                this.emojis.add(null);
            }
            end = matcher.end();
        }

        if (end < string.length()) {
            this.component.add(Component.literal(string.substring(end)).setStyle(style)); // end of text
            this.emojis.add(null); // empty emoji
        }
    }

}
