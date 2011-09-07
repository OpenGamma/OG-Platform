/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.interestrate.payments.method;

import static org.testng.AssertJUnit.assertEquals;

import javax.time.calendar.Period;
import javax.time.calendar.ZonedDateTime;

import org.testng.annotations.Test;

import cern.jet.random.engine.MersenneTwister;

import com.opengamma.financial.convention.businessday.BusinessDayConvention;
import com.opengamma.financial.convention.businessday.BusinessDayConventionFactory;
import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.financial.convention.calendar.MondayToFridayCalendar;
import com.opengamma.financial.convention.daycount.DayCount;
import com.opengamma.financial.convention.daycount.DayCountFactory;
import com.opengamma.financial.instrument.annuity.AnnuityCouponFixedDefinition;
import com.opengamma.financial.instrument.annuity.AnnuityCouponIborRatchetDefinition;
import com.opengamma.financial.instrument.index.IborIndex;
import com.opengamma.financial.interestrate.PresentValueCalculator;
import com.opengamma.financial.interestrate.TestsDataSets;
import com.opengamma.financial.interestrate.YieldCurveBundle;
import com.opengamma.financial.interestrate.annuity.definition.AnnuityCouponFixed;
import com.opengamma.financial.interestrate.annuity.definition.AnnuityCouponIborRatchet;
import com.opengamma.financial.interestrate.swaption.method.montecarlo.HullWhiteMonteCarloMethod;
import com.opengamma.financial.model.interestrate.HullWhiteTestsDataSet;
import com.opengamma.financial.model.interestrate.curve.YieldAndDiscountCurve;
import com.opengamma.financial.model.interestrate.definition.HullWhiteOneFactorPiecewiseConstantDataBundle;
import com.opengamma.financial.model.interestrate.definition.HullWhiteOneFactorPiecewiseConstantParameters;
import com.opengamma.math.random.NormalRandomNumberGenerator;
import com.opengamma.util.money.Currency;
import com.opengamma.util.money.CurrencyAmount;
import com.opengamma.util.time.DateUtils;
import com.opengamma.util.timeseries.DoubleTimeSeries;
import com.opengamma.util.timeseries.zoneddatetime.ArrayZonedDateTimeDoubleTimeSeries;

/**
 * Tests the Hull-White one factor method for Annuity on Ibor Ratchet.
 */
public class AnnuityCouponIborRatchetHullWhiteMethodTest {

  private static final Calendar CALENDAR = new MondayToFridayCalendar("A");
  private static final Currency CUR = Currency.EUR;
  //Euribor 3m
  private static final int INDEX_TENOR_MONTH = 3;
  private static final Period INDEX_TENOR = Period.ofMonths(INDEX_TENOR_MONTH);
  private static final int SETTLEMENT_DAYS = 2;
  private static final DayCount DAY_COUNT = DayCountFactory.INSTANCE.getDayCount("Actual/360");
  private static final BusinessDayConvention BUSINESS_DAY = BusinessDayConventionFactory.INSTANCE.getBusinessDayConvention("Modified Following");
  private static final boolean IS_EOM = true;
  private static final IborIndex IBOR_INDEX = new IborIndex(CUR, INDEX_TENOR, SETTLEMENT_DAYS, CALENDAR, DAY_COUNT, BUSINESS_DAY, IS_EOM);
  //Annuity description
  private static final ZonedDateTime SETTLEMENT_DATE = DateUtils.getUTCDate(2011, 9, 7);
  private static final int ANNUITY_TENOR_YEAR = 2;
  private static final Period ANNUITY_TENOR = Period.ofYears(ANNUITY_TENOR_YEAR);
  private static final boolean IS_PAYER = false;
  private static final double NOTIONAL = 100000000; // 100m
  private static final double[] MAIN_COEF = new double[] {0.4, 0.5, 0.0010};
  private static final double[] FLOOR_COEF = new double[] {0.75, 0.00, 0.00};
  private static final double[] CAP_COEF = new double[] {1.50, 1.00, 0.0050};
  private static final double FIRST_CPN_RATE = 0.02;
  private static final YieldCurveBundle CURVES = TestsDataSets.createCurves1();
  private static final String[] CURVES_NAMES = CURVES.getAllNames().toArray(new String[0]);
  private static final ZonedDateTime REFERENCE_DATE = DateUtils.getUTCDate(2011, 9, 5);

  private static final AnnuityCouponIborRatchetDefinition ANNUITY_RATCHET_DEFINITION = AnnuityCouponIborRatchetDefinition.withFirstCouponFixed(SETTLEMENT_DATE, ANNUITY_TENOR, NOTIONAL, IBOR_INDEX,
      IS_PAYER, FIRST_CPN_RATE, MAIN_COEF, FLOOR_COEF, CAP_COEF);
  private static final DoubleTimeSeries<ZonedDateTime> FIXING_TS = new ArrayZonedDateTimeDoubleTimeSeries(new ZonedDateTime[] {REFERENCE_DATE}, new double[] {FIRST_CPN_RATE});
  private static final AnnuityCouponIborRatchet ANNUITY_RATCHET = ANNUITY_RATCHET_DEFINITION.toDerivative(REFERENCE_DATE, FIXING_TS, CURVES_NAMES);

  private static final int NB_PATH = 12500;
  //  private static final HullWhiteMonteCarloMethod METHOD_HW_MONTECARLO = new HullWhiteMonteCarloMethod(new NormalRandomNumberGenerator(0.0, 1.0), NB_PATH);
  private static final HullWhiteOneFactorPiecewiseConstantParameters PARAMETERS_HW = HullWhiteTestsDataSet.createHullWhiteParameters();
  private static final HullWhiteOneFactorPiecewiseConstantDataBundle BUNDLE_HW = new HullWhiteOneFactorPiecewiseConstantDataBundle(PARAMETERS_HW, CURVES);
  private static final PresentValueCalculator PVC = PresentValueCalculator.getInstance();

