/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate.payments.provider;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertTrue;

import org.testng.annotations.Test;
import org.threeten.bp.Period;
import org.threeten.bp.ZonedDateTime;

import com.opengamma.analytics.financial.instrument.annuity.AnnuityCapFloorCMSDefinition;
import com.opengamma.analytics.financial.instrument.index.GeneratorSwapFixedIbor;
import com.opengamma.analytics.financial.instrument.index.GeneratorSwapFixedIborMaster;
import com.opengamma.analytics.financial.instrument.index.IborIndex;
import com.opengamma.analytics.financial.instrument.index.IndexSwap;
import com.opengamma.analytics.financial.instrument.payment.CapFloorCMSDefinition;
import com.opengamma.analytics.financial.instrument.payment.CouponCMSDefinition;
import com.opengamma.analytics.financial.instrument.payment.CouponFixedDefinition;
import com.opengamma.analytics.financial.instrument.swap.SwapFixedIborDefinition;
import com.opengamma.analytics.financial.interestrate.PresentValueSABRSensitivityDataBundle;
import com.opengamma.analytics.financial.interestrate.annuity.derivative.Annuity;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CapFloorCMS;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CouponCMS;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CouponFixed;
import com.opengamma.analytics.financial.interestrate.payments.derivative.Payment;
import com.opengamma.analytics.financial.model.option.definition.SABRInterestRateParameters;
import com.opengamma.analytics.financial.provider.calculator.discounting.PresentValueDiscountingCalculator;
import com.opengamma.analytics.financial.provider.calculator.sabrswaption.PresentValueCurveSensitivitySABRSwaptionCalculator;
import com.opengamma.analytics.financial.provider.calculator.sabrswaption.PresentValueSABRSensitivitySABRSwaptionCalculator;
import com.opengamma.analytics.financial.provider.calculator.sabrswaption.PresentValueSABRSwaptionCalculator;
import com.opengamma.analytics.financial.provider.description.MulticurveProviderDiscountDataSets;
import com.opengamma.analytics.financial.provider.description.SABRDataSets;
import com.opengamma.analytics.financial.provider.description.interestrate.MulticurveProviderDiscount;
import com.opengamma.analytics.financial.provider.description.interestrate.SABRSwaptionProviderDiscount;
import com.opengamma.analytics.financial.provider.description.interestrate.SABRSwaptionProviderInterface;
import com.opengamma.analytics.financial.provider.sensitivity.multicurve.MultipleCurrencyMulticurveSensitivity;
import com.opengamma.analytics.financial.provider.sensitivity.multicurve.MultipleCurrencyParameterSensitivity;
import com.opengamma.analytics.financial.provider.sensitivity.parameter.ParameterSensitivityParameterCalculator;
import com.opengamma.analytics.financial.provider.sensitivity.sabrswaption.ParameterSensitivitySABRSwaptionDiscountInterpolatedFDCalculator;
import com.opengamma.analytics.financial.schedule.ScheduleCalculator;
import com.opengamma.analytics.financial.util.AssertSensivityObjects;
import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.financial.convention.daycount.DayCount;
import com.opengamma.financial.convention.daycount.DayCounts;
import com.opengamma.util.money.Currency;
import com.opengamma.util.money.MultipleCurrencyAmount;
import com.opengamma.util.test.TestGroup;
import com.opengamma.util.time.DateUtils;
import com.opengamma.util.tuple.DoublesPair;

/**
 *  Test class for the replication method for CMS caplet/floorlet with a SABR smile.
 */
@Test(groups = TestGroup.UNIT)
public class CapFloorCMSSABRReplicationMethodTest {

  private static final MulticurveProviderDiscount MULTICURVES = MulticurveProviderDiscountDataSets.createMulticurveEurUsd();
  private static final IborIndex EURIBOR6M = MulticurveProviderDiscountDataSets.getIndexesIborMulticurveEurUsd()[1];
  private static final Currency EUR = EURIBOR6M.getCurrency();
  private static final Calendar CALENDAR = MulticurveProviderDiscountDataSets.getEURCalendar();

