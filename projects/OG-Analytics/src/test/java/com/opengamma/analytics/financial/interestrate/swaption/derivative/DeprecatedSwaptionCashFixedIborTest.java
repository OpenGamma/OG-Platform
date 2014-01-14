/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate.swaption.derivative;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertFalse;
import static org.testng.AssertJUnit.assertTrue;

import org.testng.annotations.Test;
import org.threeten.bp.Period;
import org.threeten.bp.ZonedDateTime;

import com.opengamma.analytics.financial.instrument.annuity.AnnuityCouponFixedDefinition;
import com.opengamma.analytics.financial.instrument.annuity.AnnuityCouponIborDefinition;
import com.opengamma.analytics.financial.instrument.index.IborIndex;
import com.opengamma.analytics.financial.instrument.index.IndexSwap;
import com.opengamma.analytics.financial.instrument.swap.SwapFixedIborDefinition;
import com.opengamma.analytics.financial.instrument.swaption.SwaptionCashFixedIborDefinition;
import com.opengamma.analytics.financial.interestrate.ParRateCalculator;
import com.opengamma.analytics.financial.interestrate.PresentValueSABRCalculator;
import com.opengamma.analytics.financial.interestrate.TestsDataSetsSABR;
import com.opengamma.analytics.financial.interestrate.YieldCurveBundle;
import com.opengamma.analytics.financial.interestrate.payments.derivative.Coupon;
import com.opengamma.analytics.financial.interestrate.swap.derivative.SwapFixedCoupon;
import com.opengamma.analytics.financial.interestrate.swap.method.SwapFixedCouponDiscountingMethod;
import com.opengamma.analytics.financial.model.interestrate.curve.YieldAndDiscountCurve;
import com.opengamma.analytics.financial.model.interestrate.curve.YieldCurve;
import com.opengamma.analytics.financial.model.option.definition.SABRInterestRateDataBundle;
import com.opengamma.analytics.financial.model.option.definition.SABRInterestRateParameters;
import com.opengamma.analytics.financial.model.option.pricing.analytic.formula.BlackFunctionData;
import com.opengamma.analytics.financial.model.option.pricing.analytic.formula.BlackPriceFunction;
import com.opengamma.analytics.math.curve.ConstantDoublesCurve;
import com.opengamma.analytics.math.function.Function1D;
import com.opengamma.financial.convention.businessday.BusinessDayConvention;
import com.opengamma.financial.convention.businessday.BusinessDayConventions;
import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.financial.convention.calendar.MondayToFridayCalendar;
import com.opengamma.financial.convention.daycount.DayCount;
import com.opengamma.financial.convention.daycount.DayCounts;
import com.opengamma.util.money.Currency;
import com.opengamma.util.test.TestGroup;
import com.opengamma.util.time.DateUtils;

/**
 * @deprecated This class tests deprecated functionality.
 */
