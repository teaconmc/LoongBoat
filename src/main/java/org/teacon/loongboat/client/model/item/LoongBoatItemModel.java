package org.teacon.loongboat.client.model.item;

import net.minecraft.resources.ResourceLocation;
import org.teacon.loongboat.LoongBoat;
import org.teacon.loongboat.world.entity.LoongBoatEntity;
import org.teacon.loongboat.world.item.LoongBoatItem;
import software.bernie.geckolib.model.GeoModel;

public class LoongBoatItemModel extends GeoModel<LoongBoatItem> {
    @Override
    public ResourceLocation getModelResource(LoongBoatItem animatable) {
        return new ResourceLocation(LoongBoat.MODID, "geo/entity/" + LoongBoatEntity.ENTITY_NAME + "_0.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(LoongBoatItem animatable) {
        return new ResourceLocation(LoongBoat.MODID, "textures/entity/" + LoongBoatEntity.ENTITY_NAME + ".png");
    }

    @Override
    public ResourceLocation getAnimationResource(LoongBoatItem animatable) {
        return new ResourceLocation(LoongBoat.MODID, "animations/entity/" + LoongBoatEntity.ENTITY_NAME + "_0.animation.json");
    }
}
