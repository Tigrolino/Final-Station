package com.finalstation.spawnguard;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockSupport;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.MultipleFacing;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Works out where a vine grows when a player uses bone meal on it.
 *
 * <p>Same moves a vine makes on its own in vanilla, just in a fixed order: 1. down, at the bottom
 * of the strand 2. sideways from the clicked piece (along the wall, around a corner, onto a wall
 * next to it, or along the ceiling) - picked at random if there are several 3. up, at the top of
 * the strand
 */
final class VineGrowth {

    private static final BlockFace[] SIDES = {
        BlockFace.NORTH, BlockFace.EAST, BlockFace.SOUTH, BlockFace.WEST
    };

    /**
     * One change to make: put {@code data} into {@code target}. {@code from} is the vine it grows
     * out of.
     */
    record Step(Block target, BlockData data, Block from) {}

    private VineGrowth() {}

    /** The next growth step for this vine, or null if it has nowhere to go. */
    static Step next(Block clicked) {
        if (!(clicked.getBlockData() instanceof MultipleFacing)) {
            return null;
        }
        Step down = down(clicked);
        if (down != null) {
            return down;
        }
        List<Step> sideways = sideways(clicked);
        if (!sideways.isEmpty()) {
            return sideways.get(ThreadLocalRandom.current().nextInt(sideways.size()));
        }
        return up(clicked);
    }

    // ---------------------------------------------------------- down

    private static Step down(Block clicked) {
        Block bottom = clicked;
        while (bottom.getY() > bottom.getWorld().getMinHeight()
                && bottom.getRelative(BlockFace.DOWN).getType() == Material.VINE) {
            bottom = bottom.getRelative(BlockFace.DOWN);
        }
        Block target = bottom.getRelative(BlockFace.DOWN);
        if (target.getY() < target.getWorld().getMinHeight() || !target.getType().isAir()) {
            return null;
        }
        // The new piece hangs on the same sides as the piece above it.
        MultipleFacing above = (MultipleFacing) bottom.getBlockData();
        MultipleFacing vine = emptyVine();
        boolean hangs = false;
        for (BlockFace side : SIDES) {
            if (above.hasFace(side)) {
                vine.setFace(side, true);
                hangs = true;
            }
        }
        // A vine that only sticks to the ceiling has nothing to hang from.
        return hangs ? new Step(target, vine, bottom) : null;
    }

    // ---------------------------------------------------------- sideways

    private static List<Step> sideways(Block clicked) {
        MultipleFacing vine = (MultipleFacing) clicked.getBlockData();
        List<Step> options = new ArrayList<>();

        for (BlockFace direction : SIDES) {
            if (vine.hasFace(direction)) {
                continue; // there is a wall that way, the vine is already on it
            }
            Block next = clicked.getRelative(direction);

            if (!next.getType().isAir()) {
                // A wall right next to the vine: the vine also grabs onto that wall.
                if (holds(next, direction)) {
                    MultipleFacing grown = (MultipleFacing) vine.clone();
                    grown.setFace(direction, true);
                    options.add(new Step(clicked, grown, clicked));
                }
                continue;
            }

            boolean found = false;
            for (BlockFace wallSide :
                    new BlockFace[] {clockwise(direction), counterClockwise(direction)}) {
                if (!vine.hasFace(wallSide)) {
                    continue;
                }
                // Along the same wall.
                if (holds(next.getRelative(wallSide), wallSide)) {
                    options.add(new Step(next, vineOn(wallSide), clicked));
                    found = true;
                    break;
                }
            }
            if (!found) {
                for (BlockFace wallSide :
                        new BlockFace[] {clockwise(direction), counterClockwise(direction)}) {
                    if (!vine.hasFace(wallSide)) {
                        continue;
                    }
                    // The wall ends here: go around its corner.
                    Block aroundCorner = next.getRelative(wallSide);
                    BlockFace back = direction.getOppositeFace();
                    if (aroundCorner.getType().isAir()
                            && holds(clicked.getRelative(wallSide), back)) {
                        options.add(new Step(aroundCorner, vineOn(back), clicked));
                        found = true;
                        break;
                    }
                }
            }
            // Along the ceiling.
            if (!found
                    && vine.hasFace(BlockFace.UP)
                    && holds(next.getRelative(BlockFace.UP), BlockFace.UP)) {
                options.add(new Step(next, vineOn(BlockFace.UP), clicked));
            }
        }
        return options;
    }

    // ---------------------------------------------------------- up

    private static Step up(Block clicked) {
        Block top = clicked;
        while (top.getY() < top.getWorld().getMaxHeight() - 1
                && top.getRelative(BlockFace.UP).getType() == Material.VINE) {
            top = top.getRelative(BlockFace.UP);
        }
        Block target = top.getRelative(BlockFace.UP);
        if (target.getY() > target.getWorld().getMaxHeight() - 1 || !target.getType().isAir()) {
            return null;
        }
        // Going up, the new piece needs a wall of its own to hold on to.
        MultipleFacing below = (MultipleFacing) top.getBlockData();
        MultipleFacing vine = emptyVine();
        boolean holds = false;
        for (BlockFace side : SIDES) {
            if (below.hasFace(side) && holds(target.getRelative(side), side)) {
                vine.setFace(side, true);
                holds = true;
            }
        }
        return holds ? new Step(target, vine, top) : null;
    }

    // ---------------------------------------------------------- helpers

    /**
     * Can a vine hold on to this block? {@code vineSide} is the side of the vine that touches it.
     */
    private static boolean holds(Block wall, BlockFace vineSide) {
        return wall.getBlockData().isFaceSturdy(vineSide.getOppositeFace(), BlockSupport.FULL);
    }

    private static MultipleFacing emptyVine() {
        return (MultipleFacing) Material.VINE.createBlockData();
    }

    private static MultipleFacing vineOn(BlockFace side) {
        MultipleFacing vine = emptyVine();
        vine.setFace(side, true);
        return vine;
    }

    private static BlockFace clockwise(BlockFace face) {
        return switch (face) {
            case NORTH -> BlockFace.EAST;
            case EAST -> BlockFace.SOUTH;
            case SOUTH -> BlockFace.WEST;
            default -> BlockFace.NORTH;
        };
    }

    private static BlockFace counterClockwise(BlockFace face) {
        return clockwise(face).getOppositeFace();
    }
}
