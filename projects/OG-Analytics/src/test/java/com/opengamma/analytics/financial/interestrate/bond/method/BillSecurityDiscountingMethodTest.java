/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate.bond.method;

import static org.testng.AssertJUnit.assertEquals;

import java.util.List;

import org.testng.AssertJUnit;
import org.testng.annotations.Test;
import org.threeten.bp.ZonedDateTime;

import com.opengamma.analytics.financial.instrument.bond.BillSecurityDefinition;
import com.opengamma.analytics.financial.interestrate.InterestRateCurveSensitivity;
import com.opengamma.analytics.financial.interestrate.PresentValueCalculator;
import com.opengamma.analytics.financial.interestrate.PresentValueCurveSensitivityCalculator;
import com.opengamma.analytics.financial.interestrate.TestsDataSetsSABR;
import com.opengamma.analytics.financial.interestrate.YieldCurveBundle;
import com.opengamma.analytics.financial.interestrate.bond.calculator.YieldFromCleanPriceCalculator;
import com.opengamma.analytics.financial.interestrate.bond.calculator.YieldFromCurvesCalculator;
import com.opengamma.analytics.financial.interestrate.bond.definition.BillSecurity;
import com.opengamma.analytics.financial.interestrate.method.SensitivityFiniteDifference;
import com.opengamma.analytics.financial.schedule.ScheduleCalculator;
import com.opengamma.analytics.financial.util.AssertSensivityObjects;
import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.financial.convention.calendar.MondayToFridayCalendar;
import com.opengamma.financial.convention.daycount.DayCount;
import com.opengamma.financial.convention.daycount.DayCounts;
import com.opengamma.financial.convention.yield.YieldConvention;
import com.opengamma.financial.convention.yield.YieldConventionFactory;
import com.opengamma.util.money.Currency;
import com.opengamma.util.money.CurrencyAmount;
import com.opengamma.util.test.TestGroup;
import com.opengamma.util.time.DateUtils;
import com.opengamma.util.tuple.DoublesPair;

/**
 * Tests related to the pricing of bills security by discounting.
 * @deprecated This class tests deprecated functionality
 */
@Deprecated
@Test(groups = TestGroup.UNIT)
public class BillSecurityDiscountingMethodTest {

  private final static Currency EUR = Currency.EUR;
  private static final Calendar CALENDAR = new MondayToFridayCalendar("TARGET");
  private final static ZonedDateTime REFERENCE_DATE = DateUtils.getUTCDate(2012, 1, 17);

  private static final DayCount ACT360 = DayCounts.ACT_360;
  private static final int SETTLEMENT_DAYS = 2;
  private static final YieldConvention YIELD_IAM = YieldConventionFactory.INSTANCE.getYieldConvention("INTEREST@MTY");
  private static final YieldConvention YIELD_DSC = YieldConventionFactory.INSTANCE.getYieldConvention("DISCOUNT");

  // ISIN: BE0312677462
  private final static String ISSUER_BEL = "BELGIUM GOVT";
  private final static String ISSUER_US = "US GOVT";
  private final static ZonedDateTime END_DATE = DateUtils.getUTCDate(2012, 3, 15);
  private final static double NOTIONAL = 1000;
  private static final double YIELD = 0.00185; // External source
  private static final double PRICE = 0.99971; // External source

  private final static ZonedDateTime SETTLE_DATE = ScheduleCalculator.getAdjustedDate(REFERENCE_DATE, SETTLEMENT_DAYS, CALENDAR);
  private final static String[] NAME_CURVES = TestsDataSetsSABR.nameCurvesBond3();
  private final static BillSecurityDefinition BILL_IAM_SEC_DEFINITION = new BillSecurityDefinition(EUR, END_DATE, NOTIONAL, SETTLEMENT_DAYS, CALENDAR, YIELD_IAM, ACT360, ISSUER_BEL);
  private final static BillSecurityDefinition BILL_DSC_SEC_DEFINITION = new BillSecurityDefinition(EUR, END_DATE, NOTIONAL, SETTLEMENT_DAYS, CALENDAR, YIELD_DSC, ACT360, ISSUER_US);
  //Should not be in EUR, but this is only a test
  private final static BillSecurity BILL_IAM_SEC = BILL_IAM_SEC_DEFINITION.toDerivative(REFERENCE_DATE, SETTLE_DATE, NAME_CURVES);
  private final static BillSecurity BILL_DSC_SEC = BILL_DSC_SEC_DEFINITION.toDerivative(REFERENCE_DATE, SETTLE_DATE, NAME_CURVES);
  private final static YieldCurveBundle CURVE_BUNDLE = TestsDataSetsSABR.createCurvesBond3();

