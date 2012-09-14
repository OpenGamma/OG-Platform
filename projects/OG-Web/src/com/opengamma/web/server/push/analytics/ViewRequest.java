/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.server.push.analytics;

import java.util.List;

import javax.time.InstantProvider;

import com.google.common.collect.ImmutableList;
import com.opengamma.engine.marketdata.spec.MarketDataSpecification;
import com.opengamma.engine.view.execution.ExecutionFlags;
import com.opengamma.engine.view.execution.ExecutionOptions;
import com.opengamma.engine.view.execution.InfiniteViewCycleExecutionSequence;
import com.opengamma.engine.view.execution.ViewCycleExecutionOptions;
import com.opengamma.engine.view.execution.ViewExecutionOptions;
import com.opengamma.id.UniqueId;
import com.opengamma.id.VersionCorrection;
import com.opengamma.util.ArgumentChecker;

/**
 * Contains all the parameters a client needs to provide to the server to create a view for calculating portfolio
 * analytics.
 */
public class ViewRequest {

  /** The ID if the view definition used by the view. */
  private final UniqueId _viewDefinitionId;
  /** Used for aggregating the view's portfolio. */
  private final List<String> _aggregators;
  /** Execution options for the view. */
  private final ViewExecutionOptions _executionOptions;

  /**
   *
   * @param viewDefinitionId The ID if the view definition used by the view, not null
   * @param aggregators Used for aggregating the view's portfolio, not null
   * @param marketDataSpecs The source(s) of market data for the view, not empty
   * @param valuationTime The valuation time used when calculating the analytics, can be null
   * @param portfolioVersionCorrection Version and correction time for the portfolio used when calculating the analytics
   */
  public ViewRequest(UniqueId viewDefinitionId,
                     List<String> aggregators,
                     List<MarketDataSpecification> marketDataSpecs,
                     InstantProvider valuationTime,
                     VersionCorrection portfolioVersionCorrection) {
    ArgumentChecker.notNull(viewDefinitionId, "viewDefinitionId");
    ArgumentChecker.notNull(aggregators, "aggregators");
    ArgumentChecker.notEmpty(marketDataSpecs, "marketDataSpecs");
    ArgumentChecker.notNull(portfolioVersionCorrection, "portfolioVersionCorrection");
    _viewDefinitionId = viewDefinitionId;
    _aggregators = ImmutableList.copyOf(aggregators);
    ViewCycleExecutionOptions defaultOptions = new ViewCycleExecutionOptions(valuationTime, marketDataSpecs);
    _executionOptions = ExecutionOptions.of(new InfiniteViewCycleExecutionSequence(),
                                            defaultOptions,
                                            // this recalcs periodically or when market data changes. might need to give
                                            // the user the option to specify the behaviour
                                            ExecutionFlags.triggersEnabled().get(),
                                            portfolioVersionCorrection);
  }

  public UniqueId getViewDefinitionId() {
    return _viewDefinitionId;
  }

  public List<String> getAggregators() {
    return _aggregators;
  }

  public ViewExecutionOptions getExecutionOptions() {
    return _executionOptions;
  }
}
