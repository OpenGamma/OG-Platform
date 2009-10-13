/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.viewer;

import java.awt.BorderLayout;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTree;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
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
  
  private Pair<JTabbedPane, JXTable> buildLeftPane(JXTable parentTable) {
    // build the 'results' table.
    PortfolioSelectionListenerAndTableModel listenerAndTableModel = new PortfolioSelectionListenerAndTableModel(parentTable);
    JXTable table = new JXTable(listenerAndTableModel);
    table.setName("positionTable");
    table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    JScrollPane scrollPane = new JScrollPane(table);
    JPanel panel = new JPanel(new BorderLayout());
    panel.add(scrollPane, BorderLayout.CENTER);
    // build the 'dep graph' tree table.
    PortfolioSelectionListenerAndDepGraphTreeTableModel listenerAndDepGraphTreeTableModel = new PortfolioSelectionListenerAndDepGraphTreeTableModel(parentTable);
    JXTreeTable treeTable = new JXTreeTable(listenerAndDepGraphTreeTableModel);
    //JXTree treeTable = new JXTree(listenerAndDepGraphTreeTableModel);
    //treeTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    treeTable.setShowsRootHandles(true);
    JScrollPane scrollPane2 = new JScrollPane(treeTable);
    JPanel panel2 = new JPanel(new BorderLayout());
    panel2.add(scrollPane2, BorderLayout.CENTER);
    // build the tabbed pane
    JTabbedPane tabbedPane = new JTabbedPane();
    tabbedPane.add("Results", panel);
    tabbedPane.add("Dependency Graph", panel2);
    return new Pair<JTabbedPane, JXTable>(tabbedPane, table);
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
    Pair<JTabbedPane, JXTable> buildLeftTable = buildLeftPane(table);
    JTabbedPane leftPanel = buildLeftTable.getFirst();
    JXTable leftTable = buildLeftTable.getSecond();
    
    bottomPane.add(leftPanel);
    bottomPane.add(buildRightPane(leftTable));
    
    splitPane.add(bottomPane);
    
    panel.add(splitPane, BorderLayout.CENTER);
    show(panel);
  }
  
  protected void shutdown() {
    _viewManager.stop();
  }
  
  public static void main(String[] args) {
    launch(ViewerLauncher.class, args);
  }

}
