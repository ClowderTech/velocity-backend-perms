package com.clowdertech.velocitybackendperms.utils;

import com.clowdertech.velocitybackendperms.Main;
import com.google.inject.Inject;
import com.velocitypowered.api.command.CommandMeta;
import com.velocitypowered.api.command.SimpleCommand;
import com.velocitypowered.api.plugin.annotation.DataDirectory;
import com.velocitypowered.api.command.CommandManager;
import com.velocitypowered.api.proxy.ProxyServer;
import org.slf4j.Logger;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.reflections.Reflections;
import org.reflections.scanners.SubTypesScanner;

public class CommandRegistrar {

    private final ProxyServer proxyServer;
    private final CommandManager commands;
    private final Logger logger;
    private final String basePackage;
    private final Main mainPlugin;

    @Inject
    public CommandRegistrar(ProxyServer proxy, Logger logger,
            /* e.g. "com.clowdertech.velocitybackendperms.commands" */
            @DataDirectory String basePackage, Main plugin) {
        this.proxyServer = proxy;
        this.commands = proxyServer.getCommandManager();
        this.logger = logger;
        this.basePackage = basePackage;
        this.mainPlugin = plugin;
    }

    /**
     * Scans the classpath under {@link #basePackage} for all classes
     * implementing SimpleCommand, instantiates them, and registers
     * them with Velocity. Returns the list of CommandMeta for further use.
     */
    public List<CommandMeta> registerAll() {
        List<CommandMeta> metas = new ArrayList<>();

        // 1. Scan for all subtypes of SimpleCommand under our package
        Reflections reflections = new Reflections(
                basePackage,
                new SubTypesScanner(false) // don't exclude Object
        );
        Set<Class<? extends SimpleCommand>> cmdClasses = reflections.getSubTypesOf(SimpleCommand.class);

        for (Class<? extends SimpleCommand> cmdClass : cmdClasses) {
            try {
                SimpleCommand cmdInstance;

                // 1) First look for (ProxyServer, Main) constructor:
                try {
                    Constructor<? extends SimpleCommand> ctor = cmdClass.getDeclaredConstructor(ProxyServer.class,
                            Main.class);
                    cmdInstance = ctor.newInstance(proxyServer, mainPlugin);
                } catch (NoSuchMethodException nsme) {
                    // 2) Fallback to no-arg:
                    cmdInstance = cmdClass.getDeclaredConstructor().newInstance();
                }

                // 3) Read the public CommandMeta field:
                CommandMeta meta = (CommandMeta) cmdClass.getField("commandMeta").get(cmdInstance);

                // 4) Register:
                commands.register(meta, cmdInstance);
                metas.add(meta);

                logger.info("Registered command: {}", meta.getAliases().stream().toList().get(0));
            } catch (NoSuchFieldException e) {
                logger.warn("Skipping {} – no public CommandMeta field", cmdClass.getName());
            } catch (Exception e) {
                logger.error("Failed to register command {}", cmdClass.getName(), e);
            }
        }

        return metas;
    }
}
