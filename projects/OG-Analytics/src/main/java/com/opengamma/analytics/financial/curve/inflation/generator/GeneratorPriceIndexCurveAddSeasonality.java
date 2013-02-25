/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.curve.inflation.generator;

import com.opengamma.analytics.financial.model.interestrate.curve.PriceIndexCurve;
import com.opengamma.util.ArgumentChecker;

/**
 * Store the details and generate the required curve. The curve is the sum (or the multiplication) of two curves 
 * (operation on the continuously-compounded zero-coupon rates): an existing curve referenced by its name and a new curve. 
 * The generated curve is a PriceIndexCurve.
 */
public class GeneratorPriceIndexCurveAddSeasonality extends GeneratorPriceIndexCurve {

  /**
   * The generator for the new curve.
   */
  private final GeneratorPriceIndexCurve _generator;
  /**
   * If true the rate of the new curve will be subtracted from the first one. If false the rates are added.
   */
  private final boolean _isAdditive;
  /**
   * The name of the existing curve.
   */
  private final String _existingCurveName;

  /**
   * The constructor.
   * @param generator The generator for the new curve.
   * @param isAdditive If true the rate of the new curve will be subtracted from the first one. If false the rates are added.
   * @param existingCurveName The name of the existing curve.
   */
  public GeneratorPriceIndexCurveAddSeasonality(final GeneratorPriceIndexCurve generator, final boolean isAdditive, final String existingCurveName) {
    ArgumentChecker.notNull(generator, "Generator");
    ArgumentChecker.notNull(existingCurveName, "Exisitng curve name");
    _generator = generator;
    _isAdditive = isAdditive;
    _existingCurveName = existingCurveName;
  }

  @Override
  public int getNumberOfParameter() {
    return _generator.getNumberOfParameter();
  }

  @Override
  PriceIndexCurve generateCurve(String name, double[] parameters) {
    throw new UnsupportedOperationException("Cannot create the curve form the generator without an existing curve");
  }

}
