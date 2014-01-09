/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate;

import static org.testng.AssertJUnit.assertTrue;

import java.util.BitSet;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.Test;
import org.threeten.bp.Period;
import org.threeten.bp.ZonedDateTime;

import com.opengamma.analytics.financial.instrument.index.GeneratorSwapFixedIbor;
import com.opengamma.analytics.financial.instrument.index.GeneratorSwapFixedIborMaster;
import com.opengamma.analytics.financial.instrument.swap.SwapFixedIborDefinition;
import com.opengamma.analytics.financial.instrument.swaption.SwaptionPhysicalFixedIborDefinition;
import com.opengamma.analytics.financial.interestrate.payments.derivative.Coupon;
import com.opengamma.analytics.financial.interestrate.swap.derivative.SwapFixedCoupon;
import com.opengamma.analytics.financial.interestrate.swaption.derivative.SwaptionPhysicalFixedIbor;
import com.opengamma.analytics.financial.interestrate.swaption.method.SwaptionPhysicalFixedIborSABRMethod;
import com.opengamma.analytics.financial.model.option.definition.SABRInterestRateDataBundle;
import com.opengamma.analytics.financial.model.option.definition.SABRInterestRateParameters;
import com.opengamma.analytics.financial.model.volatility.smile.fitting.SABRModelFitter;
import com.opengamma.analytics.financial.model.volatility.smile.fitting.SABRModelFitterTest;
import com.opengamma.analytics.financial.model.volatility.smile.function.SABRHaganVolatilityFunction;
import com.opengamma.analytics.financial.schedule.ScheduleCalculator;
import com.opengamma.analytics.math.interpolation.FlatExtrapolator1D;
import com.opengamma.analytics.math.interpolation.GridInterpolator2D;
import com.opengamma.analytics.math.interpolation.Interpolator1DFactory;
import com.opengamma.analytics.math.interpolation.LinearInterpolator1D;
import com.opengamma.analytics.math.matrix.DoubleMatrix1D;
import com.opengamma.analytics.math.matrix.DoubleMatrix2D;
import com.opengamma.analytics.math.statistics.leastsquare.LeastSquareResultsWithTransform;
import com.opengamma.analytics.math.surface.InterpolatedDoublesSurface;
import com.opengamma.analytics.util.time.TimeCalculator;
import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.financial.convention.calendar.MondayToFridayCalendar;
import com.opengamma.util.monitor.OperationTimer;
import com.opengamma.util.test.TestGroup;
import com.opengamma.util.time.DateUtils;
import com.opengamma.util.tuple.DoublesPair;
import com.opengamma.util.tuple.ObjectsPair;

/**
 * Tests related to the sensitivity of swaptions to the Black volatility when SABR fitting and interpolation is used.
 * @deprecated This class tests deprecated functionality.
 */
@Deprecated
@Test(groups = TestGroup.UNIT)
public class BlackSensitivityFromSABRSensitivityCalculatorTest {

  protected Logger _logger = LoggerFactory.getLogger(SABRModelFitterTest.class);
  private static final BitSet FIXED = new BitSet();
  static {
    FIXED.set(1);
  }
  private static final double ERROR = 0.0001;
  private static final SABRHaganVolatilityFunction SABR_FUNCTION = new SABRHaganVolatilityFunction();
  private static final DoubleMatrix1D SABR_INITIAL_VALUES = new DoubleMatrix1D(new double[] {0.05, 0.50, 0.70, 0.30 });
  private static final LinearInterpolator1D LINEAR = (LinearInterpolator1D) Interpolator1DFactory.getInterpolator(Interpolator1DFactory.LINEAR);
  private static final FlatExtrapolator1D FLAT = new FlatExtrapolator1D();
  private static final GridInterpolator2D INTERPOLATOR = new GridInterpolator2D(LINEAR, LINEAR, FLAT, FLAT);
  private static final Calendar NYC = new MondayToFridayCalendar("NYC");
  private static final GeneratorSwapFixedIbor USD6MLIBOR3M = GeneratorSwapFixedIborMaster.getInstance().getGenerator("USD6MLIBOR3M", NYC);

  private static final ZonedDateTime REFERENCE_DATE = DateUtils.getUTCDate(2008, 8, 18);

