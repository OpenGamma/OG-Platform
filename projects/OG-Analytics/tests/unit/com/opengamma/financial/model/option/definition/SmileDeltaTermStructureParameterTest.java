/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.model.option.definition;

import static org.testng.AssertJUnit.assertEquals;

import org.testng.annotations.Test;

import com.opengamma.math.interpolation.CombinedInterpolatorExtrapolatorFactory;
import com.opengamma.math.interpolation.Interpolator1D;
import com.opengamma.math.interpolation.Interpolator1DFactory;
import com.opengamma.math.interpolation.LinearInterpolator1D;
import com.opengamma.math.interpolation.data.ArrayInterpolator1DDataBundle;
import com.opengamma.util.tuple.Triple;

/**
 * Tests related to the construction of term structure of smile data from delta.
 * Tests related to the interpolation of volatility.
 */
public class SmileDeltaTermStructureParameterTest {

  private static final double[] TIME_TO_EXPIRY = {0.10, 0.25, 0.50, 1.00, 2.00, 3.00};
  private static final double[] ATM = {0.175, 0.185, 0.18, 0.17, 0.16, 0.17};
  private static final double[] DELTA = new double[] {0.10, 0.25};
  private static final double[][] RISK_REVERSAL = new double[][] { {-0.010, -0.0050}, {-0.011, -0.0060}, {-0.012, -0.0070}, {-0.013, -0.0080}, {-0.014, -0.0090}, {-0.014, -0.0090}};
  private static final double[][] STRANGLE = new double[][] { {0.0300, 0.0100}, {0.0310, 0.0110}, {0.0320, 0.0120}, {0.0330, 0.0130}, {0.0340, 0.0140}, {0.0340, 0.0140}};
  private static final int NB_EXP = TIME_TO_EXPIRY.length;
  private static final SmileDeltaParameter[] VOLATILITY_TERM = new SmileDeltaParameter[NB_EXP];
  static {
    for (int loopexp = 0; loopexp < NB_EXP; loopexp++) {
      VOLATILITY_TERM[loopexp] = new SmileDeltaParameter(TIME_TO_EXPIRY[loopexp], ATM[loopexp], DELTA, RISK_REVERSAL[loopexp], STRANGLE[loopexp]);
    }
  }
  private static final SmileDeltaTermStructureParameter SMILE_TERM = new SmileDeltaTermStructureParameter(VOLATILITY_TERM);

  private static final Interpolator1D INTERPOLATOR = CombinedInterpolatorExtrapolatorFactory.getInterpolator(Interpolator1DFactory.LINEAR, Interpolator1DFactory.FLAT_EXTRAPOLATOR,
      Interpolator1DFactory.FLAT_EXTRAPOLATOR);

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullVolatility() {
    new SmileDeltaTermStructureParameter(null);
  }

  @Test
  public void getter() {
    assertEquals("Smile by delta term structure: volatility", VOLATILITY_TERM, SMILE_TERM.getVolatilityTerm());
  }

