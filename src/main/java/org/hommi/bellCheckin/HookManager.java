package org.hommi.bellCheckin;

import me.TechsCode.UltraEconomy.UltraEconomy;

public class HookManager {
    public static void loadHooks() {
        if (BellCheckin.getInstance().ultraEconomy == null) {
            BellCheckin.getInstance().ultraEconomy = UltraEconomy.getAPI();;
        }
    }
}
