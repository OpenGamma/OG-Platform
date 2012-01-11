/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.interestrate.annuity.method;

import static com.opengamma.financial.interestrate.TestUtils.assertSensitivityEquals;
import static org.testng.AssertJUnit.assertEquals;
import it.unimi.dsi.fastutil.doubles.DoubleAVLTreeSet;

import java.util.List;

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
import com.opengamma.financial.interestrate.FDCurveSensitivityCalculator;
import com.opengamma.financial.interestrate.InterestRateCurveSensitivity;
import com.opengamma.financial.interestrate.PresentValueCalculator;
import com.opengamma.financial.interestrate.PresentValueHullWhiteMonteCarloCalculator;
import com.opengamma.financial.interestrate.PresentValueSABRHullWhiteMonteCarloCalculator;
import com.opengamma.financial.interestrate.TestsDataSetsSABR;
import com.opengamma.financial.interestrate.YieldCurveBundle;
import com.opengamma.financial.interestrate.annuity.definition.AnnuityCouponFixed;
import com.opengamma.financial.interestrate.annuity.definition.AnnuityCouponIborRatchet;
import com.opengamma.financial.interestrate.annuity.definition.GenericAnnuity;
import com.opengamma.financial.interestrate.payments.Coupon;
import com.opengamma.financial.interestrate.payments.Payment;
import com.opengamma.financial.interestrate.payments.derivative.CouponIborRatchet;
import com.opengamma.financial.interestrate.payments.method.CapFloorIborHullWhiteMethod;
import com.opengamma.financial.model.interestrate.HullWhiteTestsDataSet;
import com.opengamma.financial.model.interestrate.definition.HullWhiteOneFactorPiecewiseConstantDataBundle;
import com.opengamma.financial.model.interestrate.definition.HullWhiteOneFactorPiecewiseConstantParameters;
import com.opengamma.financial.model.option.definition.SABRInterestRateDataBundle;
import com.opengamma.financial.model.option.definition.SABRInterestRateParameters;
import com.opengamma.financial.montecarlo.HullWhiteMonteCarloMethod;
import com.opengamma.math.random.NormalRandomNumberGenerator;
import com.opengamma.util.money.Currency;
import com.opengamma.util.money.CurrencyAmount;
import com.opengamma.util.time.DateUtils;
import com.opengamma.util.timeseries.DoubleTimeSeries;
import com.opengamma.util.timeseries.zoneddatetime.ArrayZonedDateTimeDoubleTimeSeries;
import com.opengamma.util.tuple.DoublesPair;

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
  private static final double[] MAIN_COEF = new double[] {0.20, 0.80, 0.0010};
  private static final double[] FLOOR_COEF = new double[] {0.50, 0.00, 0.0200};
  private static final double[] CAP_COEF = new double[] {1.00, 0.00, 0.0300};
  private static final double FIRST_CPN_RATE = 0.04;
  private static final YieldCurveBundle CURVES = TestsDataSetsSABR.createCurves1();
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
    /**
     * Test the Ratchet present value in the case where the first coupon is fixed. Tested against a previous run number.
     */
    HullWhiteMonteCarloMethod methodMC;
    methodMC = new HullWhiteMonteCarloMethod(new NormalRandomNumberGenerator(0.0, 1.0, new MersenneTwister()), NB_PATH);
    // Seed fixed to the DEFAULT_SEED for testing purposes.
    CurrencyAmount pvMC = methodMC.presentValue(ANNUITY_RATCHET_FIXED, CUR, CURVES_NAMES[0], BUNDLE_HW);
    double pvMCPreviousRun = 8431517.192;
    assertEquals("Annuity Ratchet Ibor - Hull-White - Monte Carlo", pvMCPreviousRun, pvMC.getAmount(), 1.0E-2);
  }

  @Test
  public void presentValueIbor() {
    ZonedDateTime referenceDate = DateUtils.getUTCDate(2011, 8, 18);
    AnnuityCouponIborRatchet annuityRatchetIbor = ANNUITY_RATCHET_IBOR_DEFINITION.toDerivative(referenceDate, FIXING_TS, CURVES_NAMES);
    HullWhiteMonteCarloMethod methodMC;
    methodMC = new HullWhiteMonteCarloMethod(new NormalRandomNumberGenerator(0.0, 1.0, new MersenneTwister()), NB_PATH);
    // Seed fixed to the DEFAULT_SEED for testing purposes.
    CurrencyAmount pvMC = methodMC.presentValue(annuityRatchetIbor, CUR, CURVES_NAMES[0], BUNDLE_HW);
    double pvMCPreviousRun = 8172059.762;
    assertEquals("Annuity Ratchet Ibor - Hull-White - Monte Carlo", pvMCPreviousRun, pvMC.getAmount(), 1.0E-2);
  }

  @Test
  /**
   * Test the Ratchet present value in the degenerate case where the coupon are fixed (floor=cap).
   */
  public void presentValueFixedLeg() {
    HullWhiteMonteCarloMethod methodMC;
    methodMC = new HullWhiteMonteCarloMethod(new NormalRandomNumberGenerator(0.0, 1.0, new MersenneTwister()), NB_PATH);
    double[] mainFixed = new double[] {0.0, 0.0, 0.0};
    double[] floorFixed = new double[] {0.0, 0.0, FIRST_CPN_RATE};
    double[] capFixed = new double[] {0.0, 0.0, FIRST_CPN_RATE};
    AnnuityCouponIborRatchetDefinition ratchetFixedDefinition = AnnuityCouponIborRatchetDefinition.withFirstCouponFixed(SETTLEMENT_DATE, ANNUITY_TENOR, NOTIONAL, IBOR_INDEX, IS_PAYER, FIRST_CPN_RATE,
        mainFixed, floorFixed, capFixed);
    AnnuityCouponIborRatchet ratchetFixed = ratchetFixedDefinition.toDerivative(REFERENCE_DATE, FIXING_TS, CURVES_NAMES);
    CurrencyAmount pvFixedMC = methodMC.presentValue(ratchetFixed, CUR, CURVES_NAMES[0], BUNDLE_HW);

    AnnuityCouponFixedDefinition fixedDefinition = AnnuityCouponFixedDefinition.from(CUR, SETTLEMENT_DATE, ANNUITY_TENOR, INDEX_TENOR, CALENDAR, DAY_COUNT, BUSINESS_DAY, IS_EOM, NOTIONAL,
        FIRST_CPN_RATE, IS_PAYER);
    AnnuityCouponFixed fixed = fixedDefinition.toDerivative(REFERENCE_DATE, CURVES_NAMES);
    double pvFixedExpected = PVC.visit(fixed, CURVES);
    assertEquals("Annuity Ratchet Ibor - Hull-White - Monte Carlo - Degenerate in Fixed leg", pvFixedExpected, pvFixedMC.getAmount(), 2.0E+2);
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
    int nbPath = 175000;
    HullWhiteMonteCarloMethod methodMC;
    methodMC = new HullWhiteMonteCarloMethod(new NormalRandomNumberGenerator(0.0, 1.0, new MersenneTwister()), nbPath);
    //    long startTime, endTime;
    //    startTime = System.currentTimeMillis();
    CurrencyAmount pvIborMC = methodMC.presentValue(ratchetFixed, CUR, CURVES_NAMES[0], BUNDLE_HW);
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
    int nbPath = 100000;
    HullWhiteMonteCarloMethod methodMC;
    methodMC = new HullWhiteMonteCarloMethod(new NormalRandomNumberGenerator(0.0, 1.0, new MersenneTwister()), nbPath);
    //    long startTime, endTime;
    //    startTime = System.currentTimeMillis();
    CurrencyAmount pvFloorMC = methodMC.presentValue(ratchetFixed, CUR, CURVES_NAMES[0], BUNDLE_HW);
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
    int nbPath = 100000;
    HullWhiteMonteCarloMethod methodMC;
    methodMC = new HullWhiteMonteCarloMethod(new NormalRandomNumberGenerator(0.0, 1.0, new MersenneTwister()), nbPath);
    //    long startTime, endTime;
    //    startTime = System.currentTimeMillis();
    CurrencyAmount pvFlooredMC = methodMC.presentValue(ratchetIbor, CUR, CURVES_NAMES[0], BUNDLE_HW);
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

  @Test(enabled = true)
  /**
   * Tests the pricing with calibration to SABR cap/floor prices.
   */
  public void presentValueFixedWithCalibration() {
    SABRInterestRateParameters sabrParameter = TestsDataSetsSABR.createSABR1();
    SABRInterestRateDataBundle sabrBundle = new SABRInterestRateDataBundle(sabrParameter, CURVES);
    PresentValueSABRHullWhiteMonteCarloCalculator calculatorMC = PresentValueSABRHullWhiteMonteCarloCalculator.getInstance();
    double pvMC = calculatorMC.visit(ANNUITY_RATCHET_FIXED, sabrBundle);
    double pvMCPreviousRun = 8400036.210; //50000 paths: 8400036.210 - 12500 paths: 8402639.933;
    assertEquals("Annuity Ratchet Ibor - Hull-White - Monte Carlo", pvMCPreviousRun, pvMC, 1.0E-2);
  }

  @Test
  /**
   * Test the Ratchet present value curve sensitivity in the case where the first coupon is fixed.
   */
  public void presentValueCurveSensitivityFixed() {
    final double deltaTolerancePrice = 1.0E+4;
    //Testing note: Sensitivity is for a movement of 1. 1E+2 = 1 cent for a 1 bp move.
    HullWhiteMonteCarloMethod methodMC;
    int nbPath = 50000; // In line with calculator.
    methodMC = new HullWhiteMonteCarloMethod(new NormalRandomNumberGenerator(0.0, 1.0, new MersenneTwister()), nbPath);
    // Seed fixed to the DEFAULT_SEED for testing purposes.
    InterestRateCurveSensitivity pvcsMC = methodMC.presentValueCurveSensitivity(ANNUITY_RATCHET_FIXED, CURVES_NAMES[0], BUNDLE_HW);
    pvcsMC = pvcsMC.clean(1.0E-10, 1.0E-2); // (1.0E-10, 1.0E-2);

    PresentValueHullWhiteMonteCarloCalculator calculator = PresentValueHullWhiteMonteCarloCalculator.getInstance();

    // Dsc curve
    DoubleAVLTreeSet dscTime = new DoubleAVLTreeSet();
    for (int loopcpn = 0; loopcpn < ANNUITY_RATCHET_FIXED.getNumberOfPayments(); loopcpn++) {
      Coupon cpn = ANNUITY_RATCHET_FIXED.getNthPayment(loopcpn);
      dscTime.add(cpn.getPaymentTime());
    }
    double[] timesDsc = dscTime.toDoubleArray();
    List<DoublesPair> pvcsFDDscMC = FDCurveSensitivityCalculator.curveSensitvityFDCalculator(ANNUITY_RATCHET_FIXED, calculator, BUNDLE_HW, CURVES_NAMES[0], timesDsc, 1.0E-2);
    final List<DoublesPair> pvcsADDscMC = pvcsMC.getSensitivities().get(CURVES_NAMES[0]);
    assertSensitivityEquals(pvcsFDDscMC, pvcsADDscMC, deltaTolerancePrice);
    // Fwd curve
    DoubleAVLTreeSet forwardTime = new DoubleAVLTreeSet();
    for (int loopcpn = 1; loopcpn < ANNUITY_RATCHET_FIXED.getNumberOfPayments(); loopcpn++) {
      CouponIborRatchet cpn = (CouponIborRatchet) ANNUITY_RATCHET_FIXED.getNthPayment(loopcpn);
      forwardTime.add(cpn.getFixingPeriodStartTime());
      forwardTime.add(cpn.getFixingPeriodEndTime());
    }
    double[] timesFwd = forwardTime.toDoubleArray();
    List<DoublesPair> pvcsFDFwdMC = FDCurveSensitivityCalculator.curveSensitvityFDCalculator(ANNUITY_RATCHET_FIXED, calculator, BUNDLE_HW, CURVES_NAMES[1], timesFwd, 1.0E-2);
    final List<DoublesPair> pvcsADFwdMC = pvcsMC.getSensitivities().get(CURVES_NAMES[1]);
    assertSensitivityEquals(pvcsFDFwdMC, pvcsADFwdMC, deltaTolerancePrice);
  }

  @Test(enabled = false)
  /**
   * Tests of performance for the price and curve sensitivity by Monte Carlo. "enabled = false" for the standard testing.
   */
  public void performance() {
    long startTime, endTime;
    final int nbTest = 10;
    int nbPath = 12500;
    AnnuityCouponIborRatchetDefinition annuityRatchetIbor20Definition = AnnuityCouponIborRatchetDefinition.withFirstCouponIborGearing(SETTLEMENT_DATE, Period.ofYears(5), NOTIONAL, IBOR_INDEX,
        IS_PAYER, MAIN_COEF, FLOOR_COEF, CAP_COEF);
    ZonedDateTime referenceDate = DateUtils.getUTCDate(2011, 8, 18);
    AnnuityCouponIborRatchet annuityRatchetIbor20 = annuityRatchetIbor20Definition.toDerivative(referenceDate, FIXING_TS, CURVES_NAMES);
    HullWhiteMonteCarloMethod methodMC;
    methodMC = new HullWhiteMonteCarloMethod(new NormalRandomNumberGenerator(0.0, 1.0, new MersenneTwister()), nbPath);
    CurrencyAmount[] pvMC = new CurrencyAmount[nbTest];
    InterestRateCurveSensitivity[] pvcsMC = new InterestRateCurveSensitivity[nbTest];

    startTime = System.currentTimeMillis();
    for (int looptest = 0; looptest < nbTest; looptest++) {
      pvMC[looptest] = methodMC.presentValue(annuityRatchetIbor20, CUR, CURVES_NAMES[0], BUNDLE_HW);
    }
    endTime = System.currentTimeMillis();
    System.out.println(nbTest + " pv Ratchet Ibor Hull-White MC method: " + (endTime - startTime) + " ms");
    // Performance note: HW MC price (12500 paths): 9-Sep-11: On Mac Pro 3.2 GHz Quad-Core Intel Xeon: 490 ms for 10 Ratchet (20 coupons each).

    startTime = System.currentTimeMillis();
    for (int looptest = 0; looptest < nbTest; looptest++) {
      pvcsMC[looptest] = methodMC.presentValueCurveSensitivity(annuityRatchetIbor20, CURVES_NAMES[0], BUNDLE_HW);
    }
    endTime = System.currentTimeMillis();
    System.out.println(nbTest + " delta Ratchet Ibor Hull-White MC method: " + (endTime - startTime) + " ms");
    // Performance note: HW MC delta (12500 paths): 24-Oct-11: On Mac Pro 3.2 GHz Quad-Core Intel Xeon: 1325 ms for 10 Ratchet (20 coupons each).

    startTime = System.currentTimeMillis();
    for (int looptest = 0; looptest < nbTest; looptest++) {
      pvMC[looptest] = methodMC.presentValue(annuityRatchetIbor20, CUR, CURVES_NAMES[0], BUNDLE_HW);
      pvcsMC[looptest] = methodMC.presentValueCurveSensitivity(annuityRatchetIbor20, CURVES_NAMES[0], BUNDLE_HW);
    }
    endTime = System.currentTimeMillis();
    System.out.println(nbTest + " pv/delta Ratchet Ibor Hull-White MC method: " + (endTime - startTime) + " ms");
    // Performance note: HW MC price (12500 paths) - pv/delta: 31-Oct-11: On Mac Pro 3.2 GHz Quad-Core Intel Xeon: 1800 ms for 10 Ratchet (20 coupons each).
  }

}
