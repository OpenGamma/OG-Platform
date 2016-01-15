/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.curve.interestrate.generator;

import java.io.Serializable;

/**
 * common interface for describing and generating inflation and yield curves in curve construction process.
 */
public abstract class GeneratorCurve implements Serializable {

  /**
   * Returns the number of parameters expected to generate the curve.
   * @return The number of parameters.
   */
  public abstract int getNumberOfParameter();

  /**
   * Some generators require a two stage process. The generator with the general description (like interpolated) and 
   * a specific one with all the details (like the node times for the interpolated). 
   * The method create the specific generator from the generic one.
   * @param data The additional data.
   * @return The final generator.
   */
  public GeneratorCurve finalGenerator(Object data) {
    return this;
  }

  /**
   * The initial guess of parameters can be very different for different curve descriptions (in particular for functional curves).
   * The method produce a set of initial guess parameters from the instruments "rates". By default it simply return the rates.
   * @param rates The instrument estimated rates.
   * @return The initial parameters guess.
   */
  public double[] initialGuess(double[] rates) {
    return rates;
  }

}
