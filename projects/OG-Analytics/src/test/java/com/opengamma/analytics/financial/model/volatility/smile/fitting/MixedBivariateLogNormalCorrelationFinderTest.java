/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.model.volatility.smile.fitting;

import java.util.Random;

import org.testng.annotations.Test;

import com.opengamma.analytics.financial.model.option.pricing.analytic.formula.EuropeanVanillaOption;
import com.opengamma.analytics.financial.model.volatility.smile.function.MixedBivariateLogNormalModelVolatility;

/**
 * 
 */
public class MixedBivariateLogNormalCorrelationFinderTest {

  @Test
   (enabled = false)
  public void printTest() {

    final int nNormals = 2;
    final double[] weights = {0.37640291143644194, 0.623597088563558 };
    final double[] sigmasX = {0.06354423944935964, 0.10361640830108385 };
    final double[] sigmasY = {0.03794692512570427, 0.07364831519062784 };
    final double[] relativePartialForwardsX = {0.9997760696332433, 1.0001351642647986 };
    final double[] relativePartialForwardsY = {1.0002606663941294, 0.999842661886235 };

    double[] rhosGuess = new double[nNormals];

    final double forwardX = 1.3364015890354652;
    final double forwardY = 1.5992978529959616;
    final double fwdZ = forwardX / forwardY;
    final double timeToExpiry = 0.019178082191780823;

    final Random randObj = new Random();

    for (int i = 0; i < nNormals; ++i) {
      rhosGuess[i] = randObj.nextDouble();
    }

    final double[] dataStrikes = {0.8276533748061506, 0.830544004818981, 0.8356246758244018, 0.8408571903798175, 0.8438972913060586 };
    final double[] dataVols = {0.0668, 0.0653, 0.06465, 0.0668, 0.0686 };

    MixedBivariateLogNormalCorrelationFinder fitter = new MixedBivariateLogNormalCorrelationFinder(rhosGuess, dataStrikes, dataVols, timeToExpiry, weights, sigmasX, sigmasY, relativePartialForwardsX,
        relativePartialForwardsY, forwardX, forwardY);

    boolean fitDone = false;

    while (fitDone == false) {

      fitter.doFit();
      rhosGuess = fitter.getParams();
      System.out.println("\n");
      System.out.println("inintial sq: " + fitter.getInitialSq());

      if (fitter.getFinalSq() <= fitter.getInitialSq() * 1e-4) {
        fitDone = true;
        System.out.println("final sq: " + fitter.getFinalSq());
      } else {
        for (int i = 0; i < nNormals; ++i) {
          rhosGuess[i] = randObj.nextDouble();
        }
        fitter = new MixedBivariateLogNormalCorrelationFinder(rhosGuess, dataStrikes, dataVols, timeToExpiry, weights, sigmasX, sigmasY, relativePartialForwardsX,
            relativePartialForwardsY, forwardX, forwardY);
      }

    }

    System.out.println("\n");

    rhosGuess = fitter.getParams();

    final MixedBivariateLogNormalModelVolatility objZ = new MixedBivariateLogNormalModelVolatility(weights, sigmasX,
        sigmasY, relativePartialForwardsX, relativePartialForwardsY, rhosGuess);

    double[] ansVolsZ = new double[100];
    for (int i = 0; i < 100; i++) {
      double k = fwdZ * (0.97 + .6 * i / 1000.);
      final EuropeanVanillaOption option = new EuropeanVanillaOption(k, timeToExpiry, true);
      ansVolsZ[i] = objZ.getImpliedVolatilityZ(option, fwdZ);
      System.out.println(k + "\t" + ansVolsZ[i]);
    }

    System.out.println("\n");
    System.out.println(fwdZ);

    System.out.println("\n");
    for (int i = 0; i < nNormals; ++i) {
      System.out.println(rhosGuess[i]);
    }

  }

  //  @Test
  //   //   (enabled = false)
  //      public void derivativeTest() {
  //
  //    final int nNormals = 2;
  //    final double[] weights = {0.37640291143644194, 0.623597088563558 };
  //    final double[] sigmasX = {0.06354423944935964, 0.10361640830108385 };
  //    final double[] sigmasY = {0.03794692512570427, 0.07364831519062784 };
  //    final double[] relativePartialForwardsX = {0.9997760696332433, 1.0001351642647986 };
  //    final double[] relativePartialForwardsY = {1.0002606663941294, 0.999842661886235 };
  //
  //    double[] rhosGuess = new double[nNormals];
  //
  //    final double forwardX = 1.3364015890354652;
  //    final double forwardY = 1.5992978529959616;
  //    final double fwdZ = forwardX / forwardY;
  //    final double timeToExpiry = 0.019178082191780823;
  //
  //    final Random randObj = new Random();
  //
  //    for (int i = 0; i < nNormals; ++i) {
  //      rhosGuess[i] = randObj.nextDouble();
  //    }
  //
  //    final double[] dataStrikes = {0.8276533748061506, 0.830544004818981, 0.8356246758244018, 0.8408571903798175, 0.8438972913060586 };
  //    final double[] dataVols = {0.0668, 0.0653, 0.06465, 0.0668, 0.0686 };
  //    final int nDataPts = dataStrikes.length;
  //
  //    MixedBivariateLogNormalCorrelationFinder fitter = new MixedBivariateLogNormalCorrelationFinder(rhosGuess, dataStrikes, dataVols, timeToExpiry, weights, sigmasX, sigmasY, relativePartialForwardsX,
  //        relativePartialForwardsY, forwardX, forwardY);
  //
  //    final double[][] derExact = fitter.exactFunctionDerivative(rhosGuess);
  //
  //    final double[][] derFin = fitter.exactFunctionDerivativeFin(rhosGuess);
  //
  //    System.out.println("\n");
  //    for (int j = 0; j < nDataPts; ++j)
  //      for (int i = 0; i < nNormals; ++i) {
  //        System.out.println(derExact[j][i] + "\t" + derFin[j][i] + "\t" + (derExact[j][i] - derFin[j][i]));
  //      }
  //
  //  }
}
