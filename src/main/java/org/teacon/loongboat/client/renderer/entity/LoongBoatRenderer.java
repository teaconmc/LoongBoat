package org.teacon.loongboat.client.renderer.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import org.teacon.loongboat.LoongBoat;
import org.teacon.loongboat.client.model.entity.LoongBoatModel;
import org.teacon.loongboat.world.entity.LoongBoatEntity;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

public class LoongBoatRenderer extends GeoEntityRenderer<LoongBoatEntity> {
    private final LoongBoatModel.VanillaModel vanillaModel;

    public LoongBoatRenderer(EntityRendererProvider.Context context) {
        super(context, new LoongBoatModel());
        ModelLayerLocation modelLayerLocation = LoongBoatModel.VanillaModel.getModelLayerLocation();
        var modelPart = context.bakeLayer(modelLayerLocation);
        this.vanillaModel = new LoongBoatModel.VanillaModel(modelPart);
    }

    @SuppressWarnings("NullableProblems")
    @Override
    public ResourceLocation getTextureLocation(LoongBoatEntity animatable) {
        return new ResourceLocation(LoongBoat.MODID, "textures/entity/"+LoongBoatEntity.ENTITY_NAME +".png");
    }

    @Override
    public void render(LoongBoatEntity entity, float entityYaw, float partialTick, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight) {
        poseStack.pushPose();

        poseStack.mulPose(Axis.YP.rotationDegrees(-entityYaw));
        super.render(entity, entityYaw, partialTick, poseStack, bufferSource, packedLight);

        poseStack.mulPose(Axis.YP.rotationDegrees(270));
        poseStack.translate(0, 0.375F, 0);
        if (!entity.isUnderWater()) {
            VertexConsumer vc = bufferSource.getBuffer(RenderType.waterMask());
            this.vanillaModel.waterPatch().render(poseStack, vc, packedLight, OverlayTexture.NO_OVERLAY);
        }

        poseStack.popPose();
    }
}
