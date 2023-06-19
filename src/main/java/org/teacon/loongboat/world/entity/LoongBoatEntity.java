package org.teacon.loongboat.world.entity;

import net.minecraft.client.CameraType;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.vehicle.Boat;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
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

import java.util.function.Predicate;

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

    private static final RawAnimation MOVE_ANIMATION = RawAnimation.begin().thenLoop(ENTITY_NAME + ".move");
    private static final RawAnimation MOVE_HEAD_ANIMATION = RawAnimation.begin().thenLoop(ENTITY_NAME + ".move_head");
    private static final RawAnimation MOVE_HEAD_SLIGHT_ANIMATION = RawAnimation.begin().thenLoop(ENTITY_NAME + ".move_head_slight");
    private static final RawAnimation ROW_LEFT_ANIMATION = RawAnimation.begin().thenLoop(ENTITY_NAME + ".row_left");
    private static final RawAnimation ROW_RIGHT_ANIMATION = RawAnimation.begin().thenLoop(ENTITY_NAME + ".row_right");

    private int controllerCount = 0;

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

    @SuppressWarnings("NullableProblems")
    @Override
    protected void destroy(DamageSource source) {
        this.spawnAtLocation(new ItemStack(this.getDropItem(), this.getSize() + 1));
    }

    @Override
    protected int getMaxPassengers() {return (this.getSize() + 1) * 2;}

    /**
     * @see Boat#positionRider(Entity, MoveFunction)
     */
    @SuppressWarnings("NullableProblems")
    @Override
    protected void positionRider(Entity rider, Entity.MoveFunction moveFunc) {
        super.positionRider(rider, moveFunc);

        var idx = this.getPassengers().indexOf(rider); // save time by only making one query
        if (idx == -1) return; // equivalent to if (!this.hasPassenger(eider)) return;

        float xOffset = -idx * 0.625F + 0.1F;
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
        // reload all animations after resizing
        this.getAnimatableInstanceCache().getManagerForId(this.getId()).getAnimationControllers()
                .values().forEach(AnimationController::forceAnimationReset);
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
        controllerRegistrar.add(
                new AnimationController<GeoAnimatable>(this, this.nextControllerName(), 2,
                        state -> {
                            if (this.getPaddleState(Boat.PADDLE_LEFT) || this.getPaddleState(Boat.PADDLE_RIGHT))
                                if (this.isControllerFirstPerson())
                                    return state.setAndContinue(MOVE_HEAD_SLIGHT_ANIMATION);
                                else return state.setAndContinue(MOVE_HEAD_ANIMATION);
                            else return PlayState.STOP;
                        }),
                predicateController(MOVE_ANIMATION, state -> this.getPaddleState(Boat.PADDLE_LEFT) || this.getPaddleState(Boat.PADDLE_RIGHT)),
                predicateController(ROW_LEFT_ANIMATION, state -> this.getPaddleState(Boat.PADDLE_LEFT)),
                predicateController(ROW_RIGHT_ANIMATION, state -> this.getPaddleState(Boat.PADDLE_RIGHT))
        );
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {return this.cache;}

    public boolean isControllerFirstPerson() {
        var mc = Minecraft.getInstance();
        return mc.getCameraEntity() == this.getControllingPassenger()
                && mc.options.getCameraType() == CameraType.FIRST_PERSON;
    }

    @SuppressWarnings("NullableProblems")
    @Override
    public AABB getBoundingBoxForCulling() {
        var box = super.getBoundingBoxForCulling();
        var multiplier = this.getSize();
        return box.inflate(box.getXsize() * multiplier, 0, box.getZsize() * multiplier);
    }

    private String nextControllerName() {return "controller" + (this.controllerCount++);}

    private AnimationController<LoongBoatEntity> predicateController(RawAnimation anim, Predicate<AnimationState<LoongBoatEntity>> predicate) {
        return new AnimationController<>(this, this.nextControllerName(), 2,
                state -> predicate.test(state) ? state.setAndContinue(anim) : PlayState.STOP);
    }
}