  private static final SABRInterestRateParameters SABR_PARAMETER = SABRDataSets.createSABR1();
  private static final GeneratorSwapFixedIbor EUR1YEURIBOR6M = GeneratorSwapFixedIborMaster.getInstance().getGenerator("EUR1YEURIBOR6M", CALENDAR);
  private static final SABRSwaptionProviderDiscount SABR_MULTICURVES = new SABRSwaptionProviderDiscount(MULTICURVES, SABR_PARAMETER, EUR1YEURIBOR6M);

  private static final ZonedDateTime REFERENCE_DATE = DateUtils.getUTCDate(2008, 8, 18);

  private static final ZonedDateTime SPOT_DATE = ScheduleCalculator.getAdjustedDate(REFERENCE_DATE, EURIBOR6M.getSpotLag(), CALENDAR);

  private static final Period ANNUITY_TENOR = Period.ofYears(5);
  private static final ZonedDateTime SETTLEMENT_DATE = DateUtils.getUTCDate(2011, 3, 17);

  private static final IndexSwap INDEX_SWAP_5Y = new IndexSwap(EUR1YEURIBOR6M, ANNUITY_TENOR);
  private static final SwapFixedIborDefinition SWAP_DEFINITION = SwapFixedIborDefinition.from(SETTLEMENT_DATE, ANNUITY_TENOR, EUR1YEURIBOR6M, 1.0, 0.0, true);

  private static final ZonedDateTime PAYMENT_DATE = DateUtils.getUTCDate(2011, 4, 6);
  private static final ZonedDateTime FIXING_DATE = DateUtils.getUTCDate(2010, 12, 30);
  private static final ZonedDateTime ACCRUAL_START_DATE = DateUtils.getUTCDate(2011, 1, 5);
  private static final ZonedDateTime ACCRUAL_END_DATE = DateUtils.getUTCDate(2011, 4, 5);
  private static final DayCount PAYMENT_DAY_COUNT = DayCounts.ACT_360;
  private static final double ACCRUAL_FACTOR = PAYMENT_DAY_COUNT.getDayCountFraction(ACCRUAL_START_DATE, ACCRUAL_END_DATE);
  private static final double NOTIONAL = 10000000; //10m

  private static final CouponCMSDefinition CMS_COUPON_DEFINITION = CouponCMSDefinition.from(PAYMENT_DATE, ACCRUAL_START_DATE, ACCRUAL_END_DATE, ACCRUAL_FACTOR, NOTIONAL, FIXING_DATE, SWAP_DEFINITION,
      INDEX_SWAP_5Y);

  private static final double STRIKE = 0.02;
  private static final boolean IS_CAP = true;
  private static final CapFloorCMSDefinition CMS_CAP_DEFINITION = CapFloorCMSDefinition.from(CMS_COUPON_DEFINITION, STRIKE, IS_CAP);
  private static final CapFloorCMSDefinition CMS_CAP_0_DEFINITION = CapFloorCMSDefinition.from(CMS_COUPON_DEFINITION, 0.0, IS_CAP);
  private static final CapFloorCMSDefinition CMS_FLOOR_DEFINITION = CapFloorCMSDefinition.from(CMS_COUPON_DEFINITION, STRIKE, !IS_CAP);
  private static final CouponFixedDefinition COUPON_STRIKE_DEFINITION = new CouponFixedDefinition(CMS_COUPON_DEFINITION, STRIKE);

  private static final CouponCMS CMS_COUPON = (CouponCMS) CMS_COUPON_DEFINITION.toDerivative(REFERENCE_DATE);
  private static final CapFloorCMS CMS_CAP_0 = (CapFloorCMS) CMS_CAP_0_DEFINITION.toDerivative(REFERENCE_DATE);
  private static final CapFloorCMS CMS_CAP = (CapFloorCMS) CMS_CAP_DEFINITION.toDerivative(REFERENCE_DATE);
  private static final CapFloorCMS CMS_FLOOR = (CapFloorCMS) CMS_FLOOR_DEFINITION.toDerivative(REFERENCE_DATE);
  private static final CouponFixed COUPON_STRIKE = COUPON_STRIKE_DEFINITION.toDerivative(REFERENCE_DATE);

  private static final CapFloorCMSSABRReplicationMethod METHOD_CAP_CMS_SABR = CapFloorCMSSABRReplicationMethod.getDefaultInstance();

