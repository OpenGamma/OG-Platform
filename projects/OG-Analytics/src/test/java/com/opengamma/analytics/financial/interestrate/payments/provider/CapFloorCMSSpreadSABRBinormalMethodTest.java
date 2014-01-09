/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate.payments.provider;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertTrue;

import org.testng.annotations.Test;
import org.threeten.bp.LocalDateTime;
import org.threeten.bp.LocalTime;
import org.threeten.bp.Period;
import org.threeten.bp.ZoneOffset;
import org.threeten.bp.ZonedDateTime;

import com.opengamma.analytics.financial.instrument.index.GeneratorSwapFixedIbor;
import com.opengamma.analytics.financial.instrument.index.GeneratorSwapFixedIborMaster;
import com.opengamma.analytics.financial.instrument.index.IborIndex;
import com.opengamma.analytics.financial.instrument.index.IndexSwap;
import com.opengamma.analytics.financial.instrument.swap.SwapFixedIborDefinition;
import com.opengamma.analytics.financial.interestrate.PresentValueSABRSensitivityDataBundle;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CapFloorCMS;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CapFloorCMSSpread;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CouponCMS;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CouponFixed;
import com.opengamma.analytics.financial.interestrate.payments.derivative.Payment;
import com.opengamma.analytics.financial.interestrate.swap.derivative.SwapFixedCoupon;
import com.opengamma.analytics.financial.model.option.definition.SABRInterestRateCorrelationParameters;
import com.opengamma.analytics.financial.model.option.definition.SABRInterestRateParameters;
import com.opengamma.analytics.financial.model.option.pricing.analytic.formula.EuropeanVanillaOption;
import com.opengamma.analytics.financial.model.option.pricing.analytic.formula.NormalFunctionData;
import com.opengamma.analytics.financial.model.option.pricing.analytic.formula.NormalPriceFunction;
import com.opengamma.analytics.financial.model.volatility.NormalImpliedVolatilityFormula;
import com.opengamma.analytics.financial.provider.calculator.discounting.ParRateDiscountingCalculator;
import com.opengamma.analytics.financial.provider.calculator.discounting.PresentValueDiscountingCalculator;
import com.opengamma.analytics.financial.provider.calculator.sabrswaption.PresentValueCurveSensitivitySABRSwaptionCalculator;
import com.opengamma.analytics.financial.provider.calculator.sabrswaption.PresentValueCurveSensitivitySABRSwaptionRightExtrapolationCalculator;
import com.opengamma.analytics.financial.provider.calculator.sabrswaption.PresentValueSABRSensitivitySABRSwaptionCalculator;
import com.opengamma.analytics.financial.provider.calculator.sabrswaption.PresentValueSABRSensitivitySABRSwaptionRightExtrapolationCalculator;
import com.opengamma.analytics.financial.provider.calculator.sabrswaption.PresentValueSABRSwaptionCalculator;
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
import com.opengamma.analytics.math.function.DoubleFunction1D;
import com.opengamma.analytics.math.function.Function1D;
import com.opengamma.analytics.math.function.RealPolynomialFunction1D;
import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.financial.convention.daycount.DayCount;
import com.opengamma.financial.convention.daycount.DayCounts;
import com.opengamma.util.money.Currency;
import com.opengamma.util.money.MultipleCurrencyAmount;
import com.opengamma.util.test.TestGroup;
import com.opengamma.util.time.DateUtils;
import com.opengamma.util.tuple.DoublesPair;

/**
 * Tests the pricing of CMS spread option in binormal with correlation by strike approach.
 */
@Test(groups = TestGroup.UNIT)
public class CapFloorCMSSpreadSABRBinormalMethodTest {

  private static final MulticurveProviderDiscount MULTICURVES = MulticurveProviderDiscountDataSets.createMulticurveEurUsd();
  private static final IborIndex EURIBOR6M = MulticurveProviderDiscountDataSets.getIndexesIborMulticurveEurUsd()[1];
  private static final Currency EUR = EURIBOR6M.getCurrency();
  private static final Calendar CALENDAR = MulticurveProviderDiscountDataSets.getEURCalendar();

  private static final SABRInterestRateParameters SABR_PARAMETER = SABRDataSets.createSABR1();
  private static final GeneratorSwapFixedIbor EUR1YEURIBOR6M = GeneratorSwapFixedIborMaster.getInstance().getGenerator("EUR1YEURIBOR6M", CALENDAR);
  private static final double CORRELATION = 0.80;
  private static final DoubleFunction1D CORRELATION_FUNCTION = new RealPolynomialFunction1D(new double[] {CORRELATION}); // Constant function
  private static final SABRInterestRateCorrelationParameters SABR_CORRELATION = SABRInterestRateCorrelationParameters.from(SABR_PARAMETER, CORRELATION_FUNCTION);
  private static final SABRSwaptionProviderDiscount SABR_MULTICURVES = new SABRSwaptionProviderDiscount(MULTICURVES, SABR_CORRELATION, EUR1YEURIBOR6M);

  private static final ZonedDateTime REFERENCE_DATE = DateUtils.getUTCDate(2008, 8, 18);

  //Swaps
  private static final Period FIXED_PAYMENT_PERIOD = Period.ofMonths(6);
  private static final DayCount FIXED_DAY_COUNT = DayCounts.THIRTY_U_360;
  private static final boolean FIXED_IS_PAYER = true; // Irrelevant for the underlying
  private static final double RATE = 0.0; // Irrelevant for the underlying
  private static final ZonedDateTime FIXING_DATE = DateUtils.getUTCDate(2010, 12, 30);
  private static final ZonedDateTime SETTLEMENT_DATE = ScheduleCalculator.getAdjustedDate(FIXING_DATE, EURIBOR6M.getSpotLag(), CALENDAR);
  // Swap 10Y
  private static final Period ANNUITY_TENOR_1 = Period.ofYears(10);
  private static final IndexSwap CMS_INDEX_1 = new IndexSwap(FIXED_PAYMENT_PERIOD, FIXED_DAY_COUNT, EURIBOR6M, ANNUITY_TENOR_1, CALENDAR);
  private static final SwapFixedIborDefinition SWAP_DEFINITION_1 = SwapFixedIborDefinition.from(SETTLEMENT_DATE, CMS_INDEX_1, 1.0, RATE, FIXED_IS_PAYER, CALENDAR);
  // Swap 2Y
  private static final Period ANNUITY_TENOR_2 = Period.ofYears(2);
  private static final IndexSwap CMS_INDEX_2 = new IndexSwap(FIXED_PAYMENT_PERIOD, FIXED_DAY_COUNT, EURIBOR6M, ANNUITY_TENOR_2, CALENDAR);
  private static final SwapFixedIborDefinition SWAP_DEFINITION_2 = SwapFixedIborDefinition.from(SETTLEMENT_DATE, CMS_INDEX_2, 1.0, RATE, FIXED_IS_PAYER, CALENDAR);
  // CMS spread coupon
  private static final double NOTIONAL = 100000000; // 100m
  private static final ZonedDateTime PAYMENT_DATE = DateUtils.getUTCDate(2011, 4, 6);
  private static final ZonedDateTime ACCRUAL_START_DATE = DateUtils.getUTCDate(2011, 1, 5);
  private static final ZonedDateTime ACCRUAL_END_DATE = DateUtils.getUTCDate(2011, 4, 5);
  private static final DayCount PAYMENT_DAY_COUNT = DayCounts.ACT_360;
  private static final double PAYMENT_ACCRUAL_FACTOR = PAYMENT_DAY_COUNT.getDayCountFraction(ACCRUAL_START_DATE, ACCRUAL_END_DATE);
  private static final double STRIKE = 0.0010; // 10 bps
  private static final boolean IS_CAP = true;
  // to derivatives
  private static final SwapFixedCoupon<? extends Payment> SWAP_1 = SWAP_DEFINITION_1.toDerivative(REFERENCE_DATE);
  private static final SwapFixedCoupon<? extends Payment> SWAP_2 = SWAP_DEFINITION_2.toDerivative(REFERENCE_DATE);
  private static final DayCount ACT_ACT = DayCounts.ACT_ACT_ISDA;
  private static final ZonedDateTime REFERENCE_DATE_ZONED = ZonedDateTime.of(LocalDateTime.of(REFERENCE_DATE.toLocalDate(), LocalTime.of(0, 0)), ZoneOffset.UTC);
  private static final double PAYMENT_TIME = ACT_ACT.getDayCountFraction(REFERENCE_DATE_ZONED, PAYMENT_DATE);
  private static final double FIXING_TIME = ACT_ACT.getDayCountFraction(REFERENCE_DATE_ZONED, FIXING_DATE);
  private static final double SETTLEMENT_TIME = ACT_ACT.getDayCountFraction(REFERENCE_DATE_ZONED, SWAP_DEFINITION_1.getFixedLeg().getNthPayment(0).getAccrualStartDate());

