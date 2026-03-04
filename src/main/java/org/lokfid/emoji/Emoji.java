package org.lokfid.emoji;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;

public class Emoji {

    private final ResourceLocation resourceLocation;

    public Emoji(ResourceLocation resourceLocation) {
        this.resourceLocation = resourceLocation;
    }

    public void render(GuiGraphics context, Font font, int x, int y, int color) {
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();

        // Pass color directly to blit so the fade alpha is applied to the texture
        context.blit(RenderType::guiTextured, resourceLocation, x, y - font.lineHeight / 8, 0, 0, font.lineHeight, font.lineHeight, font.lineHeight, font.lineHeight, color | 0xFFFFFF);

        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.disableBlend();
    }
}
