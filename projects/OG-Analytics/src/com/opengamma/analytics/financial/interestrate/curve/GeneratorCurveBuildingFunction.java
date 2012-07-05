/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate.curve;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Set;

import com.opengamma.analytics.financial.interestrate.YieldCurveBundle;
import com.opengamma.analytics.financial.interestrate.YieldCurveBundleBuildingFunction;
import com.opengamma.analytics.financial.model.interestrate.curve.YieldAndDiscountCurve;
import com.opengamma.analytics.math.matrix.DoubleMatrix1D;

/**
 * Yield curve bundle building function based on an array of curve generators.
 */
public class GeneratorCurveBuildingFunction extends YieldCurveBundleBuildingFunction {

  private final LinkedHashMap<String, GeneratorCurve> _curveGenerators;

  /**
   * Constructor
   * @param curveGenerators The curve constructor. The order is important.
   */
  public GeneratorCurveBuildingFunction(LinkedHashMap<String, GeneratorCurve> curveGenerators) {
    _curveGenerators = curveGenerators;
  }

  @Override
  public YieldCurveBundle evaluate(DoubleMatrix1D x) {
    YieldCurveBundle bundle = new YieldCurveBundle();
    Set<String> names = _curveGenerators.keySet();
    int index = 0;
    for (String name : names) {
      GeneratorCurve gen = _curveGenerators.get(name);
      double[] paramCurve = Arrays.copyOfRange(x.getData(), index, index + gen.getNumberOfParameter());
      index += gen.getNumberOfParameter();
      YieldAndDiscountCurve newCurve = gen.generateCurve(name, bundle, paramCurve);
      bundle.setCurve(name, newCurve);
    }
    return bundle;
  }

}
