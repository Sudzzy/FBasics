package org.originmc.fbasics.factions.api;

import java.util.HashMap;

public enum FactionsVersion {

    AUTO(new String[]{"automatic", "auto"}),
    NONE(new String[]{}),
    V2_7(new String[]{"v2_7", "v2.7", "2.7", "27"}),
    V2_6(new String[]{"v2_6", "v2.6", "2.6", "26"}),
    V1_8(new String[]{"v1_8", "v1.8", "1.8", "18"}),
    V1_6(new String[]{"v1_6", "v1.6", "1.6", "16"});

    private static final HashMap<String, FactionsVersion> BY_ALIAS = new HashMap<>();

    private final String[] aliases;

    FactionsVersion(String[] aliases) {
        this.aliases = aliases;
    }

    public static FactionsVersion parse(String str) {
        str = str.toLowerCase();
        if (BY_ALIAS.containsKey(str)) {
            return BY_ALIAS.get(str);
        }
        return NONE;
    }

    static {
        for (FactionsVersion version : values()) {
            for (String alias : version.aliases) {
                BY_ALIAS.put(alias, version);
            }
        }
    }

}
