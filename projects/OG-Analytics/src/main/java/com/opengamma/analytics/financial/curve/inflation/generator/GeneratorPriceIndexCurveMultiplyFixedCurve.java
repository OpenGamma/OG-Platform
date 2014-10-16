/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.curve.inflation.generator;

import com.opengamma.analytics.financial.model.interestrate.curve.PriceIndexCurve;
import com.opengamma.analytics.financial.model.interestrate.curve.PriceIndexCurveMultiplyFixedCurve;
import com.opengamma.analytics.financial.provider.description.inflation.InflationProviderInterface;
import com.opengamma.analytics.math.curve.DoublesCurve;
import com.opengamma.util.ArgumentChecker;

/**
 * Store the details and generate the required curve. The curve is the sum (or the multiplication) of two curves 
 * (operation on the continuously-compounded zero-coupon rates): an existing curve referenced by its name and a new curve. 
 * The generated curve is a PriceIndexCurve.
 */
public class GeneratorPriceIndexCurveMultiplyFixedCurve extends GeneratorPriceIndexCurve {

  /**
   * The generator for the new curve.
   */
  private final GeneratorPriceIndexCurve _generator;
  /**
   * The name of the existing fixed curve.
   */
  private final DoublesCurve _fixedCurve;

  /**
   * The constructor.
   * @param generator The generator for the new curve.
   * @param fixedCurve The fixed curve.
   */
  public GeneratorPriceIndexCurveMultiplyFixedCurve(final GeneratorPriceIndexCurve generator, final DoublesCurve fixedCurve) {
    ArgumentChecker.notNull(generator, "Generator");
    ArgumentChecker.notNull(fixedCurve, "Fixed curve");
    _generator = generator;
    _fixedCurve = fixedCurve;
  }

  @Override
  public int getNumberOfParameter() {
    return _generator.getNumberOfParameter();
  }

  @Override
  public PriceIndexCurve generateCurve(final String name, final double[] parameters) {
    final PriceIndexCurve newCurve = _generator.generateCurve(name, parameters);
    return new PriceIndexCurveMultiplyFixedCurve(name, newCurve, _fixedCurve);
  }

  @Override
  public PriceIndexCurve generateCurve(final String name, final InflationProviderInterface inflation, final double[] parameters) {
    final PriceIndexCurve newCurve = _generator.generateCurve(name, inflation, parameters);
    return new PriceIndexCurveMultiplyFixedCurve(name, newCurve, _fixedCurve);
  }

  @Override
  public GeneratorPriceIndexCurve finalGenerator(final Object data) {
    return new GeneratorPriceIndexCurveMultiplyFixedCurve(_generator.finalGenerator(data), _fixedCurve);
  }

  @Override
  public double[] initialGuess(final double[] rates) {
    return _generator.initialGuess(rates);
  }

}