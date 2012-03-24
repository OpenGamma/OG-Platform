/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.interestrate.annuity.method;

import static org.testng.AssertJUnit.assertEquals;

import javax.time.calendar.Period;
import javax.time.calendar.ZonedDateTime;

import org.testng.annotations.Test;

import cern.jet.random.engine.MersenneTwister;

import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.financial.convention.calendar.MondayToFridayCalendar;
import com.opengamma.financial.instrument.annuity.AnnuityCapFloorIborDefinition;
import com.opengamma.financial.instrument.annuity.AnnuityCouponFixedDefinition;
import com.opengamma.financial.instrument.annuity.AnnuityCouponIborDefinition;
import com.opengamma.financial.instrument.annuity.AnnuityCouponIborRatchetDefinition;
import com.opengamma.financial.instrument.index.IborIndex;
import com.opengamma.financial.instrument.index.iborindex.IndexIborTestsMaster;
import com.opengamma.financial.interestrate.PresentValueCalculator;
import com.opengamma.financial.interestrate.TestsDataSetsSABR;
import com.opengamma.financial.interestrate.YieldCurveBundle;
import com.opengamma.financial.interestrate.annuity.definition.AnnuityCouponFixed;
import com.opengamma.financial.interestrate.annuity.definition.AnnuityCouponIborRatchet;
import com.opengamma.financial.interestrate.annuity.definition.GenericAnnuity;
import com.opengamma.financial.interestrate.payments.Coupon;
import com.opengamma.financial.interestrate.payments.Payment;
import com.opengamma.financial.interestrate.payments.method.CapFloorIborLMMDDMethod;
import com.opengamma.financial.model.interestrate.LiborMarketModelDisplacedDiffusionTestsDataSet;
import com.opengamma.financial.model.interestrate.definition.LiborMarketModelDisplacedDiffusionDataBundle;
import com.opengamma.financial.model.interestrate.definition.LiborMarketModelDisplacedDiffusionParameters;
import com.opengamma.financial.montecarlo.LiborMarketModelMonteCarloMethod;
import com.opengamma.math.random.NormalRandomNumberGenerator;
import com.opengamma.util.money.Currency;
import com.opengamma.util.money.CurrencyAmount;
import com.opengamma.util.time.DateUtils;
import com.opengamma.util.timeseries.DoubleTimeSeries;
import com.opengamma.util.timeseries.zoneddatetime.ArrayZonedDateTimeDoubleTimeSeries;

/**
 * Tests the Libor Market Model method for Annuity on Ibor Ratchet.
 */
public class AnnuityCouponIborRatchetLMMMethodTest {

