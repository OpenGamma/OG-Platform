/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.production.viewer;

import java.awt.BorderLayout;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;

import org.jdesktop.application.SingleFrameApplication;
import org.jdesktop.swingx.JXTreeTable;

import com.opengamma.engine.marketdata.spec.MarketData;
import com.opengamma.engine.view.ViewProcessor;
import com.opengamma.engine.view.client.ViewClient;
import com.opengamma.engine.view.execution.ExecutionOptions;
import com.opengamma.livedata.UserPrincipal;

/**
 * Common functionality for the thick client demo shared by the LocalEngineViewerLauncher and
 * RemoteEngineViewerLauncher which create a local engine or connect to a shared demo server
 * respectively.
 */
public abstract class AbstractEngineViewerLauncher extends SingleFrameApplication {

  protected PortfolioTreeTableModel buildTreeTableModel() {
    PortfolioTreeTableModel treeTableModel = new PortfolioTreeTableModel();
    return treeTableModel;
  }

  protected void startViewer(final ViewProcessor viewProcessor) {
    UserPrincipal user = UserPrincipal.getLocalUser();
    ViewClient viewClient = viewProcessor.createViewClient(user);
    PortfolioTreeTableModel treeTableModel = buildTreeTableModel();
    viewClient.setResultListener(treeTableModel);
    viewClient.attachToViewProcess(viewProcessor.getViewDefinitionRepository().getDefinition("Equity Option Test View 1").getUniqueId(), ExecutionOptions.infinite(MarketData.live()));

    getMainFrame().setTitle("OpenGamma Viewer");

    JXTreeTable treeTable = new JXTreeTable(treeTableModel);
    treeTable.setName("table");
    treeTable.setRootVisible(true);
    treeTable.setFillsViewportHeight(true);
    treeTable.setAutoResizeMode(JXTreeTable.AUTO_RESIZE_ALL_COLUMNS);
    treeTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

    JScrollPane scrollPane = new JScrollPane(treeTable);
    JPanel panel = new JPanel(new BorderLayout());
    panel.add(scrollPane, BorderLayout.CENTER);

    JPanel topLevelPanel = new JPanel(new BorderLayout());
    topLevelPanel.add(panel, BorderLayout.CENTER);
    show(topLevelPanel);
  }

}
