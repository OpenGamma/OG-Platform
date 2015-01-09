/**
 * Copyright (C) 2015 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.sesame.marketdata.scenarios;

import com.opengamma.sesame.marketdata.MarketDataId;

/**
 * Marker interface for types holding the details of the match when a {@link MarketDataFilter} is applied.
 * <p>
 * Match details are required for complex market data values where different parts of the object can
 * be independently perturbed. For example, the curves in a curve bundle can be independently shocked
 * although they share a single {@code MarketDataId}.
 * <p>
 * In this case, the match details might be the name of the curve within the bundle. This allows the
 * builder applying the perturbation to choose the correct curve.
 * <p>
 * Each type of market data should be associated with a single type of match details. If different perturbation
 * implementations for the same piece of market data used a different type of match details, it wouldn't be possible
 * to keep track of what market data had been perturbed.
 * <p>
 * Factory methods for match details implementations for the standard market data types are provided
 * in {@link StandardMatchDetails}.
 * <p>
 * The majority of market data types are identified by their {@link MarketDataId} alone and don't need
 * to communicate any details about a match. They should use {@link StandardMatchDetails#MATCH}.
 * <p>
 * This type is an unfortunate necessity because of our curve model. Functions, and therefore market data environment
 * and market data IDs, are written to use curve bundles. Users think in terms of individual curves, and this
 * is reflected in the scenario framework. The mismatch between these two models makes this type necessary.
 * Once the curve model is fixed this should be deleted and {@link MarketDataId} alone should be used to identify
 * all types of market data.
 */
public interface MatchDetails {

}
