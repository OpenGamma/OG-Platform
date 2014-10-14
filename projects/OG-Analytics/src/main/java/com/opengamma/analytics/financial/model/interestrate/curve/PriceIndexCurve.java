/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.model.interestrate.curve;

import java.util.List;
import java.util.Set;

/**
 * Interface to curve describing the price index related to inflation.
 */
public interface PriceIndexCurve {

  /**
   * Returns the curve name.
   * @return The name.
   */
  String getName();

  /**
   * Returns the estimated price index for a given time to index.
   * @param timeToIndex The time
   * @return The price index.
   */
  double getPriceIndex(final Double timeToIndex);

  /**
   * Returns the estimated inflation rate between two given time .
   * @param firstTime The time
   * @param secondTime The time
   * @return The price index.
   */
  double getInflationRate(final Double firstTime, final Double secondTime);

  /**
   * Gets the number of parameters in a curve.
   * @return The number of parameters
   */
  int getNumberOfParameters();

  /**
   * Return the number of intrinsic parameters for the definition of the curve. Which is the total number of parameters minus the parameters of the curves in curvesNames (If they are in curves).
   *  @param curvesNames The list of curves names.
   *  @return The number of parameters.
   */
  int getNumberOfIntrinsicParameters(final Set<String> curvesNames);

  /**
   * The list of underlying curves (up to one level).
   * @return The list.
   */
  List<String> getUnderlyingCurvesNames();

  /**
   * Gets the sensitivities of the price index to the curve parameters for a time.
   * @param time The time
   * @return The sensitivities. If the time is less than 1e<sup>-6</sup>, the rate is
   * ill-defined and zero is returned.
   */
  double[] getPriceIndexParameterSensitivity(final double time);

}
