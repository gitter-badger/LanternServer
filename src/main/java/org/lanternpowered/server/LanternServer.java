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
package org.lanternpowered.server;

import static com.google.common.base.Preconditions.checkNotNull;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.util.concurrent.ListenableFuture;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import org.lanternpowered.server.config.GlobalConfig;
import org.lanternpowered.server.console.ConsoleManager;
import org.lanternpowered.server.console.LanternConsoleSource;
import org.lanternpowered.server.entity.living.player.LanternPlayer;
import org.lanternpowered.server.game.LanternGame;
import org.lanternpowered.server.game.LanternMinecraftVersion;
import org.lanternpowered.server.game.LanternPlatform;
import org.lanternpowered.server.network.NetworkManager;
import org.lanternpowered.server.network.query.QueryServer;
import org.lanternpowered.server.network.rcon.BaseRconService;
import org.lanternpowered.server.network.rcon.RconServer;
import org.lanternpowered.server.profile.LanternGameProfileManager;
import org.lanternpowered.server.status.LanternFavicon;
import org.lanternpowered.server.util.SecurityHelper;
import org.lanternpowered.server.util.ShutdownMonitorThread;
import org.lanternpowered.server.world.LanternWorldManager;
import org.lanternpowered.server.world.chunk.LanternChunkLayout;
import org.spongepowered.api.GameState;
import org.spongepowered.api.Server;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.source.ConsoleSource;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.SpongeEventFactory;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.network.status.Favicon;
import org.spongepowered.api.profile.GameProfileManager;
import org.spongepowered.api.resourcepack.ResourcePack;
import org.spongepowered.api.scoreboard.Scoreboard;
import org.spongepowered.api.service.sql.SqlService;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.channel.MessageChannel;
import org.spongepowered.api.text.serializer.TextSerializers;
import org.spongepowered.api.util.annotation.NonnullByDefault;
import org.spongepowered.api.world.ChunkTicketManager;
import org.spongepowered.api.world.World;
import org.spongepowered.api.world.WorldCreationSettings;
import org.spongepowered.api.world.storage.ChunkLayout;
import org.spongepowered.api.world.storage.WorldProperties;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.net.BindException;
import java.net.InetSocketAddress;
import java.nio.file.Path;
import java.security.KeyPair;
import java.util.Collection;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import javax.annotation.Nullable;

@NonnullByDefault
public class LanternServer implements Server {

