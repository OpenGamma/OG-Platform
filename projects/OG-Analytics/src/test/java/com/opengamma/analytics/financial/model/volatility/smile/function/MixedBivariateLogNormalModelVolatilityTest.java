/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.model.volatility.smile.function;

import org.testng.annotations.Test;

import com.opengamma.analytics.financial.model.option.pricing.analytic.formula.EuropeanVanillaOption;

/**
 * 
 */
public class MixedBivariateLogNormalModelVolatilityTest {

  //  final double[] constWeights = {0.6, 0.3, 0.1 };
  //  final double[] sigmasX = {0.1, 0.5, 0.9 };
  //  final double[] sigmasY = {0.2, 0.7, 1.2 };
  //  final double[] relativePartialForwardsX = {1., 1., 1. };
  //  final double[] relativePartialForwardsY = {1., 1., 1. };
  //  final double[] rhos = {0.4, 0.5, 0.6 };

  //  final double[] constWeights = {0.6, 0.3, 0.1 };
  //  final double[] sigmasX = {0.3, 0.4, 0.5 };
  //  final double[] sigmasY = {0.2, 0.6, 0.9 };
  //  final double[] relativePartialForwardsX = {1., 1., 1. };
  //  final double[] relativePartialForwardsY = {1., 1., 1. };
  //  final double[] rhos = {-0.5, 0.5, 0.9 };

  final double[] constWeights = {0.8, 0.2 };
  final double[] sigmasX = {0.3, 0.4 };
  final double[] sigmasY = {0.1, 0.2 };
  final double[] relativePartialForwardsX = {1.1, 0.6 };
  final double[] relativePartialForwardsY = {1.0, 1.0 };
  final double[] rhos = {0.1, 0.8 };

  double[] weights = constWeights;

  final MixedBivariateLogNormalModelVolatility objZ = new MixedBivariateLogNormalModelVolatility(weights, sigmasX,
      sigmasY, relativePartialForwardsX, relativePartialForwardsY, rhos);
  final MixedLogNormalModelData objX = new MixedLogNormalModelData(weights, sigmasX, relativePartialForwardsX);
  final MixedLogNormalModelData objY = new MixedLogNormalModelData(weights, sigmasY, relativePartialForwardsY);

  // final MixedBivariateLogNormalModelVolatility objZ = new MixedBivariateLogNormalModelVolatility(weights, sigmasX,
  //      sigmasY, rhos);
  //  final MixedLogNormalModelData objX = new MixedLogNormalModelData(constWeights, sigmasX);
  //  final MixedLogNormalModelData objY = new MixedLogNormalModelData(constWeights, sigmasY);

  final double forwardX = 1.1;
  final double forwardY = 0.9;
  final double forwardZ = forwardX / forwardY;
  final double timeToExpiry = 0.6;
  final MixedLogNormalVolatilityFunction volfunc = MixedLogNormalVolatilityFunction.getInstance();

  @Test
      (enabled = false)
      public void printTest() {

    for (int i = 0; i < 101; i++) {
      double k = forwardZ * (0.5 + 1. * i / 100.);
      final EuropeanVanillaOption option = new EuropeanVanillaOption(k, timeToExpiry, true);
      final double vol1 = objZ.getImpliedVolatilityZ(option, forwardZ);
      final double vol2 = volfunc.getVolatility(option, forwardX, objX);
      final double vol3 = volfunc.getVolatility(option, forwardY, objY);
      System.out.println(k + "\t" + vol1 + "\t" + vol2 + "\t" + vol3);
    }
  }

  @Test
      (enabled = false)
      public void printTestStrikeTime() {
    for (int j = 0; j < 51; j++) {
      double time = 0.5 * (0.5 + 2. * j / 100.);
      for (int i = 0; i < 51; i++) {
        final double k = forwardZ * (0.5 + 1. * i / 100.);
        final EuropeanVanillaOption option = new EuropeanVanillaOption(k, time, true);
        final double vol1 = objZ.getImpliedVolatilityZ(option, forwardZ);
        System.out.println(k + "\t" + time + "\t" + vol1);
      }
    }
  }

}
