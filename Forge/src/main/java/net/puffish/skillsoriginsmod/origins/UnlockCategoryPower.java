package net.puffish.skillsoriginsmod.origins;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.apace100.calio.data.SerializableDataTypes;
import io.github.edwinmindcraft.apoli.api.IDynamicFeatureConfiguration;
import io.github.edwinmindcraft.apoli.api.power.factory.PowerFactory;
import net.minecraft.entity.Entity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import net.puffish.skillsmod.api.SkillsAPI;
import net.puffish.skillsoriginsmod.SkillsOriginsMod;

public class UnlockCategoryPower extends PowerFactory<UnlockCategoryPower.Configuration> {
	public static final Identifier ID = SkillsOriginsMod.createIdentifier("unlock_category");

	public UnlockCategoryPower() {
		super(Configuration.CODEC);
	}

	@Override
	protected void onAdded(Configuration configuration, Entity entity) {
		super.onAdded(configuration, entity);
		if (entity instanceof ServerPlayerEntity player) {
			SkillsAPI.getCategory(configuration.category())
					.ifPresent(category -> category.unlock(player));
		}
	}

	@Override
	protected void onRemoved(Configuration configuration, Entity entity) {
		super.onRemoved(configuration, entity);
		if (entity instanceof ServerPlayerEntity player) {
			SkillsAPI.getCategory(configuration.category())
					.ifPresent(category -> category.lock(player));
		}
	}

	public record Configuration(Identifier category) implements IDynamicFeatureConfiguration {
		public static final Codec<Configuration> CODEC = RecordCodecBuilder.create((instance) -> instance.group(
				SerializableDataTypes.IDENTIFIER.fieldOf("category").forGetter(Configuration::category)
		).apply(instance, Configuration::new));
	}

}