@Deprecated
@Test(groups = TestGroup.UNIT)
public class DeprecatedSwaptionCashFixedIborTest {
  // Swaption description
  private static final ZonedDateTime EXPIRY_DATE = DateUtils.getUTCDate(2011, 3, 28);
  private static final boolean IS_LONG = true;
  // Swap 2Y description
  private static final Currency CUR = Currency.EUR;
  private static final Calendar CALENDAR = new MondayToFridayCalendar("A");
  private static final BusinessDayConvention BUSINESS_DAY = BusinessDayConventions.MODIFIED_FOLLOWING;
  private static final boolean IS_EOM = true;
  private static final Period ANNUITY_TENOR = Period.ofYears(2);
  private static final ZonedDateTime SETTLEMENT_DATE = DateUtils.getUTCDate(2011, 3, 30);
  private static final double NOTIONAL = 100000000; //100m
  //  Fixed leg: Semi-annual bond
  private static final Period FIXED_PAYMENT_PERIOD = Period.ofMonths(6);
  private static final DayCount FIXED_DAY_COUNT = DayCounts.THIRTY_U_360;
  private static final double RATE = 0.0325;
  private static final boolean FIXED_IS_PAYER = true;
  private static final AnnuityCouponFixedDefinition FIXED_ANNUITY_PAYER = AnnuityCouponFixedDefinition.from(CUR, SETTLEMENT_DATE, ANNUITY_TENOR, FIXED_PAYMENT_PERIOD, CALENDAR, FIXED_DAY_COUNT,
      BUSINESS_DAY, IS_EOM, NOTIONAL, RATE, FIXED_IS_PAYER);
  private static final AnnuityCouponFixedDefinition FIXED_ANNUITY_RECEIVER = AnnuityCouponFixedDefinition.from(CUR, SETTLEMENT_DATE, ANNUITY_TENOR, FIXED_PAYMENT_PERIOD, CALENDAR, FIXED_DAY_COUNT,
      BUSINESS_DAY, IS_EOM, NOTIONAL, RATE, !FIXED_IS_PAYER);
  //  Ibor leg: quarterly money
  private static final Period INDEX_TENOR = Period.ofMonths(3);
  private static final int SETTLEMENT_DAYS = 2;
  private static final DayCount DAY_COUNT = DayCounts.ACT_360;
  private static final IborIndex INDEX = new IborIndex(CUR, INDEX_TENOR, SETTLEMENT_DAYS, DAY_COUNT, BUSINESS_DAY, IS_EOM, "Ibor");
  private static final AnnuityCouponIborDefinition IBOR_ANNUITY_RECEIVER = AnnuityCouponIborDefinition.from(SETTLEMENT_DATE, ANNUITY_TENOR, NOTIONAL, INDEX, !FIXED_IS_PAYER, CALENDAR);
  private static final AnnuityCouponIborDefinition IBOR_ANNUITY_PAYER = AnnuityCouponIborDefinition.from(SETTLEMENT_DATE, ANNUITY_TENOR, NOTIONAL, INDEX, FIXED_IS_PAYER, CALENDAR);
  // Swaption construction: All combinations
  private static final IndexSwap CMS_INDEX = new IndexSwap(FIXED_PAYMENT_PERIOD, FIXED_DAY_COUNT, INDEX, ANNUITY_TENOR, CALENDAR);
  private static final SwapFixedIborDefinition SWAP_DEFINITION_PAYER = new SwapFixedIborDefinition(FIXED_ANNUITY_PAYER, IBOR_ANNUITY_RECEIVER);
  private static final SwapFixedIborDefinition SWAP_DEFINITION_RECEIVER = new SwapFixedIborDefinition(FIXED_ANNUITY_RECEIVER, IBOR_ANNUITY_PAYER);
  private static final SwaptionCashFixedIborDefinition SWAPTION_DEFINITION_LONG_PAYER = SwaptionCashFixedIborDefinition.from(EXPIRY_DATE, SWAP_DEFINITION_PAYER, IS_LONG);
  private static final SwaptionCashFixedIborDefinition SWAPTION_DEFINITION_LONG_RECEIVER = SwaptionCashFixedIborDefinition.from(EXPIRY_DATE, SWAP_DEFINITION_RECEIVER, IS_LONG);
  private static final SwaptionCashFixedIborDefinition SWAPTION_DEFINITION_SHORT_PAYER = SwaptionCashFixedIborDefinition.from(EXPIRY_DATE, SWAP_DEFINITION_PAYER, !IS_LONG);
  private static final SwaptionCashFixedIborDefinition SWAPTION_DEFINITION_SHORT_RECEIVER = SwaptionCashFixedIborDefinition.from(EXPIRY_DATE, SWAP_DEFINITION_RECEIVER, !IS_LONG);
  // to derivatives
  private static final ZonedDateTime REFERENCE_DATE = DateUtils.getUTCDate(2010, 8, 18);
  private static final String FUNDING_CURVE_NAME = "Funding";
  private static final String FORWARD_CURVE_NAME = "Forward";
  private static final String[] CURVES_NAME = {FUNDING_CURVE_NAME, FORWARD_CURVE_NAME };
  private static final SwapFixedCoupon<Coupon> SWAP_PAYER = SWAP_DEFINITION_PAYER.toDerivative(REFERENCE_DATE, CURVES_NAME);
  private static final SwaptionCashFixedIbor SWAPTION_LONG_PAYER = SWAPTION_DEFINITION_LONG_PAYER.toDerivative(REFERENCE_DATE, CURVES_NAME);
  private static final SwaptionCashFixedIbor SWAPTION_LONG_RECEIVER = SWAPTION_DEFINITION_LONG_RECEIVER.toDerivative(REFERENCE_DATE, CURVES_NAME);
  private static final SwaptionCashFixedIbor SWAPTION_SHORT_PAYER = SWAPTION_DEFINITION_SHORT_PAYER.toDerivative(REFERENCE_DATE, CURVES_NAME);
  private static final SwaptionCashFixedIbor SWAPTION_SHORT_RECEIVER = SWAPTION_DEFINITION_SHORT_RECEIVER.toDerivative(REFERENCE_DATE, CURVES_NAME);
  // Yield curves
  private static final YieldAndDiscountCurve CURVE_5 = YieldCurve.from(ConstantDoublesCurve.from(0.05));
  private static final YieldAndDiscountCurve CURVE_4 = YieldCurve.from(ConstantDoublesCurve.from(0.04));
  // Calculators
  private static final ParRateCalculator PRC = ParRateCalculator.getInstance();
  // Volatility and pricing functions
  private static final BlackPriceFunction BLACK_FUNCTION = new BlackPriceFunction();
  private static final SwapFixedCouponDiscountingMethod METHOD_SWAP = SwapFixedCouponDiscountingMethod.getInstance();

