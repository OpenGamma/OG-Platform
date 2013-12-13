/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.model.volatility.surface;

import static org.testng.AssertJUnit.assertEquals;

import org.testng.annotations.Test;

import com.opengamma.analytics.financial.model.option.definition.SmileDeltaParameters;
import com.opengamma.analytics.financial.model.volatility.VolatilityAndBucketedSensitivities;
import com.opengamma.analytics.math.interpolation.CombinedInterpolatorExtrapolatorFactory;
import com.opengamma.analytics.math.interpolation.Interpolator1D;
import com.opengamma.analytics.math.interpolation.Interpolator1DFactory;
import com.opengamma.analytics.math.interpolation.LinearInterpolator1D;
import com.opengamma.analytics.math.interpolation.data.ArrayInterpolator1DDataBundle;
import com.opengamma.util.test.TestGroup;
import com.opengamma.util.tuple.Triple;

/**
 * Tests related to the construction of term structure of smile data from delta.
 * Tests related to the interpolation of volatility.
 */
@Test(groups = TestGroup.UNIT)
public class SmileDeltaTermStructureParametersStrikeInterpolationTest {

  private static final double[] TIME_TO_EXPIRY = {0.10, 0.25, 0.50, 1.00, 2.00, 3.00};
  private static final double[] ATM = {0.175, 0.185, 0.18, 0.17, 0.16, 0.17};
  private static final double[] DELTA = new double[] {0.10, 0.25};
  private static final double[][] RISK_REVERSAL = new double[][] { {-0.010, -0.0050}, {-0.011, -0.0060}, {-0.012, -0.0070}, {-0.013, -0.0080}, {-0.014, -0.0090}, {-0.014, -0.0090}};
  private static final double[][] STRANGLE = new double[][] { {0.0300, 0.0100}, {0.0310, 0.0110}, {0.0320, 0.0120}, {0.0330, 0.0130}, {0.0340, 0.0140}, {0.0340, 0.0140}};
  private static final int NB_EXP = TIME_TO_EXPIRY.length;
  private static final SmileDeltaParameters[] VOLATILITY_TERM = new SmileDeltaParameters[NB_EXP];
  static {
    for (int loopexp = 0; loopexp < NB_EXP; loopexp++) {
      VOLATILITY_TERM[loopexp] = new SmileDeltaParameters(TIME_TO_EXPIRY[loopexp], ATM[loopexp], DELTA, RISK_REVERSAL[loopexp], STRANGLE[loopexp]);
    }
  }
  private static final Interpolator1D INTERPOLATOR_STRIKE = CombinedInterpolatorExtrapolatorFactory.getInterpolator(Interpolator1DFactory.LINEAR, Interpolator1DFactory.FLAT_EXTRAPOLATOR,
      Interpolator1DFactory.FLAT_EXTRAPOLATOR);
  private static final Interpolator1D INTERPOLATOR_TIME = CombinedInterpolatorExtrapolatorFactory.getInterpolator(Interpolator1DFactory.TIME_SQUARE, Interpolator1DFactory.FLAT_EXTRAPOLATOR,
      Interpolator1DFactory.FLAT_EXTRAPOLATOR);
  private static final SmileDeltaTermStructureParametersStrikeInterpolation SMILE_TERM = new SmileDeltaTermStructureParametersStrikeInterpolation(VOLATILITY_TERM, INTERPOLATOR_STRIKE);

  private static final double TOLERANCE_VOL = 1.0E-10;

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullVolatility() {
    new SmileDeltaTermStructureParametersStrikeInterpolation(null, INTERPOLATOR_STRIKE);
  }

  @Test
  public void getter() {
    assertEquals("Smile by delta term structure: volatility", VOLATILITY_TERM, SMILE_TERM.getVolatilityTerm());
  }

