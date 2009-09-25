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
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.table.TableModel;

import org.jdesktop.application.SingleFrameApplication;
import org.jdesktop.swingx.JXTable;

import com.opengamma.engine.view.ViewImpl;
import com.opengamma.util.Pair;

/**
 * 
 *
 * @author jim
 */
public class ViewerLauncher extends SingleFrameApplication {
 
  private ViewManager _viewManager;
  
  private Pair<JPanel, JXTable> buildLeftTable(JXTable parentTable) {
    PortfolioSelectionListenerAndTableModel listenerAndTableModel = new PortfolioSelectionListenerAndTableModel(parentTable);
    JXTable table = new JXTable(listenerAndTableModel);
    table.setName("positionTable");
    table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    JScrollPane scrollPane = new JScrollPane(table);
    JPanel panel = new JPanel(new BorderLayout());
    panel.add(scrollPane, BorderLayout.CENTER);
    return new Pair<JPanel, JXTable>(panel, table);
  }
  
  private JPanel buildRightTable(JXTable parentTable) {
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
    _viewManager = new ViewManager();
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
    Pair<JPanel, JXTable> buildLeftTable = buildLeftTable(table);
    JPanel leftPanel = buildLeftTable.getFirst();
    JXTable leftTable = buildLeftTable.getSecond();
    
    bottomPane.add(leftPanel);
    bottomPane.add(buildRightTable(leftTable));
    
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
