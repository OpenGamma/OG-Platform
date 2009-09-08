package com.opengamma.demo.timeseries;

import java.awt.Container;
import java.awt.Dimension;
import java.awt.Toolkit;

import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JTabbedPane;
import javax.swing.SwingUtilities;

public class TimeSeriesDemo {

  static void createGUI() {
    JFrame frame = new JFrame("Time Series Demo");
    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    TimeSeriesDemoAnalysisPanel tsAnalysisPanel = new TimeSeriesDemoAnalysisPanel();
    JComponent tsSelectionPanel = new TimeSeriesDemoSelectionPanel(frame, tsAnalysisPanel);
    Container contentPane = frame.getContentPane();
    JTabbedPane tabbed = new JTabbedPane();
    tabbed.add("Selection", tsSelectionPanel);
    tabbed.add("Analysis", tsAnalysisPanel);
    contentPane.add(tabbed);
    frame.pack();
    Toolkit toolkit = frame.getToolkit();
    Dimension size = toolkit.getScreenSize();
    frame.setLocation(size.width / 2 - frame.getWidth() / 2, size.height / 2 - frame.getHeight() / 2);
    frame.setVisible(true);
  }

  public static void main(String[] args) {
    SwingUtilities.invokeLater(new Runnable() {
      public void run() {
        createGUI();
      }
    });
  }
}