  @Test
  public void constructor() {
    final SmileDeltaTermStructureParametersStrikeInterpolation smileTerm2 = new SmileDeltaTermStructureParametersStrikeInterpolation(TIME_TO_EXPIRY, DELTA, ATM, RISK_REVERSAL, STRANGLE);
    assertEquals("Smile by delta term structure: constructor", SMILE_TERM, smileTerm2);
  }

  @Test
  public void constructor2() {
    final double[][] vol = new double[NB_EXP][];
    for (int loopexp = 0; loopexp < NB_EXP; loopexp++) {
      vol[loopexp] = VOLATILITY_TERM[loopexp].getVolatility();
    }
    final SmileDeltaTermStructureParametersStrikeInterpolation smileTermVol = new SmileDeltaTermStructureParametersStrikeInterpolation(TIME_TO_EXPIRY, DELTA, vol);
    assertEquals("Smile by delta term structure: constructor", SMILE_TERM, smileTermVol);
  }

  @Test
  /**
   * Tests the volatility at a point of the grid.
   */
  public void volatilityAtPoint() {
    final double forward = 1.40;
    final double timeToExpiration = 0.50;
    final double[] strikes = SMILE_TERM.getVolatilityTerm()[2].getStrike(forward);
    final double volComputed = SMILE_TERM.getVolatility(timeToExpiration, strikes[1], forward);
    final double volExpected = SMILE_TERM.getVolatilityTerm()[2].getVolatility()[1];
    assertEquals("Smile by delta term structure: volatility at a point", volExpected, volComputed, TOLERANCE_VOL);
  }

  @Test
  /**
   * Tests the interpolation in the strike dimension at a time of the grid.
   */
  public void volatilityStrikeInterpolation() {
    final double forward = 1.40;
    final double timeToExpiration = 0.50;
    final double strike = 1.50;
    final double[] strikes = SMILE_TERM.getVolatilityTerm()[2].getStrike(forward);
    final double[] vol = SMILE_TERM.getVolatilityTerm()[2].getVolatility();
    final ArrayInterpolator1DDataBundle volatilityInterpolation = new ArrayInterpolator1DDataBundle(strikes, vol);
    final LinearInterpolator1D interpolator = new LinearInterpolator1D();
    final double volExpected = interpolator.interpolate(volatilityInterpolation, strike);
    final double volComputed = SMILE_TERM.getVolatility(timeToExpiration, strike, forward);
    assertEquals("Smile by delta term structure: volatility interpolation on strike", volExpected, volComputed, TOLERANCE_VOL);
  }

  @Test
  /**
   * Tests the extrapolation below the first expiration.
   */
  public void volatilityBelowFirstExpiry() {
    final double forward = 1.40;
    final double timeToExpiration = 0.05;
    final double strike = 1.45;
    final SmileDeltaParameters smile = new SmileDeltaParameters(timeToExpiration, ATM[0], DELTA, RISK_REVERSAL[0], STRANGLE[0]);
    final double[] strikes = smile.getStrike(forward);
    final double[] vol = smile.getVolatility();
    final ArrayInterpolator1DDataBundle volatilityInterpolation = new ArrayInterpolator1DDataBundle(strikes, vol);
    final double volExpected = INTERPOLATOR_STRIKE.interpolate(volatilityInterpolation, strike);
    final double volComputed = SMILE_TERM.getVolatility(timeToExpiration, strike, forward);
    assertEquals("Smile by delta term structure: volatility interpolation on strike", volExpected, volComputed, TOLERANCE_VOL);
  }

