/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.model.volatility.smile.fitting;

import static org.testng.Assert.assertEquals;

import java.util.Random;

import org.testng.annotations.Test;

import com.opengamma.analytics.financial.model.option.pricing.analytic.formula.EuropeanVanillaOption;
import com.opengamma.analytics.financial.model.volatility.smile.function.MixedBivariateLogNormalModelVolatility;
import com.opengamma.util.test.TestGroup;

/**
 * Test.
 */
@Test(groups = TestGroup.UNIT)
public class MixedBivariateLogNormalCorrelationFinderTest {

  private static final double INF = 1. / 0.;

  /**
   *  EPS_1 =EPS_2 = 1.E-14 should be chosen for this test
   */
  @Test
  public void recoveryTest() {

    final int nNormals = 2;
    final double[] weights = {0.37640291143644194, 0.623597088563558 };
    final double[] sigmasX = {0.06354423944935964, 0.10361640830108385 };
    final double[] sigmasY = {0.03794692512570427, 0.07364831519062784 };
    final double[] relativePartialForwardsX = {0.9997760696332433, 1.0001351642647986 };
    final double[] relativePartialForwardsY = {1.0002606663941294, 0.999842661886235 };

    final double[] rhosTrue = new double[] {0.66, 0.71 };

    double[] rhosGuess = new double[] {0.6, 0.6 };

    final double forwardX = 1.3364015890354652;
    final double forwardY = 1.5992978529959616;
    final double forwardZ = forwardX / forwardY;
    final double timeToExpiry = 0.019178082191780823;

    final double[] dataStrikes = {0.8276533748061506, 0.830544004818981, 0.8356246758244018, 0.8408571903798175, 0.8438972913060586 };
    final int nData = dataStrikes.length;
    final double[] dataVols = new double[nData];

    final MixedBivariateLogNormalModelVolatility objTrueZ = new MixedBivariateLogNormalModelVolatility(weights, sigmasX, sigmasY, relativePartialForwardsX, relativePartialForwardsY, rhosTrue);

    for (int i = 0; i < nData; ++i) {
      final EuropeanVanillaOption option = new EuropeanVanillaOption(dataStrikes[i], timeToExpiry, true);
      dataVols[i] = objTrueZ.getImpliedVolatilityZ(option, forwardZ);
    }

    final MixedBivariateLogNormalCorrelationFinder fitter = new MixedBivariateLogNormalCorrelationFinder();

    fitter.doFit(rhosGuess, dataStrikes, dataVols, timeToExpiry, weights, sigmasX, sigmasY, relativePartialForwardsX,
        relativePartialForwardsY, forwardX, forwardY);

    rhosGuess = fitter.getParams();

    final MixedBivariateLogNormalModelVolatility objFitZ = new MixedBivariateLogNormalModelVolatility(weights, sigmasX,
        sigmasY, relativePartialForwardsX, relativePartialForwardsY, rhosGuess);

    final double[] fitVolsZ = new double[100];
    final double[] trueVolsZ = new double[100];
    for (int i = 0; i < 100; i++) {
      final double k = forwardZ * (0.97 + .6 * i / 1000.);
      final EuropeanVanillaOption option = new EuropeanVanillaOption(k, timeToExpiry, true);
      fitVolsZ[i] = objFitZ.getImpliedVolatilityZ(option, forwardZ);
      trueVolsZ[i] = objTrueZ.getImpliedVolatilityZ(option, forwardZ);
      assertEquals(fitVolsZ[i], trueVolsZ[i], Math.abs((trueVolsZ[0] + trueVolsZ[99]) / 2.) * 1e-10);
    }

    for (int i = 0; i < nNormals; ++i) {
      assertEquals(rhosGuess[i], rhosTrue[i], Math.abs((rhosTrue[0] + rhosTrue[nNormals - 1]) / 2.) * 1e-11);
    }

  }

  //  /**
  //   *
  //   */
  //  @Test
  //  public void NullTest() {
  //
  //    final double[] weights = {0.37640291143644194, 0.623597088563558 };
  //    final double[] sigmasX = {0.06354423944935964, 0.10361640830108385 };
  //    final double[] sigmasY = {0.03794692512570427, 0.07364831519062784 };
  //    final double[] relativePartialForwardsX = {0.9997760696332433, 1.0001351642647986 };
  //    final double[] relativePartialForwardsY = {1.0002606663941294, 0.999842661886235 };
  //
  //    final double forwardX = 1.3364015890354652;
  //    final double forwardY = 1.5992978529959616;
  //    final double timeToExpiry = 0.019178082191780823;
  //
  //    final double[] dataStrikes = {0.8276533748061506, 0.830544004818981, 0.8356246758244018, 0.8408571903798175, 0.8438972913060586 };
  //    final double[] dataVols = {0.0668, 0.0653, 0.06465, 0.0668, 0.0686 };
  //
  //    double[] rhosGuess = new double[] {0.65, 0.65 };
  //
  //    MixedBivariateLogNormalCorrelationFinder fitter = new MixedBivariateLogNormalCorrelationFinder();
  //
  //    fitter.doFit(rhosGuess, dataStrikes, dataVols, timeToExpiry, weights, sigmasX, sigmasY, relativePartialForwardsX,
  //        relativePartialForwardsY, forwardX, forwardY);
  //  }

