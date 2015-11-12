package org.originmc.fbasics.factions.api;

public enum FactionsMode {

    WHITELIST, BLACKLIST;

    /**
     * Attempts to get a factions mode from a string.
     *
     * @param name the name of the factions mode.
     * @param def  the default mode this will return.
     * @return the factions mode.
     */
    public static FactionsMode getFactionsMode(String name, FactionsMode def) {
        FactionsMode mode;
        try {
            mode = valueOf(name);
        } catch (Exception e) {
            mode = def;
        }
        return mode;
    }

}
