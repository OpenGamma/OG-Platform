/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.view.execution;

import javax.time.Instant;
import javax.time.InstantProvider;

import com.opengamma.engine.marketdata.spec.MarketDataSnapshotSpecification;

/**
 * Encapsulates specific settings affecting the execution of an individual view cycle.
 */
public class ViewCycleExecutionOptions {

  private Instant _valuationTime;
  private MarketDataSnapshotSpecification _marketDataSnapshotSpecification;
  
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
   * Constructs an instance, specifying a market data snapshot specification.
   * 
   * @param snapshotSpec  the market data snapshot specification, may be {@code null}
   */
  public ViewCycleExecutionOptions(MarketDataSnapshotSpecification snapshotSpec) {
    setMarketDataSnapshotSpecification(snapshotSpec);
  }
  
  /**
   * Constructs an instance.
   * 
   * @param valuationTimeProvider  the valuation time provider, may be {@code null}
   * @param snapshotSpec  the market data snapshot specification, may be {@code null}
   */
  public ViewCycleExecutionOptions(InstantProvider valuationTimeProvider, MarketDataSnapshotSpecification snapshotSpec) {
    setValuationTime(valuationTimeProvider);
    setMarketDataSnapshotSpecification(snapshotSpec);
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
   * Gets the market data snapshot specification.
   * 
   * @return the market data snapshot specification, or {@code null} if not specified
   */
  public MarketDataSnapshotSpecification getMarketDataSnapshotSpecification() {
    return _marketDataSnapshotSpecification;
  }
  
  /**
   * Sets the market data snapshot specification.
   * 
   * @param marketDataSnapshotSpecification  the market data snapshot specification, may be {@code null}
   */
  public void setMarketDataSnapshotSpecification(MarketDataSnapshotSpecification marketDataSnapshotSpecification) {
    _marketDataSnapshotSpecification = marketDataSnapshotSpecification;
  }

  @Override
  public String toString() {
    return "ViewCycleExecutionOptions[valuationTime=" + _valuationTime + ", marketDataSnapshotSpecification=" + _marketDataSnapshotSpecification + "]";
  }
  
}
