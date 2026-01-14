package fr.atlasworld.cardinal.profiler;

import fr.atlasworld.cardinal.CardinalServer;
import fr.atlasworld.cardinal.bootstrap.BuildInfo;
import me.lucko.spark.common.platform.PlatformInfo;
import net.minestom.server.MinecraftServer;

public final class CardinalPlatformInfo implements PlatformInfo {

    @Override
    public Type getType() {
        return Type.SERVER;
    }

    @Override
    public String getName() {
        return CardinalServer.SERVER_BRAND;
    }

    @Override
    public String getBrand() {
        return CardinalServer.SERVER_BRAND;
    }

    @Override
    public String getVersion() {
        return BuildInfo.version();
    }

    @Override
    public String getMinecraftVersion() {
        return MinecraftServer.VERSION_NAME;
    }
}