  private static final CapFloorCMSSpread CMS_CAP_SPREAD = new CapFloorCMSSpread(EUR, PAYMENT_TIME, PAYMENT_ACCRUAL_FACTOR, NOTIONAL, FIXING_TIME, SWAP_1, CMS_INDEX_1, SWAP_2, CMS_INDEX_2,
      SETTLEMENT_TIME, STRIKE, IS_CAP);
  private static final CapFloorCMSSpread CMS_FLOOR_SPREAD = new CapFloorCMSSpread(EUR, PAYMENT_TIME, PAYMENT_ACCRUAL_FACTOR, NOTIONAL, FIXING_TIME, SWAP_1, CMS_INDEX_1, SWAP_2, CMS_INDEX_2,
      SETTLEMENT_TIME, STRIKE, !IS_CAP);

  private static final CouponCMSSABRReplicationMethod METHOD_CMS_COUPON = CouponCMSSABRReplicationMethod.getInstance();
  private static final CapFloorCMSSABRReplicationMethod METHOD_CMS_CAP = CapFloorCMSSABRReplicationMethod.getDefaultInstance();
  private static final CapFloorCMSSpreadSABRBinormalMethod METHOD_CMS_SPREAD = new CapFloorCMSSpreadSABRBinormalMethod(CORRELATION_FUNCTION, METHOD_CMS_CAP, METHOD_CMS_COUPON);
  private static final CouponFixedDiscountingMethod METHOD_CPN_FIXED = CouponFixedDiscountingMethod.getInstance();

  private static final PresentValueSABRSwaptionCalculator PVSSC = PresentValueSABRSwaptionCalculator.getInstance();
  private static final PresentValueDiscountingCalculator PVDC = PresentValueDiscountingCalculator.getInstance();
  private static final PresentValueCurveSensitivitySABRSwaptionCalculator PVCSSSC = PresentValueCurveSensitivitySABRSwaptionCalculator.getInstance();
  private static final PresentValueSABRSensitivitySABRSwaptionCalculator PVSSSSC = PresentValueSABRSensitivitySABRSwaptionCalculator.getInstance();

  private static final ParRateDiscountingCalculator PRDC = ParRateDiscountingCalculator.getInstance();

  private static final double CUT_OFF_STRIKE = 0.10;
  private static final double MU = 2.50;
  private static final PresentValueSABRSwaptionRightExtrapolationCalculator PVSSXC = new PresentValueSABRSwaptionRightExtrapolationCalculator(CUT_OFF_STRIKE, MU);
  private static final PresentValueCurveSensitivitySABRSwaptionRightExtrapolationCalculator PVCSSSXC = new PresentValueCurveSensitivitySABRSwaptionRightExtrapolationCalculator(CUT_OFF_STRIKE, MU);
  private static final PresentValueSABRSensitivitySABRSwaptionRightExtrapolationCalculator PVSSSSXC = new PresentValueSABRSensitivitySABRSwaptionRightExtrapolationCalculator(CUT_OFF_STRIKE, MU);

  private static final double SHIFT = 1.0E-7;
  private static final ParameterSensitivityParameterCalculator<SABRSwaptionProviderInterface> PS_SS_C = new ParameterSensitivityParameterCalculator<>(PVCSSSC);
  private static final ParameterSensitivitySABRSwaptionDiscountInterpolatedFDCalculator PS_SS_FDC = new ParameterSensitivitySABRSwaptionDiscountInterpolatedFDCalculator(PVSSC, SHIFT);
  private static final ParameterSensitivityParameterCalculator<SABRSwaptionProviderInterface> PS_SSX_C = new ParameterSensitivityParameterCalculator<>(PVCSSSXC);
  private static final ParameterSensitivitySABRSwaptionDiscountInterpolatedFDCalculator PS_SSX_FDC = new ParameterSensitivitySABRSwaptionDiscountInterpolatedFDCalculator(PVSSXC, SHIFT);

  private static final CapFloorCMSSABRExtrapolationRightReplicationMethod METHOD_CMS_CAP_EXTRAPOLATION = new CapFloorCMSSABRExtrapolationRightReplicationMethod(CUT_OFF_STRIKE, MU);
  private static final CouponCMSSABRExtrapolationRightReplicationMethod METHOD_CMS_COUPON_EXTRAPOLATION = new CouponCMSSABRExtrapolationRightReplicationMethod(CUT_OFF_STRIKE, MU);
  private static final CapFloorCMSSpreadSABRBinormalMethod METHOD_CMS_SPREAD_EXTRAPOLATION = new CapFloorCMSSpreadSABRBinormalMethod(CORRELATION_FUNCTION, METHOD_CMS_CAP_EXTRAPOLATION,
      METHOD_CMS_COUPON_EXTRAPOLATION);

  private static final double TOLERANCE_PV = 1.0E-2; // 0.01 currency unit for 100m notional.
  private static final double TOLERANCE_PV_DELTA = 5.0E+3; // 0.50 for 1 bp on 100m

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNotNullCorrelation() {
    new CapFloorCMSSpreadSABRBinormalMethod(null, METHOD_CMS_CAP, METHOD_CMS_COUPON);
  }

