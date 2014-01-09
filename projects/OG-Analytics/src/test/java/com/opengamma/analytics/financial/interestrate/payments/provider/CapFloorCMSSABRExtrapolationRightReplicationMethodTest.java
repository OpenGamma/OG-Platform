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

import com.opengamma.analytics.financial.instrument.annuity.AnnuityCouponFixedDefinition;
import com.opengamma.analytics.financial.instrument.annuity.AnnuityCouponIborDefinition;
import com.opengamma.analytics.financial.instrument.index.GeneratorSwapFixedIbor;
import com.opengamma.analytics.financial.instrument.index.GeneratorSwapFixedIborMaster;
import com.opengamma.analytics.financial.instrument.index.IborIndex;
import com.opengamma.analytics.financial.instrument.index.IndexSwap;
import com.opengamma.analytics.financial.instrument.payment.CapFloorCMSDefinition;
import com.opengamma.analytics.financial.instrument.payment.CouponCMSDefinition;
import com.opengamma.analytics.financial.instrument.swap.SwapFixedIborDefinition;
import com.opengamma.analytics.financial.interestrate.PresentValueSABRSensitivityDataBundle;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CapFloorCMS;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CouponCMS;
import com.opengamma.analytics.financial.model.option.definition.SABRInterestRateParameters;
import com.opengamma.analytics.financial.provider.calculator.sabrswaption.PresentValueCurveSensitivitySABRSwaptionRightExtrapolationCalculator;
import com.opengamma.analytics.financial.provider.calculator.sabrswaption.PresentValueSABRSensitivitySABRSwaptionRightExtrapolationCalculator;
import com.opengamma.analytics.financial.provider.calculator.sabrswaption.PresentValueSABRSwaptionRightExtrapolationCalculator;
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
import com.opengamma.financial.convention.businessday.BusinessDayConvention;
import com.opengamma.financial.convention.businessday.BusinessDayConventions;
import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.financial.convention.daycount.DayCount;
import com.opengamma.financial.convention.daycount.DayCounts;
import com.opengamma.util.money.Currency;
import com.opengamma.util.test.TestGroup;
import com.opengamma.util.time.DateUtils;
import com.opengamma.util.tuple.DoublesPair;

/**
 *  Test class for the replication method for CMS caplet/floorlet using a SABR smile with extrapolation.
 */
@Test(groups = TestGroup.UNIT)
public class CapFloorCMSSABRExtrapolationRightReplicationMethodTest {

  private static final MulticurveProviderDiscount MULTICURVES = MulticurveProviderDiscountDataSets.createMulticurveEurUsd();
  private static final IborIndex EURIBOR6M = MulticurveProviderDiscountDataSets.getIndexesIborMulticurveEurUsd()[1];
  private static final Currency EUR = EURIBOR6M.getCurrency();
  private static final Calendar CALENDAR = MulticurveProviderDiscountDataSets.getEURCalendar();

  private static final SABRInterestRateParameters SABR_PARAMETER = SABRDataSets.createSABR1();
  private static final GeneratorSwapFixedIbor EUR1YEURIBOR6M = GeneratorSwapFixedIborMaster.getInstance().getGenerator("EUR1YEURIBOR6M", CALENDAR);
  private static final SABRSwaptionProviderDiscount SABR_MULTICURVES = new SABRSwaptionProviderDiscount(MULTICURVES, SABR_PARAMETER, EUR1YEURIBOR6M);

