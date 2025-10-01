package com.example;

import com.google.inject.Provides;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.ChatMessageType;
import net.runelite.api.Client;
import net.runelite.api.MessageNode;
import net.runelite.api.events.ChatMessage;
import net.runelite.client.chat.ChatCommandManager;
import net.runelite.client.chat.ChatColorType;
import net.runelite.client.chat.ChatMessageBuilder;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.ClientToolbar;
import net.runelite.client.ui.NavigationButton;
import net.runelite.client.util.ImageUtil;
import net.runelite.client.util.Text;

import java.awt.image.BufferedImage;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@PluginDescriptor(
	name = "Slayer Task Counter",
	description = "Tracks the number of completed slayer tasks",
	tags = {"slayer", "task", "counter", "pvm"}
)
public class SlayerTaskCounterPlugin extends Plugin
{
	@Inject
	private Client client;

	@Inject
	private SlayerTaskCounterConfig config;

	@Inject
	private ConfigManager configManager;

	@Inject
	private ClientToolbar clientToolbar;

	@Inject
	private ChatCommandManager chatCommandManager;

	private SlayerTaskCounterPanel panel;
	private NavigationButton navButton;

	// Pattern to match slayer task completion messages (same as TaskJinglePlugin)
	private static final Pattern CHAT_COMPLETE_MESSAGE = Pattern.compile("You've completed (?:at least )?(?<tasks>[\\d,]+) (?:Wilderness )?tasks?(?: and received \\d+ points, giving you a total of (?<points>[\\d,]+)| and reached the maximum amount of Slayer points \\((?<points2>[\\d,]+)\\))?");

	// Bracelet chat messages
	private static final String SLAUGHTER_MESSAGE = "Your bracelet of slaughter prevents your slayer count from decreasing. It then crumbles to dust.";
	private static final String EXPEDITIOUS_MESSAGE = "Your expeditious bracelet helps you progress your slayer task faster. It then crumbles to dust.";

	// Cannon chat message
	private static final String CANNON_BREAK_MESSAGE = "Your cannon has broken!";

	// Chat command constant
	private static final String TASKS_COMMAND_STRING = "!tasks";

	private int currentTaskCount;
	private int slaughterCount;
	private int expeditiousCount;
	private int cannonBreakCount;

	@Override
	protected void startUp() throws Exception
	{
		log.info("Slayer Task Counter started!");
		try {
			// Load the current counts from config
			if (config != null) {
				currentTaskCount = config.taskCount();
				slaughterCount = config.slaughterCount();
				expeditiousCount = config.expeditiousCount();
				cannonBreakCount = config.cannonBreakCount();
				log.info("Loaded counts - Tasks: {}, Slaughter: {}, Expeditious: {}, Cannon Breaks: {}", 
					currentTaskCount, slaughterCount, expeditiousCount, cannonBreakCount);
			} else {
				log.warn("Config is null, using defaults");
				currentTaskCount = 0;
				slaughterCount = 0;
				expeditiousCount = 0;
				cannonBreakCount = 0;
			}

			// Create and add sidebar panel
			panel = new SlayerTaskCounterPanel(this);
			
			// Create navigation button with icon (using RuneLite's built-in icons as fallback)
			final BufferedImage icon = ImageUtil.loadImageResource(getClass(), "/util/slayer.png");
			navButton = NavigationButton.builder()
				.tooltip("Slayer Task Counter")
				.icon(icon)
				.priority(5)
				.panel(panel)
				.build();

			clientToolbar.addNavigation(navButton);
			log.info("Added sidebar panel");

			// Register the !tasks chat command
			chatCommandManager.registerCommandAsync(TASKS_COMMAND_STRING, this::tasksLookup);
		} catch (Exception e) {
			log.error("Error loading config or creating panel", e);
			currentTaskCount = 0;
			slaughterCount = 0;
			expeditiousCount = 0;
			cannonBreakCount = 0;
		}
	}

	@Override
	protected void shutDown() throws Exception
	{
		log.info("Slayer Task Counter stopped!");
		
		// Unregister chat command
		chatCommandManager.unregisterCommand(TASKS_COMMAND_STRING);
		
		// Remove sidebar panel
		if (navButton != null) {
			clientToolbar.removeNavigation(navButton);
			navButton = null;
		}
		panel = null;
	}


