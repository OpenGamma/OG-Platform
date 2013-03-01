/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.curve.interestrate.generator;

import java.util.Arrays;

import com.opengamma.analytics.financial.interestrate.InstrumentDerivative;
import com.opengamma.analytics.financial.interestrate.YieldCurveBundle;
import com.opengamma.analytics.financial.model.interestrate.curve.YieldAndDiscountAddZeroSpreadCurve;
import com.opengamma.analytics.financial.model.interestrate.curve.YieldAndDiscountCurve;
import com.opengamma.analytics.financial.provider.description.interestrate.MulticurveProviderInterface;
import com.opengamma.util.ArgumentChecker;

/**
 * Store the details and generate the required curve. The curve is the sum (or difference) of the curves 
 * (operation on the continuously-compounded zero-coupon rates)  produced by the array of generators. 
 * The number of parameter for each curve is imposed.
 * The generated curve is a YieldAndDiscountAddZeroSpreadCurve. 
 */
public class GeneratorCurveAddYieldNb extends GeneratorYDCurve {

  /**
   * The array of generators describing the different parts of the spread curve.
   */
  private final GeneratorYDCurve[] _generators;
  /**
   * The number of parameter associated to each generator.
   */
  private final int[] _nbParameters;
  /**
   * The total number of parameters (sum of _nbParameters).
   */
  private int _totalNbParameters;
  /**
   * If true, the rate of all curves, except the first one, will be subtracted from the first one. If false, all the rates are added.
   */
  private final boolean _substract;
  /**
   * The number of generators.
   */
  private final int _nbGenerators;

  /**
   * Constructor.
   * @param generators The array of constructors for the component curves.
   * @param nbParameters The number of parameter associated to each generator.
   * @param substract If true, the rate of all curves, except the first one, will be subtracted from the first one. If false, all the rates are added.
   */
  public GeneratorCurveAddYieldNb(GeneratorYDCurve[] generators, int[] nbParameters, boolean substract) {
    ArgumentChecker.notNull(generators, "Generators");
    _generators = generators;
    ArgumentChecker.isTrue(generators.length == nbParameters.length, "Number of parameters should be the same a number of generatros.");
    _nbParameters = nbParameters;
    _totalNbParameters = 0;
    for (int loopp = 0; loopp < nbParameters.length; loopp++) {
      _totalNbParameters += nbParameters[loopp];
    }
    _nbGenerators = generators.length;
    _substract = substract;
  }

  @Override
  public int getNumberOfParameter() {
    int nbParam = 0;
    for (int loopgen = 0; loopgen < _nbGenerators; loopgen++) {
      nbParam += _generators[loopgen].getNumberOfParameter();
    }
    return nbParam;
  }

  @Override
  public YieldAndDiscountCurve generateCurve(String name, double[] x) {
    ArgumentChecker.isTrue(x.length == getNumberOfParameter(), "Incorrect number of parameters");
    YieldAndDiscountCurve[] underlyingCurves = new YieldAndDiscountCurve[_nbGenerators];
    int index = 0;
    for (int loopgen = 0; loopgen < _nbGenerators; loopgen++) {
      double[] paramCurve = Arrays.copyOfRange(x, index, index + _generators[loopgen].getNumberOfParameter());
      index += _generators[loopgen].getNumberOfParameter();
      underlyingCurves[loopgen] = _generators[loopgen].generateCurve(name + "-" + loopgen, paramCurve);
    }
    return new YieldAndDiscountAddZeroSpreadCurve(name, _substract, underlyingCurves);
  }

  @Override
  public YieldAndDiscountCurve generateCurve(String name, YieldCurveBundle bundle, double[] parameters) {
    return generateCurve(name, parameters);
  }

  @Override
  public YieldAndDiscountCurve generateCurve(String name, MulticurveProviderInterface multicurve, double[] parameters) {
    return generateCurve(name, parameters);
  }

  /**
   * Create the final generators.
   * The relevant array of instrument is passed to each generator. For all the generator, except the first, the last instrument of the
   * previous generator is also passed as an indication of the previous part (can be used in "anchor" for example).
   * @param data The array of instrument used to construct the curve.
   * @return The final generator.
   */
  @Override
  public GeneratorYDCurve finalGenerator(Object data) {
    ArgumentChecker.isTrue(data instanceof InstrumentDerivative[], "data should be an array of InstrumentDerivative");
    InstrumentDerivative[] instruments = (InstrumentDerivative[]) data;
    ArgumentChecker.isTrue(instruments.length == _totalNbParameters, "The data should have the size prescribed by the _nbParameters");
    GeneratorYDCurve[] finalGenerator = new GeneratorYDCurve[_nbGenerators];
    int nbDataUsed = 0;
    InstrumentDerivative[] instruments0 = new InstrumentDerivative[_nbParameters[0]];
    System.arraycopy(instruments, 0, instruments0, 0, _nbParameters[0]);
    finalGenerator[0] = _generators[0].finalGenerator(instruments0);
    nbDataUsed += _nbParameters[0];
    for (int loopgen = 1; loopgen < _nbParameters.length; loopgen++) {
      InstrumentDerivative[] instrumentsCurrent = new InstrumentDerivative[_nbParameters[loopgen] + 1];
      System.arraycopy(instruments, nbDataUsed - 1, instrumentsCurrent, 0, _nbParameters[loopgen] + 1);
      finalGenerator[loopgen] = _generators[loopgen].finalGenerator(instrumentsCurrent);
      nbDataUsed += _nbParameters[loopgen];
    }
    return new GeneratorCurveAddYield(finalGenerator, _substract);
  }

  @Override
  public double[] initialGuess(double[] rates) {
    double[] guess = new double[rates.length];
    int nbDataUsed = 0;
    int nbParam = 0;
    for (int loopgen = 0; loopgen < _nbGenerators; loopgen++) {
      nbParam = _generators[loopgen].getNumberOfParameter();
      double[] tmp = new double[nbParam];
      System.arraycopy(rates, nbDataUsed, tmp, 0, nbParam);
      System.arraycopy(_generators[loopgen].initialGuess(tmp), 0, guess, nbDataUsed, nbParam);
      nbDataUsed += nbParam;
    }
    return guess;
  }

}
