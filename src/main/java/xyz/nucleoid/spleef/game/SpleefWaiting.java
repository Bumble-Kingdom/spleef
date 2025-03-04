package xyz.nucleoid.spleef.game;

import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.MutableText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.GameMode;
import xyz.nucleoid.fantasy.RuntimeWorldConfig;
import xyz.nucleoid.plasmid.game.GameOpenContext;
import xyz.nucleoid.plasmid.game.GameOpenProcedure;
import xyz.nucleoid.plasmid.game.GameResult;
import xyz.nucleoid.plasmid.game.GameSpace;
import xyz.nucleoid.plasmid.game.common.GameWaitingLobby;
import xyz.nucleoid.plasmid.game.event.GameActivityEvents;
import xyz.nucleoid.plasmid.game.event.GamePlayerEvents;
import xyz.nucleoid.plasmid.game.player.PlayerOffer;
import xyz.nucleoid.plasmid.game.player.PlayerOfferResult;
import xyz.nucleoid.plasmid.game.rule.GameRuleType;
import xyz.nucleoid.spleef.game.map.SpleefMap;
import xyz.nucleoid.spleef.game.map.SpleefMapGenerator;
import xyz.nucleoid.spleef.game.map.SpleefTemplateMapBuilder;
import xyz.nucleoid.stimuli.event.player.PlayerDamageEvent;
import xyz.nucleoid.stimuli.event.player.PlayerDeathEvent;

public final class SpleefWaiting {
    private final GameSpace gameSpace;
    private final ServerWorld world;
    private final SpleefMap map;
    private final SpleefConfig config;

    private SpleefWaiting(GameSpace gameSpace, ServerWorld world, SpleefMap map, SpleefConfig config) {
        this.gameSpace = gameSpace;
        this.world = world;
        this.map = map;
        this.config = config;
    }

    public static GameOpenProcedure open(GameOpenContext<SpleefConfig> context) {
        var config = context.config();
        var map = config.map().map(
            generatedConfig -> {
                var generator = new SpleefMapGenerator(generatedConfig);
                return generator.build();
            },
            templateConfig -> {
                var builder = new SpleefTemplateMapBuilder(templateConfig);
                return builder.build(context.server());
            }
        );

        var worldConfig = new RuntimeWorldConfig()
                .setGenerator(map.asGenerator(context.server()))
                .setTimeOfDay(config.timeOfDay());

        return context.openWithWorld(worldConfig, (game, world) -> {
            GameWaitingLobby.addTo(game, config.players());

            var waiting = new SpleefWaiting(game.getGameSpace(), world, map, config);

            game.deny(GameRuleType.CRAFTING);
            game.deny(GameRuleType.PORTALS);
            game.deny(GameRuleType.PVP);
            game.deny(GameRuleType.FALL_DAMAGE);
            game.deny(GameRuleType.HUNGER);
            game.deny(GameRuleType.THROW_ITEMS);
            game.deny(GameRuleType.INTERACTION);

            game.listen(GameActivityEvents.REQUEST_START, waiting::requestStart);

            game.listen(GamePlayerEvents.OFFER, waiting::offerPlayer);
            game.listen(PlayerDamageEvent.EVENT, (player, source, amount) -> ActionResult.FAIL);
            game.listen(PlayerDeathEvent.EVENT, (player, source) -> ActionResult.FAIL);
        });
    }

    private GameResult requestStart() {
        SpleefActive.open(this.gameSpace, this.world, this.map, this.config);
        return GameResult.ok();
    }

    private PlayerOfferResult offerPlayer(PlayerOffer offer) {
        var spawn = this.map.getSpawn();
        if (spawn == null) {
            return offer.reject(Text.translatable("text.spleef.no_spawn"));
        }

        return offer.accept(this.world, Vec3d.ofCenter(spawn))
                .and(() -> {
                    var player = offer.player();

                    player.changeGameMode(GameMode.ADVENTURE);
                    player.addStatusEffect(new StatusEffectInstance(
                            StatusEffects.NIGHT_VISION,
                            StatusEffectInstance.INFINITE,
                            1,
                            true,
                            false
                    ));

                    //Disclose Source + Give Credit
                    MutableText text = Text.empty();
                    text.append("This game is open source! Visit the source code by clicking on this message!");
                    text.setStyle(Style.EMPTY.withClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, "https://github.com/Bumble-Kingdom/spleef")));
                    player.sendMessage(text);

                    player.sendMessage(Text.of("The original works of this project was created by NucleoidMC licensed under LPGL3."));
                });
    }
}