  private static final Period[] EXPIRY_TENOR = new Period[] {Period.ofMonths(6), Period.ofYears(1), Period.ofYears(5) };
  private static final int NB_EXPIRY = EXPIRY_TENOR.length;
  private static final ZonedDateTime[] EXPIRY_DATE = new ZonedDateTime[NB_EXPIRY];
  private static final double[] EXPIRY_TIME = new double[NB_EXPIRY];
  private static final Period[] MATURITY_TENOR = new Period[] {Period.ofYears(1), Period.ofYears(2), Period.ofYears(5), Period.ofYears(10) };
  private static final double[] MATURITY_TIME = new double[] {1.0, 2.0, 5.0, 10.0 };
  private static final int NB_MATURITY = MATURITY_TENOR.length;
  private static final double[] STRIKE_RELATIVE = new double[] {-0.0100, -0.0050, -0.0025, 0.0000, 0.0025, 0.0050, 0.0100 };
  private static final int NB_STRIKE = STRIKE_RELATIVE.length;
  private static final double[][][] VOLATILITIES_BLACK = new double[NB_EXPIRY][NB_MATURITY][NB_STRIKE];
  static {
    VOLATILITIES_BLACK[0] = new double[][] { {0.30, 0.27, 0.25, 0.23, 0.22, 0.22, 0.23 }, {0.30, 0.27, 0.25, 0.23, 0.22, 0.22, 0.23 }, {0.31, 0.28, 0.26, 0.24, 0.23, 0.23, 0.24 },
        {0.29, 0.27, 0.26, 0.25, 0.24, 0.24, 0.25 } }; // 6M
    VOLATILITIES_BLACK[1] = new double[][] { {0.30, 0.27, 0.25, 0.24, 0.24, 0.25, 0.27 }, {0.30, 0.27, 0.25, 0.23, 0.22, 0.22, 0.23 }, {0.31, 0.28, 0.26, 0.24, 0.23, 0.23, 0.24 },
        {0.29, 0.27, 0.26, 0.25, 0.24, 0.24, 0.25 } }; // 1Y
    VOLATILITIES_BLACK[2] = new double[][] { {0.33, 0.29, 0.27, 0.25, 0.24, 0.24, 0.24 }, {0.30, 0.27, 0.25, 0.23, 0.22, 0.22, 0.23 }, {0.31, 0.28, 0.26, 0.24, 0.23, 0.23, 0.24 },
        {0.29, 0.27, 0.26, 0.25, 0.24, 0.24, 0.25 } }; // 5Y
    for (int loopexpiry = 0; loopexpiry < NB_EXPIRY; loopexpiry++) {
      EXPIRY_DATE[loopexpiry] = ScheduleCalculator.getAdjustedDate(REFERENCE_DATE, EXPIRY_TENOR[loopexpiry], USD6MLIBOR3M.getIborIndex(), NYC);
      EXPIRY_TIME[loopexpiry] = TimeCalculator.getTimeBetween(REFERENCE_DATE, EXPIRY_DATE[loopexpiry]);
    }
  }

  private static final double NOTIONAL = 1000000;
  private static final Period EXPIRY_1_SWPT = Period.ofYears(2);
  private static final ZonedDateTime EXPIRY_1_SWPT_DATE = ScheduleCalculator.getAdjustedDate(REFERENCE_DATE, EXPIRY_1_SWPT, USD6MLIBOR3M.getIborIndex(), NYC);
  private static final ZonedDateTime SETTLE_1_SWPT_DATE = ScheduleCalculator.getAdjustedDate(EXPIRY_1_SWPT_DATE, USD6MLIBOR3M.getSpotLag(), NYC);
  private static final Period MATURITY_1_SWPT = Period.ofYears(6);
  private static final double STRIKE_1 = 0.0250;
  private static final SwapFixedIborDefinition SWAP_1_DEFINITION = SwapFixedIborDefinition.from(SETTLE_1_SWPT_DATE, MATURITY_1_SWPT, USD6MLIBOR3M, NOTIONAL, STRIKE_1, true);
  private static final SwaptionPhysicalFixedIborDefinition SWAPTION_1_DEFINITION = SwaptionPhysicalFixedIborDefinition.from(EXPIRY_1_SWPT_DATE, SWAP_1_DEFINITION, true, true);

