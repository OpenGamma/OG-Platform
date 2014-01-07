/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate.payments.method;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertTrue;
import it.unimi.dsi.fastutil.doubles.DoubleAVLTreeSet;

import java.util.List;

import org.testng.annotations.Test;
import org.threeten.bp.LocalDateTime;
import org.threeten.bp.LocalTime;
import org.threeten.bp.Period;
import org.threeten.bp.ZoneOffset;
import org.threeten.bp.ZonedDateTime;

import com.opengamma.analytics.financial.instrument.index.IborIndex;
import com.opengamma.analytics.financial.instrument.index.IndexSwap;
import com.opengamma.analytics.financial.instrument.payment.CapFloorCMSSpreadDefinition;
import com.opengamma.analytics.financial.instrument.swap.SwapFixedIborDefinition;
import com.opengamma.analytics.financial.interestrate.InterestRateCurveSensitivity;
import com.opengamma.analytics.financial.interestrate.ParRateCalculator;
import com.opengamma.analytics.financial.interestrate.PresentValueCurveSensitivitySABRCalculator;
import com.opengamma.analytics.financial.interestrate.PresentValueCurveSensitivitySABRExtrapolationCalculator;
import com.opengamma.analytics.financial.interestrate.PresentValueSABRCalculator;
import com.opengamma.analytics.financial.interestrate.PresentValueSABRExtrapolationCalculator;
import com.opengamma.analytics.financial.interestrate.PresentValueSABRSensitivityDataBundle;
import com.opengamma.analytics.financial.interestrate.PresentValueSABRSensitivitySABRCalculator;
import com.opengamma.analytics.financial.interestrate.PresentValueSABRSensitivitySABRRightExtrapolationCalculator;
import com.opengamma.analytics.financial.interestrate.TestsDataSetsSABR;
import com.opengamma.analytics.financial.interestrate.YieldCurveBundle;
import com.opengamma.analytics.financial.interestrate.method.SensitivityFiniteDifference;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CapFloorCMS;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CapFloorCMSSpread;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CouponCMS;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CouponFixed;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CouponIbor;
import com.opengamma.analytics.financial.interestrate.payments.derivative.Payment;
import com.opengamma.analytics.financial.interestrate.swap.derivative.SwapFixedCoupon;
import com.opengamma.analytics.financial.model.option.definition.SABRInterestRateCorrelationParameters;
import com.opengamma.analytics.financial.model.option.definition.SABRInterestRateDataBundle;
import com.opengamma.analytics.financial.model.option.definition.SABRInterestRateParameters;
import com.opengamma.analytics.financial.model.option.pricing.analytic.formula.EuropeanVanillaOption;
import com.opengamma.analytics.financial.model.option.pricing.analytic.formula.NormalFunctionData;
import com.opengamma.analytics.financial.model.option.pricing.analytic.formula.NormalPriceFunction;
import com.opengamma.analytics.financial.model.volatility.NormalImpliedVolatilityFormula;
import com.opengamma.analytics.financial.schedule.ScheduleCalculator;
import com.opengamma.analytics.financial.util.AssertSensivityObjects;
import com.opengamma.analytics.math.function.DoubleFunction1D;
import com.opengamma.analytics.math.function.Function1D;
import com.opengamma.analytics.math.function.RealPolynomialFunction1D;
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
import com.opengamma.util.tuple.DoublesPair;

/**
 * Tests the pricing of CMS spread option in binormal with correlation by strike approach.
 * @deprecated This class tests deprecated functionality.
 */
@Deprecated
@Test(groups = TestGroup.UNIT)
public class CapFloorCMSSpreadSABRBinormalMethodTest {

  //Swaps
  private static final Currency CUR = Currency.EUR;
  private static final Calendar CALENDAR = new MondayToFridayCalendar("A");
  private static final BusinessDayConvention BUSINESS_DAY = BusinessDayConventions.MODIFIED_FOLLOWING;
  private static final boolean IS_EOM = true;
  private static final Period FIXED_PAYMENT_PERIOD = Period.ofMonths(6);
  private static final DayCount FIXED_DAY_COUNT = DayCounts.THIRTY_U_360;
  private static final boolean FIXED_IS_PAYER = true; // Irrelevant for the underlying
  private static final double RATE = 0.0; // Irrelevant for the underlying
  private static final Period INDEX_TENOR = Period.ofMonths(3);
  private static final int SETTLEMENT_DAYS = 2;
  private static final DayCount DAY_COUNT = DayCounts.ACT_360;
  private static final IborIndex IBOR_INDEX = new IborIndex(CUR, INDEX_TENOR, SETTLEMENT_DAYS, DAY_COUNT, BUSINESS_DAY, IS_EOM, "Ibor");
  private static final ZonedDateTime FIXING_DATE = DateUtils.getUTCDate(2010, 12, 30);
  private static final ZonedDateTime SETTLEMENT_DATE = ScheduleCalculator.getAdjustedDate(FIXING_DATE, SETTLEMENT_DAYS, CALENDAR);
  // Swap 10Y
  private static final Period ANNUITY_TENOR_1 = Period.ofYears(10);
  private static final IndexSwap CMS_INDEX_1 = new IndexSwap(FIXED_PAYMENT_PERIOD, FIXED_DAY_COUNT, IBOR_INDEX, ANNUITY_TENOR_1, CALENDAR);
  private static final SwapFixedIborDefinition SWAP_DEFINITION_1 = SwapFixedIborDefinition.from(SETTLEMENT_DATE, CMS_INDEX_1, 1.0, RATE, FIXED_IS_PAYER, CALENDAR);
  // Swap 2Y
  private static final Period ANNUITY_TENOR_2 = Period.ofYears(2);
  private static final IndexSwap CMS_INDEX_2 = new IndexSwap(FIXED_PAYMENT_PERIOD, FIXED_DAY_COUNT, IBOR_INDEX, ANNUITY_TENOR_2, CALENDAR);
  private static final SwapFixedIborDefinition SWAP_DEFINITION_2 = SwapFixedIborDefinition.from(SETTLEMENT_DATE, CMS_INDEX_2, 1.0, RATE, FIXED_IS_PAYER, CALENDAR);
  // CMS spread coupon
  private static final double NOTIONAL = 100000000;
  private static final ZonedDateTime PAYMENT_DATE = DateUtils.getUTCDate(2011, 4, 6);
  private static final ZonedDateTime ACCRUAL_START_DATE = DateUtils.getUTCDate(2011, 1, 5);
  private static final ZonedDateTime ACCRUAL_END_DATE = DateUtils.getUTCDate(2011, 4, 5);
  private static final DayCount PAYMENT_DAY_COUNT = DayCounts.ACT_360;
  private static final double PAYMENT_ACCRUAL_FACTOR = PAYMENT_DAY_COUNT.getDayCountFraction(ACCRUAL_START_DATE, ACCRUAL_END_DATE);
  private static final double STRIKE = 0.0010; // 10 bps
  private static final boolean IS_CAP = true;
  // to derivatives
  private static final ZonedDateTime REFERENCE_DATE = DateUtils.getUTCDate(2008, 8, 18);
  private static final String FUNDING_CURVE_NAME = "Funding";
  private static final String FORWARD_CURVE_NAME = "Forward";
  private static final String[] CURVES_NAME = {FUNDING_CURVE_NAME, FORWARD_CURVE_NAME };
  private static final SwapFixedCoupon<? extends Payment> SWAP_1 = SWAP_DEFINITION_1.toDerivative(REFERENCE_DATE, CURVES_NAME);
  private static final SwapFixedCoupon<? extends Payment> SWAP_2 = SWAP_DEFINITION_2.toDerivative(REFERENCE_DATE, CURVES_NAME);
  private static final DayCount ACT_ACT = DayCounts.ACT_ACT_ISDA;
  private static final ZonedDateTime REFERENCE_DATE_ZONED = ZonedDateTime.of(LocalDateTime.of(REFERENCE_DATE.toLocalDate(), LocalTime.MIDNIGHT), ZoneOffset.UTC);
  private static final double PAYMENT_TIME = ACT_ACT.getDayCountFraction(REFERENCE_DATE_ZONED, PAYMENT_DATE);
  private static final double FIXING_TIME = ACT_ACT.getDayCountFraction(REFERENCE_DATE_ZONED, FIXING_DATE);
  private static final double SETTLEMENT_TIME = ACT_ACT.getDayCountFraction(REFERENCE_DATE_ZONED, SWAP_DEFINITION_1.getFixedLeg().getNthPayment(0).getAccrualStartDate());

