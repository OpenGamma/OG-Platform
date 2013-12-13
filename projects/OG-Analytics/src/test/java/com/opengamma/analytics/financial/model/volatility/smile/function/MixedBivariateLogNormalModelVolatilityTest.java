/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.model.volatility.smile.function;

import static org.testng.Assert.assertEquals;

import org.testng.annotations.Test;

import com.opengamma.analytics.financial.model.option.pricing.analytic.formula.EuropeanVanillaOption;
import com.opengamma.analytics.financial.model.volatility.BlackFormulaRepository;
import com.opengamma.util.test.TestGroup;

/**
 * Test.
 */
@Test(groups = TestGroup.UNIT)
public class MixedBivariateLogNormalModelVolatilityTest {

  private static final double EPS = 1.e-15;
  private static final double INF = 1. / 0.;

  /**
   * 
   */
  @Test
  public void trivialValuesTest() {
    final double[] wghts = {0.5, 0.5 };
    final double[] sigsX = {0.5, 0.5 };
    final double[] sigsY = {0.5, 0.5 };
    final double[] rpfsX = {1.0, 1.0 };
    final double[] rpfsY = {1.0, 1.0 };
    final double[] rhs = {0., 0. };
    final double timeToExpiry = 1.;
    final double forwardX = 1.;
    final double forwardY = 1.;
    final double forwardZ = forwardX / forwardY;
    final double k = 1.;

    final MixedBivariateLogNormalModelVolatility volObjZ1 = new MixedBivariateLogNormalModelVolatility(wghts, sigsX,
        sigsY, rpfsX, rpfsY, rhs);
    final MixedBivariateLogNormalModelVolatility volObjZ2 = new MixedBivariateLogNormalModelVolatility(wghts, sigsX,
        sigsY, rhs);
    final EuropeanVanillaOption option = new EuropeanVanillaOption(k, timeToExpiry, true);

    assertEquals(volObjZ1.getImpliedVolatilityZ(option, forwardZ), Math.pow(0.5, 0.5), EPS);
    assertEquals(volObjZ2.getImpliedVolatilityZ(option, forwardZ), Math.pow(0.5, 0.5), EPS);
    assertEquals(volObjZ1.getSigmasZ()[0], Math.pow(0.5, 0.5), EPS);
    assertEquals(volObjZ1.getSigmasZ()[1], Math.pow(0.5, 0.5), EPS);
    assertEquals(volObjZ2.getSigmasZ()[0], Math.pow(0.5, 0.5), EPS);
    assertEquals(volObjZ2.getSigmasZ()[1], Math.pow(0.5, 0.5), EPS);
    assertEquals(volObjZ1.getRelativeForwardsZ()[0], 1., EPS); // Note that this is NOT Math.exp(0.25) due to driftcorrection
    assertEquals(volObjZ1.getRelativeForwardsZ()[1], 1., EPS);
    assertEquals(volObjZ2.getRelativeForwardsZ()[0], 1., EPS);
    assertEquals(volObjZ2.getRelativeForwardsZ()[1], 1., EPS);

    assertEquals(volObjZ1.getPriceZ(option, forwardZ), BlackFormulaRepository.price(forwardZ, k, timeToExpiry, Math.pow(0.5, 0.5), true), EPS);
    assertEquals(volObjZ2.getPriceZ(option, forwardZ), BlackFormulaRepository.price(forwardZ, k, timeToExpiry, Math.pow(0.5, 0.5), true), EPS);
    assertEquals(volObjZ1.getInvExpDriftCorrection(), 1. / Math.exp(0.25), EPS);
    assertEquals(volObjZ2.getInvExpDriftCorrection(), 1. / Math.exp(0.25), EPS);
    //    assertEquals(volObjZ2.getSigmasZ()[1], Math.pow(0.5, 0.5), EPS);
  }

