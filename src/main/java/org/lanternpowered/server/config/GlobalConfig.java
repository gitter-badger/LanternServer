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
package org.lanternpowered.server.config;

import static org.lanternpowered.server.config.ConfigConstants.ENABLED;

import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import ninja.leaping.configurate.objectmapping.Setting;
import ninja.leaping.configurate.objectmapping.serialize.ConfigSerializable;
import org.lanternpowered.server.config.world.chunk.ChunkLoading;
import org.lanternpowered.server.config.world.chunk.ChunkLoadingConfig;
import org.lanternpowered.server.config.world.chunk.ChunkLoadingTickets;
import org.lanternpowered.server.config.world.chunk.GlobalChunkLoading;
import org.lanternpowered.server.util.IpSet;
import org.spongepowered.api.text.Text;

import java.io.IOException;
import java.net.InetAddress;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;

public class GlobalConfig extends ConfigBase implements ChunkLoadingConfig {

    public GlobalConfig(Path path) throws IOException {
        super(path);
    }

    @Setting(value = "server", comment = "Configuration for the server.")
    private Server server = new Server();

    @Setting(value = "worlds", comment = "Configuration for the worlds.")
    private World worlds = new World();

    @Setting(value = "commands")
    private Commands commands = new Commands();

    @Setting(value = "rcon", comment = "Configuration for the rcon server.")
    private Rcon rcon = new Rcon();

    @Setting(value = "query", comment = "Configuration for the query server.")
    private Query query = new Query();

    @ConfigSerializable
    public static class Commands {

        @Setting(value = "aliases", comment = "A mapping from unqualified command alias to plugin id"
                + " of the plugin that should handle a certain command")
        private Map<String, String> aliases = Maps.newHashMap();
    }

    @ConfigSerializable
    private static final class Query {

        @Setting(value = ENABLED, comment = "Whether the query server should be enabled.")
        private boolean enabled = false;

        @Setting(value = "show-plugins", comment = "Whether all the plugins should be added to the query.")
        private boolean showPlugins = true;

        @Setting(value = "port", comment = "The port that should be bound.")
        private int port = 25563;
    }

    @ConfigSerializable
    private static final class Rcon {

        @Setting(value = ENABLED, comment = "Whether the rcon server should be enabled.")
        private boolean enabled = false;

        @Setting(value = "password", comment = "The password that is required to login.")
        private String password = "";

        @Setting(value = "port", comment = "The port that should be bound.")
        private int port = 25564;
    }

    @ConfigSerializable
    private static final class Server {

        @Setting(value = "ip", comment =
                "The ip address that should be bound, leave it empty\n " +
                "to bind to the \"localhost\"")
        private String ip = "";

        @Setting(value = "port", comment = "The port that should be bound.")
        private int port = 25565;

        @Setting(value = "name", comment = "The name of the server.")
        private String name = "Lantern Server";

        @Setting(value = "favicon", comment =
                "The path of the favicon file. The format must be in png and\n " +
                "the dimension must be 64x64, otherwise will it not work.")
        private String favicon = "favicon.png";

        @Setting(value = "online-mode", comment =
                "Whether you want to enable the online mode, it is recommend\n " +
                "to run the server in online modus.")
        private boolean onlineMode = true;

        @Setting(value = "max-players", comment =
                "The maximum amount of players that may join the server.")
        private int maxPlayers = 20;

        @Setting(value = "message-of-the-day", comment =
                "This is the message that will be displayed in the\n " +
                "server list.")
        private Text motd = Text.of("A lantern minecraft server!");

        @Setting(value = "shutdown-message", comment =
                "This is the default message that will be displayed when the server is shut down.")
        private Text shutdownMessage = Text.of("Server shutting down.");

        @Setting(value = "network-compression-threshold")
        private int networkCompressionThreshold = 256;

        // Some context related stuff, check this issue for more information
        // https://github.com/SpongePowered/SpongeCommon/commit/71220742baf4b0317ddefe625b12cc64a7ec9084
        // TODO: Move this?
        @Setting(value = "ip-sets")
        private Map<String, List<IpSet>> ipSets = Maps.newHashMap();

        // TODO: Move this?
        @Setting(value = "op-permission-level", comment = "The default op level of all the operators.")
        private int opPermissionLevel = 4;
    }

    @ConfigSerializable
    private static final class World {

        @Setting(value = ChunkLoading.CHUNK_LOADING, comment = "Configuration for the chunk loading control.")
        private GlobalChunkLoading chunkLoading = new GlobalChunkLoading();

        @Setting(value = "root-folder", comment = "The name of the root world folder.")
        private String worldFolder = "world";
    }

    public Map<String, String> getCommandAliases() {
        return this.commands.aliases;
    }

    public Map<String, Predicate<InetAddress>> getIpSets() {
        return ImmutableMap.copyOf(Maps.transformValues(this.server.ipSets, Predicates::and));
    }

    @Nullable
    public Predicate<InetAddress> getIpSet(String name) {
        return this.server.ipSets.containsKey(name) ? Predicates.and(this.server.ipSets.get(name)) : null;
    }

    public int getDefaultOpPermissionLevel() {
        return this.server.opPermissionLevel;
    }

    public Text getShutdownMessage() {
        return this.server.shutdownMessage;
    }

    public int getPlayerTicketCount() {
        return this.worlds.chunkLoading.getPlayerTicketCount();
    }

    public String getRootWorldFolder() {
        return this.worlds.worldFolder;
    }

    public String getServerIp() {
        return this.server.ip;
    }

    public int getNetworkCompressionThreshold() {
        return this.server.networkCompressionThreshold;
    }

    public int getRconPort() {
        return this.rcon.port;
    }

    public String getRconPassword() {
        return this.rcon.password;
    }

    public boolean isRconEnabled() {
        return this.rcon.enabled;
    }

    public int getQueryPort() {
        return this.query.port;
    }

    public boolean isQueryEnabled() {
        return this.query.enabled;
    }

    public boolean getShowPluginsToQuery() {
        return this.query.showPlugins;
    }

    public int getServerPort() {
        return this.server.port;
    }

    public int getMaxPlayers() {
        return this.server.maxPlayers;
    }

    public String getServerName() {
        return this.server.name;
    }

    public String getFavicon() {
        return this.server.favicon;
    }

    public Text getMotd() {
        return this.server.motd;
    }

    public boolean isOnlineMode() {
        return this.server.onlineMode;
    }

    @Override
    public ChunkLoadingTickets getChunkLoadingTickets(String plugin) {
        return this.worlds.chunkLoading.getChunkLoadingTickets(plugin);
    }

}
