package com.clowdertech.velocitybackendperms.commands;

import java.util.List;
import java.util.concurrent.CompletableFuture;

import com.clowdertech.velocitybackendperms.Main;
import com.clowdertech.velocitybackendperms.utils.Detectables;
import com.clowdertech.velocitybackendperms.utils.SuggestHelper;
import com.velocitypowered.api.command.CommandMeta;
import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.command.SimpleCommand;
import com.velocitypowered.api.permission.Tristate;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import com.velocitypowered.api.proxy.server.RegisteredServer;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

public final class TransferCommand implements SimpleCommand {

    private ProxyServer proxyServer;
    private Main plugin;
    public CommandMeta commandMeta;

    public TransferCommand(ProxyServer server, Main mainPlugin) {
        proxyServer = server;
        plugin = mainPlugin;
        commandMeta = proxyServer.getCommandManager().metaBuilder("transfer").aliases("send").plugin(plugin).build();
        proxyServer.getCommandManager().unregister("send");
    }

    @Override
    public void execute(final Invocation invocation) {
        CommandSource source = invocation.source();
        // Get the arguments after the command alias
        String[] args = invocation.arguments();

        if (proxyServer.getPlayer(args[0]).isEmpty()) {
            source.sendMessage(Component.text(
                    "Specified player does not exist or is not online. Maybe you typed their username wrong?",
                    NamedTextColor.RED));
            return;
        }

        Player tPlayer = proxyServer.getPlayer(args[0]).get();

        if (!Detectables.detectablePlayers(proxyServer, source).contains(tPlayer)) {
            source.sendMessage(
                    Component.text("You do not have permission to transfer this user.",
                            NamedTextColor.RED));
            return;
        }

        if (proxyServer.getServer(args[1]).isEmpty()) {
            source.sendMessage(Component.text(
                    "Specified server does not exist. Maybe you typed its name wrong?",
                    NamedTextColor.RED));
            return;
        }

        RegisteredServer server = proxyServer.getServer(args[1]).get();

        if (!Detectables.detectableServers(proxyServer, source).contains(server)) {
            source.sendMessage(
                    Component.text("You do not have permission to transfer to that server.",
                            NamedTextColor.RED));
            return;
        }

        plugin.transferMap.add(tPlayer);

        tPlayer.createConnectionRequest(server).connectWithIndication().thenAcceptAsync(indication -> {
            if (indication) {
                source.sendMessage(
                        Component.text("Successfully transferred player",
                                NamedTextColor.GREEN));
                tPlayer.sendMessage(
                        Component.text("You were transferred to another server",
                                NamedTextColor.GRAY));
            } else {
                source.sendMessage(
                        Component.text("Transfer was unsuccessful",
                                NamedTextColor.RED));
            }
        });
    }

    // This method allows you to control who can execute the command.
    // If the executor does not have the required permission,
    // the execution of the command and the control of its autocompletion
    // will be sent directly to the server on which the sender is located
    @Override
    public boolean hasPermission(final Invocation invocation) {
        return invocation.source().getPermissionValue("velocity.command.transfer") != Tristate.FALSE;
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
        return SuggestHelper.execute(invocation,
                inv -> Detectables.detectablePlayersStrings(proxyServer, (Player) inv.source()),
                inv -> Detectables.detectableServersStrings(proxyServer, (Player) inv.source()));
    }
}