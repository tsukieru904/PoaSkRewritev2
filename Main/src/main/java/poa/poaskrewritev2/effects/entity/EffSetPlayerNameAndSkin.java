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
import poa.util.FoliaScheduler;
import poa.packets.FakePlayer;
import poa.poaskrewritev2.PoaSkRewritev2;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class EffSetPlayerNameAndSkin extends Effect implements Listener {

    static {
        Skript.registerEffect(EffSetPlayerNameAndSkin.class,
                "fake name of %player% to %string% with skin named %string% for %players%");
    }

    public static final ConcurrentMap<Player, Map<Player, String>> playerSkinMap = new ConcurrentHashMap<>();
    public static final ConcurrentMap<Player, Map<Player, String>> playerNameMap = new ConcurrentHashMap<>();
    public static final ConcurrentMap<Player, Map<Player, UUID>> playerUuidMap = new ConcurrentHashMap<>();


    private Expression<Player> target;
    private Expression<String> name;
    private Expression<String> skinName;
    private Expression<Player> players;

    @SuppressWarnings({"unchecked", "NullableProblems"})
    @Override
    public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
        target = (Expression<Player>) exprs[0];
        name = (Expression<String>) exprs[1];
        skinName = (Expression<String>) exprs[2];
        players = (Expression<Player>) exprs[3];
        return true;
    }


    @SuppressWarnings("NullableProblems")
    @Override
    protected void execute(Event event) {
        String name = this.name.getSingle(event);
        String skinName = this.skinName.getSingle(event);

        Player target = this.target.getSingle(event);
        Player[] players = this.players.getArray(event);

        if (name == null || skinName == null || target == null)
            return;

        for (Player player : players) {
            Map<Player, String> nameMap = new HashMap<>();
            Map<Player, String> skinMap = new HashMap<>();
            Map<Player, UUID> uuidMap = new HashMap<>();

            if (playerNameMap.containsKey(player))
                nameMap = playerNameMap.get(player);

            if (playerSkinMap.containsKey(player))
                skinMap = playerSkinMap.get(player);

            if (playerUuidMap.containsKey(player))
                uuidMap = playerUuidMap.get(player);


            UUID fakeUuid;

            if (uuidMap.containsKey(target))
                fakeUuid = uuidMap.get(target);
            else
                fakeUuid = UUID.randomUUID();


            nameMap.put(target, name);
            skinMap.put(target, skinName);
            uuidMap.put(target, fakeUuid);

            playerNameMap.put(player, nameMap);
            playerSkinMap.put(player, skinMap);
            playerUuidMap.put(player, uuidMap);


            final Player recipient = player;
            final Player targetPlayer = target;

            // On Folia the recipient and target may belong to different region threads.
            // Read target state on the target entity scheduler, then send the fake-player
            // packets on the recipient's entity scheduler.
            FoliaScheduler.entity(PoaSkRewritev2.getINSTANCE(), targetPlayer, () -> {
                if (!targetPlayer.isOnline() || !recipient.isOnline()) return;

                final var targetLocation = targetPlayer.getLocation().clone();
                final int targetPing = targetPlayer.getPing();
                final int targetEntityId = targetPlayer.getEntityId();

                FoliaScheduler.entity(PoaSkRewritev2.getINSTANCE(), recipient, () ->
                        FakePlayer.spawnFakePlayer(
                                List.of(recipient),
                                name,
                                skinName,
                                targetLocation,
                                true,
                                targetPing,
                                targetEntityId,
                                fakeUuid
                        )
                );
            });
        }
    }


    public static void unfake(Player target, List<Player> players) {
        for (Player player : players) {
            Map<Player, String> nameMap = playerNameMap.get(player);
            Map<Player, String> skinMap = playerSkinMap.get(player);
            Map<Player, UUID> uuidMap = playerUuidMap.get(player);

            UUID fakeUuid = null;

            if (uuidMap != null)
                fakeUuid = uuidMap.get(target);


            if (nameMap != null) {
                nameMap.remove(target);

                if (nameMap.isEmpty())
                    playerNameMap.remove(player);
            }

            if (skinMap != null) {
                skinMap.remove(target);

                if (skinMap.isEmpty())
                    playerSkinMap.remove(player);
            }

            if (uuidMap != null) {
                uuidMap.remove(target);

                if (uuidMap.isEmpty())
                    playerUuidMap.remove(player);
            }


            if (fakeUuid != null) {
                final Player recipient = player;
                final Player targetPlayer = target;
                FoliaScheduler.entity(PoaSkRewritev2.getINSTANCE(), targetPlayer, () -> {
                    if (!targetPlayer.isOnline() || !recipient.isOnline()) return;
                    final int targetEntityId = targetPlayer.getEntityId();
                    FoliaScheduler.entity(PoaSkRewritev2.getINSTANCE(), recipient, () ->
                            FakePlayer.removeFakePlayerPacket(
                                    List.of(recipient),
                                    List.of(fakeUuid),
                                    List.of(targetEntityId)
                            )
                    );
                });
            }
        }
    }


    @Override
    public @NotNull String toString(@Nullable Event event, boolean debug) {
        return "set player name and skin";
    }


    @EventHandler
    public void entityLoadEvent(PlayerTrackEntityEvent e) {
        if (!(e.getEntity() instanceof Player target))
            return;

        Player player = e.getPlayer();

        Map<Player, String> nameMap = playerNameMap.get(player);
        Map<Player, String> skinMap = playerSkinMap.get(player);
        Map<Player, UUID> uuidMap = playerUuidMap.get(player);

        if (nameMap == null || skinMap == null || uuidMap == null)
            return;

        String name = nameMap.get(target);
        String skinName = skinMap.get(target);
        UUID fakeUuid = uuidMap.get(target);

        if (name == null || skinName == null || fakeUuid == null)
            return;


        FoliaScheduler.asyncLater(
                PoaSkRewritev2.getINSTANCE(),
                () -> {
                    Player currentPlayer = Bukkit.getPlayer(player.getUniqueId());
                    Player currentTarget = Bukkit.getPlayer(target.getUniqueId());
                    if (currentPlayer == null || currentTarget == null) return;

                    FoliaScheduler.entity(PoaSkRewritev2.getINSTANCE(), currentTarget, () -> {
                        if (!currentTarget.isOnline() || !currentPlayer.isOnline()) return;
                        final var targetLocation = currentTarget.getLocation().clone();
                        final int targetPing = currentTarget.getPing();
                        final int targetEntityId = currentTarget.getEntityId();

                        FoliaScheduler.entityLater(PoaSkRewritev2.getINSTANCE(), currentPlayer,
                                () -> FakePlayer.spawnFakePlayer(
                                        List.of(currentPlayer), name, skinName, targetLocation, true,
                                        targetPing, targetEntityId, fakeUuid), 1L);
                    });
                },
                0L
        );
    }


    @EventHandler
    public void onQuit(PlayerQuitEvent e) {
        Player player = e.getPlayer();

        playerNameMap.remove(player);
        playerSkinMap.remove(player);
        playerUuidMap.remove(player);



        for (Map.Entry<Player, Map<Player, String>> entry : playerNameMap.entrySet()) {
            Map<Player, String> map = entry.getValue();

            if (map.containsKey(player))
                map.remove(player);
        }

        for (Map.Entry<Player, Map<Player, String>> entry : playerSkinMap.entrySet()) {
            Map<Player, String> map = entry.getValue();

            if (map.containsKey(player))
                map.remove(player);
        }

        for (Map.Entry<Player, Map<Player, UUID>> entry : playerUuidMap.entrySet()) {
            Map<Player, UUID> map = entry.getValue();

            if (map.containsKey(player))
                map.remove(player);
        }
    }
}