package net.puffish.skillsoriginsmod.util;

import net.puffish.skillsmod.api.json.JsonElement;
import net.puffish.skillsmod.api.util.Problem;
import net.puffish.skillsmod.api.util.Result;

public enum PowerRewardOperation {
	ADD,
	REMOVE;

	public static Result<PowerRewardOperation, Problem> parse(JsonElement rootElement) {
		return rootElement.getAsString().andThen(string -> switch (string) {
			case "add" -> Result.success(ADD);
			case "remove" -> Result.success(REMOVE);
			default -> Result.failure(rootElement.getPath().createProblem(
					"Expected operation `add` or `remove`, but got `" + string + "`"
			));
		});
	}
}