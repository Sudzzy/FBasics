package org.originmc.fbasics.entity;

import lombok.Data;

@Data
public final class Tile {

    private final int x, y, z;

    private boolean passable = true;

    private double g = Double.MAX_VALUE;

    private double h;

    private Tile parent;

    public double getF() {
        return h + g;
    }

}