  private static final CapFloorCMSSpreadDefinition CMS_CAP_SPREAD_DEFINITION = new CapFloorCMSSpreadDefinition(CUR, PAYMENT_DATE, ACCRUAL_START_DATE, ACCRUAL_END_DATE, PAYMENT_ACCRUAL_FACTOR,
      NOTIONAL, FIXING_DATE, SWAP_DEFINITION_1, CMS_INDEX_1, SWAP_DEFINITION_2, CMS_INDEX_2, STRIKE, IS_CAP, CALENDAR, CALENDAR);
  private static final CapFloorCMSSpreadDefinition CMS_FLOOR_SPREAD_DEFINITION = new CapFloorCMSSpreadDefinition(CUR, PAYMENT_DATE, ACCRUAL_START_DATE, ACCRUAL_END_DATE, PAYMENT_ACCRUAL_FACTOR,
      NOTIONAL, FIXING_DATE, SWAP_DEFINITION_1, CMS_INDEX_1, SWAP_DEFINITION_2, CMS_INDEX_2, STRIKE, !IS_CAP, CALENDAR, CALENDAR);

  private static final CapFloorCMSSpread CMS_CAP_SPREAD = new CapFloorCMSSpread(CUR, PAYMENT_TIME, PAYMENT_ACCRUAL_FACTOR, NOTIONAL, FIXING_TIME, SWAP_1, CMS_INDEX_1, SWAP_2, CMS_INDEX_2,
      SETTLEMENT_TIME, STRIKE, IS_CAP, FUNDING_CURVE_NAME);
  private static final CapFloorCMSSpread CMS_FLOOR_SPREAD = new CapFloorCMSSpread(CUR, PAYMENT_TIME, PAYMENT_ACCRUAL_FACTOR, NOTIONAL, FIXING_TIME, SWAP_1, CMS_INDEX_1, SWAP_2, CMS_INDEX_2,
      SETTLEMENT_TIME, STRIKE, !IS_CAP, FUNDING_CURVE_NAME);

  private static final YieldCurveBundle CURVES = TestsDataSetsSABR.createCurves1();
  private static final SABRInterestRateParameters SABR_PARAMETERS = TestsDataSetsSABR.createSABR1();
  private static final SABRInterestRateDataBundle SABR_BUNDLE = new SABRInterestRateDataBundle(SABR_PARAMETERS, CURVES);
  private static final double CORRELATION = 0.80;
  private static final DoubleFunction1D CORRELATION_FUNCTION = new RealPolynomialFunction1D(new double[] {CORRELATION }); // Constant function
  private static final SABRInterestRateCorrelationParameters SABR_CORRELATION = SABRInterestRateCorrelationParameters.from(SABR_PARAMETERS, CORRELATION_FUNCTION);
  private static final SABRInterestRateDataBundle SABR_CORRELATION_BUNDLE = new SABRInterestRateDataBundle(SABR_CORRELATION, CURVES);
  private static final CouponCMSSABRReplicationMethod METHOD_CMS_COUPON = CouponCMSSABRReplicationMethod.getInstance();
  private static final CapFloorCMSSABRReplicationMethod METHOD_CMS_CAP = CapFloorCMSSABRReplicationMethod.getDefaultInstance();
  private static final CapFloorCMSSpreadSABRBinormalMethod METHOD_CMS_SPREAD = new CapFloorCMSSpreadSABRBinormalMethod(CORRELATION_FUNCTION, METHOD_CMS_CAP, METHOD_CMS_COUPON);
  private static final CouponFixedDiscountingMethod METHOD_CPN_FIXED = CouponFixedDiscountingMethod.getInstance();
  private static final PresentValueSABRCalculator PVC_SABR = PresentValueSABRCalculator.getInstance();

  private static final double CUT_OFF_STRIKE = 0.10;
  private static final double MU = 2.50;
  private static final PresentValueSABRExtrapolationCalculator PVC_SABR_EXTRA = new PresentValueSABRExtrapolationCalculator(CUT_OFF_STRIKE, MU);

  private static final CapFloorCMSSABRExtrapolationRightReplicationMethod METHOD_CMS_CAP_EXTRAPOLATION = new CapFloorCMSSABRExtrapolationRightReplicationMethod(CUT_OFF_STRIKE, MU);
  private static final CouponCMSSABRExtrapolationRightReplicationMethod METHOD_CMS_COUPON_EXTRAPOLATION = new CouponCMSSABRExtrapolationRightReplicationMethod(CUT_OFF_STRIKE, MU);
  private static final CapFloorCMSSpreadSABRBinormalMethod METHOD_CMS_SPREAD_EXTRAPOLATION = new CapFloorCMSSpreadSABRBinormalMethod(CORRELATION_FUNCTION, METHOD_CMS_CAP_EXTRAPOLATION,
      METHOD_CMS_COUPON_EXTRAPOLATION);

  private static final double TOLERANCE_PRICE = 1.0E-2; // 0.01 currency unit for 100m notional.

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNotNullCorrelation() {
    new CapFloorCMSSpreadSABRBinormalMethod(null, METHOD_CMS_CAP, METHOD_CMS_COUPON);
  }

  @Test
  public void getter() {
    final double correlation = 0.80;
    final DoubleFunction1D correlationFunction = new RealPolynomialFunction1D(new double[] {correlation }); // Constant function
    final CapFloorCMSSpreadSABRBinormalMethod method = new CapFloorCMSSpreadSABRBinormalMethod(correlationFunction, METHOD_CMS_CAP, METHOD_CMS_COUPON);
    assertEquals("CMS spread binormal method: correlation function getter", correlationFunction, method.getCorrelation());
  }

  @Test
  /**
   * Tests the present value against the price explicitly computed for constant correlation.
   */
  public void presentValue() {
    final double correlation = 0.80;
    final DoubleFunction1D correlationFunction = new RealPolynomialFunction1D(new double[] {correlation }); // Constant function
    final CapFloorCMSSpreadSABRBinormalMethod method = new CapFloorCMSSpreadSABRBinormalMethod(correlationFunction, METHOD_CMS_CAP, METHOD_CMS_COUPON);
    final double cmsSpreadPrice = method.presentValue(CMS_CAP_SPREAD, SABR_BUNDLE).getAmount();
    final double discountFactorPayment = CURVES.getCurve(FUNDING_CURVE_NAME).getDiscountFactor(PAYMENT_TIME);
    final CouponCMSSABRReplicationMethod methodCms = CouponCMSSABRReplicationMethod.getInstance();
    final CapFloorCMSSABRReplicationMethod methodCmsCap = CapFloorCMSSABRReplicationMethod.getDefaultInstance();
    final NormalImpliedVolatilityFormula impliedVolatility = new NormalImpliedVolatilityFormula();
    final NormalPriceFunction normalPrice = new NormalPriceFunction();
    final ParRateCalculator parRate = ParRateCalculator.getInstance();
    final CouponCMS cmsCoupon1 = CouponCMS.from(CMS_CAP_SPREAD, SWAP_1, SETTLEMENT_TIME);
    final CouponCMS cmsCoupon2 = CouponCMS.from(CMS_CAP_SPREAD, SWAP_2, SETTLEMENT_TIME);
    final double cmsCoupon1Price = methodCms.presentValue(cmsCoupon1, SABR_BUNDLE).getAmount();
    final double cmsCoupon2Price = methodCms.presentValue(cmsCoupon2, SABR_BUNDLE).getAmount();
    final double expectedRate1 = cmsCoupon1Price / discountFactorPayment / cmsCoupon1.getNotional() / cmsCoupon1.getPaymentYearFraction();
    final double expectedRate2 = cmsCoupon2Price / discountFactorPayment / cmsCoupon2.getNotional() / cmsCoupon2.getPaymentYearFraction();
    final double forward1 = SWAP_1.accept(parRate, CURVES);
    final double forward2 = SWAP_2.accept(parRate, CURVES);
    final CapFloorCMS cmsCap1 = CapFloorCMS.from(cmsCoupon1, forward1, true);
    final CapFloorCMS cmsCap2 = CapFloorCMS.from(cmsCoupon2, forward2, true);
    final double cmsCap1Price = methodCmsCap.presentValue(cmsCap1, SABR_BUNDLE).getAmount();
    final double cmsCap2Price = methodCmsCap.presentValue(cmsCap2, SABR_BUNDLE).getAmount();
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
    assertEquals("CMS spread: price with constant correlation", cmsSpreadPriceExpected, cmsSpreadPrice, TOLERANCE_PRICE);
  }

  @Test
  /**
   * Tests the present value with default method and with method without extrapolation.
   */
  public void presentValueConstructor() {
    final CapFloorCMSSpreadSABRBinormalMethod methodDefault = new CapFloorCMSSpreadSABRBinormalMethod(CORRELATION_FUNCTION, METHOD_CMS_CAP, METHOD_CMS_COUPON);
    final CurrencyAmount pvDefault = methodDefault.presentValue(CMS_CAP_SPREAD, SABR_BUNDLE);
    final CapFloorCMSSpreadSABRBinormalMethod methodNoExtrapolation = new CapFloorCMSSpreadSABRBinormalMethod(CORRELATION_FUNCTION, METHOD_CMS_CAP, METHOD_CMS_COUPON);
    final CurrencyAmount pvNoExtrapolation = methodNoExtrapolation.presentValue(CMS_CAP_SPREAD, SABR_BUNDLE);
    assertEquals("CMS spread: price with constant correlation", pvDefault.getAmount(), pvNoExtrapolation.getAmount(), TOLERANCE_PRICE);
  }

