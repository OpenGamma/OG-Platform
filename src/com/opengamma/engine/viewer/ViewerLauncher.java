/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.viewer;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Polygon;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSlider;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTree;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.table.TableModel;

import org.jdesktop.application.SingleFrameApplication;
import org.jdesktop.swingx.JXTable;
import org.jdesktop.swingx.JXTree;
import org.jdesktop.swingx.JXTreeTable;

import com.opengamma.engine.livedata.FixedLiveDataAvailabilityProvider;
import com.opengamma.engine.livedata.InMemoryLKVSnapshotProvider;
import com.opengamma.engine.livedata.LiveDataAvailabilityProvider;
import com.opengamma.engine.livedata.LiveDataSnapshotProvider;
import com.opengamma.engine.view.ViewImpl;
import com.opengamma.engine.view.ViewRecalculationJob;
import com.opengamma.util.Pair;

/**
 * 
 *
 * @author jim
 */
public class ViewerLauncher extends SingleFrameApplication {
  private ViewManager _viewManager;
  private LiveDataAvailabilityProvider _liveDataAvailabilityProvider = new FixedLiveDataAvailabilityProvider();
  private LiveDataSnapshotProvider _liveDataSnapshotProvider = new InMemoryLKVSnapshotProvider();
  
  protected LiveDataAvailabilityProvider getLiveDataAvailabilityProvider() {
    return _liveDataAvailabilityProvider;
  }
  
  protected LiveDataSnapshotProvider getLiveDataSnapshotProvider() {
    return _liveDataSnapshotProvider;
  }
  
  private Pair<JPanel, JXTable> buildLeftPane(JXTable parentTable) {
    // build the 'results' table.
    PortfolioSelectionListenerAndTableModel listenerAndTableModel = new PortfolioSelectionListenerAndTableModel(parentTable);
    JXTable table = new JXTable(listenerAndTableModel);
    //table.setName("positionTable");
    table.getColumnExt(0).setMinWidth(150);
    table.getColumnExt(1).setMinWidth(300);
    table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    JScrollPane scrollPane = new JScrollPane(table);
    JPanel panel = new JPanel(new BorderLayout());
    panel.add(scrollPane, BorderLayout.CENTER);
    // build the 'dep graph' tree table.

    // build the tabbed pane

    return new Pair<JPanel, JXTable>(panel, table);
  }
  
  private JPanel buildDepGraphPanel(JXTable parentTable) {
    PortfolioSelectionListenerAndDepGraphTreeTableModel listenerAndDepGraphTreeTableModel = new PortfolioSelectionListenerAndDepGraphTreeTableModel(parentTable);
    JXTreeTable treeTable = new JXTreeTable(listenerAndDepGraphTreeTableModel);
    //JXTree treeTable = new JXTree(listenerAndDepGraphTreeTableModel);
    //treeTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    treeTable.setShowsRootHandles(true);
    JScrollPane scrollPane2 = new JScrollPane(treeTable);
    JPanel panel2 = new JPanel(new BorderLayout());
    panel2.add(scrollPane2, BorderLayout.CENTER);
    
    return panel2;
  }
  
  private JPanel buildRightPane(JXTable parentTable) {
    ValueSelectionListenerPanel valueSelectionListenerPanel = new ValueSelectionListenerPanel(parentTable);
    JPanel panel = new JPanel(new BorderLayout());
    panel.add(valueSelectionListenerPanel);
    return panel;
  }
  
  private TableModel buildTableModel(ViewImpl view) {
    PortfolioTableModel tableModel = new PortfolioTableModel();
    view.addResultListener(tableModel);
    return tableModel; 
  }
  
