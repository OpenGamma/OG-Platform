/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.interestrate.annuity.method;

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
import com.opengamma.financial.instrument.annuity.AnnuityCapFloorIborDefinition;
import com.opengamma.financial.instrument.annuity.AnnuityCouponFixedDefinition;
import com.opengamma.financial.instrument.annuity.AnnuityCouponIborDefinition;
import com.opengamma.financial.instrument.annuity.AnnuityCouponIborRatchetDefinition;
import com.opengamma.financial.instrument.index.IborIndex;
import com.opengamma.financial.interestrate.PresentValueCalculator;
import com.opengamma.financial.interestrate.TestsDataSets;
import com.opengamma.financial.interestrate.YieldCurveBundle;
import com.opengamma.financial.interestrate.annuity.definition.AnnuityCouponFixed;
import com.opengamma.financial.interestrate.annuity.definition.AnnuityCouponIborRatchet;
import com.opengamma.financial.interestrate.annuity.definition.GenericAnnuity;
import com.opengamma.financial.interestrate.payments.Coupon;
import com.opengamma.financial.interestrate.payments.Payment;
import com.opengamma.financial.interestrate.payments.method.CapFloorIborHullWhiteMethod;
import com.opengamma.financial.model.interestrate.HullWhiteTestsDataSet;
import com.opengamma.financial.model.interestrate.curve.YieldAndDiscountCurve;
import com.opengamma.financial.model.interestrate.definition.HullWhiteOneFactorPiecewiseConstantDataBundle;
import com.opengamma.financial.model.interestrate.definition.HullWhiteOneFactorPiecewiseConstantParameters;
import com.opengamma.financial.montecarlo.HullWhiteMonteCarloMethod;
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

  private static final AnnuityCouponIborRatchetDefinition ANNUITY_RATCHET_FIXED_DEFINITION = AnnuityCouponIborRatchetDefinition.withFirstCouponFixed(SETTLEMENT_DATE, ANNUITY_TENOR, NOTIONAL,
      IBOR_INDEX, IS_PAYER, FIRST_CPN_RATE, MAIN_COEF, FLOOR_COEF, CAP_COEF);
  private static final AnnuityCouponIborRatchetDefinition ANNUITY_RATCHET_IBOR_DEFINITION = AnnuityCouponIborRatchetDefinition.withFirstCouponIborGearing(SETTLEMENT_DATE, ANNUITY_TENOR, NOTIONAL,
      IBOR_INDEX, IS_PAYER, MAIN_COEF, FLOOR_COEF, CAP_COEF);
  private static final DoubleTimeSeries<ZonedDateTime> FIXING_TS = new ArrayZonedDateTimeDoubleTimeSeries(new ZonedDateTime[] {REFERENCE_DATE}, new double[] {FIRST_CPN_RATE});
  private static final AnnuityCouponIborRatchet ANNUITY_RATCHET_FIXED = ANNUITY_RATCHET_FIXED_DEFINITION.toDerivative(REFERENCE_DATE, FIXING_TS, CURVES_NAMES);

  private static final int NB_PATH = 12500;
  private static final HullWhiteOneFactorPiecewiseConstantParameters PARAMETERS_HW = HullWhiteTestsDataSet.createHullWhiteParameters();
  private static final HullWhiteOneFactorPiecewiseConstantDataBundle BUNDLE_HW = new HullWhiteOneFactorPiecewiseConstantDataBundle(PARAMETERS_HW, CURVES);
  private static final PresentValueCalculator PVC = PresentValueCalculator.getInstance();

  @Test
  public void presentValueFixed() {
    YieldAndDiscountCurve curve = CURVES.getCurve(CURVES_NAMES[0]);
    HullWhiteMonteCarloMethod methodMC;
    methodMC = new HullWhiteMonteCarloMethod(new NormalRandomNumberGenerator(0.0, 1.0, new MersenneTwister()), NB_PATH);
    // Seed fixed to the DEFAULT_SEED for testing purposes.
    CurrencyAmount pvMC = methodMC.presentValue(ANNUITY_RATCHET_FIXED, CUR, curve, BUNDLE_HW);
    double pvMCPreviousRun = 6062861.494;
    assertEquals("Annuity Ratchet Ibor - Hull-White - Monte Carlo", pvMCPreviousRun, pvMC.getAmount(), 1.0E-2);
  }

  @Test
  public void presentValueIbor() {
    ZonedDateTime referenceDate = DateUtils.getUTCDate(2011, 8, 18);
    AnnuityCouponIborRatchet annuityRatchetIbor = ANNUITY_RATCHET_IBOR_DEFINITION.toDerivative(referenceDate, FIXING_TS, CURVES_NAMES);
    YieldAndDiscountCurve curve = CURVES.getCurve(CURVES_NAMES[0]);
    HullWhiteMonteCarloMethod methodMC;
    methodMC = new HullWhiteMonteCarloMethod(new NormalRandomNumberGenerator(0.0, 1.0, new MersenneTwister()), NB_PATH);
    // Seed fixed to the DEFAULT_SEED for testing purposes.
    CurrencyAmount pvMC = methodMC.presentValue(annuityRatchetIbor, CUR, curve, BUNDLE_HW);
    double pvMCPreviousRun = 5677163.669;
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
    assertEquals("Annuity Ratchet Ibor - Hull-White - Monte Carlo - Degenerate in Fixed leg", pvFixedExpected, pvFixedMC.getAmount(), 1.0E+2);
  }

  @Test(enabled = true)
  /**
   * Test the Ratchet present value in the degenerate case where the coupon are ibor (no cap/floor, ibor factor=1.0).
   */
  public void presentValueIborLeg() {
    double[] mainIbor = new double[] {0.0, 1.0, 0.0};
    double[] floorIbor = new double[] {0.0, 0.0, -10.0};
    double[] capIbor = new double[] {0.0, 0.0, +50.0};
    AnnuityCouponIborRatchetDefinition ratchetFixedDefinition = AnnuityCouponIborRatchetDefinition.withFirstCouponFixed(SETTLEMENT_DATE, ANNUITY_TENOR, NOTIONAL, IBOR_INDEX, IS_PAYER, FIRST_CPN_RATE,
        mainIbor, floorIbor, capIbor);
    AnnuityCouponIborRatchet ratchetFixed = ratchetFixedDefinition.toDerivative(REFERENCE_DATE, FIXING_TS, CURVES_NAMES);
    AnnuityCouponIborDefinition iborDefinition = AnnuityCouponIborDefinition.from(SETTLEMENT_DATE, ANNUITY_TENOR, NOTIONAL, IBOR_INDEX, IS_PAYER);
    GenericAnnuity<? extends Coupon> ibor = iborDefinition.toDerivative(REFERENCE_DATE, FIXING_TS, CURVES_NAMES);
    Coupon[] iborFirstFixed = new Coupon[ibor.getNumberOfPayments()];
    iborFirstFixed[0] = ratchetFixed.getNthPayment(0);
    for (int loopcpn = 1; loopcpn < ibor.getNumberOfPayments(); loopcpn++) {
      iborFirstFixed[loopcpn] = ibor.getNthPayment(loopcpn);
    }
    YieldAndDiscountCurve curve = CURVES.getCurve(CURVES_NAMES[0]);
    int nbPath = 175000;
    HullWhiteMonteCarloMethod methodMC;
    methodMC = new HullWhiteMonteCarloMethod(new NormalRandomNumberGenerator(0.0, 1.0, new MersenneTwister()), nbPath);
    //    long startTime, endTime;
    //    startTime = System.currentTimeMillis();
    CurrencyAmount pvIborMC = methodMC.presentValue(ratchetFixed, CUR, curve, BUNDLE_HW);
    //    endTime = System.currentTimeMillis();
    //    System.out.println("PV Ratchet ibor - Hull-White MC method (" + nbPath + " paths): " + (endTime - startTime) + " ms");
    double pvIborExpected = PVC.visit(new GenericAnnuity<Payment>(iborFirstFixed), CURVES);
    assertEquals("Annuity Ratchet Ibor - Hull-White - Monte Carlo - Degenerate in Ibor leg", pvIborExpected, pvIborMC.getAmount(), 2.5E+3);
  }

  @Test(enabled = true)
  /**
   * Test the Ratchet present value in the degenerate case where the coupon are 0.65*Ibor floored.
   */
  public void presentValueFloorFixed() {
    double strike = 0.04;
    double factor = 0.65;
    double[] mainIbor = new double[] {0.0, factor, 0.0};
    double[] floorIbor = new double[] {0.0, 0.0, factor * strike};
    double[] capIbor = new double[] {0.0, 0.0, +50.0};
    AnnuityCouponIborRatchetDefinition ratchetFixedDefinition = AnnuityCouponIborRatchetDefinition.withFirstCouponFixed(SETTLEMENT_DATE, ANNUITY_TENOR, NOTIONAL, IBOR_INDEX, IS_PAYER, FIRST_CPN_RATE,
        mainIbor, floorIbor, capIbor);
    AnnuityCouponIborRatchet ratchetFixed = ratchetFixedDefinition.toDerivative(REFERENCE_DATE, FIXING_TS, CURVES_NAMES);
    AnnuityCapFloorIborDefinition capDefinition = AnnuityCapFloorIborDefinition.from(SETTLEMENT_DATE, SETTLEMENT_DATE.plus(ANNUITY_TENOR), NOTIONAL, IBOR_INDEX, IS_PAYER, strike, true);
    GenericAnnuity<? extends Payment> cap = capDefinition.toDerivative(REFERENCE_DATE, FIXING_TS, CURVES_NAMES);
    YieldAndDiscountCurve curve = CURVES.getCurve(CURVES_NAMES[0]);
    int nbPath = 100000;
    HullWhiteMonteCarloMethod methodMC;
    methodMC = new HullWhiteMonteCarloMethod(new NormalRandomNumberGenerator(0.0, 1.0, new MersenneTwister()), nbPath);
    //    long startTime, endTime;
    //    startTime = System.currentTimeMillis();
    CurrencyAmount pvFloorMC = methodMC.presentValue(ratchetFixed, CUR, curve, BUNDLE_HW);
    //    endTime = System.currentTimeMillis();
    //    System.out.println("PV Ratchet ibor - Hull-White MC method (" + nbPath + " paths): " + (endTime - startTime) + " ms");
    CapFloorIborHullWhiteMethod methodCapHW = new CapFloorIborHullWhiteMethod();
    AnnuityCouponFixedDefinition fixedDefinition = AnnuityCouponFixedDefinition.from(CUR, SETTLEMENT_DATE, ANNUITY_TENOR, INDEX_TENOR, CALENDAR, DAY_COUNT, BUSINESS_DAY, IS_EOM, NOTIONAL, strike,
        IS_PAYER);
    AnnuityCouponFixed fixed = fixedDefinition.toDerivative(REFERENCE_DATE, CURVES_NAMES);
    double pvFlooredExpected = 0.0;
    pvFlooredExpected += PVC.visit(ratchetFixed.getNthPayment(0), CURVES);
    for (int loopcpn = 1; loopcpn < cap.getNumberOfPayments(); loopcpn++) {
      pvFlooredExpected += factor * methodCapHW.presentValue(cap.getNthPayment(loopcpn), BUNDLE_HW).getAmount();
      pvFlooredExpected += factor * PVC.visit(fixed.getNthPayment(loopcpn), CURVES);
    }
    assertEquals("Annuity Ratchet Ibor - Hull-White - Monte Carlo - Degenerate in floor leg", pvFlooredExpected, pvFloorMC.getAmount(), 2.5E+3);
  }

  @Test(enabled = true)
  /**
   * Test the Ratchet present value in the degenerate case where the coupon are 0.65*Ibor floored.
   */
  public void presentValueFlooredIbor() {
    ZonedDateTime referenceDate = DateUtils.getUTCDate(2010, 8, 18);
    double strike = 0.04;
    double factor = 0.65;
    double[] mainIbor = new double[] {0.0, factor, 0.0};
    double[] floorIbor = new double[] {0.0, 0.0, factor * strike};
    double[] capIbor = new double[] {0.0, 0.0, 100.0};
    AnnuityCouponIborRatchetDefinition ratchetIborDefinition = AnnuityCouponIborRatchetDefinition.withFirstCouponIborGearing(SETTLEMENT_DATE, ANNUITY_TENOR, NOTIONAL, IBOR_INDEX, IS_PAYER, mainIbor,
        floorIbor, capIbor);
    DoubleTimeSeries<ZonedDateTime> fixing = new ArrayZonedDateTimeDoubleTimeSeries(new ZonedDateTime[] {referenceDate}, new double[] {FIRST_CPN_RATE});
    AnnuityCouponIborRatchet ratchetIbor = ratchetIborDefinition.toDerivative(referenceDate, fixing, CURVES_NAMES);
    AnnuityCapFloorIborDefinition capDefinition = AnnuityCapFloorIborDefinition.from(SETTLEMENT_DATE, SETTLEMENT_DATE.plus(ANNUITY_TENOR), NOTIONAL, IBOR_INDEX, IS_PAYER, strike, true);
    GenericAnnuity<? extends Payment> cap = capDefinition.toDerivative(referenceDate, fixing, CURVES_NAMES);
    YieldAndDiscountCurve curve = CURVES.getCurve(CURVES_NAMES[0]);
    int nbPath = 100000;
    HullWhiteMonteCarloMethod methodMC;
    methodMC = new HullWhiteMonteCarloMethod(new NormalRandomNumberGenerator(0.0, 1.0, new MersenneTwister()), nbPath);
    //    long startTime, endTime;
    //    startTime = System.currentTimeMillis();
    CurrencyAmount pvFlooredMC = methodMC.presentValue(ratchetIbor, CUR, curve, BUNDLE_HW);
    //    endTime = System.currentTimeMillis();
    //    System.out.println("PV Ratchet ibor - Hull-White MC method (" + nbPath + " paths): " + (endTime - startTime) + " ms");
    CapFloorIborHullWhiteMethod methodCapHW = new CapFloorIborHullWhiteMethod();
    AnnuityCouponFixedDefinition fixedDefinition = AnnuityCouponFixedDefinition.from(CUR, SETTLEMENT_DATE, ANNUITY_TENOR, INDEX_TENOR, CALENDAR, DAY_COUNT, BUSINESS_DAY, IS_EOM, NOTIONAL, strike,
        IS_PAYER);
    AnnuityCouponFixed fixed = fixedDefinition.toDerivative(referenceDate, CURVES_NAMES);
    double pvFlooredExpected = 0.0;
    pvFlooredExpected += PVC.visit(ratchetIbor.getNthPayment(0), CURVES);
    for (int loopcpn = 1; loopcpn < cap.getNumberOfPayments(); loopcpn++) {
      pvFlooredExpected += factor * methodCapHW.presentValue(cap.getNthPayment(loopcpn), BUNDLE_HW).getAmount();
      pvFlooredExpected += factor * PVC.visit(fixed.getNthPayment(loopcpn), CURVES);
    }
    assertEquals("Annuity Ratchet Ibor - Hull-White - Monte Carlo - Degenerate in floor leg", pvFlooredExpected, pvFlooredMC.getAmount(), 2.5E+3);
  }

  @Test(enabled = false)
  /**
   * Tests of performance. "enabled = false" for the standard testing.
   */
  public void performance() {
    long startTime, endTime;
    final int nbTest = 10;
    int nbPath = 12500;
    AnnuityCouponIborRatchetDefinition annuityRatchetIbor20Definition = AnnuityCouponIborRatchetDefinition.withFirstCouponIborGearing(SETTLEMENT_DATE, Period.ofYears(5), NOTIONAL, IBOR_INDEX,
        IS_PAYER, MAIN_COEF, FLOOR_COEF, CAP_COEF);
    ZonedDateTime referenceDate = DateUtils.getUTCDate(2011, 8, 18);
    AnnuityCouponIborRatchet annuityRatchetIbor10Y = annuityRatchetIbor20Definition.toDerivative(referenceDate, FIXING_TS, CURVES_NAMES);
    YieldAndDiscountCurve curve = CURVES.getCurve(CURVES_NAMES[0]);
    HullWhiteMonteCarloMethod methodMC;
    methodMC = new HullWhiteMonteCarloMethod(new NormalRandomNumberGenerator(0.0, 1.0, new MersenneTwister()), nbPath);
    CurrencyAmount[] pvMC = new CurrencyAmount[nbTest];
    startTime = System.currentTimeMillis();
    for (int looptest = 0; looptest < nbTest; looptest++) {
      pvMC[looptest] = methodMC.presentValue(annuityRatchetIbor10Y, CUR, curve, BUNDLE_HW);
    }
    endTime = System.currentTimeMillis();
    System.out.println(nbTest + " pv swaption Hull-White explicit method: " + (endTime - startTime) + " ms");
    // Performance note: HW MC price (12500 paths): 9-Sep-11: On Mac Pro 3.2 GHz Quad-Core Intel Xeon: 475 ms for 10 Ratchet (20 coupons).
  }

}