  /**
   * Tests the equal and hashCode methods.
   */
  @Test
  public void equalHash() {
    assertTrue(SWAPTION_LONG_PAYER.equals(SWAPTION_LONG_PAYER));
    final SwaptionCashFixedIbor other = SWAPTION_DEFINITION_LONG_PAYER.toDerivative(REFERENCE_DATE, CURVES_NAME);
    assertTrue(SWAPTION_LONG_PAYER.equals(other));
    assertTrue(SWAPTION_LONG_PAYER.hashCode() == other.hashCode());
    assertEquals(SWAPTION_LONG_PAYER.toString(), other.toString());
    final SwaptionCashFixedIbor otherS = SWAPTION_DEFINITION_SHORT_RECEIVER.toDerivative(REFERENCE_DATE, CURVES_NAME);
    assertTrue(SWAPTION_SHORT_RECEIVER.equals(otherS));
    assertTrue(SWAPTION_SHORT_RECEIVER.hashCode() == otherS.hashCode());
    SwaptionCashFixedIbor modifiedSwaption;
    modifiedSwaption = SwaptionCashFixedIbor.from(SWAPTION_LONG_PAYER.getTimeToExpiry() - 0.01, SWAP_PAYER, SWAPTION_LONG_PAYER.getSettlementTime(), FIXED_IS_PAYER, IS_LONG);
    assertFalse(SWAPTION_LONG_PAYER.equals(modifiedSwaption));
    modifiedSwaption = SwaptionCashFixedIbor.from(SWAPTION_LONG_PAYER.getTimeToExpiry(), SWAP_PAYER, SWAPTION_LONG_PAYER.getSettlementTime() - 0.01, FIXED_IS_PAYER, IS_LONG);
    assertFalse(SWAPTION_LONG_PAYER.equals(modifiedSwaption));
    modifiedSwaption = SwaptionCashFixedIbor.from(SWAPTION_LONG_PAYER.getTimeToExpiry(), SWAP_PAYER, SWAPTION_LONG_PAYER.getSettlementTime(), FIXED_IS_PAYER, !IS_LONG);
    assertFalse(SWAPTION_LONG_PAYER.equals(modifiedSwaption));
    final SwapFixedIborDefinition otherSwapDefinition = SwapFixedIborDefinition.from(SETTLEMENT_DATE, CMS_INDEX, 2 * NOTIONAL, RATE, FIXED_IS_PAYER, CALENDAR);
    final SwapFixedCoupon<Coupon> otherSwap = otherSwapDefinition.toDerivative(REFERENCE_DATE, CURVES_NAME);
    modifiedSwaption = SwaptionCashFixedIbor.from(SWAPTION_LONG_PAYER.getTimeToExpiry(), otherSwap, SWAPTION_LONG_PAYER.getSettlementTime(), FIXED_IS_PAYER, IS_LONG);
    assertFalse(SWAPTION_LONG_PAYER.equals(modifiedSwaption));
    assertFalse(SWAPTION_LONG_PAYER.equals(EXPIRY_DATE));
    assertFalse(SWAPTION_LONG_PAYER.equals(null));
  }