  /**
   * Parameters for X,Y are also resorted corresponding to sorting of sigmaZ
   */
  @Test
  public void reorderedDataTest() {
    final double[] wghts = {0.2, 0.8 };
    final double[] sigsX = {0.4, 0.6 };
    final double[] sigsY = {0.45, 0.55 };
    final double[] rpfsX = {1.05, 0.79 / 0.8 };
    final double[] rpfsY = {1.1, 0.78 / 0.8 };
    final double[] rhs = {0., 1. };

    final MixedBivariateLogNormalModelVolatility volObjZ1 = new MixedBivariateLogNormalModelVolatility(wghts, sigsX,
        sigsY, rpfsX, rpfsY, rhs);
    final MixedBivariateLogNormalModelVolatility volObjZ2 = new MixedBivariateLogNormalModelVolatility(wghts, sigsX,
        sigsY, rhs);

    assertEquals(volObjZ1.getOrderedRelativePartialForwardsX()[0], rpfsX[1], EPS);
    assertEquals(volObjZ1.getOrderedRelativePartialForwardsX()[1], rpfsX[0], EPS);
    assertEquals(volObjZ1.getOrderedRelativePartialForwardsY()[0], rpfsY[1], EPS);
    assertEquals(volObjZ1.getOrderedRelativePartialForwardsY()[1], rpfsY[0], EPS);
    assertEquals(volObjZ1.getOrderedSigmasX()[0], sigsX[1], EPS);
    assertEquals(volObjZ1.getOrderedSigmasX()[1], sigsX[0], EPS);
    assertEquals(volObjZ1.getOrderedSigmasY()[0], sigsY[1], EPS);
    assertEquals(volObjZ1.getOrderedSigmasY()[1], sigsY[0], EPS);
    assertEquals(volObjZ2.getOrderedSigmasX()[0], sigsX[1], EPS);
    assertEquals(volObjZ2.getOrderedSigmasX()[1], sigsX[0], EPS);
    assertEquals(volObjZ2.getOrderedSigmasY()[0], sigsY[1], EPS);
    assertEquals(volObjZ2.getOrderedSigmasY()[1], sigsY[0], EPS);
    assertEquals(volObjZ1.getOrderedWeights()[0], wghts[1], EPS);
    assertEquals(volObjZ1.getOrderedWeights()[1], wghts[0], EPS);
    assertEquals(volObjZ2.getOrderedWeights()[0], wghts[1], EPS);
    assertEquals(volObjZ2.getOrderedWeights()[1], wghts[0], EPS);

  }

