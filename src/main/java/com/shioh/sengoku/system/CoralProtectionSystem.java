package com.shioh.sengoku.system;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;

public final class CoralProtectionSystem {
    private CoralProtectionSystem() {}

    // Called from server tick: scan around players in the End every tick to immediately replace dead coral
    public static void serverTick(ServerLevel level) {
        if (level == null) return;
        if (level.dimension() != Level.END) return;

        // run every tick; keep radius small to avoid heavy work
        final int radius = 6;

        level.getServer().getPlayerList().getPlayers().forEach(player -> {
            if (player.level() != level) return;
            BlockPos center = player.blockPosition();
            int minX = center.getX() - radius;
            int maxX = center.getX() + radius;
            int minY = Math.max(0, center.getY() - 3);
            int maxY = Math.min(level.getMaxBuildHeight() - 1, center.getY() + 3);
            int minZ = center.getZ() - radius;
            int maxZ = center.getZ() + radius;

            for (int x = minX; x <= maxX; x++) {
                for (int y = minY; y <= maxY; y++) {
                    for (int z = minZ; z <= maxZ; z++) {
                        BlockPos pos = new BlockPos(x, y, z);
                        BlockState state = level.getBlockState(pos);
                        // replace dead coral blocks
                        if (state.is(Blocks.DEAD_BRAIN_CORAL_BLOCK)) {
                            level.setBlock(pos, Blocks.BRAIN_CORAL_BLOCK.defaultBlockState(), 3);
                        } else if (state.is(Blocks.DEAD_TUBE_CORAL_BLOCK)) {
                            level.setBlock(pos, Blocks.TUBE_CORAL_BLOCK.defaultBlockState(), 3);
                        } else if (state.is(Blocks.DEAD_BUBBLE_CORAL_BLOCK)) {
                            level.setBlock(pos, Blocks.BUBBLE_CORAL_BLOCK.defaultBlockState(), 3);
                        } else if (state.is(Blocks.DEAD_FIRE_CORAL_BLOCK)) {
                            level.setBlock(pos, Blocks.FIRE_CORAL_BLOCK.defaultBlockState(), 3);
                        } else if (state.is(Blocks.DEAD_HORN_CORAL_BLOCK)) {
                            level.setBlock(pos, Blocks.HORN_CORAL_BLOCK.defaultBlockState(), 3);
                        }

                        // replace dead standing coral fans (copy facing/waterlogged when present)
                        else if (state.is(Blocks.DEAD_BRAIN_CORAL) || state.is(Blocks.DEAD_BRAIN_CORAL_FAN)) {
                            BlockState newState = Blocks.BRAIN_CORAL.defaultBlockState();
                            if (state.hasProperty(BlockStateProperties.FACING) && newState.hasProperty(BlockStateProperties.FACING)) {
                                newState = newState.setValue(BlockStateProperties.FACING, state.getValue(BlockStateProperties.FACING));
                            }
                            if (state.hasProperty(BlockStateProperties.WATERLOGGED) && newState.hasProperty(BlockStateProperties.WATERLOGGED)) {
                                newState = newState.setValue(BlockStateProperties.WATERLOGGED, state.getValue(BlockStateProperties.WATERLOGGED));
                            }
                            level.setBlock(pos, newState, 3);
                        } else if (state.is(Blocks.DEAD_TUBE_CORAL) || state.is(Blocks.DEAD_TUBE_CORAL_FAN)) {
                            BlockState newState = Blocks.TUBE_CORAL.defaultBlockState();
                            if (state.hasProperty(BlockStateProperties.FACING) && newState.hasProperty(BlockStateProperties.FACING)) {
                                newState = newState.setValue(BlockStateProperties.FACING, state.getValue(BlockStateProperties.FACING));
                            }
                            if (state.hasProperty(BlockStateProperties.WATERLOGGED) && newState.hasProperty(BlockStateProperties.WATERLOGGED)) {
                                newState = newState.setValue(BlockStateProperties.WATERLOGGED, state.getValue(BlockStateProperties.WATERLOGGED));
                            }
                            level.setBlock(pos, newState, 3);
                        } else if (state.is(Blocks.DEAD_BUBBLE_CORAL) || state.is(Blocks.DEAD_BUBBLE_CORAL_FAN)) {
                            BlockState newState = Blocks.BUBBLE_CORAL.defaultBlockState();
                            if (state.hasProperty(BlockStateProperties.FACING) && newState.hasProperty(BlockStateProperties.FACING)) {
                                newState = newState.setValue(BlockStateProperties.FACING, state.getValue(BlockStateProperties.FACING));
                            }
                            if (state.hasProperty(BlockStateProperties.WATERLOGGED) && newState.hasProperty(BlockStateProperties.WATERLOGGED)) {
                                newState = newState.setValue(BlockStateProperties.WATERLOGGED, state.getValue(BlockStateProperties.WATERLOGGED));
                            }
                            level.setBlock(pos, newState, 3);
                        } else if (state.is(Blocks.DEAD_FIRE_CORAL) || state.is(Blocks.DEAD_FIRE_CORAL_FAN)) {
                            BlockState newState = Blocks.FIRE_CORAL.defaultBlockState();
                            if (state.hasProperty(BlockStateProperties.FACING) && newState.hasProperty(BlockStateProperties.FACING)) {
                                newState = newState.setValue(BlockStateProperties.FACING, state.getValue(BlockStateProperties.FACING));
                            }
                            if (state.hasProperty(BlockStateProperties.WATERLOGGED) && newState.hasProperty(BlockStateProperties.WATERLOGGED)) {
                                newState = newState.setValue(BlockStateProperties.WATERLOGGED, state.getValue(BlockStateProperties.WATERLOGGED));
                            }
                            level.setBlock(pos, newState, 3);
                        } else if (state.is(Blocks.DEAD_HORN_CORAL) || state.is(Blocks.DEAD_HORN_CORAL_FAN)) {
                            BlockState newState = Blocks.HORN_CORAL.defaultBlockState();
                            if (state.hasProperty(BlockStateProperties.FACING) && newState.hasProperty(BlockStateProperties.FACING)) {
                                newState = newState.setValue(BlockStateProperties.FACING, state.getValue(BlockStateProperties.FACING));
                            }
                            if (state.hasProperty(BlockStateProperties.WATERLOGGED) && newState.hasProperty(BlockStateProperties.WATERLOGGED)) {
                                newState = newState.setValue(BlockStateProperties.WATERLOGGED, state.getValue(BlockStateProperties.WATERLOGGED));
                            }
                            level.setBlock(pos, newState, 3);
                        }

                        // replace dead wall fans (preserve facing/waterlogged)
                        else if (state.is(Blocks.DEAD_BRAIN_CORAL_WALL_FAN)) {
                            BlockState newState = Blocks.BRAIN_CORAL_WALL_FAN.defaultBlockState();
                            if (state.hasProperty(BlockStateProperties.FACING) && newState.hasProperty(BlockStateProperties.FACING)) {
                                newState = newState.setValue(BlockStateProperties.FACING, state.getValue(BlockStateProperties.FACING));
                            }
                            if (state.hasProperty(BlockStateProperties.WATERLOGGED) && newState.hasProperty(BlockStateProperties.WATERLOGGED)) {
                                newState = newState.setValue(BlockStateProperties.WATERLOGGED, state.getValue(BlockStateProperties.WATERLOGGED));
                            }
                            level.setBlock(pos, newState, 3);
                        } else if (state.is(Blocks.DEAD_TUBE_CORAL_WALL_FAN)) {
                            BlockState newState = Blocks.TUBE_CORAL_WALL_FAN.defaultBlockState();
                            if (state.hasProperty(BlockStateProperties.FACING) && newState.hasProperty(BlockStateProperties.FACING)) {
                                newState = newState.setValue(BlockStateProperties.FACING, state.getValue(BlockStateProperties.FACING));
                            }
                            if (state.hasProperty(BlockStateProperties.WATERLOGGED) && newState.hasProperty(BlockStateProperties.WATERLOGGED)) {
                                newState = newState.setValue(BlockStateProperties.WATERLOGGED, state.getValue(BlockStateProperties.WATERLOGGED));
                            }
                            level.setBlock(pos, newState, 3);
                        } else if (state.is(Blocks.DEAD_BUBBLE_CORAL_WALL_FAN)) {
                            BlockState newState = Blocks.BUBBLE_CORAL_WALL_FAN.defaultBlockState();
                            if (state.hasProperty(BlockStateProperties.FACING) && newState.hasProperty(BlockStateProperties.FACING)) {
                                newState = newState.setValue(BlockStateProperties.FACING, state.getValue(BlockStateProperties.FACING));
                            }
                            if (state.hasProperty(BlockStateProperties.WATERLOGGED) && newState.hasProperty(BlockStateProperties.WATERLOGGED)) {
                                newState = newState.setValue(BlockStateProperties.WATERLOGGED, state.getValue(BlockStateProperties.WATERLOGGED));
                            }
                            level.setBlock(pos, newState, 3);
                        } else if (state.is(Blocks.DEAD_FIRE_CORAL_WALL_FAN)) {
                            BlockState newState = Blocks.FIRE_CORAL_WALL_FAN.defaultBlockState();
                            if (state.hasProperty(BlockStateProperties.FACING) && newState.hasProperty(BlockStateProperties.FACING)) {
                                newState = newState.setValue(BlockStateProperties.FACING, state.getValue(BlockStateProperties.FACING));
                            }
                            if (state.hasProperty(BlockStateProperties.WATERLOGGED) && newState.hasProperty(BlockStateProperties.WATERLOGGED)) {
                                newState = newState.setValue(BlockStateProperties.WATERLOGGED, state.getValue(BlockStateProperties.WATERLOGGED));
                            }
                            level.setBlock(pos, newState, 3);
                        } else if (state.is(Blocks.DEAD_HORN_CORAL_WALL_FAN)) {
                            BlockState newState = Blocks.HORN_CORAL_WALL_FAN.defaultBlockState();
                            if (state.hasProperty(BlockStateProperties.FACING) && newState.hasProperty(BlockStateProperties.FACING)) {
                                newState = newState.setValue(BlockStateProperties.FACING, state.getValue(BlockStateProperties.FACING));
                            }
                            if (state.hasProperty(BlockStateProperties.WATERLOGGED) && newState.hasProperty(BlockStateProperties.WATERLOGGED)) {
                                newState = newState.setValue(BlockStateProperties.WATERLOGGED, state.getValue(BlockStateProperties.WATERLOGGED));
                            }
                            level.setBlock(pos, newState, 3);
                        }
                    }
                }
            }
        });
    }
}
