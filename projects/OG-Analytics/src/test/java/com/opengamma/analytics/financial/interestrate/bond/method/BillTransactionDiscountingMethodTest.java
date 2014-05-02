/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate.bond.method;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertTrue;

import java.util.List;

import org.testng.AssertJUnit;
import org.testng.annotations.Test;
import org.threeten.bp.ZonedDateTime;

import com.opengamma.analytics.financial.instrument.bond.BillSecurityDefinition;
import com.opengamma.analytics.financial.instrument.bond.BillTransactionDefinition;
import com.opengamma.analytics.financial.interestrate.FDCurveSensitivityCalculator;
import com.opengamma.analytics.financial.interestrate.InterestRateCurveSensitivity;
import com.opengamma.analytics.financial.interestrate.InterestRateCurveSensitivityUtils;
import com.opengamma.analytics.financial.interestrate.ParSpreadMarketQuoteCalculator;
import com.opengamma.analytics.financial.interestrate.ParSpreadMarketQuoteCurveSensitivityCalculator;
import com.opengamma.analytics.financial.interestrate.PresentValueCalculator;
import com.opengamma.analytics.financial.interestrate.PresentValueCurveSensitivityCalculator;
import com.opengamma.analytics.financial.interestrate.TestsDataSetsSABR;
import com.opengamma.analytics.financial.interestrate.YieldCurveBundle;
import com.opengamma.analytics.financial.interestrate.bond.definition.BillTransaction;
import com.opengamma.analytics.financial.interestrate.method.SensitivityFiniteDifference;
import com.opengamma.analytics.financial.schedule.ScheduleCalculator;
import com.opengamma.analytics.financial.util.AssertSensitivityObjects;
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
 * Tests related to the pricing of bills transactions by discounting.
 * @deprecated This class tests deprecated functionality
 */
@Deprecated
@Test(groups = TestGroup.UNIT)
public class BillTransactionDiscountingMethodTest {

  private final static Currency EUR = Currency.EUR;
  private static final Calendar CALENDAR = new MondayToFridayCalendar("TARGET");
  private final static ZonedDateTime REFERENCE_DATE = DateUtils.getUTCDate(2012, 1, 17);

  private static final DayCount ACT360 = DayCounts.ACT_360;
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
  private final static BillTransactionDefinition BILL_TRA_DEFINITION = BillTransactionDefinition.fromYield(BILL_SEC_DEFINITION, QUANTITY, SETTLE_DATE, YIELD, CALENDAR);
  private final static BillTransaction BILL_TRA = BILL_TRA_DEFINITION.toDerivative(REFERENCE_DATE, NAME_CURVES);

  private final static YieldCurveBundle CURVE_BUNDLE = TestsDataSetsSABR.createCurvesBond3();

  private final static BillSecurityDiscountingMethod METHOD_SECURITY = BillSecurityDiscountingMethod.getInstance();
  private final static BillTransactionDiscountingMethod METHOD_TRANSACTION = BillTransactionDiscountingMethod.getInstance();
  private final static PresentValueCalculator PVC = PresentValueCalculator.getInstance();
  private final static PresentValueCurveSensitivityCalculator PVCSC = PresentValueCurveSensitivityCalculator.getInstance();
  private final static ParSpreadMarketQuoteCalculator PSMQC = ParSpreadMarketQuoteCalculator.getInstance();
  private final static ParSpreadMarketQuoteCurveSensitivityCalculator PSMQCSC = ParSpreadMarketQuoteCurveSensitivityCalculator.getInstance();

  private static final double TOLERANCE_PV = 1.0E-2;
  private static final double TOLERANCE_SPREAD = 1.0E-8;
  private static final double TOLERANCE_SPREAD_DELTA = 1.0E-6;