  @Test
  /**
   * Tests the extrapolation above the last expiration.
   */
  public void volatilityAboveLastExpiry() {
    final double forward = 1.40;
    final double timeToExpiration = 5.00;
    final double strike = 1.45;
    final SmileDeltaParameters smile = new SmileDeltaParameters(timeToExpiration, ATM[NB_EXP - 1], DELTA, RISK_REVERSAL[NB_EXP - 1], STRANGLE[NB_EXP - 1]);
    final double[] strikes = smile.getStrike(forward);
    final double[] vol = smile.getVolatility();
    final ArrayInterpolator1DDataBundle volatilityInterpolation = new ArrayInterpolator1DDataBundle(strikes, vol);
    final double volExpected = INTERPOLATOR_STRIKE.interpolate(volatilityInterpolation, strike);
    final double volComputed = SMILE_TERM.getVolatility(timeToExpiration, strike, forward);
    assertEquals("Smile by delta term structure: volatility interpolation on strike", volExpected, volComputed, TOLERANCE_VOL);
  }

  @Test
  /**
   * Tests the interpolation in the time and strike dimensions.
   */
  public void volatilityTimeInterpolation() {
    final double forward = 1.40;
    final double timeToExpiration = 0.75;
    final double strike = 1.50;
    final double[] vol050 = SMILE_TERM.getVolatilityTerm()[2].getVolatility();
    final double[] vol100 = SMILE_TERM.getVolatilityTerm()[3].getVolatility();
    final double[] vol = new double[vol050.length];
    for (int loopvol = 0; loopvol < vol050.length; loopvol++) {
      vol[loopvol] = Math.sqrt(((vol050[loopvol] * vol050[loopvol] * TIME_TO_EXPIRY[2] + vol100[loopvol] * vol100[loopvol] * TIME_TO_EXPIRY[3]) / 2.0) / timeToExpiration);
    }
    final SmileDeltaParameters smile = new SmileDeltaParameters(timeToExpiration, DELTA, vol);
    final double[] strikes = smile.getStrike(forward);
    final ArrayInterpolator1DDataBundle volatilityInterpolation = new ArrayInterpolator1DDataBundle(strikes, vol);
    final LinearInterpolator1D interpolator = new LinearInterpolator1D();
    final double volExpected = interpolator.interpolate(volatilityInterpolation, strike);
    final double volComputed = SMILE_TERM.getVolatility(timeToExpiration, strike, forward);
    assertEquals("Smile by delta term structure: volatility interpolation on strike", volExpected, volComputed, TOLERANCE_VOL);
    final double volTriple = SMILE_TERM.getVolatility(new Triple<>(timeToExpiration, strike, forward));
    assertEquals("Smile by delta term structure: volatility interpolation on strike", volComputed, volTriple, TOLERANCE_VOL);
    final SmileDeltaTermStructureParametersStrikeInterpolation smileTerm2 = new SmileDeltaTermStructureParametersStrikeInterpolation(TIME_TO_EXPIRY, DELTA, ATM, RISK_REVERSAL, STRANGLE,
        INTERPOLATOR_STRIKE, INTERPOLATOR_TIME);
    final double volComputed2 = smileTerm2.getVolatility(timeToExpiration, strike, forward);
    assertEquals("Smile by delta term structure: volatility interpolation on strike", volComputed, volComputed2, TOLERANCE_VOL);
  }

