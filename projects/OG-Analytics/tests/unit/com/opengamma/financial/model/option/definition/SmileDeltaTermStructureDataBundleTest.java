/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.model.option.definition;

import static org.testng.AssertJUnit.assertEquals;

import org.testng.annotations.Test;

import com.opengamma.math.interpolation.LinearInterpolator1D;
import com.opengamma.math.interpolation.data.ArrayInterpolator1DDataBundle;
import com.opengamma.util.tuple.Triple;

/**
 * Tests related to the construction of term structure of smile data from delta.
 * Tests related to the interpolation of volatility.
 */
public class SmileDeltaTermStructureDataBundleTest {

  private static final double[] TIME_TO_EXPIRY = {0.0, 0.25, 0.50, 1.00, 2.00};
  private static final double[] ATM = {0.175, 0.185, 0.18, 0.17, 0.16};
  private static final double[][] DELTA = new double[][] { {0.10, 0.25}, {0.10, 0.25}, {0.10, 0.25}, {0.10, 0.25}, {0.10, 0.25}};
  private static final double[][] RISK_REVERSAL = new double[][] { {-0.010, -0.0050}, {-0.011, -0.0060}, {-0.012, -0.0070}, {-0.013, -0.0080}, {-0.014, -0.0090}};
  private static final double[][] STRANGLE = new double[][] { {0.0300, 0.0100}, {0.0310, 0.0110}, {0.0320, 0.0120}, {0.0330, 0.0130}, {0.0340, 0.0140}};
  private static final int NB_EXP = TIME_TO_EXPIRY.length;
  private static final SmileDeltaParameter[] VOLATILITY_TERM = new SmileDeltaParameter[NB_EXP];
  static {
    for (int loopexp = 0; loopexp < NB_EXP; loopexp++) {
      VOLATILITY_TERM[loopexp] = new SmileDeltaParameter(TIME_TO_EXPIRY[loopexp], ATM[loopexp], DELTA[loopexp], RISK_REVERSAL[loopexp], STRANGLE[loopexp]);
    }
  }
  private static final SmileDeltaTermStructureParameter SMILE_TERM = new SmileDeltaTermStructureParameter(VOLATILITY_TERM);

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullVolatility() {
    new SmileDeltaTermStructureParameter(null);
  }

  @Test
  public void getter() {
    assertEquals("Smile by delta term structure: volatility", VOLATILITY_TERM, SMILE_TERM.getVolatilityTerm());
  }

  @Test
  /**
   * Tests the volatility at a point of the grid.
   */
  public void volatilityAtPoint() {
    double forward = 1.40;
    double timeToExpiration = 0.50;
    double[] strikes = SMILE_TERM.getVolatilityTerm()[2].getStrike(forward);
    double volComputed = SMILE_TERM.getVolatility(timeToExpiration, strikes[1], forward);
    double volExpected = SMILE_TERM.getVolatilityTerm()[2].getVolatility()[1];
    assertEquals("Smile by delta term structure: volatility at a point", volExpected, volComputed, 1.0E-10);
  }

  @Test
  /**
   * Tests the interpolation in the strike dimension at a time of the grid.
   */
  public void volatilityStrikeInterpolation() {
    double forward = 1.40;
    double timeToExpiration = 0.50;
    double strike = 1.50;
    double[] strikes = SMILE_TERM.getVolatilityTerm()[2].getStrike(forward);
    double[] vol = SMILE_TERM.getVolatilityTerm()[2].getVolatility();
    ArrayInterpolator1DDataBundle volatilityInterpolation = new ArrayInterpolator1DDataBundle(strikes, vol);
    LinearInterpolator1D interpolator = new LinearInterpolator1D();
    double volExpected = interpolator.interpolate(volatilityInterpolation, strike);
    double volComputed = SMILE_TERM.getVolatility(timeToExpiration, strike, forward);
    assertEquals("Smile by delta term structure: volatility interpolation on strike", volExpected, volComputed, 1.0E-10);
  }

  @Test
  /**
   * Tests the interpolation in the time and strike dimensions.
   */
  public void volatilityTimeInterpolation() {
    double forward = 1.40;
    double timeToExpiration = 0.75;
    double strike = 1.50;
    double[] vol050 = SMILE_TERM.getVolatilityTerm()[2].getVolatility();
    double[] vol100 = SMILE_TERM.getVolatilityTerm()[3].getVolatility();
    double[] vol = new double[vol050.length];
    for (int loopvol = 0; loopvol < vol050.length; loopvol++) {
      vol[loopvol] = Math.sqrt(((vol050[loopvol] * vol050[loopvol] * TIME_TO_EXPIRY[2] + vol100[loopvol] * vol100[loopvol] * TIME_TO_EXPIRY[3]) / 2.0) / timeToExpiration);
    }
    SmileDeltaParameter smile = new SmileDeltaParameter(timeToExpiration, DELTA[0], vol);
    double[] strikes = smile.getStrike(forward);
    ArrayInterpolator1DDataBundle volatilityInterpolation = new ArrayInterpolator1DDataBundle(strikes, vol);
    LinearInterpolator1D interpolator = new LinearInterpolator1D();
    double volExpected = interpolator.interpolate(volatilityInterpolation, strike);
    double volComputed = SMILE_TERM.getVolatility(timeToExpiration, strike, forward);
    assertEquals("Smile by delta term structure: volatility interpolation on strike", volExpected, volComputed, 1.0E-10);
    double volTriple = SMILE_TERM.getVolatility(new Triple<Double, Double, Double>(timeToExpiration, strike, forward));
    assertEquals("Smile by delta term structure: volatility interpolation on strike", volComputed, volTriple, 1.0E-10);
  }

}
