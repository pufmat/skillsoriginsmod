package net.puffish.skillsoriginsmod.main;

import io.github.apace100.apoli.component.PowerHolderComponent;
import io.github.apace100.apoli.registry.ApoliRegistries;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.registry.Registry;
import net.puffish.skillsmod.api.SkillsAPI;
import net.puffish.skillsoriginsmod.SkillsOriginsMod;
import net.puffish.skillsoriginsmod.origins.UnlockCategoryPower;
import net.puffish.skillsoriginsmod.skills.PowerReward;

public class FabricMain implements ModInitializer {

	@Override
	public void onInitialize() {
		PowerReward.register();
		Registry.register(
				ApoliRegistries.POWER_FACTORY,
				UnlockCategoryPower.ID,
				UnlockCategoryPower.createFactory()
		);

		ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
			var component = PowerHolderComponent.KEY.get(handler.getPlayer());
			for (var power : component.getPowerTypes(true)) {
				for (var source : component.getSources(power)) {
					if (source.getNamespace().equals(SkillsOriginsMod.MOD_ID)) {
						component.removePower(power, source);
					}
				}
			}
			SkillsAPI.updateRewards(handler.getPlayer(), PowerReward.ID);
		});
	}

}