  //Swap 5Y
  private static final BusinessDayConvention BUSINESS_DAY = BusinessDayConventions.MODIFIED_FOLLOWING;
  private static final boolean IS_EOM = true;
  private static final Period ANNUITY_TENOR = Period.ofYears(5);
  private static final ZonedDateTime SETTLEMENT_DATE = DateUtils.getUTCDate(2020, 4, 28);
  //Fixed leg: Semi-annual bond
  private static final Period FIXED_PAYMENT_PERIOD = Period.ofMonths(6);
  private static final DayCount FIXED_DAY_COUNT = DayCounts.THIRTY_U_360;
  private static final double RATE = 0.0325;
  private static final boolean FIXED_IS_PAYER = true;
  private static final AnnuityCouponFixedDefinition FIXED_ANNUITY = AnnuityCouponFixedDefinition.from(EUR, SETTLEMENT_DATE, ANNUITY_TENOR, FIXED_PAYMENT_PERIOD, CALENDAR, FIXED_DAY_COUNT,
      BUSINESS_DAY, IS_EOM, 1.0, RATE, FIXED_IS_PAYER);
  //Ibor leg: quarterly money
  private static final AnnuityCouponIborDefinition IBOR_ANNUITY = AnnuityCouponIborDefinition.from(SETTLEMENT_DATE, ANNUITY_TENOR, 1.0, EURIBOR6M, !FIXED_IS_PAYER, CALENDAR);
  // CMS coupon construction
  private static final IndexSwap CMS_INDEX = new IndexSwap(FIXED_PAYMENT_PERIOD, FIXED_DAY_COUNT, EURIBOR6M, ANNUITY_TENOR, CALENDAR);
  private static final SwapFixedIborDefinition SWAP_DEFINITION = new SwapFixedIborDefinition(FIXED_ANNUITY, IBOR_ANNUITY);
  private static final ZonedDateTime FIXING_DATE = ScheduleCalculator.getAdjustedDate(SETTLEMENT_DATE, -EURIBOR6M.getSpotLag(), CALENDAR);
  private static final ZonedDateTime ACCRUAL_START_DATE = SETTLEMENT_DATE; // pre-fixed
  private static final ZonedDateTime ACCRUAL_END_DATE = ScheduleCalculator.getAdjustedDate(ACCRUAL_START_DATE, FIXED_PAYMENT_PERIOD, BUSINESS_DAY, CALENDAR);
  private static final ZonedDateTime PAYMENT_DATE = ACCRUAL_END_DATE;
  private static final DayCount PAYMENT_DAY_COUNT = DayCounts.ACT_360;
  private static final double ACCRUAL_FACTOR = PAYMENT_DAY_COUNT.getDayCountFraction(ACCRUAL_START_DATE, ACCRUAL_END_DATE);
  private static final double NOTIONAL = 10000000; //10m
  private static final CouponCMSDefinition CMS_COUPON_RECEIVER_DEFINITION = CouponCMSDefinition.from(PAYMENT_DATE, ACCRUAL_START_DATE, ACCRUAL_END_DATE, ACCRUAL_FACTOR, NOTIONAL, FIXING_DATE,
      SWAP_DEFINITION, CMS_INDEX);
  private static final CouponCMSDefinition CMS_COUPON_PAYER_DEFINITION = CouponCMSDefinition.from(PAYMENT_DATE, ACCRUAL_START_DATE, ACCRUAL_END_DATE, ACCRUAL_FACTOR, -NOTIONAL, FIXING_DATE,
      SWAP_DEFINITION, CMS_INDEX);
  // Cap/Floor construction
  private static final double STRIKE = 0.04;
  private static final boolean IS_CAP = true;
  private static final CapFloorCMSDefinition CMS_CAP_LONG_DEFINITION = CapFloorCMSDefinition.from(CMS_COUPON_RECEIVER_DEFINITION, STRIKE, IS_CAP);
  private static final CapFloorCMSDefinition CMS_CAP_SHORT_DEFINITION = CapFloorCMSDefinition.from(CMS_COUPON_PAYER_DEFINITION, STRIKE, IS_CAP);
  private static final CapFloorCMSDefinition CMS_CAP_0_DEFINITION = CapFloorCMSDefinition.from(CMS_COUPON_RECEIVER_DEFINITION, 0.0, IS_CAP);
  // to derivatives
  private static final ZonedDateTime REFERENCE_DATE = DateUtils.getUTCDate(2010, 8, 18);

  private static final CouponCMS CMS_COUPON = (CouponCMS) CMS_COUPON_RECEIVER_DEFINITION.toDerivative(REFERENCE_DATE);
  private static final CapFloorCMS CMS_CAP_0 = (CapFloorCMS) CMS_CAP_0_DEFINITION.toDerivative(REFERENCE_DATE);
  private static final CapFloorCMS CMS_CAP_LONG = (CapFloorCMS) CMS_CAP_LONG_DEFINITION.toDerivative(REFERENCE_DATE);
  private static final CapFloorCMS CMS_CAP_SHORT = (CapFloorCMS) CMS_CAP_SHORT_DEFINITION.toDerivative(REFERENCE_DATE);
  // Calculators & methods
  private static final CapFloorCMSSABRReplicationMethod METHOD_STANDARD_CAP = CapFloorCMSSABRReplicationMethod.getDefaultInstance();
  private static final CouponCMSSABRReplicationMethod METHOD_STANDARD_CPN = CouponCMSSABRReplicationMethod.getInstance();
  private static final CouponCMSDiscountingMethod METHOD_DSC_CPN = CouponCMSDiscountingMethod.getInstance();

