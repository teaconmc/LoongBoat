package org.teacon.loongboat;

import com.mojang.logging.LogUtils;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.BuildCreativeModeTabContentsEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import org.slf4j.Logger;
import org.teacon.loongboat.client.model.entity.LoongBoatEntityModel;
import org.teacon.loongboat.client.renderer.entity.LoongBoatEntityRenderer;
import org.teacon.loongboat.world.entity.LoongBoatEntity;
import org.teacon.loongboat.world.item.LoongBoatItem;

@Mod(LoongBoat.MODID)
public class LoongBoat {
    public static final String MODID = "loongboat";
    public static final Logger LOGGER = LogUtils.getLogger();
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, MODID);
    public static final DeferredRegister<EntityType<?>> ENTITIES  = DeferredRegister.create(ForgeRegistries.ENTITY_TYPES, MODID);

    public static final RegistryObject<LoongBoatItem> LOONG_BOAT_ITEM = ITEMS.register(LoongBoatItem.ITEM_NAME,
            LoongBoatItem::new);

    public static final RegistryObject<EntityType<LoongBoatEntity>> LOONG_BOAT_ENTITY = ENTITIES.register(LoongBoatEntity.ENTITY_NAME,
            () -> EntityType.Builder.<LoongBoatEntity>of(LoongBoatEntity::new, MobCategory.MISC)
                    .sized(1.375F, 0.5625F)
                    .clientTrackingRange(10)
                    .build(LoongBoatEntity.ENTITY_NAME));

    public LoongBoat() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();

        ITEMS.register(modEventBus);
        ENTITIES.register(modEventBus);

        MinecraftForge.EVENT_BUS.register(this);

        modEventBus.addListener(this::addCreative);
    }

    private void addCreative(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey() == CreativeModeTabs.TOOLS_AND_UTILITIES)
            event.accept(LOONG_BOAT_ITEM);
    }

    @Mod.EventBusSubscriber(modid = MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
    public static class ClientModEvents {
        @SubscribeEvent
        public static void registerRenderers(final EntityRenderersEvent.RegisterRenderers event) {
            event.registerEntityRenderer(LOONG_BOAT_ENTITY.get(), LoongBoatEntityRenderer::new);
        }

        @SubscribeEvent
        public static void registerLayerDefinitions(final EntityRenderersEvent.RegisterLayerDefinitions event) {
            event.registerLayerDefinition(LoongBoatEntityModel.VanillaModel.getModelLayerLocation(),
                    LoongBoatEntityModel.VanillaModel::createBodyModel);
        }
    }
}
