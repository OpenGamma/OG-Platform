/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate.annuity.provider;

import static org.testng.AssertJUnit.assertEquals;

import org.testng.annotations.Test;
import org.threeten.bp.Period;
import org.threeten.bp.ZonedDateTime;

import cern.jet.random.engine.MersenneTwister;

import com.opengamma.analytics.financial.instrument.annuity.AnnuityCapFloorIborDefinition;
import com.opengamma.analytics.financial.instrument.annuity.AnnuityCouponFixedDefinition;
import com.opengamma.analytics.financial.instrument.annuity.AnnuityCouponIborDefinition;
import com.opengamma.analytics.financial.instrument.annuity.AnnuityCouponIborRatchetDefinition;
import com.opengamma.analytics.financial.instrument.index.IborIndex;
import com.opengamma.analytics.financial.interestrate.annuity.derivative.Annuity;
import com.opengamma.analytics.financial.interestrate.annuity.derivative.AnnuityCouponFixed;
import com.opengamma.analytics.financial.interestrate.annuity.derivative.AnnuityCouponIborRatchet;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CapFloorIbor;
import com.opengamma.analytics.financial.interestrate.payments.derivative.Coupon;
import com.opengamma.analytics.financial.interestrate.payments.derivative.Payment;
import com.opengamma.analytics.financial.interestrate.payments.provider.CapFloorIborHullWhiteMethod;
import com.opengamma.analytics.financial.model.interestrate.definition.HullWhiteOneFactorPiecewiseConstantParameters;
import com.opengamma.analytics.financial.montecarlo.provider.HullWhiteMonteCarloMethod;
import com.opengamma.analytics.financial.provider.calculator.discounting.PresentValueDiscountingCalculator;
import com.opengamma.analytics.financial.provider.calculator.hullwhite.PresentValueCurveSensitivityHullWhiteMonteCarloCalculator;
import com.opengamma.analytics.financial.provider.calculator.hullwhite.PresentValueHullWhiteMonteCarloCalculator;
import com.opengamma.analytics.financial.provider.description.HullWhiteDataSets;
import com.opengamma.analytics.financial.provider.description.MulticurveProviderDiscountDataSets;
import com.opengamma.analytics.financial.provider.description.interestrate.HullWhiteOneFactorProviderDiscount;
import com.opengamma.analytics.financial.provider.description.interestrate.HullWhiteOneFactorProviderInterface;
import com.opengamma.analytics.financial.provider.description.interestrate.MulticurveProviderDiscount;
import com.opengamma.analytics.financial.provider.sensitivity.hullwhite.ParameterSensitivityHullWhiteDiscountInterpolatedFDCalculator;
import com.opengamma.analytics.financial.provider.sensitivity.multicurve.MultipleCurrencyMulticurveSensitivity;
import com.opengamma.analytics.financial.provider.sensitivity.multicurve.MultipleCurrencyParameterSensitivity;
import com.opengamma.analytics.financial.provider.sensitivity.parameter.ParameterSensitivityParameterCalculator;
import com.opengamma.analytics.financial.util.AssertSensivityObjects;
import com.opengamma.analytics.math.random.NormalRandomNumberGenerator;
import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.timeseries.DoubleTimeSeries;
import com.opengamma.timeseries.precise.zdt.ImmutableZonedDateTimeDoubleTimeSeries;
import com.opengamma.util.money.Currency;
import com.opengamma.util.money.MultipleCurrencyAmount;
import com.opengamma.util.test.TestGroup;
import com.opengamma.util.time.DateUtils;

/**
 * Tests the Hull-White one factor method for Annuity on Ibor Ratchet.
 */
@Test(groups = TestGroup.UNIT)
public class AnnuityCouponIborRatchetHullWhiteMethodTest {

