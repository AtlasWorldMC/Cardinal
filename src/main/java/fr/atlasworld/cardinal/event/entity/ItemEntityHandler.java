package fr.atlasworld.cardinal.event.entity;

import fr.atlasworld.cardinal.api.server.item.CardinalItem;
import net.minestom.server.event.item.ItemDropEvent;
import net.minestom.server.event.item.PickupItemEvent;

public final class ItemEntityHandler {
    public static void onDrop(ItemDropEvent event, CardinalItem item) {
        boolean allow = item.onDrop(event.getInstance(), event.getPlayer(), event.getItemStack());
        event.setCancelled(!allow);
    }

    public static void onPickup(PickupItemEvent event, CardinalItem item) {
        boolean allow = item.onPickup(event.getLivingEntity(), event.getItemEntity(), event.getItemStack());
        event.setCancelled(!allow);
    }
}