  /**
   * 
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void nullWghts1Test() {
    double[] wghts = new double[2];
    final double[] sigsX = {0.4, 0.6 };
    final double[] sigsY = {0.45, 0.55 };
    final double[] rpfsX = {1.05, 0.79 / 0.8 };
    final double[] rpfsY = {1.1, 0.78 / 0.8 };
    final double[] rhs = {0., 1. };

    wghts = null;

    new MixedBivariateLogNormalModelVolatility(wghts, sigsX, sigsY, rpfsX, rpfsY, rhs);

  }

  /**
   * 
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void nullWghts2Test() {
    double[] wghts = new double[2];
    final double[] sigsX = {0.4, 0.6 };
    final double[] sigsY = {0.45, 0.55 };
    final double[] rhs = {0., 1. };

    wghts = null;

    new MixedBivariateLogNormalModelVolatility(wghts, sigsX, sigsY, rhs);

  }

  /**
   * 
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void nullsigX1Test() {
    final double[] wghts = {0.2, 0.8 };
    double[] sigsX = new double[2];
    final double[] sigsY = {0.45, 0.55 };
    final double[] rpfsX = {1.05, 0.79 / 0.8 };
    final double[] rpfsY = {1.1, 0.78 / 0.8 };
    final double[] rhs = {0., 1. };

    sigsX = null;

    new MixedBivariateLogNormalModelVolatility(wghts, sigsX, sigsY, rpfsX, rpfsY, rhs);

  }

  /**
   * 
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void nullsigX2Test() {
    final double[] wghts = {0.2, 0.8 };
    double[] sigsX = new double[2];
    final double[] sigsY = {0.45, 0.55 };
    final double[] rhs = {0., 1. };

    sigsX = null;

    new MixedBivariateLogNormalModelVolatility(wghts, sigsX, sigsY, rhs);

  }

  /**
   * 
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void nullsigsY1Test() {
    final double[] wghts = {0.2, 0.8 };
    final double[] sigsX = {0.4, 0.6 };
    double[] sigsY = new double[2];
    final double[] rpfsX = {1.05, 0.79 / 0.8 };
    final double[] rpfsY = {1.1, 0.78 / 0.8 };
    final double[] rhs = {0., 1. };

    sigsY = null;

    new MixedBivariateLogNormalModelVolatility(wghts, sigsX, sigsY, rpfsX, rpfsY, rhs);

  }

  /**
   * 
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void nullsigsY2Test() {
    final double[] wghts = {0.2, 0.8 };
    final double[] sigsX = {0.4, 0.6 };
    double[] sigsY = new double[2];
    final double[] rhs = {0., 1. };

    sigsY = null;

    new MixedBivariateLogNormalModelVolatility(wghts, sigsX, sigsY, rhs);

  }

  /**
   * 
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void nullrpfsXTest() {
    final double[] wghts = {0.2, 0.8 };
    final double[] sigsX = {0.4, 0.6 };
    final double[] sigsY = {0.45, 0.55 };
    final double[] rpfsX = null;
    final double[] rpfsY = {1.1, 0.78 / 0.8 };
    final double[] rhs = {0., 1. };
    new MixedBivariateLogNormalModelVolatility(wghts, sigsX, sigsY, rpfsX, rpfsY, rhs);

  }

  /**
   * 
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void nullrpfsYTest() {
    final double[] wghts = {0.2, 0.8 };
    final double[] sigsX = {0.4, 0.6 };
    final double[] sigsY = {0.45, 0.55 };
    final double[] rpfsX = {1.05, 0.79 / 0.8 };
    double[] rpfsY = new double[2];
    final double[] rhs = {0., 1. };

    rpfsY = null;

    new MixedBivariateLogNormalModelVolatility(wghts, sigsX, sigsY, rpfsX, rpfsY, rhs);

  }

  /**
   * 
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void nullrhs1Test() {
    final double[] wghts = {0.2, 0.8 };
    final double[] sigsX = {0.4, 0.6 };
    final double[] sigsY = {0.45, 0.55 };
    final double[] rpfsX = {1.05, 0.79 / 0.8 };
    final double[] rpfsY = {1.1, 0.78 / 0.8 };
    double[] rhs = new double[2];

    rhs = null;

    new MixedBivariateLogNormalModelVolatility(wghts, sigsX, sigsY, rpfsX, rpfsY, rhs);

  }

  /**
   * 
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void nullrhs2Test() {
    final double[] wghts = {0.2, 0.8 };
    final double[] sigsX = {0.4, 0.6 };
    final double[] sigsY = {0.45, 0.55 };
    double[] rhs = new double[2];

    rhs = null;

    new MixedBivariateLogNormalModelVolatility(wghts, sigsX, sigsY, rhs);

  }

  /**
   * 
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void iNFwghts1Test() {
    final double[] wghts = {INF, 0.8 };
    final double[] sigsX = {0.4, 0.6 };
    final double[] sigsY = {0.45, 0.55 };
    final double[] rpfsX = {1.05, 0.79 / 0.8 };
    final double[] rpfsY = {1.1, 0.78 / 0.8 };
    final double[] rhs = {0., 1. };

    new MixedBivariateLogNormalModelVolatility(wghts, sigsX, sigsY, rpfsX, rpfsY, rhs);

  }

  /**
   * 
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void iNFwghts2Test() {
    final double[] wghts = {INF, 0.8 };
    final double[] sigsX = {0.4, 0.6 };
    final double[] sigsY = {0.45, 0.55 };
    final double[] rhs = {0., 1. };

    new MixedBivariateLogNormalModelVolatility(wghts, sigsX, sigsY, rhs);

  }

  /**
   * 
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void iNFsigsX1Test() {
    final double[] wghts = {0.2, 0.8 };
    final double[] sigsX = {INF, 0.6 };
    final double[] sigsY = {0.45, 0.55 };
    final double[] rpfsX = {1.05, 0.79 / 0.8 };
    final double[] rpfsY = {1.1, 0.78 / 0.8 };
    final double[] rhs = {0., 1. };

    new MixedBivariateLogNormalModelVolatility(wghts, sigsX, sigsY, rpfsX, rpfsY, rhs);

  }

  /**
   * 
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void iNFsigsX2Test() {
    final double[] wghts = {0.2, 0.8 };
    final double[] sigsX = {INF, 0.6 };
    final double[] sigsY = {0.45, 0.55 };
    final double[] rhs = {0., 1. };

    new MixedBivariateLogNormalModelVolatility(wghts, sigsX, sigsY, rhs);

  }

  /**
   * 
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void iNFsigsY1Test() {
    final double[] wghts = {0.2, 0.8 };
    final double[] sigsX = {0.4, 0.6 };
    final double[] sigsY = {INF, 0.55 };
    final double[] rpfsX = {1.05, 0.79 / 0.8 };
    final double[] rpfsY = {1.1, 0.78 / 0.8 };
    final double[] rhs = {0., 1. };

    new MixedBivariateLogNormalModelVolatility(wghts, sigsX, sigsY, rpfsX, rpfsY, rhs);

  }

  /**
   * 
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void iNFsigsY2Test() {
    final double[] wghts = {0.2, 0.8 };
    final double[] sigsX = {0.4, 0.6 };
    final double[] sigsY = {INF, 0.55 };
    final double[] rhs = {0., 1. };

    new MixedBivariateLogNormalModelVolatility(wghts, sigsX, sigsY, rhs);

  }

  /**
   * 
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void iNFrpfsXTest() {
    final double[] wghts = {0.2, 0.8 };
    final double[] sigsX = {0.4, 0.6 };
    final double[] sigsY = {0.45, 0.55 };
    final double[] rpfsX = {INF, 0.79 / 0.8 };
    final double[] rpfsY = {1.1, 0.78 / 0.8 };
    final double[] rhs = {0., 1. };

    new MixedBivariateLogNormalModelVolatility(wghts, sigsX, sigsY, rpfsX, rpfsY, rhs);

  }

  /**
   * 
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void iNFrpfsYTest() {
    final double[] wghts = {0.2, 0.8 };
    final double[] sigsX = {0.4, 0.6 };
    final double[] sigsY = {0.45, 0.55 };
    final double[] rpfsX = {1.05, 0.79 / 0.8 };
    final double[] rpfsY = {INF, 0.78 / 0.8 };
    final double[] rhs = {0., 1. };

    new MixedBivariateLogNormalModelVolatility(wghts, sigsX, sigsY, rpfsX, rpfsY, rhs);

  }

  /**
   * 
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void iNFrhs1Test() {
    final double[] wghts = {0.2, 0.8 };
    final double[] sigsX = {0.4, 0.6 };
    final double[] sigsY = {0.45, 0.55 };
    final double[] rpfsX = {1.05, 0.79 / 0.8 };
    final double[] rpfsY = {1.1, 0.78 / 0.8 };
    final double[] rhs = {INF, 1. };

    new MixedBivariateLogNormalModelVolatility(wghts, sigsX, sigsY, rpfsX, rpfsY, rhs);

  }

  /**
   * 
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void iNFrhs2Test() {
    final double[] wghts = {0.2, 0.8 };
    final double[] sigsX = {0.4, 0.6 };
    final double[] sigsY = {0.45, 0.55 };
    final double[] rhs = {INF, 1. };

    new MixedBivariateLogNormalModelVolatility(wghts, sigsX, sigsY, rhs);

  }

  /**
   * 
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void naNwghts1Test() {
    final double[] wghts = {Double.NaN, 0.8 };
    final double[] sigsX = {0.4, 0.6 };
    final double[] sigsY = {0.45, 0.55 };
    final double[] rpfsX = {1.05, 0.79 / 0.8 };
    final double[] rpfsY = {1.1, 0.78 / 0.8 };
    final double[] rhs = {0., 1. };

    new MixedBivariateLogNormalModelVolatility(wghts, sigsX, sigsY, rpfsX, rpfsY, rhs);

  }

  /**
   * 
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void naNwghts2Test() {
    final double[] wghts = {Double.NaN, 0.8 };
    final double[] sigsX = {0.4, 0.6 };
    final double[] sigsY = {0.45, 0.55 };
    final double[] rhs = {0., 1. };

    new MixedBivariateLogNormalModelVolatility(wghts, sigsX, sigsY, rhs);

  }

  /**
   * 
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void naNsigsX1Test() {
    final double[] wghts = {0.2, 0.8 };
    final double[] sigsX = {Double.NaN, 0.6 };
    final double[] sigsY = {0.45, 0.55 };
    final double[] rpfsX = {1.05, 0.79 / 0.8 };
    final double[] rpfsY = {1.1, 0.78 / 0.8 };
    final double[] rhs = {0., 1. };

    new MixedBivariateLogNormalModelVolatility(wghts, sigsX, sigsY, rpfsX, rpfsY, rhs);

  }

  /**
   * 
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void naNsigsX2Test() {
    final double[] wghts = {0.2, 0.8 };
    final double[] sigsX = {Double.NaN, 0.6 };
    final double[] sigsY = {0.45, 0.55 };
    final double[] rhs = {0., 1. };

    new MixedBivariateLogNormalModelVolatility(wghts, sigsX, sigsY, rhs);

  }

  /**
   * 
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void naNsigsY1Test() {
    final double[] wghts = {0.2, 0.8 };
    final double[] sigsX = {0.4, 0.6 };
    final double[] sigsY = {Double.NaN, 0.55 };
    final double[] rpfsX = {1.05, 0.79 / 0.8 };
    final double[] rpfsY = {1.1, 0.78 / 0.8 };
    final double[] rhs = {0., 1. };

    new MixedBivariateLogNormalModelVolatility(wghts, sigsX, sigsY, rpfsX, rpfsY, rhs);

  }

  /**
   * 
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void naNsigsY2Test() {
    final double[] wghts = {0.2, 0.8 };
    final double[] sigsX = {0.4, 0.6 };
    final double[] sigsY = {Double.NaN, 0.55 };
    final double[] rhs = {0., 1. };

    new MixedBivariateLogNormalModelVolatility(wghts, sigsX, sigsY, rhs);

  }

  /**
   * 
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void naNrpfsXTest() {
    final double[] wghts = {0.2, 0.8 };
    final double[] sigsX = {0.4, 0.6 };
    final double[] sigsY = {0.45, 0.55 };
    final double[] rpfsX = {Double.NaN, 0.79 / 0.8 };
    final double[] rpfsY = {1.1, 0.78 / 0.8 };
    final double[] rhs = {0., 1. };

    new MixedBivariateLogNormalModelVolatility(wghts, sigsX, sigsY, rpfsX, rpfsY, rhs);

  }

  /**
   * 
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void naNrpfsYTest() {
    final double[] wghts = {0.2, 0.8 };
    final double[] sigsX = {0.4, 0.6 };
    final double[] sigsY = {0.45, 0.55 };
    final double[] rpfsX = {1.05, 0.79 / 0.8 };
    final double[] rpfsY = {Double.NaN, 0.78 / 0.8 };
    final double[] rhs = {0., 1. };

    new MixedBivariateLogNormalModelVolatility(wghts, sigsX, sigsY, rpfsX, rpfsY, rhs);

  }

  /**
   * 
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void naNrhs1Test() {
    final double[] wghts = {0.2, 0.8 };
    final double[] sigsX = {0.4, 0.6 };
    final double[] sigsY = {0.45, 0.55 };
    final double[] rpfsX = {1.05, 0.79 / 0.8 };
    final double[] rpfsY = {1.1, 0.78 / 0.8 };
    final double[] rhs = {Double.NaN, 1. };

    new MixedBivariateLogNormalModelVolatility(wghts, sigsX, sigsY, rpfsX, rpfsY, rhs);

  }

  /**
   * 
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void naNrhs2Test() {
    final double[] wghts = {0.2, 0.8 };
    final double[] sigsX = {0.4, 0.6 };
    final double[] sigsY = {0.45, 0.55 };
    final double[] rhs = {Double.NaN, 1. };

    new MixedBivariateLogNormalModelVolatility(wghts, sigsX, sigsY, rhs);

  }

  /**
   * 
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void wrongLengthWghts1Test() {
    final double[] wghts = {0.2, 0.7, 0.1 };
    final double[] sigsX = {0.4, 0.6 };
    final double[] sigsY = {0.45, 0.55 };
    final double[] rpfsX = {1., 1. };
    final double[] rpfsY = {1., 1. };
    final double[] rhs = {0., 1. };

    new MixedBivariateLogNormalModelVolatility(wghts, sigsX, sigsY, rpfsX, rpfsY, rhs);

  }

  /**
   * 
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void wrongLengthWghts2Test() {
    final double[] wghts = {0.2, 0.7, 0.1 };
    final double[] sigsX = {0.4, 0.6 };
    final double[] sigsY = {0.45, 0.55 };
    final double[] rhs = {0., 1. };

    new MixedBivariateLogNormalModelVolatility(wghts, sigsX, sigsY, rhs);

  }

  /**
   * 
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void wrongLengthSigsX1Test() {
    final double[] wghts = {0.2, 0.8 };
    final double[] sigsX = {0.4, 0.6, 0.7 };
    final double[] sigsY = {0.45, 0.55 };
    final double[] rpfsX = {1., 1. };
    final double[] rpfsY = {1., 1. };
    final double[] rhs = {0., 1. };

    new MixedBivariateLogNormalModelVolatility(wghts, sigsX, sigsY, rpfsX, rpfsY, rhs);

  }

  /**
   * 
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void wrongLengthSigsX2Test() {
    final double[] wghts = {0.2, 0.8 };
    final double[] sigsX = {0.4, 0.6, 0.7 };
    final double[] sigsY = {0.45, 0.55 };
    final double[] rhs = {0., 1. };

    new MixedBivariateLogNormalModelVolatility(wghts, sigsX, sigsY, rhs);

  }

  /**
   * 
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void wrongLengthSigsY1Test() {
    final double[] wghts = {0.2, 0.8 };
    final double[] sigsX = {0.4, 0.6 };
    final double[] sigsY = {0.45, 0.55, 0.7 };
    final double[] rpfsX = {1., 1. };
    final double[] rpfsY = {1., 1. };
    final double[] rhs = {0., 1. };

    new MixedBivariateLogNormalModelVolatility(wghts, sigsX, sigsY, rpfsX, rpfsY, rhs);

  }

  /**
   * 
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void wrongLengthSigsY2Test() {
    final double[] wghts = {0.2, 0.8 };
    final double[] sigsX = {0.4, 0.6 };
    final double[] sigsY = {0.45, 0.55, 0.7 };
    final double[] rhs = {0., 1. };

    new MixedBivariateLogNormalModelVolatility(wghts, sigsX, sigsY, rhs);

  }

  /**
   * 
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void wrongLengthRpfsXTest() {
    final double[] wghts = {0.2, 0.8 };
    final double[] sigsX = {0.4, 0.6 };
    final double[] sigsY = {0.45, 0.55 };
    final double[] rpfsX = {1., 1., 1., };
    final double[] rpfsY = {1., 1. };
    final double[] rhs = {0., 1. };

    new MixedBivariateLogNormalModelVolatility(wghts, sigsX, sigsY, rpfsX, rpfsY, rhs);

  }

  /**
   * 
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void wrongLengthRpfsYTest() {
    final double[] wghts = {0.2, 0.8 };
    final double[] sigsX = {0.4, 0.6 };
    final double[] sigsY = {0.45, 0.55 };
    final double[] rpfsX = {1., 1. };
    final double[] rpfsY = {1., 1., 1. };
    final double[] rhs = {0., 1. };

    new MixedBivariateLogNormalModelVolatility(wghts, sigsX, sigsY, rpfsX, rpfsY, rhs);

  }

  /**
   * 
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void wrongLengthRhs1Test() {
    final double[] wghts = {0.2, 0.8 };
    final double[] sigsX = {0.4, 0.6 };
    final double[] sigsY = {0.45, 0.55 };
    final double[] rpfsX = {1., 1. };
    final double[] rpfsY = {1., 1. };
    final double[] rhs = {0., 1., 0.1 };

    new MixedBivariateLogNormalModelVolatility(wghts, sigsX, sigsY, rpfsX, rpfsY, rhs);

  }

  /**
   * 
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void wrongLengthRhs2Test() {
    final double[] wghts = {0.2, 0.8 };
    final double[] sigsX = {0.4, 0.6 };
    final double[] sigsY = {0.45, 0.55 };
    final double[] rhs = {0., 1., 0.1 };

    new MixedBivariateLogNormalModelVolatility(wghts, sigsX, sigsY, rhs);

  }

  /**
   * Tests below are for debugging
   */
  @Test
  (enabled = false)
  public void printTest() {

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

    final double[] weights = constWeights;

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

    for (int i = 0; i < 101; i++) {
      final double k = forwardZ * (0.5 + 1. * i / 100.);
      final EuropeanVanillaOption option = new EuropeanVanillaOption(k, timeToExpiry, true);
      final double vol1 = objZ.getImpliedVolatilityZ(option, forwardZ);
      final double vol2 = volfunc.getVolatility(option, forwardX, objX);
      final double vol3 = volfunc.getVolatility(option, forwardY, objY);
      System.out.println(k + "\t" + vol1 + "\t" + vol2 + "\t" + vol3);
    }
  }