  @Test
  public void getter() {
    final double correlation = 0.80;
    final DoubleFunction1D correlationFunction = new RealPolynomialFunction1D(new double[] {correlation}); // Constant function
    final CapFloorCMSSpreadSABRBinormalMethod method = new CapFloorCMSSpreadSABRBinormalMethod(correlationFunction, METHOD_CMS_CAP, METHOD_CMS_COUPON);
    assertEquals("CMS spread binormal method: correlation function getter", correlationFunction, method.getCorrelation());
  }

  @Test
  /**
   * Tests the present value against the price explicitly computed for constant correlation.
   */
  public void presentValue() {
    final double correlation = 0.80;
    final DoubleFunction1D correlationFunction = new RealPolynomialFunction1D(new double[] {correlation}); // Constant function
    final CapFloorCMSSpreadSABRBinormalMethod method = new CapFloorCMSSpreadSABRBinormalMethod(correlationFunction, METHOD_CMS_CAP, METHOD_CMS_COUPON);
    final double cmsSpreadPrice = method.presentValue(CMS_CAP_SPREAD, SABR_MULTICURVES).getAmount(EUR);
    final double discountFactorPayment = MULTICURVES.getDiscountFactor(EUR, PAYMENT_TIME);
    final CouponCMSSABRReplicationMethod methodCms = CouponCMSSABRReplicationMethod.getInstance();
    final CapFloorCMSSABRReplicationMethod methodCmsCap = CapFloorCMSSABRReplicationMethod.getDefaultInstance();
    final NormalImpliedVolatilityFormula impliedVolatility = new NormalImpliedVolatilityFormula();
    final NormalPriceFunction normalPrice = new NormalPriceFunction();
    final CouponCMS cmsCoupon1 = CouponCMS.from(CMS_CAP_SPREAD, SWAP_1, SETTLEMENT_TIME);
    final CouponCMS cmsCoupon2 = CouponCMS.from(CMS_CAP_SPREAD, SWAP_2, SETTLEMENT_TIME);
    final double cmsCoupon1Price = methodCms.presentValue(cmsCoupon1, SABR_MULTICURVES).getAmount(EUR);
    final double cmsCoupon2Price = methodCms.presentValue(cmsCoupon2, SABR_MULTICURVES).getAmount(EUR);
    final double expectedRate1 = cmsCoupon1Price / discountFactorPayment / cmsCoupon1.getNotional() / cmsCoupon1.getPaymentYearFraction();
    final double expectedRate2 = cmsCoupon2Price / discountFactorPayment / cmsCoupon2.getNotional() / cmsCoupon2.getPaymentYearFraction();
    final double forward1 = SWAP_1.accept(PRDC, MULTICURVES);
    final double forward2 = SWAP_2.accept(PRDC, MULTICURVES);
    final CapFloorCMS cmsCap1 = CapFloorCMS.from(cmsCoupon1, forward1, true);
    final CapFloorCMS cmsCap2 = CapFloorCMS.from(cmsCoupon2, forward2, true);
    final double cmsCap1Price = methodCmsCap.presentValue(cmsCap1, SABR_MULTICURVES).getAmount(EUR);
    final double cmsCap2Price = methodCmsCap.presentValue(cmsCap2, SABR_MULTICURVES).getAmount(EUR);
    final EuropeanVanillaOption optionCap1 = new EuropeanVanillaOption(forward1, FIXING_TIME, true);
    final NormalFunctionData dataCap1 = new NormalFunctionData(expectedRate1, 1.0, 0.0);
    final double cmsCap1IV = impliedVolatility.getImpliedVolatility(dataCap1, optionCap1, cmsCap1Price / discountFactorPayment / cmsCoupon1.getNotional() / cmsCoupon1.getPaymentYearFraction());
    final EuropeanVanillaOption optionCap2 = new EuropeanVanillaOption(forward2, FIXING_TIME, true);
    final NormalFunctionData dataCap2 = new NormalFunctionData(expectedRate2, 1.0, 0.0);
    final double cmsCap2IV = impliedVolatility.getImpliedVolatility(dataCap2, optionCap2, cmsCap2Price / discountFactorPayment / cmsCoupon2.getNotional() / cmsCoupon2.getPaymentYearFraction());
    double spreadVol = cmsCap1IV * cmsCap1IV - 2 * correlation * cmsCap1IV * cmsCap2IV + cmsCap2IV * cmsCap2IV;
    spreadVol = Math.sqrt(spreadVol);
    final EuropeanVanillaOption optionSpread = new EuropeanVanillaOption(STRIKE, FIXING_TIME, IS_CAP);
    final NormalFunctionData dataSpread = new NormalFunctionData(expectedRate1 - expectedRate2, 1.0, spreadVol);
    final Function1D<NormalFunctionData, Double> priceFunction = normalPrice.getPriceFunction(optionSpread);
    final double cmsSpreadPriceExpected = discountFactorPayment * priceFunction.evaluate(dataSpread) * CMS_CAP_SPREAD.getNotional() * CMS_CAP_SPREAD.getPaymentYearFraction();
    assertEquals("CMS spread: price with constant correlation", cmsSpreadPriceExpected, cmsSpreadPrice, TOLERANCE_PV);
  }

  @Test
  /**
   * Tests the present value with default method and with method without extrapolation.
   */
  public void presentValueConstructor() {
    final CapFloorCMSSpreadSABRBinormalMethod methodDefault = new CapFloorCMSSpreadSABRBinormalMethod(CORRELATION_FUNCTION, METHOD_CMS_CAP, METHOD_CMS_COUPON);
    final MultipleCurrencyAmount pvDefault = methodDefault.presentValue(CMS_CAP_SPREAD, SABR_MULTICURVES);
    final CapFloorCMSSpreadSABRBinormalMethod methodNoExtrapolation = new CapFloorCMSSpreadSABRBinormalMethod(CORRELATION_FUNCTION, METHOD_CMS_CAP, METHOD_CMS_COUPON);
    final MultipleCurrencyAmount pvNoExtrapolation = methodNoExtrapolation.presentValue(CMS_CAP_SPREAD, SABR_MULTICURVES);
    assertEquals("CMS spread: price with constant correlation", pvDefault.getAmount(EUR), pvNoExtrapolation.getAmount(EUR), TOLERANCE_PV);
  }

  @Test
  /**
   * Tests the present value with right extrapolation against a hard-coded price.
   */
  public void presentValueRightExtrapolation() {
    final MultipleCurrencyAmount pvExtrapolation = METHOD_CMS_SPREAD_EXTRAPOLATION.presentValue(CMS_CAP_SPREAD, SABR_MULTICURVES);
    final double pvHardCoded = 71760.169; // From previous run
    assertEquals("CMS spread: price with constant correlation", pvHardCoded, pvExtrapolation.getAmount(EUR), TOLERANCE_PV);
  }

