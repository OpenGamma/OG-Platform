/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.viewer;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;

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
    JSlider slider = new JSlider(JSlider.HORIZONTAL, 0, 4000, 1000);
    slider.addChangeListener(new ChangeListener() {
      @Override
      public void stateChanged(ChangeEvent e) {
        JSlider source = (JSlider)e.getSource();
        if (!source.getValueIsAdjusting()) {
          ViewRecalculationJob.setDelay(source.getValue());
        }
      }
    });
    panel.add(new JLabel("Delay"));
    panel.add(slider);
    //panel.setPreferredSize(new Dimension(Short.MAX_VALUE, 20));
    return panel;
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
