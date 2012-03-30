/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate.bond.method;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertTrue;

import java.util.List;

import javax.time.calendar.ZonedDateTime;

import org.testng.AssertJUnit;
import org.testng.annotations.Test;

import com.opengamma.analytics.financial.instrument.bond.BillSecurityDefinition;
import com.opengamma.analytics.financial.interestrate.InterestRateCurveSensitivity;
import com.opengamma.analytics.financial.interestrate.PresentValueCalculator;
import com.opengamma.analytics.financial.interestrate.PresentValueCurveSensitivityCalculator;
import com.opengamma.analytics.financial.interestrate.TestsDataSetsSABR;
import com.opengamma.analytics.financial.interestrate.YieldCurveBundle;
import com.opengamma.analytics.financial.interestrate.bond.calculator.YieldFromCurvesCalculator;
import com.opengamma.analytics.financial.interestrate.bond.definition.BillSecurity;
import com.opengamma.analytics.financial.interestrate.bond.method.BillSecurityDiscountingMethod;
import com.opengamma.analytics.financial.interestrate.method.SensitivityFiniteDifference;
import com.opengamma.analytics.financial.schedule.ScheduleCalculator;
import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.financial.convention.calendar.MondayToFridayCalendar;
import com.opengamma.financial.convention.daycount.DayCount;
import com.opengamma.financial.convention.daycount.DayCountFactory;
import com.opengamma.financial.convention.yield.YieldConvention;
import com.opengamma.financial.convention.yield.YieldConventionFactory;
import com.opengamma.util.money.Currency;
import com.opengamma.util.money.CurrencyAmount;
import com.opengamma.util.time.DateUtils;
import com.opengamma.util.tuple.DoublesPair;

/**
 * Tests related to the pricing of bills security by discounting.
 */
public class BillSecurityDiscountingMethodTest {

  private final static Currency EUR = Currency.EUR;
  private static final Calendar CALENDAR = new MondayToFridayCalendar("TARGET");
  private final static ZonedDateTime REFERENCE_DATE = DateUtils.getUTCDate(2012, 1, 17);

  private static final DayCount ACT360 = DayCountFactory.INSTANCE.getDayCount("Actual/360");
  private static final int SETTLEMENT_DAYS = 2;
  private static final YieldConvention YIELD_CONVENTION = YieldConventionFactory.INSTANCE.getYieldConvention("INTEREST@MTY");

  // ISIN: BE0312677462
  private final static String ISSUER_BEL = "BELGIUM GOVT";
  private final static ZonedDateTime END_DATE = DateUtils.getUTCDate(2012, 3, 15);
  private final static double NOTIONAL = 1000;
  private static final double YIELD = 0.00185; // External source
  private static final double PRICE = 0.99971; // External source

  private final static ZonedDateTime SETTLE_DATE = ScheduleCalculator.getAdjustedDate(REFERENCE_DATE, SETTLEMENT_DAYS, CALENDAR);
  private final static String[] NAME_CURVES = TestsDataSetsSABR.nameCurvesBond3();
  private final static BillSecurityDefinition BILL_SEC_DEFINITION = new BillSecurityDefinition(EUR, END_DATE, NOTIONAL, SETTLEMENT_DAYS, CALENDAR, YIELD_CONVENTION, ACT360, ISSUER_BEL);
  private final static BillSecurity BILL_SEC = BILL_SEC_DEFINITION.toDerivative(REFERENCE_DATE, SETTLE_DATE, NAME_CURVES);
  private final static YieldCurveBundle CURVE_BUNDLE = TestsDataSetsSABR.createCurvesBond3();

  private final static BillSecurityDiscountingMethod METHOD_SECURITY = BillSecurityDiscountingMethod.getInstance();
  private final static PresentValueCalculator PVC = PresentValueCalculator.getInstance();
  private final static PresentValueCurveSensitivityCalculator PVCSC = PresentValueCurveSensitivityCalculator.getInstance();
  private final static YieldFromCurvesCalculator YFCC = YieldFromCurvesCalculator.getInstance();

