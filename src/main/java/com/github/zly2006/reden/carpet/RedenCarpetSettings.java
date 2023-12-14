package com.github.zly2006.reden.carpet;

import carpet.api.settings.CarpetRule;
import carpet.api.settings.Rule;
import carpet.api.settings.RuleCategory;
import carpet.api.settings.Validator;
import com.github.zly2006.reden.utils.DebugKt;
import com.github.zly2006.reden.utils.UtilsKt;
import net.minecraft.server.command.ServerCommandSource;
import org.jetbrains.annotations.Nullable;

public class RedenCarpetSettings {
    private static final String CATEGORY_REDEN = "Reden-Undo";

    public static class Options {
        @Rule(
                categories = {CATEGORY_REDEN, RuleCategory.CREATIVE},
                options = {"-1", "0", "52428800"}, // 50 MB
                strict = false
        )
        public static int allowedUndoSizeInBytes = 52428800;

        @Rule(
                categories = {CATEGORY_REDEN, RuleCategory.CREATIVE}
        )
        public static boolean undoScheduledTicks = true;

        @Rule(
                categories = {CATEGORY_REDEN, RuleCategory.CREATIVE}
        )
        public static boolean undoEntities = true;

        @Rule(
                categories = {CATEGORY_REDEN, RuleCategory.CREATIVE}
        )
        public static boolean undoApplyingClearScheduledTicks = true;

        private static class DebugOptionObserver extends Validator<Boolean> {
            @Override
            public Boolean validate(@Nullable ServerCommandSource source, CarpetRule<Boolean> changingRule, Boolean newValue, String userInput) {
                if (!UtilsKt.isClient()) {
                    if (newValue) {
                        DebugKt.startDebugAppender();
                    } else {
                        DebugKt.stopDebugAppender();
                    }
                }
                return newValue;
            }
        }

        @Rule(
                categories = {CATEGORY_REDEN},
                validators = {DebugOptionObserver.class}
        )
        public static boolean redenDebug = false;
    }
}