  private static final double CUT_OFF_STRIKE = 0.10;
  private static final double MU = 2.50;
  private static final CapFloorCMSSABRExtrapolationRightReplicationMethod METHOD_EXTRAPOLATION_CAP = new CapFloorCMSSABRExtrapolationRightReplicationMethod(CUT_OFF_STRIKE, MU);
  private static final CouponCMSSABRExtrapolationRightReplicationMethod METHOD_EXTRAPOLATION_CPN = new CouponCMSSABRExtrapolationRightReplicationMethod(CUT_OFF_STRIKE, MU);
  // Calculators
  private static final PresentValueSABRSwaptionRightExtrapolationCalculator PVSSXC = new PresentValueSABRSwaptionRightExtrapolationCalculator(CUT_OFF_STRIKE, MU);
  private static final PresentValueCurveSensitivitySABRSwaptionRightExtrapolationCalculator PVCSSSXC = new PresentValueCurveSensitivitySABRSwaptionRightExtrapolationCalculator(CUT_OFF_STRIKE, MU);
  private static final PresentValueSABRSensitivitySABRSwaptionRightExtrapolationCalculator PVSSSSXC = new PresentValueSABRSensitivitySABRSwaptionRightExtrapolationCalculator(CUT_OFF_STRIKE, MU);

  private static final double SHIFT = 1.0E-6;
  private static final ParameterSensitivityParameterCalculator<SABRSwaptionProviderInterface> PS_SS_C = new ParameterSensitivityParameterCalculator<>(PVCSSSXC);
  private static final ParameterSensitivitySABRSwaptionDiscountInterpolatedFDCalculator PS_SS_FDC = new ParameterSensitivitySABRSwaptionDiscountInterpolatedFDCalculator(PVSSXC, SHIFT);

  private static final double TOLERANCE_PV = 1.0E-2;
  private static final double TOLERANCE_PV_DELTA = 5.0E+3; // 0.01 currency unit for 1 bp.

  @Test
  /**
   * Test the present value for a CMS coupon with pricing by replication in the SABR with extrapolation framework.
   * The present value is tested against hard-coded value and cap of strike 0.
   */
  public void presentValue() {
    // CMS cap/floor with strike 0 has the same price as a CMS coupon.
    final double priceCouponStd = METHOD_STANDARD_CPN.presentValue(CMS_COUPON, SABR_MULTICURVES).getAmount(EUR);
    final double rateCouponStd = priceCouponStd / (CMS_COUPON.getPaymentYearFraction() * CMS_COUPON.getNotional() * MULTICURVES.getDiscountFactor(EUR, CMS_COUPON.getPaymentTime()));
    final double priceCouponExtra = METHOD_EXTRAPOLATION_CPN.presentValue(CMS_COUPON, SABR_MULTICURVES).getAmount(EUR);
    final double rateCouponExtra = priceCouponExtra / (CMS_COUPON.getPaymentYearFraction() * CMS_COUPON.getNotional() * MULTICURVES.getDiscountFactor(EUR, CMS_COUPON.getPaymentTime()));
    final double priceCouponNoAdj = METHOD_DSC_CPN.presentValue(CMS_COUPON, MULTICURVES).getAmount(EUR);
    final double rateCouponNoAdj = priceCouponNoAdj / (CMS_COUPON.getPaymentYearFraction() * CMS_COUPON.getNotional() * MULTICURVES.getDiscountFactor(EUR, CMS_COUPON.getPaymentTime()));
    assertEquals("Extrapolation: comparison with standard method", rateCouponStd > rateCouponExtra, true);
    assertEquals("Extrapolation: comparison with no convexity adjustment", rateCouponExtra > rateCouponNoAdj, true);
    final double rateCouponExtraExpected = 0.0189864; // From previous run.
    assertEquals("Extrapolation: hard-coded value", rateCouponExtraExpected, rateCouponExtra, 1E-6);
    final double priceCap0Extra = METHOD_EXTRAPOLATION_CAP.presentValue(CMS_CAP_0, SABR_MULTICURVES).getAmount(EUR);
    assertEquals("Extrapolation: CMS coupon vs Cap 0", priceCouponExtra, priceCap0Extra, TOLERANCE_PV);
  }

  //  @Test
  //  /**
  //   * Tests the method against the present value calculator.
  //   */
  //  public void presentValueCouponMethodSpecificVsGeneric() {
  //    final double pvSpecific = METHOD_EXTRAPOLATION_CPN.presentValue(CMS_COUPON, SABR_MULTICURVES).getAmount(EUR);
  //    final CurrencyAmount pvGeneric = METHOD_GENERIC.presentValue(CMS_COUPON, SABR_MULTICURVES);
  //    assertEquals("Coupon CMS SABR extrapolation: method : Specific vs Generic", pvSpecific, pvGeneric.getAmount(), TOLERANCE_PRICE);
  //  }

