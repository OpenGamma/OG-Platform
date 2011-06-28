/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.view.execution;

import javax.time.Instant;
import javax.time.InstantProvider;

import com.opengamma.engine.marketdata.spec.MarketDataSpecification;
import com.opengamma.util.PublicAPI;

/**
 * Encapsulates specific settings affecting the execution of an individual view cycle.
 */
@PublicAPI
public class ViewCycleExecutionOptions {

  private Instant _valuationTime;
  private MarketDataSpecification _marketDataSpecification;
  
  // TODO [PLAT-1153] view correction time - probably want either valuation time or some fixed correction time
  
  /**
   * Constructs an empty instance.
   */
  public ViewCycleExecutionOptions() {
  }
  
  /**
   * Constructs an instance, specifying a valuation time.
   * 
   * @param valuationTimeProvider  the valuation time provider, may be {@code null}
   */
  public ViewCycleExecutionOptions(InstantProvider valuationTimeProvider) {
    setValuationTime(valuationTimeProvider.toInstant());
  }
  
  /**
   * Constructs an instance, specifying a market data specification.
   * 
   * @param marketDataSpec  the market data specification, may be {@code null}
   */
  public ViewCycleExecutionOptions(MarketDataSpecification marketDataSpec) {
    setMarketDataSpecification(marketDataSpec);
  }
  
  /**
   * Constructs an instance.
   * 
   * @param valuationTimeProvider  the valuation time provider, may be {@code null}
   * @param marketDataSpec  the market data specification, may be {@code null}
   */
  public ViewCycleExecutionOptions(InstantProvider valuationTimeProvider, MarketDataSpecification marketDataSpec) {
    setValuationTime(valuationTimeProvider);
    setMarketDataSpecification(marketDataSpec);
  }

  /**
   * Gets the valuation time. Normally the valuation time is the timestamp associated with the market data snapshot,
   * but this valuation time will be used instead if specified. 
   * 
   * @return the valuation time, or {@code null} if not specified
   */
  public Instant getValuationTime() {
    return _valuationTime;
  }
  
  /**
   * Sets the valuation time. Normally the valuation time is the timestamp associated with the market data snapshot,
   * but this valuation time will be used instead if specified.
   * 
   * @param valuationTimeProvider  the valuation time provider, may be {@code null}
   */
  public void setValuationTime(InstantProvider valuationTimeProvider) {
    _valuationTime = valuationTimeProvider != null ? valuationTimeProvider.toInstant() : null;
  }
  
  /**
   * Gets the market data specification.
   * 
   * @return the market data specification, or {@code null} if not specified
   */
  public MarketDataSpecification getMarketDataSpecification() {
    return _marketDataSpecification;
  }
  
  /**
   * Sets the market data specification.
   * 
   * @param marketDataSpec  the market data specification, may be {@code null}
   */
  public void setMarketDataSpecification(MarketDataSpecification marketDataSpec) {
    _marketDataSpecification = marketDataSpec;
  }

  @Override
  public String toString() {
    return "ViewCycleExecutionOptions[valuationTime=" + getValuationTime() + ", marketDataSpecification=" + getMarketDataSpecification() + "]";
  }
  
}