	@Subscribe
	public void onChatMessage(ChatMessage event)
	{
		try {
			String chatMsg = Text.removeTags(event.getMessage());
			
			// Check if this is a slayer task completion message
			if (chatMsg.startsWith("You've completed") && 
				(chatMsg.contains("Slayer master") || chatMsg.contains("Slayer Master")))
			{
				Matcher mComplete = CHAT_COMPLETE_MESSAGE.matcher(chatMsg);
				
				if (mComplete.find()) {
					// Simply increment our counter by 1
					currentTaskCount++;
					
					// Save to config
					configManager.setConfiguration("slayertaskcounter", "taskCount", currentTaskCount);
					
					// Send congratulatory message if enabled
					if (config != null && config.showTaskMessages())
					{
						sendChatMessage("Slayer task completed! Total tasks: " + currentTaskCount);
					}
					
					log.info("Slayer task completed! Updated count to: {}", currentTaskCount);
					
					// Update panel if it exists
					if (panel != null) {
						panel.updateTaskCount();
					}
				}
			}
			
			// Check for bracelet usage if tracking is enabled
			if (config != null && config.trackBracelets()) {
				// Check for Bracelet of Slaughter usage
				if (chatMsg.equals(SLAUGHTER_MESSAGE)) {
					slaughterCount++;
					configManager.setConfiguration("slayertaskcounter", "slaughterCount", slaughterCount);
					
					if (config.showTaskMessages()) {
						sendChatMessage("Bracelet of Slaughter used! Total used: " + slaughterCount);
					}
					
					log.info("Bracelet of Slaughter used! Updated count to: {}", slaughterCount);
					
					// Update panel if it exists
					if (panel != null) {
						panel.updateBraceletCounts();
					}
				}
				
				// Check for Expeditious Bracelet usage
				if (chatMsg.equals(EXPEDITIOUS_MESSAGE)) {
					expeditiousCount++;
					configManager.setConfiguration("slayertaskcounter", "expeditiousCount", expeditiousCount);
					
					if (config.showTaskMessages()) {
						sendChatMessage("Expeditious Bracelet used! Total used: " + expeditiousCount);
					}
					
					log.info("Expeditious Bracelet used! Updated count to: {}", expeditiousCount);
					
					// Update panel if it exists
					if (panel != null) {
						panel.updateBraceletCounts();
					}
				}
			}
			
			// Check for cannon breaks if tracking is enabled
			if (config != null && config.trackCannon()) {
				if (chatMsg.equals(CANNON_BREAK_MESSAGE)) {
					cannonBreakCount++;
					configManager.setConfiguration("slayertaskcounter", "cannonBreakCount", cannonBreakCount);
					
					if (config.showTaskMessages()) {
						sendChatMessage("Cannon broken! Total breaks: " + cannonBreakCount);
					}
					
					log.info("Cannon broken! Updated count to: {}", cannonBreakCount);
					
					// Update panel if it exists
					if (panel != null) {
						panel.updateCannonCount();
					}
				}
			}
		} catch (Exception e) {
			log.error("Error in onChatMessage", e);
		}
	}


	public int getCurrentTaskCount()
	{
		// Refresh from config to ensure we have the latest value
		if (config != null) {
			currentTaskCount = config.taskCount();
		}
		return currentTaskCount;
	}

	public int getSlaughterCount()
	{
		// Refresh from config to ensure we have the latest value
		if (config != null) {
			slaughterCount = config.slaughterCount();
		}
		return slaughterCount;
	}

	public int getExpeditiousCount()
	{
		// Refresh from config to ensure we have the latest value
		if (config != null) {
			expeditiousCount = config.expeditiousCount();
		}
		return expeditiousCount;
	}

	public int getCannonBreakCount()
	{
		// Refresh from config to ensure we have the latest value
		if (config != null) {
			cannonBreakCount = config.cannonBreakCount();
		}
		return cannonBreakCount;
	}

	/**
	 * Handles the !tasks chat command
	 * Replaces the command message with the current task count, bracelet usage, and cannon breaks
	 */
	void tasksLookup(ChatMessage chatMessage, String message)
	{
		// Get the current counts
		int taskCount = getCurrentTaskCount();
		int slaughterUsed = getSlaughterCount();
		int expeditiousUsed = getExpeditiousCount();
		int cannonBreaks = getCannonBreakCount();
		
		// Build the response message with formatting
		ChatMessageBuilder builder = new ChatMessageBuilder()
			.append(ChatColorType.HIGHLIGHT)
			.append("Slayer Tasks Completed: ")
			.append(ChatColorType.NORMAL)
			.append(String.format("%,d", taskCount));
		
		// Add bracelet information if tracking is enabled and any bracelets have been used
		if (config != null && config.trackBracelets() && (slaughterUsed > 0 || expeditiousUsed > 0)) {
			builder.append(ChatColorType.HIGHLIGHT)
				.append(" | Bracelets Used - ")
				.append(ChatColorType.NORMAL)
				.append("Slaughter: ")
				.append(ChatColorType.HIGHLIGHT)
				.append(String.valueOf(slaughterUsed))
				.append(ChatColorType.NORMAL)
				.append(", Expeditious: ")
				.append(ChatColorType.HIGHLIGHT)
				.append(String.valueOf(expeditiousUsed));
		}
		
		// Add cannon break information if tracking is enabled and cannon has broken
		if (config != null && config.trackCannon() && cannonBreaks > 0) {
			builder.append(ChatColorType.HIGHLIGHT)
				.append(" | Cannon Breaks: ")
				.append(ChatColorType.NORMAL)
				.append(String.valueOf(cannonBreaks));
		}
		
		String response = builder.build();
		
		// Replace the original message with our formatted response
		final MessageNode messageNode = chatMessage.getMessageNode();
		messageNode.setRuneLiteFormatMessage(response);
		client.refreshChat();
	}

	private void sendChatMessage(String message)
	{
		// Send message to game console
		client.addChatMessage(ChatMessageType.CONSOLE, "", message, null);
	}

	@Provides
	SlayerTaskCounterConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(SlayerTaskCounterConfig.class);
	}
}
