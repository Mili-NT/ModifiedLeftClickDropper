package com.modifiedleftclickdropper;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;

@ConfigGroup("modifiedleftclickdropper")
public interface ModifiedLeftClickDropperConfig extends Config
{
	@ConfigItem(
			keyName = "itemList",
			name = "Item list to left-click drop",
			description = "Comma delimited list of items you want to left click drop",
			position = 1
	)
	default String itemList()
	{
		return "";
	}
	@ConfigItem(
			keyName = "excludeList",
			name = "Item list to NEVER left-click drop",
			description = "Comma delimited list of items you want to be excluded from left click dropping",
			position = 2
	)
	default String excludeList()
	{
		return "";
	}
	@ConfigItem(
			keyName = "fishingMode",
			name = "Enable fishing drop settings",
			description = "Enables left click drop for all raw fish",
			position = 3
	)
	default boolean fishingMode() {
		return false;
	}
	@ConfigItem(
			keyName = "masterFarmerMode",
			name = "Enable master farmer drop settings",
			description = "Enables left click drop for all seeds except ones useful for herblore.",
			position = 4
	)
	default boolean masterFarmerMode() {
		return false;
	}
}
