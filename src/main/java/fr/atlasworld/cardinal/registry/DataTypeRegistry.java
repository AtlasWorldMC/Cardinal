package fr.atlasworld.cardinal.registry;

import fr.atlasworld.cardinal.api.data.DataType;
import fr.atlasworld.cardinal.api.registry.RegistryKey;
import net.kyori.adventure.key.Key;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class DataTypeRegistry extends CardinalRegistry<DataType<?>> {
    private final Map<String, DataType<?>> dataTypes;
    private final Map<String, DataType<?>> indexDataTypes;

    public DataTypeRegistry(@NotNull RegistryKey key) {
        super(key);

        this.dataTypes = new HashMap<>();
        this.indexDataTypes = new HashMap<>();
    }

    @Override
    public void register(@NotNull Key key, @NotNull DataType<?> value) {
        DataType<?> registeredDataType = this.dataTypes.get(value.type());
        if (registeredDataType != null)
            throw new IllegalArgumentException("Data type conflict for '" + value.type() + "' between  '" + key + "' and '" + this.retrieveKey(registeredDataType).get() + "'");

        super.register(key, value);
        this.dataTypes.put(value.type(), value);

        if (!value.indexed())
            this.indexDataTypes.put(value.indexFile(), value);
    }

    public Optional<DataType<?>> retrieveType(String type) {
        return Optional.ofNullable(this.dataTypes.get(type));
    }

    public Optional<DataType<?>> retrieveIndexedType(String indexFile) {
        return Optional.ofNullable(this.indexDataTypes.get(indexFile));
    }
}