  @Test
  /**
   * Tests the present value with right extrapolation against a hard-coded price.
   */
  public void presentValueRightExtrapolation() {
    final CurrencyAmount pvExtrapolation = METHOD_CMS_SPREAD_EXTRAPOLATION.presentValue(CMS_CAP_SPREAD, SABR_BUNDLE);
    final double pvHardCoded = 107629.349;
    assertEquals("CMS spread: price with constant correlation", pvHardCoded, pvExtrapolation.getAmount(), TOLERANCE_PRICE);
  }

  @Test
  /**
   * Tests the present value long/short parity (cap and floor).
   */
  public void presentValueLongShortParity() {
    final CapFloorCMSSpread cmsCapSpreadShort = new CapFloorCMSSpread(CUR, PAYMENT_TIME, PAYMENT_ACCRUAL_FACTOR, -NOTIONAL, FIXING_TIME, SWAP_1, CMS_INDEX_1, SWAP_2, CMS_INDEX_2, SETTLEMENT_TIME,
        STRIKE,
        IS_CAP, FUNDING_CURVE_NAME);
    final CurrencyAmount pvCapLong = METHOD_CMS_SPREAD.presentValue(CMS_CAP_SPREAD, SABR_BUNDLE);
    final CurrencyAmount pvCapShort = METHOD_CMS_SPREAD.presentValue(cmsCapSpreadShort, SABR_BUNDLE);
    assertEquals("CMS spread: Long/Short parity", pvCapLong.getAmount(), -pvCapShort.getAmount(), TOLERANCE_PRICE);
    final CurrencyAmount pvCapLongExtra = METHOD_CMS_SPREAD_EXTRAPOLATION.presentValue(CMS_CAP_SPREAD, SABR_BUNDLE);
    final CurrencyAmount pvCapShortExtra = METHOD_CMS_SPREAD_EXTRAPOLATION.presentValue(cmsCapSpreadShort, SABR_BUNDLE);
    assertEquals("CMS spread: Long/Short parity", pvCapLongExtra.getAmount(), -pvCapShortExtra.getAmount(), TOLERANCE_PRICE);

    final CapFloorCMSSpread cmsFloorSpreadShort = new CapFloorCMSSpread(CUR, PAYMENT_TIME, PAYMENT_ACCRUAL_FACTOR, -NOTIONAL, FIXING_TIME, SWAP_1, CMS_INDEX_1, SWAP_2, CMS_INDEX_2, SETTLEMENT_TIME,
        STRIKE,
        !IS_CAP, FUNDING_CURVE_NAME);
    final CurrencyAmount pvFloorLong = METHOD_CMS_SPREAD.presentValue(CMS_FLOOR_SPREAD, SABR_BUNDLE);
    final CurrencyAmount pvFloorShort = METHOD_CMS_SPREAD.presentValue(cmsFloorSpreadShort, SABR_BUNDLE);
    assertEquals("CMS spread: Long/Short parity", pvFloorLong.getAmount(), -pvFloorShort.getAmount(), TOLERANCE_PRICE);
    final CurrencyAmount pvFloorLongExtra = METHOD_CMS_SPREAD_EXTRAPOLATION.presentValue(CMS_FLOOR_SPREAD, SABR_BUNDLE);
    final CurrencyAmount pvFloorShortExtra = METHOD_CMS_SPREAD_EXTRAPOLATION.presentValue(cmsFloorSpreadShort, SABR_BUNDLE);
    assertEquals("CMS spread: Long/Short parity", pvFloorLongExtra.getAmount(), -pvFloorShortExtra.getAmount(), TOLERANCE_PRICE);
  }

  @Test
  /**
   * Tests the present value cap/floor parity (Cap - Floor = (cms1 - cms2) - strike).
   */
  public void presentValueCapFloorParity() {
    final CouponCMS cms1 = new CouponCMS(CUR, PAYMENT_TIME, PAYMENT_ACCRUAL_FACTOR, NOTIONAL, FIXING_TIME, SWAP_1, SETTLEMENT_TIME);
    final CouponCMS cms2 = new CouponCMS(CUR, PAYMENT_TIME, PAYMENT_ACCRUAL_FACTOR, NOTIONAL, FIXING_TIME, SWAP_2, SETTLEMENT_TIME);
    final CouponFixed cpnStrike = new CouponFixed(CUR, PAYMENT_TIME, FUNDING_CURVE_NAME, PAYMENT_ACCRUAL_FACTOR, NOTIONAL, STRIKE);
    // No extrapolation
    final CurrencyAmount pvCapLong = METHOD_CMS_SPREAD.presentValue(CMS_CAP_SPREAD, SABR_BUNDLE);
    final CurrencyAmount pvFloorLong = METHOD_CMS_SPREAD.presentValue(CMS_FLOOR_SPREAD, SABR_BUNDLE);
    final Double pvCMS1 = cms1.accept(PVC_SABR, SABR_BUNDLE);
    final Double pvCMS2 = cms2.accept(PVC_SABR, SABR_BUNDLE);
    final Double pvStrike = cpnStrike.accept(PVC_SABR, SABR_BUNDLE);
    assertEquals("CMS spread: Cap/Floor parity", pvCMS1 - pvCMS2 - pvStrike, pvCapLong.getAmount() - pvFloorLong.getAmount(), TOLERANCE_PRICE);
    // Extrapolation
    final CurrencyAmount pvCapLongExtra = METHOD_CMS_SPREAD_EXTRAPOLATION.presentValue(CMS_CAP_SPREAD, SABR_BUNDLE);
    final CurrencyAmount pvFloorLongExtra = METHOD_CMS_SPREAD_EXTRAPOLATION.presentValue(CMS_FLOOR_SPREAD, SABR_BUNDLE);
    final Double pvCMS1Extra = cms1.accept(PVC_SABR_EXTRA, SABR_BUNDLE);
    final Double pvCMS2Extra = cms2.accept(PVC_SABR_EXTRA, SABR_BUNDLE);
    assertEquals("CMS spread: Cap/Floor parity", pvCMS1Extra - pvCMS2Extra - pvStrike, pvCapLongExtra.getAmount() - pvFloorLongExtra.getAmount(), TOLERANCE_PRICE);
  }

  @Test
  /**
   * Tests the present value. Method vs Calculator.
   */
  public void presentValueMethodVsCalculator() {
    final CurrencyAmount pvMethod = METHOD_CMS_SPREAD.presentValue(CMS_CAP_SPREAD, SABR_CORRELATION_BUNDLE);
    final double pvCalculator = CMS_CAP_SPREAD.accept(PVC_SABR, SABR_CORRELATION_BUNDLE);
    assertEquals("CMS spread: present value Method vs Calculator", pvMethod.getAmount(), pvCalculator, TOLERANCE_PRICE);
    // Extrapolation
    final CurrencyAmount pvExtraMethod = METHOD_CMS_SPREAD_EXTRAPOLATION.presentValue(CMS_CAP_SPREAD, SABR_CORRELATION_BUNDLE);
    final double pvExtraCalculator = CMS_CAP_SPREAD.accept(PVC_SABR_EXTRA, SABR_CORRELATION_BUNDLE);
    assertEquals("CMS spread: present value Method vs Calculator", pvExtraMethod.getAmount(), pvExtraCalculator, TOLERANCE_PRICE);
  }

  @Test
  /**
   * Tests the implied correlation computation for a range of correlations.
   */
  public void impliedCorrelation() {
    final YieldCurveBundle curves = TestsDataSetsSABR.createCurves1();
    final SABRInterestRateParameters sabrParameter = TestsDataSetsSABR.createSABR1();
    final SABRInterestRateDataBundle sabrBundle = new SABRInterestRateDataBundle(sabrParameter, curves);
    final double[] correlation = new double[] {-0.50, 0.00, 0.50, 0.75, 0.80, 0.85, 0.90, 0.95, 0.99 };
    final int nbCor = correlation.length;
    final double[] impliedCorrelation = new double[nbCor];
    for (int loopcor = 0; loopcor < nbCor; loopcor++) {
      final DoubleFunction1D correlationFunction = new RealPolynomialFunction1D(new double[] {correlation[loopcor] }); // Constant function
      final CapFloorCMSSpreadSABRBinormalMethod method = new CapFloorCMSSpreadSABRBinormalMethod(correlationFunction, METHOD_CMS_CAP, METHOD_CMS_COUPON);
      final double cmsSpreadPrice = method.presentValue(CMS_CAP_SPREAD, sabrBundle).getAmount();
      impliedCorrelation[loopcor] = method.impliedCorrelation(CMS_CAP_SPREAD, sabrBundle, cmsSpreadPrice);
      assertEquals("CMS spread cap/floor: implied correlation", correlation[loopcor], impliedCorrelation[loopcor], 1.0E-10);
    }
  }