  @Test
  /**
   * Tests the present value against explicit computation.
   */
  public void presentValue() {
    final CurrencyAmount pvTransactionComputed = METHOD_TRANSACTION.presentValue(BILL_TRA, CURVE_BUNDLE);
    CurrencyAmount pvSecurity = METHOD_SECURITY.presentValue(BILL_TRA.getBillPurchased(), CURVE_BUNDLE);
    pvSecurity = pvSecurity.multipliedBy(QUANTITY);
    final double pvSettle = BILL_TRA_DEFINITION.getSettlementAmount() * CURVE_BUNDLE.getCurve(NAME_CURVES[0]).getDiscountFactor(BILL_TRA.getBillPurchased().getSettlementTime());
    assertEquals("Bill Security: discounting method - present value", pvSecurity.plus(pvSettle).getAmount(), pvTransactionComputed.getAmount(), TOLERANCE_PV);
  }

  @Test
  /**
   * Tests the present value: Method vs Calculator
   */
  public void presentValueMethodVsCalculator() {
    final CurrencyAmount pvMethod = METHOD_TRANSACTION.presentValue(BILL_TRA, CURVE_BUNDLE);
    final double pvCalculator = BILL_TRA.accept(PVC, CURVE_BUNDLE);
    assertEquals("Bill Security: discounting method - present value", pvMethod.getAmount(), pvCalculator, TOLERANCE_PV);
  }

  @Test
  public void presentValueCurveSensitivity() {
    final InterestRateCurveSensitivity pvcsComputed = METHOD_TRANSACTION.presentValueCurveSensitivity(BILL_TRA, CURVE_BUNDLE);
    assertEquals("Bill Security: present value curve sensitivity", 2, pvcsComputed.getSensitivities().size());
    assertEquals("Bill Security: present value curve sensitivity", 1, pvcsComputed.getSensitivities().get(NAME_CURVES[0]).size());
    assertEquals("Bill Security: present value curve sensitivity", 1, pvcsComputed.getSensitivities().get(NAME_CURVES[1]).size());
    final double deltaTolerancePrice = 1.0E+2;
    //Testing note: Sensitivity is for a movement of 1. 1E+2 = 0.01 unit for a 1 bp move.
    final double deltaShift = 1.0E-6;
    // Credit curve sensitivity
    final String bumpedCurveName = "Bumped Curve";
    BillTransaction billBumped = BILL_TRA_DEFINITION.toDerivative(REFERENCE_DATE, NAME_CURVES[0], bumpedCurveName);
    final double[] nodeTimesCre = new double[] {billBumped.getBillPurchased().getEndTime() };
    final double[] sensi = SensitivityFiniteDifference.curveSensitivity(billBumped, CURVE_BUNDLE, NAME_CURVES[1], bumpedCurveName, nodeTimesCre, deltaShift, METHOD_TRANSACTION);
    final List<DoublesPair> sensiPv = pvcsComputed.getSensitivities().get(NAME_CURVES[1]);
    for (int loopnode = 0; loopnode < sensi.length; loopnode++) {
      final DoublesPair pairPv = sensiPv.get(loopnode);
      assertEquals("Bill Security: curve sensitivity - Node " + loopnode, nodeTimesCre[loopnode], pairPv.getFirst(), 1E-8);
      AssertJUnit.assertEquals("Bill Security: curve sensitivity", pairPv.second, sensi[loopnode], deltaTolerancePrice);
    }
    // Discounting curve sensitivity
    billBumped = BILL_TRA_DEFINITION.toDerivative(REFERENCE_DATE, bumpedCurveName, NAME_CURVES[1]);
    final double[] nodeTimesDsc = new double[] {billBumped.getBillPurchased().getSettlementTime() };
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
    final InterestRateCurveSensitivity pvcsMethod = METHOD_TRANSACTION.presentValueCurveSensitivity(BILL_TRA, CURVE_BUNDLE);
    final InterestRateCurveSensitivity pvcsCalculator = new InterestRateCurveSensitivity(BILL_TRA.accept(PVCSC, CURVE_BUNDLE));
    AssertSensitivityObjects.assertEquals("Bill Security: discounting method - curve sensitivity", pvcsMethod, pvcsCalculator, TOLERANCE_PV);
  }

