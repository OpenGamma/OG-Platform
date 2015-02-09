/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.sesame.marketdata.scenarios;

import java.util.Set;

import com.opengamma.sesame.marketdata.MarketDataId;

/**
 * Encapsulates a rule or set of rules to decide whether a {@link Perturbation} applies to a piece of market data.
 * <p>
 * For example, a filter could apply to all yield curves whose currency is USD, or quoted prices of equity securities
 * in the pharmaceutical sector.
 * <p>
 * Market data filter implementations should generally implement the Joda Beans {@code ImmutableBean} interface
 * which allows them to be serialized and used with a remote implementation of the engine API.
 */
public interface MarketDataFilter {

  /**
   * Applies the filter to a market data ID and returns a set containing details of any matches. If the filter
   * doesn't apply to the ID the set will be empty.
   * <p>
   * This method is invoked by market data builders during construction of market data and will only be
   * invoked where the engine is building the market data itself. Therefore it's safe to assume that
   * any market data configuration linked from the market data ID will be available.
   * <p>
   * The match details are required for complex market data values where different parts of the object can
   * be independently perturbed. For example, the curves in a curve bundle can be independently shocked
   * although they share a single {@code MarketDataId}.
   * <p>
   * In this case, the match details might contain the name of the curve within the bundle. This allows the
   * builder applying the perturbation to choose the correct curve.
   * <p>
   * For most simple market data items it is only possible to apply a single perturbation. In this case
   * the match details should be {@link StandardMatchDetails#MATCHES}.
   *
   * @param marketDataId the ID of a piece of market data
   * @return details of any matches, empty if the filter doesn't match
   */
  Set<? extends MatchDetails> apply(MarketDataId<?> marketDataId);

  /**
   * Applies the filter to a market data ID and the corresponding market data value and returns a set
   * containing details of any matches. If the filter doesn't apply to the ID the set will be empty.
   * <p>
   * This method is invoked after all market data has been constructed. It is possible it will be invoked
   * for market data that has been passed in by the user and not built by the engine. Therefore it is
   * possible the engine has no configuration available for the data. So it is unsafe to dereference
   * any configuration links in this method. Any metadata needed for filtering should be derived from
   * the market data itself.
   * <p>
   * The match details are required for complex market data values where different parts of the object can
   * be independently perturbed. For example, the curves in a curve bundle can be independently shocked
   * although they share a single {@code MarketDataId}.
   * <p>
   * In this case, the match details might contain the name of the curve within the bundle. This allows the
   * builder applying the perturbation to choose the correct curve.
   * <p>
   * For most simple market data items it is only possible to apply a single perturbation. In this case
   * the match details should be {@link StandardMatchDetails#MATCHES}.
   *
   * @param marketDataId the ID of a piece of market data
   * @param marketData the market data value
   * @return details of any matches, empty if the filter doesn't match
   */
  Set<? extends MatchDetails> apply(MarketDataId<?> marketDataId, Object marketData);

  /**
   * Returns the type of market data this filter applies to.
   *
   * @return the type of market data this filter applies to
   */
  Class<?> getMarketDataType();

  /**
   * Returns the type of data this perturbation operates on.
   *
   * @return the type of data this perturbation operates on
   */
  Class<? extends MarketDataId<?>> getMarketDataIdType();
}