  private static final MulticurveProviderDiscount MULTICURVES = MulticurveProviderDiscountDataSets.createMulticurveEurUsd();
  private static final IborIndex EURIBOR3M = MulticurveProviderDiscountDataSets.getIndexesIborMulticurveEurUsd()[0];
  private static final Calendar TARGET = MulticurveProviderDiscountDataSets.getEURCalendar();
  private static final Currency CUR = EURIBOR3M.getCurrency();

  private static final ZonedDateTime REFERENCE_DATE = DateUtils.getUTCDate(2011, 9, 5);

  //Annuity description
  private static final ZonedDateTime SETTLEMENT_DATE = DateUtils.getUTCDate(2011, 9, 7);
  private static final int ANNUITY_TENOR_YEAR = 2;
  private static final Period ANNUITY_TENOR = Period.ofYears(ANNUITY_TENOR_YEAR);
  private static final boolean IS_PAYER = false;
  private static final double NOTIONAL = 100000000; // 100m
  private static final double[] MAIN_COEF = new double[] {0.20, 0.80, 0.0010};
  private static final double[] FLOOR_COEF = new double[] {0.50, 0.00, 0.0020};
  private static final double[] CAP_COEF = new double[] {1.00, 0.00, 0.0100};
  private static final double FIRST_CPN_RATE = 0.04;

  private static final AnnuityCouponIborRatchetDefinition ANNUITY_RATCHET_FIXED_DEFINITION = AnnuityCouponIborRatchetDefinition.withFirstCouponFixed(SETTLEMENT_DATE, ANNUITY_TENOR, NOTIONAL,
      EURIBOR3M, IS_PAYER, FIRST_CPN_RATE, MAIN_COEF, FLOOR_COEF, CAP_COEF, TARGET);
  private static final AnnuityCouponIborRatchetDefinition ANNUITY_RATCHET_IBOR_DEFINITION = AnnuityCouponIborRatchetDefinition.withFirstCouponIborGearing(SETTLEMENT_DATE, ANNUITY_TENOR, NOTIONAL,
      EURIBOR3M, IS_PAYER, MAIN_COEF, FLOOR_COEF, CAP_COEF, TARGET);
  private static final DoubleTimeSeries<ZonedDateTime> FIXING_TS = ImmutableZonedDateTimeDoubleTimeSeries.ofUTC(new ZonedDateTime[] {REFERENCE_DATE}, new double[] {FIRST_CPN_RATE});
  private static final AnnuityCouponIborRatchet ANNUITY_RATCHET_FIXED = ANNUITY_RATCHET_FIXED_DEFINITION.toDerivative(REFERENCE_DATE, FIXING_TS);

  private static final int NB_PATH = 12500;

  private static final HullWhiteOneFactorPiecewiseConstantParameters HW_PARAMETERS = HullWhiteDataSets.createHullWhiteParameters();
  private static final HullWhiteOneFactorProviderDiscount HW_MULTICURVES = new HullWhiteOneFactorProviderDiscount(MULTICURVES, HW_PARAMETERS, CUR);

  private static final CapFloorIborHullWhiteMethod METHOD_HW_CAP = CapFloorIborHullWhiteMethod.getInstance();

  private static final PresentValueDiscountingCalculator PVDC = PresentValueDiscountingCalculator.getInstance();

  private static final PresentValueHullWhiteMonteCarloCalculator PVHWMCC = new PresentValueHullWhiteMonteCarloCalculator(NB_PATH);
  private static final PresentValueCurveSensitivityHullWhiteMonteCarloCalculator PVCSHWMCC = new PresentValueCurveSensitivityHullWhiteMonteCarloCalculator(NB_PATH);

  private static final double SHIFT = 1.0E-6;
  private static final ParameterSensitivityParameterCalculator<HullWhiteOneFactorProviderInterface> PS_HW_C = new ParameterSensitivityParameterCalculator<>(
      PVCSHWMCC);
  private static final ParameterSensitivityHullWhiteDiscountInterpolatedFDCalculator PS_HW_FDC = new ParameterSensitivityHullWhiteDiscountInterpolatedFDCalculator(PVHWMCC, SHIFT);

