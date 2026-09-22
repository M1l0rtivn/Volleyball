package com.melok.volleyball;

import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class Volleyball_ball {
    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(Volleyball.MODID);
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(Volleyball.MODID);

    // --- Список блоков ---

    public static final DeferredBlock<Block> SIMPLE_BLOCK = registerBlock("volleyball",
            () -> new Block(BlockBehaviour.Properties.of()
                    .strength(0.3f, 3.0f)
                    .sound(SoundType.WOOL)
            ));

    private static <T extends Block> DeferredBlock<T> registerBlock(String name, Supplier<T> blockSupplier) {
        DeferredBlock<T> block = BLOCKS.register(name, blockSupplier);
        ITEMS.registerSimpleBlockItem(block, new Item.Properties());
        return block;
    }

    public static void register(IEventBus modBus) {
        BLOCKS.register(modBus);
        ITEMS.register(modBus);
    }
}