  private static final Period EXPIRY_2_SWPT = Period.ofMonths(9);
  private static final ZonedDateTime EXPIRY_2_SWPT_DATE = ScheduleCalculator.getAdjustedDate(REFERENCE_DATE, EXPIRY_2_SWPT, USD6MLIBOR3M.getIborIndex(), NYC);
  private static final ZonedDateTime SETTLE_2_SWPT_DATE = ScheduleCalculator.getAdjustedDate(EXPIRY_1_SWPT_DATE, USD6MLIBOR3M.getSpotLag(), NYC);
  private static final Period MATURITY_2_SWPT = Period.ofYears(4);
  private static final double STRIKE_2 = 0.0300;
  private static final SwapFixedIborDefinition SWAP_2_DEFINITION = SwapFixedIborDefinition.from(SETTLE_2_SWPT_DATE, MATURITY_2_SWPT, USD6MLIBOR3M, NOTIONAL, STRIKE_2, true);
  private static final SwaptionPhysicalFixedIborDefinition SWAPTION_2_DEFINITION = SwaptionPhysicalFixedIborDefinition.from(EXPIRY_2_SWPT_DATE, SWAP_2_DEFINITION, true, true);

  private static final YieldCurveBundle CURVES = TestsDataSetsSABR.createCurves2();
  private static final String[] CURVE_NAMES = TestsDataSetsSABR.curves2Names();

  private static final int NB_INS = 2;
  private static final InstrumentDerivative[] INSTRUMENTS = new InstrumentDerivative[NB_INS];
  static {
    INSTRUMENTS[0] = SWAPTION_1_DEFINITION.toDerivative(REFERENCE_DATE, CURVE_NAMES);
    INSTRUMENTS[1] = SWAPTION_2_DEFINITION.toDerivative(REFERENCE_DATE, CURVE_NAMES);
  }

  private static final ParRateCalculator PRC = ParRateCalculator.getInstance();
  private static final SwaptionPhysicalFixedIborSABRMethod METHOD_SWAPTION_SABR = SwaptionPhysicalFixedIborSABRMethod.getInstance();
  private static final PresentValueSABRSensitivitySABRCalculator PVSSC_SABR = PresentValueSABRSensitivitySABRCalculator.getInstance();

  public ObjectsPair<SABRInterestRateParameters, HashMap<DoublesPair, DoubleMatrix2D>> calibration(final double[][][] volBlack) {
    final double[] expiryTimeVector = new double[NB_EXPIRY * NB_MATURITY];
    final double[] maturityTimeVector = new double[NB_EXPIRY * NB_MATURITY];
    final double[] alphaVector = new double[NB_EXPIRY * NB_MATURITY];
    final double[] betaVector = new double[NB_EXPIRY * NB_MATURITY];
    final double[] rhoVector = new double[NB_EXPIRY * NB_MATURITY];
    final double[] nuVector = new double[NB_EXPIRY * NB_MATURITY];
    int vect = 0;
    final HashMap<DoublesPair, DoubleMatrix2D> inverseJacobianMap = new HashMap<>();
    for (int loopexpiry = 0; loopexpiry < NB_EXPIRY; loopexpiry++) {
      final ZonedDateTime settleDate = ScheduleCalculator.getAdjustedDate(EXPIRY_DATE[loopexpiry], USD6MLIBOR3M.getSpotLag(), NYC);
      for (int loopmat = 0; loopmat < NB_MATURITY; loopmat++) {
        final SwapFixedIborDefinition swapDefinition = SwapFixedIborDefinition.from(settleDate, MATURITY_TENOR[loopmat], USD6MLIBOR3M, 1.0, 0.0, true); // used to compute atm
        final SwapFixedCoupon<Coupon> swap = swapDefinition.toDerivative(REFERENCE_DATE, CURVE_NAMES);
        final double atm = swap.accept(PRC, CURVES);
        final double[] strikeAbs = new double[NB_STRIKE];
        final double[] errors = new double[NB_STRIKE];
        for (int loopstr = 0; loopstr < NB_STRIKE; loopstr++) {
          strikeAbs[loopstr] = atm + STRIKE_RELATIVE[loopstr];
          errors[loopstr] = ERROR;
        }
        final LeastSquareResultsWithTransform fittedResult = new SABRModelFitter(atm, strikeAbs, EXPIRY_TIME[loopexpiry], volBlack[loopexpiry][loopmat], errors, SABR_FUNCTION).solve(
            SABR_INITIAL_VALUES, FIXED);
        inverseJacobianMap.put(DoublesPair.of(EXPIRY_TIME[loopexpiry], MATURITY_TIME[loopmat]), fittedResult.getModelParameterSensitivityToData());
        expiryTimeVector[vect] = EXPIRY_TIME[loopexpiry];
        maturityTimeVector[vect] = MATURITY_TIME[loopmat];
        alphaVector[vect] = fittedResult.getModelParameters().getEntry(0);
        betaVector[vect] = fittedResult.getModelParameters().getEntry(1);
        rhoVector[vect] = fittedResult.getModelParameters().getEntry(2);
        nuVector[vect] = fittedResult.getModelParameters().getEntry(3);
        vect++;
      }
    }
    final InterpolatedDoublesSurface alphaSurface = InterpolatedDoublesSurface.from(expiryTimeVector, maturityTimeVector, alphaVector, INTERPOLATOR, "SABR alpha surface");
    final InterpolatedDoublesSurface betaSurface = InterpolatedDoublesSurface.from(expiryTimeVector, maturityTimeVector, betaVector, INTERPOLATOR, "SABR beta surface");
    final InterpolatedDoublesSurface nuSurface = InterpolatedDoublesSurface.from(expiryTimeVector, maturityTimeVector, nuVector, INTERPOLATOR, "SABR nu surface");
    final InterpolatedDoublesSurface rhoSurface = InterpolatedDoublesSurface.from(expiryTimeVector, maturityTimeVector, rhoVector, INTERPOLATOR, "SABR rho surface");
    final SABRInterestRateParameters sabrParameters = new SABRInterestRateParameters(alphaSurface, betaSurface, rhoSurface, nuSurface, USD6MLIBOR3M.getFixedLegDayCount(), SABR_FUNCTION);
    return ObjectsPair.of(sabrParameters, inverseJacobianMap);
  }