  private static final Calendar TARGET = new MondayToFridayCalendar("TARGET");
  private static final IborIndex INDEX_EURIBOR3M = IndexIborTestsMaster.getInstance().getIndex("EURIBOR3M", TARGET);
  private static final Currency EUR = INDEX_EURIBOR3M.getCurrency();
  // Annuity description
  private static final ZonedDateTime SETTLEMENT_DATE = DateUtils.getUTCDate(2011, 9, 7);
  private static final int ANNUITY_TENOR_YEAR = 2;
  private static final Period ANNUITY_TENOR = Period.ofYears(ANNUITY_TENOR_YEAR);
  private static final boolean IS_PAYER = false;
  private static final double NOTIONAL = 100000000; // 100m
  private static final double[] MAIN_COEF = new double[] {0.20, 0.80, 0.0010};
  private static final double[] FLOOR_COEF = new double[] {0.50, 0.00, 0.0200};
  private static final double[] CAP_COEF = new double[] {1.00, 0.00, 0.0300};
  private static final double FIRST_CPN_RATE = 0.04;
  private static final AnnuityCouponIborRatchetDefinition ANNUITY_RATCHET_FIXED_DEFINITION = AnnuityCouponIborRatchetDefinition.withFirstCouponFixed(SETTLEMENT_DATE, ANNUITY_TENOR, NOTIONAL,
      INDEX_EURIBOR3M, IS_PAYER, FIRST_CPN_RATE, MAIN_COEF, FLOOR_COEF, CAP_COEF);
  private static final AnnuityCouponIborRatchetDefinition ANNUITY_RATCHET_IBOR_DEFINITION = AnnuityCouponIborRatchetDefinition.withFirstCouponIborGearing(SETTLEMENT_DATE, ANNUITY_TENOR, NOTIONAL,
      INDEX_EURIBOR3M, IS_PAYER, MAIN_COEF, FLOOR_COEF, CAP_COEF);
  // Curves and derivatives
  private static final YieldCurveBundle CURVES = TestsDataSetsSABR.createCurves1();
  private static final String[] CURVES_NAMES = TestsDataSetsSABR.curves1Names();
  private static final ZonedDateTime REFERENCE_DATE = DateUtils.getUTCDate(2011, 9, 5);
  private static final DoubleTimeSeries<ZonedDateTime> FIXING_TS = new ArrayZonedDateTimeDoubleTimeSeries(new ZonedDateTime[] {REFERENCE_DATE}, new double[] {FIRST_CPN_RATE});
  private static final AnnuityCouponIborRatchet ANNUITY_RATCHET_FIXED = ANNUITY_RATCHET_FIXED_DEFINITION.toDerivative(REFERENCE_DATE, FIXING_TS, CURVES_NAMES);
  // Methods and calculators
  private static final int NB_PATH = 12500;
  private static final LiborMarketModelDisplacedDiffusionParameters PARAMETERS_LMM = LiborMarketModelDisplacedDiffusionTestsDataSet.createLMMParameters(REFERENCE_DATE,
      ANNUITY_RATCHET_FIXED_DEFINITION);
  private static final LiborMarketModelDisplacedDiffusionDataBundle BUNDLE_LMM = new LiborMarketModelDisplacedDiffusionDataBundle(PARAMETERS_LMM, CURVES);
  private static final PresentValueCalculator PVC = PresentValueCalculator.getInstance();

  @Test
  /**
   * Test the Ratchet present value in the case where the first coupon is fixed. Tested against a previous run number.
   */
  public void presentValueFixed() {
    LiborMarketModelMonteCarloMethod methodMC;
    methodMC = new LiborMarketModelMonteCarloMethod(new NormalRandomNumberGenerator(0.0, 1.0, new MersenneTwister()), NB_PATH);
    // Seed fixed to the DEFAULT_SEED for testing purposes.
    CurrencyAmount pvMC = methodMC.presentValue(ANNUITY_RATCHET_FIXED, EUR, CURVES.getCurve(CURVES_NAMES[0]), BUNDLE_LMM);
    double pvMCPreviousRun = 8525698.689;
    assertEquals("Annuity Ratchet Ibor - LMM - Monte Carlo", pvMCPreviousRun, pvMC.getAmount(), 1.0E-2);
  }

  @Test
  public void presentValueIbor() {
    ZonedDateTime referenceDate = DateUtils.getUTCDate(2011, 8, 18);
    AnnuityCouponIborRatchet annuityRatchetIbor = ANNUITY_RATCHET_IBOR_DEFINITION.toDerivative(referenceDate, FIXING_TS, CURVES_NAMES);
    LiborMarketModelDisplacedDiffusionParameters parameterLMM = LiborMarketModelDisplacedDiffusionTestsDataSet.createLMMParameters(referenceDate, ANNUITY_RATCHET_FIXED_DEFINITION);
    LiborMarketModelDisplacedDiffusionDataBundle bundleLMM = new LiborMarketModelDisplacedDiffusionDataBundle(parameterLMM, CURVES);
    LiborMarketModelMonteCarloMethod methodMC = new LiborMarketModelMonteCarloMethod(new NormalRandomNumberGenerator(0.0, 1.0, new MersenneTwister()), NB_PATH);
    // Seed fixed to the DEFAULT_SEED for testing purposes.
    CurrencyAmount pvMC = methodMC.presentValue(annuityRatchetIbor, EUR, CURVES.getCurve(CURVES_NAMES[0]), bundleLMM);
    double pvMCPreviousRun = 8259675.715;
    assertEquals("Annuity Ratchet Ibor - LMM - Monte Carlo", pvMCPreviousRun, pvMC.getAmount(), 1.0E-2);
  }

