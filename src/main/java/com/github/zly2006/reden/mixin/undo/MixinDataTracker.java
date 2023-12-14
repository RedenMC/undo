package com.github.zly2006.reden.mixin.undo;

import com.github.zly2006.reden.carpet.RedenCarpetSettings;
import com.github.zly2006.reden.mixinhelper.UpdateMonitorHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(DataTracker.class)
public class MixinDataTracker {
    @Shadow @Final private Entity trackedEntity;

    @Inject(
            method = "set",
            at = @At("HEAD")
    )
    private <T> void beforeDataSet(TrackedData<T> key, T value, CallbackInfo ci) {
        if (trackedEntity.getWorld().isClient ||
                !RedenCarpetSettings.Options.undoEntities) return;
        UpdateMonitorHelper.tryAddRelatedEntity(trackedEntity);
    }
    @Inject(
            method = "set",
            at = @At("RETURN")
    )
    private <T> void afterDataSet(TrackedData<T> key, T value, CallbackInfo ci) {
        // empty
    }
}
