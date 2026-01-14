package fr.atlasworld.cardinal.data.gen;

import com.google.common.base.Preconditions;
import com.google.gson.JsonElement;
import fr.atlasworld.cardinal.api.data.gen.DataBus;
import fr.atlasworld.cardinal.api.data.gen.DataGenerator;
import fr.atlasworld.cardinal.api.data.gen.provider.DataProvider;
import fr.atlasworld.cardinal.api.registry.RegistryKey;
import fr.atlasworld.cardinal.api.util.Serializers;
import fr.atlasworld.cardinal.bootstrap.LaunchArguments;
import net.kyori.adventure.key.Key;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;

public final class DataBusImpl implements DataBus {
    public static final String DATA_DIR = "data";
    public static final String ASSET_DIR = "assets";

    private final String namespace;
    private final File outputDir;

    private final Logger logger;
    private final List<DataProvider> providers;

    private DataProvider activeProvider;

    public DataBusImpl(@NotNull String namespace, File outputDir) {
        Preconditions.checkNotNull(namespace);
        Preconditions.checkArgument(RegistryKey.isValidNamespace(namespace), "Invalid namespace: " + namespace);

        this.outputDir = outputDir;
        this.namespace = namespace;
        this.logger = LoggerFactory.getLogger(DataGenerator.class.getSimpleName() + "-" + this.namespace);
        this.providers = new ArrayList<>();

        if (LaunchArguments.dataGenNoOptimization())
            this.logger.warn("Data Generator will not optimize json files, this makes the files more readable, but heavier and slower to generated, not recommended for production use.");
    }

    public void generate() throws IOException {
        for (DataProvider provider : this.providers) {
            this.logger.info("Calling generation for '{}'.", provider.path());
            this.activeProvider = provider;
            provider.generate(this);
        }

        this.activeProvider = null;
    }

    @Override
    public @NotNull String namespace() {
        return this.namespace;
    }

    @Override
    public @NotNull Logger logger() {
        return this.logger;
    }

    @Override
    public void registerProvider(@NotNull DataProvider provider) {
        this.providers.add(provider);
    }

    @Override
    public void registerProviders(@NotNull DataProvider... providers) {
        this.providers.addAll(List.of(providers));
    }

    @Override
    public void write(@NotNull DataProvider provider, @NotNull Key key, @NotNull JsonElement json) throws IOException {
        String raw = LaunchArguments.dataGenNoOptimization() ? Serializers.GSON_PRETTY_WRITING.toJson(json) : Serializers.GSON.toJson(json);

        try (OutputStream stream = this.openStream(provider, key)) {
            stream.write(raw.getBytes(StandardCharsets.UTF_8));
        }
    }

    @Override
    public @NotNull OutputStream openStream(@NotNull DataProvider provider, @NotNull Key key) throws IOException {
        Preconditions.checkState(this.activeProvider != null, "Data Generator attempted to write early to the data bus!");
        Preconditions.checkState(this.activeProvider == provider, "A provider attempted to write to the data bus, while another provider is active.");

        File baseDir = new File(this.outputDir, provider.type() == DataProvider.Type.DATA ? DATA_DIR : ASSET_DIR);
        File typeDir = new File(new File(baseDir, key.namespace()), provider.path());

        File file = new File(typeDir, key.value() + ".json");
        if (file.isFile())
            this.logger.debug("Overwriting data entry: {}", key);

        if (!file.getParentFile().exists())
            file.getParentFile().mkdirs();

        this.logger.info("Generating file: {}", file);
        return Files.newOutputStream(file.toPath(), StandardOpenOption.CREATE, StandardOpenOption.WRITE);
    }
}
