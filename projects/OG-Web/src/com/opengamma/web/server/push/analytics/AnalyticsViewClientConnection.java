/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.server.push.analytics;

import com.opengamma.engine.view.ViewComputationResultModel;
import com.opengamma.engine.view.ViewDeltaResultModel;
import com.opengamma.engine.view.client.ViewClient;
import com.opengamma.engine.view.compilation.CompiledViewDefinition;
import com.opengamma.engine.view.listener.AbstractViewResultListener;
import com.opengamma.livedata.UserPrincipal;
import com.opengamma.util.ArgumentChecker;

/**
 *
 */
/* package */ class AnalyticsViewClientConnection extends AbstractViewResultListener {

  private final AnalyticsView _view;
  private final ViewClient _viewClient;

  private boolean _running = false;

  public AnalyticsViewClientConnection(ViewRequest viewRequest, ViewClient viewClient, AnalyticsView view) {
    ArgumentChecker.notNull(viewRequest, "viewRequest");
    ArgumentChecker.notNull(viewClient, "viewClient");
    ArgumentChecker.notNull(view, "view");
    _view = view;
    _viewClient = viewClient;
  }

  @Override
  public UserPrincipal getUser() {
    return _viewClient.getUser();
  }

  @Override
  public void viewDefinitionCompiled(CompiledViewDefinition compiledViewDefinition, boolean hasMarketDataPermissions) {
    _view.updateStructure(compiledViewDefinition);
  }

  @Override
  public void cycleCompleted(ViewComputationResultModel fullResult, ViewDeltaResultModel deltaResult) {
    _view.updateResults(fullResult);
  }

  /* package */ void start() {
    if (_running) {
      throw new IllegalStateException("Already running");
    }
    // TODO set up execution options and attach client to view process
    _viewClient.setResultListener(this);
    _running = true;
  }

  /* package */ void stop() {
    if (!_running) {
      throw new IllegalStateException("Already stopped");
    }
    _viewClient.detachFromViewProcess();
    _running = false;
  }

  public AnalyticsView getView() {
    return _view;
  }
}