  @Test
  /**
   * Test the Ratchet present value in the degenerate case where the coupon are fixed (floor=cap).
   */
  public void presentValueFixedLeg() {
    int nbPath = 12500;
    LiborMarketModelMonteCarloMethod methodMC = new LiborMarketModelMonteCarloMethod(new NormalRandomNumberGenerator(0.0, 1.0, new MersenneTwister()), nbPath);
    double[] mainFixed = new double[] {0.0, 0.0, 0.0};
    double[] floorFixed = new double[] {0.0, 0.0, FIRST_CPN_RATE};
    double[] capFixed = new double[] {0.0, 0.0, FIRST_CPN_RATE};
    AnnuityCouponIborRatchetDefinition ratchetFixedDefinition = AnnuityCouponIborRatchetDefinition.withFirstCouponFixed(SETTLEMENT_DATE, ANNUITY_TENOR, NOTIONAL, INDEX_EURIBOR3M, IS_PAYER,
        FIRST_CPN_RATE, mainFixed, floorFixed, capFixed);
    AnnuityCouponIborRatchet ratchetFixed = ratchetFixedDefinition.toDerivative(REFERENCE_DATE, FIXING_TS, CURVES_NAMES);
    AnnuityCouponFixedDefinition fixedDefinition = AnnuityCouponFixedDefinition.from(EUR, SETTLEMENT_DATE, ANNUITY_TENOR, INDEX_EURIBOR3M.getTenor(), TARGET, INDEX_EURIBOR3M.getDayCount(),
        INDEX_EURIBOR3M.getBusinessDayConvention(), INDEX_EURIBOR3M.isEndOfMonth(), NOTIONAL, FIRST_CPN_RATE, IS_PAYER);
    AnnuityCouponFixed fixed = fixedDefinition.toDerivative(REFERENCE_DATE, CURVES_NAMES);
    double pvFixedExpected = PVC.visit(fixed, CURVES);
    CurrencyAmount pvFixedMC = methodMC.presentValue(ratchetFixed, EUR, CURVES.getCurve(CURVES_NAMES[0]), BUNDLE_LMM);
    assertEquals("Annuity Ratchet Ibor - LMM - Monte Carlo - Degenerate in Fixed leg", pvFixedExpected, pvFixedMC.getAmount(), 1.0E+2);
    // For 500,000 path the difference is 1.65
  }

