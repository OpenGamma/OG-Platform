/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate.payments.method;

import static org.testng.AssertJUnit.assertEquals;

import org.testng.annotations.Test;
import org.threeten.bp.Period;
import org.threeten.bp.ZonedDateTime;

import com.opengamma.analytics.financial.instrument.annuity.AnnuityCapFloorIborDefinition;
import com.opengamma.analytics.financial.instrument.index.IborIndex;
import com.opengamma.analytics.financial.interestrate.TestsDataSetsSABR;
import com.opengamma.analytics.financial.interestrate.YieldCurveBundle;
import com.opengamma.analytics.financial.interestrate.annuity.derivative.Annuity;
import com.opengamma.analytics.financial.interestrate.method.SuccessiveRootFinderCalibrationEngine;
import com.opengamma.analytics.financial.interestrate.payments.derivative.Payment;
import com.opengamma.analytics.financial.model.interestrate.definition.HullWhiteOneFactorPiecewiseConstantParameters;
import com.opengamma.analytics.financial.model.option.definition.SABRInterestRateDataBundle;
import com.opengamma.analytics.financial.model.option.definition.SABRInterestRateParameters;
import com.opengamma.financial.convention.businessday.BusinessDayConvention;
import com.opengamma.financial.convention.businessday.BusinessDayConventions;
import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.financial.convention.calendar.MondayToFridayCalendar;
import com.opengamma.financial.convention.daycount.DayCount;
import com.opengamma.financial.convention.daycount.DayCounts;
import com.opengamma.util.money.Currency;
import com.opengamma.util.money.CurrencyAmount;
import com.opengamma.util.test.TestGroup;
import com.opengamma.util.time.DateUtils;

/**
 * Tests related to the calibration engine for Hull-White one factor calibration to cap/floor.
 * @deprecated This class tests deprecated functionality.
 */
@Deprecated
@Test(groups = TestGroup.UNIT)
public class CapFloorHullWhiteCalibrationObjectiveTest {
  // Cap/floor description
  private static final Period TENOR_IBOR = Period.ofMonths(3);
  private static final int SETTLEMENT_DAYS = 2;
  private static final Calendar CALENDAR = new MondayToFridayCalendar("A");
  private static final DayCount DAY_COUNT_INDEX = DayCounts.ACT_360;
  private static final BusinessDayConvention BUSINESS_DAY = BusinessDayConventions.MODIFIED_FOLLOWING;
  private static final boolean IS_EOM = true;
  private static final Currency CUR = Currency.EUR;
  private static final IborIndex INDEX = new IborIndex(CUR, TENOR_IBOR, SETTLEMENT_DAYS, DAY_COUNT_INDEX, BUSINESS_DAY, IS_EOM);
  private static final ZonedDateTime SETTLEMENT_DATE = DateUtils.getUTCDate(2011, 9, 9);
  private static final ZonedDateTime MATURITY_DATE = SETTLEMENT_DATE.plusYears(5);
  private static final double NOTIONAL = 100000000; //100m
  private static final double STRIKE = 0.04;
  private static final boolean IS_CAP = true;
  private static final boolean IS_PAYER = false;
  private static final AnnuityCapFloorIborDefinition CAP_DEFINITION = AnnuityCapFloorIborDefinition.fromWithNoInitialCaplet(SETTLEMENT_DATE, MATURITY_DATE, NOTIONAL, INDEX, IS_PAYER, STRIKE, IS_CAP, CALENDAR);
  // To derivative
  private static final ZonedDateTime REFERENCE_DATE = DateUtils.getUTCDate(2011, 9, 7);
  private static final YieldCurveBundle CURVES = TestsDataSetsSABR.createCurves1();
  private static final SABRInterestRateParameters SABR_PARAMETER = TestsDataSetsSABR.createSABR1();
  private static final SABRInterestRateDataBundle SABR_BUNDLE = new SABRInterestRateDataBundle(SABR_PARAMETER, CURVES);
  private static final String[] CURVES_NAME = CURVES.getAllNames().toArray(new String[CURVES.size()]);
  private static final Annuity<? extends Payment> CAP = CAP_DEFINITION.toDerivative(REFERENCE_DATE, CURVES_NAME);

  private static final CapFloorIborSABRMethod METHOD_CAP_SABR = CapFloorIborSABRMethod.getInstance();
  private static final CapFloorIborHullWhiteMethod METHOD_CAP_HW = new CapFloorIborHullWhiteMethod();

  @Test
  /**
   * Tests the correctness of Hull-White one factor calibration to swaptions with SABR price.
   */
  public void calibration() {
    final double meanReversion = 0.01;
    final HullWhiteOneFactorPiecewiseConstantParameters hwParameters = new HullWhiteOneFactorPiecewiseConstantParameters(meanReversion, new double[] {0.01}, new double[0]);
    final CapFloorHullWhiteCalibrationObjective objective = new CapFloorHullWhiteCalibrationObjective(hwParameters);
    final SuccessiveRootFinderCalibrationEngine calibrationEngine = new CapFloorHullWhiteSuccessiveRootFinderCalibrationEngine(objective);
    for (int loopexp = 0; loopexp < CAP.getNumberOfPayments(); loopexp++) {
      calibrationEngine.addInstrument(CAP.getNthPayment(loopexp), METHOD_CAP_SABR);
    }
    calibrationEngine.calibrate(SABR_BUNDLE);
    final CurrencyAmount[] pvSabr = new CurrencyAmount[CAP.getNumberOfPayments()];
    final CurrencyAmount[] pvHw = new CurrencyAmount[CAP.getNumberOfPayments()];
    for (int loopexp = 0; loopexp < CAP.getNumberOfPayments(); loopexp++) {
      pvSabr[loopexp] = METHOD_CAP_SABR.presentValue(CAP.getNthPayment(loopexp), SABR_BUNDLE);
      pvHw[loopexp] = METHOD_CAP_HW.presentValue(CAP.getNthPayment(loopexp), objective.getHwBundle());
      assertEquals("Hull-White calibration: cap/floor " + loopexp, pvSabr[loopexp].getAmount(), pvHw[loopexp].getAmount(), 1E-2);
    }
  }

}