  @Test
  public void blackNodeSensitivity() {

    final ObjectsPair<SABRInterestRateParameters, HashMap<DoublesPair, DoubleMatrix2D>> result = calibration(VOLATILITIES_BLACK);
    final SABRInterestRateParameters sabrParameters = result.getFirst();
    final HashMap<DoublesPair, DoubleMatrix2D> inverseJacobianMap = result.getSecond();
    final SABRInterestRateDataBundle sabrBundle = new SABRInterestRateDataBundle(sabrParameters, CURVES);

    final double bump = 0.001;
    final double[][][] sensiBlackFD = new double[NB_EXPIRY][NB_MATURITY][NB_STRIKE];

    final double[] pv = new double[NB_INS];

    final OperationTimer timer = new OperationTimer(_logger, "Calibrating {}x{}x{}x{} SABR and computing Black sensitivities for {} instruments", NB_INS, NB_EXPIRY, NB_MATURITY, NB_STRIKE, NB_INS);
    for (int loopins = 0; loopins < NB_INS; loopins++) {
      pv[loopins] = METHOD_SWAPTION_SABR.presentValue(INSTRUMENTS[loopins], sabrBundle).getAmount();
      final PresentValueSABRSensitivityDataBundle sensiPoint = INSTRUMENTS[loopins].accept(PVSSC_SABR, sabrBundle);
      final PresentValueSABRSensitivityDataBundle sensiNode = SABRSensitivityNodeCalculator.calculateNodeSensitivities(sensiPoint, sabrParameters);
      final Map<DoublesPair, DoubleMatrix1D> sensiBlack = BlackSensitivityFromSABRSensitivityCalculator.blackSensitivity(sensiNode, inverseJacobianMap);
      for (int loopexpiry = 0; loopexpiry < NB_EXPIRY; loopexpiry++) {
        for (int loopmat = 0; loopmat < NB_MATURITY; loopmat++) {
          for (int loopstr = 0; loopstr < NB_STRIKE; loopstr++) {
            final double[][][] bumpedVol = bump(loopexpiry, loopmat, loopstr, bump);
            final ObjectsPair<SABRInterestRateParameters, HashMap<DoublesPair, DoubleMatrix2D>> bumpedResult = calibration(bumpedVol);
            final SABRInterestRateDataBundle bumpedSabrBundle = new SABRInterestRateDataBundle(bumpedResult.getFirst(), CURVES);
            final double bumpedPv = METHOD_SWAPTION_SABR.presentValue(INSTRUMENTS[loopins], bumpedSabrBundle).getAmount();
            sensiBlackFD[loopexpiry][loopmat][loopstr] = (bumpedPv - pv[loopins]) / bump;
            final double sensiCalc = sensiBlack.get(DoublesPair.of(EXPIRY_TIME[loopexpiry], MATURITY_TIME[loopmat])).getEntry(loopstr);
            if (loopins != 0 && loopexpiry != 1 && loopmat != 2 && loopstr != 0) { // The first point has a larger error wrt FD.
              assertTrue("Black Node Sensitivity: FD [" + loopexpiry + ", " + loopmat + ", " + loopstr + "]",
                  Math.abs(sensiBlackFD[loopexpiry][loopmat][loopstr] - sensiCalc) < 25.0 || Math.abs(sensiBlackFD[loopexpiry][loopmat][loopstr] / sensiCalc - 1) < 0.05);
            }
          } // end loopstr
        } // end loopmat
      } // end loopexpiry
    } // end loopins
    timer.finished();
  }

