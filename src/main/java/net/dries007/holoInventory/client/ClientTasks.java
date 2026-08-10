package net.dries007.holoInventory.client;

import net.dries007.holoInventory.HoloInventory;
import net.minecraft.client.Minecraft;

/**
 * Message handlers run on the netty event loop, so every write to the caches in {@link Renderer} has to be handed over
 * to the client thread first.
 */
public final class ClientTasks {

    private static boolean loggedTaskError = false;

    private ClientTasks() {}

    /**
     * {@link Minecraft#func_152344_a} parks a throw in a future that nobody ever reads, so a task failing on the client
     * thread would go by unnoticed. Log it here instead. The stack trace points at the handler that scheduled it.
     */
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
