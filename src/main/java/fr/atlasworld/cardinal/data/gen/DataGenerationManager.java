package fr.atlasworld.cardinal.data.gen;

import fr.atlasworld.cardinal.api.data.gen.DataGenerator;
import fr.atlasworld.cardinal.bootstrap.LaunchArguments;
import fr.atlasworld.cardinal.plugin.CardinalPluginManager;
import fr.atlasworld.cardinal.plugin.GroupClassLoader;
import fr.atlasworld.cardinal.plugin.PluginClassLoader;

import java.io.File;
import java.lang.reflect.Constructor;

public final class DataGenerationManager {
    private final DataGenerator generator;
    private final DataBusImpl dataBus;

    public DataGenerationManager(CardinalPluginManager manager) {
        this.generator = this.loadGenerator(manager.groupClassLoader());

        String identifier = ((PluginClassLoader) this.generator.getClass().getClassLoader()).meta().identifier();
        File file = new File(LaunchArguments.dataOutput());

        this.dataBus = new DataBusImpl(identifier, file);
    }

    private DataGenerator loadGenerator(GroupClassLoader loader) {
        String classPath = LaunchArguments.dataGen();
        if (classPath == null)
            throw new IllegalStateException("THIS SHOULD NOT HAPPEN: DataGenerator class is empty while Cardinal is started in a data generation mode!!!");

        try {
            Class<? extends DataGenerator> generatorClass = loader.loadClass(classPath, false, false, true).asSubclass(DataGenerator.class);
            Constructor<? extends DataGenerator> constructor = generatorClass.getDeclaredConstructor();

            constructor.setAccessible(true);
            try {
                return constructor.newInstance();
            } catch (Throwable ex) {
                throw new IllegalStateException("Failed to initiate data generator: " + classPath, ex);
            }
        } catch (ClassCastException ex) {
            throw new IllegalStateException("DataGenerator class must implement DataGenerator interface: " + classPath);
        } catch (ClassNotFoundException ex) {
            throw new IllegalStateException("Class '" + classPath + "' does not exists!", ex);
        } catch (NoSuchMethodException ex) {
            throw new IllegalStateException("DataGenerator class '" + classPath + "' constructor should not take parameters as input", ex);
        }
    }

    public void initialize() {
        try {
            this.generator.registerProviders(this.dataBus);
        } catch (Throwable ex) {
            this.dataBus.logger().error("Data generation failed: ", ex);
        }
    }

    public void generate() {
        try {
            this.dataBus.generate();
        } catch (Throwable ex) {
            this.dataBus.logger().error("Data generation failed: ", ex);
        }
    }
}
