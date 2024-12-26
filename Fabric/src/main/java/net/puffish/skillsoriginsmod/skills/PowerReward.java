package net.puffish.skillsoriginsmod.skills;

import io.github.apace100.apoli.component.PowerHolderComponent;
import io.github.apace100.apoli.power.PowerType;
import io.github.apace100.apoli.power.PowerTypeRegistry;
import net.minecraft.server.network.ServerPlayerEntity;
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
import net.puffish.skillsoriginsmod.util.PowerRewardOperation;
import org.apache.commons.lang3.RandomStringUtils;

import java.util.ArrayList;
import java.util.List;

public class PowerReward implements Reward {
	public static final Identifier ID = SkillsOriginsMod.createIdentifier("power");

	private final PowerType<?> powerType;
	private final PowerRewardOperation operation;
	private final Identifier source;

	private PowerReward(PowerType<?> powerType, PowerRewardOperation operation, Identifier source) {
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
				.andThen(rootObject -> rootObject.noUnused(PowerReward::parse));
	}

	private static Result<PowerReward, Problem> parse(JsonObject rootObject) {
		var problems = new ArrayList<Problem>();

		var optPower = rootObject.get("power")
				.andThen(PowerReward::parsePowerType)
				.ifFailure(problems::add)
				.getSuccess();

		var operation = rootObject.get("operation")
				.getSuccess()
				.flatMap(element -> PowerRewardOperation.parse(element)
						.ifFailure(problems::add)
						.getSuccess()
				)
				.orElse(PowerRewardOperation.ADD);

		if (problems.isEmpty()) {
			return Result.success(new PowerReward(
					optPower.orElseThrow(),
					operation,
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
		if (context.getCount() > 0) {
			unlock(context.getPlayer());
		} else {
			lock(context.getPlayer());
		}
	}

	@Override
	public void dispose(RewardDisposeContext context) {
		context.getServer().getPlayerManager().getPlayerList().forEach(this::lock);
	}

	private void unlock(ServerPlayerEntity player) {
		var component = PowerHolderComponent.KEY.get(player);
		List<Identifier> sources = component.getSources(powerType);
		switch (operation) {
			case ADD -> component.addPower(powerType, source);
			case REMOVE -> {
				if (sources.isEmpty()) {
					return;
				}
				for (Identifier source : sources) {
					component.removePower(powerType, source);
				}
			}
			default -> throw new IllegalStateException();
		}
		component.sync();
	}

	private void lock(ServerPlayerEntity player) {
		var component = PowerHolderComponent.KEY.get(player);
		List<Identifier> sources = component.getSources(powerType);
		switch (operation) {
			case ADD -> {
				if (sources.isEmpty()) {
					return;
				}
				for (Identifier source : sources) {
					component.removePower(powerType, source);
				}
			}
			case REMOVE -> component.addPower(powerType, source);
			default -> throw new IllegalStateException();
		}
		component.sync();
	}

}