  private static final PresentValueDiscountingCalculator PVDC = PresentValueDiscountingCalculator.getInstance();
  private static final PresentValueSABRSwaptionCalculator PVSSC = PresentValueSABRSwaptionCalculator.getInstance();
  private static final PresentValueCurveSensitivitySABRSwaptionCalculator PVCSSSC = PresentValueCurveSensitivitySABRSwaptionCalculator.getInstance();
  private static final PresentValueSABRSensitivitySABRSwaptionCalculator PVSSSSC = PresentValueSABRSensitivitySABRSwaptionCalculator.getInstance();

  private static final double SHIFT = 1.0E-7;
  private static final ParameterSensitivityParameterCalculator<SABRSwaptionProviderInterface> PS_SS_C = new ParameterSensitivityParameterCalculator<>(PVCSSSC);
  private static final ParameterSensitivitySABRSwaptionDiscountInterpolatedFDCalculator PS_SS_FDC = new ParameterSensitivitySABRSwaptionDiscountInterpolatedFDCalculator(PVSSC, SHIFT);

  private static final double TOLERANCE_PV = 1.0E-2;
  private static final double TOLERANCE_PV_DELTA = 1.0E+2; //Testing note: Sensitivity is for a movement of 1. 1E+2 = 1 cent for a 1 bp move.

  @Test
  /**
   * Tests the price of CMS coupon and cap/floor using replication in the SABR framework. Values are tested against hard-coded values.
   */
  public void testPriceReplication() {
    // CMS cap/floor with strike 0 has the same price as a CMS coupon.
    final MultipleCurrencyAmount priceCMSCoupon = PVSSC.visit(CMS_COUPON, SABR_MULTICURVES);
    final MultipleCurrencyAmount priceCMSCap0 = PVSSC.visit(CMS_CAP_0, SABR_MULTICURVES);
    assertEquals("CapFloorCMSSABRReplicationMethod: present value", priceCMSCoupon.getAmount(EUR), priceCMSCap0.getAmount(EUR), TOLERANCE_PV);
    final MultipleCurrencyAmount priceCMSCap = PVSSC.visit(CMS_CAP, SABR_MULTICURVES);
    assertEquals("CapFloorCMSSABRReplicationMethod: present value", 5224.559, priceCMSCap.getAmount(EUR), TOLERANCE_PV); //From previous run
    final MultipleCurrencyAmount priceCMSFloor = PVSSC.visit(CMS_FLOOR, SABR_MULTICURVES);
    assertEquals("CapFloorCMSSABRReplicationMethod: present value", 20149.939, priceCMSFloor.getAmount(EUR), TOLERANCE_PV); //From previous run
    final MultipleCurrencyAmount priceStrike = COUPON_STRIKE.accept(PVDC, MULTICURVES);
    // Cap/floor parity: !cash-settled swaption price is arbitrable: no exact cap/floor/swap parity!
    assertEquals("CapFloorCMSSABRReplicationMethod: present value", priceCMSCap.getAmount(EUR) - priceCMSFloor.getAmount(EUR), priceCMSCoupon.getAmount(EUR) - priceStrike.getAmount(EUR), 2.0E+3);
  }

  @Test
  /**
   * Tests the price of CMS coupon and cap/floor using replication in the SABR framework. Values are tested against hard-coded values.
   */
  public void presentValueMethodVsCalculator() {
    final double pvMethod = METHOD_CAP_CMS_SABR.presentValue(CMS_CAP, SABR_MULTICURVES).getAmount(EUR);
    final double pvCalculator = PVSSC.visit(CMS_CAP, SABR_MULTICURVES).getAmount(EUR);
    assertEquals("CMS cap/floor SABR: Present value : method vs calculator", pvMethod, pvCalculator, TOLERANCE_PV);
  }

  @Test
  /**
   * Tests present value curve sensitivity when the valuation date is on trade date.
   */
  public void presentValueCurveSensitivityCap() {
    final MultipleCurrencyParameterSensitivity pvpsExact = PS_SS_C.calculateSensitivity(CMS_CAP, SABR_MULTICURVES, SABR_MULTICURVES.getMulticurveProvider().getAllNames());
    final MultipleCurrencyParameterSensitivity pvpsFD = PS_SS_FDC.calculateSensitivity(CMS_CAP, SABR_MULTICURVES);
    AssertSensivityObjects.assertEquals("SwaptionPhysicalFixedIborSABRMethod: presentValueCurveSensitivity ", pvpsExact, pvpsFD, TOLERANCE_PV_DELTA);
  }