  private double[][][] bump(final int exp, final int mat, final int str, final double bump) {
    final double[][][] vol = new double[NB_EXPIRY][NB_MATURITY][NB_STRIKE];
    for (int loopexpiry = 0; loopexpiry < NB_EXPIRY; loopexpiry++) {
      for (int loopmat = 0; loopmat < NB_MATURITY; loopmat++) {
        for (int loopstr = 0; loopstr < NB_STRIKE; loopstr++) {
          vol[loopexpiry][loopmat][loopstr] = VOLATILITIES_BLACK[loopexpiry][loopmat][loopstr];
        }
      }
    }
    vol[exp][mat][str] += bump;
    return vol;
  }

  @Test(enabled = false)
  /**
   * Analyzes the smoothness of the Black sensitivities to change in strike.
   */
  public void analysisSensitivitySmoothness() {

    final ObjectsPair<SABRInterestRateParameters, HashMap<DoublesPair, DoubleMatrix2D>> result = calibration(VOLATILITIES_BLACK);
    final SABRInterestRateParameters sabrParameters = result.getFirst();
    final HashMap<DoublesPair, DoubleMatrix2D> inverseJacobianMap = result.getSecond();
    final SABRInterestRateDataBundle sabrBundle = new SABRInterestRateDataBundle(sabrParameters, CURVES);

    final double strikeRange = 0.0600;
    final double strikeStart = 0.0050;
    final int nbStrikeSwapt = 100;
    final double[] strikes = new double[nbStrikeSwapt + 1];
    final SwaptionPhysicalFixedIbor[] swaptions = new SwaptionPhysicalFixedIbor[nbStrikeSwapt + 1];
    final double[] pv = new double[nbStrikeSwapt + 1];
    final double[][][][] blackSensi1 = new double[2][2][NB_STRIKE][nbStrikeSwapt + 1];
    for (int loopswpt = 0; loopswpt <= nbStrikeSwapt; loopswpt++) {
      strikes[loopswpt] = strikeStart + loopswpt * strikeRange / nbStrikeSwapt;
      final SwapFixedIborDefinition swapDefinition = SwapFixedIborDefinition.from(SETTLE_1_SWPT_DATE, MATURITY_1_SWPT, USD6MLIBOR3M, NOTIONAL, strikes[loopswpt], true);
      final SwaptionPhysicalFixedIborDefinition swaptionDefinition = SwaptionPhysicalFixedIborDefinition.from(EXPIRY_1_SWPT_DATE, swapDefinition, true, true);
      swaptions[loopswpt] = swaptionDefinition.toDerivative(REFERENCE_DATE, CURVE_NAMES);
      pv[loopswpt] = METHOD_SWAPTION_SABR.presentValue(swaptions[loopswpt], sabrBundle).getAmount();
      final PresentValueSABRSensitivityDataBundle sensiPoint = swaptions[loopswpt].accept(PVSSC_SABR, sabrBundle);
      final PresentValueSABRSensitivityDataBundle sensiNode = SABRSensitivityNodeCalculator.calculateNodeSensitivities(sensiPoint, sabrParameters);
      final Map<DoublesPair, DoubleMatrix1D> sensiBlack = BlackSensitivityFromSABRSensitivityCalculator.blackSensitivity(sensiNode, inverseJacobianMap);
      for (int loopexpiry = 0; loopexpiry < 2; loopexpiry++) {
        for (int loopmat = 0; loopmat < 2; loopmat++) {
          for (int loopstr = 0; loopstr < NB_STRIKE; loopstr++) {
            blackSensi1[loopexpiry][loopmat][loopstr][loopswpt] = sensiBlack.get(DoublesPair.of(EXPIRY_TIME[loopexpiry + 1], MATURITY_TIME[loopmat + 2])).getEntry(loopstr);
          }
        }
      }
    } // end loopswpt
    @SuppressWarnings("unused")
    final double atm = swaptions[0].getUnderlyingSwap().accept(PRC, sabrBundle);
  }

}