    public static void main(String[] args) {
        try {
            // The server wasn't run from a terminal, we will just display
            // a message and the server won't be run.
            /*
             * TODO: Currently disabled until the IDE bug is fixed...
             * https://bugs.eclipse.org/bugs/show_bug.cgi?id=122429
             */
            /*
            if (System.console() == null) {
                JFrame jFrame = new JFrame();
                jFrame.setTitle("Lantern Server");
                jFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                // Create the label that should be displayed
                JLabel label = new JLabel("You have to run LanternServer through a terminal (bash).");
                // Make the size of the font a bit bigger
                Font font = label.getFont();
                label.setFont(font.deriveFont(font.getSize() * 1.4f));
                // Add the label
                jFrame.getContentPane().add(label);
                // Make the frame fit around the added content
                jFrame.pack();
                // Make it visible
                jFrame.setVisible(true);
                // Disable resizing
                jFrame.setResizable(false);
                return;
            }
            */

            // Create the game instance
            final LanternGame game = new LanternGame();
            game.preInitialize();

            final ConsoleManager consoleManager = new ConsoleManager();
            // Start the console (command input/completer)
            consoleManager.start();

            final GlobalConfig globalConfig = game.getGlobalConfig();
            RconServer rconServer = null;
            QueryServer queryServer = null;
            // Enable the query server if needed
            if (globalConfig.isQueryEnabled()) {
                queryServer = new QueryServer(game, globalConfig.getShowPluginsToQuery());
            }
            // Enable the rcon server if needed
            if (globalConfig.isRconEnabled()) {
                rconServer = new RconServer(globalConfig.getRconPassword());
            }

            // Create the server instance
            final LanternServer server = new LanternServer(game, consoleManager, rconServer, queryServer);

            // Send some startup info
            LanternGame.log().info("Starting Lantern Server {}", LanternPlatform.IMPL_VERSION);
            LanternGame.log().info("\tfor Minecraft {} with protocol {}",  LanternMinecraftVersion.CURRENT.getName(),
                    LanternMinecraftVersion.CURRENT.getProtocol());
            LanternGame.log().info("\ton  SpongeAPI {}", LanternPlatform.API_VERSION);

            // The root world folder
            final Path worldFolder = new File(game.getGlobalConfig().getRootWorldFolder()).toPath();

            // Initialize the game
            game.initialize(server, rconServer == null ? new BaseRconService(globalConfig.getRconPassword()) :
                    rconServer, worldFolder);

            // Bind the network channel
            server.bind();
            // Bind the query server
            server.bindQuery();
            // Bind the rcon server
            server.bindRcon();
            // Start the server
            server.start();
            LanternGame.log().info("Ready for connections.");
        } catch (BindException e) {
            // descriptive bind error messages
            LanternGame.log().error("The server could not bind to the requested address.");
            if (e.getMessage().startsWith("Cannot assign requested address")) {
                LanternGame.log().error("The 'server.ip' in your configuration may not be valid.");
                LanternGame.log().error("Unless you are sure you need it, try removing it.");
                LanternGame.log().error(e.toString());
            } else if (e.getMessage().startsWith("Address already in use")) {
                LanternGame.log().error("The address was already in use. Check that no server is");
                LanternGame.log().error("already running on that port. If needed, try killing all");
                LanternGame.log().error("Java processes using Task Manager or similar.");
                LanternGame.log().error(e.toString());
            } else {
                LanternGame.log().error("An unknown bind error has occurred.", e);
            }
            System.exit(1);
        } catch (Throwable t) {
            // general server startup crash
            LanternGame.log().error("Error during server startup.", t);
            System.exit(1);
        }
    }

    // The executor service for the server ticks
    private final ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor(
            runnable -> new Thread(runnable, "server"));

    // The world manager
    private LanternWorldManager worldManager;

    // The network manager
    private final NetworkManager networkManager = new NetworkManager(this);

    // The rcon server/service
    private final RconServer rconServer;

    // The query server
    private final QueryServer queryServer;

    // The key pair used for authentication
    private final KeyPair keyPair = SecurityHelper.generateKeyPair();

    // The broadcast channel
    private volatile MessageChannel broadcastChannel = MessageChannel.TO_ALL;

    // The game instance
    private final LanternGame game;

    // The console manager
    private final ConsoleManager consoleManager;

    // The maximum amount of players that can join
    private int maxPlayers;

    // The amount of ticks the server is running
    private final AtomicInteger runningTimeTicks = new AtomicInteger(0);

    private Favicon favicon;
    private boolean onlineMode;
    private boolean whitelist;

    private volatile boolean shuttingDown;

    public LanternServer(LanternGame game, ConsoleManager consoleManager, @Nullable RconServer rconServer,
            @Nullable QueryServer queryServer) {
        this.consoleManager = consoleManager;
        this.queryServer = queryServer;
        this.rconServer = rconServer;
        this.game = game;
    }

    /**
     * Get the socket address to bind to for a specified service.
     * 
     * @param port the port to use
     * @return the socket address
     */
    private InetSocketAddress getBindAddress(int port) {
        final String ip = this.game.getGlobalConfig().getServerIp();
        if (ip.length() == 0) {
            return new InetSocketAddress(port);
        } else {
            return new InetSocketAddress(ip, port);
        }
    }

