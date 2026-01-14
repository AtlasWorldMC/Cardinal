package fr.atlasworld.cardinal.api.server.item;

import com.google.common.base.Preconditions;
import fr.atlasworld.cardinal.api.registry.CardinalRegistries;
import fr.atlasworld.cardinal.api.registry.RegistryHolder;
import fr.atlasworld.cardinal.api.registry.RegistryKey;
import fr.atlasworld.cardinal.api.server.CardinalItemProviders;
import fr.atlasworld.cardinal.api.server.component.ServerDataComponent;
import fr.atlasworld.cardinal.api.util.MathUtils;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.nbt.BinaryTag;
import net.kyori.adventure.nbt.CompoundBinaryTag;
import net.minestom.server.codec.Codec;
import net.minestom.server.codec.StructCodec;
import net.minestom.server.codec.Transcoder;
import net.minestom.server.component.DataComponent;
import net.minestom.server.component.DataComponentMap;
import net.minestom.server.component.DataComponents;
import net.minestom.server.item.Material;
import net.minestom.server.item.component.AttributeList;
import net.minestom.server.item.component.CustomData;
import net.minestom.server.item.component.ItemRarity;
import net.minestom.server.sound.SoundEvent;
import net.minestom.server.tag.Tag;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * Represents the raw data loaded of an item loaded from a datapack.
 */
public final class ItemPrototype implements ServerDataComponent.Holder {
    public static final Codec<ItemPrototype> CODEC = StructCodec.struct(
            "material", Codec.KEY, prototype -> prototype.material().key(),
            "provider", Codec.KEY.optional(CardinalItemProviders.BASIC.key()), prototype -> prototype.provider().key(),
            "custom_model", Codec.BOOLEAN.optional(true), ItemPrototype::custom,
            "components", Codec.NBT_COMPOUND.optional(CompoundBinaryTag.empty()), ItemPrototype::componentsAsNBT,
            ItemPrototype::fromCodec
    );

    private final Material material;
    private final RegistryHolder<ItemProvider> provider;
    private final boolean custom;
    private final Map<Key, BinaryTag> components;

    private ItemPrototype(@NotNull Material material, @NotNull RegistryHolder<ItemProvider> provider, boolean custom, @NotNull Map<Key, BinaryTag> components) {
        this.material = material;
        this.provider = provider;
        this.custom = custom;
        this.components = components;
    }

    /**
     * The base material the item uses.
     *
     * @return base material the item uses.
     */
    public Material material() {
        return this.material;
    }

    /**
     * The provider associated with the item.
     * <br>
     * This defines the behavior of the item, mostly used to add custom functionalities to the item.
     *
     * @return the provider associated with the item.
     * @see ItemProvider
     */
    public RegistryHolder<ItemProvider> provider() {
        return this.provider;
    }

    /**
     * Whether the item should be considered as custom.
     * <br>
     * This means that on the client side, the item will appear with a custom model, and a custom name.
     *
     * @return {@code true} if the item should be considered as custom, {@code false} otherwise.
     */
    public boolean custom() {
        return this.custom;
    }

    /**
     * Retrieve the raw components attached to the item.
     *
     * @return raw components attached to the item.
     */
    public Map<Key, BinaryTag> components() {
        return this.components;
    }

    /**
     * Retrieve the components in a {@link CompoundBinaryTag} format.
     *
     * @return components of the item in a {@link CompoundBinaryTag} format.
     */
    public CompoundBinaryTag componentsAsNBT() {
        CompoundBinaryTag.Builder builder = CompoundBinaryTag.builder();
        this.components.forEach((key, binaryTag) -> builder.put(key.asString(), binaryTag));
        return builder.build();
    }

    /**
     * {@inheritDoc}
     * @param component {@inheritDoc}
     *
     * @return {@inheritDoc}
     * @param <T> {@inheritDoc}
     */
    @Override
    public <T> @Nullable T get(RegistryHolder<ServerDataComponent<T>> component) {
        Key key = component.key();
        if (!this.components.containsKey(key))
            return null;

        return component.get().decode(Transcoder.NBT, this.components.get(key)).orElse(null);
    }

    public DataComponentMap resolveComponents() {
        DataComponentMap.Builder builder = DataComponentMap.builder();

        for (Key key : this.components.keySet()) {
            ComponentEntry<?> entry = this.resolveComponent(key);
            entry.encode(builder);
        }

        return builder.build();
    }

