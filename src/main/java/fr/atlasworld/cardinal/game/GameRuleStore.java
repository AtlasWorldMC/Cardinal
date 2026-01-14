package fr.atlasworld.cardinal.game;

import com.google.common.base.Preconditions;
import fr.atlasworld.cardinal.api.game.GameRule;
import fr.atlasworld.cardinal.registry.CardinalRegistries;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class GameRuleStore {
    private final Map<GameRule<?>, Holder<?>> rules;

    public GameRuleStore() {
        this.rules = new ConcurrentHashMap<>();
    }

    @SuppressWarnings("unchecked")
    public <T> @Nullable T get(@NotNull GameRule<T> rule) {
        Preconditions.checkNotNull(rule);
        Preconditions.checkArgument(CardinalRegistries.GAME_RULES.containsValue(rule), "Game rule not registered.");

        Holder<T> holder = (Holder<T>) this.rules.get(rule);
        return holder != null ? holder.get() : rule.defaultValue();
    }

    @SuppressWarnings("unchecked")
    public <T> void setGame(@NotNull GameRule<T> rule, @Nullable T value) {
        Preconditions.checkNotNull(rule);
        Preconditions.checkArgument(CardinalRegistries.GAME_RULES.containsValue(rule), "Game rule not registered.");

        Holder<T> previousHolder = (Holder<T>) this.rules.get(rule);
        Holder<T> holder;
        if (previousHolder != null)
            holder = previousHolder.withDefaultValue(value);
        else
            holder = new Holder<>(rule, value, null);

        this.rules.put(rule, holder);
    }

    @SuppressWarnings("unchecked")
    public <T> void set(@NotNull GameRule<T> rule, @Nullable T value) {
        Preconditions.checkNotNull(rule);
        Preconditions.checkArgument(CardinalRegistries.GAME_RULES.containsValue(rule), "Game rule not registered.");

        Holder<T> previousHolder = (Holder<T>) this.rules.get(rule);
        Holder<T> holder;
        if (previousHolder != null)
            holder = previousHolder.withValue(value);
        else
            holder = new Holder<>(rule, null, value);

        this.rules.put(rule, holder);
    }

    private record Holder<T> (@NotNull GameRule<T> rule, @Nullable T defaultValue, @Nullable T value) {
        public @Nullable T get() {
            if (this.value != null)
                return this.value;

            if (this.defaultValue != null)
                return this.defaultValue;

            if (this.rule.defaultValue() != null)
                return this.rule.defaultValue();

            return null;
        }

        public @Nullable T defaultValue() {
            return this.defaultValue;
        }

        public @Nullable T value() {
            return this.value;
        }

        public Holder<T> withDefaultValue(@Nullable T defaultValue) {
            return new Holder<>(this.rule, defaultValue, this.value);
        }

        public Holder<T> withValue(@Nullable T value) {
            return new Holder<>(this.rule, this.defaultValue, value);
        }
    }
}