  @Test(enabled = true)
  /**
   * Test the Ratchet present value in the degenerate case where the coupon are ibor (no cap/floor, ibor factor=1.0).
   */
  public void presentValueIborLeg() {
    int nbPath = 12500;
    double[] mainIbor = new double[] {0.0, 1.0, 0.0};
    double[] floorIbor = new double[] {0.0, 0.0, -10.0};
    double[] capIbor = new double[] {0.0, 0.0, +50.0};
    AnnuityCouponIborRatchetDefinition ratchetFixedDefinition = AnnuityCouponIborRatchetDefinition.withFirstCouponFixed(SETTLEMENT_DATE, ANNUITY_TENOR, NOTIONAL, INDEX_EURIBOR3M, IS_PAYER,
        FIRST_CPN_RATE, mainIbor, floorIbor, capIbor);
    AnnuityCouponIborRatchet ratchetFixed = ratchetFixedDefinition.toDerivative(REFERENCE_DATE, FIXING_TS, CURVES_NAMES);
    AnnuityCouponIborDefinition iborDefinition = AnnuityCouponIborDefinition.from(SETTLEMENT_DATE, ANNUITY_TENOR, NOTIONAL, INDEX_EURIBOR3M, IS_PAYER);
    GenericAnnuity<? extends Coupon> ibor = iborDefinition.toDerivative(REFERENCE_DATE, FIXING_TS, CURVES_NAMES);
    Coupon[] iborFirstFixed = new Coupon[ibor.getNumberOfPayments()];
    iborFirstFixed[0] = ratchetFixed.getNthPayment(0);
    for (int loopcpn = 1; loopcpn < ibor.getNumberOfPayments(); loopcpn++) {
      iborFirstFixed[loopcpn] = ibor.getNthPayment(loopcpn);
    }
    LiborMarketModelDisplacedDiffusionParameters parameterLMM = LiborMarketModelDisplacedDiffusionTestsDataSet.createLMMParameters(REFERENCE_DATE, ratchetFixedDefinition);
    LiborMarketModelDisplacedDiffusionDataBundle bundleLMM = new LiborMarketModelDisplacedDiffusionDataBundle(parameterLMM, CURVES);
    LiborMarketModelMonteCarloMethod methodMC = new LiborMarketModelMonteCarloMethod(new NormalRandomNumberGenerator(0.0, 1.0, new MersenneTwister()), nbPath);
    CurrencyAmount pvIborMC = methodMC.presentValue(ratchetFixed, EUR, CURVES.getCurve(CURVES_NAMES[0]), bundleLMM);
    double pvIborExpected = PVC.visit(new GenericAnnuity<Payment>(iborFirstFixed), CURVES);
    assertEquals("Annuity Ratchet Ibor - Hull-White - Monte Carlo - Degenerate in Ibor leg", pvIborExpected, pvIborMC.getAmount(), 1.0E+4);
    // For 500,000 path the difference is 755.92
  }

  @Test(enabled = true)
  /**
   * Test the Ratchet present value in the degenerate case where the coupon are 0.65*Ibor floored.
   */
  public void presentValueFloorFixed() {
    int nbPath = 12500;
    double strike = 0.04;
    double factor = 0.65;
    double[] mainIbor = new double[] {0.0, factor, 0.0};
    double[] floorIbor = new double[] {0.0, 0.0, factor * strike};
    double[] capIbor = new double[] {0.0, 0.0, +50.0};
    AnnuityCouponIborRatchetDefinition ratchetFixedDefinition = AnnuityCouponIborRatchetDefinition.withFirstCouponFixed(SETTLEMENT_DATE, ANNUITY_TENOR, NOTIONAL, INDEX_EURIBOR3M, IS_PAYER,
        FIRST_CPN_RATE, mainIbor, floorIbor, capIbor);
    AnnuityCouponIborRatchet ratchetFixed = ratchetFixedDefinition.toDerivative(REFERENCE_DATE, FIXING_TS, CURVES_NAMES);
    AnnuityCapFloorIborDefinition capDefinition = AnnuityCapFloorIborDefinition.from(SETTLEMENT_DATE, SETTLEMENT_DATE.plus(ANNUITY_TENOR), NOTIONAL, INDEX_EURIBOR3M, IS_PAYER, strike, true);
    GenericAnnuity<? extends Payment> cap = capDefinition.toDerivative(REFERENCE_DATE, FIXING_TS, CURVES_NAMES);
    LiborMarketModelMonteCarloMethod methodMC = new LiborMarketModelMonteCarloMethod(new NormalRandomNumberGenerator(0.0, 1.0, new MersenneTwister()), nbPath);
    CapFloorIborLMMDDMethod methodCapLMM = new CapFloorIborLMMDDMethod();
    AnnuityCouponFixedDefinition fixedDefinition = AnnuityCouponFixedDefinition.from(EUR, SETTLEMENT_DATE, ANNUITY_TENOR, INDEX_EURIBOR3M.getTenor(), TARGET, INDEX_EURIBOR3M.getDayCount(),
        INDEX_EURIBOR3M.getBusinessDayConvention(), INDEX_EURIBOR3M.isEndOfMonth(), NOTIONAL, strike, IS_PAYER);
    AnnuityCouponFixed fixed = fixedDefinition.toDerivative(REFERENCE_DATE, CURVES_NAMES);
    double pvFlooredExpected = 0.0;
    pvFlooredExpected += PVC.visit(ratchetFixed.getNthPayment(0), CURVES);
    for (int loopcpn = 1; loopcpn < cap.getNumberOfPayments(); loopcpn++) {
      pvFlooredExpected += factor * methodCapLMM.presentValue(cap.getNthPayment(loopcpn), BUNDLE_LMM).getAmount();
      pvFlooredExpected += factor * PVC.visit(fixed.getNthPayment(loopcpn), CURVES);
    }
    CurrencyAmount pvFloorMC = methodMC.presentValue(ratchetFixed, EUR, CURVES.getCurve(CURVES_NAMES[0]), BUNDLE_LMM);
    assertEquals("Annuity Ratchet Ibor - Hull-White - LMM - Degenerate in floor leg", pvFlooredExpected, pvFloorMC.getAmount(), 2.5E+3);
    // For 500,000 path the difference is 561.70
  }