    public void bind() throws BindException {
        InetSocketAddress address = this.getBindAddress(this.game.getGlobalConfig().getServerPort());

        ChannelFuture future = this.networkManager.init(address);
        Channel channel = future.awaitUninterruptibly().channel();
        if (!channel.isActive()) {
            final Throwable cause = future.cause();
            if (cause instanceof BindException) {
                throw (BindException) cause;
            }
            throw new RuntimeException("Failed to bind to address", cause);
        }

        LanternGame.log().info("Successfully bound to: " + channel.localAddress());
    }

    private void bindQuery() {
        if (this.queryServer == null) {
            return;
        }

        InetSocketAddress address = this.getBindAddress(this.game.getGlobalConfig().getQueryPort());
        this.game.getLogger().info("Binding query to address: " + address + "...");

        ChannelFuture future = this.queryServer.bind(address);
        Channel channel = future.awaitUninterruptibly().channel();
        if (!channel.isActive()) {
            this.game.getLogger().warn("Failed to bind query. Address already in use?");
        }
    }

    private void bindRcon() {
        if (this.rconServer == null) {
            return;
        }

        InetSocketAddress address = this.getBindAddress(this.game.getGlobalConfig().getRconPort());
        this.game.getLogger().info("Binding rcon to address: " + address + "...");

        ChannelFuture future = this.rconServer.bind(address);
        Channel channel = future.awaitUninterruptibly().channel();
        if (!channel.isActive()) {
            this.game.getLogger().warn("Failed to bind rcon. Address already in use?");
        }
    }

    public void start() throws IOException {
        this.worldManager = new LanternWorldManager(this.game, this.game.getSavesDirectory());
        this.worldManager.init();

        this.game.setGameState(GameState.SERVER_ABOUT_TO_START);
        this.game.getEventManager().post(SpongeEventFactory.createGameAboutToStartServerEvent(Cause.of(this.game),
                GameState.SERVER_ABOUT_TO_START));
        this.game.setGameState(GameState.SERVER_STARTING);
        this.game.getEventManager().post(SpongeEventFactory.createGameStartingServerEvent(Cause.of(this.game), 
                GameState.SERVER_STARTING));

        final GlobalConfig config = this.game.getGlobalConfig();
        this.maxPlayers = config.getMaxPlayers();
        this.onlineMode = config.isOnlineMode();

        final File faviconFile = new File(config.getFavicon());
        if (faviconFile.exists()) {
            try {
                this.favicon = LanternFavicon.load(faviconFile.toPath());
            } catch (IOException e) {
                LanternGame.log().error("Failed to load the favicon", e);
            }
        }

        this.executor.scheduleAtFixedRate(() -> {
            try {
                pulse();
            } catch (Exception e) {
                LanternGame.log().error("Error while pulsing", e);
            }
        }, 0, LanternGame.TICK_DURATION, TimeUnit.MILLISECONDS);

        this.game.setGameState(GameState.SERVER_STARTED);
        this.game.getEventManager().post(SpongeEventFactory.createGameStartedServerEvent(Cause.of(this.game), 
                GameState.SERVER_STARTED));
    }

    /**
     * Pulses (ticks) the game.
     */
    private void pulse() {
        this.runningTimeTicks.incrementAndGet();
        // Pulse the network sessions
        this.networkManager.getSessionRegistry().pulse();
        // Pulse the sync scheduler tasks
        this.game.getScheduler().pulseSyncScheduler();
        // Pulse the world threads
        this.worldManager.pulse();
    }

    /**
     * Gets the key pair.
     * 
     * @return the key pair
     */
    public KeyPair getKeyPair() {
        return this.keyPair;
    }

    /**
     * Gets the favicon of the server.
     * 
     * @return the favicon
     */
    public Optional<Favicon> getFavicon() {
        return Optional.ofNullable(this.favicon);
    }