  //  private static final ParRateCalculator PRC = ParRateCalculator.getInstance();

  @Test
  public void presentValue() {
    YieldAndDiscountCurve curve = CURVES.getCurve(CURVES_NAMES[0]);
    HullWhiteMonteCarloMethod methodMC;
    methodMC = new HullWhiteMonteCarloMethod(new NormalRandomNumberGenerator(0.0, 1.0, new MersenneTwister()), NB_PATH);
    // Seed fixed to the DEFAULT_SEED for testing purposes.
    CurrencyAmount pvMC = methodMC.presentValue(ANNUITY_RATCHET, CUR, curve, BUNDLE_HW);
    double pvMCPreviousRun = 6070747.817;
    assertEquals("Annuity Ratchet Ibor - Hull-White - Monte Carlo", pvMCPreviousRun, pvMC.getAmount(), 1.0E-2);
  }

  @Test
  /**
   * Test the Ratchet present value in the degenerate case where the coupon are fixed (floor=cap).
   */
  public void presentValueFixedLeg() {
    YieldAndDiscountCurve curve = CURVES.getCurve(CURVES_NAMES[0]);
    HullWhiteMonteCarloMethod methodMC;
    methodMC = new HullWhiteMonteCarloMethod(new NormalRandomNumberGenerator(0.0, 1.0, new MersenneTwister()), NB_PATH);
    double[] mainFixed = new double[] {0.0, 0.0, 0.0};
    double[] floorFixed = new double[] {0.0, 0.0, FIRST_CPN_RATE};
    double[] capFixed = new double[] {0.0, 0.0, FIRST_CPN_RATE};
    AnnuityCouponIborRatchetDefinition ratchetFixedDefinition = AnnuityCouponIborRatchetDefinition.withFirstCouponFixed(SETTLEMENT_DATE, ANNUITY_TENOR, NOTIONAL, IBOR_INDEX, IS_PAYER, FIRST_CPN_RATE,
        mainFixed, floorFixed, capFixed);
    AnnuityCouponIborRatchet ratchetFixed = ratchetFixedDefinition.toDerivative(REFERENCE_DATE, FIXING_TS, CURVES_NAMES);
    CurrencyAmount pvFixedMC = methodMC.presentValue(ratchetFixed, CUR, curve, BUNDLE_HW);

    AnnuityCouponFixedDefinition fixedDefinition = AnnuityCouponFixedDefinition.from(CUR, SETTLEMENT_DATE, ANNUITY_TENOR, INDEX_TENOR, CALENDAR, DAY_COUNT, BUSINESS_DAY, IS_EOM, NOTIONAL,
        FIRST_CPN_RATE, IS_PAYER);
    AnnuityCouponFixed fixed = fixedDefinition.toDerivative(REFERENCE_DATE, CURVES_NAMES);
    double pvFixedExpected = PVC.visit(fixed, CURVES);
    assertEquals("Annuity Ratchet Ibor - Hull-White - Monte Carlo", pvFixedExpected, pvFixedMC.getAmount(), 1.0E+2);
  }

  // TODO: run the test when the CouponIbor.toDerivative is improved
  //  @Test
  //  /**
  //   * Test the Ratchet present value in the degenerate case where the coupon are fixed (floor=cap).
  //   */
  //  public void presentValueIborLeg() {
  //    AnnuityCouponIborDefinition iborDefinition = AnnuityCouponIborDefinition.from(SETTLEMENT_DATE, ANNUITY_TENOR, NOTIONAL, IBOR_INDEX, IS_PAYER);
  //    GenericAnnuity<? extends Coupon> ibor = iborDefinition.toDerivative(REFERENCE_DATE, FIXING_TS, CURVES_NAMES);
  //    double forward = PRC.visit(ibor.getNthPayment(0), CURVES);
  //    DoubleTimeSeries<ZonedDateTime> fixingForward = new ArrayZonedDateTimeDoubleTimeSeries(new ZonedDateTime[] {REFERENCE_DATE}, new double[] {forward}); // To match the forward
  //    YieldAndDiscountCurve curve = CURVES.getCurve(CURVES_NAMES[0]);
  //    int nbPath = 25000;
  //    HullWhiteMonteCarloMethod methodMC;
  //    methodMC = new HullWhiteMonteCarloMethod(new NormalRandomNumberGenerator(0.0, 1.0, new MersenneTwister()), nbPath);
  //    double[] mainIbor = new double[] {0.0, 1.0, 0.0};
  //    double[] floorIbor = new double[] {0.0, 0.0, -10.0};
  //    double[] capIbor = new double[] {0.0, 0.0, +50.0};
  //    AnnuityCouponIborRatchetDefinition ratchetFixedDefinition = AnnuityCouponIborRatchetDefinition.withFirstCouponFixed(SETTLEMENT_DATE, ANNUITY_TENOR, NOTIONAL, IBOR_INDEX, IS_PAYER, forward,
  //        mainIbor, floorIbor, capIbor);
  //    AnnuityCouponIborRatchet ratchetFixed = ratchetFixedDefinition.toDerivative(REFERENCE_DATE, fixingForward, CURVES_NAMES);
  //    CurrencyAmount pvIborMC = methodMC.presentValue(ratchetFixed, CUR, curve, BUNDLE_HW);
  //    double pvIborExpected = PVC.visit(ibor, CURVES);
  //    assertEquals("Annuity Ratchet Ibor - Hull-White - Monte Carlo", pvIborExpected, pvIborMC.getAmount(), 1.0E+2);
  //  }

}