    @SuppressWarnings("unchecked")
    private <T> ComponentEntry<T> resolveComponent(@NotNull Key key) {
        Preconditions.checkNotNull(key, "Key cannot be null!");

        BinaryTag data = this.components.get(key);
        Preconditions.checkNotNull(data, "No component found for key '" + key + "'!");

        DataComponent<@NotNull T> vanillaComponent = (DataComponent<@NotNull T>) DataComponent.fromKey(key);
        if (vanillaComponent != null)
            return new ComponentEntry<>(vanillaComponent, key, data);

        ServerDataComponent<T> component = (ServerDataComponent<T>) CardinalRegistries.DATA_COMPONENTS.retrieveValue(key)
                .orElseThrow(() -> new IllegalArgumentException("Unknown component: " + key));

        return new ComponentEntry<>(component, key, data);
    }

    /**
     * Private method to parse the content of the codec into a proper {@link ItemPrototype}.
     *
     * @param materialKey key of the material.
     * @param providerKey key of the item provider.
     * @param customModel whether the item should use a custom model.
     * @param components the components attached to the item.
     *
     * @return a new {@link ItemPrototype} with the parsed data.
     */
    @ApiStatus.Internal
    private static ItemPrototype fromCodec(@NotNull Key materialKey, @NotNull Key providerKey, boolean customModel, @NotNull CompoundBinaryTag components) {
        Preconditions.checkNotNull(materialKey, "Material cannot be null!");
        Preconditions.checkNotNull(providerKey, "Provider cannot be null!");
        Preconditions.checkNotNull(components, "Components cannot be null!");

        Material material = Material.fromKey(materialKey);
        Preconditions.checkNotNull(material, "Material '" + materialKey + "' does not exist!");

        RegistryHolder<ItemProvider> provider = CardinalRegistries.ITEM_PROVIDERS.retrieveHolder(providerKey);
        Preconditions.checkArgument(provider.referencePresent(), "No provider found for key '" + providerKey + "'!");

        Map<Key, BinaryTag> parsedComponents = components.stream()
                .map(entry -> {
                    Key key = RegistryKey.fromString(entry.getKey()).orElseThrow(() -> new IllegalArgumentException("Invalid component key: " + entry.getKey()));
                    return Map.entry(key, entry.getValue());
                })
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

        return new ItemPrototype(material, provider, customModel, parsedComponents);
    }

    /**
     * Item prototype builder, used to create a new {@link ItemPrototype}.
     */
    public static final class Builder {
        private final @NotNull Map<Key, BinaryTag> components;

        private @NotNull Material material;
        private @NotNull RegistryHolder<ItemProvider> provider;
        private boolean custom;

        public Builder() {
            this.components = new HashMap<>();

            this.material = Material.STICK;
            this.provider = CardinalItemProviders.BASIC;
            this.custom = true;
        }

        /**
         * Sets the material the item will use.
         *
         * @param material material of the item.
         *
         * @return this builder.
         */
        public Builder material(@NotNull Material material) {
            Preconditions.checkNotNull(material, "Material cannot be null!");
            this.material = material;
            return this;
        }

        /**
         * Sets the item provider the item will use.
         * <br>
         * This is used to control the behavior of the item.
         *
         * @param provider item provider.
         *
         * @return this builder.
         */
        public Builder provider(@NotNull RegistryHolder<ItemProvider> provider) {
            Preconditions.checkNotNull(provider, "Provider cannot be null!");
            this.provider = provider;
            return this;
        }

        /**
         * Whether the item should use a custom model.
         *
         *
         * @param customModel {@code true} if the item should use a custom model, {@code false} otherwise.
         *
         * @return this builder.
         */
        public Builder custom(boolean customModel) {
            this.custom = customModel;
            return this;
        }

        /**
         * Attach a component to the item.
         *
         * @param component component to attach.
         * @param value value to associate with the component.
         *
         * @return this builder.
         * @param <T> the type of the component.
         */
        public <T> Builder component(@NotNull DataComponent<@NotNull T> component, @NotNull T value) {
            Preconditions.checkNotNull(component, "Component cannot be null!");
            Preconditions.checkNotNull(value, "Value cannot be null!");

            Key key = component.key();
            BinaryTag tag = component.encode(Transcoder.NBT, value).orElseThrow("Failed to encode value for component '" + key + "'!");

            this.components.put(key, tag);
            return this;
        }

        /**
         * Attach a component to the item.
         *
         * @param component component to attach.
         * @param value value to associate with the component.
         *
         * @return this builder.
         * @param <T> the type of the component.
         */
        public <T> Builder component(@NotNull RegistryHolder<ServerDataComponent<@NotNull T>> component, @NotNull T value) {
            Preconditions.checkNotNull(component, "Component cannot be null!");
            Preconditions.checkNotNull(value, "Value cannot be null!");

            Key key = component.key();
            BinaryTag tag = component.resolve().encode(Transcoder.NBT, value).orElseThrow("Failed to encode value for component '" + key + "'!");

            this.components.put(key, tag);
            return this;
        }