  @Test
  public void constructor() {
    SmileDeltaTermStructureParameter smileTerm2 = new SmileDeltaTermStructureParameter(TIME_TO_EXPIRY, DELTA, ATM, RISK_REVERSAL, STRANGLE);
    assertEquals("Smile by delta term structure: constructor", SMILE_TERM, smileTerm2);
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
   * Tests the extrapolation below the first expiration.
   */
  public void volatilityBelowFirstExpiry() {
    double forward = 1.40;
    double timeToExpiration = 0.05;
    double strike = 1.45;
    SmileDeltaParameter smile = new SmileDeltaParameter(timeToExpiration, ATM[0], DELTA, RISK_REVERSAL[0], STRANGLE[0]);
    double[] strikes = smile.getStrike(forward);
    double[] vol = smile.getVolatility();
    ArrayInterpolator1DDataBundle volatilityInterpolation = new ArrayInterpolator1DDataBundle(strikes, vol);
    double volExpected = INTERPOLATOR.interpolate(volatilityInterpolation, strike);
    double volComputed = SMILE_TERM.getVolatility(timeToExpiration, strike, forward);
    assertEquals("Smile by delta term structure: volatility interpolation on strike", volExpected, volComputed, 1.0E-10);
  }

  @Test
  /**
   * Tests the extrapolation above the last expiration.
   */
  public void volatilityAboveLastExpiry() {
    double forward = 1.40;
    double timeToExpiration = 5.00;
    double strike = 1.45;
    SmileDeltaParameter smile = new SmileDeltaParameter(timeToExpiration, ATM[NB_EXP - 1], DELTA, RISK_REVERSAL[NB_EXP - 1], STRANGLE[NB_EXP - 1]);
    double[] strikes = smile.getStrike(forward);
    double[] vol = smile.getVolatility();
    ArrayInterpolator1DDataBundle volatilityInterpolation = new ArrayInterpolator1DDataBundle(strikes, vol);
    double volExpected = INTERPOLATOR.interpolate(volatilityInterpolation, strike);
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
    SmileDeltaParameter smile = new SmileDeltaParameter(timeToExpiration, DELTA, vol);
    double[] strikes = smile.getStrike(forward);
    ArrayInterpolator1DDataBundle volatilityInterpolation = new ArrayInterpolator1DDataBundle(strikes, vol);
    LinearInterpolator1D interpolator = new LinearInterpolator1D();
    double volExpected = interpolator.interpolate(volatilityInterpolation, strike);
    double volComputed = SMILE_TERM.getVolatility(timeToExpiration, strike, forward);
    assertEquals("Smile by delta term structure: volatility interpolation on strike", volExpected, volComputed, 1.0E-10);
    double volTriple = SMILE_TERM.getVolatility(new Triple<Double, Double, Double>(timeToExpiration, strike, forward));
    assertEquals("Smile by delta term structure: volatility interpolation on strike", volComputed, volTriple, 1.0E-10);
  }

  @Test
  /**
   * Tests the interpolation and its derivative with respect to the data.
   */
  public void volatilityAjoint() {
    double forward = 1.40;
    double[] timeToExpiration = new double[] {0.75, 1.00, 2.50};
    double[] strike = new double[] {1.50, 1.70, 2.20};
    double[] tolerance = new double[] {3.0E-2, 1.0E-1, 1.0E-5};
    int nbTest = strike.length;
    double shift = 0.00001;
    for (int looptest = 0; looptest < nbTest; looptest++) {
      double vol = SMILE_TERM.getVolatility(timeToExpiration[looptest], strike[looptest], forward);
      double[][] bucketSensi = new double[TIME_TO_EXPIRY.length][2 * DELTA.length + 1];
      double[][] bucketTest = new double[TIME_TO_EXPIRY.length][2 * DELTA.length + 1];
      double volComputed = SMILE_TERM.getVolatility(timeToExpiration[looptest], strike[looptest], forward, bucketSensi);
      assertEquals("Smile by delta term structure: volatility adjoint", vol, volComputed, 1.0E-10);
      SmileDeltaParameter[] volData = new SmileDeltaParameter[TIME_TO_EXPIRY.length];
      double[] volBumped = new double[2 * DELTA.length + 1];
      for (int loopexp = 0; loopexp < TIME_TO_EXPIRY.length; loopexp++) {
        for (int loopsmile = 0; loopsmile < 2 * DELTA.length + 1; loopsmile++) {
          System.arraycopy(SMILE_TERM.getVolatilityTerm(), 0, volData, 0, TIME_TO_EXPIRY.length);
          System.arraycopy(SMILE_TERM.getVolatilityTerm()[loopexp].getVolatility(), 0, volBumped, 0, 2 * DELTA.length + 1);
          volBumped[loopsmile] += shift;
          volData[loopexp] = new SmileDeltaParameter(TIME_TO_EXPIRY[loopexp], DELTA, volBumped);
          SmileDeltaTermStructureParameter smileTermBumped = new SmileDeltaTermStructureParameter(volData);
          bucketTest[loopexp][loopsmile] = (smileTermBumped.getVolatility(timeToExpiration[looptest], strike[looptest], forward) - volComputed) / shift;
          // FIXME: the strike sensitivity to volatility is missing. To be corrected when [PLAT-1396] is fixed.
          assertEquals("Smile by delta term structure: (test: " + looptest + ") volatility bucket sensitivity " + loopexp + " - " + loopsmile, bucketTest[loopexp][loopsmile],
              bucketSensi[loopexp][loopsmile], tolerance[looptest]);
        }
      }
    }
  }

  @Test(enabled = false)
  /**
   * Code to graph the strikes for the given deltas at different expirations. In normal tests, should be (enabled=false). 
   */
  public void deltaSmile() {
    double forward = 1.40;
    double expiryMax = 2.0;
    int nbExp = 50;
    int nbVol = 2 * DELTA.length + 1;
    double[][] strikes = new double[nbExp][nbVol];
    double[] expiries = new double[nbExp];
    double[] variancePeriodT = new double[nbVol];
    double[] volatilityT = new double[nbVol];
    double[] variancePeriod0 = new double[nbVol];
    double[] variancePeriod1 = new double[nbVol];
    for (int loopexp = 0; loopexp < nbExp; loopexp++) {
      expiries[loopexp] = loopexp * expiryMax / nbExp;
      ArrayInterpolator1DDataBundle interpData = new ArrayInterpolator1DDataBundle(TIME_TO_EXPIRY, new double[NB_EXP]);
      int indexLower = interpData.getLowerBoundIndex(expiries[loopexp]);
      if (expiries[loopexp] < 1.0E-10) {
        for (int loopvol = 0; loopvol < nbVol; loopvol++) {
          volatilityT[loopvol] = SMILE_TERM.getVolatilityTerm()[indexLower].getVolatility()[loopvol];
        }
      } else {
        double weight0 = (TIME_TO_EXPIRY[indexLower + 1] - expiries[loopexp]) / (TIME_TO_EXPIRY[indexLower + 1] - TIME_TO_EXPIRY[indexLower]);
        // Implementation note: Linear interpolation on variance over the period (s^2*t).
        for (int loopvol = 0; loopvol < nbVol; loopvol++) {
          variancePeriod0[loopvol] = SMILE_TERM.getVolatilityTerm()[indexLower].getVolatility()[loopvol] * SMILE_TERM.getVolatilityTerm()[indexLower].getVolatility()[loopvol]
              * TIME_TO_EXPIRY[indexLower];
          variancePeriod1[loopvol] = SMILE_TERM.getVolatilityTerm()[indexLower + 1].getVolatility()[loopvol] * SMILE_TERM.getVolatilityTerm()[indexLower + 1].getVolatility()[loopvol]
              * TIME_TO_EXPIRY[indexLower + 1];
          variancePeriodT[loopvol] = weight0 * variancePeriod0[loopvol] + (1 - weight0) * variancePeriod1[loopvol];
          volatilityT[loopvol] = Math.sqrt(variancePeriodT[loopvol] / expiries[loopexp]);
        }
      }
      SmileDeltaParameter smile = new SmileDeltaParameter(expiries[loopexp], DELTA, volatilityT);
      strikes[loopexp] = smile.getStrike(forward);
    }
  }

}