  @Test
  /**
   * Tests the par spread.
   */
  public void parSpread() {
    final double spread = METHOD_TRANSACTION.parSpread(BILL_TRA, CURVE_BUNDLE);
    final BillTransactionDefinition bill0Definition = BillTransactionDefinition.fromYield(BILL_SEC_DEFINITION, QUANTITY, SETTLE_DATE, YIELD + spread, CALENDAR);
    final BillTransaction bill0 = bill0Definition.toDerivative(REFERENCE_DATE, NAME_CURVES);
    final CurrencyAmount pv0 = METHOD_TRANSACTION.presentValue(bill0, CURVE_BUNDLE);
    assertEquals("Bill Security: discounting method - par spread", 0, pv0.getAmount(), TOLERANCE_PV);
  }

  @Test
  /**
   * Tests the par spread (Method vs Calculator).
   */
  public void parSpreadMethodVsCalculator() {
    final double spreadMethod = METHOD_TRANSACTION.parSpread(BILL_TRA, CURVE_BUNDLE);
    final double spreadCalculator = BILL_TRA.accept(PSMQC, CURVE_BUNDLE);
    assertEquals("Bill Security: discounting method - par spread", spreadMethod, spreadCalculator, TOLERANCE_SPREAD);
  }

  @Test
  /**
   * Tests the par spread curve sensitivity vs a finite difference calculation.
   */
  public void parSpreadCurveSensitivity() {
    InterestRateCurveSensitivity pscsComputed = METHOD_TRANSACTION.parSpreadCurveSensitivity(BILL_TRA, CURVE_BUNDLE);
    pscsComputed = pscsComputed.cleaned();
    assertEquals("Bill Transaction: par spread curve sensitivity", 2, pscsComputed.getSensitivities().size());
    assertEquals("Bill Transaction: par spread curve sensitivity", 1, pscsComputed.getSensitivities().get(NAME_CURVES[0]).size());
    assertEquals("Bill Transaction: par spread sensitivity", 1, pscsComputed.getSensitivities().get(NAME_CURVES[1]).size());
    //Testing note: Sensitivity is for a movement of 1. 1E+2 = 0.01 unit for a 1 bp move.
    final double deltaShift = 1.0E-6;
    // Credit curve sensitivity
    final String bumpedCurveName = "Bumped Curve";
    final BillTransaction billBumped = BILL_TRA_DEFINITION.toDerivative(REFERENCE_DATE, NAME_CURVES[0], bumpedCurveName);
    final double[] nodeTimesDsc = new double[] {billBumped.getBillPurchased().getSettlementTime() };
    final List<DoublesPair> sensiDscFD = FDCurveSensitivityCalculator.curveSensitvityFDCalculator(BILL_TRA, PSMQC, CURVE_BUNDLE, NAME_CURVES[0], nodeTimesDsc, deltaShift);
    final List<DoublesPair> sensiDscComputed = pscsComputed.getSensitivities().get(NAME_CURVES[0]);
    assertTrue("Bill Transaction: par spread curve sensitivity - dsc", InterestRateCurveSensitivityUtils.compare(sensiDscFD, sensiDscComputed, TOLERANCE_SPREAD_DELTA));
    final double[] nodeTimesCre = new double[] {billBumped.getBillPurchased().getEndTime() };
    final List<DoublesPair> sensiFwdFD = FDCurveSensitivityCalculator.curveSensitvityFDCalculator(BILL_TRA, PSMQC, CURVE_BUNDLE, NAME_CURVES[1], nodeTimesCre, deltaShift);
    final List<DoublesPair> sensiFwdComputed = pscsComputed.getSensitivities().get(NAME_CURVES[1]);
    assertTrue("Bill Transaction: par spread curve sensitivity - fwd", InterestRateCurveSensitivityUtils.compare(sensiFwdFD, sensiFwdComputed, TOLERANCE_SPREAD_DELTA));
  }

  @Test
  /**
   * Tests the par spread curve sensitivity  (Method vs Calculator).
   */
  public void parSpreadCurveSensitivityMethodVsCalculator() {
    final InterestRateCurveSensitivity pscsMethod = METHOD_TRANSACTION.parSpreadCurveSensitivity(BILL_TRA, CURVE_BUNDLE);
    final InterestRateCurveSensitivity pscsCalculator = BILL_TRA.accept(PSMQCSC, CURVE_BUNDLE);
    AssertSensitivityObjects.assertEquals("parSpread: curve sensitivity - fwd", pscsMethod, pscsCalculator, TOLERANCE_SPREAD_DELTA);
  }

}