    /**
     * Gets all the active command sources.
     * 
     * @return the active command sources
     */
    public Collection<CommandSource> getActiveCommandSources() {
        ImmutableList.Builder<CommandSource> commandSources = ImmutableList.builder();
        commandSources.add(this.getConsole());
        commandSources.addAll(this.getOnlinePlayers());
        return commandSources.build();
    }

    @Override
    public Collection<Player> getOnlinePlayers() {
        return Lists.newArrayList();
    }

    @Override
    public int getMaxPlayers() {
        return this.maxPlayers;
    }

    @Override
    public Optional<Player> getPlayer(UUID uniqueId) {
        // TODO Auto-generated method stub
        return Optional.empty();
    }

    @Override
    public Optional<Player> getPlayer(String name) {
        // TODO Auto-generated method stub
        return Optional.empty();
    }

    @Override
    public Collection<World> getWorlds() {
        return this.worldManager.getWorlds();
    }

    @Override
    public Collection<WorldProperties> getUnloadedWorlds() {
        return this.worldManager.getUnloadedWorlds();
    }

    @Override
    public Collection<WorldProperties> getAllWorldProperties() {
        return this.worldManager.getAllWorldProperties();
    }

    @Override
    public Optional<World> getWorld(UUID uniqueId) {
        return this.worldManager.getWorld(uniqueId);
    }

    @Override
    public Optional<World> getWorld(String worldName) {
        return this.worldManager.getWorld(worldName);
    }

    @Override
    public Optional<WorldProperties> getDefaultWorld() {
        return this.worldManager.getDefaultWorld();
    }

    @Override
    public String getDefaultWorldName() {
        return this.game.getGlobalConfig().getRootWorldFolder();
    }

    @Override
    public Optional<World> loadWorld(String worldName) {
        return this.worldManager.loadWorld(worldName);
    }

    @Override
    public Optional<World> loadWorld(UUID uniqueId) {
        return this.worldManager.loadWorld(uniqueId);
    }

    @Override
    public Optional<World> loadWorld(WorldProperties properties) {
        return this.worldManager.loadWorld(properties);
    }

    @Override
    public Optional<WorldProperties> getWorldProperties(String worldName) {
        return this.worldManager.getWorldProperties(worldName);
    }

    @Override
    public Optional<WorldProperties> getWorldProperties(UUID uniqueId) {
        return this.worldManager.getWorldProperties(uniqueId);
    }

    @Override
    public boolean unloadWorld(World world) {
        return this.worldManager.unloadWorld(world);
    }

    @Override
    public ListenableFuture<Optional<WorldProperties>> copyWorld(WorldProperties worldProperties, String copyName) {
        return this.worldManager.copyWorld(worldProperties, copyName);
    }

    @Override
    public Optional<WorldProperties> renameWorld(WorldProperties worldProperties, String newName) {
        return this.worldManager.renameWorld(worldProperties, newName);
    }

    @Override
    public ListenableFuture<Boolean> deleteWorld(WorldProperties worldProperties) {
        return this.worldManager.deleteWorld(worldProperties);
    }

    @Override
    public boolean saveWorldProperties(WorldProperties properties) {
        return this.worldManager.saveWorldProperties(properties);
    }

    @Override
    public Optional<Scoreboard> getServerScoreboard() {
        return Optional.empty();
    }

    @Override
    public ChunkLayout getChunkLayout() {
        return LanternChunkLayout.INSTANCE;
    }

    @Override
    public int getRunningTimeTicks() {
        return this.runningTimeTicks.get();
    }

    @Override
    public MessageChannel getBroadcastChannel() {
        return this.broadcastChannel;
    }

    @Override
    public void setBroadcastChannel(MessageChannel channel) {
        this.broadcastChannel = checkNotNull(channel, "channel");
    }

    @Override
    public Optional<InetSocketAddress> getBoundAddress() {
        return Optional.of((InetSocketAddress) this.networkManager.getAddress());
    }