  @Test
  /**
   * Tests the price curve sensitivity of CMS coupon and cap/floor using replication in the SABR framework. Values are tested against finite difference values.
   */
  public void presentValueCurveSensitivityCap() {
    InterestRateCurveSensitivity pvcsCap = METHOD_CMS_SPREAD.presentValueCurveSensitivity(CMS_CAP_SPREAD, SABR_BUNDLE);
    pvcsCap = pvcsCap.cleaned();
    final double deltaToleranceRelative = 2.5E-4; // Numerical imprecision, reduce to E-6 when nbInteration = 1000;
    final double deltaShift = 1.0E-6;
    final String bumpedCurveName = "Bumped Curve";
    // 1. Forward curve sensitivity
    final String[] CurveNameBumpedForward = {FUNDING_CURVE_NAME, bumpedCurveName };
    final CapFloorCMSSpread capBumpedForward = (CapFloorCMSSpread) CMS_CAP_SPREAD_DEFINITION.toDerivative(REFERENCE_DATE, CurveNameBumpedForward);
    final DoubleAVLTreeSet forwardTime = new DoubleAVLTreeSet();
    for (int loopcpn = 0; loopcpn < CMS_CAP_SPREAD.getUnderlyingSwap1().getSecondLeg().getNumberOfPayments(); loopcpn++) {
      final CouponIbor cpn = (CouponIbor) CMS_CAP_SPREAD.getUnderlyingSwap1().getSecondLeg().getNthPayment(loopcpn);
      forwardTime.add(cpn.getFixingPeriodStartTime());
      forwardTime.add(cpn.getFixingPeriodEndTime());
    }
    for (int loopcpn = 0; loopcpn < CMS_CAP_SPREAD.getUnderlyingSwap2().getSecondLeg().getNumberOfPayments(); loopcpn++) {
      final CouponIbor cpn = (CouponIbor) CMS_CAP_SPREAD.getUnderlyingSwap2().getSecondLeg().getNthPayment(loopcpn);
      forwardTime.add(cpn.getFixingPeriodStartTime());
      forwardTime.add(cpn.getFixingPeriodEndTime());
    }
    final double[] nodeTimesForward = forwardTime.toDoubleArray();
    final double[] sensiForwardMethod = SensitivityFiniteDifference.curveSensitivity(capBumpedForward, SABR_BUNDLE, FORWARD_CURVE_NAME, bumpedCurveName, nodeTimesForward, deltaShift,
        METHOD_CMS_SPREAD);
    final List<DoublesPair> sensiPvForward = pvcsCap.getSensitivities().get(FORWARD_CURVE_NAME);
    for (int loopnode = 0; loopnode < sensiForwardMethod.length; loopnode++) {
      final DoublesPair pairPv = sensiPvForward.get(loopnode);
      assertEquals("Sensitivity CMS cap/floor pv to forward curve: Node " + loopnode, nodeTimesForward[loopnode], pairPv.getFirst(), 1E-8);
      assertEquals("Sensitivity finite difference method: node sensitivity " + loopnode + " - Difference " + (sensiForwardMethod[loopnode] - pairPv.second), 0,
          (sensiForwardMethod[loopnode] - pairPv.second) / pairPv.second, deltaToleranceRelative);
    }
    // 2. Discounting curve sensitivity
    final String[] CurveNameBumpedDisc = {bumpedCurveName, FORWARD_CURVE_NAME };
    final CapFloorCMSSpread capBumpedDisc = (CapFloorCMSSpread) CMS_CAP_SPREAD_DEFINITION.toDerivative(REFERENCE_DATE, CurveNameBumpedDisc);
    final DoubleAVLTreeSet discTime = new DoubleAVLTreeSet();
    discTime.add(capBumpedDisc.getPaymentTime());
    for (int loopcpn = 0; loopcpn < CMS_CAP_SPREAD.getUnderlyingSwap1().getSecondLeg().getNumberOfPayments(); loopcpn++) {
      final CouponIbor cpn = (CouponIbor) CMS_CAP_SPREAD.getUnderlyingSwap1().getSecondLeg().getNthPayment(loopcpn);
      discTime.add(cpn.getPaymentTime());
    }
    for (int loopcpn = 0; loopcpn < CMS_CAP_SPREAD.getUnderlyingSwap2().getSecondLeg().getNumberOfPayments(); loopcpn++) {
      final CouponIbor cpn = (CouponIbor) CMS_CAP_SPREAD.getUnderlyingSwap2().getSecondLeg().getNthPayment(loopcpn);
      discTime.add(cpn.getPaymentTime());
    }
    final double[] nodeTimesDisc = discTime.toDoubleArray();
    final double[] sensiDiscMethod = SensitivityFiniteDifference.curveSensitivity(capBumpedDisc, SABR_BUNDLE, FUNDING_CURVE_NAME, bumpedCurveName, nodeTimesDisc, deltaShift, METHOD_CMS_SPREAD);
    final List<DoublesPair> sensiPvDisc = pvcsCap.getSensitivities().get(FUNDING_CURVE_NAME);
    for (int loopnode = 0; loopnode < sensiDiscMethod.length; loopnode++) {
      final DoublesPair pairPv = sensiPvDisc.get(loopnode);
      assertEquals("Sensitivity CMS cap/floor pv to forward curve: Node " + loopnode, nodeTimesDisc[loopnode], pairPv.getFirst(), 1E-8);
      assertEquals("Sensitivity finite difference method: node sensitivity " + loopnode, 0, (sensiDiscMethod[loopnode] - pairPv.second) / pairPv.second, deltaToleranceRelative);
    }
  }

  @Test
  /**
   * Tests the price curve sensitivity of CMS coupon and cap/floor using replication in the SABR framework. Values are tested against finite difference values.
   */
  public void presentValueCurveSensitivityCapExtrapolation() {
    InterestRateCurveSensitivity pvcsCap = METHOD_CMS_SPREAD_EXTRAPOLATION.presentValueCurveSensitivity(CMS_CAP_SPREAD, SABR_BUNDLE);
    pvcsCap = pvcsCap.cleaned();
    final double deltaToleranceRelative = 2.5E-4; // Numerical imprecision, reduce to E-6 when nbInteration = 1000;
    final double deltaShift = 1.0E-6;
    final String bumpedCurveName = "Bumped Curve";
    // 1. Forward curve sensitivity
    final String[] CurveNameBumpedForward = {FUNDING_CURVE_NAME, bumpedCurveName };
    final CapFloorCMSSpread capBumpedForward = (CapFloorCMSSpread) CMS_CAP_SPREAD_DEFINITION.toDerivative(REFERENCE_DATE, CurveNameBumpedForward);
    final DoubleAVLTreeSet forwardTime = new DoubleAVLTreeSet();
    for (int loopcpn = 0; loopcpn < CMS_CAP_SPREAD.getUnderlyingSwap1().getSecondLeg().getNumberOfPayments(); loopcpn++) {
      final CouponIbor cpn = (CouponIbor) CMS_CAP_SPREAD.getUnderlyingSwap1().getSecondLeg().getNthPayment(loopcpn);
      forwardTime.add(cpn.getFixingPeriodStartTime());
      forwardTime.add(cpn.getFixingPeriodEndTime());
    }
    for (int loopcpn = 0; loopcpn < CMS_CAP_SPREAD.getUnderlyingSwap2().getSecondLeg().getNumberOfPayments(); loopcpn++) {
      final CouponIbor cpn = (CouponIbor) CMS_CAP_SPREAD.getUnderlyingSwap2().getSecondLeg().getNthPayment(loopcpn);
      forwardTime.add(cpn.getFixingPeriodStartTime());
      forwardTime.add(cpn.getFixingPeriodEndTime());
    }
    final double[] nodeTimesForward = forwardTime.toDoubleArray();
    final double[] sensiForwardMethod = SensitivityFiniteDifference.curveSensitivity(capBumpedForward, SABR_BUNDLE, FORWARD_CURVE_NAME, bumpedCurveName, nodeTimesForward, deltaShift,
        METHOD_CMS_SPREAD_EXTRAPOLATION);
    final List<DoublesPair> sensiPvForward = pvcsCap.getSensitivities().get(FORWARD_CURVE_NAME);
    for (int loopnode = 0; loopnode < sensiForwardMethod.length; loopnode++) {
      final DoublesPair pairPv = sensiPvForward.get(loopnode);
      assertEquals("Sensitivity CMS cap/floor pv to forward curve: Node " + loopnode, nodeTimesForward[loopnode], pairPv.getFirst(), 1E-8);
      assertEquals("Sensitivity finite difference method: node sensitivity " + loopnode + " - Difference " + (sensiForwardMethod[loopnode] - pairPv.second), 0,
          (sensiForwardMethod[loopnode] - pairPv.second) / pairPv.second, deltaToleranceRelative);
    }
    // 2. Discounting curve sensitivity
    final String[] CurveNameBumpedDisc = {bumpedCurveName, FORWARD_CURVE_NAME };
    final CapFloorCMSSpread capBumpedDisc = (CapFloorCMSSpread) CMS_CAP_SPREAD_DEFINITION.toDerivative(REFERENCE_DATE, CurveNameBumpedDisc);
    final DoubleAVLTreeSet discTime = new DoubleAVLTreeSet();
    discTime.add(capBumpedDisc.getPaymentTime());
    for (int loopcpn = 0; loopcpn < CMS_CAP_SPREAD.getUnderlyingSwap1().getSecondLeg().getNumberOfPayments(); loopcpn++) {
      final CouponIbor cpn = (CouponIbor) CMS_CAP_SPREAD.getUnderlyingSwap1().getSecondLeg().getNthPayment(loopcpn);
      discTime.add(cpn.getPaymentTime());
    }
    for (int loopcpn = 0; loopcpn < CMS_CAP_SPREAD.getUnderlyingSwap2().getSecondLeg().getNumberOfPayments(); loopcpn++) {
      final CouponIbor cpn = (CouponIbor) CMS_CAP_SPREAD.getUnderlyingSwap2().getSecondLeg().getNthPayment(loopcpn);
      discTime.add(cpn.getPaymentTime());
    }
    final double[] nodeTimesDisc = discTime.toDoubleArray();
    final double[] sensiDiscMethod = SensitivityFiniteDifference
        .curveSensitivity(capBumpedDisc, SABR_BUNDLE, FUNDING_CURVE_NAME, bumpedCurveName, nodeTimesDisc, deltaShift, METHOD_CMS_SPREAD_EXTRAPOLATION);
    final List<DoublesPair> sensiPvDisc = pvcsCap.getSensitivities().get(FUNDING_CURVE_NAME);
    for (int loopnode = 0; loopnode < sensiDiscMethod.length; loopnode++) {
      final DoublesPair pairPv = sensiPvDisc.get(loopnode);
      assertEquals("Sensitivity CMS cap/floor pv to forward curve: Node " + loopnode, nodeTimesDisc[loopnode], pairPv.getFirst(), 1E-8);
      assertEquals("Sensitivity finite difference method: node sensitivity " + loopnode, 0, (sensiDiscMethod[loopnode] - pairPv.second) / pairPv.second, deltaToleranceRelative);
    }
  }

