/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.curve;

import com.opengamma.analytics.financial.interestrate.YieldCurveBundle;
import com.opengamma.analytics.financial.model.interestrate.curve.YieldAndDiscountAddZeroSpreadCurve;
import com.opengamma.analytics.financial.model.interestrate.curve.YieldAndDiscountCurve;
import com.opengamma.util.ArgumentChecker;

/**
 * Store the details and generate the required curve. The curve is the sum (or difference) of two curves 
 * (operation on the continuously-compounded zero-coupon rates): an existing curve and a new curve. 
 * The generated curve is a YieldAndDiscountAddZeroSpreadCurve.
 */
public class GeneratorCurveAddYieldExisiting extends GeneratorCurve {

  /**
   * The generator for the new curve.
   */
  private final GeneratorCurve _generator;
  /**
   * If true the rate of the new curve will be subtracted from the first one. If false the rates are added.
   */
  private final boolean _substract;
  /**
   * The name of the existing curve.
   */
  private final String _existingCurveName;

  /**
   * The constructor.
   * @param generator The generator for the new curve.
   * @param substract If true the rate of the new curve will be subtracted from the first one. If false the rates are added.
   * @param existingCurveName The name of the existing curve.
   */
  public GeneratorCurveAddYieldExisiting(final GeneratorCurve generator, final boolean substract, final String existingCurveName) {
    ArgumentChecker.notNull(generator, "Generator");
    ArgumentChecker.notNull(existingCurveName, "Exisitng curve name");
    _generator = generator;
    _substract = substract;
    _existingCurveName = existingCurveName;
  }

  @Override
  public int getNumberOfParameter() {
    return _generator.getNumberOfParameter();
  }

  @Override
  public YieldAndDiscountCurve generateCurve(String name, double[] parameters) {
    throw new UnsupportedOperationException("Cannot create the curve form the generator without an existing curve");
  }

  @Override
  public YieldAndDiscountCurve generateCurve(String name, YieldCurveBundle bundle, double[] parameters) {
    YieldAndDiscountCurve existingCurve = bundle.getCurve(_existingCurveName);
    YieldAndDiscountCurve newCurve = _generator.generateCurve(name + "-0", bundle, parameters);
    return new YieldAndDiscountAddZeroSpreadCurve(name, _substract, existingCurve, newCurve);
  }

  @Override
  public GeneratorCurve finalGenerator(Object data) {
    return new GeneratorCurveAddYieldExisiting(_generator.finalGenerator(data), _substract, _existingCurveName);
  }

}
