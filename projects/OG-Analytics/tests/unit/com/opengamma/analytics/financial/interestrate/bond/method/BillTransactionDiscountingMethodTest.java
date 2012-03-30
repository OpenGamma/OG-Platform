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
import com.opengamma.analytics.financial.instrument.bond.BillTransactionDefinition;
import com.opengamma.analytics.financial.interestrate.InterestRateCurveSensitivity;
import com.opengamma.analytics.financial.interestrate.PresentValueCalculator;
import com.opengamma.analytics.financial.interestrate.PresentValueCurveSensitivityCalculator;
import com.opengamma.analytics.financial.interestrate.TestsDataSetsSABR;
import com.opengamma.analytics.financial.interestrate.YieldCurveBundle;
import com.opengamma.analytics.financial.interestrate.bond.definition.BillTransaction;
import com.opengamma.analytics.financial.interestrate.bond.method.BillSecurityDiscountingMethod;
import com.opengamma.analytics.financial.interestrate.bond.method.BillTransactionDiscountingMethod;
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
 * Tests related to the pricing of bills transactions by discounting.
 */
public class BillTransactionDiscountingMethodTest {

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
  //  private static final double PRICE = 0.99971; // External source

  private final static ZonedDateTime SETTLE_DATE = ScheduleCalculator.getAdjustedDate(REFERENCE_DATE, SETTLEMENT_DAYS, CALENDAR);
  private final static String[] NAME_CURVES = TestsDataSetsSABR.nameCurvesBond3();
  private final static BillSecurityDefinition BILL_SEC_DEFINITION = new BillSecurityDefinition(EUR, END_DATE, NOTIONAL, SETTLEMENT_DAYS, CALENDAR, YIELD_CONVENTION, ACT360, ISSUER_BEL);
  private final static double QUANTITY = 123456.7;
  private final static BillTransactionDefinition BILL_TRA_DEFINITION = BillTransactionDefinition.fromYield(BILL_SEC_DEFINITION, QUANTITY, SETTLE_DATE, YIELD);
  private final static BillTransaction BILL_TRA = BILL_TRA_DEFINITION.toDerivative(REFERENCE_DATE, NAME_CURVES);

  private final static YieldCurveBundle CURVE_BUNDLE = TestsDataSetsSABR.createCurvesBond3();

  private final static BillSecurityDiscountingMethod METHOD_SECURITY = BillSecurityDiscountingMethod.getInstance();
  private final static BillTransactionDiscountingMethod METHOD_TRANSACTION = BillTransactionDiscountingMethod.getInstance();
  private final static PresentValueCalculator PVC = PresentValueCalculator.getInstance();
  private final static PresentValueCurveSensitivityCalculator PVCSC = PresentValueCurveSensitivityCalculator.getInstance();

  private static final double TOLERANCE_PV = 1.0E-2;

  @Test
  /**
   * Tests the present value against explicit computation.
   */
  public void presentValue() {
    CurrencyAmount pvTransactionComputed = METHOD_TRANSACTION.presentValue(BILL_TRA, CURVE_BUNDLE);
    CurrencyAmount pvSecurity = METHOD_SECURITY.presentValue(BILL_TRA.getBillPurchased(), CURVE_BUNDLE);
    pvSecurity = pvSecurity.multipliedBy(QUANTITY);
    double pvSettle = BILL_TRA_DEFINITION.getSettlementAmount() * CURVE_BUNDLE.getCurve(NAME_CURVES[0]).getDiscountFactor(BILL_TRA.getBillPurchased().getSettlementTime());
    assertEquals("Bill Security: discounting method - present value", pvSecurity.plus(pvSettle).getAmount(), pvTransactionComputed.getAmount(), TOLERANCE_PV);
  }

  @Test
  /**
   * Tests the present value: Method vs Calculator
   */
  public void presentValueMethodVsCalculator() {
    CurrencyAmount pvMethod = METHOD_TRANSACTION.presentValue(BILL_TRA, CURVE_BUNDLE);
    double pvCalculator = PVC.visit(BILL_TRA, CURVE_BUNDLE);
    assertEquals("Bill Security: discounting method - present value", pvMethod.getAmount(), pvCalculator, TOLERANCE_PV);
  }