  @Test
  /**
   * Tests the present value long/short parity (cap and floor).
   */
  public void presentValueLongShortParity() {
    final CapFloorCMSSpread cmsCapSpreadShort = new CapFloorCMSSpread(EUR, PAYMENT_TIME, PAYMENT_ACCRUAL_FACTOR, -NOTIONAL, FIXING_TIME, SWAP_1, CMS_INDEX_1, SWAP_2, CMS_INDEX_2, SETTLEMENT_TIME,
        STRIKE, IS_CAP);
    final MultipleCurrencyAmount pvCapLong = METHOD_CMS_SPREAD.presentValue(CMS_CAP_SPREAD, SABR_MULTICURVES);
    final MultipleCurrencyAmount pvCapShort = METHOD_CMS_SPREAD.presentValue(cmsCapSpreadShort, SABR_MULTICURVES);
    assertEquals("CMS spread: Long/Short parity", pvCapLong.getAmount(EUR), -pvCapShort.getAmount(EUR), TOLERANCE_PV);
    final MultipleCurrencyAmount pvCapLongExtra = METHOD_CMS_SPREAD_EXTRAPOLATION.presentValue(CMS_CAP_SPREAD, SABR_MULTICURVES);
    final MultipleCurrencyAmount pvCapShortExtra = METHOD_CMS_SPREAD_EXTRAPOLATION.presentValue(cmsCapSpreadShort, SABR_MULTICURVES);
    assertEquals("CMS spread: Long/Short parity", pvCapLongExtra.getAmount(EUR), -pvCapShortExtra.getAmount(EUR), TOLERANCE_PV);

    final CapFloorCMSSpread cmsFloorSpreadShort = new CapFloorCMSSpread(EUR, PAYMENT_TIME, PAYMENT_ACCRUAL_FACTOR, -NOTIONAL, FIXING_TIME, SWAP_1, CMS_INDEX_1, SWAP_2, CMS_INDEX_2, SETTLEMENT_TIME,
        STRIKE, !IS_CAP);
    final MultipleCurrencyAmount pvFloorLong = METHOD_CMS_SPREAD.presentValue(CMS_FLOOR_SPREAD, SABR_MULTICURVES);
    final MultipleCurrencyAmount pvFloorShort = METHOD_CMS_SPREAD.presentValue(cmsFloorSpreadShort, SABR_MULTICURVES);
    assertEquals("CMS spread: Long/Short parity", pvFloorLong.getAmount(EUR), -pvFloorShort.getAmount(EUR), TOLERANCE_PV);
    final MultipleCurrencyAmount pvFloorLongExtra = METHOD_CMS_SPREAD_EXTRAPOLATION.presentValue(CMS_FLOOR_SPREAD, SABR_MULTICURVES);
    final MultipleCurrencyAmount pvFloorShortExtra = METHOD_CMS_SPREAD_EXTRAPOLATION.presentValue(cmsFloorSpreadShort, SABR_MULTICURVES);
    assertEquals("CMS spread: Long/Short parity", pvFloorLongExtra.getAmount(EUR), -pvFloorShortExtra.getAmount(EUR), TOLERANCE_PV);
  }

  @Test
  /**
   * Tests the present value cap/floor parity (Cap - Floor = (cms1 - cms2) - strike).
   */
  public void presentValueCapFloorParity() {
    final CouponCMS cms1 = new CouponCMS(EUR, PAYMENT_TIME, PAYMENT_ACCRUAL_FACTOR, NOTIONAL, FIXING_TIME, SWAP_1, SETTLEMENT_TIME);
    final CouponCMS cms2 = new CouponCMS(EUR, PAYMENT_TIME, PAYMENT_ACCRUAL_FACTOR, NOTIONAL, FIXING_TIME, SWAP_2, SETTLEMENT_TIME);
    final CouponFixed cpnStrike = new CouponFixed(EUR, PAYMENT_TIME, PAYMENT_ACCRUAL_FACTOR, NOTIONAL, STRIKE);
    // No extrapolation
    final MultipleCurrencyAmount pvCapLong = METHOD_CMS_SPREAD.presentValue(CMS_CAP_SPREAD, SABR_MULTICURVES);
    final MultipleCurrencyAmount pvFloorLong = METHOD_CMS_SPREAD.presentValue(CMS_FLOOR_SPREAD, SABR_MULTICURVES);
    final MultipleCurrencyAmount pvCMS1 = cms1.accept(PVSSC, SABR_MULTICURVES);
    final MultipleCurrencyAmount pvCMS2 = cms2.accept(PVSSC, SABR_MULTICURVES);
    final MultipleCurrencyAmount pvStrike = cpnStrike.accept(PVDC, MULTICURVES);
    assertEquals("CMS spread: Cap/Floor parity", pvCMS1.getAmount(EUR) - pvCMS2.getAmount(EUR) - pvStrike.getAmount(EUR), pvCapLong.getAmount(EUR) - pvFloorLong.getAmount(EUR), TOLERANCE_PV);
    // Extrapolation
    final MultipleCurrencyAmount pvCapLongExtra = METHOD_CMS_SPREAD_EXTRAPOLATION.presentValue(CMS_CAP_SPREAD, SABR_MULTICURVES);
    final MultipleCurrencyAmount pvFloorLongExtra = METHOD_CMS_SPREAD_EXTRAPOLATION.presentValue(CMS_FLOOR_SPREAD, SABR_MULTICURVES);
    final MultipleCurrencyAmount pvCMS1Extra = cms1.accept(PVSSXC, SABR_MULTICURVES);
    final MultipleCurrencyAmount pvCMS2Extra = cms2.accept(PVSSXC, SABR_MULTICURVES);
    assertEquals("CMS spread: Cap/Floor parity", pvCMS1Extra.getAmount(EUR) - pvCMS2Extra.getAmount(EUR) - pvStrike.getAmount(EUR), pvCapLongExtra.getAmount(EUR) - pvFloorLongExtra.getAmount(EUR),
        TOLERANCE_PV);
  }

  @Test
  /**
   * Tests the present value. Method vs Calculator.
   */
  public void presentValueMethodVsCalculator() {
    final MultipleCurrencyAmount pvMethod = METHOD_CMS_SPREAD.presentValue(CMS_CAP_SPREAD, SABR_MULTICURVES);
    final MultipleCurrencyAmount pvCalculator = CMS_CAP_SPREAD.accept(PVSSC, SABR_MULTICURVES);
    assertEquals("CMS spread: present value Method vs Calculator", pvMethod.getAmount(EUR), pvCalculator.getAmount(EUR), TOLERANCE_PV);
    // Extrapolation
    final MultipleCurrencyAmount pvExtraMethod = METHOD_CMS_SPREAD_EXTRAPOLATION.presentValue(CMS_CAP_SPREAD, SABR_MULTICURVES);
    final MultipleCurrencyAmount pvExtraCalculator = CMS_CAP_SPREAD.accept(PVSSXC, SABR_MULTICURVES);
    assertEquals("CMS spread: present value Method vs Calculator", pvExtraMethod.getAmount(EUR), pvExtraCalculator.getAmount(EUR), TOLERANCE_PV);
  }