  private static final double TOLERANCE_PV = 1.0E-2;
  private static final double TOLERANCE_PV_DELTA_MC = 5.0E+3; //Testing note: Sensitivity is for a movement of 1. 1E+2 = 1 cent for a 1 bp move.

  @Test
  /**
   * Test the Ratchet present value in the case where the first coupon is fixed. Tested against a previous run number.
   */
  public void presentValueFixed() {
    HullWhiteMonteCarloMethod methodMC;
    methodMC = new HullWhiteMonteCarloMethod(new NormalRandomNumberGenerator(0.0, 1.0, new MersenneTwister()), NB_PATH);
    // Seed fixed to the DEFAULT_SEED for testing purposes.
    final MultipleCurrencyAmount pvMC = methodMC.presentValue(ANNUITY_RATCHET_FIXED, CUR, HW_MULTICURVES);
    final double pvMCPreviousRun = 4658897.913;
    assertEquals("Annuity Ratchet Ibor - Hull-White - Monte Carlo", pvMCPreviousRun, pvMC.getAmount(CUR), TOLERANCE_PV);
  }

  @Test
  public void presentValueIbor() {
    final ZonedDateTime referenceDate = DateUtils.getUTCDate(2011, 8, 18);
    final AnnuityCouponIborRatchet annuityRatchetIbor = ANNUITY_RATCHET_IBOR_DEFINITION.toDerivative(referenceDate, FIXING_TS);
    HullWhiteMonteCarloMethod methodMC;
    methodMC = new HullWhiteMonteCarloMethod(new NormalRandomNumberGenerator(0.0, 1.0, new MersenneTwister()), NB_PATH);
    // Seed fixed to the DEFAULT_SEED for testing purposes.
    final MultipleCurrencyAmount pvMC = methodMC.presentValue(annuityRatchetIbor, CUR, HW_MULTICURVES);
    final double pvMCPreviousRun = 4406845.218;
    assertEquals("Annuity Ratchet Ibor - Hull-White - Monte Carlo", pvMCPreviousRun, pvMC.getAmount(CUR), TOLERANCE_PV);
  }

  @Test
  /**
   * Test the Ratchet present value in the degenerate case where the coupon are fixed (floor=cap).
   */
  public void presentValueFixedLeg() {
    HullWhiteMonteCarloMethod methodMC;
    methodMC = new HullWhiteMonteCarloMethod(new NormalRandomNumberGenerator(0.0, 1.0, new MersenneTwister()), NB_PATH);
    final double[] mainFixed = new double[] {0.0, 0.0, 0.0};
    final double[] floorFixed = new double[] {0.0, 0.0, FIRST_CPN_RATE};
    final double[] capFixed = new double[] {0.0, 0.0, FIRST_CPN_RATE};
    final AnnuityCouponIborRatchetDefinition ratchetFixedDefinition = AnnuityCouponIborRatchetDefinition.withFirstCouponFixed(SETTLEMENT_DATE, ANNUITY_TENOR, NOTIONAL, EURIBOR3M, IS_PAYER,
        FIRST_CPN_RATE, mainFixed, floorFixed, capFixed, TARGET);
    final AnnuityCouponIborRatchet ratchetFixed = ratchetFixedDefinition.toDerivative(REFERENCE_DATE, FIXING_TS);
    final MultipleCurrencyAmount pvFixedMC = methodMC.presentValue(ratchetFixed, CUR, HW_MULTICURVES);

    final AnnuityCouponFixedDefinition fixedDefinition = AnnuityCouponFixedDefinition.from(CUR, SETTLEMENT_DATE, ANNUITY_TENOR, EURIBOR3M.getTenor(), TARGET, EURIBOR3M.getDayCount(),
        EURIBOR3M.getBusinessDayConvention(), EURIBOR3M.isEndOfMonth(), NOTIONAL, FIRST_CPN_RATE, IS_PAYER);
    final AnnuityCouponFixed fixed = fixedDefinition.toDerivative(REFERENCE_DATE);
    final MultipleCurrencyAmount pvFixedExpected = fixed.accept(PVDC, MULTICURVES);
    assertEquals("Annuity Ratchet Ibor - Hull-White - Monte Carlo - Degenerate in Fixed leg", pvFixedExpected.getAmount(CUR), pvFixedMC.getAmount(CUR), 2.0E+2);
  }

