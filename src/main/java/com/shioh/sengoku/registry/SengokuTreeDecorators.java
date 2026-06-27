package com.shioh.sengoku.registry;

import com.shioh.sengoku.sengokuFabric;
import com.shioh.sengoku.worldgen.treedecorators.MangroveLeaveVineDecorator;
import com.shioh.sengoku.worldgen.treedecorators.SakuraLeaveVineDecorator;
import com.shioh.sengoku.worldgen.treedecorators.WeepingLeaveVineDecorator;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.levelgen.feature.treedecorators.TreeDecoratorType;

public class SengokuTreeDecorators {
    public static TreeDecoratorType<WeepingLeaveVineDecorator> SENGOKU_LEAVE_VINE;
    public static TreeDecoratorType<MangroveLeaveVineDecorator> SENGOKU_MANGROVE_LEAVE_VINE;
    public static TreeDecoratorType<SakuraLeaveVineDecorator> SENGOKU_SAKURA_LEAVE_VINE;

    public static void register() {
        SENGOKU_LEAVE_VINE = Registry.register(
            BuiltInRegistries.TREE_DECORATOR_TYPE,
            sengokuFabric.asId("leave_vine"),
            new TreeDecoratorType<>(WeepingLeaveVineDecorator.CODEC)
        );

        SENGOKU_MANGROVE_LEAVE_VINE = Registry.register(
            BuiltInRegistries.TREE_DECORATOR_TYPE,
            sengokuFabric.asId("mangrove_leave_vine"),
            new TreeDecoratorType<>(MangroveLeaveVineDecorator.CODEC)
        );

        SENGOKU_SAKURA_LEAVE_VINE = Registry.register(
            BuiltInRegistries.TREE_DECORATOR_TYPE,
            sengokuFabric.asId("sakura_leave_vine"),
            new TreeDecoratorType<>(SakuraLeaveVineDecorator.CODEC)
        );
    }
}
