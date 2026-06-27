package com.shioh.sengoku.util;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

import static net.minecraft.world.level.block.BedBlock.*;

public interface IBedShape {
    BooleanProperty mBedV$NORTH =   BlockStateProperties.NORTH;
    BooleanProperty mBedV$EAST =    BlockStateProperties.EAST;
    BooleanProperty mBedV$SOUTH =   BlockStateProperties.SOUTH;
    BooleanProperty mBedV$WEST =    BlockStateProperties.WEST;
    VoxelShape FLAT_SHAPE_LEG_NW = Shapes.or(BASE, LEG_NORTH_WEST);
    VoxelShape FLAT_SHAPE_LEG_NE = Shapes.or(BASE, LEG_NORTH_EAST);
    VoxelShape FLAT_SHAPE_LEG_SW = Shapes.or(BASE, LEG_SOUTH_WEST);
    VoxelShape FLAT_SHAPE_LEG_SE = Shapes.or(BASE, LEG_SOUTH_EAST);
    // Pillows
    double min = 0.0;                  double max = 16.0;
    double pb_h = 1.0;                 double pb_inset = 0.0;
    double pb_y1 = 9.0;                double pb_y2 = pb_y1 + pb_h;
    double pb_xz1 = min + pb_inset;    double pb_xz2 = max - pb_inset;
    double pb_d = 8.0;
    //  Pillow Bottoms                                      X1,             Y1,         Z1,             X2,             Y2,         Z2
    VoxelShape PILLOW_BOTTOM_NORTH =     Block.box(      pb_xz1,         pb_y1,      pb_xz1,         pb_xz2,         pb_y2,  pb_xz2 - pb_d);
    VoxelShape PILLOW_BOTTOM_EAST =      Block.box(  pb_xz1 + pb_d,  pb_y1,      pb_xz1,         pb_xz2,         pb_y2,      pb_xz2);
    VoxelShape PILLOW_BOTTOM_SOUTH =     Block.box(      pb_xz1,         pb_y1,  pb_xz1 + pb_d,  pb_xz2,         pb_y2,      pb_xz2);
    VoxelShape PILLOW_BOTTOM_WEST =      Block.box(      pb_xz1,         pb_y1,      pb_xz1,     pb_xz2 - pb_d,  pb_y2,      pb_xz2);
    double pt_h = 1.0;                 double ptm_inset = 2.0;
    double pt_y1 = pb_y2;              double pt_y2 = pt_y1 + pt_h;
    double ptm_xz1 = min + ptm_inset;  double ptm_xz2 = max - ptm_inset;
    double ptm_xz_d = 7.0;
    //  Pillow Top Main parts                              X1,                 Y1,         Z1,                 X2,                 Y2,         Z2
    VoxelShape PILLOW_TOP_MAIN_NORTH =   Block.box(      ptm_xz1,            pt_y1,      ptm_xz1,            ptm_xz2,            pt_y2,  ptm_xz2 - ptm_xz_d);
    VoxelShape PILLOW_TOP_MAIN_EAST =    Block.box(  ptm_xz1 + ptm_xz_d, pt_y1,      ptm_xz1,            ptm_xz2,            pt_y2,      ptm_xz2);
    VoxelShape PILLOW_TOP_MAIN_SOUTH =   Block.box(      ptm_xz1,            pt_y1,  ptm_xz1 + ptm_xz_d, ptm_xz2,            pt_y2,      ptm_xz2);
    VoxelShape PILLOW_TOP_MAIN_WEST =    Block.box(      ptm_xz1,            pt_y1,      ptm_xz1,        ptm_xz2 - ptm_xz_d, pt_y2,      ptm_xz2);
    double pte_xz_d1 = 1.0;
    double pte_xz_d2 = 2.0;
    //  Pillow Top Edge parts                            X1,                         Y1,         Z2,                             X2,                             Y2,         Z2
    VoxelShape PILLOW_TOP_EDGE_NORTH =   Block.box(min + pte_xz_d1 + pte_xz_d2,  pt_y1, min + pte_xz_d1,             max - pte_xz_d1 - pte_xz_d2,    pt_y2, min + pte_xz_d2);
    VoxelShape PILLOW_TOP_EDGE_EAST =    Block.box(max - pte_xz_d2,              pt_y1, min + pte_xz_d1 + pte_xz_d2, max - pte_xz_d1,                pt_y2, max - pte_xz_d1 - pte_xz_d2);
    VoxelShape PILLOW_TOP_EDGE_SOUTH =   Block.box(min + pte_xz_d1 + pte_xz_d2,  pt_y1, max - pte_xz_d2,             max - pte_xz_d1 - pte_xz_d2,    pt_y2, max - pte_xz_d1);
    VoxelShape PILLOW_TOP_EDGE_WEST =    Block.box(min + pte_xz_d1,              pt_y1, min + pte_xz_d1 + pte_xz_d2, min + pte_xz_d2,                pt_y2, max - pte_xz_d1 - pte_xz_d2);
    VoxelShape NORTH_SHAPE_PILLOWED =    Shapes.or(NORTH_SHAPE,  PILLOW_BOTTOM_NORTH,    PILLOW_TOP_MAIN_NORTH,  PILLOW_TOP_EDGE_NORTH);
    VoxelShape EAST_SHAPE_PILLOWED =     Shapes.or(EAST_SHAPE,   PILLOW_BOTTOM_EAST,     PILLOW_TOP_MAIN_EAST,   PILLOW_TOP_EDGE_EAST);
    VoxelShape SOUTH_SHAPE_PILLOWED =    Shapes.or(SOUTH_SHAPE,  PILLOW_BOTTOM_SOUTH,    PILLOW_TOP_MAIN_SOUTH,  PILLOW_TOP_EDGE_SOUTH);
    VoxelShape WEST_SHAPE_PILLOWED =     Shapes.or(WEST_SHAPE,   PILLOW_BOTTOM_WEST,     PILLOW_TOP_MAIN_WEST,   PILLOW_TOP_EDGE_WEST);
    //  Connected Pillow Top Main parts                    X1,                 Y1,     Z1,                     X2,                 Y2,         Z2
    VoxelShape PILLOW_TOP_MAIN_NORTH_E =     Block.box(ptm_xz1,              pt_y1, ptm_xz1,                 max,                pt_y2,  ptm_xz2 - ptm_xz_d);
    VoxelShape PILLOW_TOP_MAIN_NORTH_W =     Block.box(min,                  pt_y1, ptm_xz1,                 ptm_xz2,            pt_y2,  ptm_xz2 - ptm_xz_d);
    VoxelShape PILLOW_TOP_MAIN_NORTH_EW =    Block.box(min,                  pt_y1, ptm_xz1,                 max,                pt_y2,  ptm_xz2 - ptm_xz_d);
    VoxelShape PILLOW_TOP_MAIN_EAST_N =      Block.box(ptm_xz1 + ptm_xz_d,pt_y1, min,                     ptm_xz2,            pt_y2,      ptm_xz2);
    VoxelShape PILLOW_TOP_MAIN_EAST_S =      Block.box(ptm_xz1 + ptm_xz_d,pt_y1, ptm_xz1,                 ptm_xz2,            pt_y2,      max);
    VoxelShape PILLOW_TOP_MAIN_EAST_NS =     Block.box(ptm_xz1 + ptm_xz_d,pt_y1, min,                     ptm_xz2,            pt_y2,      max);
    VoxelShape PILLOW_TOP_MAIN_SOUTH_E =     Block.box(ptm_xz1,              pt_y1, ptm_xz1 + ptm_xz_d,   max,                pt_y2,      ptm_xz2);
    VoxelShape PILLOW_TOP_MAIN_SOUTH_W =     Block.box(min,                  pt_y1, ptm_xz1 + ptm_xz_d,   ptm_xz2,            pt_y2,      ptm_xz2);
    VoxelShape PILLOW_TOP_MAIN_SOUTH_EW =    Block.box(min,                  pt_y1, ptm_xz1 + ptm_xz_d,   max,                pt_y2,      ptm_xz2);
    VoxelShape PILLOW_TOP_MAIN_WEST_N =      Block.box(ptm_xz1,              pt_y1, min,                 ptm_xz2 - ptm_xz_d,  pt_y2,      ptm_xz2);
    VoxelShape PILLOW_TOP_MAIN_WEST_S =      Block.box(ptm_xz1,              pt_y1, ptm_xz1,             ptm_xz2 - ptm_xz_d,  pt_y2,      max);
    VoxelShape PILLOW_TOP_MAIN_WEST_NS =     Block.box(ptm_xz1,              pt_y1, min,                 ptm_xz2 - ptm_xz_d,  pt_y2,      max);
    // Connected Pillow Top Edge parts                      X1,                             Y1,         Z1,                             X2,                         Y2,         Z2
    VoxelShape PILLOW_TOP_EDGE_NORTH_E =     Block.box(min + pte_xz_d1 + pte_xz_d2,  pt_y1, min + pte_xz_d1,             max - pte_xz_d1,            pt_y2, min + pte_xz_d2);
    VoxelShape PILLOW_TOP_EDGE_NORTH_W =     Block.box(min + pte_xz_d1,              pt_y1, min + pte_xz_d1,             max - pte_xz_d1 - pte_xz_d2,pt_y2, min + pte_xz_d2);
    VoxelShape PILLOW_TOP_EDGE_NORTH_EW =    Block.box(min + pte_xz_d1,              pt_y1, min + pte_xz_d1,             max - pte_xz_d1,            pt_y2, min + pte_xz_d2);
    VoxelShape PILLOW_TOP_EDGE_EAST_N =      Block.box(max - pte_xz_d2,              pt_y1, min + pte_xz_d1,             max - pte_xz_d1,            pt_y2, max - pte_xz_d1 - pte_xz_d2);
    VoxelShape PILLOW_TOP_EDGE_EAST_S =      Block.box(max - pte_xz_d2,              pt_y1, min + pte_xz_d1 + pte_xz_d2, max - pte_xz_d1,            pt_y2, max - pte_xz_d1);
    VoxelShape PILLOW_TOP_EDGE_EAST_NS =     Block.box(max - pte_xz_d2,              pt_y1, min + pte_xz_d1,             max - pte_xz_d1,            pt_y2, max - pte_xz_d1);
    VoxelShape PILLOW_TOP_EDGE_SOUTH_E =     Block.box(min + pte_xz_d1 + pte_xz_d2,  pt_y1, max - pte_xz_d2,             max - pte_xz_d1,            pt_y2, max - pte_xz_d1);
    VoxelShape PILLOW_TOP_EDGE_SOUTH_W =     Block.box(min + pte_xz_d1,              pt_y1, max - pte_xz_d2,             max - pte_xz_d1 - pte_xz_d2,pt_y2, max - pte_xz_d1);
    VoxelShape PILLOW_TOP_EDGE_SOUTH_EW =    Block.box(min + pte_xz_d1,              pt_y1, max - pte_xz_d2,             max - pte_xz_d1,            pt_y2, max - pte_xz_d1);
    VoxelShape PILLOW_TOP_EDGE_WEST_N =      Block.box(min + pte_xz_d1,              pt_y1, min + pte_xz_d1,             min + pte_xz_d2,            pt_y2, max - pte_xz_d1 - pte_xz_d2);
    VoxelShape PILLOW_TOP_EDGE_WEST_S =      Block.box(min + pte_xz_d1,              pt_y1, min + pte_xz_d1 + pte_xz_d2, min + pte_xz_d2,            pt_y2, max - pte_xz_d1);
    VoxelShape PILLOW_TOP_EDGE_WEST_NS =     Block.box(min + pte_xz_d1,              pt_y1, min + pte_xz_d1,             min + pte_xz_d2,            pt_y2, max - pte_xz_d1);
    VoxelShape NORTH_SHAPE_PILLOWED_E =  Shapes.or(FLAT_SHAPE_LEG_NW,    PILLOW_BOTTOM_NORTH,PILLOW_TOP_MAIN_NORTH_E,    PILLOW_TOP_EDGE_NORTH_E);
    VoxelShape NORTH_SHAPE_PILLOWED_W =  Shapes.or(FLAT_SHAPE_LEG_NE,    PILLOW_BOTTOM_NORTH,PILLOW_TOP_MAIN_NORTH_W,    PILLOW_TOP_EDGE_NORTH_W);
    VoxelShape NORTH_SHAPE_PILLOWED_EW = Shapes.or(BASE,                 PILLOW_BOTTOM_NORTH,PILLOW_TOP_MAIN_NORTH_EW,   PILLOW_TOP_EDGE_NORTH_EW);
    VoxelShape EAST_SHAPE_PILLOWED_N =   Shapes.or(FLAT_SHAPE_LEG_SE,    PILLOW_BOTTOM_EAST, PILLOW_TOP_MAIN_EAST_N,     PILLOW_TOP_EDGE_EAST_N);
    VoxelShape EAST_SHAPE_PILLOWED_S =   Shapes.or(FLAT_SHAPE_LEG_NE,    PILLOW_BOTTOM_EAST, PILLOW_TOP_MAIN_EAST_S,     PILLOW_TOP_EDGE_EAST_S);
    VoxelShape EAST_SHAPE_PILLOWED_NS =  Shapes.or(BASE,                 PILLOW_BOTTOM_EAST, PILLOW_TOP_MAIN_EAST_NS,    PILLOW_TOP_EDGE_EAST_NS);
    VoxelShape SOUTH_SHAPE_PILLOWED_E =  Shapes.or(FLAT_SHAPE_LEG_SW,    PILLOW_BOTTOM_SOUTH,PILLOW_TOP_MAIN_SOUTH_E,    PILLOW_TOP_EDGE_SOUTH_E);
    VoxelShape SOUTH_SHAPE_PILLOWED_W =  Shapes.or(FLAT_SHAPE_LEG_SE,    PILLOW_BOTTOM_SOUTH,PILLOW_TOP_MAIN_SOUTH_W,    PILLOW_TOP_EDGE_SOUTH_W);
    VoxelShape SOUTH_SHAPE_PILLOWED_EW = Shapes.or(BASE,                 PILLOW_BOTTOM_SOUTH,PILLOW_TOP_MAIN_SOUTH_EW,   PILLOW_TOP_EDGE_SOUTH_EW);
    VoxelShape WEST_SHAPE_PILLOWED_N =   Shapes.or(FLAT_SHAPE_LEG_SW,    PILLOW_BOTTOM_WEST, PILLOW_TOP_MAIN_WEST_N,     PILLOW_TOP_EDGE_WEST_N);
    VoxelShape WEST_SHAPE_PILLOWED_S =   Shapes.or(FLAT_SHAPE_LEG_NW,    PILLOW_BOTTOM_WEST, PILLOW_TOP_MAIN_WEST_S,     PILLOW_TOP_EDGE_WEST_S);
    VoxelShape WEST_SHAPE_PILLOWED_NS =  Shapes.or(BASE,                 PILLOW_BOTTOM_WEST, PILLOW_TOP_MAIN_WEST_NS,    PILLOW_TOP_EDGE_WEST_NS);
    // Bound Bamboo
    VoxelShape BB_BASE = Block.box(0.0, 0.0, 0.0, 16.0, 8.0, 16.0);
    VoxelShape BB_BASE_1 = Block.box(0.0, 1.0, 0.0, 16.0, 9.0, 16.0);
    VoxelShape BB_NORTH_PILLOWED =    Shapes.or(BB_BASE_1,    PILLOW_BOTTOM_NORTH,    PILLOW_TOP_MAIN_NORTH,  PILLOW_TOP_EDGE_NORTH).move(0.0,-0.0625, 0.0);
    VoxelShape BB_EAST_PILLOWED =     Shapes.or(BB_BASE_1,    PILLOW_BOTTOM_EAST,     PILLOW_TOP_MAIN_EAST,   PILLOW_TOP_EDGE_EAST).move(0.0,-0.0625, 0.0);
    VoxelShape BB_SOUTH_PILLOWED =    Shapes.or(BB_BASE_1,    PILLOW_BOTTOM_SOUTH,    PILLOW_TOP_MAIN_SOUTH,  PILLOW_TOP_EDGE_SOUTH).move(0.0,-0.0625, 0.0);
    VoxelShape BB_WEST_PILLOWED =     Shapes.or(BB_BASE_1,    PILLOW_BOTTOM_WEST,     PILLOW_TOP_MAIN_WEST,   PILLOW_TOP_EDGE_WEST).move(0.0,-0.0625, 0.0);
    VoxelShape BB_NORTH_PILLOWED_E =  Shapes.or(BB_BASE_1,    PILLOW_BOTTOM_NORTH,PILLOW_TOP_MAIN_NORTH_E,    PILLOW_TOP_EDGE_NORTH_E).move(0.0,-0.0625, 0.0);
    VoxelShape BB_NORTH_PILLOWED_W =  Shapes.or(BB_BASE_1,    PILLOW_BOTTOM_NORTH,PILLOW_TOP_MAIN_NORTH_W,    PILLOW_TOP_EDGE_NORTH_W).move(0.0,-0.0625, 0.0);
    VoxelShape BB_NORTH_PILLOWED_EW = Shapes.or(BB_BASE_1,    PILLOW_BOTTOM_NORTH,PILLOW_TOP_MAIN_NORTH_EW,   PILLOW_TOP_EDGE_NORTH_EW).move(0.0,-0.0625, 0.0);
    VoxelShape BB_EAST_PILLOWED_N =   Shapes.or(BB_BASE_1,    PILLOW_BOTTOM_EAST, PILLOW_TOP_MAIN_EAST_N,     PILLOW_TOP_EDGE_EAST_N).move(0.0,-0.0625, 0.0);
    VoxelShape BB_EAST_PILLOWED_S =   Shapes.or(BB_BASE_1,    PILLOW_BOTTOM_EAST, PILLOW_TOP_MAIN_EAST_S,     PILLOW_TOP_EDGE_EAST_S).move(0.0,-0.0625, 0.0);
    VoxelShape BB_EAST_PILLOWED_NS =  Shapes.or(BB_BASE_1,    PILLOW_BOTTOM_EAST, PILLOW_TOP_MAIN_EAST_NS,    PILLOW_TOP_EDGE_EAST_NS).move(0.0,-0.0625, 0.0);
    VoxelShape BB_SOUTH_PILLOWED_E =  Shapes.or(BB_BASE_1,    PILLOW_BOTTOM_SOUTH,PILLOW_TOP_MAIN_SOUTH_E,    PILLOW_TOP_EDGE_SOUTH_E).move(0.0,-0.0625, 0.0);
    VoxelShape BB_SOUTH_PILLOWED_W =  Shapes.or(BB_BASE_1,    PILLOW_BOTTOM_SOUTH,PILLOW_TOP_MAIN_SOUTH_W,    PILLOW_TOP_EDGE_SOUTH_W).move(0.0,-0.0625, 0.0);
    VoxelShape BB_SOUTH_PILLOWED_EW = Shapes.or(BB_BASE_1,    PILLOW_BOTTOM_SOUTH,PILLOW_TOP_MAIN_SOUTH_EW,   PILLOW_TOP_EDGE_SOUTH_EW).move(0.0,-0.0625, 0.0);
    VoxelShape BB_WEST_PILLOWED_N =   Shapes.or(BB_BASE_1,    PILLOW_BOTTOM_WEST, PILLOW_TOP_MAIN_WEST_N,     PILLOW_TOP_EDGE_WEST_N).move(0.0,-0.0625, 0.0);
    VoxelShape BB_WEST_PILLOWED_S =   Shapes.or(BB_BASE_1,    PILLOW_BOTTOM_WEST, PILLOW_TOP_MAIN_WEST_S,     PILLOW_TOP_EDGE_WEST_S).move(0.0,-0.0625, 0.0);
    VoxelShape BB_WEST_PILLOWED_NS =  Shapes.or(BB_BASE_1,    PILLOW_BOTTOM_WEST, PILLOW_TOP_MAIN_WEST_NS,    PILLOW_TOP_EDGE_WEST_NS).move(0.0,-0.0625, 0.0);
}