  @Test
  /**
   * Tests the price curve sensitivity of CMS coupon and cap/floor using replication in the SABR framework. Values are tested against finite difference values.
   */
  public void presentValueCurveSensitivityCurveUpCap() {
    final YieldCurveBundle curvesUp = TestsDataSetsSABR.createCurves2();
    final SABRInterestRateDataBundle sabrBundleCurveUp = new SABRInterestRateDataBundle(SABR_PARAMETERS, curvesUp);
    final String[] curvesUpName = TestsDataSetsSABR.curves2Names();
    final CapFloorCMSSpread cmsCapSpread = (CapFloorCMSSpread) CMS_CAP_SPREAD_DEFINITION.toDerivative(REFERENCE_DATE, new String[] {curvesUpName[0], curvesUpName[1] });
    InterestRateCurveSensitivity pvcsCap = METHOD_CMS_SPREAD.presentValueCurveSensitivity(cmsCapSpread, sabrBundleCurveUp);
    pvcsCap = pvcsCap.cleaned();
    final double deltaToleranceRelative = 3.0E-4; // Numerical imprecision, reduce to E-6 when nbInteration = 1000;
    final double deltaShift = 1.0E-6;
    final String bumpedCurveName = "Bumped Curve";
    // 1. Forward curve sensitivity
    final String[] CurveNameBumpedForward = {curvesUpName[0], bumpedCurveName };
    final CapFloorCMSSpread capBumpedForward = (CapFloorCMSSpread) CMS_CAP_SPREAD_DEFINITION.toDerivative(REFERENCE_DATE, CurveNameBumpedForward);
    final DoubleAVLTreeSet forwardTime = new DoubleAVLTreeSet();
    for (int loopcpn = 0; loopcpn < CMS_CAP_SPREAD.getUnderlyingSwap1().getSecondLeg().getNumberOfPayments(); loopcpn++) {
      final CouponIbor cpn = (CouponIbor) CMS_CAP_SPREAD.getUnderlyingSwap1().getSecondLeg().getNthPayment(loopcpn);
      forwardTime.add(cpn.getFixingPeriodStartTime());
      forwardTime.add(cpn.getFixingPeriodEndTime());
    }
    for (int loopcpn = 0; loopcpn < CMS_CAP_SPREAD.getUnderlyingSwap2().getSecondLeg().getNumberOfPayments(); loopcpn++) {
      final CouponIbor cpn = (CouponIbor) CMS_CAP_SPREAD.getUnderlyingSwap2().getSecondLeg().getNthPayment(loopcpn);
      forwardTime.add(cpn.getFixingPeriodStartTime());
      forwardTime.add(cpn.getFixingPeriodEndTime());
    }
    final double[] nodeTimesForward = forwardTime.toDoubleArray();
    final double[] sensiForwardMethod = SensitivityFiniteDifference.curveSensitivity(capBumpedForward, sabrBundleCurveUp, curvesUpName[1], bumpedCurveName, nodeTimesForward, deltaShift,
        METHOD_CMS_SPREAD);
    final List<DoublesPair> sensiPvForward = pvcsCap.getSensitivities().get(curvesUpName[1]);
    for (int loopnode = 0; loopnode < sensiForwardMethod.length; loopnode++) {
      final DoublesPair pairPv = sensiPvForward.get(loopnode);
      assertEquals("Sensitivity CMS cap/floor pv to forward curve: Node " + loopnode, nodeTimesForward[loopnode], pairPv.getFirst(), 1E-8);
      assertEquals("Sensitivity finite difference method: node sensitivity " + loopnode + " - Difference " + (sensiForwardMethod[loopnode] - pairPv.second), 0,
          (sensiForwardMethod[loopnode] - pairPv.second) / pairPv.second, deltaToleranceRelative);
    }
    // 2. Discounting curve sensitivity
    final String[] CurveNameBumpedDisc = {bumpedCurveName, curvesUpName[1] };
    final CapFloorCMSSpread capBumpedDisc = (CapFloorCMSSpread) CMS_CAP_SPREAD_DEFINITION.toDerivative(REFERENCE_DATE, CurveNameBumpedDisc);
    final DoubleAVLTreeSet discTime = new DoubleAVLTreeSet();
    discTime.add(capBumpedDisc.getPaymentTime());
    for (int loopcpn = 0; loopcpn < CMS_CAP_SPREAD.getUnderlyingSwap1().getSecondLeg().getNumberOfPayments(); loopcpn++) {
      final CouponIbor cpn = (CouponIbor) CMS_CAP_SPREAD.getUnderlyingSwap1().getSecondLeg().getNthPayment(loopcpn);
      discTime.add(cpn.getPaymentTime());
    }
    for (int loopcpn = 0; loopcpn < CMS_CAP_SPREAD.getUnderlyingSwap2().getSecondLeg().getNumberOfPayments(); loopcpn++) {
      final CouponIbor cpn = (CouponIbor) CMS_CAP_SPREAD.getUnderlyingSwap2().getSecondLeg().getNthPayment(loopcpn);
      discTime.add(cpn.getPaymentTime());
    }
    final double[] nodeTimesDisc = discTime.toDoubleArray();
    final double[] sensiDiscMethod = SensitivityFiniteDifference.curveSensitivity(capBumpedDisc, sabrBundleCurveUp, curvesUpName[0], bumpedCurveName, nodeTimesDisc, deltaShift, METHOD_CMS_SPREAD);
    final List<DoublesPair> sensiPvDisc = pvcsCap.getSensitivities().get(curvesUpName[0]);
    for (int loopnode = 0; loopnode < sensiDiscMethod.length; loopnode++) {
      final DoublesPair pairPv = sensiPvDisc.get(loopnode);
      assertEquals("Sensitivity CMS cap/floor pv to forward curve: Node " + loopnode, nodeTimesDisc[loopnode], pairPv.getFirst(), 1E-8);
      assertEquals("Sensitivity finite difference method: node sensitivity " + loopnode, 0, (sensiDiscMethod[loopnode] - pairPv.second) / pairPv.second, deltaToleranceRelative);
    }
  }

