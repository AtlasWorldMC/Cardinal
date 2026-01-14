package fr.atlasworld.cardinal.event;

import fr.atlasworld.cardinal.api.server.item.CardinalItem;
import fr.atlasworld.cardinal.event.entity.EntityEquipmentHandler;
import fr.atlasworld.cardinal.event.entity.EntityHandler;
import fr.atlasworld.cardinal.event.entity.ItemEntityHandler;
import fr.atlasworld.cardinal.event.item.ItemUseHandler;
import fr.atlasworld.cardinal.event.player.PlayerBlockHandler;
import fr.atlasworld.cardinal.event.player.PlayerLifecycleHandler;
import fr.atlasworld.cardinal.util.Logging;
import net.minestom.server.event.Event;
import net.minestom.server.event.EventListener;
import net.minestom.server.event.EventNode;
import net.minestom.server.event.entity.EntityAttackEvent;
import net.minestom.server.event.entity.EntityDamageEvent;
import net.minestom.server.event.item.*;
import net.minestom.server.event.player.*;
import net.minestom.server.event.trait.ItemEvent;
import org.slf4j.Logger;

import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public final class ServerListener {
    private static final Logger LOGGER = Logging.logger();
    private static final EventNode<Event> SERVER_EVENT_NODE = EventNodeFactory.createServerNode();

    public static void initialize() {
        registerItemListener(ItemDropEvent.class, ItemEntityHandler::onDrop);
        registerItemListener(PickupItemEvent.class, ItemEntityHandler::onPickup);

        registerItemListener(PlayerBeginItemUseEvent.class, ItemUseHandler::onUseStart);
        registerItemListener(PlayerCancelItemUseEvent.class, ItemUseHandler::onUseCancel);
        registerItemListener(PlayerFinishItemUseEvent.class, ItemUseHandler::onUseFinish);

        registerListener(EntityEquipEvent.class, EntityEquipmentHandler::onEquipmentChange);

        registerListener(EntityAttackEvent.class, EntityHandler::onEntityAttack);
        registerListener(EntityDamageEvent.class, EntityHandler::onEntityDamage);

        registerListener(PlayerBlockBreakEvent.class, PlayerBlockHandler::onBlockBreak);
        registerListener(PlayerBlockPlaceEvent.class, PlayerBlockHandler::onBlockPlace);
        registerListener(PlayerBlockInteractEvent.class, PlayerBlockHandler::onInteract);
        registerListener(PlayerPickBlockEvent.class, PlayerBlockHandler::onPickBlock);

        registerListener(AsyncPlayerConfigurationEvent.class, PlayerLifecycleHandler::handlePlayerConfiguration);
        registerListener(PlayerGameModeRequestEvent.class, PlayerLifecycleHandler::onGameModeRequest);
        registerListener(PlayerMoveEvent.class, PlayerLifecycleHandler::onPlayerMove);
        registerListener(PlayerDisconnectEvent.class, PlayerLifecycleHandler::handlePlayerDisconnect);
    }

    public static <E extends ItemEvent> void registerItemListener(Class<E> eventClass, BiConsumer<E, CardinalItem> handler) {
        registerListener(eventClass, true, event -> {
            Optional<CardinalItem> item = CardinalItem.fromStack(event.getItemStack());
            if (item.isEmpty())
                return;

            try {
                handler.accept(event, item.get());
            } catch (Throwable ex) {
                LOGGER.error("Failed to pass {} event to {}:", eventClass.getSimpleName(), item.getClass().getSimpleName(), ex);
            }
        });
    }

    public static <E extends Event> void registerListener(Class<E> eventClass, Consumer<E> handler) {
        registerListener(eventClass, true, handler);
    }

    public static <E extends Event> void registerListener(Class<E> eventClass, boolean skipCancelled, Consumer<E> handler) {
        SERVER_EVENT_NODE.addListener(EventListener.builder(eventClass)
                .ignoreCancelled(skipCancelled)
                .handler(handler)
                .build()
        );
    }
}
