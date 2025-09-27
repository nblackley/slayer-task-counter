package com.example;

import lombok.extern.slf4j.Slf4j;
import net.runelite.client.ui.PluginPanel;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

@Slf4j
public class SlayerTaskCounterPanel extends PluginPanel
{
	private final SlayerTaskCounterPlugin plugin;
	private final JLabel taskCountLabel;
	private final JButton refreshButton;

	public SlayerTaskCounterPanel(SlayerTaskCounterPlugin plugin)
	{
		this.plugin = plugin;

		setLayout(new BorderLayout());
		setBorder(new EmptyBorder(10, 10, 10, 10));

		// Title
		JLabel titleLabel = new JLabel("Slayer Task Counter");
		titleLabel.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 16));
		titleLabel.setHorizontalAlignment(SwingConstants.CENTER);
		add(titleLabel, BorderLayout.NORTH);

		// Main content panel
		JPanel contentPanel = new JPanel();
		contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
		contentPanel.setBorder(new EmptyBorder(20, 0, 0, 0));

		// Task count display
		JLabel countLabelText = new JLabel("Completed Tasks:");
		countLabelText.setAlignmentX(Component.CENTER_ALIGNMENT);
		contentPanel.add(countLabelText);

		taskCountLabel = new JLabel("0");
		taskCountLabel.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 24));
		taskCountLabel.setForeground(Color.GREEN);
		taskCountLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
		contentPanel.add(taskCountLabel);

		// Add some spacing
		contentPanel.add(Box.createVerticalStrut(20));

		// Refresh button
		refreshButton = new JButton("Refresh");
		refreshButton.setAlignmentX(Component.CENTER_ALIGNMENT);
		refreshButton.addActionListener(e -> updateTaskCount());
		contentPanel.add(refreshButton);

		add(contentPanel, BorderLayout.CENTER);

		// Initialize with current count
		updateTaskCount();
	}

	public void updateTaskCount()
	{
		try {
			int count = plugin.getCurrentTaskCount();
			taskCountLabel.setText(String.valueOf(count));
			log.debug("Updated panel task count to: {}", count);
		} catch (Exception e) {
			log.error("Error updating task count in panel", e);
			taskCountLabel.setText("Error");
		}
	}
}