  private static final double TOLERANCE_PV = 1.0E-2;
  private static final double TOLERANCE_PRICE = 1.0E-8;
  private static final double TOLERANCE_PRICE_EXTERNAL = 1.0E-5;
  private static final double TOLERANCE_YIELD = 1.0E-8;
  private static final double TOLERANCE_YIELD_EXTERNAL = 1.0E-4;

  @Test
  /**
   * Tests the present value against explicit computation.
   */
  public void presentValue() {
    CurrencyAmount pvComputed = METHOD_SECURITY.presentValue(BILL_SEC, CURVE_BUNDLE);
    double pvExpected = NOTIONAL * CURVE_BUNDLE.getCurve(NAME_CURVES[1]).getDiscountFactor(BILL_SEC.getEndTime());
    assertEquals("Bill Security: discounting method - present value", pvExpected, pvComputed.getAmount(), TOLERANCE_PV);
  }

  @Test
  /**
   * Tests the present value: Method vs Calculator
   */
  public void presentValueMethodVsCalculator() {
    CurrencyAmount pvMethod = METHOD_SECURITY.presentValue(BILL_SEC, CURVE_BUNDLE);
    double pvCalculator = PVC.visit(BILL_SEC, CURVE_BUNDLE);
    assertEquals("Bill Security: discounting method - present value", pvMethod.getAmount(), pvCalculator, TOLERANCE_PV);
  }

  @Test
  public void priceFromYield() {
    double priceComputed = METHOD_SECURITY.priceFromYield(BILL_SEC, YIELD);
    double priceExpected = 1.0 / (1 + BILL_SEC.getAccralFactor() * YIELD);
    assertEquals("Bill Security: discounting method - price", priceExpected, priceComputed, TOLERANCE_PRICE);
  }

  @Test
  public void priceFromYieldExternal() {
    double priceComputed = METHOD_SECURITY.priceFromYield(BILL_SEC, YIELD);
    assertEquals("Bill Security: discounting method - price", PRICE, priceComputed, TOLERANCE_PRICE_EXTERNAL);
  }

  @Test
  public void yieldFromPrice() {
    double yieldComputed = METHOD_SECURITY.yieldFromPrice(BILL_SEC, PRICE);
    double yieldExpected = (1.0 / PRICE - 1.0) / BILL_SEC.getAccralFactor();
    assertEquals("Bill Security: discounting method - yield", yieldExpected, yieldComputed, TOLERANCE_YIELD);
  }

  @Test
  public void yieldFromPriceExternal() {
    double yieldComputed = METHOD_SECURITY.yieldFromPrice(BILL_SEC, PRICE);
    assertEquals("Bill Security: discounting method - yield", YIELD, yieldComputed, TOLERANCE_YIELD_EXTERNAL);
  }

  @Test
  public void yieldFromPriceCoherence() {
    double priceComputed = METHOD_SECURITY.priceFromYield(BILL_SEC, YIELD);
    double yieldComputed = METHOD_SECURITY.yieldFromPrice(BILL_SEC, priceComputed);
    assertEquals("Bill Security: discounting method - yield", YIELD, yieldComputed, TOLERANCE_YIELD);
  }

  @Test
  public void priceFromYieldCoherence() {
    double yieldComputed = METHOD_SECURITY.yieldFromPrice(BILL_SEC, PRICE);
    double priceComputed = METHOD_SECURITY.priceFromYield(BILL_SEC, yieldComputed);
    assertEquals("Bill Security: discounting method - price", PRICE, priceComputed, TOLERANCE_PRICE);
  }

  @Test
  public void presentValueFromPrice() {
    CurrencyAmount pvComputed = METHOD_SECURITY.presentValueFromPrice(BILL_SEC, PRICE, CURVE_BUNDLE);
    double pvExpected = NOTIONAL * PRICE * CURVE_BUNDLE.getCurve(NAME_CURVES[0]).getDiscountFactor(BILL_SEC.getSettlementTime());
    assertEquals("Bill Security: discounting method - present value", pvExpected, pvComputed.getAmount(), TOLERANCE_PV);
  }

