package org.lokfid.mixins;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.ChatFormatting;
import net.minecraft.client.GuiMessage;
import net.minecraft.network.chat.*;
import net.minecraft.client.ComponentCollector;
import net.minecraft.client.GuiMessageTag;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.ChatComponent;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.Mth;
import org.lokfid.emoji.TextWithEmoji;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.rusherhack.client.api.Globals.mc;

@Mixin(value = ChatComponent.class, priority = Integer.MIN_VALUE)
public abstract class ChatComponentMixin {

    @Unique
    private final List<TextWithEmoji> hudMessages = new ArrayList<>();

    @Shadow public abstract int getWidth();


    @Shadow public abstract double getScale();

    @Unique
    private int s;

    @Inject(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiGraphics;drawString(Lnet/minecraft/client/gui/Font;Lnet/minecraft/util/FormattedCharSequence;III)I"))
    private void capture(GuiGraphics guiGraphics, int i, int j, int k, boolean bl, CallbackInfo ci, @Local(ordinal = 13) int s){
        this.s = s;
    }

    @WrapOperation(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiGraphics;drawString(Lnet/minecraft/client/gui/Font;Lnet/minecraft/util/FormattedCharSequence;III)I"))
        private int render(GuiGraphics instance, Font font, FormattedCharSequence text, int x, int y, int color, Operation<Integer> original){
            if (s >= 0 && s < hudMessages.size()) {
                return hudMessages.get(s).render(instance, font, x, y, color);
            }
            return x;
    }



//    @Inject(at = @At("TAIL"), method = "addMessage(Lnet/minecraft/network/chat/Component;Lnet/minecraft/network/chat/MessageSignature;ILnet/minecraft/client/GuiMessageTag;Z)V")
@Inject(at = @At("TAIL"), method = "addMessageToDisplayQueue")
    private void add(GuiMessage guiMessage, CallbackInfo ci){
        int i = Mth.floor(getWidth() / getScale());
        GuiMessageTag.Icon icon = guiMessage.icon();
        if (icon != null) {
            i -= icon.width + 4 + 2;
        }

        List<Component> breakText = breakRenderedChatMessageLines(guiMessage.content(), i, mc.font);

        if (breakText.isEmpty()) {
            hudMessages.add(0, new TextWithEmoji(Component.empty()));
        }
        for (Component text : breakText) {
            hudMessages.add(0, new TextWithEmoji(text));
        }
        while (hudMessages.size() > 5000) {
            hudMessages.remove(hudMessages.size() - 1);
        }

    }

    @Inject(at = @At("HEAD"), method = "clearMessages")
    private void clear(boolean clearSentMsgHistory, CallbackInfo ci){
        hudMessages.clear();
    }

    @Inject(at = @At("HEAD"), method = "refreshTrimmedMessages")
    private void refresh(CallbackInfo ci) {
        hudMessages.clear();
    }

    @Unique
    private static List<Component> breakRenderedChatMessageLines(Component message, int width, Font textRenderer) {
        ComponentCollector componentCollector = new ComponentCollector();
        message.visit((style, messagex) -> {
            componentCollector.append(FormattedText.of(getRenderedChatMessage(messagex), style));
            return Optional.empty();
        }, Style.EMPTY);
        List<Component> list = new ArrayList<>();
        textRenderer.getSplitter().splitLines(componentCollector.getResult(), width, Style.EMPTY, (text, lastLineWrapped) -> {
            MutableComponent newText = Component.empty();
            if (lastLineWrapped) {
                newText.append(" ");
            }
            text.visit((style, asString) -> {
                newText.append(Component.literal(asString).setStyle(style));
                return Optional.empty();
            }, Style.EMPTY);
            list.add(newText);
        });
        return list;
    }

    @Unique
    private static String getRenderedChatMessage(String message) {
        return mc.options.chatColors().get() ? message : ChatFormatting.stripFormatting(message);
    }
}
