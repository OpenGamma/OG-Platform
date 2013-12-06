/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate.swap.method;

import static org.testng.AssertJUnit.assertEquals;

import org.testng.annotations.Test;
import org.threeten.bp.Period;
import org.threeten.bp.ZonedDateTime;

import com.opengamma.analytics.financial.calculator.PresentValueMCACalculator;
import com.opengamma.analytics.financial.instrument.annuity.AnnuityCouponFixedDefinition;
import com.opengamma.analytics.financial.instrument.index.GeneratorSwapFixedIbor;
import com.opengamma.analytics.financial.instrument.index.GeneratorSwapFixedIborMaster;
import com.opengamma.analytics.financial.instrument.index.IborIndex;
import com.opengamma.analytics.financial.instrument.swap.SwapFixedIborSpreadDefinition;
import com.opengamma.analytics.financial.interestrate.TestsDataSetsSABR;
import com.opengamma.analytics.financial.interestrate.YieldCurveBundle;
import com.opengamma.analytics.financial.interestrate.annuity.derivative.AnnuityCouponFixed;
import com.opengamma.analytics.financial.interestrate.payments.derivative.Coupon;
import com.opengamma.analytics.financial.interestrate.swap.derivative.SwapFixedCoupon;
import com.opengamma.analytics.financial.schedule.ScheduleCalculator;
import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.financial.convention.calendar.MondayToFridayCalendar;
import com.opengamma.util.money.Currency;
import com.opengamma.util.money.CurrencyAmount;
import com.opengamma.util.money.MultipleCurrencyAmount;
import com.opengamma.util.test.TestGroup;
import com.opengamma.util.time.DateUtils;

/**
 * Tests related to the pricing and sensitivities of Swap Ibor with spread in the discounting method.
 * @deprecated This class tests deprecated functionality.
 */
@Deprecated
@Test(groups = TestGroup.UNIT)
public class SwapFixedIborSpreadDiscountingMethodTest {

  private static final Calendar TARGET = new MondayToFridayCalendar("TARGET");
  private static final GeneratorSwapFixedIbor EUR1YEURIBOR3M = GeneratorSwapFixedIborMaster.getInstance().getGenerator("EUR1YEURIBOR3M", TARGET);
  private static final IborIndex EURIBOR3M = EUR1YEURIBOR3M.getIborIndex();
  private static final Currency EUR = EURIBOR3M.getCurrency();
  private static final GeneratorSwapFixedIbor EUR3MEURIBOR3M = new GeneratorSwapFixedIbor("EUR3MEURIBOR3M", EURIBOR3M.getTenor(), EURIBOR3M.getDayCount(), EURIBOR3M, TARGET);

  private static final ZonedDateTime REFERENCE_DATE = DateUtils.getUTCDate(2012, 8, 31);
  private static final Period START_TENOR = Period.ofMonths(6);
  private static final Period SWAP_TENOR = Period.ofYears(5);
  private static final ZonedDateTime START_DATE = ScheduleCalculator.getAdjustedDate(REFERENCE_DATE, START_TENOR, EURIBOR3M, TARGET);
  private static final double NOTIONAL = 123000000;
  private static final double SPREAD = 0.0010;
  private static final double FIXED_RATE = 0.0150;
  private static final boolean IS_PAYER = false;

  private static final SwapFixedIborSpreadDefinition SWAP_SPREAD_EUR1Y3M_DEFINITION = SwapFixedIborSpreadDefinition
      .from(START_DATE, SWAP_TENOR, EUR1YEURIBOR3M, NOTIONAL, FIXED_RATE, SPREAD, IS_PAYER, TARGET);
  private static final SwapFixedIborSpreadDefinition SWAP_SPREAD_EUR3M3M_DEFINITION = SwapFixedIborSpreadDefinition
      .from(START_DATE, SWAP_TENOR, EUR3MEURIBOR3M, NOTIONAL, FIXED_RATE, SPREAD, IS_PAYER, TARGET);
  private static final AnnuityCouponFixedDefinition ANNUITY_SPREAD_DEFINITION = AnnuityCouponFixedDefinition.from(EUR, START_DATE, SWAP_TENOR, EURIBOR3M.getTenor(), TARGET, EURIBOR3M.getDayCount(),
      EURIBOR3M.getBusinessDayConvention(), EURIBOR3M.isEndOfMonth(), NOTIONAL, SPREAD, !IS_PAYER);

  private static final YieldCurveBundle CURVES = TestsDataSetsSABR.createCurves1();
  private static final String[] CURVE_NAMES = TestsDataSetsSABR.curves1Names();

  private static final SwapFixedCoupon<Coupon> SWAP_SPREAD_EUR1Y3M = SWAP_SPREAD_EUR1Y3M_DEFINITION.toDerivative(REFERENCE_DATE, CURVE_NAMES);
  private static final SwapFixedCoupon<Coupon> SWAP_SPREAD_EUR3M3M = SWAP_SPREAD_EUR3M3M_DEFINITION.toDerivative(REFERENCE_DATE, CURVE_NAMES);
  private static final AnnuityCouponFixed ANNUITY_SPREAD = ANNUITY_SPREAD_DEFINITION.toDerivative(REFERENCE_DATE, CURVE_NAMES);

