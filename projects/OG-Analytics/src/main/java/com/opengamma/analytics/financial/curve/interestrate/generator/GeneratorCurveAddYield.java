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
 * (operation on the continuously-compounded zero-coupon rates) produced by the array of generators.
 * The generated curve is a {@link YieldAndDiscountAddZeroSpreadCurve}.
 */
@SuppressWarnings("deprecation")
public class GeneratorCurveAddYield extends GeneratorYDCurve {

  /**
   * The array of generators describing the different parts of the spread curve.
   */
  private final GeneratorYDCurve[] _generators;
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
   * @param subtract If true, the rate of all curves, except the first one, will be subtracted from the first one. If false, all the rates are added.
   */
  public GeneratorCurveAddYield(final GeneratorYDCurve[] generators, final boolean subtract) {
    ArgumentChecker.notNull(generators, "Generators");
    _generators = generators;
    _nbGenerators = generators.length;
    _substract = subtract;
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
  public YieldAndDiscountCurve generateCurve(final String name, final double[] x) {
    ArgumentChecker.notNull(name, "Name");
    ArgumentChecker.isTrue(x.length == getNumberOfParameter(), "Incorrect number of parameters");
    final YieldAndDiscountCurve[] underlyingCurves = new YieldAndDiscountCurve[_nbGenerators];
    int index = 0;
    for (int loopgen = 0; loopgen < _nbGenerators; loopgen++) {
      final double[] paramCurve = Arrays.copyOfRange(x, index, index + _generators[loopgen].getNumberOfParameter());
      index += _generators[loopgen].getNumberOfParameter();
      underlyingCurves[loopgen] = _generators[loopgen].generateCurve(name + "-" + loopgen, paramCurve);
    }
    return new YieldAndDiscountAddZeroSpreadCurve(name, _substract, underlyingCurves);
  }

  /**
   * {@inheritDoc}
   * @deprecated {@link YieldCurveBundle} is deprecated.
   */
  @Deprecated
  @Override
  public YieldAndDiscountCurve generateCurve(final String name, final YieldCurveBundle bundle, final double[] parameters) {
    return generateCurve(name, parameters);
  }

  @Override
  public YieldAndDiscountCurve generateCurve(final String name, final MulticurveProviderInterface bundle, final double[] parameters) {
    return generateCurve(name, parameters);
  }

  /**
   * All generator but the last should have a known number of parameters.
   * The number of data corresponding to each known generator is eliminated and only the last part is used to create the final generator version.
   * If several generators had a unknown number of parameters, it would be unclear which instrument correspond to which generator.
   * In the last generator, the previous instrument is passed to create the anchor.
   * @param data The array of instrument used to construct the curve.
   * @return The final generator.
   */
  @Override
  public GeneratorYDCurve finalGenerator(final Object data) {
    ArgumentChecker.isTrue(data instanceof InstrumentDerivative[], "data should be an array of InstrumentDerivative");
    final InstrumentDerivative[] instruments = (InstrumentDerivative[]) data;
    final GeneratorYDCurve[] finalGenerator = new GeneratorYDCurve[_nbGenerators];
    int nbDataUsed = 0;
    int nbParam = 0;
    for (int loopgen = 0; loopgen < _nbGenerators - 1; loopgen++) {
      nbParam = _generators[loopgen].getNumberOfParameter();
      final InstrumentDerivative[] instrumentsLoop = new InstrumentDerivative[nbParam];
      System.arraycopy(instruments, nbDataUsed, instrumentsLoop, 0, nbParam);
      finalGenerator[loopgen] = _generators[loopgen].finalGenerator(instrumentsLoop);
      nbDataUsed += nbParam;
    }
    final InstrumentDerivative[] instrumentsLast = new InstrumentDerivative[instruments.length - nbDataUsed + 1];
    instrumentsLast[0] = instruments[nbDataUsed - 1];
    // Implementation note: The anchor is the previous instrument.
    System.arraycopy(instruments, nbDataUsed, instrumentsLast, 1, instruments.length - nbDataUsed);
    finalGenerator[_nbGenerators - 1] = _generators[_nbGenerators - 1].finalGenerator(instrumentsLast);
    return new GeneratorCurveAddYield(finalGenerator, _substract);
  }

  @Override
  public double[] initialGuess(final double[] rates) {
    final double[] guess = new double[rates.length];
    int nbDataUsed = 0;
    int nbParam = 0;
    for (int loopgen = 0; loopgen < _nbGenerators; loopgen++) {
      nbParam = _generators[loopgen].getNumberOfParameter();
      final double[] tmp = new double[nbParam];
      System.arraycopy(rates, nbDataUsed, tmp, 0, nbParam);
      System.arraycopy(_generators[loopgen].initialGuess(tmp), 0, guess, nbDataUsed, nbParam);
      nbDataUsed += nbParam;
    }
    return guess;
  }

}
