/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate.curve;

import com.opengamma.analytics.financial.interestrate.YieldCurveBundle;
import com.opengamma.analytics.financial.model.interestrate.curve.YieldAndDiscountCurve;

/**
 * Interface for describing and generating curves in curve construction process.
 */
public interface GeneratorCurve {

  /**
   * Returns the number of parameters expected to generate the curve.
   * @return The number of parameters.
   */
  int getNumberOfParameter();

  /**
   * Generate a curve using the parameters of a vector.
   * @param name The curve name.
   * @param parameters The parameters.
   * @return The curve.
   */
  YieldAndDiscountCurve generateCurve(final String name, final double[] parameters);

  /**
   * Generate a curve using the parameters of a vector and an existing bundle. The existing bundle will be required if the generated curve depends on previous curves.
   * @param name The curve name.
   * @param bundle The bundle of existing curves.
   * @param parameters The parameters.
   * @return The curve.
   */
  YieldAndDiscountCurve generateCurve(final String name, final YieldCurveBundle bundle, final double[] parameters);

}