  @Test
  /**
   * Tests the implied correlation computation for a range of correlations.
   */
  public void impliedCorrelation() {
    final double[] correlation = new double[] {-0.50, 0.00, 0.50, 0.75, 0.80, 0.85, 0.90, 0.95, 0.99};
    final int nbCor = correlation.length;
    final double[] impliedCorrelation = new double[nbCor];
    for (int loopcor = 0; loopcor < nbCor; loopcor++) {
      final DoubleFunction1D correlationFunction = new RealPolynomialFunction1D(new double[] {correlation[loopcor]}); // Constant function
      final CapFloorCMSSpreadSABRBinormalMethod method = new CapFloorCMSSpreadSABRBinormalMethod(correlationFunction, METHOD_CMS_CAP, METHOD_CMS_COUPON);
      final double cmsSpreadPrice = method.presentValue(CMS_CAP_SPREAD, SABR_MULTICURVES).getAmount(EUR);
      impliedCorrelation[loopcor] = method.impliedCorrelation(CMS_CAP_SPREAD, SABR_MULTICURVES, cmsSpreadPrice);
      assertEquals("CMS spread cap/floor: implied correlation", correlation[loopcor], impliedCorrelation[loopcor], 1.0E-10);
    }
  }

  @Test
  /**
   * Tests the price curve sensitivity of CMS coupon and cap/floor using replication in the SABR framework. Values are tested against finite difference values.
   */
  public void presentValueCurveSensitivityCap() {
    final MultipleCurrencyParameterSensitivity pvpsExact = PS_SS_C.calculateSensitivity(CMS_CAP_SPREAD, SABR_MULTICURVES, SABR_MULTICURVES.getMulticurveProvider().getAllNames());
    final MultipleCurrencyParameterSensitivity pvpsFD = PS_SS_FDC.calculateSensitivity(CMS_CAP_SPREAD, SABR_MULTICURVES);
    AssertSensivityObjects.assertEquals("CapFloorCMSSpreadSABRBinormalMethod: presentValueCurveSensitivity ", pvpsExact, pvpsFD, TOLERANCE_PV_DELTA);
  }

  @Test
  /**
   * Tests the price curve sensitivity of CMS coupon and cap/floor using replication in the SABR framework. Values are tested against finite difference values.
   */
  public void presentValueCurveSensitivityFloor() {
    final MultipleCurrencyParameterSensitivity pvpsExact = PS_SS_C.calculateSensitivity(CMS_FLOOR_SPREAD, SABR_MULTICURVES, SABR_MULTICURVES.getMulticurveProvider().getAllNames());
    final MultipleCurrencyParameterSensitivity pvpsFD = PS_SS_FDC.calculateSensitivity(CMS_FLOOR_SPREAD, SABR_MULTICURVES);
    AssertSensivityObjects.assertEquals("CapFloorCMSSpreadSABRBinormalMethod: presentValueCurveSensitivity ", pvpsExact, pvpsFD, TOLERANCE_PV_DELTA);
  }

  @Test
  /**
   * Tests the price curve sensitivity of CMS coupon and cap/floor using replication in the SABR framework. Values are tested against finite difference values.
   */
  public void presentValueCurveSensitivityCapExtrapolation() {
    final MultipleCurrencyParameterSensitivity pvpsExact = PS_SSX_C.calculateSensitivity(CMS_CAP_SPREAD, SABR_MULTICURVES, SABR_MULTICURVES.getMulticurveProvider().getAllNames());
    final MultipleCurrencyParameterSensitivity pvpsFD = PS_SSX_FDC.calculateSensitivity(CMS_CAP_SPREAD, SABR_MULTICURVES);
    AssertSensivityObjects.assertEquals("CapFloorCMSSpreadSABRBinormalMethod: presentValueCurveSensitivity ", pvpsExact, pvpsFD, TOLERANCE_PV_DELTA);
  }

  @Test
  /**
   * Tests the present value against the price explicitly computed for constant correlation.
   */
  public void presentValueCurveSensitivityMethodVsCalculator() {
    final MultipleCurrencyMulticurveSensitivity pvcsMethod = METHOD_CMS_SPREAD.presentValueCurveSensitivity(CMS_CAP_SPREAD, SABR_MULTICURVES);
    final MultipleCurrencyMulticurveSensitivity pvcsCalculator = CMS_CAP_SPREAD.accept(PVCSSSC, SABR_MULTICURVES);
    AssertSensivityObjects.assertEquals("CMS spread: curve sensitivity Method vs Calculator", pvcsMethod, pvcsCalculator, TOLERANCE_PV_DELTA);
    // Extrapolation
    final MultipleCurrencyMulticurveSensitivity pvcsMethodExtra = METHOD_CMS_SPREAD_EXTRAPOLATION.presentValueCurveSensitivity(CMS_CAP_SPREAD, SABR_MULTICURVES);
    final MultipleCurrencyMulticurveSensitivity pvcsCalculatorExtra = CMS_CAP_SPREAD.accept(PVCSSSXC, SABR_MULTICURVES);
    AssertSensivityObjects.assertEquals("CMS spread: curve sensitivity Method vs Calculator", pvcsMethodExtra, pvcsCalculatorExtra, TOLERANCE_PV_DELTA);
  }

  @Test
  /**
   * Tests the long/short parity for the present value curve sensitivity of a cap.
   */
  public void presentValueCurveSensitivityCapLongShortParity() {
    final CapFloorCMSSpread cmsCapSpreadShort = new CapFloorCMSSpread(EUR, PAYMENT_TIME, PAYMENT_ACCRUAL_FACTOR, -NOTIONAL, FIXING_TIME, SWAP_1, CMS_INDEX_1, SWAP_2, CMS_INDEX_2, SETTLEMENT_TIME,
        STRIKE, IS_CAP);
    MultipleCurrencyMulticurveSensitivity pvcsLong = METHOD_CMS_SPREAD.presentValueCurveSensitivity(CMS_CAP_SPREAD, SABR_MULTICURVES);
    pvcsLong = pvcsLong.cleaned();
    MultipleCurrencyMulticurveSensitivity pvcsShort = METHOD_CMS_SPREAD.presentValueCurveSensitivity(cmsCapSpreadShort, SABR_MULTICURVES);
    pvcsShort = pvcsShort.multipliedBy(-1);
    pvcsShort = pvcsShort.cleaned();
    AssertSensivityObjects.assertEquals("CMS cap spread: Long/Short parity", pvcsLong, pvcsShort, TOLERANCE_PV);
  }

  @Test
  /**
   * Tests the long/short parity for the present value curve sensitivity of a floor.
   */
  public void presentValueCurveSensitivityFloorLongShortParity() {
    final CapFloorCMSSpread cmsCapSpreadShort = new CapFloorCMSSpread(EUR, PAYMENT_TIME, PAYMENT_ACCRUAL_FACTOR, -NOTIONAL, FIXING_TIME, SWAP_1, CMS_INDEX_1, SWAP_2, CMS_INDEX_2, SETTLEMENT_TIME,
        STRIKE, !IS_CAP);
    MultipleCurrencyMulticurveSensitivity pvcsLong = METHOD_CMS_SPREAD.presentValueCurveSensitivity(CMS_FLOOR_SPREAD, SABR_MULTICURVES);
    pvcsLong = pvcsLong.cleaned();
    MultipleCurrencyMulticurveSensitivity pvcsShort = METHOD_CMS_SPREAD.presentValueCurveSensitivity(cmsCapSpreadShort, SABR_MULTICURVES);
    pvcsShort = pvcsShort.multipliedBy(-1);
    pvcsShort = pvcsShort.cleaned();
    AssertSensivityObjects.assertEquals("CMS floor spread: Long/Short parity", pvcsLong, pvcsShort, TOLERANCE_PV);
  }

