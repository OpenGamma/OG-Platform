/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.curve.inflation.generator;

import java.util.Arrays;

import com.opengamma.analytics.financial.interestrate.InstrumentDerivative;
import com.opengamma.analytics.financial.model.interestrate.curve.PriceIndexCurve;
import com.opengamma.analytics.financial.model.interestrate.curve.PriceIndexCurveAddPriceIndexSpreadCurve;
import com.opengamma.analytics.financial.model.interestrate.curve.PriceIndexCurveSimple;
import com.opengamma.analytics.financial.provider.description.inflation.InflationProviderInterface;
import com.opengamma.util.ArgumentChecker;

/**
 * Store the details and generate the required curve. The curve is the sum (or difference) of two (or more) curves 
 * (operation on the price index): an existing curve referenced by its name and a new curve. 
 * The generated curve is a PriceIndexCurveAddPriceIndexSpreadCurve.
 */
public class GeneratorPriceIndexCurveAddPriceIndex extends GeneratorPriceIndexCurve {

  /**
   * The array of generators describing the different parts of the spread curve.
   */
  private final GeneratorPriceIndexCurve[] _generators;
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
   * @param substract If true, the rate of all curves, except the first one, will be subtracted from the first one. If false, all the rates are added.
   */
  public GeneratorPriceIndexCurveAddPriceIndex(GeneratorPriceIndexCurve[] generators, boolean substract) {
    ArgumentChecker.notNull(generators, "Generators");
    _generators = generators;
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
  public PriceIndexCurve generateCurve(String name, double[] x) {
    ArgumentChecker.notNull(name, "Name");
    ArgumentChecker.isTrue(x.length == getNumberOfParameter(), "Incorrect number of parameters");
    PriceIndexCurve[] underlyingCurves = new PriceIndexCurveSimple[_nbGenerators];
    int index = 0;
    for (int loopgen = 0; loopgen < _nbGenerators; loopgen++) {
      double[] paramCurve = Arrays.copyOfRange(x, index, index + _generators[loopgen].getNumberOfParameter());
      index += _generators[loopgen].getNumberOfParameter();
      underlyingCurves[loopgen] = _generators[loopgen].generateCurve(name + "-" + loopgen, paramCurve);
    }
    return new PriceIndexCurveAddPriceIndexSpreadCurve(name, _substract, underlyingCurves);
  }

  @Override
  public PriceIndexCurve generateCurve(String name, InflationProviderInterface inflation, double[] parameters) {
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
  public GeneratorPriceIndexCurve finalGenerator(Object data) {
    ArgumentChecker.isTrue(data instanceof InstrumentDerivative[], "data should be an array of InstrumentDerivative");
    InstrumentDerivative[] instruments = (InstrumentDerivative[]) data;
    GeneratorPriceIndexCurve[] finalGenerator = new GeneratorPriceIndexCurve[_nbGenerators];
    int nbDataUsed = 0;
    int nbParam = 0;
    for (int loopgen = 0; loopgen < _nbGenerators - 1; loopgen++) {
      nbParam = _generators[loopgen].getNumberOfParameter();
      InstrumentDerivative[] instrumentsLoop = new InstrumentDerivative[nbParam];
      System.arraycopy(instruments, nbDataUsed, instrumentsLoop, 0, nbParam);
      finalGenerator[loopgen] = _generators[loopgen].finalGenerator(instrumentsLoop);
      nbDataUsed += nbParam;
    }
    InstrumentDerivative[] instrumentsLast = new InstrumentDerivative[instruments.length - nbDataUsed + 1];
    instrumentsLast[0] = instruments[nbDataUsed - 1];
    // Implementation note: The anchor is the previous instrument.
    System.arraycopy(instruments, nbDataUsed, instrumentsLast, 1, instruments.length - nbDataUsed);
    finalGenerator[_nbGenerators - 1] = _generators[_nbGenerators - 1].finalGenerator(instrumentsLast);
    return new GeneratorPriceIndexCurveAddPriceIndex(finalGenerator, _substract);
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