  @Test
  /**
   * Tests the price curve sensitivity of CMS coupon and cap/floor using replication in the SABR framework. Values are tested against finite difference values.
   */
  public void presentValueCurveSensitivityFloor() {
    InterestRateCurveSensitivity pvcsFloor = METHOD_CMS_SPREAD.presentValueCurveSensitivity(CMS_FLOOR_SPREAD, SABR_BUNDLE);
    pvcsFloor = pvcsFloor.cleaned();
    final double deltaToleranceRelative = 7.0E-4; // Numerical imprecision, reduce to E-6 when nbInteration = 1000;
    final double deltaShift = 1.0E-6;
    final String bumpedCurveName = "Bumped Curve";
    // 1. Forward curve sensitivity
    final String[] CurveNameBumpedForward = {FUNDING_CURVE_NAME, bumpedCurveName };
    final CapFloorCMSSpread floorBumpedForward = (CapFloorCMSSpread) CMS_FLOOR_SPREAD_DEFINITION.toDerivative(REFERENCE_DATE, CurveNameBumpedForward);
    final DoubleAVLTreeSet forwardTime = new DoubleAVLTreeSet();
    for (int loopcpn = 0; loopcpn < CMS_FLOOR_SPREAD.getUnderlyingSwap1().getSecondLeg().getNumberOfPayments(); loopcpn++) {
      final CouponIbor cpn = (CouponIbor) CMS_FLOOR_SPREAD.getUnderlyingSwap1().getSecondLeg().getNthPayment(loopcpn);
      forwardTime.add(cpn.getFixingPeriodStartTime());
      forwardTime.add(cpn.getFixingPeriodEndTime());
    }
    for (int loopcpn = 0; loopcpn < CMS_FLOOR_SPREAD.getUnderlyingSwap2().getSecondLeg().getNumberOfPayments(); loopcpn++) {
      final CouponIbor cpn = (CouponIbor) CMS_FLOOR_SPREAD.getUnderlyingSwap2().getSecondLeg().getNthPayment(loopcpn);
      forwardTime.add(cpn.getFixingPeriodStartTime());
      forwardTime.add(cpn.getFixingPeriodEndTime());
    }
    final double[] nodeTimesForward = forwardTime.toDoubleArray();
    final double[] sensiForwardMethod = SensitivityFiniteDifference.curveSensitivity(floorBumpedForward, SABR_BUNDLE, FORWARD_CURVE_NAME, bumpedCurveName, nodeTimesForward, deltaShift,
        METHOD_CMS_SPREAD);
    final List<DoublesPair> sensiPvForward = pvcsFloor.getSensitivities().get(FORWARD_CURVE_NAME);
    for (int loopnode = 0; loopnode < sensiForwardMethod.length; loopnode++) {
      final DoublesPair pairPv = sensiPvForward.get(loopnode);
      assertEquals("Sensitivity CMS cap/floor pv to forward curve: Node " + loopnode, nodeTimesForward[loopnode], pairPv.getFirst(), 1E-8);
      assertEquals("Sensitivity finite difference method: forward node sensitivity " + loopnode + " - Difference " + (sensiForwardMethod[loopnode] - pairPv.second), 0,
          (sensiForwardMethod[loopnode] - pairPv.second) / pairPv.second, deltaToleranceRelative);
    }
    // 2. Discounting curve sensitivity
    final String[] CurveNameBumpedDisc = {bumpedCurveName, FORWARD_CURVE_NAME };
    final CapFloorCMSSpread floorBumpedDisc = (CapFloorCMSSpread) CMS_FLOOR_SPREAD_DEFINITION.toDerivative(REFERENCE_DATE, CurveNameBumpedDisc);
    final DoubleAVLTreeSet discTime = new DoubleAVLTreeSet();
    discTime.add(floorBumpedDisc.getPaymentTime());
    for (int loopcpn = 0; loopcpn < CMS_FLOOR_SPREAD.getUnderlyingSwap1().getSecondLeg().getNumberOfPayments(); loopcpn++) {
      final CouponIbor cpn = (CouponIbor) CMS_FLOOR_SPREAD.getUnderlyingSwap1().getSecondLeg().getNthPayment(loopcpn);
      discTime.add(cpn.getPaymentTime());
    }
    for (int loopcpn = 0; loopcpn < CMS_FLOOR_SPREAD.getUnderlyingSwap2().getSecondLeg().getNumberOfPayments(); loopcpn++) {
      final CouponIbor cpn = (CouponIbor) CMS_FLOOR_SPREAD.getUnderlyingSwap2().getSecondLeg().getNthPayment(loopcpn);
      discTime.add(cpn.getPaymentTime());
    }
    final double[] nodeTimesDisc = discTime.toDoubleArray();
    final double[] sensiDiscMethod = SensitivityFiniteDifference.curveSensitivity(floorBumpedDisc, SABR_BUNDLE, FUNDING_CURVE_NAME, bumpedCurveName, nodeTimesDisc, deltaShift, METHOD_CMS_SPREAD);
    final List<DoublesPair> sensiPvDisc = pvcsFloor.getSensitivities().get(FUNDING_CURVE_NAME);
    for (int loopnode = 0; loopnode < sensiDiscMethod.length; loopnode++) {
      final DoublesPair pairPv = sensiPvDisc.get(loopnode);
      assertEquals("Sensitivity CMS cap/floor pv to forward curve: Node " + loopnode, nodeTimesDisc[loopnode], pairPv.getFirst(), 1E-8);
      assertEquals("Sensitivity finite difference method: discounting node sensitivity " + loopnode, 0, (sensiDiscMethod[loopnode] - pairPv.second) / pairPv.second, deltaToleranceRelative);
    }
  }

  @Test
  /**
   * Tests the present value against the price explicitly computed for constant correlation.
   */
  public void presentValueCurveSensitivityMethodVsCalculator() {
    final PresentValueCurveSensitivitySABRCalculator calculator = PresentValueCurveSensitivitySABRCalculator.getInstance();
    final SABRInterestRateCorrelationParameters sabrCorrelation = SABRInterestRateCorrelationParameters.from(SABR_PARAMETERS, CORRELATION_FUNCTION);
    final SABRInterestRateDataBundle sabrBundleCor = new SABRInterestRateDataBundle(sabrCorrelation, CURVES);
    final InterestRateCurveSensitivity pvcsMethod = METHOD_CMS_SPREAD.presentValueCurveSensitivity(CMS_CAP_SPREAD, sabrBundleCor);
    final InterestRateCurveSensitivity pvcsCalculator = new InterestRateCurveSensitivity(CMS_CAP_SPREAD.accept(calculator, sabrBundleCor));
    assertEquals("CMS spread: curve sensitivity Method vs Calculator", pvcsMethod, pvcsCalculator);
    // Extrapolation
    final PresentValueCurveSensitivitySABRExtrapolationCalculator calculatorExtra = new PresentValueCurveSensitivitySABRExtrapolationCalculator(CUT_OFF_STRIKE, MU);
    final InterestRateCurveSensitivity pvcsMethodExtra = METHOD_CMS_SPREAD_EXTRAPOLATION.presentValueCurveSensitivity(CMS_CAP_SPREAD, sabrBundleCor);
    final InterestRateCurveSensitivity pvcsCalculatorExtra = new InterestRateCurveSensitivity(CMS_CAP_SPREAD.accept(calculatorExtra, sabrBundleCor));
    assertEquals("CMS spread: curve sensitivity Method vs Calculator", pvcsMethodExtra, pvcsCalculatorExtra);
  }

  @Test
  /**
   * Tests the long/short parity for the present value curve sensitivity of a cap.
   */
  public void presentValueCurveSensitivityCapLongShortParity() {
    final CapFloorCMSSpread cmsCapSpreadShort = new CapFloorCMSSpread(CUR, PAYMENT_TIME, PAYMENT_ACCRUAL_FACTOR, -NOTIONAL, FIXING_TIME, SWAP_1, CMS_INDEX_1, SWAP_2, CMS_INDEX_2, SETTLEMENT_TIME,
        STRIKE,
        IS_CAP, FUNDING_CURVE_NAME);
    final SABRInterestRateCorrelationParameters sabrCorrelation = SABRInterestRateCorrelationParameters.from(SABR_PARAMETERS, CORRELATION_FUNCTION);
    final SABRInterestRateDataBundle sabrBundleCor = new SABRInterestRateDataBundle(sabrCorrelation, CURVES);
    InterestRateCurveSensitivity pvcsLong = METHOD_CMS_SPREAD.presentValueCurveSensitivity(CMS_CAP_SPREAD, sabrBundleCor);
    pvcsLong = pvcsLong.cleaned();
    InterestRateCurveSensitivity pvcsShort = METHOD_CMS_SPREAD.presentValueCurveSensitivity(cmsCapSpreadShort, sabrBundleCor);
    pvcsShort = pvcsShort.multipliedBy(-1);
    pvcsShort = pvcsShort.cleaned();
    AssertSensivityObjects.assertEquals("CMS cap spread: Long/Short parity", pvcsLong, pvcsShort, TOLERANCE_PRICE);
  }

