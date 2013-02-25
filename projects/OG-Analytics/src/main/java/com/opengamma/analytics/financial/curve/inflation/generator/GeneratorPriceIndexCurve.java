/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.curve.inflation.generator;

import com.opengamma.analytics.financial.model.interestrate.curve.PriceIndexCurve;

/**
 * Interface for describing and generating inflation curves in curve construction process.
 */
public abstract class GeneratorPriceIndexCurve {

  /**
   * Returns the number of parameters expected to generate the curve.
   * @return The number of parameters.
   */
  public abstract int getNumberOfParameter();

  /**
   * Generate a curve using the parameters of a vector.
   * @param name The curve name.
   * @param parameters The parameters.
   * @return The curve.
   */
  abstract PriceIndexCurve generateCurve(final String name, final double[] parameters);

  /**
   * Some generators require a two stage process. The generator with the general description (like interpolated) and 
   * a specific one with all the details (like the node times for the interpolated). 
   * The method create the specific generator from the generic one.
   * @param data The additional data.
   * @return The final generator.
   */
  public GeneratorPriceIndexCurve finalGenerator(Object data) {
    return this;
  }

}
