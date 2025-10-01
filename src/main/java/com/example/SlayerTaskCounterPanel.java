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
	private final JLabel slaughterCountLabel;
	private final JLabel expeditiousCountLabel;
	private final JLabel cannonBreakCountLabel;
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
		contentPanel.add(Box.createVerticalStrut(15));

		// Bracelet tracking section
		JLabel braceletSectionLabel = new JLabel("Bracelet Usage:");
		braceletSectionLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
		braceletSectionLabel.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 14));
		contentPanel.add(braceletSectionLabel);

		contentPanel.add(Box.createVerticalStrut(10));

		// Slaughter bracelet count
		JLabel slaughterLabelText = new JLabel("Bracelet of Slaughter:");
		slaughterLabelText.setAlignmentX(Component.CENTER_ALIGNMENT);
		contentPanel.add(slaughterLabelText);

		slaughterCountLabel = new JLabel("0");
		slaughterCountLabel.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 16));
		slaughterCountLabel.setForeground(Color.ORANGE);
		slaughterCountLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
		contentPanel.add(slaughterCountLabel);

		contentPanel.add(Box.createVerticalStrut(10));

		// Expeditious bracelet count
		JLabel expeditiousLabelText = new JLabel("Expeditious Bracelet:");
		expeditiousLabelText.setAlignmentX(Component.CENTER_ALIGNMENT);
		contentPanel.add(expeditiousLabelText);

		expeditiousCountLabel = new JLabel("0");
		expeditiousCountLabel.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 16));
		expeditiousCountLabel.setForeground(Color.CYAN);
		expeditiousCountLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
		contentPanel.add(expeditiousCountLabel);

		contentPanel.add(Box.createVerticalStrut(15));

		// Cannon break count
		JLabel cannonLabelText = new JLabel("Cannon Breaks:");
		cannonLabelText.setAlignmentX(Component.CENTER_ALIGNMENT);
		contentPanel.add(cannonLabelText);

		cannonBreakCountLabel = new JLabel("0");
		cannonBreakCountLabel.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 16));
		cannonBreakCountLabel.setForeground(Color.RED);
		cannonBreakCountLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
		contentPanel.add(cannonBreakCountLabel);

		// Add some spacing
		contentPanel.add(Box.createVerticalStrut(20));

		// Refresh button
		refreshButton = new JButton("Refresh");
		refreshButton.setAlignmentX(Component.CENTER_ALIGNMENT);
		refreshButton.addActionListener(e -> updateAllCounts());
		contentPanel.add(refreshButton);

		add(contentPanel, BorderLayout.CENTER);

		// Initialize with current counts
		updateAllCounts();
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

	public void updateBraceletCounts()
	{
		try {
			int slaughterCount = plugin.getSlaughterCount();
			int expeditiousCount = plugin.getExpeditiousCount();
			
			slaughterCountLabel.setText(String.valueOf(slaughterCount));
			expeditiousCountLabel.setText(String.valueOf(expeditiousCount));
			
			log.debug("Updated panel bracelet counts - Slaughter: {}, Expeditious: {}", 
				slaughterCount, expeditiousCount);
		} catch (Exception e) {
			log.error("Error updating bracelet counts in panel", e);
			slaughterCountLabel.setText("Error");
			expeditiousCountLabel.setText("Error");
		}
	}

	public void updateCannonCount()
	{
		try {
			int cannonBreaks = plugin.getCannonBreakCount();
			cannonBreakCountLabel.setText(String.valueOf(cannonBreaks));
			log.debug("Updated panel cannon break count to: {}", cannonBreaks);
		} catch (Exception e) {
			log.error("Error updating cannon break count in panel", e);
			cannonBreakCountLabel.setText("Error");
		}
	}

	public void updateAllCounts()
	{
		updateTaskCount();
		updateBraceletCounts();
		updateCannonCount();
	}
}
