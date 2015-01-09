/**
 * Copyright (C) 2015 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.sesame.marketdata.scenarios;

import com.google.common.collect.ImmutableSet;

/**
 * Helper class for creating {@link MatchDetails} instances for the standard market data types built into the
 * OpenGamma platform.
 * <p>
 * Any user-defined {@link MarketDataFilter} implementations acting on the standard types should use the 
 * standard match details implementation for that type. Otherwise there is a risk that two perturbations
 * could accidentally be applied to the same piece of market data.
 * <p>
 * If a type of market data doesn't need to use match details (which should be most types) the filter should return
 * {@link #MATCH} from its {@code apply} method.
 */
public final class StandardMatchDetails {

  /**
   * If a type of market data doesn't need to use match details (which should be most types) the filter should return
   * this from its {@code apply} method.
   */
  public final ImmutableSet<MatchDetails> MATCH = ImmutableSet.<MatchDetails>of(new NoDetails());

  private StandardMatchDetails() {
  }

  /**
   * Returns match details for a single curve inside a multicurve bundle.
   *
   * @param curveName the name of the curve that matched the filter
   * @return match details containing the name of the matched curve
   */
  public static MulticurveMatchDetails multicurve(String curveName) {
    return new MulticurveMatchDetails(curveName);
  }

  /**
   * {@code MatchDetails} implementation for market data types that don't need any details.
   */
  public static final class NoDetails implements MatchDetails {

    private NoDetails() {
    }
  }
}
