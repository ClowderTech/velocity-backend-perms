package com.clowdertech.velocitybackendperms;

import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.permission.Tristate;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import net.kyori.adventure.text.Component;
import com.velocitypowered.api.command.SimpleCommand;
import java.util.concurrent.CompletableFuture;
import java.util.List;

public final class ConnectCommand implements SimpleCommand {

    private ProxyServer proxyServer;

    public ConnectCommand(ProxyServer server) {
        proxyServer = server;
    }

    @Override
    public void execute(final Invocation invocation) {
        CommandSource source = invocation.source();
        // Get the arguments after the command alias
        String[] args = invocation.arguments();

        if (!(source instanceof Player)) {
            source.sendMessage(Component.text("Only players can use this command."));
            return;
        }

        Player player = (Player) source;
        String serverName = args[0];

        proxyServer.getServer(serverName).ifPresentOrElse(server -> {
            String permission = "velocity.server." + serverName;
            if (player.getPermissionValue(permission) != Tristate.FALSE) {
                player.createConnectionRequest(server).fireAndForget();
            } else {
                player.sendMessage(Component.text("You lack permission to join this server."));
            }
        }, () -> player.sendMessage(Component.text("Server not found.")));
    }

    // This method allows you to control who can execute the command.
    // If the executor does not have the required permission,
    // the execution of the command and the control of its autocompletion
    // will be sent directly to the server on which the sender is located
    @Override
    public boolean hasPermission(final Invocation invocation) {
        return invocation.source().getPermissionValue("velocity.command.connect") != Tristate.FALSE;
    }

    // With this method you can control the suggestions to send
    // to the CommandSource according to the arguments
    // it has already written or other requirements you need
    // @Override
    // public List<String> suggest(final Invocation invocation) {
    // return List.of();
    // }

    // Here you can offer argument suggestions in the same way as the previous
    // method,
    // but asynchronously. It is recommended to use this method instead of the
    // previous one
    // especially in cases where you make a more extensive logic to provide the
    // suggestions
    @Override
    public CompletableFuture<List<String>> suggestAsync(final Invocation invocation) {
        return CompletableFuture.completedFuture(proxyServer.getAllServers().stream()
                .filter(server -> ((Player) invocation.source()).getPermissionValue(
                        "velocity.server." + server.getServerInfo().getName()) != Tristate.FALSE)
                .map(server -> server.getServerInfo().getName()).toList());
    }
}