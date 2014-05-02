/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate.bond.provider;

import static org.testng.AssertJUnit.assertEquals;

import org.testng.annotations.Test;
import org.threeten.bp.ZonedDateTime;

import com.opengamma.analytics.financial.instrument.bond.BillSecurityDefinition;
import com.opengamma.analytics.financial.instrument.bond.BillTransactionDefinition;
import com.opengamma.analytics.financial.interestrate.bond.definition.BillTransaction;
import com.opengamma.analytics.financial.provider.calculator.issuer.ParSpreadMarketQuoteCurveSensitivityIssuerDiscountingCalculator;
import com.opengamma.analytics.financial.provider.calculator.issuer.ParSpreadMarketQuoteIssuerDiscountingCalculator;
import com.opengamma.analytics.financial.provider.calculator.issuer.ParSpreadRateIssuerDiscountingCalculator;
import com.opengamma.analytics.financial.provider.calculator.issuer.PresentValueCurveSensitivityIssuerCalculator;
import com.opengamma.analytics.financial.provider.calculator.issuer.PresentValueIssuerCalculator;
import com.opengamma.analytics.financial.provider.description.IssuerProviderDiscountDataSets;
import com.opengamma.analytics.financial.provider.description.interestrate.IssuerProviderDiscount;
import com.opengamma.analytics.financial.provider.description.interestrate.ParameterIssuerProviderInterface;
import com.opengamma.analytics.financial.provider.sensitivity.issuer.ParameterSensitivityIssuerCalculator;
import com.opengamma.analytics.financial.provider.sensitivity.issuer.ParameterSensitivityIssuerDiscountInterpolatedFDCalculator;
import com.opengamma.analytics.financial.provider.sensitivity.issuer.SimpleParameterSensitivityIssuerCalculator;
import com.opengamma.analytics.financial.provider.sensitivity.issuer.SimpleParameterSensitivityIssuerDiscountInterpolatedFDCalculator;
import com.opengamma.analytics.financial.provider.sensitivity.multicurve.MulticurveSensitivity;
import com.opengamma.analytics.financial.provider.sensitivity.multicurve.MultipleCurrencyMulticurveSensitivity;
import com.opengamma.analytics.financial.provider.sensitivity.multicurve.MultipleCurrencyParameterSensitivity;
import com.opengamma.analytics.financial.provider.sensitivity.multicurve.SimpleParameterSensitivity;
import com.opengamma.analytics.financial.schedule.ScheduleCalculator;
import com.opengamma.analytics.financial.util.AssertSensitivityObjects;
import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.financial.convention.calendar.MondayToFridayCalendar;
import com.opengamma.financial.convention.daycount.DayCount;
import com.opengamma.financial.convention.daycount.DayCounts;
import com.opengamma.financial.convention.yield.YieldConvention;
import com.opengamma.financial.convention.yield.YieldConventionFactory;
import com.opengamma.util.money.Currency;
import com.opengamma.util.money.MultipleCurrencyAmount;
import com.opengamma.util.test.TestGroup;
import com.opengamma.util.time.DateUtils;

/**
 * Tests related to the pricing of bills transactions by discounting.
 */
@Test(groups = TestGroup.UNIT)
public class BillTransactionDiscountingMethodTest {

  private final static IssuerProviderDiscount ISSUER_MULTICURVE = IssuerProviderDiscountDataSets.getIssuerSpecificProvider();
  private final static String[] ISSUER_NAMES = IssuerProviderDiscountDataSets.getIssuerNames();

  private final static Currency EUR = Currency.EUR;
  private static final Calendar CALENDAR = new MondayToFridayCalendar("TARGET");
  private final static ZonedDateTime REFERENCE_DATE = DateUtils.getUTCDate(2012, 1, 17);

  private static final DayCount ACT360 = DayCounts.ACT_360;
  private static final int SETTLEMENT_DAYS = 2;
  private static final YieldConvention YIELD_CONVENTION = YieldConventionFactory.INSTANCE.getYieldConvention("INTEREST@MTY");

  // ISIN: BE0312677462
  private final static ZonedDateTime END_DATE = DateUtils.getUTCDate(2012, 3, 15);
  private final static double NOTIONAL = 1000;
  private static final double YIELD = 0.00185; // External source

  private final static ZonedDateTime SETTLE_DATE = ScheduleCalculator.getAdjustedDate(REFERENCE_DATE, SETTLEMENT_DAYS, CALENDAR);
  private final static BillSecurityDefinition BILL_SEC_DEFINITION = new BillSecurityDefinition(EUR, END_DATE, NOTIONAL, SETTLEMENT_DAYS, CALENDAR, YIELD_CONVENTION, ACT360, ISSUER_NAMES[1]);
  private final static double QUANTITY = 123456.7;
  private final static BillTransactionDefinition BILL_TRA_DEFINITION = BillTransactionDefinition.fromYield(BILL_SEC_DEFINITION, QUANTITY, SETTLE_DATE, YIELD, CALENDAR);
  private final static BillTransaction BILL_TRA = BILL_TRA_DEFINITION.toDerivative(REFERENCE_DATE);