  /**
   * 
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void nullweightsTest() {

    final double[] weights = null;
    final double[] sigmasX = {0.06354423944935964, 0.10361640830108385 };
    final double[] sigmasY = {0.03794692512570427, 0.07364831519062784 };
    final double[] relativePartialForwardsX = {0.9997760696332433, 1.0001351642647986 };
    final double[] relativePartialForwardsY = {1.0002606663941294, 0.999842661886235 };

    final double forwardX = 1.3364015890354652;
    final double forwardY = 1.5992978529959616;
    final double timeToExpiry = 0.019178082191780823;

    final double[] dataStrikes = {0.8276533748061506, 0.830544004818981, 0.8356246758244018, 0.8408571903798175, 0.8438972913060586 };
    final double[] dataVols = {0.0668, 0.0653, 0.06465, 0.0668, 0.0686 };

    final double[] rhosGuess = new double[] {0.65, 0.65 };

    final MixedBivariateLogNormalCorrelationFinder fitter = new MixedBivariateLogNormalCorrelationFinder();

    fitter.doFit(rhosGuess, dataStrikes, dataVols, timeToExpiry, weights, sigmasX, sigmasY, relativePartialForwardsX,
        relativePartialForwardsY, forwardX, forwardY);
  }

  /**
   * 
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void nullsigmasXTest() {

    final double[] weights = {0.37640291143644194, 0.623597088563558 };
    final double[] sigmasX = null;
    final double[] sigmasY = {0.03794692512570427, 0.07364831519062784 };
    final double[] relativePartialForwardsX = {0.9997760696332433, 1.0001351642647986 };
    final double[] relativePartialForwardsY = {1.0002606663941294, 0.999842661886235 };

    final double forwardX = 1.3364015890354652;
    final double forwardY = 1.5992978529959616;
    final double timeToExpiry = 0.019178082191780823;

    final double[] dataStrikes = {0.8276533748061506, 0.830544004818981, 0.8356246758244018, 0.8408571903798175, 0.8438972913060586 };
    final double[] dataVols = {0.0668, 0.0653, 0.06465, 0.0668, 0.0686 };

    final double[] rhosGuess = new double[] {0.65, 0.65 };

    final MixedBivariateLogNormalCorrelationFinder fitter = new MixedBivariateLogNormalCorrelationFinder();

    fitter.doFit(rhosGuess, dataStrikes, dataVols, timeToExpiry, weights, sigmasX, sigmasY, relativePartialForwardsX,
        relativePartialForwardsY, forwardX, forwardY);
  }

  /**
   * 
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void nullsigmasYTest() {

    final double[] weights = {0.37640291143644194, 0.623597088563558 };
    final double[] sigmasX = {0.06354423944935964, 0.10361640830108385 };
    final double[] sigmasY = null;
    final double[] relativePartialForwardsX = {0.9997760696332433, 1.0001351642647986 };
    final double[] relativePartialForwardsY = {1.0002606663941294, 0.999842661886235 };

    final double forwardX = 1.3364015890354652;
    final double forwardY = 1.5992978529959616;
    final double timeToExpiry = 0.019178082191780823;

    final double[] dataStrikes = {0.8276533748061506, 0.830544004818981, 0.8356246758244018, 0.8408571903798175, 0.8438972913060586 };
    final double[] dataVols = {0.0668, 0.0653, 0.06465, 0.0668, 0.0686 };

    final double[] rhosGuess = new double[] {0.65, 0.65 };

    final MixedBivariateLogNormalCorrelationFinder fitter = new MixedBivariateLogNormalCorrelationFinder();

    fitter.doFit(rhosGuess, dataStrikes, dataVols, timeToExpiry, weights, sigmasX, sigmasY, relativePartialForwardsX,
        relativePartialForwardsY, forwardX, forwardY);
  }

  /**
   * 
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void nullrelativePartialForwardsXTest() {

    final double[] weights = {0.37640291143644194, 0.623597088563558 };
    final double[] sigmasX = {0.06354423944935964, 0.10361640830108385 };
    final double[] sigmasY = {0.03794692512570427, 0.07364831519062784 };
    final double[] relativePartialForwardsX = null;
    final double[] relativePartialForwardsY = {1.0002606663941294, 0.999842661886235 };

    final double forwardX = 1.3364015890354652;
    final double forwardY = 1.5992978529959616;
    final double timeToExpiry = 0.019178082191780823;

    final double[] dataStrikes = {0.8276533748061506, 0.830544004818981, 0.8356246758244018, 0.8408571903798175, 0.8438972913060586 };
    final double[] dataVols = {0.0668, 0.0653, 0.06465, 0.0668, 0.0686 };

    final double[] rhosGuess = new double[] {0.65, 0.65 };

    final MixedBivariateLogNormalCorrelationFinder fitter = new MixedBivariateLogNormalCorrelationFinder();

    fitter.doFit(rhosGuess, dataStrikes, dataVols, timeToExpiry, weights, sigmasX, sigmasY, relativePartialForwardsX,
        relativePartialForwardsY, forwardX, forwardY);
  }

  /**
   * 
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void nullrelativePartialForwardsYTest() {

    final double[] weights = {0.37640291143644194, 0.623597088563558 };
    final double[] sigmasX = {0.06354423944935964, 0.10361640830108385 };
    final double[] sigmasY = {0.03794692512570427, 0.07364831519062784 };
    final double[] relativePartialForwardsX = {0.9997760696332433, 1.0001351642647986 };
    final double[] relativePartialForwardsY = null;

    final double forwardX = 1.3364015890354652;
    final double forwardY = 1.5992978529959616;
    final double timeToExpiry = 0.019178082191780823;

    final double[] dataStrikes = {0.8276533748061506, 0.830544004818981, 0.8356246758244018, 0.8408571903798175, 0.8438972913060586 };
    final double[] dataVols = {0.0668, 0.0653, 0.06465, 0.0668, 0.0686 };

    final double[] rhosGuess = new double[] {0.65, 0.65 };

    final MixedBivariateLogNormalCorrelationFinder fitter = new MixedBivariateLogNormalCorrelationFinder();

    fitter.doFit(rhosGuess, dataStrikes, dataVols, timeToExpiry, weights, sigmasX, sigmasY, relativePartialForwardsX,
        relativePartialForwardsY, forwardX, forwardY);
  }

  /**
   * 
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void nulldataStrikesTest() {

    final double[] weights = {0.37640291143644194, 0.623597088563558 };
    final double[] sigmasX = {0.06354423944935964, 0.10361640830108385 };
    final double[] sigmasY = {0.03794692512570427, 0.07364831519062784 };
    final double[] relativePartialForwardsX = {0.9997760696332433, 1.0001351642647986 };
    final double[] relativePartialForwardsY = {1.0002606663941294, 0.999842661886235 };

    final double forwardX = 1.3364015890354652;
    final double forwardY = 1.5992978529959616;
    final double timeToExpiry = 0.019178082191780823;

    final double[] dataStrikes = null;
    final double[] dataVols = {0.0668, 0.0653, 0.06465, 0.0668, 0.0686 };

    final double[] rhosGuess = new double[] {0.65, 0.65 };

    final MixedBivariateLogNormalCorrelationFinder fitter = new MixedBivariateLogNormalCorrelationFinder();

    fitter.doFit(rhosGuess, dataStrikes, dataVols, timeToExpiry, weights, sigmasX, sigmasY, relativePartialForwardsX,
        relativePartialForwardsY, forwardX, forwardY);
  }

  /**
   * 
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void nulldataVolsTest() {

    final double[] weights = {0.37640291143644194, 0.623597088563558 };
    final double[] sigmasX = {0.06354423944935964, 0.10361640830108385 };
    final double[] sigmasY = {0.03794692512570427, 0.07364831519062784 };
    final double[] relativePartialForwardsX = {0.9997760696332433, 1.0001351642647986 };
    final double[] relativePartialForwardsY = {1.0002606663941294, 0.999842661886235 };

    final double forwardX = 1.3364015890354652;
    final double forwardY = 1.5992978529959616;
    final double timeToExpiry = 0.019178082191780823;

    final double[] dataStrikes = {0.8276533748061506, 0.830544004818981, 0.8356246758244018, 0.8408571903798175, 0.8438972913060586 };
    final double[] dataVols = null;

    final double[] rhosGuess = new double[] {0.65, 0.65 };

    final MixedBivariateLogNormalCorrelationFinder fitter = new MixedBivariateLogNormalCorrelationFinder();

    fitter.doFit(rhosGuess, dataStrikes, dataVols, timeToExpiry, weights, sigmasX, sigmasY, relativePartialForwardsX,
        relativePartialForwardsY, forwardX, forwardY);
  }

  /**
   * 
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void wrongLengthsigmasXTest() {

    final double[] weights = {0.37640291143644194, 0.623597088563558 };
    final double[] sigmasX = {0.06354423944935964, 0.10361640830108385, 0.2 };
    final double[] sigmasY = {0.03794692512570427, 0.07364831519062784 };
    final double[] relativePartialForwardsX = {0.9997760696332433, 1.0001351642647986 };
    final double[] relativePartialForwardsY = {1.0002606663941294, 0.999842661886235 };

    final double forwardX = 1.3364015890354652;
    final double forwardY = 1.5992978529959616;
    final double timeToExpiry = 0.019178082191780823;

    final double[] dataStrikes = {0.8276533748061506, 0.830544004818981, 0.8356246758244018, 0.8408571903798175, 0.8438972913060586 };
    final double[] dataVols = {0.0668, 0.0653, 0.06465, 0.0668, 0.0686 };

    final double[] rhosGuess = new double[] {0.65, 0.65 };

    final MixedBivariateLogNormalCorrelationFinder fitter = new MixedBivariateLogNormalCorrelationFinder();

    fitter.doFit(rhosGuess, dataStrikes, dataVols, timeToExpiry, weights, sigmasX, sigmasY, relativePartialForwardsX,
        relativePartialForwardsY, forwardX, forwardY);
  }

  /**
   * 
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void wrongLengthsigmasYTest() {

    final double[] weights = {0.37640291143644194, 0.623597088563558 };
    final double[] sigmasX = {0.06354423944935964, 0.10361640830108385 };
    final double[] sigmasY = {0.03794692512570427, 0.07364831519062784, 0.1 };
    final double[] relativePartialForwardsX = {0.9997760696332433, 1.0001351642647986 };
    final double[] relativePartialForwardsY = {1.0002606663941294, 0.999842661886235 };

    final double forwardX = 1.3364015890354652;
    final double forwardY = 1.5992978529959616;
    final double timeToExpiry = 0.019178082191780823;

    final double[] dataStrikes = {0.8276533748061506, 0.830544004818981, 0.8356246758244018, 0.8408571903798175, 0.8438972913060586 };
    final double[] dataVols = {0.0668, 0.0653, 0.06465, 0.0668, 0.0686 };

    final double[] rhosGuess = new double[] {0.65, 0.65 };

    final MixedBivariateLogNormalCorrelationFinder fitter = new MixedBivariateLogNormalCorrelationFinder();

    fitter.doFit(rhosGuess, dataStrikes, dataVols, timeToExpiry, weights, sigmasX, sigmasY, relativePartialForwardsX,
        relativePartialForwardsY, forwardX, forwardY);
  }

  /**
   * 
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void wrongLengthrelativePartialForwardsXTest() {

    final double[] weights = {0.37640291143644194, 0.623597088563558 };
    final double[] sigmasX = {0.06354423944935964, 0.10361640830108385 };
    final double[] sigmasY = {0.03794692512570427, 0.07364831519062784 };
    final double[] relativePartialForwardsX = {0.9997760696332433, 1.0001351642647986, 0.1 };
    final double[] relativePartialForwardsY = {1.0002606663941294, 0.999842661886235 };

    final double forwardX = 1.3364015890354652;
    final double forwardY = 1.5992978529959616;
    final double timeToExpiry = 0.019178082191780823;

    final double[] dataStrikes = {0.8276533748061506, 0.830544004818981, 0.8356246758244018, 0.8408571903798175, 0.8438972913060586 };
    final double[] dataVols = {0.0668, 0.0653, 0.06465, 0.0668, 0.0686 };

    final double[] rhosGuess = new double[] {0.65, 0.65 };

    final MixedBivariateLogNormalCorrelationFinder fitter = new MixedBivariateLogNormalCorrelationFinder();

    fitter.doFit(rhosGuess, dataStrikes, dataVols, timeToExpiry, weights, sigmasX, sigmasY, relativePartialForwardsX,
        relativePartialForwardsY, forwardX, forwardY);
  }

  /**
   * 
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void wrongLengthweightsrelativePartialForwardsYTest() {

    final double[] weights = {0.37640291143644194, 0.623597088563558 };
    final double[] sigmasX = {0.06354423944935964, 0.10361640830108385 };
    final double[] sigmasY = {0.03794692512570427, 0.07364831519062784 };
    final double[] relativePartialForwardsX = {0.9997760696332433, 1.0001351642647986 };
    final double[] relativePartialForwardsY = {1.0002606663941294, 0.999842661886235, 0.1 };

    final double forwardX = 1.3364015890354652;
    final double forwardY = 1.5992978529959616;
    final double timeToExpiry = 0.019178082191780823;

    final double[] dataStrikes = {0.8276533748061506, 0.830544004818981, 0.8356246758244018, 0.8408571903798175, 0.8438972913060586 };
    final double[] dataVols = {0.0668, 0.0653, 0.06465, 0.0668, 0.0686 };

    final double[] rhosGuess = new double[] {0.65, 0.65 };

    final MixedBivariateLogNormalCorrelationFinder fitter = new MixedBivariateLogNormalCorrelationFinder();

    fitter.doFit(rhosGuess, dataStrikes, dataVols, timeToExpiry, weights, sigmasX, sigmasY, relativePartialForwardsX,
        relativePartialForwardsY, forwardX, forwardY);
  }

  /**
   * 
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void wrongDataLengthTest() {

    final double[] weights = {0.37640291143644194, 0.623597088563558 };
    final double[] sigmasX = {0.06354423944935964, 0.10361640830108385 };
    final double[] sigmasY = {0.03794692512570427, 0.07364831519062784 };
    final double[] relativePartialForwardsX = {0.9997760696332433, 1.0001351642647986 };
    final double[] relativePartialForwardsY = {1.0002606663941294, 0.999842661886235 };

    final double forwardX = 1.3364015890354652;
    final double forwardY = 1.5992978529959616;
    final double timeToExpiry = 0.019178082191780823;

    final double[] dataStrikes = {0.8276533748061506, 0.830544004818981, 0.8356246758244018, 0.8408571903798175, 0.8438972913060586 };
    final double[] dataVols = {0.0668, 0.0653, 0.06465, 0.0668, 0.0686, 0.05 };

    final double[] rhosGuess = new double[] {0.65, 0.65 };

    final MixedBivariateLogNormalCorrelationFinder fitter = new MixedBivariateLogNormalCorrelationFinder();

    fitter.doFit(rhosGuess, dataStrikes, dataVols, timeToExpiry, weights, sigmasX, sigmasY, relativePartialForwardsX,
        relativePartialForwardsY, forwardX, forwardY);
  }

  /**
   * 
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void negativeForwardXTest() {

    final double[] weights = {0.37640291143644194, 0.623597088563558 };
    final double[] sigmasX = {0.06354423944935964, 0.10361640830108385 };
    final double[] sigmasY = {0.03794692512570427, 0.07364831519062784 };
    final double[] relativePartialForwardsX = {0.9997760696332433, 1.0001351642647986 };
    final double[] relativePartialForwardsY = {1.0002606663941294, 0.999842661886235 };

    final double forwardX = -1.3364015890354652;
    final double forwardY = 1.5992978529959616;
    final double timeToExpiry = 0.019178082191780823;

    final double[] dataStrikes = {0.8276533748061506, 0.830544004818981, 0.8356246758244018, 0.8408571903798175, 0.8438972913060586 };
    final double[] dataVols = {0.0668, 0.0653, 0.06465, 0.0668, 0.0686 };

    final double[] rhosGuess = new double[] {0.65, 0.65 };

    final MixedBivariateLogNormalCorrelationFinder fitter = new MixedBivariateLogNormalCorrelationFinder();

    fitter.doFit(rhosGuess, dataStrikes, dataVols, timeToExpiry, weights, sigmasX, sigmasY, relativePartialForwardsX,
        relativePartialForwardsY, forwardX, forwardY);
  }

  /**
   * 
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void negativeForwardYTest() {

    final double[] weights = {0.37640291143644194, 0.623597088563558 };
    final double[] sigmasX = {0.06354423944935964, 0.10361640830108385 };
    final double[] sigmasY = {0.03794692512570427, 0.07364831519062784 };
    final double[] relativePartialForwardsX = {0.9997760696332433, 1.0001351642647986 };
    final double[] relativePartialForwardsY = {1.0002606663941294, 0.999842661886235 };

    final double forwardX = 1.3364015890354652;
    final double forwardY = -1.5992978529959616;
    final double timeToExpiry = 0.019178082191780823;

    final double[] dataStrikes = {0.8276533748061506, 0.830544004818981, 0.8356246758244018, 0.8408571903798175, 0.8438972913060586 };
    final double[] dataVols = {0.0668, 0.0653, 0.06465, 0.0668, 0.0686 };

    final double[] rhosGuess = new double[] {0.65, 0.65 };

    final MixedBivariateLogNormalCorrelationFinder fitter = new MixedBivariateLogNormalCorrelationFinder();

    fitter.doFit(rhosGuess, dataStrikes, dataVols, timeToExpiry, weights, sigmasX, sigmasY, relativePartialForwardsX,
        relativePartialForwardsY, forwardX, forwardY);
  }

  /**
   * 
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void negativeTimeToExpiryTest() {

    final double[] weights = {0.37640291143644194, 0.623597088563558 };
    final double[] sigmasX = {0.06354423944935964, 0.10361640830108385 };
    final double[] sigmasY = {0.03794692512570427, 0.07364831519062784 };
    final double[] relativePartialForwardsX = {0.9997760696332433, 1.0001351642647986 };
    final double[] relativePartialForwardsY = {1.0002606663941294, 0.999842661886235 };

    final double forwardX = 1.3364015890354652;
    final double forwardY = 1.5992978529959616;
    final double timeToExpiry = -0.019178082191780823;

    final double[] dataStrikes = {0.8276533748061506, 0.830544004818981, 0.8356246758244018, 0.8408571903798175, 0.8438972913060586 };
    final double[] dataVols = {0.0668, 0.0653, 0.06465, 0.0668, 0.0686 };

    final double[] rhosGuess = new double[] {0.65, 0.65 };

    final MixedBivariateLogNormalCorrelationFinder fitter = new MixedBivariateLogNormalCorrelationFinder();

    fitter.doFit(rhosGuess, dataStrikes, dataVols, timeToExpiry, weights, sigmasX, sigmasY, relativePartialForwardsX,
        relativePartialForwardsY, forwardX, forwardY);
  }

  /**
   * 
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void iNFweightsTest() {

    final double[] weights = {INF, 0.623597088563558 };
    final double[] sigmasX = {0.06354423944935964, 0.10361640830108385 };
    final double[] sigmasY = {0.03794692512570427, 0.07364831519062784 };
    final double[] relativePartialForwardsX = {0.9997760696332433, 1.0001351642647986 };
    final double[] relativePartialForwardsY = {1.0002606663941294, 0.999842661886235 };

    final double forwardX = 1.3364015890354652;
    final double forwardY = 1.5992978529959616;
    final double timeToExpiry = 0.019178082191780823;

    final double[] dataStrikes = {0.8276533748061506, 0.830544004818981, 0.8356246758244018, 0.8408571903798175, 0.8438972913060586 };
    final double[] dataVols = {0.0668, 0.0653, 0.06465, 0.0668, 0.0686 };

    final double[] rhosGuess = new double[] {0.65, 0.65 };

    final MixedBivariateLogNormalCorrelationFinder fitter = new MixedBivariateLogNormalCorrelationFinder();

    fitter.doFit(rhosGuess, dataStrikes, dataVols, timeToExpiry, weights, sigmasX, sigmasY, relativePartialForwardsX,
        relativePartialForwardsY, forwardX, forwardY);
  }

  /**
   * 
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void iNFsigmasXTest() {

    final double[] weights = {0.37640291143644194, 0.623597088563558 };
    final double[] sigmasX = {INF, 0.10361640830108385 };
    final double[] sigmasY = {0.03794692512570427, 0.07364831519062784 };
    final double[] relativePartialForwardsX = {0.9997760696332433, 1.0001351642647986 };
    final double[] relativePartialForwardsY = {1.0002606663941294, 0.999842661886235 };

    final double forwardX = 1.3364015890354652;
    final double forwardY = 1.5992978529959616;
    final double timeToExpiry = 0.019178082191780823;

    final double[] dataStrikes = {0.8276533748061506, 0.830544004818981, 0.8356246758244018, 0.8408571903798175, 0.8438972913060586 };
    final double[] dataVols = {0.0668, 0.0653, 0.06465, 0.0668, 0.0686 };

    final double[] rhosGuess = new double[] {0.65, 0.65 };

    final MixedBivariateLogNormalCorrelationFinder fitter = new MixedBivariateLogNormalCorrelationFinder();

    fitter.doFit(rhosGuess, dataStrikes, dataVols, timeToExpiry, weights, sigmasX, sigmasY, relativePartialForwardsX,
        relativePartialForwardsY, forwardX, forwardY);
  }

  /**
   * 
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void iNFsigmasYTest() {

    final double[] weights = {0.37640291143644194, 0.623597088563558 };
    final double[] sigmasX = {0.06354423944935964, 0.10361640830108385 };
    final double[] sigmasY = {INF, 0.07364831519062784 };
    final double[] relativePartialForwardsX = {0.9997760696332433, 1.0001351642647986 };
    final double[] relativePartialForwardsY = {1.0002606663941294, 0.999842661886235 };

    final double forwardX = 1.3364015890354652;
    final double forwardY = 1.5992978529959616;
    final double timeToExpiry = 0.019178082191780823;

    final double[] dataStrikes = {0.8276533748061506, 0.830544004818981, 0.8356246758244018, 0.8408571903798175, 0.8438972913060586 };
    final double[] dataVols = {0.0668, 0.0653, 0.06465, 0.0668, 0.0686 };

    final double[] rhosGuess = new double[] {0.65, 0.65 };

    final MixedBivariateLogNormalCorrelationFinder fitter = new MixedBivariateLogNormalCorrelationFinder();

    fitter.doFit(rhosGuess, dataStrikes, dataVols, timeToExpiry, weights, sigmasX, sigmasY, relativePartialForwardsX,
        relativePartialForwardsY, forwardX, forwardY);
  }

  /**
   * 
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void iNFrelativePartialForwardsXTest() {

    final double[] weights = {0.37640291143644194, 0.623597088563558 };
    final double[] sigmasX = {0.06354423944935964, 0.10361640830108385 };
    final double[] sigmasY = {0.03794692512570427, 0.07364831519062784 };
    final double[] relativePartialForwardsX = {INF, 1.0001351642647986 };
    final double[] relativePartialForwardsY = {1.0002606663941294, 0.999842661886235 };

    final double forwardX = 1.3364015890354652;
    final double forwardY = 1.5992978529959616;
    final double timeToExpiry = 0.019178082191780823;

    final double[] dataStrikes = {0.8276533748061506, 0.830544004818981, 0.8356246758244018, 0.8408571903798175, 0.8438972913060586 };
    final double[] dataVols = {0.0668, 0.0653, 0.06465, 0.0668, 0.0686 };

    final double[] rhosGuess = new double[] {0.65, 0.65 };

    final MixedBivariateLogNormalCorrelationFinder fitter = new MixedBivariateLogNormalCorrelationFinder();

    fitter.doFit(rhosGuess, dataStrikes, dataVols, timeToExpiry, weights, sigmasX, sigmasY, relativePartialForwardsX,
        relativePartialForwardsY, forwardX, forwardY);
  }

  /**
   * 
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void iNFrelativePartialForwardsYTest() {

    final double[] weights = {0.37640291143644194, 0.623597088563558 };
    final double[] sigmasX = {0.06354423944935964, 0.10361640830108385 };
    final double[] sigmasY = {0.03794692512570427, 0.07364831519062784 };
    final double[] relativePartialForwardsX = {0.9997760696332433, 1.0001351642647986 };
    final double[] relativePartialForwardsY = {INF, 0.999842661886235 };

    final double forwardX = 1.3364015890354652;
    final double forwardY = 1.5992978529959616;
    final double timeToExpiry = 0.019178082191780823;

    final double[] dataStrikes = {0.8276533748061506, 0.830544004818981, 0.8356246758244018, 0.8408571903798175, 0.8438972913060586 };
    final double[] dataVols = {0.0668, 0.0653, 0.06465, 0.0668, 0.0686 };

    final double[] rhosGuess = new double[] {0.65, 0.65 };

    final MixedBivariateLogNormalCorrelationFinder fitter = new MixedBivariateLogNormalCorrelationFinder();

    fitter.doFit(rhosGuess, dataStrikes, dataVols, timeToExpiry, weights, sigmasX, sigmasY, relativePartialForwardsX,
        relativePartialForwardsY, forwardX, forwardY);
  }

  /**
   * 
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void iNFforwardXTest() {

    final double[] weights = {0.37640291143644194, 0.623597088563558 };
    final double[] sigmasX = {0.06354423944935964, 0.10361640830108385 };
    final double[] sigmasY = {0.03794692512570427, 0.07364831519062784 };
    final double[] relativePartialForwardsX = {0.9997760696332433, 1.0001351642647986 };
    final double[] relativePartialForwardsY = {1.0002606663941294, 0.999842661886235 };

    final double forwardX = INF;
    final double forwardY = 1.5992978529959616;
    final double timeToExpiry = 0.019178082191780823;

    final double[] dataStrikes = {0.8276533748061506, 0.830544004818981, 0.8356246758244018, 0.8408571903798175, 0.8438972913060586 };
    final double[] dataVols = {0.0668, 0.0653, 0.06465, 0.0668, 0.0686 };

    final double[] rhosGuess = new double[] {0.65, 0.65 };

    final MixedBivariateLogNormalCorrelationFinder fitter = new MixedBivariateLogNormalCorrelationFinder();

    fitter.doFit(rhosGuess, dataStrikes, dataVols, timeToExpiry, weights, sigmasX, sigmasY, relativePartialForwardsX,
        relativePartialForwardsY, forwardX, forwardY);
  }

  /**
   * 
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void iNFforwardYTest() {

    final double[] weights = {0.37640291143644194, 0.623597088563558 };
    final double[] sigmasX = {0.06354423944935964, 0.10361640830108385 };
    final double[] sigmasY = {0.03794692512570427, 0.07364831519062784 };
    final double[] relativePartialForwardsX = {0.9997760696332433, 1.0001351642647986 };
    final double[] relativePartialForwardsY = {1.0002606663941294, 0.999842661886235 };

    final double forwardX = 1.3364015890354652;
    final double forwardY = INF;
    final double timeToExpiry = 0.019178082191780823;

    final double[] dataStrikes = {0.8276533748061506, 0.830544004818981, 0.8356246758244018, 0.8408571903798175, 0.8438972913060586 };
    final double[] dataVols = {0.0668, 0.0653, 0.06465, 0.0668, 0.0686 };

    final double[] rhosGuess = new double[] {0.65, 0.65 };

    final MixedBivariateLogNormalCorrelationFinder fitter = new MixedBivariateLogNormalCorrelationFinder();

    fitter.doFit(rhosGuess, dataStrikes, dataVols, timeToExpiry, weights, sigmasX, sigmasY, relativePartialForwardsX,
        relativePartialForwardsY, forwardX, forwardY);
  }

  /**
   * 
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void iNFtimeToExpiryTest() {

    final double[] weights = {0.37640291143644194, 0.623597088563558 };
    final double[] sigmasX = {0.06354423944935964, 0.10361640830108385 };
    final double[] sigmasY = {0.03794692512570427, 0.07364831519062784 };
    final double[] relativePartialForwardsX = {0.9997760696332433, 1.0001351642647986 };
    final double[] relativePartialForwardsY = {1.0002606663941294, 0.999842661886235 };

    final double forwardX = 1.3364015890354652;
    final double forwardY = 1.5992978529959616;
    final double timeToExpiry = INF;

    final double[] dataStrikes = {0.8276533748061506, 0.830544004818981, 0.8356246758244018, 0.8408571903798175, 0.8438972913060586 };
    final double[] dataVols = {0.0668, 0.0653, 0.06465, 0.0668, 0.0686 };

    final double[] rhosGuess = new double[] {0.65, 0.65 };

    final MixedBivariateLogNormalCorrelationFinder fitter = new MixedBivariateLogNormalCorrelationFinder();

    fitter.doFit(rhosGuess, dataStrikes, dataVols, timeToExpiry, weights, sigmasX, sigmasY, relativePartialForwardsX,
        relativePartialForwardsY, forwardX, forwardY);
  }

  /**
   * 
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void iNFdataStrikesTest() {

    final double[] weights = {0.37640291143644194, 0.623597088563558 };
    final double[] sigmasX = {0.06354423944935964, 0.10361640830108385 };
    final double[] sigmasY = {0.03794692512570427, 0.07364831519062784 };
    final double[] relativePartialForwardsX = {0.9997760696332433, 1.0001351642647986 };
    final double[] relativePartialForwardsY = {1.0002606663941294, 0.999842661886235 };

    final double forwardX = 1.3364015890354652;
    final double forwardY = 1.5992978529959616;
    final double timeToExpiry = 0.019178082191780823;

    final double[] dataStrikes = {INF, 0.830544004818981, 0.8356246758244018, 0.8408571903798175, 0.8438972913060586 };
    final double[] dataVols = {0.0668, 0.0653, 0.06465, 0.0668, 0.0686 };

    final double[] rhosGuess = new double[] {0.65, 0.65 };

    final MixedBivariateLogNormalCorrelationFinder fitter = new MixedBivariateLogNormalCorrelationFinder();

    fitter.doFit(rhosGuess, dataStrikes, dataVols, timeToExpiry, weights, sigmasX, sigmasY, relativePartialForwardsX,
        relativePartialForwardsY, forwardX, forwardY);
  }

  /**
   * 
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void iNFdataVolsTest() {

    final double[] weights = {0.37640291143644194, 0.623597088563558 };
    final double[] sigmasX = {0.06354423944935964, 0.10361640830108385 };
    final double[] sigmasY = {0.03794692512570427, 0.07364831519062784 };
    final double[] relativePartialForwardsX = {0.9997760696332433, 1.0001351642647986 };
    final double[] relativePartialForwardsY = {1.0002606663941294, 0.999842661886235 };

    final double forwardX = 1.3364015890354652;
    final double forwardY = 1.5992978529959616;
    final double timeToExpiry = 0.019178082191780823;

    final double[] dataStrikes = {0.8276533748061506, 0.830544004818981, 0.8356246758244018, 0.8408571903798175, 0.8438972913060586 };
    final double[] dataVols = {0.0668, 0.0653, INF, 0.0668, 0.0686 };

    final double[] rhosGuess = new double[] {0.65, 0.65 };

    final MixedBivariateLogNormalCorrelationFinder fitter = new MixedBivariateLogNormalCorrelationFinder();

    fitter.doFit(rhosGuess, dataStrikes, dataVols, timeToExpiry, weights, sigmasX, sigmasY, relativePartialForwardsX,
        relativePartialForwardsY, forwardX, forwardY);
  }

  /**
   * 
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void iNFrhosGuessTest() {

    final double[] weights = {0.37640291143644194, 0.623597088563558 };
    final double[] sigmasX = {0.06354423944935964, 0.10361640830108385 };
    final double[] sigmasY = {0.03794692512570427, 0.07364831519062784 };
    final double[] relativePartialForwardsX = {0.9997760696332433, 1.0001351642647986 };
    final double[] relativePartialForwardsY = {1.0002606663941294, 0.999842661886235 };

    final double forwardX = 1.3364015890354652;
    final double forwardY = 1.5992978529959616;
    final double timeToExpiry = 0.019178082191780823;

    final double[] dataStrikes = {0.8276533748061506, 0.830544004818981, 0.8356246758244018, 0.8408571903798175, 0.8438972913060586 };
    final double[] dataVols = {0.0668, 0.0653, 0.06465, 0.0668, 0.0686 };

    final double[] rhosGuess = new double[] {INF, 0.65 };

    final MixedBivariateLogNormalCorrelationFinder fitter = new MixedBivariateLogNormalCorrelationFinder();

    fitter.doFit(rhosGuess, dataStrikes, dataVols, timeToExpiry, weights, sigmasX, sigmasY, relativePartialForwardsX,
        relativePartialForwardsY, forwardX, forwardY);
  }

  /**
   * 
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void naNweightsTest() {

    final double[] weights = {Double.NaN, 0.623597088563558 };
    final double[] sigmasX = {0.06354423944935964, 0.10361640830108385 };
    final double[] sigmasY = {0.03794692512570427, 0.07364831519062784 };
    final double[] relativePartialForwardsX = {0.9997760696332433, 1.0001351642647986 };
    final double[] relativePartialForwardsY = {1.0002606663941294, 0.999842661886235 };

    final double forwardX = 1.3364015890354652;
    final double forwardY = 1.5992978529959616;
    final double timeToExpiry = 0.019178082191780823;

    final double[] dataStrikes = {0.8276533748061506, 0.830544004818981, 0.8356246758244018, 0.8408571903798175, 0.8438972913060586 };
    final double[] dataVols = {0.0668, 0.0653, 0.06465, 0.0668, 0.0686 };

    final double[] rhosGuess = new double[] {0.65, 0.65 };

    final MixedBivariateLogNormalCorrelationFinder fitter = new MixedBivariateLogNormalCorrelationFinder();

    fitter.doFit(rhosGuess, dataStrikes, dataVols, timeToExpiry, weights, sigmasX, sigmasY, relativePartialForwardsX,
        relativePartialForwardsY, forwardX, forwardY);
  }

  /**
   * 
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void naNsigmasXTest() {

    final double[] weights = {0.37640291143644194, 0.623597088563558 };
    final double[] sigmasX = {Double.NaN, 0.10361640830108385 };
    final double[] sigmasY = {0.03794692512570427, 0.07364831519062784 };
    final double[] relativePartialForwardsX = {0.9997760696332433, 1.0001351642647986 };
    final double[] relativePartialForwardsY = {1.0002606663941294, 0.999842661886235 };

    final double forwardX = 1.3364015890354652;
    final double forwardY = 1.5992978529959616;
    final double timeToExpiry = 0.019178082191780823;

    final double[] dataStrikes = {0.8276533748061506, 0.830544004818981, 0.8356246758244018, 0.8408571903798175, 0.8438972913060586 };
    final double[] dataVols = {0.0668, 0.0653, 0.06465, 0.0668, 0.0686 };

    final double[] rhosGuess = new double[] {0.65, 0.65 };

    final MixedBivariateLogNormalCorrelationFinder fitter = new MixedBivariateLogNormalCorrelationFinder();

    fitter.doFit(rhosGuess, dataStrikes, dataVols, timeToExpiry, weights, sigmasX, sigmasY, relativePartialForwardsX,
        relativePartialForwardsY, forwardX, forwardY);
  }

  /**
   * 
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void naNsigmasYTest() {

    final double[] weights = {0.37640291143644194, 0.623597088563558 };
    final double[] sigmasX = {0.06354423944935964, 0.10361640830108385 };
    final double[] sigmasY = {Double.NaN, 0.07364831519062784 };
    final double[] relativePartialForwardsX = {0.9997760696332433, 1.0001351642647986 };
    final double[] relativePartialForwardsY = {1.0002606663941294, 0.999842661886235 };

    final double forwardX = 1.3364015890354652;
    final double forwardY = 1.5992978529959616;
    final double timeToExpiry = 0.019178082191780823;

    final double[] dataStrikes = {0.8276533748061506, 0.830544004818981, 0.8356246758244018, 0.8408571903798175, 0.8438972913060586 };
    final double[] dataVols = {0.0668, 0.0653, 0.06465, 0.0668, 0.0686 };

    final double[] rhosGuess = new double[] {0.65, 0.65 };

    final MixedBivariateLogNormalCorrelationFinder fitter = new MixedBivariateLogNormalCorrelationFinder();

    fitter.doFit(rhosGuess, dataStrikes, dataVols, timeToExpiry, weights, sigmasX, sigmasY, relativePartialForwardsX,
        relativePartialForwardsY, forwardX, forwardY);
  }

  /**
   * 
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void naNrelativePartialForwardsXTest() {

    final double[] weights = {0.37640291143644194, 0.623597088563558 };
    final double[] sigmasX = {0.06354423944935964, 0.10361640830108385 };
    final double[] sigmasY = {0.03794692512570427, 0.07364831519062784 };
    final double[] relativePartialForwardsX = {Double.NaN, 1.0001351642647986 };
    final double[] relativePartialForwardsY = {1.0002606663941294, 0.999842661886235 };

    final double forwardX = 1.3364015890354652;
    final double forwardY = 1.5992978529959616;
    final double timeToExpiry = 0.019178082191780823;

    final double[] dataStrikes = {0.8276533748061506, 0.830544004818981, 0.8356246758244018, 0.8408571903798175, 0.8438972913060586 };
    final double[] dataVols = {0.0668, 0.0653, 0.06465, 0.0668, 0.0686 };

    final double[] rhosGuess = new double[] {0.65, 0.65 };

    final MixedBivariateLogNormalCorrelationFinder fitter = new MixedBivariateLogNormalCorrelationFinder();

    fitter.doFit(rhosGuess, dataStrikes, dataVols, timeToExpiry, weights, sigmasX, sigmasY, relativePartialForwardsX,
        relativePartialForwardsY, forwardX, forwardY);
  }

  /**
   * 
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void naNrelativePartialForwardsYTest() {

    final double[] weights = {0.37640291143644194, 0.623597088563558 };
    final double[] sigmasX = {0.06354423944935964, 0.10361640830108385 };
    final double[] sigmasY = {0.03794692512570427, 0.07364831519062784 };
    final double[] relativePartialForwardsX = {0.9997760696332433, 1.0001351642647986 };
    final double[] relativePartialForwardsY = {1.0002606663941294, Double.NaN };

    final double forwardX = 1.3364015890354652;
    final double forwardY = 1.5992978529959616;
    final double timeToExpiry = 0.019178082191780823;

    final double[] dataStrikes = {0.8276533748061506, 0.830544004818981, 0.8356246758244018, 0.8408571903798175, 0.8438972913060586 };
    final double[] dataVols = {0.0668, 0.0653, 0.06465, 0.0668, 0.0686 };

    final double[] rhosGuess = new double[] {0.65, 0.65 };

    final MixedBivariateLogNormalCorrelationFinder fitter = new MixedBivariateLogNormalCorrelationFinder();

    fitter.doFit(rhosGuess, dataStrikes, dataVols, timeToExpiry, weights, sigmasX, sigmasY, relativePartialForwardsX,
        relativePartialForwardsY, forwardX, forwardY);
  }

  /**
   * 
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void naNforwardXTest() {

    final double[] weights = {0.37640291143644194, 0.623597088563558 };
    final double[] sigmasX = {0.06354423944935964, 0.10361640830108385 };
    final double[] sigmasY = {0.03794692512570427, 0.07364831519062784 };
    final double[] relativePartialForwardsX = {0.9997760696332433, 1.0001351642647986 };
    final double[] relativePartialForwardsY = {1.0002606663941294, 0.999842661886235 };

    final double forwardX = Double.NaN;
    final double forwardY = 1.5992978529959616;
    final double timeToExpiry = 0.019178082191780823;

    final double[] dataStrikes = {0.8276533748061506, 0.830544004818981, 0.8356246758244018, 0.8408571903798175, 0.8438972913060586 };
    final double[] dataVols = {0.0668, 0.0653, 0.06465, 0.0668, 0.0686 };

    final double[] rhosGuess = new double[] {0.65, 0.65 };

    final MixedBivariateLogNormalCorrelationFinder fitter = new MixedBivariateLogNormalCorrelationFinder();

    fitter.doFit(rhosGuess, dataStrikes, dataVols, timeToExpiry, weights, sigmasX, sigmasY, relativePartialForwardsX,
        relativePartialForwardsY, forwardX, forwardY);
  }

  /**
   * 
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void naNforwardYTest() {

    final double[] weights = {0.37640291143644194, 0.623597088563558 };
    final double[] sigmasX = {0.06354423944935964, 0.10361640830108385 };
    final double[] sigmasY = {0.03794692512570427, 0.07364831519062784 };
    final double[] relativePartialForwardsX = {0.9997760696332433, 1.0001351642647986 };
    final double[] relativePartialForwardsY = {1.0002606663941294, 0.999842661886235 };

    final double forwardX = 1.3364015890354652;
    final double forwardY = Double.NaN;
    final double timeToExpiry = 0.019178082191780823;

    final double[] dataStrikes = {0.8276533748061506, 0.830544004818981, 0.8356246758244018, 0.8408571903798175, 0.8438972913060586 };
    final double[] dataVols = {0.0668, 0.0653, 0.06465, 0.0668, 0.0686 };

    final double[] rhosGuess = new double[] {0.65, 0.65 };

    final MixedBivariateLogNormalCorrelationFinder fitter = new MixedBivariateLogNormalCorrelationFinder();

    fitter.doFit(rhosGuess, dataStrikes, dataVols, timeToExpiry, weights, sigmasX, sigmasY, relativePartialForwardsX,
        relativePartialForwardsY, forwardX, forwardY);
  }

  /**
   * 
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void naNtimeToExpiryTest() {

    final double[] weights = {0.37640291143644194, 0.623597088563558 };
    final double[] sigmasX = {0.06354423944935964, 0.10361640830108385 };
    final double[] sigmasY = {0.03794692512570427, 0.07364831519062784 };
    final double[] relativePartialForwardsX = {0.9997760696332433, 1.0001351642647986 };
    final double[] relativePartialForwardsY = {1.0002606663941294, 0.999842661886235 };

    final double forwardX = 1.3364015890354652;
    final double forwardY = 1.5992978529959616;
    final double timeToExpiry = Double.NaN;

    final double[] dataStrikes = {0.8276533748061506, 0.830544004818981, 0.8356246758244018, 0.8408571903798175, 0.8438972913060586 };
    final double[] dataVols = {0.0668, 0.0653, 0.06465, 0.0668, 0.0686 };

    final double[] rhosGuess = new double[] {0.65, 0.65 };

    final MixedBivariateLogNormalCorrelationFinder fitter = new MixedBivariateLogNormalCorrelationFinder();

    fitter.doFit(rhosGuess, dataStrikes, dataVols, timeToExpiry, weights, sigmasX, sigmasY, relativePartialForwardsX,
        relativePartialForwardsY, forwardX, forwardY);
  }

  /**
   * 
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void naNdataStrikesTest() {

    final double[] weights = {0.37640291143644194, 0.623597088563558 };
    final double[] sigmasX = {0.06354423944935964, 0.10361640830108385 };
    final double[] sigmasY = {0.03794692512570427, 0.07364831519062784 };
    final double[] relativePartialForwardsX = {0.9997760696332433, 1.0001351642647986 };
    final double[] relativePartialForwardsY = {1.0002606663941294, 0.999842661886235 };

    final double forwardX = 1.3364015890354652;
    final double forwardY = 1.5992978529959616;
    final double timeToExpiry = 0.019178082191780823;

    final double[] dataStrikes = {0.8276533748061506, Double.NaN, 0.8356246758244018, 0.8408571903798175, 0.8438972913060586 };
    final double[] dataVols = {0.0668, 0.0653, 0.06465, 0.0668, 0.0686 };

    final double[] rhosGuess = new double[] {0.65, 0.65 };

    final MixedBivariateLogNormalCorrelationFinder fitter = new MixedBivariateLogNormalCorrelationFinder();

    fitter.doFit(rhosGuess, dataStrikes, dataVols, timeToExpiry, weights, sigmasX, sigmasY, relativePartialForwardsX,
        relativePartialForwardsY, forwardX, forwardY);
  }

  /**
   * 
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void naNdataVolsTest() {

    final double[] weights = {0.37640291143644194, 0.623597088563558 };
    final double[] sigmasX = {0.06354423944935964, 0.10361640830108385 };
    final double[] sigmasY = {0.03794692512570427, 0.07364831519062784 };
    final double[] relativePartialForwardsX = {0.9997760696332433, 1.0001351642647986 };
    final double[] relativePartialForwardsY = {1.0002606663941294, 0.999842661886235 };

    final double forwardX = 1.3364015890354652;
    final double forwardY = 1.5992978529959616;
    final double timeToExpiry = 0.019178082191780823;

    final double[] dataStrikes = {0.8276533748061506, 0.830544004818981, 0.8356246758244018, 0.8408571903798175, 0.8438972913060586 };
    final double[] dataVols = {0.0668, Double.NaN, 0.06465, 0.0668, 0.0686 };

    final double[] rhosGuess = new double[] {0.65, 0.65 };

    final MixedBivariateLogNormalCorrelationFinder fitter = new MixedBivariateLogNormalCorrelationFinder();

    fitter.doFit(rhosGuess, dataStrikes, dataVols, timeToExpiry, weights, sigmasX, sigmasY, relativePartialForwardsX,
        relativePartialForwardsY, forwardX, forwardY);
  }

  /**
   * 
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void naNrhosGuessTest() {

    final double[] weights = {0.37640291143644194, 0.623597088563558 };
    final double[] sigmasX = {0.06354423944935964, 0.10361640830108385 };
    final double[] sigmasY = {0.03794692512570427, 0.07364831519062784 };
    final double[] relativePartialForwardsX = {0.9997760696332433, 1.0001351642647986 };
    final double[] relativePartialForwardsY = {1.0002606663941294, 0.999842661886235 };

    final double forwardX = 1.3364015890354652;
    final double forwardY = 1.5992978529959616;
    final double timeToExpiry = 0.019178082191780823;

    final double[] dataStrikes = {0.8276533748061506, 0.830544004818981, 0.8356246758244018, 0.8408571903798175, 0.8438972913060586 };
    final double[] dataVols = {0.0668, 0.0653, 0.06465, 0.0668, 0.0686 };

    final double[] rhosGuess = new double[] {0.65, Double.NaN };

    final MixedBivariateLogNormalCorrelationFinder fitter = new MixedBivariateLogNormalCorrelationFinder();

    fitter.doFit(rhosGuess, dataStrikes, dataVols, timeToExpiry, weights, sigmasX, sigmasY, relativePartialForwardsX,
        relativePartialForwardsY, forwardX, forwardY);
  }

  /**
   * Test below is for debugging
   */
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

    MixedBivariateLogNormalCorrelationFinder fitter = new MixedBivariateLogNormalCorrelationFinder();

    boolean fitDone = false;

    while (fitDone == false) {

      fitter.doFit(rhosGuess, dataStrikes, dataVols, timeToExpiry, weights, sigmasX, sigmasY, relativePartialForwardsX,
          relativePartialForwardsY, forwardX, forwardY);
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
        fitter = new MixedBivariateLogNormalCorrelationFinder();
      }

    }

    System.out.println("\n");

    rhosGuess = fitter.getParams();

    final MixedBivariateLogNormalModelVolatility objZ = new MixedBivariateLogNormalModelVolatility(weights, sigmasX,
        sigmasY, relativePartialForwardsX, relativePartialForwardsY, rhosGuess);

    final double[] ansVolsZ = new double[100];
    for (int i = 0; i < 100; i++) {
      final double k = fwdZ * (0.97 + .6 * i / 1000.);
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
