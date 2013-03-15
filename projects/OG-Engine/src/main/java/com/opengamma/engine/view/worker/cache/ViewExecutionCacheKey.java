/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.view.worker.cache;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;

import com.opengamma.engine.marketdata.availability.MarketDataAvailabilityProvider;
import com.opengamma.engine.view.ViewDefinition;
import com.opengamma.id.UniqueId;
import com.opengamma.util.ArgumentChecker;

/**
 * Digest of the view execution parameters to determine when it may be possible to share or reuse a dependency graph between workers.
 * <p>
 * The digest comprises the view definition's unique identifier and the cache hint keys from the market data providers.
 */
public final class ViewExecutionCacheKey implements Serializable {

  private static final long serialVersionUID = 1L;

  private final UniqueId _viewDefinitionId;
  private final Serializable[] _marketDataProvider;

  /* package */ViewExecutionCacheKey(final UniqueId viewDefinitionId, final Serializable[] marketDataProvider) {
    ArgumentChecker.isTrue(marketDataProvider.length > 0, "marketDataProvider");
    _viewDefinitionId = viewDefinitionId;
    _marketDataProvider = marketDataProvider;
  }

  /**
   * Creates a new key based on the view definition and an ordered collection of market data availability providers.
   * 
   * @param viewDefinition the view definition, not null
   * @param marketDataProviders the market data availability providers, not null and not containing nulls
   * @return the cache key, not null
   */
  public static ViewExecutionCacheKey of(final ViewDefinition viewDefinition, final Iterable<MarketDataAvailabilityProvider> marketDataProviders) {
    final Collection<Serializable> tokens = new LinkedList<Serializable>();
    for (MarketDataAvailabilityProvider marketDataProvider : marketDataProviders) {
      tokens.add(marketDataProvider.getAvailabilityHintKey());
    }
    return new ViewExecutionCacheKey(viewDefinition.getUniqueId(), tokens.toArray(new Serializable[tokens.size()]));
  }

  /**
   * Creates a new key based on the view definition and a single market data availability provider.
   * 
   * @param viewDefinition the view definition, not null
   * @param marketDataProvider the market data availability provider, not null
   * @return the cache key, not null
   */
  public static ViewExecutionCacheKey of(final ViewDefinition viewDefinition, final MarketDataAvailabilityProvider marketDataProvider) {
    return new ViewExecutionCacheKey(viewDefinition.getUniqueId(), new Serializable[] {marketDataProvider.getAvailabilityHintKey() });
  }

  /**
   * Creates a new key based on the view definition and an array of market data availability providers.
   * 
   * @param viewDefinition the view definition, not null
   * @param marketDataProviders the market data availability providers, not null and not containing nulls
   * @return the cache key, not null
   */
  public static ViewExecutionCacheKey of(final ViewDefinition viewDefinition, final MarketDataAvailabilityProvider[] marketDataProviders) {
    final Serializable[] tokens = new Serializable[marketDataProviders.length];
    for (int i = 0; i < marketDataProviders.length; i++) {
      tokens[i] = marketDataProviders[i].getAvailabilityHintKey();
    }
    return new ViewExecutionCacheKey(viewDefinition.getUniqueId(), tokens);
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
    return _viewDefinitionId.equals(other._viewDefinitionId)
        && Arrays.deepEquals(_marketDataProvider, other._marketDataProvider);
  }

  @Override
  public int hashCode() {
    int hc = _viewDefinitionId.hashCode();
    hc += (hc << 4) + Arrays.deepHashCode(_marketDataProvider);
    return hc;
  }

  @Override
  public String toString() {
    final StringBuilder sb = new StringBuilder();
    sb.append("ViewExecution[").append(_viewDefinitionId);
    for (Serializable marketDataProvider : _marketDataProvider) {
      sb.append(", ").append(marketDataProvider);
    }
    sb.append("]");
    return sb.toString();
  }

}
