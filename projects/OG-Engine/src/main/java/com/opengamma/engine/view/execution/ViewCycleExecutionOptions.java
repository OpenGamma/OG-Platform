/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.view.execution;

import java.util.Collections;
import java.util.List;

import javax.time.Instant;
import javax.time.InstantProvider;

import com.google.common.collect.ImmutableList;
import com.opengamma.engine.marketdata.spec.MarketDataSpecification;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.PublicAPI;

/**
 * Encapsulates specific settings affecting the execution of an individual view cycle.
 */
@PublicAPI
public class ViewCycleExecutionOptions {

  private final Instant _valuationTime;
  private final List<MarketDataSpecification> _marketDataSpecifications;
  
  // TODO [PLAT-1153] view correction time - probably want either valuation time or some fixed correction time
  
  /**
   * Constructs an empty instance.
   */
  public ViewCycleExecutionOptions() {
    this(null, Collections.<MarketDataSpecification>emptyList());
  }
  
  /**
   * Constructs an instance, specifying a valuation time.
   * 
   * @param valuationTimeProvider  the valuation time provider, may be null
   */
  public ViewCycleExecutionOptions(InstantProvider valuationTimeProvider) {
    this(valuationTimeProvider, Collections.<MarketDataSpecification>emptyList());
  }
  
  /**
   * Constructs an instance, specifying a market data specification.
   * 
   * @param marketDataSpec  the market data specification, may be null
   */
  public ViewCycleExecutionOptions(MarketDataSpecification marketDataSpec) {
    this(null, marketDataSpec);
  }
  
  /**
   * Constructs an instance.
   * 
   * @param valuationTimeProvider  the valuation time provider, may be null
   * @param marketDataSpec  the market data specification, may be null
   */
  public ViewCycleExecutionOptions(InstantProvider valuationTimeProvider, MarketDataSpecification marketDataSpec) {
    this(valuationTimeProvider, marketDataSpec == null ?
        Collections.<MarketDataSpecification>emptyList() : Collections.singletonList(marketDataSpec));
  }

  /**
   * Constructs an instance.
   *
   * @param valuationTimeProvider  the valuation time provider, may be null
   * @param marketDataSpecs  the market data specification, not null, may be empty
   */
  public ViewCycleExecutionOptions(InstantProvider valuationTimeProvider, List<MarketDataSpecification> marketDataSpecs) {
    ArgumentChecker.notNull(marketDataSpecs, "marketDataSpecs");
    _valuationTime = valuationTimeProvider == null ? null : valuationTimeProvider.toInstant();
    _marketDataSpecifications = ImmutableList.copyOf(marketDataSpecs);
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
   * Gets the market data specifications.
   *
   * @return the market data specifications, not null but possibly empty
   */
  public List<MarketDataSpecification> getMarketDataSpecifications() {
    return _marketDataSpecifications;
  }

  @Override
  public String toString() {
    return "ViewCycleExecutionOptions[valuationTime=" + _valuationTime + ", marketDataSpecifications=" + _marketDataSpecifications + "]";
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((_marketDataSpecifications == null) ? 0 : _marketDataSpecifications.hashCode());
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
    if (_marketDataSpecifications == null) {
      if (other._marketDataSpecifications != null) {
        return false;
      }
    } else if (!_marketDataSpecifications.equals(other._marketDataSpecifications)) {
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