  @Test
  public void testPriceBlack() {
    // Black price with given volatility and market standard formula for cash settlement
    final YieldCurveBundle CURVES = new YieldCurveBundle();
    CURVES.setCurve(FUNDING_CURVE_NAME, CURVE_5);
    CURVES.setCurve(FORWARD_CURVE_NAME, CURVE_4);
    final double sigmaBlack = 0.20;
    final double forward = SWAP_PAYER.accept(PRC, CURVES);
    final double pvbp = METHOD_SWAP.getAnnuityCash(SWAP_PAYER, forward);
    final BlackFunctionData data = new BlackFunctionData(forward, pvbp, sigmaBlack);

    final Function1D<BlackFunctionData, Double> funcLongPayer = BLACK_FUNCTION.getPriceFunction(SWAPTION_LONG_PAYER);
    final double priceLongPayer = funcLongPayer.evaluate(data) * (SWAPTION_LONG_PAYER.isLong() ? 1.0 : -1.0);
    final Function1D<BlackFunctionData, Double> funcLongReceiver = BLACK_FUNCTION.getPriceFunction(SWAPTION_LONG_RECEIVER);
    final double priceLongReceiver = funcLongReceiver.evaluate(data) * (SWAPTION_LONG_RECEIVER.isLong() ? 1.0 : -1.0);
    final Function1D<BlackFunctionData, Double> funcShortPayer = BLACK_FUNCTION.getPriceFunction(SWAPTION_SHORT_PAYER);
    final double priceShortPayer = funcShortPayer.evaluate(data) * (SWAPTION_SHORT_PAYER.isLong() ? 1.0 : -1.0);
    final Function1D<BlackFunctionData, Double> funcShortReceiver = BLACK_FUNCTION.getPriceFunction(SWAPTION_SHORT_RECEIVER);
    final double priceShortReceiver = funcShortReceiver.evaluate(data) * (SWAPTION_SHORT_RECEIVER.isLong() ? 1.0 : -1.0);
    // From previous run
    final double expectedPvbp = 190280393.401;
    assertEquals(expectedPvbp, pvbp, 1E-2);
    final double expectedPriceLongPayer = 1553486.18;
    assertEquals(expectedPriceLongPayer, priceLongPayer, 1E-2);
    final double expectedPriceLongReceiver = 38997.648;
    assertEquals(expectedPriceLongReceiver, priceLongReceiver, 1E-2);
    // Long/Short parity
    assertEquals(priceLongPayer, -priceShortPayer, 1E-2);
    assertEquals(priceLongReceiver, -priceShortReceiver, 1E-2);
    // No payer/Receiver parity in cash settled swaptions!
  }

  @Test
  public void testPriceSABRSurface() {
    final YieldCurveBundle curves = TestsDataSetsSABR.createCurves1();
    final SABRInterestRateParameters sabrParameter = TestsDataSetsSABR.createSABR1();
    final SABRInterestRateDataBundle sabrBundle = new SABRInterestRateDataBundle(sabrParameter, curves);
    final PresentValueSABRCalculator pvcSabr = PresentValueSABRCalculator.getInstance();
    // Swaption pricing.
    final double priceLongPayer = SWAPTION_LONG_PAYER.accept(pvcSabr, sabrBundle);
    final double priceShortPayer = SWAPTION_SHORT_PAYER.accept(pvcSabr, sabrBundle);
    final double priceLongReceiver = SWAPTION_LONG_RECEIVER.accept(pvcSabr, sabrBundle);
    final double priceShortReceiver = SWAPTION_SHORT_RECEIVER.accept(pvcSabr, sabrBundle);
    // Long/Short parity
    assertEquals(priceLongPayer, -priceShortPayer, 1E-2);
    assertEquals(priceLongReceiver, -priceShortReceiver, 1E-2);
    // From previous run
    final double expectedPriceLongPayer = 1599334.211;
    assertEquals(expectedPriceLongPayer, priceLongPayer, 1E-2);
  }
}
