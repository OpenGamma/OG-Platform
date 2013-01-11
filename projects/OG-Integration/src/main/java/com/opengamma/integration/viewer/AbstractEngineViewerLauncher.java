/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.integration.viewer;

import java.awt.BorderLayout;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;

import org.jdesktop.application.SingleFrameApplication;
import org.jdesktop.swingx.JXTreeTable;

import com.opengamma.engine.marketdata.spec.MarketData;
import com.opengamma.engine.view.ViewDefinition;
import com.opengamma.engine.view.ViewProcessor;
import com.opengamma.engine.view.client.ViewClient;
import com.opengamma.engine.view.execution.ExecutionOptions;
import com.opengamma.id.VersionCorrection;
import com.opengamma.livedata.UserPrincipal;

/**
 * Common functionality for the thick client demo shared by the LocalEngineViewerLauncher and
 * RemoteEngineViewerLauncher which create a local engine or connect to a shared demo server
 * respectively.
 */
public abstract class AbstractEngineViewerLauncher extends SingleFrameApplication {

  protected PortfolioTreeTableModel buildTreeTableModel() {
    final PortfolioTreeTableModel treeTableModel = new PortfolioTreeTableModel();
    return treeTableModel;
  }

  protected void startViewer(final ViewProcessor viewProcessor) {
    final UserPrincipal user = UserPrincipal.getLocalUser();
    final ViewClient viewClient = viewProcessor.createViewClient(user);
    final PortfolioTreeTableModel treeTableModel = buildTreeTableModel();
    viewClient.setResultListener(treeTableModel);
    viewClient.attachToViewProcess(viewProcessor.getConfigSource().getSingle(ViewDefinition.class, "Equity Portfolio View", VersionCorrection.LATEST).getUniqueId(),
        ExecutionOptions.infinite(MarketData.live()));

    getMainFrame().setTitle("OpenGamma Viewer");

    final JXTreeTable treeTable = new JXTreeTable(treeTableModel);
    treeTable.setName("table");
    treeTable.setRootVisible(true);
    treeTable.setFillsViewportHeight(true);
    treeTable.setAutoResizeMode(JXTreeTable.AUTO_RESIZE_ALL_COLUMNS);
    treeTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

    final JScrollPane scrollPane = new JScrollPane(treeTable);
    final JPanel panel = new JPanel(new BorderLayout());
    panel.add(scrollPane, BorderLayout.CENTER);

    final JPanel topLevelPanel = new JPanel(new BorderLayout());
    topLevelPanel.add(panel, BorderLayout.CENTER);
    show(topLevelPanel);
  }

}