  @Test
  public void presentValueCurveSensitivity() {
    InterestRateCurveSensitivity pvcsComputed = METHOD_TRANSACTION.presentValueCurveSensitivity(BILL_TRA, CURVE_BUNDLE);
    assertEquals("Bill Security: present value curve sensitivity", 2, pvcsComputed.getSensitivities().size());
    assertEquals("Bill Security: present value curve sensitivity", 1, pvcsComputed.getSensitivities().get(NAME_CURVES[0]).size());
    assertEquals("Bill Security: present value curve sensitivity", 1, pvcsComputed.getSensitivities().get(NAME_CURVES[1]).size());
    final double deltaTolerancePrice = 1.0E+2;
    //Testing note: Sensitivity is for a movement of 1. 1E+2 = 0.01 unit for a 1 bp move. 
    final double deltaShift = 1.0E-6;
    // Credit curve sensitivity
    final String bumpedCurveName = "Bumped Curve";
    BillTransaction billBumped = BILL_TRA_DEFINITION.toDerivative(REFERENCE_DATE, NAME_CURVES[0], bumpedCurveName);
    final double[] nodeTimesCre = new double[] {billBumped.getBillPurchased().getEndTime()};
    final double[] sensi = SensitivityFiniteDifference.curveSensitivity(billBumped, CURVE_BUNDLE, NAME_CURVES[1], bumpedCurveName, nodeTimesCre, deltaShift, METHOD_TRANSACTION);
    final List<DoublesPair> sensiPv = pvcsComputed.getSensitivities().get(NAME_CURVES[1]);
    for (int loopnode = 0; loopnode < sensi.length; loopnode++) {
      final DoublesPair pairPv = sensiPv.get(loopnode);
      assertEquals("Bill Security: curve sensitivity - Node " + loopnode, nodeTimesCre[loopnode], pairPv.getFirst(), 1E-8);
      AssertJUnit.assertEquals("Bill Security: curve sensitivity", pairPv.second, sensi[loopnode], deltaTolerancePrice);
    }
    // Discounting curve sensitivity
    billBumped = BILL_TRA_DEFINITION.toDerivative(REFERENCE_DATE, bumpedCurveName, NAME_CURVES[1]);
    final double[] nodeTimesDsc = new double[] {billBumped.getBillPurchased().getSettlementTime()};
    final double[] sensiDsc = SensitivityFiniteDifference.curveSensitivity(billBumped, CURVE_BUNDLE, NAME_CURVES[0], bumpedCurveName, nodeTimesDsc, deltaShift, METHOD_TRANSACTION);
    final List<DoublesPair> sensiDscPv = pvcsComputed.getSensitivities().get(NAME_CURVES[0]);
    for (int loopnode = 0; loopnode < sensiDsc.length; loopnode++) {
      final DoublesPair pairPv = sensiDscPv.get(loopnode);
      assertEquals("Bill Security: curve sensitivity - Node " + loopnode, nodeTimesDsc[loopnode], pairPv.getFirst(), 1E-8);
      AssertJUnit.assertEquals("Bill Security: curve sensitivity", pairPv.second, sensiDsc[loopnode], deltaTolerancePrice);
    }
  }

  @Test
  public void presentValueCurveSensitivityMethodVsCalculator() {
    InterestRateCurveSensitivity pvcsMethod = METHOD_TRANSACTION.presentValueCurveSensitivity(BILL_TRA, CURVE_BUNDLE);
    InterestRateCurveSensitivity pvcsCalculator = new InterestRateCurveSensitivity(PVCSC.visit(BILL_TRA, CURVE_BUNDLE));
    assertTrue("Bill Security: discounting method - curve sensitivity", InterestRateCurveSensitivity.compare(pvcsMethod, pvcsCalculator, TOLERANCE_PV));
  }

}
