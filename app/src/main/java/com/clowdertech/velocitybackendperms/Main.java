package com.clowdertech.velocitybackendperms;

import com.google.inject.Inject;
import com.velocitypowered.api.command.CommandManager;
import com.velocitypowered.api.command.CommandMeta;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.player.ServerPreConnectEvent;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.plugin.annotation.DataDirectory;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import com.velocitypowered.api.permission.Tristate;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.slf4j.Logger;

import java.nio.file.Path;

@Plugin(id = "velocity-backend-perms", name = "VelocityBackendPerms", version = "1.0.0", description = "Gates backend server access via LuckPerms", authors = {
        "MrScarySpaceCat" })
public class Main {

    private final ProxyServer server;
    private final Logger logger;
    private final Path dataDirectory;

    @Inject
    public Main(ProxyServer server, Logger logger, @DataDirectory Path dataDirectory) {
        this.server = server;
        this.logger = logger;
        this.dataDirectory = dataDirectory;
    }

    @Subscribe
    public void onProxyInitialization(ProxyInitializeEvent event) {
        CommandManager commandManager = server.getCommandManager();

        CommandMeta commandMeta = commandManager.metaBuilder("connect").aliases("server").plugin(this).build();

        commandManager.unregister("server");
        commandManager.register(commandMeta, new ConnectCommand(server));

        logger.info("VelocityBackendPerms initialized, data directory: {}", dataDirectory);
    }

    @Subscribe
    public void onServerPreConnect(ServerPreConnectEvent event) {
        Player player = event.getPlayer();
        RegisteredServer target = event.getResult().getServer().orElse(null);
        if (target == null) {
            return;
        }

        String node = "velocity.server." + target.getServerInfo().getName();
        Tristate outcome = player.getPermissionValue(node);

        // Only block on explicit FALSE; UNDEFINED → allow
        if (outcome == Tristate.FALSE) {
            player.disconnect(
                    Component.text("You lack permission to join this server.", NamedTextColor.RED));
            event.setResult(ServerPreConnectEvent.ServerResult.denied());
            logger.debug("Denied {} → {} (node {} = FALSE)",
                    player.getUsername(),
                    target.getServerInfo().getName(),
                    node);
        } else {
            logger.trace("Allowed {} → {} (node {} = {})",
                    player.getUsername(),
                    target.getServerInfo().getName(),
                    node,
                    outcome);
        }
    }
}
