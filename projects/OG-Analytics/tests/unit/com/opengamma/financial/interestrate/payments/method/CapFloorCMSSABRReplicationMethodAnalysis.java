/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.interestrate.payments.method;

import javax.time.calendar.Period;
import javax.time.calendar.ZonedDateTime;

import org.testng.annotations.Test;

import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.financial.convention.calendar.MondayToFridayCalendar;
import com.opengamma.financial.convention.daycount.DayCount;
import com.opengamma.financial.convention.daycount.DayCountFactory;
import com.opengamma.financial.instrument.annuity.AnnuityCapFloorCMSDefinition;
import com.opengamma.financial.instrument.index.IndexSwap;
import com.opengamma.financial.instrument.index.SwapGenerator;
import com.opengamma.financial.instrument.index.generator.USD6MLIBOR3M;
import com.opengamma.financial.interestrate.InterestRateCurveSensitivity;
import com.opengamma.financial.interestrate.ParRateCalculator;
import com.opengamma.financial.interestrate.PresentValueCurveSensitivitySABRCalculator;
import com.opengamma.financial.interestrate.PresentValueNodeSensitivityCalculator;
import com.opengamma.financial.interestrate.PresentValueSABRCalculator;
import com.opengamma.financial.interestrate.TestsDataSets;
import com.opengamma.financial.interestrate.YieldCurveBundle;
import com.opengamma.financial.interestrate.annuity.definition.GenericAnnuity;
import com.opengamma.financial.interestrate.payments.CapFloorCMS;
import com.opengamma.financial.interestrate.payments.Payment;
import com.opengamma.financial.model.interestrate.curve.YieldAndDiscountCurve;
import com.opengamma.financial.model.interestrate.curve.YieldCurve;
import com.opengamma.financial.model.option.definition.SABRInterestRateDataBundle;
import com.opengamma.financial.model.option.definition.SABRInterestRateParameters;
import com.opengamma.financial.schedule.ScheduleCalculator;
import com.opengamma.math.curve.InterpolatedDoublesCurve;
import com.opengamma.math.interpolation.CombinedInterpolatorExtrapolatorFactory;
import com.opengamma.math.interpolation.Interpolator1D;
import com.opengamma.math.interpolation.Interpolator1DFactory;
import com.opengamma.math.matrix.DoubleMatrix1D;
import com.opengamma.util.time.DateUtils;

/**
 *  Analysis PV and sensitivities for CMS caplet/floorlet with a SABR smile.
 */
public class CapFloorCMSSABRReplicationMethodAnalysis {

  private static final String DSC_CURVE_NAME = "USD Discounting";
  private static final String FWD_CURVE_NAME = "USD LIBOR 3M";
  private static final String[] CURVE_NAME = new String[] {DSC_CURVE_NAME, FWD_CURVE_NAME};
  private static final double[] NODE_TIME = new double[] {1.0, 2.0, 3.0, 4.0, 5.0, 6.0, 7.0, 8.0, 9.0, 10.0, 15.0, 20.0, 25.0};
  private static final double[] DSC_RATE_INIT = new double[] {0.0200, 0.0200, 0.0200, 0.0200, 0.0210, 0.0220, 0.0230, 0.0240, 0.0250, 0.0260, 0.0270, 0.0280, 0.0290};
  private static final double[] FWD_RATE_INIT = new double[] {0.0200, 0.0200, 0.0200, 0.0250, 0.0250, 0.0260, 0.0260, 0.0280, 0.0270, 0.0290, 0.0290, 0.0280, 0.0290};

  private static final Calendar CALENDAR = new MondayToFridayCalendar("USD Calendar");
  private static final SwapGenerator USD_GENERATOR = new USD6MLIBOR3M(CALENDAR);
  private static final IndexSwap USD_SWAP_10Y = new IndexSwap(USD_GENERATOR, Period.ofYears(5));

  private static final ZonedDateTime REFERENCE_DATE = DateUtils.getUTCDate(2011, 12, 07);
  private static final ZonedDateTime SPOT_DATE = ScheduleCalculator.getAdjustedDate(REFERENCE_DATE, CALENDAR, USD_GENERATOR.getIborIndex().getSpotLag());

  private static final Period START_CMSCAP = Period.ofYears(5);
  private static final Period LENGTH_CMSCAP = Period.ofYears(10);

  private static final ZonedDateTime START_DATE = ScheduleCalculator.getAdjustedDate(SPOT_DATE, USD_GENERATOR.getIborIndex().getBusinessDayConvention(), CALENDAR, USD_GENERATOR.getIborIndex()
      .isEndOfMonth(), START_CMSCAP);
  private static final ZonedDateTime END_DATE = START_DATE.plus(LENGTH_CMSCAP);

  private static final double NOTIONAL = 1000000.0; //1m
  private static final double STRIKE = 0.03;
  private static final boolean IS_CAP = true;
  private static final Period CAP_PERIOD = Period.ofMonths(6);
  private static final DayCount CAP_DAYCOUNT = DayCountFactory.INSTANCE.getDayCount("ACT/360");