  @Test
  public void presentValueFromYield() {
    CurrencyAmount pvComputed = METHOD_SECURITY.presentValueFromYield(BILL_SEC, YIELD, CURVE_BUNDLE);
    double price = METHOD_SECURITY.priceFromYield(BILL_SEC, YIELD);
    double pvExpected = NOTIONAL * price * CURVE_BUNDLE.getCurve(NAME_CURVES[0]).getDiscountFactor(BILL_SEC.getSettlementTime());
    assertEquals("Bill Security: discounting method - present value", pvExpected, pvComputed.getAmount(), TOLERANCE_PV);
  }

  @Test
  public void priceFromCurves() {
    double priceComputed = METHOD_SECURITY.priceFromCurves(BILL_SEC, CURVE_BUNDLE);
    CurrencyAmount pvComputed = METHOD_SECURITY.presentValue(BILL_SEC, CURVE_BUNDLE);
    double priceExpected = pvComputed.getAmount() / (NOTIONAL * CURVE_BUNDLE.getCurve(NAME_CURVES[0]).getDiscountFactor(BILL_SEC.getSettlementTime()));
    assertEquals("Bill Security: discounting method - price", priceExpected, priceComputed, TOLERANCE_PRICE);
  }

  @Test
  public void yieldFromCurves() {
    double yieldComputed = METHOD_SECURITY.yieldFromCurves(BILL_SEC, CURVE_BUNDLE);
    double priceComputed = METHOD_SECURITY.priceFromCurves(BILL_SEC, CURVE_BUNDLE);
    double yieldExpected = METHOD_SECURITY.yieldFromPrice(BILL_SEC, priceComputed);
    assertEquals("Bill Security: discounting method - yield", yieldExpected, yieldComputed, TOLERANCE_YIELD);
  }

  @Test
  public void yieldFromCurvesMethodVsCalculator() {
    double yieldMethod = METHOD_SECURITY.yieldFromCurves(BILL_SEC, CURVE_BUNDLE);
    double yieldCalculator = YFCC.visit(BILL_SEC, CURVE_BUNDLE);
    assertEquals("Bill Security: discounting method - yield", yieldMethod, yieldCalculator, TOLERANCE_YIELD);
  }

  @Test
  public void presentValueCurveSensitivity() {
    InterestRateCurveSensitivity pvcsComputed = METHOD_SECURITY.presentValueCurveSensitivity(BILL_SEC, CURVE_BUNDLE);
    assertEquals("Bill Security: present value curve sensitivity", 1, pvcsComputed.getSensitivities().size());
    assertEquals("Bill Security: present value curve sensitivity", 1, pvcsComputed.getSensitivities().get(NAME_CURVES[1]).size());
    final double deltaTolerancePrice = 1.0E+2;
    //Testing note: Sensitivity is for a movement of 1. 1E+2 = 0.01 unit for a 1 bp move. 
    final double deltaShift = 1.0E-6;
    // Credit curve sensitivity
    final String bumpedCurveName = "Bumped Curve";
    BillSecurity billBumped = BILL_SEC_DEFINITION.toDerivative(REFERENCE_DATE, NAME_CURVES[0], bumpedCurveName);
    final double[] nodeTimes = new double[] {billBumped.getEndTime()};
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
    InterestRateCurveSensitivity pvcsMethod = METHOD_SECURITY.presentValueCurveSensitivity(BILL_SEC, CURVE_BUNDLE);
    InterestRateCurveSensitivity pvcsCalculator = new InterestRateCurveSensitivity(PVCSC.visit(BILL_SEC, CURVE_BUNDLE));
    assertTrue("Bill Security: discounting method - curve sensitivity", InterestRateCurveSensitivity.compare(pvcsMethod, pvcsCalculator, TOLERANCE_PV));
  }

}
