package poa.poaskrewritev2.expressions;

import ch.njol.skript.Skript;
import ch.njol.skript.aliases.ItemType;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.util.Kleenean;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.BlockData;
import org.bukkit.event.Event;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Queue;
import java.util.Set;

public class ExprConnectedBlocks extends SimpleExpression<Block> {

    private static final BlockFace[] SEARCH_DIRECTIONS = {
            BlockFace.UP,
            BlockFace.DOWN,
            BlockFace.EAST,
            BlockFace.WEST,
            BlockFace.SOUTH,
            BlockFace.NORTH
    };

    static {
        Skript.registerExpression(
                ExprConnectedBlocks.class,
                Block.class,
                ExpressionType.COMBINED,
                "[next] %number% connected block[s] (matching|of) %itemtypes/blockdatas% from %location%"
        );
    }

    private Expression<Number> limitExpression;
    private Expression<?> filterExpression;
    private Expression<Location> locationExpression;

    @SuppressWarnings({"unchecked"})
    @Override
    public boolean init(
            Expression<?>[] expressions,
            int matchedPattern,
            Kleenean isDelayed,
            ParseResult parseResult
    ) {
        limitExpression = (Expression<Number>) expressions[0];
        filterExpression = expressions[1];
        locationExpression = (Expression<Location>) expressions[2];

        return true;
    }

    @Override
    protected Block @NotNull [] get(@NotNull Event event) {
        Number limitValue = limitExpression.getSingle(event);
        Location startLocation = locationExpression.getSingle(event);
        Object[] filters = filterExpression.getArray(event);

        if (limitValue == null
                || startLocation == null
                || filters.length == 0) {
            return new Block[0];
        }

        int returnLimit = limitValue.intValue();

        if (returnLimit <= 0) {
            return new Block[0];
        }

        return findConnectedBlocks(
                startLocation,
                returnLimit,
                filters
        ).toArray(Block[]::new);
    }

    /**
     * Searches through blocks connected on their six direct faces.
     *
     * A block must match at least one filter to be returned and to allow
     * the search to continue through it.
     *
     * The limit controls the maximum number of returned blocks. Blocks
     * rejected by the filters may still be checked and do not count
     * towards the limit.
     */
    public List<Block> findConnectedBlocks(
            @Nullable Location start,
            int returnLimit,
            Object @NotNull [] filters
    ) {
        if (start == null || returnLimit <= 0 || filters.length == 0) {
            return List.of();
        }

        Block startBlock = start.getBlock();

        if (!matchesAnyFilter(startBlock, filters)) {
            return List.of();
        }

        int initialCapacity = Math.min(returnLimit, 1024);

        List<Block> results = new ArrayList<>(initialCapacity);
        Queue<Block> pendingBlocks = new ArrayDeque<>(initialCapacity);

        /*
         * Includes both matching and rejected blocks. This prevents the
         * same rejected neighbour from being checked several times.
         */
        Set<Block> checkedBlocks = new HashSet<>(initialCapacity);

        checkedBlocks.add(startBlock);
        pendingBlocks.add(startBlock);

        while (!pendingBlocks.isEmpty()
                && results.size() < returnLimit) {

            Block current = pendingBlocks.poll();
            results.add(current);

            if (results.size() >= returnLimit) {
                break;
            }

            for (BlockFace direction : SEARCH_DIRECTIONS) {
                addIfMatching(
                        pendingBlocks,
                        checkedBlocks,
                        current.getRelative(direction),
                        filters
                );
            }
        }

        return results;
    }

    private void addIfMatching(
            Queue<Block> pendingBlocks,
            Set<Block> checkedBlocks,
            Block block,
            Object[] filters
    ) {
        /*
         * Set#add returns false if this block has already been checked.
         */
        if (!checkedBlocks.add(block)) {
            return;
        }

        if (matchesAnyFilter(block, filters)) {
            pendingBlocks.add(block);
        }
    }

    private boolean matchesAnyFilter(
            Block block,
            Object[] filters
    ) {
        /*
         * Do not allow an air filter to start searching through the
         * effectively unlimited connected air surrounding structures.
         */
        if (block.getType().isAir()) {
            return false;
        }

        BlockData currentBlockData = block.getBlockData();

        for (Object filter : filters) {
            if (filter instanceof ItemType itemType) {
                /*
                 * ItemType is used only for material and alias matching.
                 *
                 * Examples:
                 * oak stairs
                 * dirt
                 * stone
                 */
                if (itemType.isOfType(block.getType())) {
                    return true;
                }

                continue;
            }

            if (filter instanceof BlockData requiredBlockData) {
                /*
                 * The current complete block data must call matches()
                 * with the parsed filter as the argument.
                 *
                 * This allows partial filters such as:
                 * oak_stairs[facing=east]
                 *
                 * Other properties, such as waterlogged and shape, do
                 * not need to be specified unless they matter.
                 */
                if (currentBlockData.matches(requiredBlockData)) {
                    return true;
                }
            }
        }

        return false;
    }

    @Override
    public boolean isSingle() {
        return false;
    }

    @Override
    public @NotNull Class<? extends Block> getReturnType() {
        return Block.class;
    }

    @Override
    public @NotNull String toString(
            @Nullable Event event,
            boolean debug
    ) {
        return "connected blocks matching item types or block data";
    }
}