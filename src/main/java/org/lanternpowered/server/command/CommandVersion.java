package org.lanternpowered.server.command;

import org.lanternpowered.server.LanternServer;
import org.lanternpowered.server.game.LanternGame;
import org.lanternpowered.server.game.LanternMinecraftVersion;
import org.lanternpowered.server.text.translation.TranslationManager;
import org.spongepowered.api.text.Texts;
import org.spongepowered.api.text.translation.Translation;
import org.spongepowered.api.util.command.CommandException;
import org.spongepowered.api.util.command.CommandResult;
import org.spongepowered.api.util.command.CommandSource;
import org.spongepowered.api.util.command.args.CommandContext;
import org.spongepowered.api.util.command.spec.CommandExecutor;
import org.spongepowered.api.util.command.spec.CommandSpec;

public class CommandVersion implements Command {

    private final Translation description;
    private final Translation minecraftVersion;
    private final Translation implementationVersion;
    private final Translation apiVersion;

    public CommandVersion(LanternGame game) {
        TranslationManager manager = game.getRegistry().getTranslationManager();

        this.description = manager.get("commands.version.description");
        this.minecraftVersion = manager.get("commands.version.minecraft");
        this.implementationVersion = manager.get("commands.version.implementation");
        this.apiVersion = manager.get("commands.version.api");
    }

    @Override
    public CommandSpec build() {
        return CommandSpec.builder()
                .permission("minecraft.command.version")
                .description(Texts.of(this.description))
                .executor(new CommandExecutor() {

                    @Override
                    public CommandResult execute(CommandSource src, CommandContext args) throws CommandException {
                        src.sendMessage(Texts.of(minecraftVersion, LanternMinecraftVersion.CURRENT.getName(),
                                LanternMinecraftVersion.CURRENT.getProtocol()));
                        String version = LanternServer.class.getPackage().getSpecificationVersion();
                        src.sendMessage(Texts.of(apiVersion, version == null ? "unknown" : version));
                        version = LanternServer.class.getPackage().getImplementationVersion();
                        src.sendMessage(Texts.of(implementationVersion, version == null ? "unknown" : version));
                        return CommandResult.success();
                    }

                }).build();
    }
}