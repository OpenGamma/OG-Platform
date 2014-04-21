/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.view.worker.cache;

import java.io.Serializable;

import org.apache.commons.lang.ObjectUtils;

import com.opengamma.engine.marketdata.availability.MarketDataAvailabilityProvider;
import com.opengamma.engine.marketdata.manipulator.MarketDataSelectionGraphManipulator;
import com.opengamma.engine.view.ViewDefinition;
import com.opengamma.id.UniqueId;

/**
 * Digest of the view execution parameters to determine when it may be possible to share or reuse a dependency graph between workers.
 * <p>
 * The digest comprises the view definition's unique identifier and the cache hint keys from the market data provider and scenario manipulations.
 */
public final class ViewExecutionCacheKey implements Serializable {

  private static final long serialVersionUID = 1L;

  private final UniqueId _viewDefinitionId;
  private final Serializable _marketDataProvider;
  private final Serializable _scenarioManipulations;

  /* package */ViewExecutionCacheKey(final UniqueId viewDefinitionId, final Serializable marketDataProvider, final Serializable scenarioManipulations) {
    _viewDefinitionId = viewDefinitionId;
    _marketDataProvider = marketDataProvider;
    _scenarioManipulations = scenarioManipulations;
  }

  private static Serializable getMarketDataSelectorCacheHintKey(final MarketDataSelectionGraphManipulator graphManipulations) {
    if (graphManipulations == null) {
      return "No-op";
    } else {
      return graphManipulations.getCacheHintKey();
    }
  }

  /**
   * Creates a new key based on the view definition and a single market data availability provider (this may be a composite data provider).
   * 
   * @param viewDefinition the view definition, not null
   * @param marketDataProvider the market data availability provider, not null
   * @param graphManipulations any selectors that have been applied to the graph which will cause deviation from the normally produced graph, null for none
   * @return the cache key, not null
   */
  public static ViewExecutionCacheKey of(final ViewDefinition viewDefinition, final MarketDataAvailabilityProvider marketDataProvider,
      final MarketDataSelectionGraphManipulator graphManipulations) {
    return new ViewExecutionCacheKey(viewDefinition.getUniqueId(), marketDataProvider.getAvailabilityHintKey(), getMarketDataSelectorCacheHintKey(graphManipulations));
  }

  @Override
  public boolean equals(final Object o) {
    if (o == this) {
      return true;
    }
    if (!(o instanceof ViewExecutionCacheKey)) {
      return false;
    }
    final ViewExecutionCacheKey other = (ViewExecutionCacheKey) o;
    return _viewDefinitionId.equals(other._viewDefinitionId) && ObjectUtils.equals(_marketDataProvider, other._marketDataProvider) &&
        ObjectUtils.equals(_scenarioManipulations, other._scenarioManipulations);
  }

  @Override
  public int hashCode() {
    int hc = _viewDefinitionId.hashCode();
    hc += (hc << 4) + ObjectUtils.hashCode(_marketDataProvider);
    hc += (hc << 4) + ObjectUtils.hashCode(_scenarioManipulations);
    return hc;
  }

  @Override
  public String toString() {
    final StringBuilder sb = new StringBuilder();
    sb.append("ViewExecution[").append(_viewDefinitionId);
    sb.append(", ").append(_marketDataProvider);
    sb.append(", ").append(_scenarioManipulations);
    sb.append("]");
    return sb.toString();
  }

}