  private final static BillSecurityDiscountingMethod METHOD_SECURITY = BillSecurityDiscountingMethod.getInstance();
  private final static BillTransactionDiscountingMethod METHOD_TRANSACTION = BillTransactionDiscountingMethod.getInstance();

  private static final double SHIFT_FD = 1.0E-6;

  private final static PresentValueIssuerCalculator PVIC = PresentValueIssuerCalculator.getInstance();
  private final static PresentValueCurveSensitivityIssuerCalculator PVCSIC = PresentValueCurveSensitivityIssuerCalculator.getInstance();
  private static final ParameterSensitivityIssuerCalculator<ParameterIssuerProviderInterface> PS_PVI_C = new ParameterSensitivityIssuerCalculator<ParameterIssuerProviderInterface>(PVCSIC);
  private static final ParameterSensitivityIssuerDiscountInterpolatedFDCalculator PS_PVI_FDC = new ParameterSensitivityIssuerDiscountInterpolatedFDCalculator(PVIC, SHIFT_FD);

  private final static ParSpreadMarketQuoteIssuerDiscountingCalculator PSMQIDC = ParSpreadMarketQuoteIssuerDiscountingCalculator.getInstance();
  private final static ParSpreadRateIssuerDiscountingCalculator PSRIDC = ParSpreadRateIssuerDiscountingCalculator.getInstance();
  private final static ParSpreadMarketQuoteCurveSensitivityIssuerDiscountingCalculator PSMQCSIDC = ParSpreadMarketQuoteCurveSensitivityIssuerDiscountingCalculator.getInstance();
  private static final SimpleParameterSensitivityIssuerCalculator<ParameterIssuerProviderInterface> PS_PSMQ_C = new SimpleParameterSensitivityIssuerCalculator<>(PSMQCSIDC);
  private static final SimpleParameterSensitivityIssuerDiscountInterpolatedFDCalculator PS_PSMQ_FDC = new SimpleParameterSensitivityIssuerDiscountInterpolatedFDCalculator(PSMQIDC, SHIFT_FD);

  private static final double TOLERANCE_PV = 1.0E-2;
  private static final double TOLERANCE_PV_DELTA = 1.0E+2; //Testing note: Sensitivity is for a movement of 1. 1E+2 = 1 cent for a 1 bp move.
  private static final double TOLERANCE_SPREAD = 1.0E-8;
  private static final double TOLERANCE_SPREAD_DELTA = 1.0E-6;

  @Test
  /**
   * Tests the present value against explicit computation.
   */
  public void presentValue() {
    final MultipleCurrencyAmount pvTransactionComputed = METHOD_TRANSACTION.presentValue(BILL_TRA, ISSUER_MULTICURVE);
    MultipleCurrencyAmount pvSecurity = METHOD_SECURITY.presentValue(BILL_TRA.getBillPurchased(), ISSUER_MULTICURVE);
    pvSecurity = pvSecurity.multipliedBy(QUANTITY);
    final double pvSettle = BILL_TRA_DEFINITION.getSettlementAmount() * ISSUER_MULTICURVE.getMulticurveProvider().getDiscountFactor(EUR, BILL_TRA.getBillPurchased().getSettlementTime());
    assertEquals("Bill Security: discounting method - present value", pvSecurity.getAmount(EUR) + pvSettle, pvTransactionComputed.getAmount(EUR), TOLERANCE_PV);
  }

  @Test
  /**
   * Tests the present value against explicit computation.
   */
  public void presentValueFromYield() {
    final MultipleCurrencyAmount pvTransactionComputed = METHOD_TRANSACTION.presentValueFromYield(BILL_TRA, YIELD, ISSUER_MULTICURVE);
    final MultipleCurrencyAmount pvSecurity = METHOD_SECURITY.presentValue(BILL_TRA.getBillStandard(), ISSUER_MULTICURVE);
    final double pvSettle = ISSUER_MULTICURVE.getMulticurveProvider().getDiscountFactor(BILL_TRA.getCurrency(), BILL_TRA.getBillPurchased().getSettlementTime())
        * BILL_TRA.getSettlementAmount();
    final MultipleCurrencyAmount pvTransactionExpected = pvSecurity.plus(MultipleCurrencyAmount.of(BILL_TRA.getCurrency(), pvSettle));
    assertEquals("Bill Security: discounting method - present value from yield", pvTransactionExpected.getAmount(EUR), pvTransactionComputed.getAmount(EUR), TOLERANCE_PV_DELTA);
  }

