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

import com.opengamma.analytics.financial.model.volatility.smile.function.HestonModelData;
import com.opengamma.analytics.financial.model.volatility.smile.function.HestonVolatilityFunction;
import com.opengamma.analytics.financial.model.volatility.smile.function.VolatilityFunctionProvider;
import com.opengamma.util.test.TestGroup;

/**
 * Test.
 */
@Test(groups = TestGroup.UNIT)
public class HestonModelFitterTest extends SmileModelFitterTest<HestonModelData> {

  private static final double KAPPA = 0.1;
  private static final double THETA = 0.2;
  private static final double VOL0 = 0.2;
  private static double OMEGA = 0.7;
  private static double RHO = -0.9;
  private static Logger LOGGER = LoggerFactory.getLogger(HestonModelFitterTest.class);
  private static RandomEngine RANDOM = new MersenneTwister();

  public HestonModelFitterTest() {
    _paramValueEps = 1e-5;
  }

  @Override
  Logger getlogger() {
    return LOGGER;
  }

  @Override
  VolatilityFunctionProvider<HestonModelData> getModel() {
    return new HestonVolatilityFunction();
  }

  @Override
  HestonModelData getModelData() {
    return new HestonModelData(KAPPA, THETA, VOL0, OMEGA, RHO);
  }

  @Override
  SmileModelFitter<HestonModelData> getFitter(double forward, double[] strikes, double timeToExpiry, double[] impliedVols, double[] error, VolatilityFunctionProvider<HestonModelData> model) {
    return new HestonModelFitter(forward, strikes, timeToExpiry, impliedVols, error, model);
  }

  @Override
  double[][] getStartValues() {
    return new double[][] { {0.3, 0.1, 0.2, 0.4, 0.3 }, {0.05, 0.2, 0.1, 1, -0.3 } };
  }

  @Override
  BitSet[] getFixedValues() {
    BitSet[] fixed = new BitSet[2];
    fixed[0] = new BitSet();
    fixed[0].set(2);
    fixed[1] = new BitSet();
    return fixed;
  }

  @Override
  double[] getRandomStartValues() {
    double kappa = RANDOM.nextDouble() * 2.0;
    double theta =RANDOM.nextDouble();
    double vol0 = RANDOM.nextDouble();
    double omega = 1.5 * RANDOM.nextDouble();
    double rho = 2 * RANDOM.nextDouble() - 1;
    return new double[] {kappa, theta, vol0, omega, rho };
  }

}
