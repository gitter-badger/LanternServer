/*
 * This file is part of LanternServer, licensed under the MIT License (MIT).
 *
 * Copyright (c) LanternPowered <https://github.com/LanternPowered>
 * Copyright (c) Contributors
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the Software), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED AS IS, WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package org.lanternpowered.server.world;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

import com.flowpowered.math.vector.Vector3d;
import com.flowpowered.math.vector.Vector3i;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import org.lanternpowered.server.config.world.WorldConfig;
import org.lanternpowered.server.game.LanternGame;
import org.lanternpowered.server.network.vanilla.message.type.play.MessagePlayOutSetDifficulty;
import org.lanternpowered.server.network.vanilla.message.type.play.MessagePlayOutWorldBorder;
import org.lanternpowered.server.world.difficulty.LanternDifficulty;
import org.lanternpowered.server.world.dimension.LanternDimensionType;
import org.lanternpowered.server.world.gen.LanternGeneratorType;
import org.lanternpowered.server.world.rules.Rule;
import org.lanternpowered.server.world.rules.RuleDataTypes;
import org.lanternpowered.server.world.rules.RuleType;
import org.lanternpowered.server.world.rules.Rules;
import org.spongepowered.api.GameRegistry;
import org.spongepowered.api.data.DataContainer;
import org.spongepowered.api.data.DataQuery;
import org.spongepowered.api.data.DataView;
import org.spongepowered.api.data.MemoryDataContainer;
import org.spongepowered.api.entity.living.player.gamemode.GameMode;
import org.spongepowered.api.world.DimensionType;
import org.spongepowered.api.world.GeneratorType;
import org.spongepowered.api.world.difficulty.Difficulty;
import org.spongepowered.api.world.gen.WorldGeneratorModifier;
import org.spongepowered.api.world.storage.WorldProperties;

import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import javax.annotation.Nullable;

public final class LanternWorldProperties implements WorldProperties {

    private static final int BOUNDARY = 29999984;

    // The unique id of the world
    final UUID uniqueId;

    // The world config
    @Nullable WorldConfig worldConfig;

    // The rules of the world
    private final Rules rules = new Rules(this);

    // This is a map added by sponge, not sure what it is supposed to do yet
    final List<UUID> pendingUniqueIds = Lists.newArrayList();

    // The settings that were used to create the properties
    @Nullable LanternWorldCreationSettings creationSettings;

    // The extra properties
    DataContainer properties;

    // The type of the dimension
    LanternDimensionType<?> dimensionType;

    // The world generator modifiers
    ImmutableSet<WorldGeneratorModifier> generatorModifiers = ImmutableSet.of();

    // The generator type
    LanternGeneratorType generatorType;

    // The generator settings
    DataContainer generatorSettings;

    // Whether the difficulty is locked
    boolean difficultyLocked;

    // The name of the world
    private String name;

    // The spawn position
    Vector3i spawnPosition = Vector3i.ZERO;

    // Whether the world is initialized
    boolean initialized;
    boolean bonusChestEnabled;
    boolean commandsAllowed;
    boolean mapFeatures;
    boolean thundering;
    boolean raining;

    int rainTime;
    int thunderTime;
    int clearWeatherTime;

    long sizeOnDisk;
    long time;
    long age;

    @Nullable private LanternWorld world;

    // World border properties
    double borderCenterX;
    double borderCenterZ;

    // The current radius of the border
    double borderDiameterStart = 60000000f;
    double borderDiameterEnd = this.borderDiameterStart;

    int borderWarningDistance = 5;
    int borderWarningTime = 15;

    double borderDamage = 1;
    double borderDamageThreshold = 5;

    // The remaining time will be stored in this
    // for the first world tick
    long borderLerpTime;

    // Shrink or growing times
    private long borderTimeStart = -1;
    private long borderTimeEnd;

    // The last time the world was played in
    private long lastPlayed;

    public LanternWorldProperties(UUID uniqueId, String name) {
        this.uniqueId = uniqueId;
        this.name = name;
    }

    public LanternWorldProperties(String name) {
        this.uniqueId = UUID.randomUUID();
        this.name = name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void update(WorldConfig worldConfig, @Nullable OverriddenWorldProperties overrides,
            @Nullable LanternWorldCreationSettings creationSettings) throws IOException {
        this.worldConfig = worldConfig;
        List<String> worldGenModifiers = null;
        if (creationSettings != null) {
            this.properties = new MemoryDataContainer();
            this.creationSettings = creationSettings;
            this.commandsAllowed = creationSettings.commandsAllowed();
            this.dimensionType = creationSettings.getDimensionType();
            this.generatorType = (LanternGeneratorType) creationSettings.getGeneratorType();
            this.generatorSettings = creationSettings.getGeneratorSettings();
            this.bonusChestEnabled = creationSettings.bonusChestEnabled();
            this.mapFeatures = creationSettings.usesMapFeatures();
            this.worldConfig.getGeneration().setSeed(creationSettings.getSeed());
            this.setGameMode(creationSettings.getGameMode());
            this.setAllowsPlayerRespawns(creationSettings.allowPlayerRespawns());
            this.setDifficulty(this.creationSettings.getDifficulty());
            this.setKeepSpawnLoaded(this.creationSettings.doesKeepSpawnLoaded());
            this.setWaterEvaporates(this.creationSettings.waterEvaporates());
            this.setGeneratorModifiers(this.creationSettings.getGeneratorModifiers());
            this.setEnabled(this.creationSettings.isEnabled());
            this.setPVPEnabled(this.creationSettings.isPVPEnabled());
            this.setBuildHeight(this.creationSettings.getBuildHeight());
            this.setHardcore(this.creationSettings.isHardcore());
            worldConfig.save();
        } else if (overrides != null) {
            this.setHardcore(overrides.hardcore);
            this.setDifficulty(overrides.difficulty);
            this.setKeepSpawnLoaded(this.dimensionType.doesKeepSpawnLoaded());
            this.setAllowsPlayerRespawns(this.dimensionType.allowsPlayerRespawns());
            this.setWaterEvaporates(this.dimensionType.doesWaterEvaporate());
            this.worldConfig.getGeneration().setSeed(overrides.seed);
            if (overrides.generatorModifiers != null) {
                final List<String> modifiers = this.worldConfig.getGeneration().getGenerationModifiers();
                modifiers.clear();
                modifiers.addAll(overrides.generatorModifiers);
                worldGenModifiers = modifiers;
            }
            if (overrides.keepSpawnLoaded != null) {
                this.setKeepSpawnLoaded(overrides.keepSpawnLoaded);
            } else {
                this.setKeepSpawnLoaded(this.dimensionType.doesKeepSpawnLoaded());
            }
            if (overrides.loadOnStartup != null) {
                this.setLoadOnStartup(overrides.loadOnStartup);
            }
            if (overrides.enabled != null) {
                this.setEnabled(overrides.enabled);
            }
            worldConfig.save();
        } else {
            worldConfig.load();
            worldGenModifiers = worldConfig.getGeneration().getGenerationModifiers();
        }
        if (worldGenModifiers != null) {
            final ImmutableSet.Builder<WorldGeneratorModifier> genModifiers = ImmutableSet.builder();
            final GameRegistry registry = LanternGame.get().getRegistry();
            for (String modifier : worldGenModifiers) {
                Optional<WorldGeneratorModifier> genModifier = registry.getType(WorldGeneratorModifier.class, modifier);
                if (genModifier.isPresent()) {
                    genModifiers.add(genModifier.get());
                } else {
                    LanternGame.log().error("World generator modifier with id " + modifier +
                            " not found. Missing plugin?");
                }
            }
            this.generatorModifiers = genModifiers.build();
        }
    }

    /**
     * Gets the {@link Rules} that is attached to this properties.
     *
     * @return the rules
     */
    public Rules getRules() {
        return this.rules;
    }

    public WorldConfig getConfig() {
        return this.worldConfig;
    }

    /**
     * Sets whether the world is initialized.
     */
    public void setInitialized() {
        this.initialized = true;
    }

    public boolean doesWaterEvaporate() {
        return this.worldConfig.doesWaterEvaporate();
    }

    public void setWaterEvaporates(boolean evaporates) {
        this.worldConfig.setDoesWaterEvaporate(evaporates);
    }

    public boolean allowsPlayerRespawns() {
        return this.worldConfig.allowPlayerRespawns();
    }

    public void setAllowsPlayerRespawns(boolean allow) {
        boolean update = this.worldConfig.allowPlayerRespawns() != allow;
        this.worldConfig.setAllowPlayerRespawns(allow);
        if (update && this.world != null) {
            this.world.enableSpawnArea(allow);
        }
    }

    public int getBuildHeight() {
        return this.worldConfig.getMaxBuildHeight();
    }

    public void setBuildHeight(int buildHeight) {
        this.worldConfig.setMaxBuildHeight(buildHeight);
    }

    long getLastPlayedTime() {
        if (this.world != null) {
            return this.lastPlayed = System.currentTimeMillis();
        }
        return this.lastPlayed;
    }

    void setLastPlayedTime(long time) {
        this.lastPlayed = time;
    }

    public Optional<LanternWorld> getWorld() {
        return Optional.ofNullable(this.world);
    }

    void setWorld(@Nullable LanternWorld world) {
        this.world = world;
        if (this.world != null && world == null) {
            this.lastPlayed = System.currentTimeMillis();
        }
    }

    @Override
    public int getContentVersion() {
        return 0;
    }

    @Override
    public DataContainer toContainer() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public boolean isEnabled() {
        return this.worldConfig.isWorldEnabled();
    }

    @Override
    public void setEnabled(boolean state) {
        this.worldConfig.setWorldEnabled(state);
    }

    @Override
    public boolean loadOnStartup() {
        return this.worldConfig.loadOnStartup();
    }

    @Override
    public void setLoadOnStartup(boolean state) {
        this.worldConfig.setLoadOnStartup(state);
    }

    @Override
    public boolean doesKeepSpawnLoaded() {
        return this.worldConfig.getKeepSpawnLoaded();
    }

    @Override
    public void setKeepSpawnLoaded(boolean state) {
        this.worldConfig.setKeepSpawnLoaded(state);
    }

    @Override
    public String getWorldName() {
        return this.name;
    }

    @Override
    public UUID getUniqueId() {
        return this.uniqueId;
    }

    @Override
    public Vector3i getSpawnPosition() {
        return this.spawnPosition;
    }

    @Override
    public void setSpawnPosition(Vector3i position) {
        this.spawnPosition = checkNotNull(position, "position");
        // Generate the spawn are at the new spawn position
        // if the world is present
        boolean keepSpawnLoaded = this.worldConfig.getKeepSpawnLoaded();
        if (keepSpawnLoaded && this.world != null) {
            this.world.enableSpawnArea(true);
        }
    }

    @Override
    public LanternGeneratorType getGeneratorType() {
        return this.generatorType;
    }

    @Override
    public void setGeneratorType(GeneratorType type) {
        this.generatorType = (LanternGeneratorType) checkNotNull(type, "type");
    }

    @Override
    public long getSeed() {
        return this.worldConfig.getGeneration().getSeed();
    }

    @Override
    public long getTotalTime() {
        return this.age;
    }

    @Override
    public long getWorldTime() {
        return this.time;
    }

    @Override
    public void setWorldTime(long time) {
        this.time = time;
    }

    @Override
    public DimensionType getDimensionType() {
        return this.dimensionType;
    }

    @Override
    public boolean isRaining() {
        return this.raining;
    }

    @Override
    public void setRaining(boolean state) {
        this.raining = state;
        if (this.world != null) {
            this.world.setRaining(state);
        }
    }

    @Override
    public int getRainTime() {
        return this.rainTime;
    }

    @Override
    public void setRainTime(int time) {
        this.rainTime = time;
        if (this.world != null) {
            this.world.setRainTime(time);
        }
    }

    @Override
    public boolean isThundering() {
        return this.thundering;
    }

    @Override
    public void setThundering(boolean state) {
        this.thundering = state;
        if (this.world != null) {
            this.world.setThundering(state);
        }
    }

    @Override
    public int getThunderTime() {
        return this.thunderTime;
    }

    @Override
    public void setThunderTime(int time) {
        this.thunderTime = time;
        if (this.world != null) {
            this.world.setThunderTime(time);
        }
    }

    @Override
    public GameMode getGameMode() {
        return this.worldConfig.getGameMode();
    }

    @Override
    public void setGameMode(GameMode gameMode) {
        this.worldConfig.setGameMode(gameMode);
    }

    @Override
    public boolean usesMapFeatures() {
        return this.mapFeatures;
    }

    @Override
    public void setMapFeaturesEnabled(boolean state) {
        this.mapFeatures = state;
    }

    @Override
    public boolean isHardcore() {
        return this.worldConfig.isHardcore();
    }

    @Override
    public void setHardcore(boolean state) {
        this.worldConfig.setHardcore(state);
    }

    @Override
    public boolean areCommandsAllowed() {
        return this.commandsAllowed;
    }

    @Override
    public void setCommandsAllowed(boolean state) {
        this.commandsAllowed = state;
    }

    @Override
    public boolean isInitialized() {
        return this.initialized;
    }

    @Override
    public Difficulty getDifficulty() {
        return this.worldConfig.getDifficulty();
    }

    @Override
    public void setDifficulty(Difficulty difficulty) {
        checkNotNull(difficulty, "difficulty");
        if (this.getDifficulty() != difficulty && this.world != null) {
            this.world.broadcast(() -> new MessagePlayOutSetDifficulty((LanternDifficulty) difficulty));
        }
        this.worldConfig.setDifficulty(difficulty);
    }

    @Override
    public Optional<String> getGameRule(String gameRule) {
        Optional<RuleType<?>> optRuleType = RuleType.get(gameRule);
        if (!optRuleType.isPresent()) {
            return Optional.empty();
        }
        Optional<Rule> rule = this.rules.getRule((RuleType) optRuleType.get());
        if (!rule.isPresent()) {
            return Optional.empty();
        }
        return Optional.of(rule.get().getRawValue());
    }

    @Override
    public Map<String, String> getGameRules() {
        ImmutableMap.Builder<String, String> builder = ImmutableMap.builder();
        for (Map.Entry<RuleType<?>, Rule<?>> entry : this.rules.getRules().entrySet()) {
            builder.put(entry.getKey().getName(), entry.getValue().getRawValue());
        }
        return builder.build();
    }

    @Override
    public void setGameRule(String gameRule, String value) {
        // We cannot know what type a plugin rule would be, so string
        this.rules.getOrCreateRule(RuleType.getOrCreate(gameRule, RuleDataTypes.STRING, "")).setRawValue(value);
    }

    @Override
    public DataContainer getAdditionalProperties() {
        return this.properties;
    }

    @Override
    public Optional<DataView> getPropertySection(DataQuery path) {
        return this.properties.getView(path);
    }

    @Override
    public void setPropertySection(DataQuery path, DataView data) {
        this.properties.set(path, data);
    }

    @Override
    public Collection<WorldGeneratorModifier> getGeneratorModifiers() {
        return this.generatorModifiers;
    }

    @Override
    public void setGeneratorModifiers(Collection<WorldGeneratorModifier> modifiers) {
        this.generatorModifiers = ImmutableSet.copyOf(this.generatorModifiers);
        final List<String> genModifiers = this.worldConfig.getGeneration().getGenerationModifiers();
        genModifiers.clear();
        genModifiers.addAll(modifiers.stream().map(m -> m.getId()).collect(Collectors.toList()));
    }

    @Override
    public DataContainer getGeneratorSettings() {
        if (this.generatorSettings == null) {
            this.generatorSettings = this.generatorType.getGeneratorSettings();
        }
        return this.generatorSettings;
    }

    @Override
    public Vector3d getWorldBorderCenter() {
        return new Vector3d(this.borderCenterX, 0, this.borderCenterZ);
    }

    public MessagePlayOutWorldBorder createWorldBorderMessage() {
        return MessagePlayOutWorldBorder.initialize(this.borderCenterX, this.borderCenterZ, this.borderDiameterStart,
                this.borderDiameterEnd, this.getWorldBorderTimeRemaining(), BOUNDARY, this.borderWarningDistance,
                this.borderWarningTime);
    }

    public void setBorderDiameter(double startDiameter, double endDiameter, long time) {
        checkArgument(startDiameter >= 0, "The start diameter cannot be negative!");
        checkArgument(endDiameter >= 0, "The end diameter cannot be negative!");
        checkArgument(time >= 0, "The duration cannot be negative!");

        // Only shrink or grow if needed
        if (time == 0 || startDiameter == endDiameter) {
            this.borderDiameterStart = endDiameter;
            this.borderDiameterEnd = endDiameter;
            this.setCurrentBorderTime(0);
            if (this.world != null) {
                this.world.broadcast(() -> MessagePlayOutWorldBorder.setSize(endDiameter));
            }
        } else {
            this.borderDiameterStart = startDiameter;
            this.borderDiameterEnd = endDiameter;
            this.setCurrentBorderTime(time);
            if (this.world != null) {
                this.world.broadcast(() -> MessagePlayOutWorldBorder.lerpSize(startDiameter,
                        endDiameter, time));
            }
        }
    }

    @Override
    public void setWorldBorderCenter(double x, double z) {
        this.borderCenterX = x;
        this.borderCenterZ = z;

        if (this.world != null) {
            this.world.broadcast(() -> MessagePlayOutWorldBorder.setCenter(this.borderCenterX,
                    this.borderCenterZ));
        }
    }

    @Override
    public double getWorldBorderDiameter() {
        if (this.borderTimeStart == -1) {
            this.updateCurrentBorderTime();
        }
        if (this.borderDiameterStart != this.borderDiameterEnd) {
            double d = Math.max(this.borderTimeEnd - System.currentTimeMillis(), 0) / (this.borderTimeEnd - this.borderTimeStart);

            if (d == 0d) {
                return this.borderDiameterStart;
            } else {
                return this.borderDiameterStart + (this.borderDiameterEnd - this.borderDiameterStart) * d;
            }
        } else {
            return this.borderDiameterStart;
        }
    }

    @Override
    public void setWorldBorderDiameter(double diameter) {
        this.borderDiameterStart = diameter;
    }

    @Override
    public long getWorldBorderTimeRemaining() {
        if (this.borderTimeStart == -1) {
            this.updateCurrentBorderTime();
        }
        return Math.max(this.borderTimeEnd - System.currentTimeMillis(), 0);
    }

    void updateCurrentBorderTime() {
        this.updateCurrentBorderTime(this.borderLerpTime);
    }

    private void setCurrentBorderTime(long time) {
        this.updateCurrentBorderTime(time);
        this.borderLerpTime = time;
    }

    private void updateCurrentBorderTime(long time) {
        this.borderTimeStart = System.currentTimeMillis();
        this.borderTimeEnd = this.borderTimeStart + time;
    }

    @Override
    public void setWorldBorderTimeRemaining(long time) {
        this.setCurrentBorderTime(time);
        if (this.world != null) {
            this.world.broadcast(() -> time == 0 ? MessagePlayOutWorldBorder.setSize(this.borderDiameterEnd) :
                MessagePlayOutWorldBorder.lerpSize(this.getWorldBorderDiameter(), this.borderDiameterEnd,
                        this.getWorldBorderTimeRemaining()));
        }
    }

    @Override
    public double getWorldBorderTargetDiameter() {
        return this.borderDiameterEnd;
    }

    @Override
    public void setWorldBorderTargetDiameter(double diameter) {
        this.borderDiameterEnd = diameter;
        if (this.world != null) {
            this.world.broadcast(() -> this.getWorldBorderTimeRemaining() == 0 ? MessagePlayOutWorldBorder.setSize(
                    diameter) : MessagePlayOutWorldBorder.lerpSize(this.getWorldBorderDiameter(),
                            diameter, this.getWorldBorderTimeRemaining()));
        }
    }

    @Override
    public double getWorldBorderDamageThreshold() {
        return this.borderDamageThreshold;
    }

    @Override
    public void setWorldBorderDamageThreshold(double distance) {
        this.borderDamageThreshold = distance;
    }

    @Override
    public double getWorldBorderDamageAmount() {
        return this.borderDamage;
    }

    @Override
    public void setWorldBorderDamageAmount(double damage) {
        this.borderDamage = damage;
    }

    @Override
    public int getWorldBorderWarningTime() {
        return this.borderWarningTime;
    }

    @Override
    public void setWorldBorderWarningTime(int time) {
        this.borderWarningTime = time;
        if (this.world != null) {
            this.world.broadcast(() -> MessagePlayOutWorldBorder.setWarningTime(time));
        }
    }

    @Override
    public int getWorldBorderWarningDistance() {
        return this.borderWarningDistance;
    }

    @Override
    public void setWorldBorderWarningDistance(int distance) {
        this.borderWarningDistance = distance;
        if (this.world != null) {
            this.world.broadcast(() -> MessagePlayOutWorldBorder.setWarningBlocks(distance));
        }
    }

    @Override
    public boolean isPVPEnabled() {
        return this.worldConfig.getPVPEnabled();
    }

    @Override
    public void setPVPEnabled(boolean enabled) {
        this.worldConfig.setPVPEnabled(enabled);
    }

    public static class OverriddenWorldProperties {

        private final Difficulty difficulty;
        private final GameMode gameMode;
        private final boolean hardcore;
        private final long seed;
        @Nullable private final Boolean enabled;
        @Nullable private final Boolean keepSpawnLoaded;
        @Nullable private final Boolean loadOnStartup;
        @Nullable private final List<String> generatorModifiers;

        public OverriddenWorldProperties(Difficulty difficulty, GameMode gameMode, boolean hardcore, long seed,
                @Nullable Boolean enabled, @Nullable Boolean keepSpawnLoaded, @Nullable Boolean loadOnStartup,
                @Nullable List<String> generatorModifiers) {
            this.generatorModifiers = generatorModifiers;
            this.keepSpawnLoaded = keepSpawnLoaded;
            this.loadOnStartup = loadOnStartup;
            this.difficulty = difficulty;
            this.gameMode = gameMode;
            this.hardcore = hardcore;
            this.enabled = enabled;
            this.seed = seed;
        }
    }
}
