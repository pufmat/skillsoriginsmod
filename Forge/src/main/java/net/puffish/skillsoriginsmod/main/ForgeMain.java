package net.puffish.skillsoriginsmod.main;

import io.github.edwinmindcraft.apoli.api.component.IPowerContainer;
import io.github.edwinmindcraft.apoli.api.registry.ApoliRegistries;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.puffish.skillsmod.api.SkillsAPI;
import net.puffish.skillsoriginsmod.SkillsOriginsMod;
import net.puffish.skillsoriginsmod.origins.UnlockCategoryPower;
import net.puffish.skillsoriginsmod.skills.ChangePowerReward;
import net.puffish.skillsoriginsmod.skills.PowerReward;
import net.puffish.skillsoriginsmod.skills.RemovePowerReward;

@Mod(SkillsOriginsMod.MOD_ID)
public class ForgeMain {

	public ForgeMain() {
		PowerReward.register();
		RemovePowerReward.register();
		ChangePowerReward.register();

		var forgeEventBus = MinecraftForge.EVENT_BUS;
		forgeEventBus.addListener(this::onPlayerLoggedIn);

		var deferredRegister = DeferredRegister.create(
				ApoliRegistries.POWER_FACTORY_KEY,
				UnlockCategoryPower.ID.getNamespace()
		);
		deferredRegister.register(UnlockCategoryPower.ID.getPath(), UnlockCategoryPower::new);
		deferredRegister.register(FMLJavaModLoadingContext.get().getModEventBus());
	}

	private void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
		if (event.getEntity() instanceof ServerPlayerEntity player) {
			IPowerContainer.get(player).ifPresent(container -> {
				for (var power : container.getPowerTypes(true)) {
					for (var source : container.getSources(power)) {
						if (source.getNamespace().equals(SkillsOriginsMod.MOD_ID)) {
							container.removePower(power, source);
						}
					}
				}
			});
			SkillsAPI.updateRewards(player, PowerReward.ID);
			SkillsAPI.updateRewards(player, RemovePowerReward.ID);
			SkillsAPI.updateRewards(player, ChangePowerReward.ID);
		}
	}
}
