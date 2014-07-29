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

import com.opengamma.analytics.financial.model.volatility.smile.function.SABRFormulaData;
import com.opengamma.analytics.financial.model.volatility.smile.function.SABRHaganVolatilityFunction;
import com.opengamma.analytics.financial.model.volatility.smile.function.VolatilityFunctionProvider;
import com.opengamma.util.test.TestGroup;

/**
 * Test.
 */
@Test(groups = TestGroup.UNIT)
public class SABRModelFitter2Test extends SmileModelFitterTest<SABRFormulaData> {

  private static final double ALPHA = 0.05;
  private static final double BETA = 0.5;
  private static double RHO = -0.3;
  private static double NU = 0.2;
  private static Logger LOGGER = LoggerFactory.getLogger(SABRModelFitter2Test.class);
  private static RandomEngine RANDOM = new MersenneTwister();

  public SABRModelFitter2Test() {
    _chiSqEps = 1e-4;
  }

  @Override
  VolatilityFunctionProvider<SABRFormulaData> getModel() {
    return new SABRHaganVolatilityFunction();
  }

  @Override
  SABRFormulaData getModelData() {
    return new SABRFormulaData(ALPHA, BETA, RHO, NU);
  }

  @Override
  SmileModelFitter<SABRFormulaData> getFitter(double forward, double[] strikes, double timeToExpiry, double[] impliedVols, double[] error, VolatilityFunctionProvider<SABRFormulaData> model) {
    return new SABRModelFitter(forward, strikes, timeToExpiry, impliedVols, error, model);
  }

  @Override
  double[][] getStartValues() {
    return new double[][] { {0.1, 0.7, 0.0, 0.3 }, {0.01, 0.95, 0.9, 0.4 }, {0.01, 0.5, -0.7, 0.6 } };
  }

  @Override
  Logger getlogger() {
    return LOGGER;
  }

  @Override
  BitSet[] getFixedValues() {
    final BitSet[] fixed = new BitSet[3];
    fixed[0] = new BitSet();
    fixed[1] = new BitSet();
    fixed[2] = new BitSet();
    fixed[2].set(1);
    return fixed;
  }

  @Override
  double[] getRandomStartValues() {
    final double alpha = 0.1 + 0.4 * RANDOM.nextDouble();
    final double beta = RANDOM.nextDouble();
    final double rho = 2 * RANDOM.nextDouble() - 1;
    final double nu = 1.5 * RANDOM.nextDouble();

    return new double[] {alpha, beta, rho, nu };
  }

}
