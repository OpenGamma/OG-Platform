/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.sesame.marketdata.scenarios;

import com.opengamma.util.ArgumentChecker;

/**
 * A perturbation whose associated filter matches a piece of market data, plus details of the match.
 * <p>
 * The match details are required for complex market data values where different parts of the object can
 * be independently perturbed. For example, the curves in a curve bundle can be independently shocked
 * although they share a single {@code MarketDataId}.
 * <p>
 * In this case, the match details might be the name of the curve within the bundle. This allows the
 * builder applying the perturbation to choose the correct curve.
 * <p>
 * This type is an unfortunate necessity because of our curve model. Functions, and therefore market data environment
 * and market data IDs, are written to use curve bundles. Users think in terms of individual curves, and this
 * is reflected in the scenario framework. The mismatch between these two models makes {@link MatchDetails} and
 * this type necessary. Once the curve model is fixed this should be deleted and {@link Perturbation} can
 * be used directly.
 */
public class FilteredPerturbation {

  private final Perturbation _perturbation;
  private final MatchDetails _matchDetails;

  /**
   * @param perturbation a perturbation that applies to a piece of market data
   * @param matchDetails details of the match when the {@link MarketDataFilter} matched the market data
   */
  public FilteredPerturbation(Perturbation perturbation, MatchDetails matchDetails) {
    _perturbation = ArgumentChecker.notNull(perturbation, "perturbation");
    _matchDetails = ArgumentChecker.notNull(matchDetails, "matchDetails");
  }

  /**
   * @return the underlying perturbation
   */
  public Perturbation getPerturbation() {
    return _perturbation;
  }

  /**
   * Returns true if this object's match details are equal to {@code matchDetails}.
   *
   * @return true if this object's match details are equal to {@code matchDetails}
   */
  public boolean detailsMatch(MatchDetails matchDetails) {
    return _matchDetails.equals(matchDetails);
  }

  /**
   * Applies the perturbation to some market data, returning a new, modified instance of the data.
   * <p>
   * The match details are required for complex market data values where different parts of the object can
   * be independently perturbed. For example, the curves in a curve bundle can be independently shocked
   * although they share a single {@code MarketDataId}.
   * <p>
   * In this case, the match details might be the name of the curve within the bundle. This allows the
   * builder applying the perturbation to choose the correct curve.
   *
   * @param marketData a piece of market data
   * @return a new item of market data derived by applying the perturbation to the input data
   */
  public Object apply(Object marketData) {
    return _perturbation.apply(marketData, _matchDetails);
  }
}