  private final static BillSecurityDiscountingMethod METHOD_SECURITY = BillSecurityDiscountingMethod.getInstance();
  private final static PresentValueCalculator PVC = PresentValueCalculator.getInstance();
  private final static PresentValueCurveSensitivityCalculator PVCSC = PresentValueCurveSensitivityCalculator.getInstance();
  private final static YieldFromCurvesCalculator YFCC = YieldFromCurvesCalculator.getInstance();
  private static final YieldFromCleanPriceCalculator YFPC = YieldFromCleanPriceCalculator.getInstance();

  private static final double TOLERANCE_PV = 1.0E-2;
  private static final double TOLERANCE_PRICE = 1.0E-8;
  private static final double TOLERANCE_PRICE_EXTERNAL = 1.0E-5;
  private static final double TOLERANCE_YIELD = 1.0E-8;
  private static final double TOLERANCE_YIELD_EXTERNAL = 1.0E-4;
  private static final double TOLERANCE_YIELD_DERIVATIVE = 1.0E-6;

  @Test
  /**
   * Tests the present value against explicit computation.
   */
  public void presentValue() {
    final CurrencyAmount pvComputed = METHOD_SECURITY.presentValue(BILL_IAM_SEC, CURVE_BUNDLE);
    final double pvExpected = NOTIONAL * CURVE_BUNDLE.getCurve(NAME_CURVES[1]).getDiscountFactor(BILL_IAM_SEC.getEndTime());
    assertEquals("Bill Security: discounting method - present value", pvExpected, pvComputed.getAmount(), TOLERANCE_PV);
  }

  @Test
  /**
   * Tests the present value: Method vs Calculator
   */
  public void presentValueMethodVsCalculator() {
    final CurrencyAmount pvMethod = METHOD_SECURITY.presentValue(BILL_IAM_SEC, CURVE_BUNDLE);
    final double pvCalculator = BILL_IAM_SEC.accept(PVC, CURVE_BUNDLE);
    assertEquals("Bill Security: discounting method - present value", pvMethod.getAmount(), pvCalculator, TOLERANCE_PV);
  }

  @Test
  public void priceFromYield() {
    final double[] yields = new double[] {0.0010, 0.0, -0.0010 };
    for (final double yield2 : yields) {
      final double priceComputed = METHOD_SECURITY.priceFromYield(BILL_IAM_SEC, yield2);
      final double priceExpected = 1.0 / (1 + BILL_IAM_SEC.getAccrualFactor() * yield2);
      assertEquals("Bill Security: discounting method - price", priceExpected, priceComputed, TOLERANCE_PRICE);
    }
  }

  @Test
  public void priceFromYieldExternal() {
    final double priceComputed = METHOD_SECURITY.priceFromYield(BILL_IAM_SEC, YIELD);
    assertEquals("Bill Security: discounting method - price", PRICE, priceComputed, TOLERANCE_PRICE_EXTERNAL);
  }

  @Test
  public void yieldFromPrice() {
    final double yieldComputedIAM = METHOD_SECURITY.yieldFromPrice(BILL_IAM_SEC, PRICE);
    final double yieldExpectedIAM = (1.0 / PRICE - 1.0) / BILL_IAM_SEC.getAccrualFactor();
    assertEquals("Bill Security: discounting method - yield", yieldExpectedIAM, yieldComputedIAM, TOLERANCE_YIELD);
    final double yieldComputedDSC = METHOD_SECURITY.yieldFromPrice(BILL_DSC_SEC, PRICE);
    final double yieldExpectedDSC = (1.0 - PRICE) / BILL_DSC_SEC.getAccrualFactor();
    assertEquals("Bill Security: discounting method - yield", yieldExpectedDSC, yieldComputedDSC, TOLERANCE_YIELD);
  }

