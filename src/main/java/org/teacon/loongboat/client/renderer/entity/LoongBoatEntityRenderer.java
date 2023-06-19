package org.teacon.loongboat.client.renderer.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import org.teacon.loongboat.client.model.entity.LoongBoatEntityModel;
import org.teacon.loongboat.world.entity.LoongBoatEntity;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

public class LoongBoatEntityRenderer extends GeoEntityRenderer<LoongBoatEntity> {
    private final LoongBoatEntityModel.VanillaModel vanillaModel;

    public LoongBoatEntityRenderer(EntityRendererProvider.Context context) {
        super(context, new LoongBoatEntityModel());
        ModelLayerLocation modelLayerLocation = LoongBoatEntityModel.VanillaModel.getModelLayerLocation();
        var modelPart = context.bakeLayer(modelLayerLocation);
        this.vanillaModel = new LoongBoatEntityModel.VanillaModel(modelPart);
    }

    @SuppressWarnings("NullableProblems")
    @Override
    public void render(LoongBoatEntity loongBoatEntity, float entityYaw, float partialTick, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight) {
        poseStack.pushPose();

        poseStack.mulPose(Axis.YP.rotationDegrees(-entityYaw));
        super.render(loongBoatEntity, entityYaw, partialTick, poseStack, bufferSource, packedLight);

        poseStack.mulPose(Axis.YP.rotationDegrees(270));
        poseStack.translate(-0.685 * loongBoatEntity.getSize(), 0.375F, 0);
        if (!loongBoatEntity.isUnderWater()) {
            VertexConsumer vc = bufferSource.getBuffer(RenderType.waterMask());
            this.vanillaModel.waterPatch(loongBoatEntity).render(poseStack, vc, packedLight, OverlayTexture.NO_OVERLAY);
        }

        poseStack.popPose();
    }
}
