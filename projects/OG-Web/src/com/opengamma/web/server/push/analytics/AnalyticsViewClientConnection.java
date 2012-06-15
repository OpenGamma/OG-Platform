/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.server.push.analytics;

import com.opengamma.engine.marketdata.NamedMarketDataSpecificationRepository;
import com.opengamma.engine.marketdata.spec.MarketDataSpecification;
import com.opengamma.engine.view.ViewComputationResultModel;
import com.opengamma.engine.view.ViewDeltaResultModel;
import com.opengamma.engine.view.client.ViewClient;
import com.opengamma.engine.view.compilation.CompiledViewDefinition;
import com.opengamma.engine.view.execution.ExecutionOptions;
import com.opengamma.engine.view.listener.AbstractViewResultListener;
import com.opengamma.livedata.UserPrincipal;
import com.opengamma.util.ArgumentChecker;

/**
 *
 */
/* package */ class AnalyticsViewClientConnection extends AbstractViewResultListener {

  private final AnalyticsView _view;
  private final ViewClient _viewClient;
  private final NamedMarketDataSpecificationRepository _namedMarketDataSpecRepo;

  private boolean _running = false;
  private final ViewRequest _viewRequest;

  public AnalyticsViewClientConnection(ViewRequest viewRequest,
                                       ViewClient viewClient,
                                       AnalyticsView view,
                                       NamedMarketDataSpecificationRepository namedMarketDataSpecRepo) {
    ArgumentChecker.notNull(viewRequest, "viewRequest");
    ArgumentChecker.notNull(viewClient, "viewClient");
    ArgumentChecker.notNull(view, "view");
    ArgumentChecker.notNull(namedMarketDataSpecRepo, "namedMarketDataSpecRepo");
    _viewRequest = viewRequest;
    _view = view;
    _viewClient = viewClient;
    _namedMarketDataSpecRepo = namedMarketDataSpecRepo;
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
    // TODO process the request and set up the execution options property
    _viewClient.setResultListener(this);
    String liveMarketDataProvider = "Live market data (Bloomberg, Activ)";
    MarketDataSpecification marketDataSpec = _namedMarketDataSpecRepo.getSpecification(liveMarketDataProvider);
    _viewClient.attachToViewProcess(_viewRequest.getViewDefinitionId(), ExecutionOptions.infinite(marketDataSpec));
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