  @Test
  public void yieldFromPriceDerivative() {
    final double shift = 1.0E-8;
    final double yieldIAM = METHOD_SECURITY.yieldFromPrice(BILL_IAM_SEC, PRICE);
    final double yieldPIAM = METHOD_SECURITY.yieldFromPrice(BILL_IAM_SEC, PRICE + shift);
    final double yieldDerivativeExpectedIAM = (yieldPIAM - yieldIAM) / shift;
    final double yieldDerivativeComputedIAM = METHOD_SECURITY.yieldFromPriceDerivative(BILL_IAM_SEC, PRICE);
    assertEquals("Bill Security: discounting method - yield", yieldDerivativeExpectedIAM, yieldDerivativeComputedIAM, TOLERANCE_YIELD_DERIVATIVE);
    final double yieldDSC = METHOD_SECURITY.yieldFromPrice(BILL_DSC_SEC, PRICE);
    final double yieldPDSC = METHOD_SECURITY.yieldFromPrice(BILL_DSC_SEC, PRICE + shift);
    final double yieldDerivativeExpectedDSC = (yieldPDSC - yieldDSC) / shift;
    final double yieldDerivativeComputedDSC = METHOD_SECURITY.yieldFromPriceDerivative(BILL_DSC_SEC, PRICE);
    assertEquals("Bill Security: discounting method - yield", yieldDerivativeExpectedDSC, yieldDerivativeComputedDSC, TOLERANCE_YIELD_DERIVATIVE);
  }

  @Test
  public void yieldFromPriceExternal() {
    final double yieldComputed = METHOD_SECURITY.yieldFromPrice(BILL_IAM_SEC, PRICE);
    assertEquals("Bill Security: discounting method - yield", YIELD, yieldComputed, TOLERANCE_YIELD_EXTERNAL);
  }

  @Test
  public void yieldFromPriceCoherence() {
    final double priceComputed = METHOD_SECURITY.priceFromYield(BILL_IAM_SEC, YIELD);
    final double yieldComputed = METHOD_SECURITY.yieldFromPrice(BILL_IAM_SEC, priceComputed);
    assertEquals("Bill Security: discounting method - yield", YIELD, yieldComputed, TOLERANCE_YIELD);
  }

  @Test
  public void priceFromYieldCoherence() {
    final double yieldComputed = METHOD_SECURITY.yieldFromPrice(BILL_IAM_SEC, PRICE);
    final double priceComputed = METHOD_SECURITY.priceFromYield(BILL_IAM_SEC, yieldComputed);
    assertEquals("Bill Security: discounting method - price", PRICE, priceComputed, TOLERANCE_PRICE);
  }

  @Test
  public void presentValueFromPrice() {
    final CurrencyAmount pvComputed = METHOD_SECURITY.presentValueFromPrice(BILL_IAM_SEC, PRICE, CURVE_BUNDLE);
    final double pvExpected = NOTIONAL * PRICE * CURVE_BUNDLE.getCurve(NAME_CURVES[0]).getDiscountFactor(BILL_IAM_SEC.getSettlementTime());
    assertEquals("Bill Security: discounting method - present value", pvExpected, pvComputed.getAmount(), TOLERANCE_PV);
  }

  @Test
  public void presentValueFromYield() {
    final CurrencyAmount pvComputed = METHOD_SECURITY.presentValueFromYield(BILL_IAM_SEC, YIELD, CURVE_BUNDLE);
    final double price = METHOD_SECURITY.priceFromYield(BILL_IAM_SEC, YIELD);
    final double pvExpected = NOTIONAL * price * CURVE_BUNDLE.getCurve(NAME_CURVES[0]).getDiscountFactor(BILL_IAM_SEC.getSettlementTime());
    assertEquals("Bill Security: discounting method - present value", pvExpected, pvComputed.getAmount(), TOLERANCE_PV);
  }

