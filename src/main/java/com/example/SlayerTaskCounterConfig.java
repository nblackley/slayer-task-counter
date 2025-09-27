package com.example;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;

@ConfigGroup("slayertaskcounter")
public interface SlayerTaskCounterConfig extends Config
{
	@ConfigItem(
		keyName = "taskCount",
		name = "Task Count",
		description = "Number of completed slayer tasks"
	)
	default int taskCount()
	{
		return 0;
	}

	@ConfigItem(
		keyName = "showTaskMessages",
		name = "Show Task Messages",
		description = "Display messages when tasks are completed"
	)
	default boolean showTaskMessages()
	{
		return true;
	}

}
