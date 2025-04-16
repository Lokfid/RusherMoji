package org.lokfid.emoji;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import org.lokfid.EmojiPlugin;

public class Emoji {

    private final ResourceLocation resourceLocation;

    public Emoji(ResourceLocation resourceLocation) {
        this.resourceLocation = resourceLocation;
    }

    public void render(GuiGraphics context, Font font, int x, int y, int color){

        //So context.blit doesn't have a way to make the texture transparent. So we do this
        float alpha = ((color >> 24) & 0xFF) / 255.0F;
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, alpha);

        //Render the texture itself
        context.blit(resourceLocation, x, y - font.lineHeight / 8, 0, 0, font.lineHeight, font.lineHeight, font.lineHeight, font.lineHeight);

        RenderSystem.disableBlend();
    }
}