    @Override
    public boolean hasWhitelist() {
        return this.whitelist;
    }

    @Override
    public void setHasWhitelist(boolean enabled) {
        this.whitelist = enabled;
    }

    @Override
    public boolean getOnlineMode() {
        return this.onlineMode;
    }

    @Override
    public Text getMotd() {
        return this.game.getGlobalConfig().getMotd();
    }

    @Override
    public void shutdown() {
        this.shutdown(this.game.getGlobalConfig().getShutdownMessage());
    }

    @SuppressWarnings("deprecation")
    @Override
    public void shutdown(Text kickMessage) {
        checkNotNull(kickMessage, "kickMessage");
        if (this.shuttingDown) {
            return;
        }
        this.shuttingDown = true;

        this.game.setGameState(GameState.SERVER_STOPPING);
        this.game.getEventManager().post(SpongeEventFactory.createGameStoppingServerEvent(Cause.of(this.game), 
                GameState.SERVER_STOPPING));

        // Debug a message
        LanternGame.log().info("Stopping the server... ({})", TextSerializers.LEGACY_FORMATTING_CODE.serialize(kickMessage));

        // Stop the console
        this.consoleManager.shutdown();

        // Kick all the online players
        this.getOnlinePlayers().forEach(player -> ((LanternPlayer) player).getConnection().disconnect(kickMessage));

        // Stop the network servers - starts the shutdown process
        // It may take a second or two for Netty to totally clean up
        this.networkManager.shutdown();

        if (this.queryServer != null) {
            this.queryServer.shutdown();
        }
        if (this.rconServer != null) {
            this.rconServer.shutdown();
        }

        // Stop the world manager
        this.worldManager.shutdown();

        // Shutdown the executor
        this.executor.shutdown();

        // Stop the async scheduler
        this.game.getScheduler().shutdownAsyncScheduler();

        // Close the sql service if possible
        this.game.getServiceManager().provide(SqlService.class).ifPresent(service -> {
            if (service instanceof Closeable) {
                try {
                    ((Closeable) service).close();
                } catch (IOException e) {
                    LanternGame.log().error("A error occurred while closing the sql service.", e);
                }
            }
        });

        // Shutdown the game profile resolver if possible
        this.game.getServiceManager().provide(GameProfileManager.class).ifPresent(gameProfileResolver -> {
            if (gameProfileResolver instanceof LanternGameProfileManager) {
                ((LanternGameProfileManager) gameProfileResolver).shutdown();
            }
        });

        try {
            this.game.getOpsConfig().save();
        } catch (IOException e) {
            LanternGame.log().error("A error occurred while saving the ops config.", e);
        }
        try {
            this.game.getWhitelistConfig().save();
        } catch (IOException e) {
            LanternGame.log().error("A error occurred while saving the whitelist config.", e);
        }
        try {
            this.game.getBanConfig().save();
        } catch (IOException e) {
            LanternGame.log().error("A error occurred while saving the bans config.", e);
        }

        this.game.setGameState(GameState.SERVER_STOPPED);
        this.game.getEventManager().post(SpongeEventFactory.createGameStoppedServerEvent(Cause.of(this.game), 
                GameState.SERVER_STOPPED));

        // Wait for a while and terminate any rogue threads
        new ShutdownMonitorThread().start();
    }

    @Override
    public ConsoleSource getConsole() {
        return LanternConsoleSource.INSTANCE;
    }

    @Override
    public ChunkTicketManager getChunkTicketManager() {
        return this.game.getChunkTicketManager();
    }

    @Override
    public double getTicksPerSecond() {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public Optional<ResourcePack> getDefaultResourcePack() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public GameProfileManager getGameProfileManager() {
        return this.game.getGameProfileManager();
    }

    @Override
    public Optional<WorldProperties> createWorldProperties(WorldCreationSettings settings) {
        return this.worldManager.createWorld(settings);
    }

}
