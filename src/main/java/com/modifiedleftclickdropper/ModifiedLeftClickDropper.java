package com.modifiedleftclickdropper;

import com.google.common.base.Splitter;
import com.google.common.collect.ArrayListMultimap;
import com.google.inject.Provides;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.GameState;
import net.runelite.api.MenuAction;
import net.runelite.api.MenuEntry;
import net.runelite.api.events.ClientTick;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.events.ConfigChanged;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.overlay.OverlayManager;
import net.runelite.client.util.Text;

import javax.inject.Inject;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@PluginDescriptor(
		name = "Modified Left Click Drop"
)
public class ModifiedLeftClickDropper extends Plugin
{
	@Inject
	private Client client;

	@Inject
	private ModifiedLeftClickDropperConfig config;
	@Inject
	private OverlayManager overlayManager;
	@Inject
	private ItemOverlay itemOverlay;
	private final ArrayListMultimap<String, Integer> optionIndexes = ArrayListMultimap.create();
	private List<String> itemList;
	private List<String> excludeList;
	private boolean fishingMode;
	private boolean masterFarmerMode;

	private HashSet<String> releaseItems;

	private final Splitter CONFIG_SPLITTER = Splitter
			.onPattern("([,\n])")
			.omitEmptyStrings()
			.trimResults();

	public List<String> patternizeList(String input) {
		return CONFIG_SPLITTER.splitToList(input)
				.stream()
				.map(item -> item.toLowerCase().replace("*", ".*"))
				.collect(Collectors.toList());
	}
	public void applyModes() {
		List<String> fishingModeDrops = patternizeList("Raw*");
		List<String> masterFarmerModeDrops = patternizeList("*seed,*spore");
		List<String> masterFarmerModeExclusions = patternizeList("Ranarr seed,Snapdragon seed,Irit seed,Cadantine seed,Avantoe seed," +
				"Kwuarm seed,Lantadyme seed,Torstol seed,Dwarf weed seed,Snape grass seed,Watermelon seed,Jangerberry seed," +
				"Whiteberry seed,Poison ivy seed,Willow seed,Maple seed,Yew seed,Magic seed,Mahogany seed,*tree seed,Hespori seed,Spirit seed");

		if (fishingMode) {
			itemList.addAll(fishingModeDrops);
		}
		if (masterFarmerMode) {
			itemList.addAll(masterFarmerModeDrops);
			excludeList.addAll(masterFarmerModeExclusions);
		}

	}
	@Subscribe
	public void onClientTick(ClientTick clientTick) {
		// The menu is not rebuilt when it is open, so don't swap or else it will
		// repeatedly swap entries
		if (client.getGameState() != GameState.LOGGED_IN || client.isMenuOpen()) {
			return;
		}

		MenuEntry[] menuEntries = client.getMenuEntries();

		// Build option map for quick lookup in findIndex
		int idx = 0;
		optionIndexes.clear();
		for (MenuEntry entry : menuEntries) {
			String option = Text.removeTags(entry.getOption()).toLowerCase();
			optionIndexes.put(option, idx++);
		}

		swapMenuEntry(menuEntries);
	}

	public boolean isItemNameIncluded(String itemName) {
		itemName = itemName.toLowerCase();
		for (String excludeItem : excludeList) {
			if (itemName.matches(excludeItem)) {
				return false;
			}
		}
		for (String includedItem : itemList) {
			if (itemName.matches(includedItem)) {
				return true;
			}
		}
		return false;
	}
	public boolean matchItemPattern(String item, String target) {
		for (String excludeItem : excludeList) {
			if (target.matches(excludeItem)) {
				return false; // Return false if a match is found in excludeList
			}
		}
		return target.matches(item);
	}

	private void swapMenuEntry(MenuEntry[] menuEntry) {
		try {
			if (itemList == null || menuEntry == null|| itemList.size() == 0) {
				return;
			}

			// menuEntry.length - 1 is the default left click option. Use,Wear,Wield,Break, etc.
			final String option = Text.removeTags(menuEntry[menuEntry.length - 1].getOption()).toLowerCase();
			final String target = Text.removeTags(menuEntry[menuEntry.length - 1].getTarget()).toLowerCase();

			for (String item : itemList) {
				if (matchItemPattern(item, target)) {

					// salamanders are the exception to the rule below
					if (option.equals("wield") && releaseItems.contains(target)) {
						swap("release", option, target, true);
					}
					// swap first option with drop
					else {
						swap("drop", option, target, true);
					}
				}
			}

		} catch (Exception ignored) {
			// ignored
		}
	}

	private void swap(String optionA, String optionB, String target, boolean strict) {
		MenuEntry[] entries = client.getMenuEntries();

		int idxA = searchIndex(entries, optionA, target, strict);
		int idxB = searchIndex(entries, optionB, target, strict);

		if (idxA != idxB) {
			MenuEntry entry1 = entries[idxB];
			MenuEntry entry2 = entries[idxA];
			entries[idxA] = entry1;
			entries[idxB] = entry2;

			// Item op4 and op5 are CC_OP_LOW_PRIORITY, so they get added underneath Use,
			// but this also causes them to get sorted after client tick. Change them to
			// CC_OP to avoid this.
			if (entry1.isItemOp() && entry1.getType() == MenuAction.CC_OP_LOW_PRIORITY) {
				entry1.setType(MenuAction.CC_OP);
			}
			if (entry2.isItemOp() && entry2.getType() == MenuAction.CC_OP_LOW_PRIORITY) {
				entry2.setType(MenuAction.CC_OP);
			}

			client.setMenuEntries(entries);
		}
	}

	private int searchIndex(MenuEntry[] entries, String option, String target, boolean strict) {
		for (int i = entries.length - 1; i >= 0; i--) {
			MenuEntry entry = entries[i];
			String entryOption = Text.removeTags(entry.getOption()).toLowerCase();
			String entryTarget = Text.removeTags(entry.getTarget()).toLowerCase();

			if (strict) {
				if (entryOption.equals(option) && entryTarget.equals(target)) {
					return i;
				}
			} else {
				if (entryOption.contains(option.toLowerCase()) && entryTarget.equals(target)) {
					return i;
				}
			}
		}
		return -1;
	}

	@Provides
	ModifiedLeftClickDropperConfig provideConfig(ConfigManager configManager) {
		return configManager.getConfig(ModifiedLeftClickDropperConfig.class);
	}

	@Override
	protected void startUp() {
		itemList = patternizeList(config.itemList().toLowerCase());
		excludeList = patternizeList(config.excludeList().toLowerCase());
		fishingMode = config.fishingMode();
		masterFarmerMode = config.masterFarmerMode();
		applyModes();
		releaseItems = new HashSet<>();
		releaseItems.add("black salamander");
		releaseItems.add("orange salamander");
		releaseItems.add("red salamander");
		releaseItems.add("swamp lizard");
		overlayManager.add(itemOverlay);
	}

	@Override
	protected void shutDown() {
		releaseItems = null;
		itemList = null;
		overlayManager.remove(itemOverlay);
	}

	@Subscribe
	private void onConfigChanged(ConfigChanged event) {
		if (event.getGroup().equals("modifiedleftclickdropper")) {
			itemList = patternizeList(config.itemList());
			excludeList = patternizeList(config.excludeList());
			fishingMode = config.fishingMode();
			masterFarmerMode = config.masterFarmerMode();
			applyModes();
		}
	}
}