  @Test
  /**
   * Tests the long/short parity for the present value curve sensitivity of a floor.
   */
  public void presentValueCurveSensitivityFloorLongShortParity() {
    final CapFloorCMSSpread cmsCapSpreadShort = new CapFloorCMSSpread(CUR, PAYMENT_TIME, PAYMENT_ACCRUAL_FACTOR, -NOTIONAL, FIXING_TIME, SWAP_1, CMS_INDEX_1, SWAP_2, CMS_INDEX_2, SETTLEMENT_TIME,
        STRIKE,
        !IS_CAP, FUNDING_CURVE_NAME);
    final SABRInterestRateCorrelationParameters sabrCorrelation = SABRInterestRateCorrelationParameters.from(SABR_PARAMETERS, CORRELATION_FUNCTION);
    final SABRInterestRateDataBundle sabrBundleCor = new SABRInterestRateDataBundle(sabrCorrelation, CURVES);
    InterestRateCurveSensitivity pvcsLong = METHOD_CMS_SPREAD.presentValueCurveSensitivity(CMS_FLOOR_SPREAD, sabrBundleCor);
    pvcsLong = pvcsLong.cleaned();
    InterestRateCurveSensitivity pvcsShort = METHOD_CMS_SPREAD.presentValueCurveSensitivity(cmsCapSpreadShort, sabrBundleCor);
    pvcsShort = pvcsShort.multipliedBy(-1);
    pvcsShort = pvcsShort.cleaned();
    AssertSensivityObjects.assertEquals("CMS floor spread: Long/Short parity", pvcsLong, pvcsShort, TOLERANCE_PRICE);
  }

  @Test
  /**
   * Tests the present value curve sensitivity cap/floor parity (Cap - Floor = (cms1 - cms2) - strike).
   */
  public void presentValueCurveSensitivityCapFloorParity() {
    final SABRInterestRateCorrelationParameters sabrCorrelation = SABRInterestRateCorrelationParameters.from(SABR_PARAMETERS, CORRELATION_FUNCTION);
    final SABRInterestRateDataBundle sabrBundleCor = new SABRInterestRateDataBundle(sabrCorrelation, CURVES);
    InterestRateCurveSensitivity pvcsCapLong = METHOD_CMS_SPREAD.presentValueCurveSensitivity(CMS_CAP_SPREAD, sabrBundleCor);
    pvcsCapLong = pvcsCapLong.cleaned();
    InterestRateCurveSensitivity pvcsFloorLong = METHOD_CMS_SPREAD.presentValueCurveSensitivity(CMS_FLOOR_SPREAD, sabrBundleCor);
    pvcsFloorLong = pvcsFloorLong.cleaned();
    final CouponCMS cms1 = new CouponCMS(CUR, PAYMENT_TIME, PAYMENT_ACCRUAL_FACTOR, NOTIONAL, FIXING_TIME, SWAP_1, SETTLEMENT_TIME);
    final CouponCMS cms2 = new CouponCMS(CUR, PAYMENT_TIME, PAYMENT_ACCRUAL_FACTOR, NOTIONAL, FIXING_TIME, SWAP_2, SETTLEMENT_TIME);
    final CouponFixed cpnStrike = new CouponFixed(CUR, PAYMENT_TIME, FUNDING_CURVE_NAME, PAYMENT_ACCRUAL_FACTOR, NOTIONAL, STRIKE);
    InterestRateCurveSensitivity pvcsCMS1 = METHOD_CMS_COUPON.presentValueCurveSensitivity(cms1, sabrBundleCor);
    pvcsCMS1 = pvcsCMS1.cleaned();
    InterestRateCurveSensitivity pvcsCMS2 = METHOD_CMS_COUPON.presentValueCurveSensitivity(cms2, sabrBundleCor);
    pvcsCMS2 = pvcsCMS2.cleaned();
    final InterestRateCurveSensitivity pvcsStrike = METHOD_CPN_FIXED.presentValueCurveSensitivity(cpnStrike, sabrBundleCor);
    InterestRateCurveSensitivity pvcsParity1 = pvcsCMS1.plus(pvcsCMS2.plus(pvcsStrike).multipliedBy(-1));
    pvcsParity1 = pvcsParity1.cleaned();
    InterestRateCurveSensitivity pvcsParity2 = pvcsCapLong.plus(pvcsFloorLong.multipliedBy(-1));
    pvcsParity2 = pvcsParity2.cleaned();
    AssertSensivityObjects.assertEquals("CMS spread: curve sensitivity - Cap/Floor parity", pvcsParity1, pvcsParity2, TOLERANCE_PRICE);
  }

  @Test
  /**
   * Tests the present value SABR parameters sensitivity vs finite difference.
   */
  public void presentValueSABRSensitivity() {
    final double pv = METHOD_CMS_SPREAD.presentValue(CMS_CAP_SPREAD, SABR_BUNDLE).getAmount();
    final PresentValueSABRSensitivityDataBundle pvsCapLong = METHOD_CMS_SPREAD.presentValueSABRSensitivity(CMS_CAP_SPREAD, SABR_BUNDLE);
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
    final SABRInterestRateParameters sabrParameterAlphaBumped = TestsDataSetsSABR.createSABR1AlphaBumped(shiftAlpha);
    final SABRInterestRateDataBundle sabrBundleAlphaBumped = new SABRInterestRateDataBundle(sabrParameterAlphaBumped, CURVES);
    final double pvLongPayerAlphaBumped = METHOD_CMS_SPREAD.presentValue(CMS_CAP_SPREAD, sabrBundleAlphaBumped).getAmount();
    final double expectedAlphaSensi = (pvLongPayerAlphaBumped - pv) / shiftAlpha;
    assertEquals("Number of alpha sensitivity", pvsCapLong.getAlpha().getMap().keySet().size(), 2);
    assertEquals("Alpha sensitivity expiry/tenor", pvsCapLong.getAlpha().getMap().keySet().contains(expectedExpiryTenor1), true);
    assertEquals("Alpha sensitivity expiry/tenor", pvsCapLong.getAlpha().getMap().keySet().contains(expectedExpiryTenor2), true);
    assertEquals("Alpha sensitivity value", expectedAlphaSensi, pvsCapLong.getAlpha().getMap().get(expectedExpiryTenor1) + pvsCapLong.getAlpha().getMap().get(expectedExpiryTenor2), 5.0E+3);
    // Rho sensitivity vs finite difference computation
    final SABRInterestRateParameters sabrParameterRhoBumped = TestsDataSetsSABR.createSABR1RhoBumped();
    final SABRInterestRateDataBundle sabrBundleRhoBumped = new SABRInterestRateDataBundle(sabrParameterRhoBumped, CURVES);
    final double pvLongPayerRhoBumped = METHOD_CMS_SPREAD.presentValue(CMS_CAP_SPREAD, sabrBundleRhoBumped).getAmount();
    final double expectedRhoSensi = (pvLongPayerRhoBumped - pv) / shift;
    assertEquals("Number of rho sensitivity", pvsCapLong.getRho().getMap().keySet().size(), 2);
    assertEquals("Rho sensitivity expiry/tenor", pvsCapLong.getRho().getMap().keySet().contains(expectedExpiryTenor1), true);
    assertEquals("Rho sensitivity expiry/tenor", pvsCapLong.getRho().getMap().keySet().contains(expectedExpiryTenor2), true);
    assertEquals("Rho sensitivity value", expectedRhoSensi, pvsCapLong.getRho().getMap().get(expectedExpiryTenor1) + pvsCapLong.getRho().getMap().get(expectedExpiryTenor2), 5.0E+1);
    // Alpha sensitivity vs finite difference computation
    final SABRInterestRateParameters sabrParameterNuBumped = TestsDataSetsSABR.createSABR1NuBumped();
    final SABRInterestRateDataBundle sabrBundleNuBumped = new SABRInterestRateDataBundle(sabrParameterNuBumped, CURVES);
    final double pvLongPayerNuBumped = METHOD_CMS_SPREAD.presentValue(CMS_CAP_SPREAD, sabrBundleNuBumped).getAmount();
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
    final double pv = METHOD_CMS_SPREAD_EXTRAPOLATION.presentValue(CMS_CAP_SPREAD, SABR_BUNDLE).getAmount();
    final PresentValueSABRSensitivityDataBundle pvsCapLong = METHOD_CMS_SPREAD_EXTRAPOLATION.presentValueSABRSensitivity(CMS_CAP_SPREAD, SABR_BUNDLE);
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
    final SABRInterestRateParameters sabrParameterAlphaBumped = TestsDataSetsSABR.createSABR1AlphaBumped(shiftAlpha);
    final SABRInterestRateDataBundle sabrBundleAlphaBumped = new SABRInterestRateDataBundle(sabrParameterAlphaBumped, CURVES);
    final double pvLongPayerAlphaBumped = METHOD_CMS_SPREAD_EXTRAPOLATION.presentValue(CMS_CAP_SPREAD, sabrBundleAlphaBumped).getAmount();
    final double expectedAlphaSensi = (pvLongPayerAlphaBumped - pv) / shiftAlpha;
    assertEquals("Number of alpha sensitivity", pvsCapLong.getAlpha().getMap().keySet().size(), 2);
    assertEquals("Alpha sensitivity expiry/tenor", pvsCapLong.getAlpha().getMap().keySet().contains(expectedExpiryTenor1), true);
    assertEquals("Alpha sensitivity expiry/tenor", pvsCapLong.getAlpha().getMap().keySet().contains(expectedExpiryTenor2), true);
    assertEquals("Alpha sensitivity value", expectedAlphaSensi, pvsCapLong.getAlpha().getMap().get(expectedExpiryTenor1) + pvsCapLong.getAlpha().getMap().get(expectedExpiryTenor2), 5.0E+3);
    // Rho sensitivity vs finite difference computation
    final SABRInterestRateParameters sabrParameterRhoBumped = TestsDataSetsSABR.createSABR1RhoBumped();
    final SABRInterestRateDataBundle sabrBundleRhoBumped = new SABRInterestRateDataBundle(sabrParameterRhoBumped, CURVES);
    final double pvLongPayerRhoBumped = METHOD_CMS_SPREAD_EXTRAPOLATION.presentValue(CMS_CAP_SPREAD, sabrBundleRhoBumped).getAmount();
    final double expectedRhoSensi = (pvLongPayerRhoBumped - pv) / shift;
    assertEquals("Number of rho sensitivity", pvsCapLong.getRho().getMap().keySet().size(), 2);
    assertEquals("Rho sensitivity expiry/tenor", pvsCapLong.getRho().getMap().keySet().contains(expectedExpiryTenor1), true);
    assertEquals("Rho sensitivity expiry/tenor", pvsCapLong.getRho().getMap().keySet().contains(expectedExpiryTenor2), true);
    assertEquals("Rho sensitivity value", expectedRhoSensi, pvsCapLong.getRho().getMap().get(expectedExpiryTenor1) + pvsCapLong.getRho().getMap().get(expectedExpiryTenor2), 5.0E+1);
    // Alpha sensitivity vs finite difference computation
    final SABRInterestRateParameters sabrParameterNuBumped = TestsDataSetsSABR.createSABR1NuBumped();
    final SABRInterestRateDataBundle sabrBundleNuBumped = new SABRInterestRateDataBundle(sabrParameterNuBumped, CURVES);
    final double pvLongPayerNuBumped = METHOD_CMS_SPREAD_EXTRAPOLATION.presentValue(CMS_CAP_SPREAD, sabrBundleNuBumped).getAmount();
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
    final PresentValueSABRSensitivitySABRCalculator calculator = PresentValueSABRSensitivitySABRCalculator.getInstance();
    final SABRInterestRateCorrelationParameters sabrCorrelation = SABRInterestRateCorrelationParameters.from(SABR_PARAMETERS, CORRELATION_FUNCTION);
    final SABRInterestRateDataBundle sabrBundleCor = new SABRInterestRateDataBundle(sabrCorrelation, CURVES);
    final PresentValueSABRSensitivityDataBundle pvcsMethod = METHOD_CMS_SPREAD.presentValueSABRSensitivity(CMS_CAP_SPREAD, sabrBundleCor);
    final PresentValueSABRSensitivityDataBundle pvcsCalculator = CMS_CAP_SPREAD.accept(calculator, sabrBundleCor);
    assertEquals("CMS spread: SABR sensitivity Method vs Calculator", pvcsMethod, pvcsCalculator);
    // Extrapolation
    final PresentValueSABRSensitivitySABRRightExtrapolationCalculator calculatorExtra = new PresentValueSABRSensitivitySABRRightExtrapolationCalculator(CUT_OFF_STRIKE, MU);
    final PresentValueSABRSensitivityDataBundle pvcsMethodExtra = METHOD_CMS_SPREAD_EXTRAPOLATION.presentValueSABRSensitivity(CMS_CAP_SPREAD, sabrBundleCor);
    final PresentValueSABRSensitivityDataBundle pvcsCalculatorExtra = CMS_CAP_SPREAD.accept(calculatorExtra, sabrBundleCor);
    assertEquals("CMS spread: SABR sensitivity Method vs Calculator", pvcsMethodExtra, pvcsCalculatorExtra);
  }

