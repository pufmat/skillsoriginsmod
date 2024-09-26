package net.puffish.skillsoriginsmod.skills;

import io.github.edwinmindcraft.apoli.api.ApoliAPI;
import io.github.edwinmindcraft.apoli.api.component.IPowerContainer;
import io.github.edwinmindcraft.apoli.api.power.configuration.ConfiguredPower;
import io.github.edwinmindcraft.apoli.api.registry.ApoliDynamicRegistries;
import net.minecraft.registry.RegistryKey;
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

	private final RegistryKey<ConfiguredPower<?, ?>> powerKey;
	private final Identifier source;

	private PowerReward(RegistryKey<ConfiguredPower<?, ?>> powerKey, Identifier source) {
		this.powerKey = powerKey;
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
				.andThen(powerElement -> PowerReward.parsePowerKey(powerElement, context))
				.ifFailure(problems::add)
				.getSuccess();

		if (problems.isEmpty()) {
			return Result.success(new PowerReward(
					optPower.orElseThrow(),
					SkillsOriginsMod.createIdentifier(RandomStringUtils.random(16, "abcdefghijklmnopqrstuvwxyz0123456789"))
			));
		} else {
			return Result.failure(Problem.combine(problems));
		}
	}

	private static Result<RegistryKey<ConfiguredPower<?, ?>>, Problem> parsePowerKey(JsonElement element, RewardConfigContext context) {
		return BuiltinJson.parseIdentifier(element)
				.mapFailure(problem -> element.getPath().createProblem("Expected power type"))
				.andThen(id -> {
					if (ApoliAPI.getPowers(context.getServer()).containsId(id)) {
						return Result.success(RegistryKey.of(ApoliDynamicRegistries.CONFIGURED_POWER_KEY, id));
					} else {
						return Result.failure(element.getPath().createProblem("Unknown power type `" + id + "`"));
					}
				});
	}

	@Override
	public void update(RewardUpdateContext context) {
		IPowerContainer.get(context.getPlayer()).ifPresent(component -> {
			if (context.getCount() > 0) {
				component.addPower(powerKey, source);
			} else {
				component.removePower(powerKey, source);
			}
			component.sync();
		});
	}

	@Override
	public void dispose(RewardDisposeContext context) {
		for (var player : context.getServer().getPlayerManager().getPlayerList()) {
			IPowerContainer.get(player).ifPresent(component -> {
				component.removePower(powerKey, source);
				component.sync();
			});
		}
	}

}
