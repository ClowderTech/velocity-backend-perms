package com.clowdertech.velocitybackendperms.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.WeakHashMap;

import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.permission.Tristate;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import com.velocitypowered.api.proxy.server.RegisteredServer;

public class Detectables {
    public static List<Player> detectablePlayers(ProxyServer proxyServer, CommandSource source) {
        List<Player> players = new ArrayList<Player>();

        WeakHashMap<Player, RegisteredServer> connectionMap = new WeakHashMap<Player, RegisteredServer>();

        proxyServer.getAllServers().forEach(server -> {
            server.getPlayersConnected().forEach(player -> {
                connectionMap.put(player, server);
            });
        });

        connectionMap.forEach((player, server) -> {
            if (source.getPermissionValue("velocity.server."
                    + server.getServerInfo().getName()) != Tristate.FALSE) {
                players.add(player);
            } else if (source instanceof Player && player.getUniqueId() == ((Player) source).getUniqueId()) {
                players.add(player);
            }

            return;
        });

        return players;
    }

    public static List<String> detectablePlayersStrings(ProxyServer proxyServer, CommandSource source) {
        return detectablePlayers(proxyServer, (Player) source).stream().map(player -> player.getUsername()).toList();
    }

    public static List<RegisteredServer> detectableServers(ProxyServer proxyServer, CommandSource source) {
        List<RegisteredServer> servers = new ArrayList<RegisteredServer>();

        proxyServer.getAllServers().forEach(server -> {
            if (source.getPermissionValue("velocity.server."
                    + server.getServerInfo().getName()) == Tristate.FALSE) {
                return;
            }

            servers.add(server);
        });

        return servers;
    }

    public static List<String> detectableServersStrings(ProxyServer proxyServer, CommandSource source) {
        return detectableServers(proxyServer, source).stream().map(server -> server.getServerInfo().getName()).toList();
    }
}