  @Test
  /**
   * Tests present value curve sensitivity when the valuation date is on trade date.
   */
  public void presentValueCurveSensitivityFoor() {
    final MultipleCurrencyParameterSensitivity pvpsExact = PS_SS_C.calculateSensitivity(CMS_FLOOR, SABR_MULTICURVES, SABR_MULTICURVES.getMulticurveProvider().getAllNames());
    final MultipleCurrencyParameterSensitivity pvpsFD = PS_SS_FDC.calculateSensitivity(CMS_FLOOR, SABR_MULTICURVES);
    AssertSensivityObjects.assertEquals("SwaptionPhysicalFixedIborSABRMethod: presentValueCurveSensitivity ", pvpsExact, pvpsFD, TOLERANCE_PV_DELTA);
  }

  @Test
  /**
   * Tests the present value SABR parameters sensitivity vs finite difference.
   */
  public void presentValueSABRSensitivity() {
    final double pv = METHOD_CAP_CMS_SABR.presentValue(CMS_CAP, SABR_MULTICURVES).getAmount(EUR);
    final PresentValueSABRSensitivityDataBundle pvsCapLong = METHOD_CAP_CMS_SABR.presentValueSABRSensitivity(CMS_CAP, SABR_MULTICURVES);
    // SABR sensitivity vs finite difference
    final double shift = 0.0001;
    final double shiftAlpha = 0.00001;
    final double maturity = CMS_CAP.getUnderlyingSwap().getFixedLeg().getNthPayment(CMS_CAP.getUnderlyingSwap().getFixedLeg().getNumberOfPayments() - 1).getPaymentTime() - CMS_CAP.getSettlementTime();
    final DoublesPair expectedExpiryTenor = DoublesPair.of(CMS_CAP.getFixingTime(), maturity);
    // Alpha sensitivity vs finite difference computation
    final SABRInterestRateParameters sabrParameterAlphaBumped = SABRDataSets.createSABR1AlphaBumped(shiftAlpha);
    final SABRSwaptionProviderDiscount sabrBundleAlphaBumped = new SABRSwaptionProviderDiscount(MULTICURVES, sabrParameterAlphaBumped, EUR1YEURIBOR6M);
    final double pvLongPayerAlphaBumped = METHOD_CAP_CMS_SABR.presentValue(CMS_CAP, sabrBundleAlphaBumped).getAmount(EUR);
    final double expectedAlphaSensi = (pvLongPayerAlphaBumped - pv) / shiftAlpha;
    assertEquals("Number of alpha sensitivity", pvsCapLong.getAlpha().getMap().keySet().size(), 1);
    assertEquals("Alpha sensitivity expiry/tenor", pvsCapLong.getAlpha().getMap().keySet().contains(expectedExpiryTenor), true);
    assertEquals("Alpha sensitivity value", expectedAlphaSensi, pvsCapLong.getAlpha().getMap().get(expectedExpiryTenor), 3.0E+1);
    // Rho sensitivity vs finite difference computation
    final SABRInterestRateParameters sabrParameterRhoBumped = SABRDataSets.createSABR1RhoBumped();
    final SABRSwaptionProviderDiscount sabrBundleRhoBumped = new SABRSwaptionProviderDiscount(MULTICURVES, sabrParameterRhoBumped, EUR1YEURIBOR6M);
    final double pvLongPayerRhoBumped = METHOD_CAP_CMS_SABR.presentValue(CMS_CAP, sabrBundleRhoBumped).getAmount(EUR);
    final double expectedRhoSensi = (pvLongPayerRhoBumped - pv) / shift;
    assertEquals("Number of rho sensitivity", pvsCapLong.getRho().getMap().keySet().size(), 1);
    assertEquals("Rho sensitivity expiry/tenor", pvsCapLong.getRho().getMap().keySet().contains(expectedExpiryTenor), true);
    assertEquals("Rho sensitivity value", expectedRhoSensi, pvsCapLong.getRho().getMap().get(expectedExpiryTenor), 1.0E+0);
    // Alpha sensitivity vs finite difference computation
    final SABRInterestRateParameters sabrParameterNuBumped = SABRDataSets.createSABR1NuBumped();
    final SABRSwaptionProviderDiscount sabrBundleNuBumped = new SABRSwaptionProviderDiscount(MULTICURVES, sabrParameterNuBumped, EUR1YEURIBOR6M);
    final double pvLongPayerNuBumped = METHOD_CAP_CMS_SABR.presentValue(CMS_CAP, sabrBundleNuBumped).getAmount(EUR);
    final double expectedNuSensi = (pvLongPayerNuBumped - pv) / shift;
    assertEquals("Number of nu sensitivity", pvsCapLong.getNu().getMap().keySet().size(), 1);
    assertTrue("Nu sensitivity expiry/tenor", pvsCapLong.getNu().getMap().keySet().contains(expectedExpiryTenor));
    assertEquals("Nu sensitivity value", expectedNuSensi, pvsCapLong.getNu().getMap().get(expectedExpiryTenor), 2.0E+0);
  }

