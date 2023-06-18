package org.teacon.loongboat.mixin;

import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.teacon.loongboat.client.model.entity.LoongBoatEntityModel;
import software.bernie.geckolib.core.animatable.GeoAnimatable;
import software.bernie.geckolib.core.animatable.model.CoreGeoModel;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.model.GeoModel;

@Mixin(GeoModel.class)
public abstract class MixinGeoModel<T extends GeoAnimatable> implements CoreGeoModel<T> {
    @Redirect(method = "handleAnimations",
            remap = false,
            at = @At(value = "INVOKE",
                    ordinal = 1,
                    remap = false,
                    target = "Lsoftware/bernie/geckolib/core/animation/AnimatableManager;getFirstTickTime()D"))
    private double modifyReturnValue(AnimatableManager<?> animatableManager) {
        //noinspection ConstantValue
        return (((Object)this) instanceof LoongBoatEntityModel)
                ? -Minecraft.getInstance().getFrameTime() // fix with partial tick
                : animatableManager.getFirstTickTime(); // default value (idk why)
    }
}