  @Test
  /**
   * Tests the price of CMS coupon and cap/floor using replication in the SABR framework.  Method v Calculator.
   */
  public void presentValueMethodVsCalculator() {
    final double pvMethod = METHOD_EXTRAPOLATION_CAP.presentValue(CMS_CAP_LONG, SABR_MULTICURVES).getAmount(EUR);
    final double pvCalculator = CMS_CAP_LONG.accept(PVSSXC, SABR_MULTICURVES).getAmount(EUR);
    assertEquals("CMS cap/floor SABR: Present value : method vs calculator", pvMethod, pvCalculator, TOLERANCE_PV);
  }

  @Test
  /**
   * Test the present value for a CMS cap with pricing by replication in the SABR with extrapolation framework.
   * The present value is tested against hard-coded value and a long/short parity is tested.
   */
  public void presentValueReplicationCap() {
    // CMS cap/floor with strike 0 has the same price as a CMS coupon.
    final double priceCapLongStd = METHOD_STANDARD_CAP.presentValue(CMS_CAP_LONG, SABR_MULTICURVES).getAmount(EUR);
    final double priceCapLongExtra = METHOD_EXTRAPOLATION_CAP.presentValue(CMS_CAP_LONG, SABR_MULTICURVES).getAmount(EUR);
    final double priceCapShortExtra = METHOD_EXTRAPOLATION_CAP.presentValue(CMS_CAP_SHORT, SABR_MULTICURVES).getAmount(EUR);
    assertEquals("CMS cap by replication - Extrapolation: comparison with standard method", priceCapLongStd > priceCapLongExtra, true);
    final double priceCapExtraExpected = 30696.572; // From previous run.
    assertEquals("CMS cap by replication - Extrapolation: hard-coded value", priceCapExtraExpected, priceCapLongExtra, TOLERANCE_PV);
    assertEquals("CMS cap by replication - Extrapolation: long/short parity", -priceCapShortExtra, priceCapLongExtra, TOLERANCE_PV);
  }

  @Test
  /**
   * Test the present value rate sensitivity for a CMS cap with pricing by replication in the SABR with extrapolation framework.
   */
  public void presentValueCurveSensitivity() {
    final MultipleCurrencyParameterSensitivity pvpsCapLongExact = PS_SS_C.calculateSensitivity(CMS_CAP_LONG, SABR_MULTICURVES, SABR_MULTICURVES.getMulticurveProvider().getAllNames());
    final MultipleCurrencyParameterSensitivity pvpsCapLongFD = PS_SS_FDC.calculateSensitivity(CMS_CAP_LONG, SABR_MULTICURVES);
    AssertSensivityObjects.assertEquals("SwaptionPhysicalFixedIborSABRMethod: presentValueCurveSensitivity ", pvpsCapLongExact, pvpsCapLongFD, TOLERANCE_PV_DELTA);
    final MultipleCurrencyParameterSensitivity pvpsCapShortExact = PS_SS_C.calculateSensitivity(CMS_CAP_SHORT, SABR_MULTICURVES, SABR_MULTICURVES.getMulticurveProvider().getAllNames());
    final MultipleCurrencyParameterSensitivity pvpsCapShortFD = PS_SS_FDC.calculateSensitivity(CMS_CAP_SHORT, SABR_MULTICURVES);
    AssertSensivityObjects.assertEquals("SwaptionPhysicalFixedIborSABRMethod: presentValueCurveSensitivity ", pvpsCapShortExact, pvpsCapShortFD, TOLERANCE_PV_DELTA);
  }

  @Test
  /**
   * Test the present value rate sensitivity for a CMS cap with pricing by replication in the SABR with extrapolation framework. Method v Calculator.
   */
  public void presentValueCurveSensitivityMethodVsCalculator() {
    final MultipleCurrencyMulticurveSensitivity pvcsMethod = METHOD_EXTRAPOLATION_CAP.presentValueCurveSensitivity(CMS_CAP_LONG, SABR_MULTICURVES);
    final MultipleCurrencyMulticurveSensitivity pvcsCalculator = CMS_CAP_LONG.accept(PVCSSSXC, SABR_MULTICURVES);
    AssertSensivityObjects.assertEquals("CMS cap/floor SABR: Present value : method vs calculator", pvcsMethod, pvcsCalculator, TOLERANCE_PV_DELTA);
  }