  @Test(enabled = true)
  /**
   * Test the Ratchet present value in the degenerate case where the coupon are ibor (no cap/floor, ibor factor=1.0).
   */
  public void presentValueIborLeg() {
    final double[] mainIbor = new double[] {0.0, 1.0, 0.0};
    final double[] floorIbor = new double[] {0.0, 0.0, -10.0};
    final double[] capIbor = new double[] {0.0, 0.0, +50.0};
    final AnnuityCouponIborRatchetDefinition ratchetFixedDefinition = AnnuityCouponIborRatchetDefinition.withFirstCouponFixed(SETTLEMENT_DATE, ANNUITY_TENOR, NOTIONAL, EURIBOR3M, IS_PAYER,
        FIRST_CPN_RATE, mainIbor, floorIbor, capIbor, TARGET);
    final AnnuityCouponIborRatchet ratchetFixed = ratchetFixedDefinition.toDerivative(REFERENCE_DATE, FIXING_TS);
    final AnnuityCouponIborDefinition iborDefinition = AnnuityCouponIborDefinition.from(SETTLEMENT_DATE, ANNUITY_TENOR, NOTIONAL, EURIBOR3M, IS_PAYER, TARGET);
    final Annuity<? extends Coupon> ibor = iborDefinition.toDerivative(REFERENCE_DATE, FIXING_TS);
    final Coupon[] iborFirstFixed = new Coupon[ibor.getNumberOfPayments()];
    iborFirstFixed[0] = ratchetFixed.getNthPayment(0);
    for (int loopcpn = 1; loopcpn < ibor.getNumberOfPayments(); loopcpn++) {
      iborFirstFixed[loopcpn] = ibor.getNthPayment(loopcpn);
    }
    final int nbPath = 175000;
    HullWhiteMonteCarloMethod methodMC;
    methodMC = new HullWhiteMonteCarloMethod(new NormalRandomNumberGenerator(0.0, 1.0, new MersenneTwister()), nbPath);
    final MultipleCurrencyAmount pvIborMC = methodMC.presentValue(ratchetFixed, CUR, HW_MULTICURVES);
    final MultipleCurrencyAmount pvIborExpected = new Annuity<Payment>(iborFirstFixed).accept(PVDC, MULTICURVES);
    assertEquals("Annuity Ratchet Ibor - Hull-White - Monte Carlo - Degenerate in Ibor leg", pvIborExpected.getAmount(CUR), pvIborMC.getAmount(CUR), 3.0E+3);
  }

