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

	@ConfigItem(
		keyName = "slaughterCount",
		name = "Bracelet of Slaughter Count",
		description = "Number of Bracelet of Slaughter used"
	)
	default int slaughterCount()
	{
		return 0;
	}

	@ConfigItem(
		keyName = "expeditiousCount",
		name = "Expeditious Bracelet Count",
		description = "Number of Expeditious Bracelets used"
	)
	default int expeditiousCount()
	{
		return 0;
	}

	@ConfigItem(
		keyName = "trackBracelets",
		name = "Track Bracelets",
		description = "Track usage of Bracelet of Slaughter and Expeditious Bracelets"
	)
	default boolean trackBracelets()
	{
		return true;
	}

	@ConfigItem(
		keyName = "cannonBreakCount",
		name = "Cannon Break Count",
		description = "Number of times your cannon has broken"
	)
	default int cannonBreakCount()
	{
		return 0;
	}

	@ConfigItem(
		keyName = "trackCannon",
		name = "Track Cannon Breaks",
		description = "Track when your cannon breaks during slayer tasks"
	)
	default boolean trackCannon()
	{
		return true;
	}

}
