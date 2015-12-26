package org.originmc.fbasics.util;

import lombok.Data;
import org.originmc.fbasics.entity.Tile;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

@Data
public final class PathUtils {

    private static final int[][] ADJACENT = {
            {-1, 0, 0},
            {0, -1, 0},
            {0, 0, -1},
            {1, 0, 0},
            {0, 1, 0},
            {0, 0, 1}
    };

    private final Tile[][][] area;

    private final Tile start;

    private final Tile end;

    private final List<Tile> open = new ArrayList<>();

    private final List<Tile> closed = new ArrayList<>();

    /**
     * Checks to see if there is a path available to be taken from start to end within the specified area.
     *
     * @param area  all the possible tiles to move through, must contain start and end.
     * @param start the tile to start from.
     * @param end   the tile to end at.
     * @return true if a path can be made.
     */
    public static boolean hasPath(Tile[][][] area, Tile start, Tile end) {
        return new PathUtils(area, start, end).process();
    }

    /**
     * Finds the quickest path to take to a specific location forgetting corners, depending on which tiles are passable
     * in the area.
     *
     * @param area  all the possible tiles to move through, must contain start and end.
     * @param start the tile to start from.
     * @param end   the tile to end at.
     * @return the most optimal path, a sorted list of tiles from start to end.
     */
    public static List<Tile> getPath(Tile[][][] area, Tile start, Tile end) {
        // Return nothing if there is no path available.
        PathUtils pathFinder = new PathUtils(area, start, end);
        if (!pathFinder.process()) return null;

        // Build route starting from the end adding each parent subsequently.
        Tile parent;
        LinkedList<Tile> route = new LinkedList<>();
        route.add(end);
        while ((parent = end.getParent()) != null) {
            route.add(parent);
            end = parent;
        }

        // Reverse the route and return;
        Collections.reverse(route);
        return new ArrayList<>(route);
    }

    /**
     * Processes the entire pathway if possible using the current set tiles.
     *
     * @return true if a pathway was successfully found.
     */
    private boolean process() {
        // Calculate all the H (Heuristic) values first.
        for (int x = 0; x < area.length; x++) {
            for (int y = 0; y < area[x].length; y++) {
                for (int z = 0; z < area[x][y].length; z++) {
                    area[x][y][z].setH(Math.abs(end.getX() - x) + Math.abs(end.getY() - y) + Math.abs(end.getZ() - z));
                }
            }
        }

        // Perform the A* path finding algorithm until closed list contains the end tile.
        Tile current;
        open.add(start);
        start.setG(0);
        while (!closed.contains(end)) {
            // If there are no more tiles to move onto, there is no possible path.
            current = getNextTile();
            if (current == null) return false;

            // Calculate all adjacent tiles for the current tile.
            processAdjacentTiles(current);
        }

        return true;
    }

    /**
     * Retrieves the next tile to work on from the open list.
     *
     * @return tile with the lowest F value to A* specifications.
     */
    private Tile getNextTile() {
        // Iterate through all current open tiles.
        double f = Double.MAX_VALUE;
        Tile next = null;
        for (Tile tile : open) {
            // Set next tile if this tile has a lower F value.
            if (tile.getF() < f || f == Double.MAX_VALUE) {
                f = tile.getF();
                next = tile;
            }
        }

        // Do nothing more if next tile was not successfully found.
        if (next == null) return null;

        // Remove tile from open and add to closed lists, finally return the tile.
        open.remove(next);
        closed.add(next);
        return next;
    }

    /**
     * Processes one tile of the pathway, updating all surrounding tiles' G values.
     *
     * @param base the tile to process.
     */
    private void processAdjacentTiles(Tile base) {
        for (int[] modifier : ADJACENT) {
            // Do nothing if there is no such tile.
            int x = base.getX() + modifier[0];
            int y = base.getY() + modifier[1];
            int z = base.getZ() + modifier[2];
            if (x < 0 || y < 0 || z < 0 || area.length <= x || area[x].length <= y + 1 || area[x][y].length <= z) {
                continue;
            }

            // Do nothing if a player cannot move through the area.
            Tile current = area[x][y][z];
            if (!current.isPassable() || !area[x][y + 1][z].isPassable()) continue;

            // Update the G value and base if it is more efficient.
            if (current.getG() > base.getG() + 1) {
                current.setG(base.getG() + 1);
                current.setParent(base);
                open.add(current);
            }
        }
    }

}
