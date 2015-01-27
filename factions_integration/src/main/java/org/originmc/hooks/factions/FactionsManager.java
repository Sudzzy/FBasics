package org.originmc.hooks.factions;

import java.util.List;

public class FactionsManager {

    private FactionsHook hook = null;

    public FactionsManager(String version, String msgFaction, List<String> factions) {
        try {
            if (version.startsWith("1.6")) {
                Class<?> clazz = Class.forName("org.originmc.fbasics.hooks.factions.Factions1695");
                Object object = clazz.newInstance();
                hook = (FactionsHook) object;
            } else if (version.startsWith("1.8")) {
                Class<?> clazz = Class.forName("org.originmc.fbasics.hooks.factions.Factions182");
                Object object = clazz.newInstance();
                hook = (FactionsHook) object;
            } else if (version.startsWith("2.6")) {
                Class<?> clazz = Class.forName("org.originmc.fbasics.hooks.factions.Factions260");
                Object object = clazz.newInstance();
                hook = (FactionsHook) object;
            } else if (version.startsWith("2.7")) {
                Class<?> clazz = Class.forName("org.originmc.fbasics.hooks.factions.Factions275");
                Object object = clazz.newInstance();
                hook = (FactionsHook) object;
            }
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        }
    }

    public FactionsHook getHook() {
        return hook;
    }
}