  @Test(enabled = true)
  /**
   * Test the Ratchet present value in the degenerate case where the coupon are 0.65*Ibor floored.
   */
  public void presentValueFloorFixed() {
    final double strike = 0.04;
    final double factor = 0.65;
    final double[] mainIbor = new double[] {0.0, factor, 0.0};
    final double[] floorIbor = new double[] {0.0, 0.0, factor * strike};
    final double[] capIbor = new double[] {0.0, 0.0, +50.0};
    final AnnuityCouponIborRatchetDefinition ratchetFixedDefinition = AnnuityCouponIborRatchetDefinition.withFirstCouponFixed(SETTLEMENT_DATE, ANNUITY_TENOR, NOTIONAL, EURIBOR3M, IS_PAYER,
        FIRST_CPN_RATE, mainIbor, floorIbor, capIbor, TARGET);
    final AnnuityCouponIborRatchet ratchetFixed = ratchetFixedDefinition.toDerivative(REFERENCE_DATE, FIXING_TS);
    final AnnuityCapFloorIborDefinition capDefinition = AnnuityCapFloorIborDefinition.from(SETTLEMENT_DATE, SETTLEMENT_DATE.plus(ANNUITY_TENOR), NOTIONAL, EURIBOR3M, IS_PAYER, strike, true, TARGET);
    final Annuity<? extends Payment> cap = capDefinition.toDerivative(REFERENCE_DATE, FIXING_TS);
    final int nbPath = 100000;
    HullWhiteMonteCarloMethod methodMC;
    methodMC = new HullWhiteMonteCarloMethod(new NormalRandomNumberGenerator(0.0, 1.0, new MersenneTwister()), nbPath);
    final MultipleCurrencyAmount pvFloorMC = methodMC.presentValue(ratchetFixed, CUR, HW_MULTICURVES);
    final AnnuityCouponFixedDefinition fixedDefinition = AnnuityCouponFixedDefinition.from(CUR, SETTLEMENT_DATE, ANNUITY_TENOR, EURIBOR3M.getTenor(), TARGET, EURIBOR3M.getDayCount(),
        EURIBOR3M.getBusinessDayConvention(), EURIBOR3M.isEndOfMonth(), NOTIONAL, strike, IS_PAYER);
    final AnnuityCouponFixed fixed = fixedDefinition.toDerivative(REFERENCE_DATE);
    MultipleCurrencyAmount pvFlooredExpected = MultipleCurrencyAmount.of(CUR, 0.0);
    pvFlooredExpected = pvFlooredExpected.plus(ratchetFixed.getNthPayment(0).accept(PVDC, MULTICURVES));
    for (int loopcpn = 1; loopcpn < cap.getNumberOfPayments(); loopcpn++) {
      pvFlooredExpected = pvFlooredExpected.plus(METHOD_HW_CAP.presentValue((CapFloorIbor) cap.getNthPayment(loopcpn), HW_MULTICURVES).multipliedBy(factor));
      pvFlooredExpected = pvFlooredExpected.plus(fixed.getNthPayment(loopcpn).accept(PVDC, MULTICURVES).multipliedBy(factor));
    }
    assertEquals("Annuity Ratchet Ibor - Hull-White - Monte Carlo - Degenerate in floor leg", pvFlooredExpected.getAmount(CUR), pvFloorMC.getAmount(CUR), 2.5E+3);
  }

