package com.github.zly2006.reden.carpet;

import carpet.settings.ParsedRule;
import carpet.settings.Rule;
import carpet.settings.RuleCategory;
import carpet.settings.Validator;
import com.github.zly2006.reden.utils.DebugKt;
import com.github.zly2006.reden.utils.UtilsKt;
import net.minecraft.server.command.ServerCommandSource;

public class RedenCarpetSettings {
    private static final String CATEGORY_REDEN = "Reden-Undo";

    public static class Options {
        @Rule(
                category = {CATEGORY_REDEN, RuleCategory.CREATIVE},
                options = {"-1", "0", "52428800"}, // 50 MB
                strict = false,
                desc = "Memory size a player can use for undo. Default is 5MB, 0 to disable, -1 for infinite. (This setting was broken n 1.18)"
        )
        public static int allowedUndoSizeInBytes = 52428800;

        @Rule(
                category = {CATEGORY_REDEN, RuleCategory.CREATIVE},
                desc = "undoScheduledTicks"
        )
        public static boolean undoScheduledTicks = true;

        @Rule(
                category = {CATEGORY_REDEN, RuleCategory.CREATIVE},
                desc = "undoEntities"
        )
        public static boolean undoEntities = true;

        @Rule(
                category = {CATEGORY_REDEN, RuleCategory.CREATIVE},
                desc = "undoApplyingClearScheduledTicks"
        )
        public static boolean undoApplyingClearScheduledTicks = true;

        private static class DebugOptionObserver extends Validator<Boolean> {
            @Override
            public Boolean validate(ServerCommandSource serverCommandSource, ParsedRule<Boolean> parsedRule, Boolean newValue, String s) {
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
                category = {CATEGORY_REDEN},
                validate = {DebugOptionObserver.class},
                desc = "debug loggers"
        )
        public static boolean redenDebug = false;
    }
}