  @Test
  /**
   * Tests the cap present value SABR parameters sensitivity vs finite difference.
   */
  public void presentValueSABRSensitivity() {
    final double pv = METHOD_EXTRAPOLATION_CAP.presentValue(CMS_CAP_LONG, SABR_MULTICURVES).getAmount(EUR);
    final PresentValueSABRSensitivityDataBundle pvsCapLong = METHOD_EXTRAPOLATION_CAP.presentValueSABRSensitivity(CMS_CAP_LONG, SABR_MULTICURVES);
    // SABR sensitivity vs finite difference
    final double shift = 0.0001;
    final double shiftAlpha = 0.00001;
    final double maturity = CMS_CAP_LONG.getUnderlyingSwap().getFixedLeg().getNthPayment(CMS_CAP_LONG.getUnderlyingSwap().getFixedLeg().getNumberOfPayments() - 1).getPaymentTime()
        - CMS_CAP_LONG.getSettlementTime();
    final DoublesPair expectedExpiryTenor = DoublesPair.of(CMS_CAP_LONG.getFixingTime(), maturity);
    // Alpha sensitivity vs finite difference computation
    final SABRInterestRateParameters sabrParameterAlphaBumped = SABRDataSets.createSABR1AlphaBumped(shiftAlpha);
    final SABRSwaptionProviderDiscount sabrBundleAlphaBumped = new SABRSwaptionProviderDiscount(MULTICURVES, sabrParameterAlphaBumped, EUR1YEURIBOR6M);
    final double pvLongPayerAlphaBumped = METHOD_EXTRAPOLATION_CAP.presentValue(CMS_CAP_LONG, sabrBundleAlphaBumped).getAmount(EUR);
    final double expectedAlphaSensi = (pvLongPayerAlphaBumped - pv) / shiftAlpha;
    assertEquals("Number of alpha sensitivity", pvsCapLong.getAlpha().getMap().keySet().size(), 1);
    assertEquals("Alpha sensitivity expiry/tenor", pvsCapLong.getAlpha().getMap().keySet().contains(expectedExpiryTenor), true);
    assertEquals("Alpha sensitivity value", expectedAlphaSensi, pvsCapLong.getAlpha().getMap().get(expectedExpiryTenor), TOLERANCE_PV_DELTA);
    // Rho sensitivity vs finite difference computation
    final SABRInterestRateParameters sabrParameterRhoBumped = SABRDataSets.createSABR1RhoBumped();
    final SABRSwaptionProviderDiscount sabrBundleRhoBumped = new SABRSwaptionProviderDiscount(MULTICURVES, sabrParameterRhoBumped, EUR1YEURIBOR6M);
    final double pvLongPayerRhoBumped = METHOD_EXTRAPOLATION_CAP.presentValue(CMS_CAP_LONG, sabrBundleRhoBumped).getAmount(EUR);
    final double expectedRhoSensi = (pvLongPayerRhoBumped - pv) / shift;
    assertEquals("Number of rho sensitivity", pvsCapLong.getRho().getMap().keySet().size(), 1);
    assertEquals("Rho sensitivity expiry/tenor", pvsCapLong.getRho().getMap().keySet().contains(expectedExpiryTenor), true);
    assertEquals("Rho sensitivity value", expectedRhoSensi, pvsCapLong.getRho().getMap().get(expectedExpiryTenor), TOLERANCE_PV_DELTA);
    // Alpha sensitivity vs finite difference computation
    final SABRInterestRateParameters sabrParameterNuBumped = SABRDataSets.createSABR1NuBumped();
    final SABRSwaptionProviderDiscount sabrBundleNuBumped = new SABRSwaptionProviderDiscount(MULTICURVES, sabrParameterNuBumped, EUR1YEURIBOR6M);
    final double pvLongPayerNuBumped = METHOD_EXTRAPOLATION_CAP.presentValue(CMS_CAP_LONG, sabrBundleNuBumped).getAmount(EUR);
    final double expectedNuSensi = (pvLongPayerNuBumped - pv) / shift;
    assertEquals("Number of nu sensitivity", pvsCapLong.getNu().getMap().keySet().size(), 1);
    assertTrue("Nu sensitivity expiry/tenor", pvsCapLong.getNu().getMap().keySet().contains(expectedExpiryTenor));
    assertEquals("Nu sensitivity value", expectedNuSensi, pvsCapLong.getNu().getMap().get(expectedExpiryTenor), TOLERANCE_PV_DELTA);
  }

