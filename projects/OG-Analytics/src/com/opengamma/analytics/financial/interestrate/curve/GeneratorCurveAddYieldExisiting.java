/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate.curve;

import com.opengamma.analytics.financial.interestrate.YieldCurveBundle;
import com.opengamma.analytics.financial.model.interestrate.curve.YieldAndDiscountAddZeroSpreadCurve;
import com.opengamma.analytics.financial.model.interestrate.curve.YieldAndDiscountCurve;

/**
 * 
 */
public class GeneratorCurveAddYieldExisiting implements GeneratorCurve {

  private final GeneratorCurve _generator;
  private final boolean _substract;
  private final String _existingCurveName;

  public GeneratorCurveAddYieldExisiting(final GeneratorCurve generator, final boolean substract, final String existingCurveName) {
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
    YieldAndDiscountCurve newCurve = _generator.generateCurve("Spread", bundle, parameters);
    return new YieldAndDiscountAddZeroSpreadCurve(name, _substract, existingCurve, newCurve);
  }
}