        /**
         * Sets the max stack size of the item.
         *
         * @param stackSize max stack size, between 1 and 99, inclusive.
         *
         * @return this builder.
         */
        public Builder maxStackSize(int stackSize) {
            Preconditions.checkArgument(MathUtils.isBetween(stackSize, 1, 99), "Stack size must be between 1 and 99!");
            return this.component(DataComponents.MAX_STACK_SIZE, stackSize);
        }

        /**
         * Sets the rarity of the item.
         *
         * @param rarity item rarity.
         *
         * @return this builder.
         */
        public Builder rarity(@NotNull ItemRarity rarity) {
            Preconditions.checkNotNull(rarity, "Rarity cannot be null!");
            return this.component(DataComponents.RARITY, rarity);
        }

        /**
         * Sets the break sound of the weapon / tool / armor.
         *
         * @param sound sound to play when the item breaks.
         *
         * @return this builder.
         */
        public Builder breakSound(@NotNull SoundEvent sound) {
            Preconditions.checkNotNull(sound, "Sound may not be null!");
            return this.component(DataComponents.BREAK_SOUND, sound);
        }

        /**
         * Sets the max durability of the item.
         *
         * @param durability durability of the item.
         *
         * @return this builder.
         */
        public Builder durability(int durability) {
            Preconditions.checkArgument(durability >= 0, "Durability must be positive or zero!");

            return this.component(DataComponents.MAX_DAMAGE, durability)
                    .component(DataComponents.MAX_STACK_SIZE, 1)
                    .component(DataComponents.DAMAGE, 1);
        }

        public Builder repairCost(int repairCost) {
            Preconditions.checkArgument(repairCost >= 0, "Repair cost must be positive or zero!");

            return this.component(DataComponents.REPAIR_COST, repairCost);
        }

        /**
         * Sets the attribute modifiers of the item.
         * <br><br>
         * This will override previously set modifiers.
         *
         * @param builder builder that should define the modifiers.
         *
         * @return this builder.
         */
        public Builder attributeModifier(@NotNull Consumer<List<AttributeList.Modifier>> builder) {
            Preconditions.checkNotNull(builder, "Builder cannot be null!");
            List<AttributeList.Modifier> modifiers = new ArrayList<>();
            builder.accept(modifiers);

            return this.component(DataComponents.ATTRIBUTE_MODIFIERS, new AttributeList(modifiers));
        }

        /**
         * Build the item prototype.
         *
         * @return newly created item prototype.
         */
        public ItemPrototype build() {
            return new ItemPrototype(this.material, this.provider, this.custom, this.components);
        }
    }

    private static class ComponentEntry<T> {
        private final @Nullable DataComponent<@NotNull T> component;
        private final @Nullable ServerDataComponent<T> serverComponent;
        private final Key key;
        private final BinaryTag tag;

        public ComponentEntry(@NotNull DataComponent<@NotNull T> component, @NotNull Key key, @NotNull BinaryTag tag) {
            Preconditions.checkNotNull(component, "Component cannot be null!");
            Preconditions.checkNotNull(key, "Key cannot be null!");
            Preconditions.checkNotNull(tag, "Tag cannot be null!");

            this.component = component;
            this.serverComponent = null;
            this.key = key;
            this.tag = tag;
        }

        public ComponentEntry(@NotNull ServerDataComponent<T> serverComponent, @NotNull Key key, @NotNull BinaryTag tag) {
            Preconditions.checkNotNull(serverComponent, "Server component cannot be null!");
            Preconditions.checkNotNull(key, "Key cannot be null!");
            Preconditions.checkNotNull(tag, "Tag cannot be null!");

            this.component = null;
            this.serverComponent = serverComponent;
            this.key = key;
            this.tag = tag;
        }

        public DataComponent<?> component() {
            return this.component;
        }

        public ServerDataComponent<?> serverComponent() {
            return this.serverComponent;
        }

        public Key key() {
            return this.key;
        }

        public BinaryTag tag() {
            return this.tag;
        }

        public void encode(DataComponentMap.Builder map) {
            if (this.component != null) {
                map.set(this.component, this.component.decode(Transcoder.NBT, this.tag).orElseThrow("Unable to encode component: " + this.key));
                return;
            }

            // This is used to validate that this component can be parsed, but I don't like the way this is done atm.
            // TODO: Find maybe some way to not waste time parsing components that will never be used.
            this.serverComponent.decode(Transcoder.NBT, this.tag).orElseThrow("Unable to encode component: " + this.key);

            CustomData data = map.get(DataComponents.CUSTOM_DATA, CustomData.EMPTY);
            data = data.withTag(Tag.NBT(this.key.toString()), this.tag);
            map.set(DataComponents.CUSTOM_DATA, data);
        }
    }
}