  @Test
  /**
   * Tests the coupon present value SABR parameters sensitivity vs finite difference.
   */
  public void presentValueSABRSensitivityCoupon() {
    final double pv = METHOD_EXTRAPOLATION_CPN.presentValue(CMS_COUPON, SABR_MULTICURVES).getAmount(EUR);
    final PresentValueSABRSensitivityDataBundle pvsCpn = METHOD_EXTRAPOLATION_CPN.presentValueSABRSensitivity(CMS_COUPON, SABR_MULTICURVES);
    // SABR sensitivity vs finite difference
    final double shift = 0.0001;
    final double shiftAlpha = 0.00001;
    final double maturity = CMS_COUPON.getUnderlyingSwap().getFixedLeg().getNthPayment(CMS_COUPON.getUnderlyingSwap().getFixedLeg().getNumberOfPayments() - 1).getPaymentTime()
        - CMS_COUPON.getSettlementTime();
    final DoublesPair expectedExpiryTenor = DoublesPair.of(CMS_COUPON.getFixingTime(), maturity);
    // Alpha sensitivity vs finite difference computation
    final SABRInterestRateParameters sabrParameterAlphaBumped = SABRDataSets.createSABR1AlphaBumped(shiftAlpha);
    final SABRSwaptionProviderDiscount sabrBundleAlphaBumped = new SABRSwaptionProviderDiscount(MULTICURVES, sabrParameterAlphaBumped, EUR1YEURIBOR6M);
    final double pvLongPayerAlphaBumped = METHOD_EXTRAPOLATION_CPN.presentValue(CMS_COUPON, sabrBundleAlphaBumped).getAmount(EUR);
    final double expectedAlphaSensi = (pvLongPayerAlphaBumped - pv) / shiftAlpha;
    assertEquals("Number of alpha sensitivity", pvsCpn.getAlpha().getMap().keySet().size(), 1);
    assertEquals("Alpha sensitivity expiry/tenor", pvsCpn.getAlpha().getMap().keySet().contains(expectedExpiryTenor), true);
    assertEquals("Alpha sensitivity value", expectedAlphaSensi, pvsCpn.getAlpha().getMap().get(expectedExpiryTenor), TOLERANCE_PV_DELTA);
    // Rho sensitivity vs finite difference computation
    final SABRInterestRateParameters sabrParameterRhoBumped = SABRDataSets.createSABR1RhoBumped();
    final SABRSwaptionProviderDiscount sabrBundleRhoBumped = new SABRSwaptionProviderDiscount(MULTICURVES, sabrParameterRhoBumped, EUR1YEURIBOR6M);
    final double pvLongPayerRhoBumped = METHOD_EXTRAPOLATION_CPN.presentValue(CMS_COUPON, sabrBundleRhoBumped).getAmount(EUR);
    final double expectedRhoSensi = (pvLongPayerRhoBumped - pv) / shift;
    assertEquals("Number of rho sensitivity", pvsCpn.getRho().getMap().keySet().size(), 1);
    assertEquals("Rho sensitivity expiry/tenor", pvsCpn.getRho().getMap().keySet().contains(expectedExpiryTenor), true);
    assertEquals("Rho sensitivity value", expectedRhoSensi, pvsCpn.getRho().getMap().get(expectedExpiryTenor), TOLERANCE_PV_DELTA);
    // Nu sensitivity vs finite difference computation
    final SABRInterestRateParameters sabrParameterNuBumped = SABRDataSets.createSABR1NuBumped();
    final SABRSwaptionProviderDiscount sabrBundleNuBumped = new SABRSwaptionProviderDiscount(MULTICURVES, sabrParameterNuBumped, EUR1YEURIBOR6M);
    final double pvLongPayerNuBumped = METHOD_EXTRAPOLATION_CPN.presentValue(CMS_COUPON, sabrBundleNuBumped).getAmount(EUR);
    final double expectedNuSensi = (pvLongPayerNuBumped - pv) / shift;
    assertEquals("Number of nu sensitivity", pvsCpn.getNu().getMap().keySet().size(), 1);
    assertTrue("Nu sensitivity expiry/tenor", pvsCpn.getNu().getMap().keySet().contains(expectedExpiryTenor));
    assertEquals("Nu sensitivity value", expectedNuSensi, pvsCpn.getNu().getMap().get(expectedExpiryTenor), TOLERANCE_PV_DELTA);
  }

