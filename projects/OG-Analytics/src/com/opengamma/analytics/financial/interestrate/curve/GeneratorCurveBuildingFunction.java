/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate.curve;

import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import com.opengamma.analytics.financial.forex.method.FXMatrix;
import com.opengamma.analytics.financial.interestrate.YieldCurveBundle;
import com.opengamma.analytics.financial.interestrate.YieldCurveBundleBuildingFunction;
import com.opengamma.analytics.financial.model.interestrate.curve.YieldAndDiscountCurve;
import com.opengamma.analytics.math.matrix.DoubleMatrix1D;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.money.Currency;

/**
 * Yield curve bundle building function based on an map of curve generators.
 */
public class GeneratorCurveBuildingFunction extends YieldCurveBundleBuildingFunction {

  /**
   * The map with the curve names and the related generators.
   */
  private final LinkedHashMap<String, GeneratorCurve> _curveGenerators;
  /**
   * A map linking each curve in the bundle to its currency.
   */
  private final Map<String, Currency> _curveCurrency;
  /**
   * The matrix containing the exchange rates.
   */
  private final FXMatrix _fxMatrix;

  // REVIEW: Should the FXMatrix and currency map be in the "KnownCurves" (to be replaced by "KnownBundle")

  /**
   * Constructor
   * @param curveGenerators The curve constructor. The order is important.
   */
  public GeneratorCurveBuildingFunction(LinkedHashMap<String, GeneratorCurve> curveGenerators) {
    ArgumentChecker.notNull(curveGenerators, "Curve generator map");
    _curveGenerators = curveGenerators;
    _curveCurrency = new HashMap<String, Currency>();
    _fxMatrix = new FXMatrix();
  }

  /**
   * Constructor with a given fxMatrix and currency map. The currency map and FXMatrix are directly used.
   * @param curveGenerators The curve constructor. The order is important.
   * @param fxMatrix The FXMatrix.
   * @param curveCurrency The map of currencies.
   */
  public GeneratorCurveBuildingFunction(LinkedHashMap<String, GeneratorCurve> curveGenerators, final FXMatrix fxMatrix, final Map<String, Currency> curveCurrency) {
    ArgumentChecker.notNull(curveGenerators, "Curve generator map");
    _curveGenerators = curveGenerators;
    _curveCurrency = curveCurrency;
    _fxMatrix = fxMatrix;
  }

  @Override
  public YieldCurveBundle evaluate(DoubleMatrix1D x) {
    YieldCurveBundle bundle = new YieldCurveBundle(_fxMatrix, _curveCurrency);
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