  @Test
  /**
   * Tests the present value SABR parameters sensitivity: Method vs Calculator.
   */
  public void presentValueSABRSensitivityMethodVsCalculator() {
    final PresentValueSABRSensitivityDataBundle pvssMethod = METHOD_CAP_CMS_SABR.presentValueSABRSensitivity(CMS_CAP, SABR_MULTICURVES);
    final PresentValueSABRSensitivityDataBundle pvssCalculator = PVSSSSC.visit(CMS_CAP, SABR_MULTICURVES);
    assertEquals("CMS cap/floor SABR: Present value SABR sensitivity: method vs calculator", pvssMethod, pvssCalculator);
  }

  @Test
  /**
   * Tests the present value strike sensitivity: Cap.
   */
  public void presentValueStrikeSensitivityCap() {
    final double[] strikes = new double[] {0.0001, 0.0010, 0.0050, 0.0100, 0.0200, 0.0400, 0.0500};
    final int nbStrikes = strikes.length;
    final double shift = 1.0E-5;
    final double[] errorRelative = new double[nbStrikes];
    for (int loopstrike = 0; loopstrike < nbStrikes; loopstrike++) {
      final CapFloorCMSDefinition cmsCapDefinition = CapFloorCMSDefinition.from(CMS_COUPON_DEFINITION, strikes[loopstrike], IS_CAP);
      final CapFloorCMSDefinition cmsCapShiftUpDefinition = CapFloorCMSDefinition.from(CMS_COUPON_DEFINITION, strikes[loopstrike] + shift, IS_CAP);
      final CapFloorCMSDefinition cmsCapShiftDoDefinition = CapFloorCMSDefinition.from(CMS_COUPON_DEFINITION, strikes[loopstrike] - shift, IS_CAP);
      final CapFloorCMS cmsCap = (CapFloorCMS) cmsCapDefinition.toDerivative(REFERENCE_DATE);
      final CapFloorCMS cmsCapShiftUp = (CapFloorCMS) cmsCapShiftUpDefinition.toDerivative(REFERENCE_DATE);
      final CapFloorCMS cmsCapShiftDo = (CapFloorCMS) cmsCapShiftDoDefinition.toDerivative(REFERENCE_DATE);
      final double pvShiftUp = METHOD_CAP_CMS_SABR.presentValue(cmsCapShiftUp, SABR_MULTICURVES).getAmount(EUR);
      final double pvShiftDo = METHOD_CAP_CMS_SABR.presentValue(cmsCapShiftDo, SABR_MULTICURVES).getAmount(EUR);
      final double sensiExpected = (pvShiftUp - pvShiftDo) / (2 * shift);
      final double sensiComputed = METHOD_CAP_CMS_SABR.presentValueStrikeSensitivity(cmsCap, SABR_MULTICURVES);
      errorRelative[loopstrike] = (sensiExpected - sensiComputed) / sensiExpected;
      assertEquals("CMS cap/floor SABR: Present value strike sensitivity " + loopstrike, 0, errorRelative[loopstrike], 5.0E-4); // Numerical imprecision, reduce to E-6 when nbInteration = 1000;
    }
  }

