package com.clowdertech.velocitybackendperms.utils;

import com.velocitypowered.api.command.SimpleCommand.Invocation;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

public final class SuggestHelper {

    @SafeVarargs
    public static CompletableFuture<List<String>> execute(
            Invocation invocation,
            Function<Invocation, List<String>>... completers) {

        String[] args = invocation.arguments();
        int argCount = args.length;

        // Velocity quirk: "/cmd␣" yields args.length==1 && args[0].isEmpty()
        int idx = (argCount == 0 || (argCount == 1 && args[0].isEmpty()))
                ? 0
                : argCount - 1;

        if (idx < 0 || idx >= completers.length) {
            // No completer provided for this index
            return CompletableFuture.completedFuture(List.of());
        }

        try {
            List<String> suggestions = completers[idx].apply(invocation);
            return CompletableFuture.completedFuture(suggestions);
        } catch (Exception ex) {
            return CompletableFuture.completedFuture(List.of());
        }
    }
}
