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

	// Chat command constant
	private static final String TASKS_COMMAND_STRING = "!tasks";

	private int currentTaskCount;

	@Override
	protected void startUp() throws Exception
	{
		log.info("Slayer Task Counter started!");
		try {
			// Load the current task count from config
			if (config != null) {
				currentTaskCount = config.taskCount();
				log.info("Loaded task count: {}", currentTaskCount);
			} else {
				log.warn("Config is null, using defaults");
				currentTaskCount = 0;
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

	/**
	 * Handles the !tasks chat command
	 * Replaces the command message with the current task count
	 */
	void tasksLookup(ChatMessage chatMessage, String message)
	{
		// Get the current task count
		int taskCount = getCurrentTaskCount();
		
		// Build the response message with formatting
		String response = new ChatMessageBuilder()
			.append(ChatColorType.HIGHLIGHT)
			.append("Slayer Tasks Completed: ")
			.append(ChatColorType.NORMAL)
			.append(String.format("%,d", taskCount))
			.build();
		
		
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