  @Test
  /**
   * Tests the present value curve sensitivity cap/floor parity (Cap - Floor = (cms1 - cms2) - strike).
   */
  public void presentValueCurveSensitivityCapFloorParity() {
    MultipleCurrencyMulticurveSensitivity pvcsCapLong = METHOD_CMS_SPREAD.presentValueCurveSensitivity(CMS_CAP_SPREAD, SABR_MULTICURVES);
    pvcsCapLong = pvcsCapLong.cleaned();
    MultipleCurrencyMulticurveSensitivity pvcsFloorLong = METHOD_CMS_SPREAD.presentValueCurveSensitivity(CMS_FLOOR_SPREAD, SABR_MULTICURVES);
    pvcsFloorLong = pvcsFloorLong.cleaned();
    final CouponCMS cms1 = new CouponCMS(EUR, PAYMENT_TIME, PAYMENT_ACCRUAL_FACTOR, NOTIONAL, FIXING_TIME, SWAP_1, SETTLEMENT_TIME);
    final CouponCMS cms2 = new CouponCMS(EUR, PAYMENT_TIME, PAYMENT_ACCRUAL_FACTOR, NOTIONAL, FIXING_TIME, SWAP_2, SETTLEMENT_TIME);
    final CouponFixed cpnStrike = new CouponFixed(EUR, PAYMENT_TIME, PAYMENT_ACCRUAL_FACTOR, NOTIONAL, STRIKE);
    MultipleCurrencyMulticurveSensitivity pvcsCMS1 = METHOD_CMS_COUPON.presentValueCurveSensitivity(cms1, SABR_MULTICURVES);
    pvcsCMS1 = pvcsCMS1.cleaned();
    MultipleCurrencyMulticurveSensitivity pvcsCMS2 = METHOD_CMS_COUPON.presentValueCurveSensitivity(cms2, SABR_MULTICURVES);
    pvcsCMS2 = pvcsCMS2.cleaned();
    final MultipleCurrencyMulticurveSensitivity pvcsStrike = METHOD_CPN_FIXED.presentValueCurveSensitivity(cpnStrike, MULTICURVES);
    MultipleCurrencyMulticurveSensitivity pvcsParity1 = pvcsCMS1.plus(pvcsCMS2.plus(pvcsStrike).multipliedBy(-1));
    pvcsParity1 = pvcsParity1.cleaned();
    MultipleCurrencyMulticurveSensitivity pvcsParity2 = pvcsCapLong.plus(pvcsFloorLong.multipliedBy(-1));
    pvcsParity2 = pvcsParity2.cleaned();
    AssertSensivityObjects.assertEquals("CMS spread: curve sensitivity - Cap/Floor parity", pvcsParity1, pvcsParity2, TOLERANCE_PV);
  }

  @Test
  /**
   * Tests the present value SABR parameters sensitivity vs finite difference.
   */
  public void presentValueSABRSensitivity() {
    final double pv = METHOD_CMS_SPREAD.presentValue(CMS_CAP_SPREAD, SABR_MULTICURVES).getAmount(EUR);
    final PresentValueSABRSensitivityDataBundle pvsCapLong = METHOD_CMS_SPREAD.presentValueSABRSensitivity(CMS_CAP_SPREAD, SABR_MULTICURVES);
    // SABR sensitivity vs finite difference
    final double shift = 0.0001;
    final double shiftAlpha = 0.00001;
    final double maturity1 = CMS_CAP_SPREAD.getUnderlyingSwap1().getFixedLeg().getNthPayment(CMS_CAP_SPREAD.getUnderlyingSwap1().getFixedLeg().getNumberOfPayments() - 1).getPaymentTime()
        - CMS_CAP_SPREAD.getSettlementTime();
    final DoublesPair expectedExpiryTenor1 = DoublesPair.of(CMS_CAP_SPREAD.getFixingTime(), maturity1);
    final double maturity2 = CMS_CAP_SPREAD.getUnderlyingSwap2().getFixedLeg().getNthPayment(CMS_CAP_SPREAD.getUnderlyingSwap2().getFixedLeg().getNumberOfPayments() - 1).getPaymentTime()
        - CMS_CAP_SPREAD.getSettlementTime();
    final DoublesPair expectedExpiryTenor2 = DoublesPair.of(CMS_CAP_SPREAD.getFixingTime(), maturity2);
    // Alpha sensitivity vs finite difference computation
    final SABRInterestRateParameters sabrParameterAlphaBumped = SABRDataSets.createSABR1AlphaBumped(shiftAlpha);
    final SABRSwaptionProviderDiscount sabrBundleAlphaBumped = new SABRSwaptionProviderDiscount(MULTICURVES, sabrParameterAlphaBumped, EUR1YEURIBOR6M);
    final double pvLongPayerAlphaBumped = METHOD_CMS_SPREAD.presentValue(CMS_CAP_SPREAD, sabrBundleAlphaBumped).getAmount(EUR);
    final double expectedAlphaSensi = (pvLongPayerAlphaBumped - pv) / shiftAlpha;
    assertEquals("Number of alpha sensitivity", pvsCapLong.getAlpha().getMap().keySet().size(), 2);
    assertEquals("Alpha sensitivity expiry/tenor", pvsCapLong.getAlpha().getMap().keySet().contains(expectedExpiryTenor1), true);
    assertEquals("Alpha sensitivity expiry/tenor", pvsCapLong.getAlpha().getMap().keySet().contains(expectedExpiryTenor2), true);
    assertEquals("Alpha sensitivity value", expectedAlphaSensi, pvsCapLong.getAlpha().getMap().get(expectedExpiryTenor1) + pvsCapLong.getAlpha().getMap().get(expectedExpiryTenor2), 5.0E+3);
    // Rho sensitivity vs finite difference computation
    final SABRInterestRateParameters sabrParameterRhoBumped = SABRDataSets.createSABR1RhoBumped();
    final SABRSwaptionProviderDiscount sabrBundleRhoBumped = new SABRSwaptionProviderDiscount(MULTICURVES, sabrParameterRhoBumped, EUR1YEURIBOR6M);
    final double pvLongPayerRhoBumped = METHOD_CMS_SPREAD.presentValue(CMS_CAP_SPREAD, sabrBundleRhoBumped).getAmount(EUR);
    final double expectedRhoSensi = (pvLongPayerRhoBumped - pv) / shift;
    assertEquals("Number of rho sensitivity", pvsCapLong.getRho().getMap().keySet().size(), 2);
    assertEquals("Rho sensitivity expiry/tenor", pvsCapLong.getRho().getMap().keySet().contains(expectedExpiryTenor1), true);
    assertEquals("Rho sensitivity expiry/tenor", pvsCapLong.getRho().getMap().keySet().contains(expectedExpiryTenor2), true);
    assertEquals("Rho sensitivity value", expectedRhoSensi, pvsCapLong.getRho().getMap().get(expectedExpiryTenor1) + pvsCapLong.getRho().getMap().get(expectedExpiryTenor2), 5.0E+1);
    // Alpha sensitivity vs finite difference computation
    final SABRInterestRateParameters sabrParameterNuBumped = SABRDataSets.createSABR1NuBumped();
    final SABRSwaptionProviderDiscount sabrBundleNuBumped = new SABRSwaptionProviderDiscount(MULTICURVES, sabrParameterNuBumped, EUR1YEURIBOR6M);
    final double pvLongPayerNuBumped = METHOD_CMS_SPREAD.presentValue(CMS_CAP_SPREAD, sabrBundleNuBumped).getAmount(EUR);
    final double expectedNuSensi = (pvLongPayerNuBumped - pv) / shift;
    assertEquals("Number of nu sensitivity", pvsCapLong.getNu().getMap().keySet().size(), 2);
    assertTrue("Nu sensitivity expiry/tenor", pvsCapLong.getNu().getMap().keySet().contains(expectedExpiryTenor1));
    assertTrue("Nu sensitivity expiry/tenor", pvsCapLong.getNu().getMap().keySet().contains(expectedExpiryTenor2));
    assertEquals("Nu sensitivity value", expectedNuSensi, pvsCapLong.getNu().getMap().get(expectedExpiryTenor1) + pvsCapLong.getNu().getMap().get(expectedExpiryTenor2), 2.0E+2);
  }

