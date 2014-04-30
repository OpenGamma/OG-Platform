/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.model.volatility.smile.fitting;

import java.util.BitSet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.Test;

import cern.jet.random.engine.MersenneTwister;
import cern.jet.random.engine.RandomEngine;

import com.opengamma.analytics.financial.model.volatility.smile.function.SVIFormulaData;
import com.opengamma.analytics.financial.model.volatility.smile.function.SVIVolatilityFunction;
import com.opengamma.analytics.financial.model.volatility.smile.function.VolatilityFunctionProvider;
import com.opengamma.util.test.TestGroup;

/**
 * Test.
 */
@Test(groups = TestGroup.UNIT)
public class SVIModelFitterTest extends SmileModelFitterTest<SVIFormulaData> {

  private static Logger LOGGER = LoggerFactory.getLogger(SVIModelFitterTest.class);

  private static final double A = 0.1;
  private static final double B = 0.3;
  private static double RHO = -0.5;
  private static double NU = 0.3;
  private static double M = 0.2;
  private static RandomEngine RANDOM = new MersenneTwister();

  public SVIModelFitterTest() {
    _chiSqEps = 1e-4;
  }

  @Override
  VolatilityFunctionProvider<SVIFormulaData> getModel() {
    return new SVIVolatilityFunction();
  }

  @Override
  SVIFormulaData getModelData() {
    return new SVIFormulaData(A, B, RHO, NU, M);
  }

  @Override
  SmileModelFitter<SVIFormulaData> getFitter(double forward, double[] strikes, double timeToExpiry, double[] impliedVols, double[] error, VolatilityFunctionProvider<SVIFormulaData> model) {
    return new SVIModelFitter(forward, strikes, timeToExpiry, impliedVols, error, model);
  }

  @Override
  double[][] getStartValues() {
    return new double[][] { {0.1, 0.1, 0.01, 0.01, 0.0 }, {0.05, 0.05, 0, 0.3, 0.2 }, {0.2, 0.1, 0.6, 0.1, 0.3 } };
  }

  @Override
  BitSet[] getFixedValues() {
    final BitSet[] fixed = new BitSet[3];
    fixed[0] = new BitSet();
    fixed[1] = new BitSet();
    fixed[1].set(4);
    fixed[2] = new BitSet();
    return fixed;
  }

  @Override
  Logger getlogger() {
    return LOGGER;
  }

  @Override
  double[] getRandomStartValues() {
    final double a = 0.5 * RANDOM.nextDouble();
    final double b = 0.5 * RANDOM.nextDouble();
    final double rho = 2 * RANDOM.nextDouble() - 1;
    final double nu = 0.5 * RANDOM.nextDouble();
    final double m = 0.5 * RANDOM.nextDouble() - 0.25;
    return new double[] {a, b, rho, nu, m };
  }

}
