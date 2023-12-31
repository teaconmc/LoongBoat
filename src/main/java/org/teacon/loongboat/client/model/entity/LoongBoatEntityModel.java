package org.teacon.loongboat.client.model.entity;

import com.google.common.collect.ImmutableList;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.vehicle.Boat;
import org.teacon.loongboat.LoongBoat;
import org.teacon.loongboat.world.entity.LoongBoatEntity;
import software.bernie.geckolib.core.animatable.model.CoreGeoBone;
import software.bernie.geckolib.core.animation.AnimationState;
import software.bernie.geckolib.model.GeoModel;

public class LoongBoatEntityModel extends GeoModel<LoongBoatEntity> {

    @Override
    public ResourceLocation getModelResource(LoongBoatEntity loongBoatEntity) {
        return new ResourceLocation(LoongBoat.MODID, "geo/entity/" + LoongBoatEntity.ENTITY_NAME + "_" + loongBoatEntity.getSize() + ".geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(LoongBoatEntity loongBoatEntity) {
        return new ResourceLocation(LoongBoat.MODID, "textures/entity/" + LoongBoatEntity.ENTITY_NAME + ".png");
    }

    @Override
    public ResourceLocation getAnimationResource(LoongBoatEntity loongBoatEntity) {
        return new ResourceLocation(LoongBoat.MODID, "animations/entity/" + LoongBoatEntity.ENTITY_NAME + "_" + loongBoatEntity.getSize() + ".animation.json");
    }

    /**
     * Transform the boat's head when the player in first-person mode to avoid blocking view
     */
    @Override
    public void setCustomAnimations(LoongBoatEntity loongBoat, long instanceId, AnimationState<LoongBoatEntity> animationState) {
        if (loongBoat.isControllerFirstPerson()) {
            var head = getAnimationProcessor().getBone("dragon_head");
            head.setPosY(-5.5F);
            head.setPosZ(-1F);
            resizeBone(head, 0.8F);
            var horn = getAnimationProcessor().getBone("horn");
            var horn2 = getAnimationProcessor().getBone("horn2");
            horn.setRotX(0.3F);
            resizeBone(horn, 0.8F);
            horn2.setRotX(0.3F);
            resizeBone(horn2, 0.8F);
        }
    }

    private static void resizeBone(CoreGeoBone bone, float scale) {
        bone.setScaleX(scale);
        bone.setScaleY(scale);
        bone.setScaleZ(scale);
    }

    /**
     * Render a water patch to prevent rendering of water in boat
     */
    public static class VanillaModel extends EntityModel<Boat> {

        private static final String WATER_PATCH_NAME = "water_patch";
        private static final int TEXTURE_WIDTH = 256;
        private static final int TEXTURE_HEIGHT = 256;
        private final ImmutableList<ModelPart> waterPatchs;

        public VanillaModel(ModelPart modelPart) {
            var builder = ImmutableList.<ModelPart>builder();
            for (int size = 0; size < 3; size++) {
                builder.add(modelPart.getChild(WATER_PATCH_NAME + "_" + size));
            }
            this.waterPatchs = builder.build();
        }

        public static LayerDefinition createBodyModel() {
            var meshDefinition = new MeshDefinition();
            var partDefinition = meshDefinition.getRoot();
            for (int size = 0; size < 3; size++) {
                partDefinition.addOrReplaceChild(
                        WATER_PATCH_NAME + "_" + size,
                        CubeListBuilder.create().texOffs(0, 0)
                                .addBox(-11 * (size + 1), -9, -3,
                                        22 * (size + 1), 16, 3),
                        PartPose.offsetAndRotation(0, -3, 1,
                                ((float) Math.PI / 2), 0, 0));
            }
            return LayerDefinition.create(meshDefinition, TEXTURE_WIDTH, TEXTURE_HEIGHT);
        }

        public static ModelLayerLocation getModelLayerLocation() {
            return new ModelLayerLocation(new ResourceLocation(LoongBoat.MODID, LoongBoatEntity.ENTITY_NAME), "main");
        }

        @SuppressWarnings("NullableProblems")
        @Override
        public void setupAnim(Boat boat, float f1, float f2, float f3, float f4, float f5) {}

        @SuppressWarnings("NullableProblems")
        @Override
        public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer, int p_103113_, int p_103114_, float p_103115_, float p_103116_, float p_103117_, float p_103118_) {
            this.waterPatchs.forEach(patch -> patch.render(
                    poseStack, vertexConsumer, p_103113_, p_103114_, p_103115_, p_103116_, p_103117_, p_103118_));
        }

        public ModelPart waterPatch(LoongBoatEntity loongBoatEntity) {
            return this.waterPatchs.get(loongBoatEntity.getSize());
        }
    }
}