  @Test
  /**
   * Tests the present value SABR parameters sensitivity vs finite difference.
   */
  public void presentValueSABRSensitivityExtrapolation() {
    final double pv = METHOD_CMS_SPREAD_EXTRAPOLATION.presentValue(CMS_CAP_SPREAD, SABR_MULTICURVES).getAmount(EUR);
    final PresentValueSABRSensitivityDataBundle pvsCapLong = METHOD_CMS_SPREAD_EXTRAPOLATION.presentValueSABRSensitivity(CMS_CAP_SPREAD, SABR_MULTICURVES);
    // SABR sensitivity vs finite difference
    final double shift = 0.0001;
    final double shiftAlpha = 0.00001;
    final double maturity1 = CMS_CAP_SPREAD.getUnderlyingSwap1().getFixedLeg().getNthPayment(CMS_CAP_SPREAD.getUnderlyingSwap1().getFixedLeg().getNumberOfPayments() - 1).getPaymentTime()
        - CMS_CAP_SPREAD.getSettlementTime();
    final DoublesPair expectedExpiryTenor1 = DoublesPair.of(CMS_CAP_SPREAD.getFixingTime(), maturity1);
    final double maturity2 = CMS_CAP_SPREAD.getUnderlyingSwap2().getFixedLeg().getNthPayment(CMS_CAP_SPREAD.getUnderlyingSwap2().getFixedLeg().getNumberOfPayments() - 1).getPaymentTime()
        - CMS_CAP_SPREAD.getSettlementTime();
    final DoublesPair expectedExpiryTenor2 = DoublesPair.of(CMS_CAP_SPREAD.getFixingTime(), maturity2);
    // Alpha sensitivity vs finite difference computation
    final SABRInterestRateParameters sabrParameterAlphaBumped = SABRDataSets.createSABR1AlphaBumped(shiftAlpha);
    final SABRSwaptionProviderDiscount sabrBundleAlphaBumped = new SABRSwaptionProviderDiscount(MULTICURVES, sabrParameterAlphaBumped, EUR1YEURIBOR6M);
    final double pvLongPayerAlphaBumped = METHOD_CMS_SPREAD_EXTRAPOLATION.presentValue(CMS_CAP_SPREAD, sabrBundleAlphaBumped).getAmount(EUR);
    final double expectedAlphaSensi = (pvLongPayerAlphaBumped - pv) / shiftAlpha;
    assertEquals("Number of alpha sensitivity", pvsCapLong.getAlpha().getMap().keySet().size(), 2);
    assertEquals("Alpha sensitivity expiry/tenor", pvsCapLong.getAlpha().getMap().keySet().contains(expectedExpiryTenor1), true);
    assertEquals("Alpha sensitivity expiry/tenor", pvsCapLong.getAlpha().getMap().keySet().contains(expectedExpiryTenor2), true);
    assertEquals("Alpha sensitivity value", expectedAlphaSensi, pvsCapLong.getAlpha().getMap().get(expectedExpiryTenor1) + pvsCapLong.getAlpha().getMap().get(expectedExpiryTenor2), 5.0E+3);
    // Rho sensitivity vs finite difference computation
    final SABRInterestRateParameters sabrParameterRhoBumped = SABRDataSets.createSABR1RhoBumped();
    final SABRSwaptionProviderDiscount sabrBundleRhoBumped = new SABRSwaptionProviderDiscount(MULTICURVES, sabrParameterRhoBumped, EUR1YEURIBOR6M);
    final double pvLongPayerRhoBumped = METHOD_CMS_SPREAD_EXTRAPOLATION.presentValue(CMS_CAP_SPREAD, sabrBundleRhoBumped).getAmount(EUR);
    final double expectedRhoSensi = (pvLongPayerRhoBumped - pv) / shift;
    assertEquals("Number of rho sensitivity", pvsCapLong.getRho().getMap().keySet().size(), 2);
    assertEquals("Rho sensitivity expiry/tenor", pvsCapLong.getRho().getMap().keySet().contains(expectedExpiryTenor1), true);
    assertEquals("Rho sensitivity expiry/tenor", pvsCapLong.getRho().getMap().keySet().contains(expectedExpiryTenor2), true);
    assertEquals("Rho sensitivity value", expectedRhoSensi, pvsCapLong.getRho().getMap().get(expectedExpiryTenor1) + pvsCapLong.getRho().getMap().get(expectedExpiryTenor2), 5.0E+1);
    // Alpha sensitivity vs finite difference computation
    final SABRInterestRateParameters sabrParameterNuBumped = SABRDataSets.createSABR1NuBumped();
    final SABRSwaptionProviderDiscount sabrBundleNuBumped = new SABRSwaptionProviderDiscount(MULTICURVES, sabrParameterNuBumped, EUR1YEURIBOR6M);
    final double pvLongPayerNuBumped = METHOD_CMS_SPREAD_EXTRAPOLATION.presentValue(CMS_CAP_SPREAD, sabrBundleNuBumped).getAmount(EUR);
    final double expectedNuSensi = (pvLongPayerNuBumped - pv) / shift;
    assertEquals("Number of nu sensitivity", pvsCapLong.getNu().getMap().keySet().size(), 2);
    assertTrue("Nu sensitivity expiry/tenor", pvsCapLong.getNu().getMap().keySet().contains(expectedExpiryTenor1));
    assertTrue("Nu sensitivity expiry/tenor", pvsCapLong.getNu().getMap().keySet().contains(expectedExpiryTenor2));
    assertEquals("Nu sensitivity value", expectedNuSensi, pvsCapLong.getNu().getMap().get(expectedExpiryTenor1) + pvsCapLong.getNu().getMap().get(expectedExpiryTenor2), 2.0E+2);
  }