  public JComponent createSlider() {
    JPanel panel = new JPanel(new FlowLayout());
    final JSlider slider = new JSlider(JSlider.HORIZONTAL, 0, 4000, 1000);
    slider.addChangeListener(new ChangeListener() {
      @Override
      public void stateChanged(ChangeEvent e) {
        JSlider source = (JSlider)e.getSource();
        if (!source.getValueIsAdjusting()) {
          ViewRecalculationJob.setDelay(source.getValue());
        }
      }
    });
    ViewRecalculationJob.setDelay(1000);
    ViewRecalculationJob.pause(false);
    final Icon play = createPlayIcon();
    final Icon pause = createPauseIcon();
    final JButton pauseButton = new JButton(pause);
    pauseButton.addActionListener(new ActionListener() {
      public boolean _paused = true;
      @Override
      public void actionPerformed(ActionEvent e) {
        JButton button = (JButton) e.getSource();
        if (_paused) {
          button.setIcon(play);
        } else {
          button.setIcon(pause);
        }
        slider.setEnabled(!_paused);
        ViewRecalculationJob.pause(_paused);
        _paused = !_paused;
      }
    });
    panel.add(new JLabel("Delay"));
    panel.add(slider);
    panel.add(pauseButton);
    return panel;
  }
  
  private Icon createPauseIcon() {
    Image image = new BufferedImage(24, 24, BufferedImage.TYPE_INT_ARGB );
    Graphics graphics = image.getGraphics();
    graphics.setColor(Color.BLACK);
    graphics.fillRect(8-2, 4, 4, 16);
    graphics.fillRect(16-2, 4, 4, 16);
    Icon icon = new ImageIcon(image);
    return icon;
  }

  private Icon createPlayIcon() {
    Image image = new BufferedImage(24, 24, BufferedImage.TYPE_INT_ARGB );
    Graphics graphics = image.getGraphics();
    graphics.setColor(Color.BLACK);
    graphics.fillPolygon(new Polygon(new int[] {7,7,20}, new int[] {4,20,12}, 3));
    Icon icon = new ImageIcon(image);
    return icon;
  }
  
  @Override
  protected void startup() {
    _viewManager = new ViewManager(getLiveDataAvailabilityProvider(), getLiveDataSnapshotProvider());
    TableModel tableModel = buildTableModel(_viewManager.getView());
    _viewManager.start();
    JXTable table = new JXTable(tableModel);
    table.setName("table");
    table.setShowGrid(true);
    table.setFillsViewportHeight(true);
    table.setAutoResizeMode(JXTable.AUTO_RESIZE_ALL_COLUMNS);
    table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    table.setSortable(true);
    
    JScrollPane scrollPane = new JScrollPane(table);
    JPanel panel = new JPanel(new BorderLayout());
   
    JSplitPane splitPane = new JSplitPane(SwingConstants.HORIZONTAL);
    splitPane.add(scrollPane);
    
    JSplitPane bottomPane = new JSplitPane(SwingConstants.VERTICAL);
    bottomPane.setDividerLocation(0.9d);
    Pair<JPanel, JXTable> buildLeftTable = buildLeftPane(table);
    JPanel leftPanel = buildLeftTable.getFirst();
    JXTable leftTable = buildLeftTable.getSecond();
    bottomPane.add(leftPanel);
    bottomPane.add(buildRightPane(leftTable));
    
    JPanel depGraphPanel = buildDepGraphPanel(table);
    JTabbedPane tabbedPane = new JTabbedPane();
    tabbedPane.add("Results", bottomPane);
    tabbedPane.add("Dependency Graph", depGraphPanel);
    
    splitPane.add(tabbedPane);
    
    panel.add(splitPane, BorderLayout.CENTER);
    
    JPanel topLevelPanel = new JPanel(new BorderLayout());
    topLevelPanel.add(createSlider(), BorderLayout.NORTH);
    topLevelPanel.add(panel, BorderLayout.CENTER);
    show(topLevelPanel);
  }
  
  protected void shutdown() {
    _viewManager.stop();
  }
  
  public static void main(String[] args) {
    launch(ViewerLauncher.class, args);
  }

}
