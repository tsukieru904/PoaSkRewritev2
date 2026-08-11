package poa.poaskrewritev2.effects.entity;

import ch.njol.skript.Skript;
import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Kleenean;
import io.papermc.paper.event.player.PlayerTrackEntityEvent;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import poa.packets.FakePlayer;
import poa.poaskrewritev2.PoaSkRewritev2;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class EffUnsetPlayerNameAndSkin extends Effect{


    static {
        Skript.registerEffect(EffUnsetPlayerNameAndSkin.class,
                "unset fake (name|skin) of %player% for %players%");
    }


    private Expression<Player> target;
    private Expression<Player> players;

    @SuppressWarnings({"unchecked", "NullableProblems"})
    @Override
    public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
        target = (Expression<Player>) exprs[0];
        players = (Expression<Player>) exprs[1];
        return true;
    }



    @Override
    protected void execute(Event event) {
        EffSetPlayerNameAndSkin.unfake(target.getSingle(event), Arrays.stream(players.getArray(event)).toList());
    }



    @Override
    public @NotNull String toString(@Nullable Event event, boolean debug) {
        return "un set player name and skin";
    }






}
