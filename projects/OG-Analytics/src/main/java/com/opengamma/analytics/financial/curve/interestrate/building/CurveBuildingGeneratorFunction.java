/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.curve.interestrate.building;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Set;

import com.opengamma.analytics.financial.curve.interestrate.generator.GeneratorYDCurve;
import com.opengamma.analytics.financial.interestrate.YieldCurveBundle;
import com.opengamma.analytics.financial.interestrate.YieldCurveBundleBuildingFunction;
import com.opengamma.analytics.financial.model.interestrate.curve.YieldAndDiscountCurve;
import com.opengamma.analytics.financial.provider.curve.multicurve.MulticurveDiscountBuildingRepository;
import com.opengamma.analytics.math.matrix.DoubleMatrix1D;
import com.opengamma.util.ArgumentChecker;

/**
 * Yield curve bundle building function based on an map of curve generators.
 * @deprecated Curve builders that use and populate {@link YieldCurveBundle}s are deprecated. Use classes such as
 * {@link MulticurveDiscountBuildingRepository}.
 */
@Deprecated
public class CurveBuildingGeneratorFunction extends YieldCurveBundleBuildingFunction {

  /**
   * The map with the curve names and the related generators.
   */
  private final LinkedHashMap<String, GeneratorYDCurve> _curveGenerators;
  /**
   * The yield curve bundle with known data (curves).
   */
  private final YieldCurveBundle _knownData;

  /**
   * Constructor
   * @param curveGenerators The curve constructor. The order is important.
   */
  public CurveBuildingGeneratorFunction(final LinkedHashMap<String, GeneratorYDCurve> curveGenerators) {
    ArgumentChecker.notNull(curveGenerators, "Curve generator map");
    _curveGenerators = curveGenerators;
    _knownData = new YieldCurveBundle();
  }

  /**
   * Constructor
   * @param curveGenerators The curve constructor. The order is important.
   * @param knownData The yield curve bundle with known data (curves).
   */
  public CurveBuildingGeneratorFunction(final LinkedHashMap<String, GeneratorYDCurve> curveGenerators, final YieldCurveBundle knownData) {
    ArgumentChecker.notNull(curveGenerators, "Curve generator map");
    _curveGenerators = curveGenerators;
    _knownData = knownData;
  }

  /**
   * Gets the know curves.
   * @return The known curves.
   */
  public YieldCurveBundle getKnownData() {
    return _knownData;
  }

  @Override
  public YieldCurveBundle evaluate(final DoubleMatrix1D x) {
    final YieldCurveBundle bundle = _knownData.copy();
    final Set<String> names = _curveGenerators.keySet();
    int index = 0;
    for (final String name : names) {
      final GeneratorYDCurve gen = _curveGenerators.get(name);
      final double[] paramCurve = Arrays.copyOfRange(x.getData(), index, index + gen.getNumberOfParameter());
      index += gen.getNumberOfParameter();
      final YieldAndDiscountCurve newCurve = gen.generateCurve(name, bundle, paramCurve);
      bundle.setCurve(name, newCurve);
    }
    return bundle;
  }

}