  private static final AnnuityCapFloorCMSDefinition CAP_DEFINITION = AnnuityCapFloorCMSDefinition.from(START_DATE, END_DATE, NOTIONAL, USD_SWAP_10Y, CAP_PERIOD, CAP_DAYCOUNT, false, STRIKE, IS_CAP);
  private static final GenericAnnuity<? extends Payment> CAP = CAP_DEFINITION.toDerivative(REFERENCE_DATE, CURVE_NAME);
  private static final AnnuityCapFloorCMSDefinition FLOOR_DEFINITION = AnnuityCapFloorCMSDefinition
      .from(START_DATE, END_DATE, NOTIONAL, USD_SWAP_10Y, CAP_PERIOD, CAP_DAYCOUNT, false, STRIKE, !IS_CAP);
  private static final GenericAnnuity<? extends Payment> FLOOR = FLOOR_DEFINITION.toDerivative(REFERENCE_DATE, CURVE_NAME);

  private static final PresentValueSABRCalculator PVC = PresentValueSABRCalculator.getInstance();
  private static final PresentValueCurveSensitivitySABRCalculator PVCSC = PresentValueCurveSensitivitySABRCalculator.getInstance();
  private static final PresentValueNodeSensitivityCalculator PVNSC = PresentValueNodeSensitivityCalculator.getDefaultInstance();
  private static final ParRateCalculator PRC = ParRateCalculator.getInstance();

  @Test
  public void init() {
    // Curves
    Interpolator1D interpolator = CombinedInterpolatorExtrapolatorFactory.getInterpolator(Interpolator1DFactory.DOUBLE_QUADRATIC, Interpolator1DFactory.FLAT_EXTRAPOLATOR);
    InterpolatedDoublesCurve dscCurve = new InterpolatedDoublesCurve(NODE_TIME, DSC_RATE_INIT, interpolator, true);
    InterpolatedDoublesCurve fwdCurve = new InterpolatedDoublesCurve(NODE_TIME, FWD_RATE_INIT, interpolator, true);
    final YieldAndDiscountCurve dscYC = new YieldCurve(dscCurve);
    final YieldAndDiscountCurve fwdYC = new YieldCurve(fwdCurve);
    final YieldCurveBundle curves = new YieldCurveBundle();
    curves.setCurve(DSC_CURVE_NAME, dscYC);
    curves.setCurve(FWD_CURVE_NAME, fwdYC);
    SABRInterestRateParameters sabrParameters = TestsDataSets.createSABR2();
    SABRInterestRateDataBundle sabrBundle = new SABRInterestRateDataBundle(sabrParameters, curves);

    double pv = PVC.visit(CAP, sabrBundle);

    InterestRateCurveSensitivity pvcs = new InterestRateCurveSensitivity(PVCSC.visit(CAP, sabrBundle));
    pvcs = pvcs.clean();
    DoubleMatrix1D pvcns = PVNSC.curveToNodeSensitivities(pvcs.getSensitivities(), curves);

    double[] fwd = new double[CAP.getNumberOfPayments()];
    for (int loopcpn = 0; loopcpn < CAP.getNumberOfPayments(); loopcpn++) {
      fwd[loopcpn] = PRC.visit(((CapFloorCMS) CAP.getNthPayment(loopcpn)).getUnderlyingSwap(), curves);
    }

    double test = 0.0;
    test++;
  }

  @Test
  /**
   * Computes the pv for a set of close curves with the 10Y and 15Y rates changed.
   */
  public void surface() {
    int nbShift = 10;
    double shift = 0.0002; // 1bp

    // Curves
    Interpolator1D interpolator = CombinedInterpolatorExtrapolatorFactory.getInterpolator(Interpolator1DFactory.DOUBLE_QUADRATIC, Interpolator1DFactory.FLAT_EXTRAPOLATOR);
    InterpolatedDoublesCurve dscCurve = new InterpolatedDoublesCurve(NODE_TIME, DSC_RATE_INIT, interpolator, true);
    InterpolatedDoublesCurve fwdCurve = new InterpolatedDoublesCurve(NODE_TIME, FWD_RATE_INIT, interpolator, true);
    final YieldAndDiscountCurve dscYC = new YieldCurve(dscCurve);
    final YieldAndDiscountCurve fwdYC = new YieldCurve(fwdCurve);
    final YieldCurveBundle curves = new YieldCurveBundle();
    curves.setCurve(DSC_CURVE_NAME, dscYC);
    curves.setCurve(FWD_CURVE_NAME, fwdYC);
    SABRInterestRateParameters sabrParameters = TestsDataSets.createSABR2();

    double[] fwdRateShift = FWD_RATE_INIT.clone();
    int index1 = 9; // 10Y
    int index2 = 10; // 15Y
    double[][] pvCap = new double[nbShift][nbShift];
    double[][] pvFloor = new double[nbShift][nbShift];

    for (int loopshift1 = 0; loopshift1 < nbShift; loopshift1++) {
      fwdRateShift[index1] = FWD_RATE_INIT[index1] + shift * loopshift1;
      for (int loopshift2 = 0; loopshift2 < nbShift; loopshift2++) {
        fwdRateShift[index2] = FWD_RATE_INIT[index2] + shift * loopshift2;
        YieldAndDiscountCurve fwdYCShift = new YieldCurve(new InterpolatedDoublesCurve(NODE_TIME, fwdRateShift, interpolator, true));
        curves.replaceCurve(FWD_CURVE_NAME, fwdYCShift);
        SABRInterestRateDataBundle sabrBundle = new SABRInterestRateDataBundle(sabrParameters, curves);
        pvCap[loopshift1][loopshift2] = PVC.visit(CAP, sabrBundle);
        pvFloor[loopshift1][loopshift2] = PVC.visit(FLOOR, sabrBundle);
      }
    }

    double test = 0.0;
    test++;
  }

}
