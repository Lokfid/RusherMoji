package org.lokfid.emoji;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;

public class Emoji {

    private final ResourceLocation resourceLocation;

    public Emoji(ResourceLocation resourceLocation) {
        this.resourceLocation = resourceLocation;
    }

    public void render(GuiGraphics context, Font font, int x, int y, int color){
        context.blit(resourceLocation, x, y - font.lineHeight / 8, 0, 0, font.lineHeight, font.lineHeight, font.lineHeight, font.lineHeight);
    }
}