  @Test(enabled = true)
  /**
   * Test the Ratchet present value in the degenerate case where the coupon are 0.65*Ibor floored.
   */
  public void presentValueFlooredIbor() {
    final ZonedDateTime referenceDate = DateUtils.getUTCDate(2010, 8, 18);
    final double strike = 0.04;
    final double factor = 0.65;
    final double[] mainIbor = new double[] {0.0, factor, 0.0};
    final double[] floorIbor = new double[] {0.0, 0.0, factor * strike};
    final double[] capIbor = new double[] {0.0, 0.0, 100.0};
    final AnnuityCouponIborRatchetDefinition ratchetIborDefinition = AnnuityCouponIborRatchetDefinition.withFirstCouponIborGearing(SETTLEMENT_DATE, ANNUITY_TENOR, NOTIONAL, EURIBOR3M, IS_PAYER,
        mainIbor, floorIbor, capIbor, TARGET);
    final DoubleTimeSeries<ZonedDateTime> fixing = ImmutableZonedDateTimeDoubleTimeSeries.ofUTC(new ZonedDateTime[] {referenceDate}, new double[] {FIRST_CPN_RATE});
    final AnnuityCouponIborRatchet ratchetIbor = ratchetIborDefinition.toDerivative(referenceDate, fixing);
    final AnnuityCapFloorIborDefinition capDefinition = AnnuityCapFloorIborDefinition.from(SETTLEMENT_DATE, SETTLEMENT_DATE.plus(ANNUITY_TENOR), NOTIONAL, EURIBOR3M, IS_PAYER, strike, true, TARGET);
    final Annuity<? extends Payment> cap = capDefinition.toDerivative(referenceDate, fixing);
    final int nbPath = 100000;
    HullWhiteMonteCarloMethod methodMC;
    methodMC = new HullWhiteMonteCarloMethod(new NormalRandomNumberGenerator(0.0, 1.0, new MersenneTwister()), nbPath);
    //    long startTime, endTime;
    //    startTime = System.currentTimeMillis();
    final MultipleCurrencyAmount pvFlooredMC = methodMC.presentValue(ratchetIbor, CUR, HW_MULTICURVES);
    //    endTime = System.currentTimeMillis();
    //    System.out.println("PV Ratchet ibor - Hull-White MC method (" + nbPath + " paths): " + (endTime - startTime) + " ms");
    final AnnuityCouponFixedDefinition fixedDefinition = AnnuityCouponFixedDefinition.from(CUR, SETTLEMENT_DATE, ANNUITY_TENOR, EURIBOR3M.getTenor(), TARGET, EURIBOR3M.getDayCount(),
        EURIBOR3M.getBusinessDayConvention(), EURIBOR3M.isEndOfMonth(), NOTIONAL, strike, IS_PAYER);
    final AnnuityCouponFixed fixed = fixedDefinition.toDerivative(referenceDate);
    MultipleCurrencyAmount pvFlooredExpected = MultipleCurrencyAmount.of(CUR, 0.0);
    pvFlooredExpected = pvFlooredExpected.plus(ratchetIbor.getNthPayment(0).accept(PVDC, MULTICURVES));
    for (int loopcpn = 1; loopcpn < cap.getNumberOfPayments(); loopcpn++) {
      pvFlooredExpected = pvFlooredExpected.plus(METHOD_HW_CAP.presentValue((CapFloorIbor) cap.getNthPayment(loopcpn), HW_MULTICURVES).multipliedBy(factor));
      pvFlooredExpected = pvFlooredExpected.plus(fixed.getNthPayment(loopcpn).accept(PVDC, MULTICURVES).multipliedBy(factor));
    }
    assertEquals("Annuity Ratchet Ibor - Hull-White - Monte Carlo - Degenerate in floor leg", pvFlooredExpected.getAmount(CUR), pvFlooredMC.getAmount(CUR), 2.5E+3);
  }

  //  @Test(enabled = true)
  //  /**
  //   * Tests the pricing with calibration to SABR cap/floor prices.
  //   */
  //  public void presentValueFixedWithCalibration() {
  //    final SABRInterestRateParameters sabrParameter = TestsDataSetsSABR.createSABR1();
  //    final SABRInterestRateDataBundle sabrBundle = new SABRInterestRateDataBundle(sabrParameter, MULTICURVES);
  //    final PresentValueSABRHullWhiteMonteCarloCalculator calculatorMC = PresentValueSABRHullWhiteMonteCarloCalculator.getInstance();
  //    final double pvMC = ANNUITY_RATCHET_FIXED.accept(calculatorMC, sabrBundle);
  //    final double pvMCPreviousRun = 8400036.210; //50000 paths: 8400036.210 - 12500 paths: 8402639.933;
  //    assertEquals("Annuity Ratchet Ibor - Hull-White - Monte Carlo", pvMCPreviousRun, pvMC, 1.0E-2);
  //  }

