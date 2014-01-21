/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.analytics;

import java.util.List;

import org.threeten.bp.Instant;

import com.google.common.collect.ImmutableList;
import com.opengamma.engine.marketdata.spec.MarketDataSpecification;
import com.opengamma.id.UniqueId;
import com.opengamma.id.VersionCorrection;
import com.opengamma.util.ArgumentChecker;

/**
 * Contains all the parameters a client needs to provide to the server to create a view for calculating portfolio
 * analytics.
 */
public class ViewRequest {

  /** The unique identifier of the view definition. */
  private final UniqueId _viewDefinitionId;
  /** The unique identifier of an existing view process to attach to. */
  private final UniqueId _viewProcessId;
  /** Used for aggregating the view's portfolio. */
  private final List<String> _aggregators;
  /** Valuation time used by the calculation engine. */
  private final Instant _valuationTime;
  /** Sources of market data used in the calculation in priority order. */
  private final List<MarketDataSpecification> _marketDataSpecs;
  /** Version time and correction time for the portfolio used as a basis for the calculations. */
  private final VersionCorrection _portfolioVersionCorrection;
  /** Whether to display blotter columns in the portfolio view showing a summary of the security details. */
  private final boolean _blotter;

  /**
   *
   * @param viewDefinitionId The unqiue identifier of the view definition, not null
   * @param viewProcessId  the unique identifier of an existing view process to connect to, null to use the default
   * @param aggregators Used for aggregating the view's portfolio, not null
   * @param marketDataSpecs The source(s) of market data for the view, not empty
   * @param valuationTime The valuation time used when calculating the analytics, can be null
   * @param blotter Whether to show blotter columns containing security and trade data in the portfolio grid
   * @param portfolioVersionCorrection Version and correction time for the portfolio used when calculating the analytics
   */
  public ViewRequest(UniqueId viewDefinitionId,
                     UniqueId viewProcessId,
                     List<String> aggregators,
                     List<MarketDataSpecification> marketDataSpecs,
                     Instant valuationTime,
                     VersionCorrection portfolioVersionCorrection,
                     boolean blotter) {
    ArgumentChecker.notNull(viewDefinitionId, "viewDefinitionId");
    ArgumentChecker.notNull(aggregators, "aggregators");
    ArgumentChecker.notEmpty(marketDataSpecs, "marketDataSpecs");
    ArgumentChecker.notNull(portfolioVersionCorrection, "portfolioVersionCorrection");
    _marketDataSpecs = marketDataSpecs;
    _valuationTime = valuationTime;
    _viewDefinitionId = viewDefinitionId;
    _viewProcessId = viewProcessId;
    _aggregators = ImmutableList.copyOf(aggregators);
    _portfolioVersionCorrection = portfolioVersionCorrection;
    _blotter = blotter;
  }

  /**
   * @return the unique identifier of the view definition, not null
   */
  public UniqueId getViewDefinitionId() {
    return _viewDefinitionId;
  }
  
  /**
   * @return the unique identifier of an existing view process to attach to, null to use the default
   */
  public UniqueId getViewProcessId() {
    return _viewProcessId;
  }

  /**
   * @return Used for aggregating the view's portfolio, not null but can be empty
   */
  public List<String> getAggregators() {
    return _aggregators;
  }

  /**
   * @return Valuation time used by the calculation engine, can be null to use the default
   */
  public Instant getValuationTime() {
    return _valuationTime;
  }

  /**
   * @return Sources of market data used in the calculation in priority order
   */
  public List<MarketDataSpecification> getMarketDataSpecs() {
    return _marketDataSpecs;
  }

  /**
   * @return Version time and correction time for the portfolio used as a basis for the calculations
   */
  public VersionCorrection getPortfolioVersionCorrection() {
    return _portfolioVersionCorrection;
  }

  /**
   * @return Whether to show blotter columns in the portfolio view which show a summary of the security details.
   */
  public boolean showBlotterColumns() {
    return _blotter;
  }
}