  @Test
  /**
   * Tests the present value against the price explicitly computed for constant correlation.
   */
  public void presentValueSABRSensitivityMethodVsCalculator() {
    final PresentValueSABRSensitivityDataBundle pvcsMethod = METHOD_CMS_SPREAD.presentValueSABRSensitivity(CMS_CAP_SPREAD, SABR_MULTICURVES);
    final PresentValueSABRSensitivityDataBundle pvcsCalculator = CMS_CAP_SPREAD.accept(PVSSSSC, SABR_MULTICURVES);
    assertEquals("CMS spread: SABR sensitivity Method vs Calculator", pvcsMethod, pvcsCalculator);
    // Extrapolation
    final PresentValueSABRSensitivityDataBundle pvcsMethodExtra = METHOD_CMS_SPREAD_EXTRAPOLATION.presentValueSABRSensitivity(CMS_CAP_SPREAD, SABR_MULTICURVES);
    final PresentValueSABRSensitivityDataBundle pvcsCalculatorExtra = CMS_CAP_SPREAD.accept(PVSSSSXC, SABR_MULTICURVES);
    assertEquals("CMS spread: SABR sensitivity Method vs Calculator", pvcsMethodExtra, pvcsCalculatorExtra);
  }

  @Test
  /**
   * Tests the long/short parity for the present value SABR sensitivity.
   */
  public void presentValueSABRSensitivityLongShortParity() {
    final CapFloorCMSSpread cmsSpreadShort = new CapFloorCMSSpread(EUR, PAYMENT_TIME, PAYMENT_ACCRUAL_FACTOR, -NOTIONAL, FIXING_TIME, SWAP_1, CMS_INDEX_1, SWAP_2, CMS_INDEX_2, SETTLEMENT_TIME,
        STRIKE, IS_CAP);
    final PresentValueSABRSensitivityDataBundle pvssLong = METHOD_CMS_SPREAD.presentValueSABRSensitivity(CMS_CAP_SPREAD, SABR_MULTICURVES);
    PresentValueSABRSensitivityDataBundle pvssShort = METHOD_CMS_SPREAD.presentValueSABRSensitivity(cmsSpreadShort, SABR_MULTICURVES);
    pvssShort = pvssShort.multiplyBy(-1);
    assertEquals("CMS spread: Long/Short parity", pvssLong, pvssShort);
  }

  @Test(enabled = false)
  /**
   * Tests of performance. "enabled = false" for the standard testing.
   */
  public void performance() {
    long startTime, endTime;
    final int nbTest = 100;
    final double[] pv = new double[nbTest];
    final PresentValueSABRSensitivityDataBundle[] pvss = new PresentValueSABRSensitivityDataBundle[nbTest];
    final MultipleCurrencyMulticurveSensitivity[] pvcs = new MultipleCurrencyMulticurveSensitivity[nbTest];

    startTime = System.currentTimeMillis();
    for (int looptest = 0; looptest < nbTest; looptest++) {
      pv[looptest] = METHOD_CMS_SPREAD.presentValue(CMS_CAP_SPREAD, SABR_MULTICURVES).getAmount(EUR);
    }
    endTime = System.currentTimeMillis();
    System.out.println(nbTest + " CMS spread cap by SABR replication (price): " + (endTime - startTime) + " ms");

    startTime = System.currentTimeMillis();
    for (int looptest = 0; looptest < nbTest; looptest++) {
      pvcs[looptest] = METHOD_CMS_SPREAD.presentValueCurveSensitivity(CMS_CAP_SPREAD, SABR_MULTICURVES);
    }
    endTime = System.currentTimeMillis();
    System.out.println(nbTest + " CMS spread cap by SABR replication (curve risk): " + (endTime - startTime) + " ms");

    startTime = System.currentTimeMillis();
    for (int looptest = 0; looptest < nbTest; looptest++) {
      pvss[looptest] = METHOD_CMS_SPREAD.presentValueSABRSensitivity(CMS_CAP_SPREAD, SABR_MULTICURVES);
    }
    endTime = System.currentTimeMillis();
    System.out.println(nbTest + " CMS spread cap by SABR replication (sabr risk): " + (endTime - startTime) + " ms");
    // Performance note: price: 8-Dec-11: On Mac Pro 3.2 GHz Quad-Core Intel Xeon: 30 ms for 100 caplet 10Y-2Y.
    // Performance note: delta: 8-Dec-11: On Mac Pro 3.2 GHz Quad-Core Intel Xeon: 135 ms for 100 caplet 10Y-2Y.
    // Performance note: vega: 8-Dec-11: On Mac Pro 3.2 GHz Quad-Core Intel Xeon: 250 ms for 100 caplet 10Y-2Y.
  }

  @Test(enabled = false)
  /**
   * Tests of performance. "enabled = false" for the standard testing.
   */
  public void performanceExtrapolation() {
    long startTime, endTime;
    final int nbTest = 100;
    final double[] pv = new double[nbTest];
    final PresentValueSABRSensitivityDataBundle[] pvss = new PresentValueSABRSensitivityDataBundle[nbTest];
    final MultipleCurrencyMulticurveSensitivity[] pvcs = new MultipleCurrencyMulticurveSensitivity[nbTest];

    startTime = System.currentTimeMillis();
    for (int looptest = 0; looptest < nbTest; looptest++) {
      pv[looptest] = METHOD_CMS_SPREAD_EXTRAPOLATION.presentValue(CMS_CAP_SPREAD, SABR_MULTICURVES).getAmount(EUR);
    }
    endTime = System.currentTimeMillis();
    System.out.println(nbTest + " CMS spread cap by SABR extrapolation replication (price): " + (endTime - startTime) + " ms");

    startTime = System.currentTimeMillis();
    for (int looptest = 0; looptest < nbTest; looptest++) {
      pvcs[looptest] = METHOD_CMS_SPREAD_EXTRAPOLATION.presentValueCurveSensitivity(CMS_CAP_SPREAD, SABR_MULTICURVES);
    }
    endTime = System.currentTimeMillis();
    System.out.println(nbTest + " CMS spread cap by SABR extrapolation replication (curve risk): " + (endTime - startTime) + " ms");

    startTime = System.currentTimeMillis();
    for (int looptest = 0; looptest < nbTest; looptest++) {
      pvss[looptest] = METHOD_CMS_SPREAD_EXTRAPOLATION.presentValueSABRSensitivity(CMS_CAP_SPREAD, SABR_MULTICURVES);
    }
    endTime = System.currentTimeMillis();
    System.out.println(nbTest + " CMS spread cap by SABR extrapolation replication (sabr risk): " + (endTime - startTime) + " ms");
    // Performance note: CMS spread binormal SABR extrapolation price: 13-Jun-12: On Mac Pro 3.2 GHz Quad-Core Intel Xeon: xx ms for 100 caplet 10Y-2Y.
    // Performance note: CMS spread binormal SABR extrapolation delta: 13-Jun-12: On Mac Pro 3.2 GHz Quad-Core Intel Xeon: xx ms for 100 caplet 10Y-2Y.
    // Performance note: CMS spread binormal SABR extrapolation vega: 13-Jun-12: On Mac Pro 3.2 GHz Quad-Core Intel Xeon: xx ms for 100 caplet 10Y-2Y.
  }

}