  @Test
  /**
   * Tests the present value: Method vs Calculator
   */
  public void presentValueMethodVsCalculator() {
    final MultipleCurrencyAmount pvMethod = METHOD_TRANSACTION.presentValue(BILL_TRA, ISSUER_MULTICURVE);
    final MultipleCurrencyAmount pvCalculator = BILL_TRA.accept(PVIC, ISSUER_MULTICURVE);
    assertEquals("Bill Security: discounting method - present value", pvMethod.getAmount(EUR), pvCalculator.getAmount(EUR), TOLERANCE_PV);
  }

  @Test
  /**
   * Tests present value curve sensitivity.
   */
  public void presentValueCurveSensitivity() {
    final MultipleCurrencyParameterSensitivity pvpsDepositExact = PS_PVI_C.calculateSensitivity(BILL_TRA, ISSUER_MULTICURVE, ISSUER_MULTICURVE.getAllNames());
    final MultipleCurrencyParameterSensitivity pvpsDepositFD = PS_PVI_FDC.calculateSensitivity(BILL_TRA, ISSUER_MULTICURVE);
    AssertSensitivityObjects.assertEquals("DepositCounterpartDiscountingMethod: presentValueCurveSensitivity ", pvpsDepositExact, pvpsDepositFD, TOLERANCE_PV_DELTA);
  }

  @Test
  public void presentValueCurveSensitivityMethodVsCalculator() {
    final MultipleCurrencyMulticurveSensitivity pvcsMethod = METHOD_TRANSACTION.presentValueCurveSensitivity(BILL_TRA, ISSUER_MULTICURVE);
    final MultipleCurrencyMulticurveSensitivity pvcsCalculator = BILL_TRA.accept(PVCSIC, ISSUER_MULTICURVE);
    AssertSensitivityObjects.assertEquals("Bill Security: discounting method - curve sensitivity", pvcsMethod, pvcsCalculator, TOLERANCE_PV_DELTA);
  }

  @Test
  /**
   * Tests the par spread.
   */
  public void parSpread() {
    final double spread = METHOD_TRANSACTION.parSpread(BILL_TRA, ISSUER_MULTICURVE);
    final BillTransactionDefinition bill0Definition = BillTransactionDefinition.fromYield(BILL_SEC_DEFINITION, QUANTITY, SETTLE_DATE, YIELD + spread, CALENDAR);
    final BillTransaction bill0 = bill0Definition.toDerivative(REFERENCE_DATE);
    final MultipleCurrencyAmount pv0 = METHOD_TRANSACTION.presentValue(bill0, ISSUER_MULTICURVE);
    assertEquals("Bill Security: discounting method - par spread", 0, pv0.getAmount(EUR), TOLERANCE_PV);
  }

  @Test
  /**
   * Tests the par spread (Method vs Calculator).
   */
  public void parSpreadMethodVsCalculator() {
    final double spreadMethod = METHOD_TRANSACTION.parSpread(BILL_TRA, ISSUER_MULTICURVE);
    final double spreadCalculator = BILL_TRA.accept(PSMQIDC, ISSUER_MULTICURVE);
    assertEquals("Bill Security: discounting method - par spread", spreadMethod, spreadCalculator, TOLERANCE_SPREAD);
  }

  @Test
  /**
   * Tests the par spread (Method vs Calculator).
   */
  public void parSpreadCalculatorVsCalculator2() {
    final double spreadCalculator = BILL_TRA.accept(PSMQIDC, ISSUER_MULTICURVE);
    final double spreadCalculator2 = BILL_TRA.accept(PSRIDC, ISSUER_MULTICURVE);
    assertEquals("Bill Security: discounting method - par spread", spreadCalculator, spreadCalculator2, TOLERANCE_SPREAD);
  }

  @Test
  /**
   * Tests parSpread curve sensitivity.
   */
  public void parSpreadCurveSensitivity() {
    final SimpleParameterSensitivity pspsDepositExact = PS_PSMQ_C.calculateSensitivity(BILL_TRA, ISSUER_MULTICURVE, ISSUER_MULTICURVE.getAllNames());
    final SimpleParameterSensitivity pspsDepositFD = PS_PSMQ_FDC.calculateSensitivity(BILL_TRA, ISSUER_MULTICURVE);
    AssertSensitivityObjects.assertEquals("DepositCounterpartDiscountingMethod: presentValueCurveSensitivity ", pspsDepositExact, pspsDepositFD, TOLERANCE_PV_DELTA);
  }

  @Test
  /**
   * Tests the par spread curve sensitivity  (Method vs Calculator).
   */
  public void parSpreadCurveSensitivityMethodVsCalculator() {
    final MulticurveSensitivity pscsMethod = METHOD_TRANSACTION.parSpreadCurveSensitivity(BILL_TRA, ISSUER_MULTICURVE);
    final MulticurveSensitivity pscsCalculator = BILL_TRA.accept(PSMQCSIDC, ISSUER_MULTICURVE);
    AssertSensitivityObjects.assertEquals("parSpread: curve sensitivity - fwd", pscsMethod, pscsCalculator, TOLERANCE_SPREAD_DELTA);
  }

}
