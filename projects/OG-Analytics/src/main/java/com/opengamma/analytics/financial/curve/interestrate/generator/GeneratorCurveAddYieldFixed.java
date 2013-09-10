/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.curve.interestrate.generator;

import com.opengamma.analytics.financial.interestrate.YieldCurveBundle;
import com.opengamma.analytics.financial.model.interestrate.curve.YieldAndDiscountAddZeroFixedCurve;
import com.opengamma.analytics.financial.model.interestrate.curve.YieldAndDiscountCurve;
import com.opengamma.analytics.financial.provider.description.interestrate.MulticurveProviderInterface;
import com.opengamma.util.ArgumentChecker;

/**
 * Store the details and generate the required curve. The curve is the sum (or difference) of two curves
 * (operation on the continuously-compounded zero-coupon rates): a fixed curve and a new curve.
 * The generated curve is a YieldAndDiscountAddZeroSpreadCurve.
 */
@SuppressWarnings("deprecation")
public class GeneratorCurveAddYieldFixed extends GeneratorYDCurve {

  /**
   * The generator for the new curve.
   */
  private final GeneratorYDCurve _generator;
  /**
   * If true the rate of the new curve will be subtracted from the first one. If false the rates are added.
   */
  private final boolean _substract;
  /**
   * The fixed curve.
   */
  private final YieldAndDiscountCurve _fixedCurve;

  /**
   * The constructor.
   * @param generator The generator for the new curve.
   * @param substract If true the rate of the new curve will be subtracted from the first one. If false the rates are added.
   * @param fixedCurve The fixed curve.
   */
  public GeneratorCurveAddYieldFixed(final GeneratorYDCurve generator, final boolean substract, final YieldAndDiscountCurve fixedCurve) {
    ArgumentChecker.notNull(generator, "Generator");
    ArgumentChecker.notNull(fixedCurve, "Fixed curve");
    _generator = generator;
    _substract = substract;
    _fixedCurve = fixedCurve;
  }

  @Override
  public int getNumberOfParameter() {
    return _generator.getNumberOfParameter();
  }

  @Override
  public YieldAndDiscountCurve generateCurve(final String name, final double[] parameters) {
    final YieldAndDiscountCurve newCurve = _generator.generateCurve(name + "-0", parameters);
    return new YieldAndDiscountAddZeroFixedCurve(name, _substract, newCurve, _fixedCurve);
  }

  /**
   * {@inheritDoc}
   * @deprecated Curve builders that use and populate {@link YieldCurveBundle}s are deprecated.
   */
  @Deprecated
  @Override
  public YieldAndDiscountCurve generateCurve(final String name, final YieldCurveBundle bundle, final double[] parameters) {
    final YieldAndDiscountCurve newCurve = _generator.generateCurve(name + "-0", bundle, parameters);
    return new YieldAndDiscountAddZeroFixedCurve(name, _substract, newCurve, _fixedCurve);
  }

  @Override
  public YieldAndDiscountCurve generateCurve(final String name, final MulticurveProviderInterface multicurve, final double[] parameters) {
    final YieldAndDiscountCurve newCurve = _generator.generateCurve(name + "-0", multicurve, parameters);
    return new YieldAndDiscountAddZeroFixedCurve(name, _substract, newCurve, _fixedCurve);
  }

  @Override
  public GeneratorYDCurve finalGenerator(final Object data) {
    return new GeneratorCurveAddYieldFixed(_generator.finalGenerator(data), _substract, _fixedCurve);
  }

  @Override
  public double[] initialGuess(final double[] rates) {
    return _generator.initialGuess(rates);
  }

}