  @Test
  /**
   * Tests the present value SABR parameters sensitivity: Method vs Calculator.
   */
  public void presentValueSABRSensitivityMethodVsCalculator() {
    final PresentValueSABRSensitivityDataBundle pvssMethod = METHOD_EXTRAPOLATION_CAP.presentValueSABRSensitivity(CMS_CAP_LONG, SABR_MULTICURVES);
    final PresentValueSABRSensitivityDataBundle pvssCalculator = CMS_CAP_LONG.accept(PVSSSSXC, SABR_MULTICURVES);
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
      final CapFloorCMSDefinition cmsCapDefinition = CapFloorCMSDefinition.from(CMS_COUPON_RECEIVER_DEFINITION, strikes[loopstrike], IS_CAP);
      final CapFloorCMSDefinition cmsCapShiftUpDefinition = CapFloorCMSDefinition.from(CMS_COUPON_RECEIVER_DEFINITION, strikes[loopstrike] + shift, IS_CAP);
      final CapFloorCMSDefinition cmsCapShiftDoDefinition = CapFloorCMSDefinition.from(CMS_COUPON_RECEIVER_DEFINITION, strikes[loopstrike] - shift, IS_CAP);
      final CapFloorCMS cmsCap = (CapFloorCMS) cmsCapDefinition.toDerivative(REFERENCE_DATE);
      final CapFloorCMS cmsCapShiftUp = (CapFloorCMS) cmsCapShiftUpDefinition.toDerivative(REFERENCE_DATE);
      final CapFloorCMS cmsCapShiftDo = (CapFloorCMS) cmsCapShiftDoDefinition.toDerivative(REFERENCE_DATE);
      final double pvShiftUp = METHOD_EXTRAPOLATION_CAP.presentValue(cmsCapShiftUp, SABR_MULTICURVES).getAmount(EUR);
      final double pvShiftDo = METHOD_EXTRAPOLATION_CAP.presentValue(cmsCapShiftDo, SABR_MULTICURVES).getAmount(EUR);
      final double sensiExpected = (pvShiftUp - pvShiftDo) / (2 * shift);
      final double sensiComputed = METHOD_EXTRAPOLATION_CAP.presentValueStrikeSensitivity(cmsCap, SABR_MULTICURVES);
      errorRelative[loopstrike] = (sensiExpected - sensiComputed) / sensiExpected;
      assertEquals("CMS cap/floor SABR: Present value strike sensitivity " + loopstrike, 0, errorRelative[loopstrike], 5.0E-3);
    }
  }

  @Test(enabled = true)
  /**
   * Tests to estimate the impact of mu on the CMS coupon pricing. "enabled = false" for the standard testing.
   */
  public void testPriceMultiMu() {
    final double[] mu = new double[] {1.10, 1.30, 1.55, 2.25, 3.50, 6.00, 15.0};
    final int nbMu = mu.length;
    final double priceCouponStd = METHOD_STANDARD_CPN.presentValue(CMS_COUPON, SABR_MULTICURVES).getAmount(EUR);
    @SuppressWarnings("unused")
    final double rateCouponStd = priceCouponStd / (CMS_COUPON.getPaymentYearFraction() * CMS_COUPON.getNotional() * MULTICURVES.getDiscountFactor(EUR, CMS_COUPON.getPaymentTime()));
    final double[] priceCouponExtra = new double[nbMu];
    final double[] rateCouponExtra = new double[nbMu];
    for (int loopmu = 0; loopmu < nbMu; loopmu++) {
      final CouponCMSSABRExtrapolationRightReplicationMethod methodExtrapolation = new CouponCMSSABRExtrapolationRightReplicationMethod(CUT_OFF_STRIKE, mu[loopmu]);
      priceCouponExtra[loopmu] = methodExtrapolation.presentValue(CMS_COUPON, SABR_MULTICURVES).getAmount(EUR);
      rateCouponExtra[loopmu] = priceCouponExtra[loopmu] / (CMS_COUPON.getPaymentYearFraction() * CMS_COUPON.getNotional() * MULTICURVES.getDiscountFactor(EUR, CMS_COUPON.getPaymentTime()));
    }
    final double priceCouponNoAdj = METHOD_DSC_CPN.presentValue(CMS_COUPON, MULTICURVES).getAmount(EUR);
    final double rateCouponNoAdj = priceCouponNoAdj / (CMS_COUPON.getPaymentYearFraction() * CMS_COUPON.getNotional() * MULTICURVES.getDiscountFactor(EUR, CMS_COUPON.getPaymentTime()));
    for (int loopmu = 1; loopmu < nbMu; loopmu++) {
      assertTrue("Extrapolation: comparison with standard method", rateCouponExtra[loopmu - 1] > rateCouponExtra[loopmu]);
    }
    assertTrue("Extrapolation: comparison with standard method", rateCouponExtra[nbMu - 1] > rateCouponNoAdj);
  }

  @Test(enabled = false)
  /**
   * Tests of performance. "enabled = false" for the standard testing.
   */
  public void performanceCoupon() {
    long startTime, endTime;
    final int nbTest = 1000;

    startTime = System.currentTimeMillis();
    for (int looptest = 0; looptest < nbTest; looptest++) {
      METHOD_STANDARD_CPN.presentValue(CMS_COUPON, SABR_MULTICURVES);
      METHOD_STANDARD_CPN.presentValueCurveSensitivity(CMS_COUPON, SABR_MULTICURVES);
      METHOD_STANDARD_CPN.presentValueSABRSensitivity(CMS_COUPON, SABR_MULTICURVES);
    }
    endTime = System.currentTimeMillis();
    System.out.println(nbTest + " CMS coupon by replication SABR standard (price+delta+vega): " + (endTime - startTime) + " ms");

    startTime = System.currentTimeMillis();
    for (int looptest = 0; looptest < nbTest; looptest++) {
      METHOD_EXTRAPOLATION_CPN.presentValue(CMS_COUPON, SABR_MULTICURVES);
      METHOD_EXTRAPOLATION_CPN.presentValueCurveSensitivity(CMS_COUPON, SABR_MULTICURVES);
      METHOD_EXTRAPOLATION_CPN.presentValueSABRSensitivity(CMS_COUPON, SABR_MULTICURVES);
    }
    endTime = System.currentTimeMillis();
    System.out.println(nbTest + " CMS coupon by replication SABR with extrapolation (price+delta+vega): " + (endTime - startTime) + " ms");
    // Performance note: price (standard SABR): 15-Nov-11: On Mac Pro 3.2 GHz Quad-Core Intel Xeon: 55 ms for 1000 CMS coupon 5Y.
    // Performance note: price (SABR with extrapolation): 15-Nov-11: On Mac Pro 3.2 GHz Quad-Core Intel Xeon: 80 ms for 1000 CMS coupon 5Y.
    // Performance note: price+delta (standard SABR): 15-Nov-11: On Mac Pro 3.2 GHz Quad-Core Intel Xeon: 215 ms for 1000 CMS coupon 5Y.
    // Performance note: price+delta (SABR with extrapolation): 15-Nov-11: On Mac Pro 3.2 GHz Quad-Core Intel Xeon: 455 ms for 1000 CMS coupon 5Y.
    // Performance note: price+delta+vega (standard SABR): 18-Apr-2012: On Mac Pro 3.2 GHz Quad-Core Intel Xeon: 710 ms for 1000 CMS coupon 5Y.
    // Performance note: price+delta+vega (SABR with extrapolation): 18-Apr-2012: On Mac Pro 3.2 GHz Quad-Core Intel Xeon: 690 ms for 1000 CMS coupon 5Y.

  }

  @Test(enabled = false)
  /**
   * Tests of performance. "enabled = false" for the standard testing.
   */
  public void performanceCap() {
    long startTime, endTime;
    final int nbTest = 1000;

    startTime = System.currentTimeMillis();
    for (int looptest = 0; looptest < nbTest; looptest++) {
      METHOD_STANDARD_CAP.presentValue(CMS_CAP_LONG, SABR_MULTICURVES);
      METHOD_STANDARD_CAP.presentValueCurveSensitivity(CMS_CAP_LONG, SABR_MULTICURVES);
      METHOD_STANDARD_CAP.presentValueSABRSensitivity(CMS_CAP_LONG, SABR_MULTICURVES);
    }
    endTime = System.currentTimeMillis();
    System.out.println(nbTest + " CMS cap by replication SABR standard (price+delta+vega): " + (endTime - startTime) + " ms");

    startTime = System.currentTimeMillis();
    for (int looptest = 0; looptest < nbTest; looptest++) {
      METHOD_EXTRAPOLATION_CAP.presentValue(CMS_CAP_LONG, SABR_MULTICURVES);
      METHOD_EXTRAPOLATION_CAP.presentValueCurveSensitivity(CMS_CAP_LONG, SABR_MULTICURVES);
      METHOD_EXTRAPOLATION_CAP.presentValueSABRSensitivity(CMS_CAP_LONG, SABR_MULTICURVES);
    }
    endTime = System.currentTimeMillis();
    System.out.println(nbTest + " CMS cap by replication SABR with extrapolation (price+delta+vega): " + (endTime - startTime) + " ms");
    // Performance note: price+delta+vega (standard SABR): 28-Nov-2012: On Mac Pro 3.2 GHz Quad-Core Intel Xeon: 675 ms for 1000 CMS cap 5Y.
    // Performance note: price+delta+vega (SABR with extrapolation): 28-Nov-2012: On Mac Pro 3.2 GHz Quad-Core Intel Xeon: 515 ms for 1000 CMS cap 5Y.

  }

}
