package org.teacon.loongboat.world.entity;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.vehicle.Boat;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.teacon.loongboat.LoongBoat;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.core.animatable.GeoAnimatable;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.AnimationState;
import software.bernie.geckolib.core.animation.RawAnimation;
import software.bernie.geckolib.core.object.PlayState;
import software.bernie.geckolib.util.GeckoLibUtil;

import java.util.Optional;

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
    private static final RawAnimation MOVE_ANIMATION = RawAnimation.begin().thenLoop("animation.loong_boat.move");
    private static final String ANIMATION_CONTROLLER_NAME = "controller";

    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

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

    @SuppressWarnings("NullableProblems")
    @Override
    public Item getDropItem() {return LoongBoat.LOONG_BOAT_ITEM.get();}

    @Override
    protected int getMaxPassengers() {
        return (this.getSize() + 1) * 2;
    }

    /**
     * @see Boat#positionRider(Entity, MoveFunction)
     */
    @SuppressWarnings("NullableProblems")
    @Override
    protected void positionRider(Entity rider, Entity.MoveFunction moveFunc) {
        super.positionRider(rider, moveFunc);

        var idx = this.getPassengers().indexOf(rider); // save time by only making one query
        if (idx == -1) return; // equivalent to if (!this.hasPassenger(eider)) return;

        float xOffset = (this.getSize() - idx) * 0.735F + 0.4F;
        if (rider instanceof Animal) xOffset += 0.2F;
        var posPlanar = new Vec3(xOffset, 0, 0)
                .yRot((-this.getYRot() - 90) * ((float) Math.PI / 180))
                .add(this.position());
        moveFunc.accept(rider, posPlanar.x, rider.getY(), posPlanar.z);

        // make animals always facing sidewards
        if (rider instanceof Animal && this.getPassengers().size() != this.getMaxPassengers()) {
            int facing = rider.getId() % 2 == 0 ? 90 : 270;
            rider.setYBodyRot(((Animal) rider).yBodyRot + facing);
            rider.setYHeadRot(rider.getYHeadRot() + facing);
        }
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
        Optional.ofNullable(this.getAnimatableInstanceCache()
                        .getManagerForId(this.getId())
                        .getAnimationControllers()
                        .get(ANIMATION_CONTROLLER_NAME))
                .ifPresent(AnimationController::forceAnimationReset);
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
        controllerRegistrar.add(new AnimationController<>(this, ANIMATION_CONTROLLER_NAME, 0, this::predicate));
    }

    private <T extends GeoAnimatable> PlayState predicate(AnimationState<T> tAnimationState) {
        // FIXME: jitter animation
        if (this.getPaddleState(0) || this.getPaddleState(1)) {
            return tAnimationState.setAndContinue(MOVE_ANIMATION);
        }

        return PlayState.STOP;
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {return this.cache;}
}