  @Test
  public void priceFromCurves() {
    final double priceComputed = METHOD_SECURITY.priceFromCurves(BILL_IAM_SEC, CURVE_BUNDLE);
    final CurrencyAmount pvComputed = METHOD_SECURITY.presentValue(BILL_IAM_SEC, CURVE_BUNDLE);
    final double priceExpected = pvComputed.getAmount() / (NOTIONAL * CURVE_BUNDLE.getCurve(NAME_CURVES[0]).getDiscountFactor(BILL_IAM_SEC.getSettlementTime()));
    assertEquals("Bill Security: discounting method - price", priceExpected, priceComputed, TOLERANCE_PRICE);
  }

  @Test
  public void yieldFromCurves() {
    final double yieldComputed = METHOD_SECURITY.yieldFromCurves(BILL_IAM_SEC, CURVE_BUNDLE);
    final double priceComputed = METHOD_SECURITY.priceFromCurves(BILL_IAM_SEC, CURVE_BUNDLE);
    final double yieldExpected = METHOD_SECURITY.yieldFromPrice(BILL_IAM_SEC, priceComputed);
    assertEquals("Bill Security: discounting method - yield", yieldExpected, yieldComputed, TOLERANCE_YIELD);
  }

  @Test
  public void presentValueCurveSensitivity() {
    final InterestRateCurveSensitivity pvcsComputed = METHOD_SECURITY.presentValueCurveSensitivity(BILL_IAM_SEC, CURVE_BUNDLE);
    assertEquals("Bill Security: present value curve sensitivity", 1, pvcsComputed.getSensitivities().size());
    assertEquals("Bill Security: present value curve sensitivity", 1, pvcsComputed.getSensitivities().get(NAME_CURVES[1]).size());
    final double deltaTolerancePrice = 1.0E+2;
    //Testing note: Sensitivity is for a movement of 1. 1E+2 = 0.01 unit for a 1 bp move.
    final double deltaShift = 1.0E-6;
    // Credit curve sensitivity
    final String bumpedCurveName = "Bumped Curve";
    final BillSecurity billBumped = BILL_IAM_SEC_DEFINITION.toDerivative(REFERENCE_DATE, NAME_CURVES[0], bumpedCurveName);
    final double[] nodeTimes = new double[] {billBumped.getEndTime() };
    final double[] sensi = SensitivityFiniteDifference.curveSensitivity(billBumped, CURVE_BUNDLE, NAME_CURVES[1], bumpedCurveName, nodeTimes, deltaShift, METHOD_SECURITY);
    final List<DoublesPair> sensiPv = pvcsComputed.getSensitivities().get(NAME_CURVES[1]);
    for (int loopnode = 0; loopnode < sensi.length; loopnode++) {
      final DoublesPair pairPv = sensiPv.get(loopnode);
      assertEquals("Bill Security: curve sensitivity - Node " + loopnode, nodeTimes[loopnode], pairPv.getFirst(), 1E-8);
      AssertJUnit.assertEquals("Bill Security: curve sensitivity", pairPv.second, sensi[loopnode], deltaTolerancePrice);
    }
  }

  @Test
  public void presentValueCurveSensitivityMethodVsCalculator() {
    final InterestRateCurveSensitivity pvcsMethod = METHOD_SECURITY.presentValueCurveSensitivity(BILL_IAM_SEC, CURVE_BUNDLE);
    final InterestRateCurveSensitivity pvcsCalculator = new InterestRateCurveSensitivity(BILL_IAM_SEC.accept(PVCSC, CURVE_BUNDLE));
    AssertSensivityObjects.assertEquals("Bill Security: discounting method - curve sensitivity", pvcsMethod, pvcsCalculator, TOLERANCE_PV);
  }

  @Test
  public void methodVsCalculator() {
    double yield1 = METHOD_SECURITY.yieldFromCurves(BILL_IAM_SEC, CURVE_BUNDLE);
    double yield2 = BILL_IAM_SEC.accept(YFCC, CURVE_BUNDLE);
    assertEquals("Bill Security: discounting method - yield from curves", yield1, yield2, TOLERANCE_YIELD);
    yield1 = METHOD_SECURITY.yieldFromPrice(BILL_IAM_SEC, PRICE);
    yield2 = BILL_IAM_SEC.accept(YFPC, PRICE);
    assertEquals("Bill Security: discounting method - yield from price", yield1, yield2, TOLERANCE_YIELD);
  }


}
