package org.teacon.loongboat.world.entity;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.vehicle.Boat;
import net.minecraft.world.level.Level;
import org.teacon.loongboat.LoongBoat;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.core.animatable.GeoAnimatable;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animatable.instance.SingletonAnimatableInstanceCache;
import software.bernie.geckolib.core.animation.*;
import software.bernie.geckolib.core.object.PlayState;

public class LoongBoatEntity extends Boat implements GeoEntity {
    public static final String ENTITY_NAME = "loong_boat";

    /**
     * valid values: 0, 1, 2; invalid sizes are reset to DEFAULT_SIZE in setSize
     *
     * @see LoongBoatEntity#setSize
     */
    private static final EntityDataAccessor<Byte> DATA_ID_SIZE =
            SynchedEntityData.defineId(LoongBoatEntity.class, EntityDataSerializers.BYTE);
    private static final byte DEFAULT_SIZE = 0;
    private static final String SIZE_DATA_KEY = "Size";

    private final AnimatableInstanceCache cache = new SingletonAnimatableInstanceCache(this);

    public LoongBoatEntity(EntityType<? extends Boat> entityType, Level level) {
        super(entityType, level);
    }

    public LoongBoatEntity(Level level, double x, double y, double z) {
        this(LoongBoat.LOONG_BOAT_ENTITY.get(), level);
        this.setPos(x, y, z);
        this.xo = x;
        this.yo = y;
        this.zo = z;
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(DATA_ID_SIZE, DEFAULT_SIZE);
    }

    public byte getSize() {return this.entityData.get(DATA_ID_SIZE);}

    public void setSize(byte size) {
        if (size < 0 || size > 2) {
            LoongBoat.LOGGER.warn("Loong Boat entity (uuid="
                    + this.getStringUUID()
                    + ") has its Size been set to an invalid value: "
                    + size);
            size = DEFAULT_SIZE;
        }
        this.entityData.set(DATA_ID_SIZE, size);
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
        tag.putByte(SIZE_DATA_KEY, this.getSize());
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
        if (tag.contains(SIZE_DATA_KEY, 1)) { // id of ByteTag is 1
            this.setSize(tag.getByte(SIZE_DATA_KEY));
        }
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllerRegistrar) {
        controllerRegistrar.add(new AnimationController<>(this, "controller", 0, this::predicate));
    }

    private <T extends GeoAnimatable> PlayState predicate(AnimationState<T> tAnimationState) {
        if (this.getPaddleState(0) || this.getPaddleState(1)) {
            tAnimationState.getController().setAnimation(RawAnimation.begin().then("animation.loong_boat.move", Animation.LoopType.LOOP));
            return PlayState.CONTINUE;
        }

        return PlayState.STOP;
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {return cache;}
}
