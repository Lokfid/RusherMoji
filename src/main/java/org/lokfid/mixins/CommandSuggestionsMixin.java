package org.lokfid.mixins;

import net.minecraft.client.gui.components.CommandSuggestions;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.multiplayer.ClientSuggestionProvider;
import org.lokfid.EmojiPlugin;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Slice;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.*;

@Mixin(CommandSuggestions.class)
public abstract class CommandSuggestionsMixin {
    @Shadow
    @Final
    EditBox input;

    @Shadow
    private static int getLastWordIndex(String text) {
        throw new AssertionError();
    }

    @Shadow
    public abstract void showSuggestions(boolean narrateFirstSuggestion);

    @Unique
    private boolean shouldShow;

    @Inject(method = "updateCommandInfo()V", at = @At(value = "FIELD",
            target = "Lnet/minecraft/client/gui/components/CommandSuggestions;pendingSuggestions:Ljava/util/concurrent/CompletableFuture;",
            opcode = Opcodes.PUTFIELD,
            shift = At.Shift.AFTER,
            ordinal = 0),
            slice = @Slice(from = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/components/CommandSuggestions;getLastWordIndex(Ljava/lang/String;)I")))
    private void afterGetChatSuggestions(CallbackInfo ci) {
        if (shouldShow) showSuggestions(true);
    }

    @WrapOperation(method = "updateCommandInfo", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/multiplayer/ClientSuggestionProvider;getCustomTabSugggestions()Ljava/util/Collection;"))
    private Collection<String> suggest(ClientSuggestionProvider instance, Operation<Collection<String>> original) {
        Set<String> suggestions = new HashSet<>(instance.getCustomTabSugggestions());

        String input = this.input.getValue();
        int cursorPos = this.input.getCursorPosition();

        String beforeCursor = input.substring(0, cursorPos);
        int currentWordStart = getLastWordIndex(beforeCursor);

        String currentWord = beforeCursor.substring(currentWordStart);

        if (currentWord.startsWith(":")) {
            List<String> emojiSuggestions = new ArrayList<>();

            for (var emojiName : EmojiPlugin.EMOJIS.keySet()) {
                if (emojiName.startsWith(currentWord)) emojiSuggestions.add(emojiName);
            }

            if (!emojiSuggestions.isEmpty()) {
                shouldShow = true;
                suggestions.addAll(emojiSuggestions);
                return suggestions;
            }
        }
        shouldShow = false;
        return suggestions;
    }
}