  @Test
  /**
   * Tests the present value strike sensitivity: Floor.
   */
  public void presentValueStrikeSensitivityFloor() {
    final double[] strikes = new double[] {0.0001, 0.0010, 0.0050, 0.0100, 0.0200, 0.0400};
    final int nbStrikes = strikes.length;
    final double shift = 1.0E-5;
    final double[] errorRelative = new double[nbStrikes];
    for (int loopstrike = 0; loopstrike < nbStrikes; loopstrike++) {
      final CapFloorCMSDefinition cmsFloorDefinition = CapFloorCMSDefinition.from(CMS_COUPON_DEFINITION, strikes[loopstrike], !IS_CAP);
      final CapFloorCMSDefinition cmsFloorShiftUpDefinition = CapFloorCMSDefinition.from(CMS_COUPON_DEFINITION, strikes[loopstrike] + shift, !IS_CAP);
      final CapFloorCMSDefinition cmsFloorShiftDoDefinition = CapFloorCMSDefinition.from(CMS_COUPON_DEFINITION, strikes[loopstrike] - shift, !IS_CAP);
      final CapFloorCMS cmsFloor = (CapFloorCMS) cmsFloorDefinition.toDerivative(REFERENCE_DATE);
      final CapFloorCMS cmsFloorShiftUp = (CapFloorCMS) cmsFloorShiftUpDefinition.toDerivative(REFERENCE_DATE);
      final CapFloorCMS cmsFloorShiftDo = (CapFloorCMS) cmsFloorShiftDoDefinition.toDerivative(REFERENCE_DATE);
      final double pvShiftUp = METHOD_CAP_CMS_SABR.presentValue(cmsFloorShiftUp, SABR_MULTICURVES).getAmount(EUR);
      final double pvShiftDo = METHOD_CAP_CMS_SABR.presentValue(cmsFloorShiftDo, SABR_MULTICURVES).getAmount(EUR);
      final double sensiExpected = (pvShiftUp - pvShiftDo) / (2 * shift);
      final double sensiComputed = METHOD_CAP_CMS_SABR.presentValueStrikeSensitivity(cmsFloor, SABR_MULTICURVES);
      errorRelative[loopstrike] = (sensiExpected - sensiComputed) / sensiExpected;
      assertEquals("CMS cap/floor SABR: Present value strike sensitivity " + loopstrike, 0, errorRelative[loopstrike], 5.0E-4);
    }
  }

  @Test
  /**
   * Tests the present value of an annuity vs the sum of pv of each caplet.
   */
  public void presentValueAnnuity() {
    final Period START_CMSCAP = Period.ofYears(5);
    final Period LENGTH_CMSCAP = Period.ofYears(10);
    final ZonedDateTime START_DATE = ScheduleCalculator.getAdjustedDate(SPOT_DATE, START_CMSCAP, EUR1YEURIBOR6M.getIborIndex().getBusinessDayConvention(), CALENDAR, EUR1YEURIBOR6M.getIborIndex()
        .isEndOfMonth());
    final ZonedDateTime END_DATE = START_DATE.plus(LENGTH_CMSCAP);
    final Period capPeriod = Period.ofMonths(6);
    final DayCount capDayCount = DayCounts.ACT_360;
    final AnnuityCapFloorCMSDefinition capDefinition = AnnuityCapFloorCMSDefinition.from(START_DATE, END_DATE, NOTIONAL, INDEX_SWAP_5Y, capPeriod, capDayCount, false, STRIKE, IS_CAP, CALENDAR);
    final Annuity<? extends Payment> cap = capDefinition.toDerivative(REFERENCE_DATE);
    final double pvCalculator = PVSSC.visit(cap, SABR_MULTICURVES).getAmount(EUR);
    double pvExpected = 0.0;
    for (int loopcpn = 0; loopcpn < cap.getNumberOfPayments(); loopcpn++) {
      pvExpected += PVSSC.visit(cap.getNthPayment(loopcpn), SABR_MULTICURVES).getAmount(EUR);
    }
    assertEquals("Cap annuity - SABR pv", pvExpected, pvCalculator, 1.0E-2);
  }

