/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate.curve;

import java.util.Arrays;

import com.opengamma.analytics.financial.interestrate.YieldCurveBundle;
import com.opengamma.analytics.financial.model.interestrate.curve.YieldAndDiscountAddZeroSpreadCurve;
import com.opengamma.analytics.financial.model.interestrate.curve.YieldAndDiscountCurve;
import com.opengamma.util.ArgumentChecker;

/**
 * Store the details and generate the required curve. The curve is the sum (or difference) of the curves produced by the array of generators. 
 * The generated curve is a YieldAndDiscountAddZeroSpreadCurve.
 */
public class GeneratorCurveAddYield implements GeneratorCurve {

  private final GeneratorCurve[] _generators;
  private final boolean _substract;
  private final int _nbParameters;
  private final int _nbGenerators;

  /**
   * Constructor.
   * @param generators The array of constructors for the component curves.
   * @param substract If true, the rate of all curves, except the first one, will be subtracted from the first one. If false, all the rates are added.
   */
  public GeneratorCurveAddYield(GeneratorCurve[] generators, boolean substract) {
    _generators = generators;
    _nbGenerators = generators.length;
    _substract = substract;
    int nbParam = 0;
    for (int loopgen = 0; loopgen < _nbGenerators; loopgen++) {
      nbParam += _generators[loopgen].getNumberOfParameter();
    }
    _nbParameters = nbParam;
  }

  @Override
  public int getNumberOfParameter() {
    return _nbParameters;
  }

  @Override
  public YieldAndDiscountCurve generateCurve(String name, double[] x) {
    ArgumentChecker.isTrue(x.length == _nbParameters, "Incorrect number of parameters");
    YieldAndDiscountCurve[] underlyingCurves = new YieldAndDiscountCurve[_nbGenerators];
    int index = 0;
    for (int loopgen = 0; loopgen < _nbGenerators; loopgen++) {
      double[] paramCurve = Arrays.copyOfRange(x, index, index + _generators[loopgen].getNumberOfParameter());
      index += _generators[loopgen].getNumberOfParameter();
      underlyingCurves[loopgen] = _generators[loopgen].generateCurve("", paramCurve);
    }
    return new YieldAndDiscountAddZeroSpreadCurve(name, _substract, underlyingCurves);
  }

  @Override
  public YieldAndDiscountCurve generateCurve(String name, YieldCurveBundle bundle, double[] parameters) {
    return generateCurve(name, parameters);
  }

}
