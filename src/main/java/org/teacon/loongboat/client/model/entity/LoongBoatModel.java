package org.teacon.loongboat.client.model.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.WaterPatchModel;
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
import software.bernie.geckolib.model.DefaultedEntityGeoModel;

public class LoongBoatModel extends DefaultedEntityGeoModel<LoongBoatEntity> {
    public LoongBoatModel() {
        super(new ResourceLocation(LoongBoat.MODID, LoongBoatEntity.ENTITY_NAME));
    }

    public static class VanillaModel extends EntityModel<Boat> implements WaterPatchModel {

        private static final String WATER_PATCH_NAME = "water_patch";
        private static final int TEXTURE_WIDTH = 256;
        private static final int TEXTURE_HEIGHT = 256;
        private final ModelPart waterPatch;

        public VanillaModel(ModelPart modelPart) {
            this.waterPatch = modelPart.getChild(WATER_PATCH_NAME);
        }

        public static LayerDefinition createBodyModel() {
            var meshDefinition = new MeshDefinition();
            var partDefinition = meshDefinition.getRoot();
            partDefinition.addOrReplaceChild(WATER_PATCH_NAME, CubeListBuilder.create()
                            .texOffs(0, 0)
                            .addBox(-14, -9, -3, 28, 16, 3),
                    PartPose.offsetAndRotation(0, -3, 1, ((float)Math.PI / 2), 0, 0));
            return LayerDefinition.create(meshDefinition,  TEXTURE_WIDTH, TEXTURE_HEIGHT);
        }

        public static ModelLayerLocation getModelLayerLocation() {
            return new ModelLayerLocation(new ResourceLocation(LoongBoat.MODID, LoongBoatEntity.ENTITY_NAME), "main");
        }

        @Override
        public void setupAnim(Boat p_102618_, float p_102619_, float p_102620_, float p_102621_, float p_102622_, float p_102623_) {}

        @Override
        public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer, int p_103113_, int p_103114_, float p_103115_, float p_103116_, float p_103117_, float p_103118_) {
            this.waterPatch.render(poseStack, vertexConsumer, p_103113_, p_103114_, p_103115_, p_103116_, p_103117_, p_103118_);
        }

        @SuppressWarnings("NullableProblems")
        @Override
        public ModelPart waterPatch() {
            return waterPatch;
        }
    }
}
