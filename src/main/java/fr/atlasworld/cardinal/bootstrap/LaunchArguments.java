package fr.atlasworld.cardinal.bootstrap;

import fr.atlasworld.cardinal.CardinalServer;
import joptsimple.OptionException;
import joptsimple.OptionParser;
import joptsimple.OptionSet;
import joptsimple.OptionSpec;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.io.File;
import java.io.IOException;
import java.util.List;

public class LaunchArguments {
    private static OptionSpec<String> DATA_GENERATOR_CLASS;
    private static OptionSpec<String> DATA_OUTPUT;

    private static final OptionParser PARSER = new OptionParser() {
        {
            // Informative
            acceptsAll(allOf("v", "version"), "Displays server version.");
            acceptsAll(allOf("h", "help", "?"), "Displays help menu.");

            // Dev
            acceptsAll(allOf("dev"), "Enables developer mode.");

            // Data Generation
            DATA_GENERATOR_CLASS = acceptsAll(allOf("data-gen"),
                    "Target data generator to use to generate plugin data and assets packs")
                    .withRequiredArg().describedAs("data-generator-class").ofType(String.class);
            DATA_OUTPUT = acceptsAll(allOf("data-gen-out"), "The output directory of the generated assets and data.")
                    .withRequiredArg().describedAs("Output Directory")
                    .defaultsTo("output/")
                    .ofType(String.class);
            acceptsAll(List.of("data-gen-no-optimization"), "Disables the data generation optimizations, makes file heavier but more readable.");

            // Utilities
            acceptsAll(allOf("skip-cache", "no-cache"), "Skips the cache.");
            acceptsAll(List.of("add-plugin"), "Adds an extra plugin file.")
                    .withRequiredArg()
                    .ofType(File.class)
                    .describedAs("file");
        }
    };

    private static OptionSet parsedArgs;

    public static void parse(String[] args) {
        if (parsedArgs != null)
            throw new IllegalStateException("Launch arguments have already been parsed.");

        try {
            parsedArgs = PARSER.parse(args);
        } catch (OptionException ex) {
            System.err.println("\nFailed to start " + CardinalServer.SERVER_BRAND + ": " + ex.getMessage() + "\n");
            System.err.println("Use '--help', '-? or -h' to display available options.'");
            System.exit(-1);
        }

        processAction();
    }

    public static boolean devMode() {
        return parsedArgs.has("dev");
    }

    public static boolean skipCache() {
        return parsedArgs.has("skip-cache");
    }

    // Data Generation
    public static @Nullable String dataGen() {
        if (parsedArgs.has(DATA_GENERATOR_CLASS))
            return parsedArgs.valueOf(DATA_GENERATOR_CLASS);

        return null;
    }

    public static @NotNull String dataOutput() {
        return parsedArgs.valueOf(DATA_OUTPUT);
    }

    public static boolean dataGenNoOptimization() {
        return parsedArgs.has("data-gen-no-optimization");
    }

    @SuppressWarnings("unchecked")
    public static List<File> addedPlugins() {
        return (List<File>) parsedArgs.valuesOf("add-plugin");
    }

    private static void processAction() {
        if (parsedArgs.has("help"))
            printHelp();

        if (parsedArgs.has("version"))
            printVersion();
    }

    private static void printHelp() {
        try {
            System.out.println();
            PARSER.printHelpOn(System.out);
            System.out.println();

            System.exit(0);
        } catch (IOException ex) {
            System.err.println("Failed to print help menu: " + ex.getMessage());
            System.exit(-1);
        }
    }

    private static void printVersion() {
        System.out.println();
        System.out.println(BuildInfo.print());
        System.out.println("Compiled " + BuildInfo.printableTimeSinceBuild() + " ago");
        System.out.println();

        System.exit(0);
    }

    private static List<String> allOf(String... str) {
        return List.of(str);
    }
}