  @Test
  /**
   * Tests the long/short parity for the present value SABR sensitivity.
   */
  public void presentValueSABRSensitivityLongShortParity() {
    final CapFloorCMSSpread cmsSpreadShort = new CapFloorCMSSpread(CUR, PAYMENT_TIME, PAYMENT_ACCRUAL_FACTOR, -NOTIONAL, FIXING_TIME, SWAP_1, CMS_INDEX_1, SWAP_2, CMS_INDEX_2, SETTLEMENT_TIME,
        STRIKE,
        IS_CAP, FUNDING_CURVE_NAME);
    final SABRInterestRateCorrelationParameters sabrCorrelation = SABRInterestRateCorrelationParameters.from(SABR_PARAMETERS, CORRELATION_FUNCTION);
    final SABRInterestRateDataBundle sabrBundleCor = new SABRInterestRateDataBundle(sabrCorrelation, CURVES);
    final PresentValueSABRSensitivityDataBundle pvssLong = METHOD_CMS_SPREAD.presentValueSABRSensitivity(CMS_CAP_SPREAD, sabrBundleCor);
    PresentValueSABRSensitivityDataBundle pvssShort = METHOD_CMS_SPREAD.presentValueSABRSensitivity(cmsSpreadShort, sabrBundleCor);
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
    final InterestRateCurveSensitivity[] pvcs = new InterestRateCurveSensitivity[nbTest];

    startTime = System.currentTimeMillis();
    for (int looptest = 0; looptest < nbTest; looptest++) {
      pv[looptest] = METHOD_CMS_SPREAD.presentValue(CMS_CAP_SPREAD, SABR_BUNDLE).getAmount();
    }
    endTime = System.currentTimeMillis();
    System.out.println(nbTest + " CMS spread cap by SABR replication (price): " + (endTime - startTime) + " ms");

    startTime = System.currentTimeMillis();
    for (int looptest = 0; looptest < nbTest; looptest++) {
      pvcs[looptest] = METHOD_CMS_SPREAD.presentValueCurveSensitivity(CMS_CAP_SPREAD, SABR_BUNDLE);
    }
    endTime = System.currentTimeMillis();
    System.out.println(nbTest + " CMS spread cap by SABR replication (curve risk): " + (endTime - startTime) + " ms");

    startTime = System.currentTimeMillis();
    for (int looptest = 0; looptest < nbTest; looptest++) {
      pvss[looptest] = METHOD_CMS_SPREAD.presentValueSABRSensitivity(CMS_CAP_SPREAD, SABR_BUNDLE);
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
    final InterestRateCurveSensitivity[] pvcs = new InterestRateCurveSensitivity[nbTest];

    startTime = System.currentTimeMillis();
    for (int looptest = 0; looptest < nbTest; looptest++) {
      pv[looptest] = METHOD_CMS_SPREAD_EXTRAPOLATION.presentValue(CMS_CAP_SPREAD, SABR_BUNDLE).getAmount();
    }
    endTime = System.currentTimeMillis();
    System.out.println(nbTest + " CMS spread cap by SABR extrapolation replication (price): " + (endTime - startTime) + " ms");

    startTime = System.currentTimeMillis();
    for (int looptest = 0; looptest < nbTest; looptest++) {
      pvcs[looptest] = METHOD_CMS_SPREAD_EXTRAPOLATION.presentValueCurveSensitivity(CMS_CAP_SPREAD, SABR_BUNDLE);
    }
    endTime = System.currentTimeMillis();
    System.out.println(nbTest + " CMS spread cap by SABR extrapolation replication (curve risk): " + (endTime - startTime) + " ms");

    startTime = System.currentTimeMillis();
    for (int looptest = 0; looptest < nbTest; looptest++) {
      pvss[looptest] = METHOD_CMS_SPREAD_EXTRAPOLATION.presentValueSABRSensitivity(CMS_CAP_SPREAD, SABR_BUNDLE);
    }
    endTime = System.currentTimeMillis();
    System.out.println(nbTest + " CMS spread cap by SABR extrapolation replication (sabr risk): " + (endTime - startTime) + " ms");
    // Performance note: CMS spread binormal SABR extrapolation price: 13-Jun-12: On Mac Pro 3.2 GHz Quad-Core Intel Xeon: xx ms for 100 caplet 10Y-2Y.
    // Performance note: CMS spread binormal SABR extrapolation delta: 13-Jun-12: On Mac Pro 3.2 GHz Quad-Core Intel Xeon: xx ms for 100 caplet 10Y-2Y.
    // Performance note: CMS spread binormal SABR extrapolation vega: 13-Jun-12: On Mac Pro 3.2 GHz Quad-Core Intel Xeon: xx ms for 100 caplet 10Y-2Y.
  }

}
