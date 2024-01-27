package com.modifiedleftclickdropper;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;

@ConfigGroup("modifiedleftclickdropper")
public interface ModifiedLeftClickDropperConfig extends Config
{
	@ConfigItem(
			keyName = "itemList",
			name = "Item list to left click drop",
			description = "Comma delimited list of items you want to left click drop",
			position = 1
	)
	default String itemList()
	{
		return "";
	}
}