  @Test(enabled = false)
  /**
   * Tests of performance for the price and curve sensitivity by Monte Carlo. "enabled = false" for the standard testing.
   */
  public void performance() {
    long startTime, endTime;
    final int nbTest = 5;
    int nbPath = 12500;
    AnnuityCouponIborRatchetDefinition annuityRatchetIbor20Definition = AnnuityCouponIborRatchetDefinition.withFirstCouponIborGearing(SETTLEMENT_DATE, Period.ofYears(5), NOTIONAL, INDEX_EURIBOR3M,
        IS_PAYER, MAIN_COEF, FLOOR_COEF, CAP_COEF);
    ZonedDateTime referenceDate = DateUtils.getUTCDate(2011, 8, 18);
    LiborMarketModelDisplacedDiffusionParameters parameterLMM = LiborMarketModelDisplacedDiffusionTestsDataSet.createLMMParameters(referenceDate, annuityRatchetIbor20Definition);
    LiborMarketModelDisplacedDiffusionDataBundle bundleLMM = new LiborMarketModelDisplacedDiffusionDataBundle(parameterLMM, CURVES);
    AnnuityCouponIborRatchet annuityRatchetIbor20 = annuityRatchetIbor20Definition.toDerivative(referenceDate, FIXING_TS, CURVES_NAMES);
    LiborMarketModelMonteCarloMethod methodMC = new LiborMarketModelMonteCarloMethod(new NormalRandomNumberGenerator(0.0, 1.0, new MersenneTwister()), nbPath);
    CurrencyAmount[] pvMC = new CurrencyAmount[nbTest];
    //    InterestRateCurveSensitivity[] pvcsMC = new InterestRateCurveSensitivity[nbTest];

    startTime = System.currentTimeMillis();
    for (int looptest = 0; looptest < nbTest; looptest++) {
      pvMC[looptest] = methodMC.presentValue(annuityRatchetIbor20, EUR, CURVES.getCurve(CURVES_NAMES[0]), bundleLMM);
    }
    endTime = System.currentTimeMillis();
    System.out.println(nbTest + " pv Ratchet Ibor LMM MC method: " + (endTime - startTime) + " ms");
    // Performance note: HW MC price (12500 paths): 9-Sep-11: On Mac Pro 3.2 GHz Quad-Core Intel Xeon: 2700 ms for 5 Ratchet (20 coupons each).
  }

}