  private static final SwapFixedIborSpreadDiscountingMethod METHOD_SWAP_SPREAD = SwapFixedIborSpreadDiscountingMethod.getInstance();
  private static final PresentValueMCACalculator PVC = PresentValueMCACalculator.getInstance();

  private static final double TOLERANCE_PV = 1.0E-2;
  private static final double TOLERANCE_RATE = 1.0E-8;

  @Test
  public void couponEquivalentSpreadModified() {
    final double pvbp3MMod = METHOD_SWAP_SPREAD.presentValueBasisPoint(SWAP_SPREAD_EUR3M3M, EURIBOR3M.getDayCount(), CURVES);
    final double pvbp3M = METHOD_SWAP_SPREAD.presentValueBasisPoint(SWAP_SPREAD_EUR3M3M, CURVES);
    assertEquals("SwapFixedIborSpreadDiscountingMethod: presentValueBasisPoint - modification", pvbp3M, pvbp3MMod, TOLERANCE_PV);
    final double cesm3M = METHOD_SWAP_SPREAD.couponEquivalentSpreadModified(SWAP_SPREAD_EUR3M3M, pvbp3M, CURVES);
    final double cesmExpected = FIXED_RATE - SPREAD; // The convention is the same on both legs.
    assertEquals("SwapFixedIborSpreadDiscountingMethod: couponEquivalentSpreadModified", cesmExpected, cesm3M, TOLERANCE_RATE);
    final double pvbp1Y = METHOD_SWAP_SPREAD.presentValueBasisPoint(SWAP_SPREAD_EUR3M3M, CURVES);
    final double cesm1Y = METHOD_SWAP_SPREAD.couponEquivalentSpreadModified(SWAP_SPREAD_EUR1Y3M, pvbp1Y, CURVES);
    final double pvFixed = SWAP_SPREAD_EUR1Y3M.getFixedLeg().accept(PVC, CURVES).getAmount(EUR);
    final double pvAnnuitySpread = ANNUITY_SPREAD.accept(PVC, CURVES).getAmount(EUR);
    final CurrencyAmount pvIborNoSpread = METHOD_SWAP_SPREAD.presentValueIborNoSpreadPositiveNotional(SWAP_SPREAD_EUR1Y3M.getSecondLeg(), CURVES);
    final double cesm1YExpected = (pvFixed + pvAnnuitySpread) / pvbp1Y;
    assertEquals("SwapFixedIborSpreadDiscountingMethod: couponEquivalentSpreadModified", cesm1YExpected, cesm1Y, TOLERANCE_RATE);
    final double pvIbor = SWAP_SPREAD_EUR1Y3M.getSecondLeg().accept(PVC, CURVES).getAmount(EUR);
    final double pvIborNoSpreadExpected = -(pvIbor - pvAnnuitySpread);
    assertEquals("SwapFixedIborSpreadDiscountingMethod: presentValueIborNoSpreadPositiveNotional", pvIborNoSpreadExpected, pvIborNoSpread.getAmount(), TOLERANCE_PV);
  }

  @Test
  public void presentValueIborNoSpreadPositiveNotional() {
    final CurrencyAmount pvs = METHOD_SWAP_SPREAD.presentValueSpreadPositiveNotional(SWAP_SPREAD_EUR1Y3M.getSecondLeg(), CURVES);
    final MultipleCurrencyAmount pvAnnuitySpread = ANNUITY_SPREAD.accept(PVC, CURVES); // Should be negative: pay float
    assertEquals("SwapFixedIborSpreadDiscountingMethod: presentValueIborNoSpreadPositiveNotional", -pvAnnuitySpread.getAmount(EUR), pvs.getAmount(), TOLERANCE_PV);
  }

  @Test
  public void forwardSwapSpreadModified() {
    final double pvbp1Y = METHOD_SWAP_SPREAD.presentValueBasisPoint(SWAP_SPREAD_EUR3M3M, CURVES);
    final double forwardComputed = METHOD_SWAP_SPREAD.forwardSwapSpreadModified(SWAP_SPREAD_EUR1Y3M, pvbp1Y, CURVES);
    final double pvAnnuitySpread = ANNUITY_SPREAD.accept(PVC, CURVES).getAmount(EUR);
    final double pvAnnuityIbor = SWAP_SPREAD_EUR1Y3M.getSecondLeg().accept(PVC, CURVES).getAmount(EUR);
    final double forwardExpected = -(pvAnnuityIbor - pvAnnuitySpread) / pvbp1Y;
    assertEquals("SwapFixedIborSpreadDiscountingMethod: forwardSwapSpreadModified", forwardExpected, forwardComputed, TOLERANCE_RATE);
  }

}
