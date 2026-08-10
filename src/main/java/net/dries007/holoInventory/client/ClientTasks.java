package net.dries007.holoInventory.client;

import net.dries007.holoInventory.HoloInventory;
import net.minecraft.client.Minecraft;

public final class ClientTasks {

    private static boolean loggedTaskError = false;

    private ClientTasks() {}

    /** {@link Minecraft#func_152344_a} parks a throw in a future nobody reads, so failures have to be logged here. */
    public static void schedule(Runnable task) {
        Minecraft.getMinecraft().func_152344_a(() -> {
            try {
                task.run();
            } catch (Exception e) {
                if (!loggedTaskError) {
                    loggedTaskError = true;
                    HoloInventory.getLogger()
                            .warn("Could not handle a hologram packet. Further errors are not logged.", e);
                }
            }
        });
    }
}