  /**
   * 
   */
  @Test
  (enabled = false)
  public void printTestStrikeTime() {

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

    final double[] weights = constWeights;

    final MixedBivariateLogNormalModelVolatility objZ = new MixedBivariateLogNormalModelVolatility(weights, sigmasX,
        sigmasY, relativePartialForwardsX, relativePartialForwardsY, rhos);

    // final MixedBivariateLogNormalModelVolatility objZ = new MixedBivariateLogNormalModelVolatility(weights, sigmasX,
    //      sigmasY, rhos);
    //  final MixedLogNormalModelData objX = new MixedLogNormalModelData(constWeights, sigmasX);
    //  final MixedLogNormalModelData objY = new MixedLogNormalModelData(constWeights, sigmasY);

    final double forwardX = 1.1;
    final double forwardY = 0.9;
    final double forwardZ = forwardX / forwardY;

    for (int j = 0; j < 51; j++) {
      final double time = 0.5 * (0.5 + 2. * j / 100.);
      for (int i = 0; i < 51; i++) {
        final double k = forwardZ * (0.5 + 1. * i / 100.);
        final EuropeanVanillaOption option = new EuropeanVanillaOption(k, time, true);
        final double vol1 = objZ.getImpliedVolatilityZ(option, forwardZ);
        System.out.println(k + "\t" + time + "\t" + vol1);
      }
    }
  }

}
