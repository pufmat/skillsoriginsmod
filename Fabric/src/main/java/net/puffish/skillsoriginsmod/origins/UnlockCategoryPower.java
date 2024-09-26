package net.puffish.skillsoriginsmod.origins;

import io.github.apace100.apoli.power.Power;
import io.github.apace100.apoli.power.PowerType;
import io.github.apace100.apoli.power.factory.PowerFactory;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataTypes;
import net.minecraft.entity.LivingEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import net.puffish.skillsmod.api.SkillsAPI;
import net.puffish.skillsoriginsmod.SkillsOriginsMod;

public class UnlockCategoryPower extends Power {
	public static final Identifier ID = SkillsOriginsMod.createIdentifier("unlock_category");

	private final Identifier category;

	private UnlockCategoryPower(PowerType<?> type, LivingEntity entity, Identifier category) {
		super(type, entity);
		this.category = category;
	}

	public static PowerFactory<UnlockCategoryPower> createFactory() {
		return new PowerFactory<>(
				ID,
				new SerializableData().add("category", SerializableDataTypes.IDENTIFIER),
				data -> (type, player) -> new UnlockCategoryPower(type, player, data.get("category"))
		);
	}

	@Override
	public void onAdded() {
		super.onAdded();
		if (entity instanceof ServerPlayerEntity player) {
			SkillsAPI.getCategory(category)
					.ifPresent(category -> category.unlock(player));
		}
	}

	@Override
	public void onRemoved() {
		super.onRemoved();
		if (entity instanceof ServerPlayerEntity player) {
			SkillsAPI.getCategory(category)
					.ifPresent(category -> category.lock(player));
		}
	}
}
