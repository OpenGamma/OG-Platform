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
   * @param valuationTimeProvider  the valuation time provider, may be null
   */
  public ViewCycleExecutionOptions(InstantProvider valuationTimeProvider) {
    setValuationTime(valuationTimeProvider.toInstant());
  }
  
  /**
   * Constructs an instance, specifying a market data specification.
   * 
   * @param marketDataSpec  the market data specification, may be null
   */
  public ViewCycleExecutionOptions(MarketDataSpecification marketDataSpec) {
    setMarketDataSpecification(marketDataSpec);
  }
  
  /**
   * Constructs an instance.
   * 
   * @param valuationTimeProvider  the valuation time provider, may be null
   * @param marketDataSpec  the market data specification, may be null
   */
  public ViewCycleExecutionOptions(InstantProvider valuationTimeProvider, MarketDataSpecification marketDataSpec) {
    setValuationTime(valuationTimeProvider);
    setMarketDataSpecification(marketDataSpec);
  }

  /**
   * Gets the valuation time. Normally the valuation time is the timestamp associated with the market data snapshot,
   * but this valuation time will be used instead if specified. 
   * 
   * @return the valuation time, or null if not specified
   */
  public Instant getValuationTime() {
    return _valuationTime;
  }
  
  /**
   * Sets the valuation time. Normally the valuation time is the timestamp associated with the market data snapshot,
   * but this valuation time will be used instead if specified.
   * 
   * @param valuationTimeProvider  the valuation time provider, may be null
   */
  public void setValuationTime(InstantProvider valuationTimeProvider) {
    _valuationTime = valuationTimeProvider != null ? valuationTimeProvider.toInstant() : null;
  }
  
  /**
   * Gets the market data specification.
   * 
   * @return the market data specification, or null if not specified
   */
  public MarketDataSpecification getMarketDataSpecification() {
    return _marketDataSpecification;
  }
  
  /**
   * Sets the market data specification.
   * 
   * @param marketDataSpec  the market data specification, may be null
   */
  public void setMarketDataSpecification(MarketDataSpecification marketDataSpec) {
    _marketDataSpecification = marketDataSpec;
  }

  @Override
  public String toString() {
    return "ViewCycleExecutionOptions[valuationTime=" + getValuationTime() + ", marketDataSpecification=" + getMarketDataSpecification() + "]";
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((_marketDataSpecification == null) ? 0 : _marketDataSpecification.hashCode());
    result = prime * result + ((_valuationTime == null) ? 0 : _valuationTime.hashCode());
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (!(obj instanceof ViewCycleExecutionOptions)) {
      return false;
    }
    ViewCycleExecutionOptions other = (ViewCycleExecutionOptions) obj;
    if (_marketDataSpecification == null) {
      if (other._marketDataSpecification != null) {
        return false;
      }
    } else if (!_marketDataSpecification.equals(other._marketDataSpecification)) {
      return false;
    }
    if (_valuationTime == null) {
      if (other._valuationTime != null) {
        return false;
      }
    } else if (!_valuationTime.equals(other._valuationTime)) {
      return false;
    }
    return true;
  }
  
}
