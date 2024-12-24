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

public class PowerReward implements Reward {
	public static final Identifier ID = SkillsOriginsMod.createIdentifier("power");

	private final PowerType<?> powerType;
	private final String operation;
	private final Identifier source;

	private PowerReward(PowerType<?> powerType, String operation, Identifier source) {
		this.powerType = powerType;
		this.operation = operation;
		this.source = source;
	}

	public static void register() {
		SkillsAPI.registerReward(ID, PowerReward::parse);
	}

	private static Result<PowerReward, Problem> parse(RewardConfigContext context) {
		return context.getData()
				.andThen(JsonElement::getAsObject)
				.andThen(rootObject -> rootObject.noUnused(o -> parse(o, context)));
	}

	private static Result<PowerReward, Problem> parse(JsonObject rootObject, RewardConfigContext context) {
		var problems = new ArrayList<Problem>();

		var optPower = rootObject.get("power")
				.andThen(PowerReward::parsePowerType)
				.ifFailure(problems::add)
				.getSuccess();

		var optOperation = rootObject.get("operation")
				.andThen(PowerReward::parseOperation)
				.ifFailure(problems::add)
				.getSuccess();

		if (problems.isEmpty()) {
			return Result.success(new PowerReward(
					optPower.orElseThrow(),
					optOperation.orElseThrow(),
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

	private static Result<String, Problem> parseOperation(JsonElement element) {
		return element.getAsString()
				.mapFailure(problem -> element.getPath().createProblem("Expected operation \"add\" or \"remove\""))
				.andThen(operation -> {
					if (operation.equals("add") || operation.equals("remove")) {
						return Result.success(operation);
					} else {
						return Result.failure(element.getPath().createProblem("Unknown operation `" + operation + "`"));
					}
				});
	}

	@Override
	public void update(RewardUpdateContext context) {
		var component = PowerHolderComponent.KEY.get(context.getPlayer());
		if (context.getCount() > 0) {
			if (operation.equals("add")) {
				component.addPower(powerType, source);
			} else {
				component.removePower(powerType, source);
			}
		}
		component.sync();
	}

	@Override
	public void dispose(RewardDisposeContext context) {
		for (var player : context.getServer().getPlayerManager().getPlayerList()) {
			var component = PowerHolderComponent.KEY.get(player);
			if (operation.equals("add")) {
				component.removePower(powerType, source);
			} else {
				component.addPower(powerType, source);
			}
			component.sync();
		}
	}
}