  @Test
  /**
   * Test the Ratchet present value curve sensitivity in the case where the first coupon is fixed.
   */
  public void presentValueCurveSensitivityFixed() {
    final MultipleCurrencyParameterSensitivity pvpsExact = PS_HW_C.calculateSensitivity(ANNUITY_RATCHET_FIXED, HW_MULTICURVES, HW_MULTICURVES.getMulticurveProvider().getAllNames());
    final MultipleCurrencyParameterSensitivity pvpsFD = PS_HW_FDC.calculateSensitivity(ANNUITY_RATCHET_FIXED, HW_MULTICURVES);
    AssertSensivityObjects.assertEquals("SwaptionPhysicalFixedIborSABRMethod: presentValueCurveSensitivity ", pvpsExact, pvpsFD, TOLERANCE_PV_DELTA_MC);
  }

  @Test(enabled = false)
  /**
   * Tests of performance for the price and curve sensitivity by Monte Carlo. "enabled = false" for the standard testing.
   */
  public void performance() {
    long startTime, endTime;
    final int nbTest = 10;
    final int nbPath = 12500;
    final AnnuityCouponIborRatchetDefinition annuityRatchetIbor20Definition = AnnuityCouponIborRatchetDefinition.withFirstCouponIborGearing(SETTLEMENT_DATE, Period.ofYears(5), NOTIONAL, EURIBOR3M,
        IS_PAYER, MAIN_COEF, FLOOR_COEF, CAP_COEF, TARGET);
    final ZonedDateTime referenceDate = DateUtils.getUTCDate(2011, 8, 18);
    final AnnuityCouponIborRatchet annuityRatchetIbor20 = annuityRatchetIbor20Definition.toDerivative(referenceDate, FIXING_TS);
    HullWhiteMonteCarloMethod methodMC;
    methodMC = new HullWhiteMonteCarloMethod(new NormalRandomNumberGenerator(0.0, 1.0, new MersenneTwister()), nbPath);
    final MultipleCurrencyAmount[] pvMC = new MultipleCurrencyAmount[nbTest];
    final MultipleCurrencyMulticurveSensitivity[] pvcsMC = new MultipleCurrencyMulticurveSensitivity[nbTest];

    startTime = System.currentTimeMillis();
    for (int looptest = 0; looptest < nbTest; looptest++) {
      pvMC[looptest] = methodMC.presentValue(annuityRatchetIbor20, CUR, HW_MULTICURVES);
    }
    endTime = System.currentTimeMillis();
    System.out.println(nbTest + " pv Ratchet Ibor Hull-White MC method: " + (endTime - startTime) + " ms");
    // Performance note: HW MC price (12500 paths): 07-Dec-12: On Mac Pro 3.2 GHz Quad-Core Intel Xeon: 565 ms for 10 Ratchet (20 coupons each).

    startTime = System.currentTimeMillis();
    for (int looptest = 0; looptest < nbTest; looptest++) {
      pvcsMC[looptest] = methodMC.presentValueCurveSensitivity(annuityRatchetIbor20, CUR, HW_MULTICURVES);
    }
    endTime = System.currentTimeMillis();
    System.out.println(nbTest + " delta Ratchet Ibor Hull-White MC method: " + (endTime - startTime) + " ms");
    // Performance note: HW MC delta (40 deltas - 12500 paths): 07-Dec-12: On Mac Pro 3.2 GHz Quad-Core Intel Xeon: 1220 ms for 10 Ratchet (20 coupons each).

    startTime = System.currentTimeMillis();
    for (int looptest = 0; looptest < nbTest; looptest++) {
      pvMC[looptest] = methodMC.presentValue(annuityRatchetIbor20, CUR, HW_MULTICURVES);
      pvcsMC[looptest] = methodMC.presentValueCurveSensitivity(annuityRatchetIbor20, CUR, HW_MULTICURVES);
    }
    endTime = System.currentTimeMillis();
    System.out.println(nbTest + " pv/delta Ratchet Ibor Hull-White MC method: " + (endTime - startTime) + " ms");
    // Performance note: HW MC price (12500 paths) - pv/delta: 07-Dec-12: On Mac Pro 3.2 GHz Quad-Core Intel Xeon: 1760 ms for 10 Ratchet (20 coupons each).
  }

}
