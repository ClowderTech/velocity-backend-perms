package com.clowdertech.velocitybackendperms;

import com.clowdertech.velocitybackendperms.utils.CommandRegistrar;
import com.google.inject.Inject;
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
import java.util.Map;
import java.util.WeakHashMap;

@Plugin(id = "velocity-backend-perms", name = "VelocityBackendPerms", version = "1.0.0", description = "Gates backend server access via LuckPerms", authors = {
        "MrScarySpaceCat" })
public class Main {

    private final ProxyServer server;
    private final Logger logger;
    private final Path dataDirectory;
    public final Map<Player, Boolean> transferMap = new WeakHashMap<>();

    @Inject
    public Main(ProxyServer server, Logger logger, @DataDirectory Path dataDirectory) {
        this.server = server;
        this.logger = logger;
        this.dataDirectory = dataDirectory;
    }

    @Subscribe
    public void onProxyInitialization(ProxyInitializeEvent event) {
            CommandRegistrar registrar = new CommandRegistrar(
            server,
            logger,
            "com.clowdertech.velocitybackendperms.commands",
            this
        );
        registrar.registerAll();

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
        if (outcome == Tristate.FALSE && !transferMap.get(player)) {
            player.disconnect(
                    Component.text("You lack permission to join this server.", NamedTextColor.RED));
            event.setResult(ServerPreConnectEvent.ServerResult.denied());
            logger.debug("Denied {} → {} (node {} = FALSE)",
                    player.getUsername(),
                    target.getServerInfo().getName(),
                    node);
        } else {
            if (transferMap.get(player)) {
                transferMap.remove(player);
            }
            logger.trace("Allowed {} → {} (node {} = {})",
                    player.getUsername(),
                    target.getServerInfo().getName(),
                    node,
                    outcome);
        }
    }
}
