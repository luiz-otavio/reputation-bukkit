package com.orbitalstudios.minecraft.util;

import com.orbitalstudios.minecraft.ReputationPlugin;
import org.bukkit.Bukkit;

/**
 * @author Luiz O. F. Corrêa
 * @since 07/01/2023
 **/
public class Synchronize {

    public static void callToSync(Runnable runnable) {
        if (Bukkit.isPrimaryThread()) {
            runnable.run();
        } else {
            Bukkit.getScheduler().callSyncMethod(ReputationPlugin.getInstance(), () -> {
                runnable.run();
                return null;
            });
        }
    }

}
