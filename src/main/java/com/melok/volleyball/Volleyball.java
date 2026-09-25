package com.melok.volleyball;

import com.mojang.logging.LogUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.item.FallingBlockEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.event.entity.player.AttackEntityEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import org.slf4j.Logger;

@Mod(Volleyball.MODID)
public class Volleyball {
    public static final String MODID = "volleyball";
    private static final Logger LOGGER = LogUtils.getLogger();


    public Volleyball(IEventBus modEventBus, ModContainer modContainer) {
        Volleyball_ball.register(modEventBus);
        NeoForge.EVENT_BUS.register(this);
        modEventBus.addListener(this::addCreative);
    }

    private void addCreative(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey() == CreativeModeTabs.BUILDING_BLOCKS) {
            event.accept(Volleyball_ball.SIMPLE_BLOCK);
        }
    }

    @SubscribeEvent
    public void onEntityInteract(PlayerInteractEvent.EntityInteract event){
        Player player = event.getEntity();
        var target = event.getTarget();
        var level = player.level();

        MinecraftServer server = player.getServer();
        LOGGER.info("Интеракт со сущностью");
        if (event.getHand() != InteractionHand.MAIN_HAND) {
            return;
        }

        if (!player.level().isClientSide()) {

            if(target instanceof FallingBlockEntity fallingBlockEntity) {
                Vec3 motion = target.getDeltaMovement();
                target.setDeltaMovement(motion.x,motion.y + 1.2D,motion.z);
                target.hasImpulse = true;
                target.hurtMarked = true;

                event.setCanceled(true);
                event.setCancellationResult(InteractionResult.SUCCESS);
                LOGGER.info("Все готово");
            }

        }
    }

    @SubscribeEvent
    public void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        var block = event.getUseBlock();
        var player = event.getEntity();
        if (event.getHand() != InteractionHand.MAIN_HAND || !player.isCrouching()) {
            return;
        }

        if (!player.getMainHandItem().isEmpty()) {
            return;
        }

        if (event.getLevel() instanceof ServerLevel level) {
            BlockPos pos = event.getPos();
            BlockState state = level.getBlockState(pos);
            if (state.is(Volleyball_ball.SIMPLE_BLOCK.get())) {

                if (state.isAir() || state.is(Blocks.BEDROCK)) {
                    return;
                }

                FallingBlockEntity fallingBlock = FallingBlockEntity.fall(level, pos, state);

                level.setBlockAndUpdate(pos, Blocks.AIR.defaultBlockState());

                fallingBlock.setDeltaMovement(new Vec3(0, 0.8D, 0));
                fallingBlock.hasImpulse = true;
                fallingBlock.hurtMarked = true;

                event.setCanceled(true);
                event.setCancellationResult(InteractionResult.SUCCESS);
            }
        }
    }

    @SubscribeEvent
    public void onPlayerAttackMob(AttackEntityEvent event) {
        var player = event.getEntity();
        var target = event.getTarget();
        float mult = 1;

        if (player.level() instanceof ServerLevel level) {
            if(target instanceof FallingBlockEntity fallingBlockEntity) {
                if (fallingBlockEntity.getBlockState().is(Volleyball_ball.SIMPLE_BLOCK.get())) {
                    if(!player.onGround()) {
                        mult = 1.2f;
                    }
                    Vec3 look = player.getLookAngle();
                    Vec3 finals = new Vec3(look.x * mult, look.y * mult, look.z * mult);
                    fallingBlockEntity.setDeltaMovement(finals);
                    fallingBlockEntity.hasImpulse = true;
                    fallingBlockEntity.hurtMarked = true;
                    mult = 1;
                    event.setCanceled(true);
                }

            }
        }
    }

}