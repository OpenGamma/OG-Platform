/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.sesame.marketdata.scenarios;

import com.opengamma.sesame.marketdata.MarketDataEnvironment;

/**
 * Describes a perturbation applied to a single piece of data as part of a scenario.
 * <p>
 * For example, a 5 basis point parallel shift of a curve, or a 10% increase in the quoted price of a security.
 * <p>
 * Perturbations are referred to as "input" and "output" perturbations. Input perturbations apply to the data used
 * when building market data, e.g. the quotes for the nodal points when building a curve. Output perturbations apply
 * to the market data in the {@link MarketDataEnvironment}, e.g. a shift applied to a calibrated curve.
 * <p>
 * Perturbation implementations should generally implement the Joda Beans {@code ImmutableBean} interface
 * which allows them to be serialized and used with a remote implementation of the engine API.
 */
public interface Perturbation {

  /**
   * Applies the perturbation to some market data, returning a new, modified instance of the data.
   * <p>
   * The match details are required for complex market data values where different parts of the object can
   * be independently perturbed. For example, the curves in a curve bundle can be independently shocked
   * although they share a single {@code MarketDataId}.
   * <p>
   * In this case, the match details might be the name of the curve within the bundle. This allows the
   * builder applying the perturbation to choose the correct curve.
   * <p>
   * The types of {@code marketData} and {@code matchDetails} are guaranteed by the scenario framework to be
   * compatible with the types returned by {@link #getMarketDataType()} and {@link #getMatchDetailsType()}.
   * Therefore implementations can safely cast the arguments without needing to check the types.
   *
   * @param marketData a piece of market data
   * @param matchDetails details of the match which the {@link MarketDataFilter} was applied to the market data
   * @return a new item of market data derived by applying the perturbation to the input data
   */
  Object apply(Object marketData, MatchDetails matchDetails);

  /**
   * Returns the type of data this perturbation operates on.
   * <p>
   * This is the type expected for the {@code marketData} argument of the {@link #apply} method.
   *
   * @return the type of data this perturbation operates on
   */
  Class<?> getMarketDataType();

  /**
   * Returns the type of data this perturbation operates on.
   * <p>
   * This is the type expected for the {@code marketData} argument of the {@link #apply} method.
   *
   * @return the type of data this perturbation operates on
   */
  Class<? extends MatchDetails> getMatchDetailsType();
}