  @Test
  /**
   * Tests the interpolation and its derivative with respect to the data by comparison to finite difference.
   */
  public void volatilityAjoint() {
    final double forward = 1.40;
    final double[] timeToExpiration = new double[] {0.75, 1.00, 2.50};
    final double[] strike = new double[] {1.50, 1.70, 2.20};
    final double[] tolerance = new double[] {3.0E-2, 1.0E-1, 1.0E-5};
    final int nbTest = strike.length;
    final double shift = 0.00001;
    for (int looptest = 0; looptest < nbTest; looptest++) {
      final double vol = SMILE_TERM.getVolatility(timeToExpiration[looptest], strike[looptest], forward);
      final double[][] bucketTest = new double[TIME_TO_EXPIRY.length][2 * DELTA.length + 1];
      final VolatilityAndBucketedSensitivities volComputed = SMILE_TERM.getVolatilityAndSensitivities(timeToExpiration[looptest], strike[looptest], forward);
      final double[][] bucketSensi = volComputed.getBucketedSensitivities();
      assertEquals("Smile by delta term structure: volatility adjoint", vol, volComputed.getVolatility(), 1.0E-10);
      final SmileDeltaParameters[] volData = new SmileDeltaParameters[TIME_TO_EXPIRY.length];
      final double[] volBumped = new double[2 * DELTA.length + 1];
      for (int loopexp = 0; loopexp < TIME_TO_EXPIRY.length; loopexp++) {
        for (int loopsmile = 0; loopsmile < 2 * DELTA.length + 1; loopsmile++) {
          System.arraycopy(SMILE_TERM.getVolatilityTerm(), 0, volData, 0, TIME_TO_EXPIRY.length);
          System.arraycopy(SMILE_TERM.getVolatilityTerm()[loopexp].getVolatility(), 0, volBumped, 0, 2 * DELTA.length + 1);
          volBumped[loopsmile] += shift;
          volData[loopexp] = new SmileDeltaParameters(TIME_TO_EXPIRY[loopexp], DELTA, volBumped);
          final SmileDeltaTermStructureParametersStrikeInterpolation smileTermBumped = new SmileDeltaTermStructureParametersStrikeInterpolation(volData, INTERPOLATOR_STRIKE);
          bucketTest[loopexp][loopsmile] = (smileTermBumped.getVolatility(timeToExpiration[looptest], strike[looptest], forward) - volComputed.getVolatility()) / shift;
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
    final double forward = 1.40;
    final double expiryMax = 2.0;
    final int nbExp = 50;
    final int nbVol = 2 * DELTA.length + 1;
    final double[][] strikes = new double[nbExp][nbVol];
    final double[] expiries = new double[nbExp];
    final double[] variancePeriodT = new double[nbVol];
    final double[] volatilityT = new double[nbVol];
    final double[] variancePeriod0 = new double[nbVol];
    final double[] variancePeriod1 = new double[nbVol];
    for (int loopexp = 0; loopexp < nbExp; loopexp++) {
      expiries[loopexp] = loopexp * expiryMax / nbExp;
      final ArrayInterpolator1DDataBundle interpData = new ArrayInterpolator1DDataBundle(TIME_TO_EXPIRY, new double[NB_EXP]);
      final int indexLower = interpData.getLowerBoundIndex(expiries[loopexp]);
      if (expiries[loopexp] < 1.0E-10) {
        for (int loopvol = 0; loopvol < nbVol; loopvol++) {
          volatilityT[loopvol] = SMILE_TERM.getVolatilityTerm()[indexLower].getVolatility()[loopvol];
        }
      } else {
        final double weight0 = (TIME_TO_EXPIRY[indexLower + 1] - expiries[loopexp]) / (TIME_TO_EXPIRY[indexLower + 1] - TIME_TO_EXPIRY[indexLower]);
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
      final SmileDeltaParameters smile = new SmileDeltaParameters(expiries[loopexp], DELTA, volatilityT);
      strikes[loopexp] = smile.getStrike(forward);
    }
  }

  @Test(enabled = false)
  /**
   * Analysis the code performance. In normal tests, should be (enabled=false).
   */
  public void performance() {
    long startTime, endTime;
    final int nbTest = 100000;

    final double forward = 1.40;
    final double timeToExpiration = 0.50;
    final double strike = 1.50;

    @SuppressWarnings("unused")
    double volComputed;

    startTime = System.currentTimeMillis();
    for (int looptest = 0; looptest < nbTest; looptest++) {
      volComputed = SMILE_TERM.getVolatility(timeToExpiration, strike, forward);
    }
    endTime = System.currentTimeMillis();
    System.out.println(nbTest + " Smile Delta volatility: " + (endTime - startTime) + " ms");
    // Performance note: price: 18-Jun-12: On Mac Pro 3.2 GHz Quad-Core Intel Xeon: 225 ms for 100000 volatilities.
  }

}
