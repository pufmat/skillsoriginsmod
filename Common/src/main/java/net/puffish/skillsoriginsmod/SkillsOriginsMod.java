package net.puffish.skillsoriginsmod;

import net.minecraft.util.Identifier;

public class SkillsOriginsMod {
	public static final String MOD_ID = "puffish_skills_origins";

	public static Identifier createIdentifier(String path) {
		return new Identifier(MOD_ID, path);
	}
}