  @Test
  /**
   * Tests the present value of an annuity vs the sum of pv of each caplet.
   */
  public void presentValueCurveSensitivityAnnuity() {
    final Period START_CMSCAP = Period.ofYears(5);
    final Period LENGTH_CMSCAP = Period.ofYears(10);
    final ZonedDateTime START_DATE = ScheduleCalculator.getAdjustedDate(SPOT_DATE, START_CMSCAP, EUR1YEURIBOR6M.getIborIndex().getBusinessDayConvention(), CALENDAR, EUR1YEURIBOR6M.getIborIndex()
        .isEndOfMonth());
    final ZonedDateTime END_DATE = START_DATE.plus(LENGTH_CMSCAP);
    final Period capPeriod = Period.ofMonths(6);
    final DayCount capDayCount = DayCounts.ACT_360;
    final AnnuityCapFloorCMSDefinition capDefinition = AnnuityCapFloorCMSDefinition.from(START_DATE, END_DATE, NOTIONAL, INDEX_SWAP_5Y, capPeriod, capDayCount, false, STRIKE, IS_CAP, CALENDAR);
    final Annuity<? extends Payment> cap = capDefinition.toDerivative(REFERENCE_DATE);
    MultipleCurrencyMulticurveSensitivity pvcsCalculator = PVCSSSC.visit(cap, SABR_MULTICURVES);
    pvcsCalculator = pvcsCalculator.cleaned();
    MultipleCurrencyMulticurveSensitivity pvcsExpected = new MultipleCurrencyMulticurveSensitivity();
    for (int loopcpn = 0; loopcpn < cap.getNumberOfPayments(); loopcpn++) {
      pvcsExpected = pvcsExpected.plus(PVCSSSC.visit(cap.getNthPayment(loopcpn), SABR_MULTICURVES));
    }
    pvcsExpected = pvcsExpected.cleaned();
    AssertSensivityObjects.assertEquals("Cap annuity - SABR pv", pvcsExpected, pvcsCalculator, 1.0E-2);
  }

  //  @Test(enabled = false)
  //  /**
  //   * Tests of performance. "enabled = false" for the standard testing.
  //   */
  //  public void performance() {
  //    long startTime, endTime;
  //    final int nbTest = 1000;
  //
  //    startTime = System.currentTimeMillis();
  //    for (int looptest = 0; looptest < nbTest; looptest++) {
  //      PV.visit(CMS_CAP, SABR_MULTICURVES);
  //      PVCSC_SABR.visit(CMS_CAP, SABR_MULTICURVES);
  //      PVSSC_SABR.visit(CMS_CAP, SABR_MULTICURVES);
  //    }
  //    endTime = System.currentTimeMillis();
  //    System.out.println(nbTest + " CMS cap by replication (price+delta+vega): " + (endTime - startTime) + " ms");
  //    // Performance note: price+delta: 9-Dec-11: On Mac Pro 3.2 GHz Quad-Core Intel Xeon: 280 ms for 1000 cap 5Y.
  //    // Performance note: price+delta+vega: 9-Dec-11: On Mac Pro 3.2 GHz Quad-Core Intel Xeon: 790 ms for 1000 cap 5Y.
  //
  //    startTime = System.currentTimeMillis();
  //    for (int looptest = 0; looptest < nbTest; looptest++) {
  //      PV.visit(CMS_FLOOR, SABR_MULTICURVES);
  //      PVCSC_SABR.visit(CMS_FLOOR, SABR_MULTICURVES);
  //      PVSSC_SABR.visit(CMS_FLOOR, SABR_MULTICURVES);
  //    }
  //    endTime = System.currentTimeMillis();
  //    System.out.println(nbTest + " CMS floor by replication (price+delta+vega): " + (endTime - startTime) + " ms");
  //    // Performance note: price+delta: 9-Dec-11: On Mac Pro 3.2 GHz Quad-Core Intel Xeon: 230 ms for 1000 floor 5Y.
  //    // Performance note: price+delta+vega: 9-Dec-11: On Mac Pro 3.2 GHz Quad-Core Intel Xeon: 555 ms for 1000 cap 5Y.
  //  }

}
