/**
 * Copyright (C) 2015 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.sesame.marketdata.scenarios;

/**
 * The type of the data affected by a {@link Perturbation}. A perturbation can be applied to the raw input data
 * used for building market data or to the market data itself.
 */
public enum PerturbationTarget {

  /**
   * The perturbation transforms raw input data used to build market data, for example by shifting the quotes used
   * when calibrating a curve.
   * <p>
   * For this target type, the market data type of the filter is not necessarily the same as the
   * data type transformed by the perturbation.
   */
  INPUT,

  /**
   * The perturbation transforms built market data, for example by applying a parallel shift to a curve.
   * <p>
   * For this target type, the market data type of the filter is the same as the data type of the perturbation.
   */
  OUTPUT
}
