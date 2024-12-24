package net.puffish.skillsoriginsmod.skills;

import io.github.apace100.apoli.component.PowerHolderComponent;
import io.github.apace100.apoli.power.PowerType;
import io.github.apace100.apoli.power.PowerTypeRegistry;
import net.minecraft.util.Identifier;
import net.puffish.skillsmod.api.SkillsAPI;
import net.puffish.skillsmod.api.json.BuiltinJson;
import net.puffish.skillsmod.api.json.JsonElement;
import net.puffish.skillsmod.api.json.JsonObject;
import net.puffish.skillsmod.api.reward.Reward;
import net.puffish.skillsmod.api.reward.RewardConfigContext;
import net.puffish.skillsmod.api.reward.RewardDisposeContext;
import net.puffish.skillsmod.api.reward.RewardUpdateContext;
import net.puffish.skillsmod.api.util.Problem;
import net.puffish.skillsmod.api.util.Result;
import net.puffish.skillsoriginsmod.SkillsOriginsMod;
import org.apache.commons.lang3.RandomStringUtils;

import java.util.ArrayList;

public class ChangePowerReward implements Reward {
    public static final Identifier ID = SkillsOriginsMod.createIdentifier("change_power");

    private final PowerType<?> removedPowerType;
    private final PowerType<?> newPowerType;
    private final Identifier source;

    private ChangePowerReward(PowerType<?> removedPowerType, PowerType<?> newPowerType, Identifier source) {
        this.removedPowerType = removedPowerType;
        this.newPowerType = newPowerType;
        this.source = source;
    }

    public static void register() {
        SkillsAPI.registerReward(ID, ChangePowerReward::parse);
    }

    private static Result<ChangePowerReward, Problem> parse(RewardConfigContext context) {
        return context.getData()
                .andThen(JsonElement::getAsObject)
                .andThen(rootObject -> rootObject.noUnused(ChangePowerReward::parse));
    }

    private static Result<ChangePowerReward, Problem> parse(JsonObject rootObject) {
        var problems = new ArrayList<Problem>();

        var optRemovedPower = rootObject.get("remove_power")
                .andThen(ChangePowerReward::parsePowerType)
                .ifFailure(problems::add)
                .getSuccess();

        var optNewPower = rootObject.get("new_power")
                .andThen(ChangePowerReward::parsePowerType)
                .ifFailure(problems::add)
                .getSuccess();

        if (problems.isEmpty()) {
            return Result.success(new ChangePowerReward(
                    optRemovedPower.orElseThrow(),
                    optNewPower.orElseThrow(),
                    SkillsOriginsMod.createIdentifier(RandomStringUtils.random(16, "abcdefghijklmnopqrstuvwxyz0123456789"))
            ));
        } else {
            return Result.failure(Problem.combine(problems));
        }
    }

    private static Result<PowerType<?>, Problem> parsePowerType(JsonElement element) {
        return BuiltinJson.parseIdentifier(element)
                .mapFailure(problem -> element.getPath().createProblem("Expected power type"))
                .andThen(id -> {
                    try {
                        return Result.success((PowerType<?>) PowerTypeRegistry.get(id));
                    } catch (Exception ignored) {
                        return Result.failure(element.getPath().createProblem("Unknown power type `" + id + "`"));
                    }
                });
    }

    @Override
    public void update(RewardUpdateContext context) {
        var component = PowerHolderComponent.KEY.get(context.getPlayer());
        if (context.getCount() > 0) {
            component.removePower(removedPowerType, source);
            component.addPower(newPowerType, source);
        }
        component.sync();
    }

    @Override
    public void dispose(RewardDisposeContext context) {
        for (var player : context.getServer().getPlayerManager().getPlayerList()) {
            var component = PowerHolderComponent.KEY.get(player);
            component.removePower(newPowerType, source);
            component.addPower(removedPowerType, source);
            component.sync();
        }
    }
}