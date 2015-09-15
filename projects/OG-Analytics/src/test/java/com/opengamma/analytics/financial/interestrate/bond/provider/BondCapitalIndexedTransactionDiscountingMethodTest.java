/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate.bond.provider;

import static org.testng.AssertJUnit.assertEquals;

import org.testng.annotations.Test;
import org.threeten.bp.Period;
import org.threeten.bp.ZonedDateTime;

import com.opengamma.analytics.financial.instrument.bond.BondCapitalIndexedSecurityDefinition;
import com.opengamma.analytics.financial.instrument.bond.BondCapitalIndexedTransactionDefinition;
import com.opengamma.analytics.financial.instrument.index.IndexPrice;
import com.opengamma.analytics.financial.instrument.inflation.CouponInflationZeroCouponInterpolationGearingDefinition;
import com.opengamma.analytics.financial.interestrate.bond.definition.BondCapitalIndexedSecurity;
import com.opengamma.analytics.financial.interestrate.bond.definition.BondCapitalIndexedTransaction;
import com.opengamma.analytics.financial.interestrate.payments.derivative.Coupon;
import com.opengamma.analytics.financial.provider.calculator.inflation.PresentValueCurveSensitivityDiscountingInflationCalculator;
import com.opengamma.analytics.financial.provider.calculator.inflation.PresentValueDiscountingInflationCalculator;
import com.opengamma.analytics.financial.provider.calculator.inflationissuer.ParSpreadMarketQuoteCurveSensitivityInflationIssuerDiscountingCalculator;
import com.opengamma.analytics.financial.provider.calculator.inflationissuer.ParSpreadMarketQuoteInflationIssuerDiscountingCalculator;
import com.opengamma.analytics.financial.provider.calculator.inflationissuer.PresentValueCurveSensitivityInflationIssuerCalculator;
import com.opengamma.analytics.financial.provider.calculator.inflationissuer.PresentValueInflationIssuerDiscountingCalculator;
import com.opengamma.analytics.financial.provider.description.MulticurveProviderDiscountDataSets;
import com.opengamma.analytics.financial.provider.description.inflation.InflationIssuerProviderDiscount;
import com.opengamma.analytics.financial.provider.description.inflation.ParameterInflationProviderInterface;
import com.opengamma.analytics.financial.provider.sensitivity.inflation.InflationSensitivity;
import com.opengamma.analytics.financial.provider.sensitivity.inflation.MultipleCurrencyInflationSensitivity;
import com.opengamma.analytics.financial.provider.sensitivity.inflation.ParameterSensitivityInflationMulticurveDiscountInterpolatedFDCalculator;
import com.opengamma.analytics.financial.provider.sensitivity.inflation.ParameterSensitivityInflationParameterCalculator;
import com.opengamma.analytics.financial.provider.sensitivity.multicurve.MultipleCurrencyParameterSensitivity;
import com.opengamma.analytics.financial.util.AssertSensitivityObjects;
import com.opengamma.financial.convention.businessday.BusinessDayConvention;
import com.opengamma.financial.convention.businessday.BusinessDayConventions;
import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.financial.convention.calendar.MondayToFridayCalendar;
import com.opengamma.financial.convention.daycount.DayCount;
import com.opengamma.financial.convention.daycount.DayCounts;
import com.opengamma.financial.convention.yield.YieldConvention;
import com.opengamma.financial.convention.yield.YieldConventionFactory;
import com.opengamma.timeseries.DoubleTimeSeries;
import com.opengamma.util.money.Currency;
import com.opengamma.util.money.MultipleCurrencyAmount;
import com.opengamma.util.test.TestGroup;
import com.opengamma.util.time.DateUtils;

/**
 * Tests the present value of Capital inflation indexed bonds.
 */
@Test(groups = TestGroup.UNIT)
public class BondCapitalIndexedTransactionDiscountingMethodTest {

  private static final InflationIssuerProviderDiscount MARKET = MulticurveProviderDiscountDataSets.createMarket1();
  private static final IndexPrice[] PRICE_INDEXES = MulticurveProviderDiscountDataSets.getPriceIndexes();
  private static final IndexPrice PRICE_INDEX_USCPI = PRICE_INDEXES[2];
  private static final String[] ISSUER_NAMES = MulticurveProviderDiscountDataSets.getIssuerNames();
  private static final String ISSUER_US_GOVT = ISSUER_NAMES[0];
  private static final ZonedDateTime PRICING_DATE = DateUtils.getUTCDate(2011, 8, 8);
  private static final Currency USD = Currency.USD;
  
  private static final double SHIFT_FD = 1.0E-7;
  private static final double TOLERANCE_PV = 1.0E-2;
  private static final double TOLERANCE_PV_DELTA = 1.0E+2;
  private static final double TOLERANCE_PRICE = 1.0E-8;
  private static final double TOLERANCE_PRICE_DELTA = 1.0E-6;

  private static final BondCapitalIndexedSecurityDiscountingMethod METHOD_BOND_SECURITY = new BondCapitalIndexedSecurityDiscountingMethod();
  private static final BondCapitalIndexedTransactionDiscountingMethod METHOD_BOND_TRANSACTION = new BondCapitalIndexedTransactionDiscountingMethod();
  private static final PresentValueDiscountingInflationCalculator PVDIC = PresentValueDiscountingInflationCalculator.getInstance();
  private static final PresentValueInflationIssuerDiscountingCalculator PVDIIC = PresentValueInflationIssuerDiscountingCalculator.getInstance();
  private static final PresentValueCurveSensitivityDiscountingInflationCalculator PVCSIC = 
      PresentValueCurveSensitivityDiscountingInflationCalculator.getInstance();
  private static final PresentValueCurveSensitivityInflationIssuerCalculator PVCSIIC =
      PresentValueCurveSensitivityInflationIssuerCalculator.getInstance();
  private static final ParameterSensitivityInflationParameterCalculator<ParameterInflationProviderInterface> PSC = new ParameterSensitivityInflationParameterCalculator<>(PVCSIC);
  private static final ParameterSensitivityInflationMulticurveDiscountInterpolatedFDCalculator PS_PV_FDC = new ParameterSensitivityInflationMulticurveDiscountInterpolatedFDCalculator(PVDIC, SHIFT_FD);
  private static final ParSpreadMarketQuoteInflationIssuerDiscountingCalculator PSMQIIC = 
      ParSpreadMarketQuoteInflationIssuerDiscountingCalculator.getInstance();
  private static final ParSpreadMarketQuoteCurveSensitivityInflationIssuerDiscountingCalculator PSMQCSIIC = 
      ParSpreadMarketQuoteCurveSensitivityInflationIssuerDiscountingCalculator.getInstance();
  
  // 2% 10-YEAR TREASURY INFLATION-PROTECTED SECURITIES (TIPS) Due January 15, 2016 - US912828ET33
  private static final Calendar CALENDAR_USD = new MondayToFridayCalendar("USD");
  private static final BusinessDayConvention BUSINESS_DAY_USD = BusinessDayConventions.FOLLOWING;
  private static final DayCount DAY_COUNT_TIPS_1 = DayCounts.ACT_ACT_ISDA;
  private static final boolean IS_EOM_TIPS_1 = false;
  private static final ZonedDateTime START_DATE_TIPS_1 = DateUtils.getUTCDate(2006, 1, 15);
  private static final ZonedDateTime MATURITY_DATE_TIPS_1 = DateUtils.getUTCDate(2016, 1, 15);
  private static final YieldConvention YIELD_CONVENTION_TIPS_1 = YieldConventionFactory.INSTANCE.getYieldConvention("UK:BUMP/DMO METHOD"); // To check
  private static final int MONTH_LAG_TIPS_1 = 3;
  private static final double INDEX_START_TIPS_1 = 198.47742; // Date:
  private static final double NOTIONAL_TIPS_1 = 100.00;
  private static final double REAL_RATE_TIPS_1 = 0.02;
  private static final Period COUPON_PERIOD_TIPS_1 = Period.ofMonths(6);
  private static final int SETTLEMENT_DAYS_TIPS_1 = 2;

  private static final BondCapitalIndexedSecurityDefinition<CouponInflationZeroCouponInterpolationGearingDefinition> BOND_SECURITY_TIPS_1_DEFINITION = BondCapitalIndexedSecurityDefinition
      .fromInterpolation(PRICE_INDEX_USCPI, MONTH_LAG_TIPS_1, START_DATE_TIPS_1, INDEX_START_TIPS_1, MATURITY_DATE_TIPS_1, COUPON_PERIOD_TIPS_1, NOTIONAL_TIPS_1, REAL_RATE_TIPS_1, BUSINESS_DAY_USD,
          SETTLEMENT_DAYS_TIPS_1, CALENDAR_USD, DAY_COUNT_TIPS_1, YIELD_CONVENTION_TIPS_1, IS_EOM_TIPS_1, ISSUER_US_GOVT);
  private static final DoubleTimeSeries<ZonedDateTime> US_CPI = MulticurveProviderDiscountDataSets.usCpiFrom2009();
  private static final BondCapitalIndexedSecurity<Coupon> BOND_SECURITY_TIPS_1 = BOND_SECURITY_TIPS_1_DEFINITION.toDerivative(PRICING_DATE, US_CPI);

  private static final double QUANTITY_TIPS_1 = 654321;
  private static final ZonedDateTime SETTLE_DATE_TIPS_1 = DateUtils.getUTCDate(2011, 8, 10);
  private static final double PRICE_TIPS_1 = 1.05;
  private static final BondCapitalIndexedTransactionDefinition<CouponInflationZeroCouponInterpolationGearingDefinition> 
    BOND_TIPS_1_TRANSACTION_DEFINITION = new BondCapitalIndexedTransactionDefinition<>(BOND_SECURITY_TIPS_1_DEFINITION, 
        QUANTITY_TIPS_1, SETTLE_DATE_TIPS_1, PRICE_TIPS_1);
  private static final BondCapitalIndexedTransaction<Coupon> BOND_TIPS_1_TRANSACTION = 
      BOND_TIPS_1_TRANSACTION_DEFINITION.toDerivative(PRICING_DATE, US_CPI);

  @Test
  public void presentValueTips1() {
    final MultipleCurrencyAmount pv = METHOD_BOND_TRANSACTION.presentValue(BOND_TIPS_1_TRANSACTION, MARKET);
    final MultipleCurrencyAmount pvSecurity = METHOD_BOND_SECURITY.presentValue(BOND_SECURITY_TIPS_1, MARKET);
    final MultipleCurrencyAmount pvSettlement = BOND_TIPS_1_TRANSACTION.getBondTransaction().getSettlement().
        accept(PVDIC, MARKET.getInflationProvider()).multipliedBy(- BOND_TIPS_1_TRANSACTION.getQuantity() * 
            (PRICE_TIPS_1 + BOND_TIPS_1_TRANSACTION.getBondTransaction().getAccruedInterest() 
                / BOND_TIPS_1_TRANSACTION.getNotionalStandard()));
    assertEquals("Inflation Capital Indexed bond transaction: present value", 
        pvSecurity.multipliedBy(QUANTITY_TIPS_1).plus(pvSettlement).getAmount(BOND_SECURITY_TIPS_1.getCurrency()),
        pv.getAmount(BOND_SECURITY_TIPS_1.getCurrency()), TOLERANCE_PV);
  }

  @Test
  /**
   * Tests the present value Method vs Calculator.
   */
  public void presentValueMethodVsCalculator() {
    final MultipleCurrencyAmount pvMethod = METHOD_BOND_TRANSACTION.presentValue(BOND_TIPS_1_TRANSACTION, MARKET);
    final MultipleCurrencyAmount pvCalculator = BOND_TIPS_1_TRANSACTION.accept(PVDIIC, MARKET);
    assertEquals("Inflation Capital Indexed bond transaction: Method vs Calculator", pvMethod, pvCalculator);
  }

  @Test
  /**
   * Test the present value parameter curves sensitivity.
   */
  public void presentValueParameterCurveSensitivity() {
    final MultipleCurrencyParameterSensitivity pvicsFD = 
        PS_PV_FDC.calculateSensitivity(BOND_TIPS_1_TRANSACTION.getBondTransaction().getCoupon(), MARKET.getInflationProvider());
    final MultipleCurrencyParameterSensitivity pvicsExact = 
        PSC.calculateSensitivity(BOND_TIPS_1_TRANSACTION.getBondTransaction().getCoupon(), MARKET.getInflationProvider(), MARKET.getAllNames());
    AssertSensitivityObjects.assertEquals("Bond capital indexed security: presentValueParameterCurveSensitivity ", 
        pvicsExact, pvicsFD, TOLERANCE_PV_DELTA);

  }

  @Test
  /** Test the present value curves sensitivity. */
  public void presentValueCurveSensitivity() {
    MultipleCurrencyInflationSensitivity pvcisSecurity = METHOD_BOND_SECURITY.presentValueCurveSensitivity(
        BOND_TIPS_1_TRANSACTION.getBondTransaction(), MARKET);
    MultipleCurrencyInflationSensitivity pvcisSettle = 
        BOND_TIPS_1_TRANSACTION.getBondTransaction().getSettlement().accept(PVCSIC, MARKET.getInflationProvider());
    double notional = BOND_TIPS_1_TRANSACTION.getNotionalStandard();
    double dirtyPrice = PRICE_TIPS_1 + BOND_TIPS_1_TRANSACTION.getBondTransaction().getAccruedInterest() / notional;
    MultipleCurrencyInflationSensitivity pvcsiExpected = pvcisSecurity.multipliedBy(QUANTITY_TIPS_1).
        plus(pvcisSettle.multipliedBy(-QUANTITY_TIPS_1 * dirtyPrice)).cleaned();
    MultipleCurrencyInflationSensitivity pvcisComputed = 
        METHOD_BOND_TRANSACTION.presentValueCurveSensitivity(BOND_TIPS_1_TRANSACTION, MARKET).cleaned();
    AssertSensitivityObjects.assertEquals("Bond capital indexed security: presentValueCurveSensitivity ", 
        pvcsiExpected, pvcisComputed, TOLERANCE_PV_DELTA);
  }

  @Test
  /** Test the present value curves sensitivity: method vs Calculator */
  public void presentValueCurveSensitivityMethodVsCalculator() {
    MultipleCurrencyInflationSensitivity pvcisMethod = 
        METHOD_BOND_TRANSACTION.presentValueCurveSensitivity(BOND_TIPS_1_TRANSACTION, MARKET);
    MultipleCurrencyInflationSensitivity pvcisCalculator = BOND_TIPS_1_TRANSACTION.accept(PVCSIIC, MARKET);  
    AssertSensitivityObjects.assertEquals("Bond capital indexed security: presentValueCurveSensitivity ", 
        pvcisMethod, pvcisCalculator, TOLERANCE_PV_DELTA);  
  }

  @Test
  /** Test the par spread when the bond settlement date is in the future. */
  public void parSpreadSettlementFuture() {
    double psComputed = METHOD_BOND_TRANSACTION.parSpread(BOND_TIPS_1_TRANSACTION, MARKET);
    BondCapitalIndexedTransactionDefinition<CouponInflationZeroCouponInterpolationGearingDefinition> 
      transaction0Definition = new BondCapitalIndexedTransactionDefinition<>(BOND_SECURITY_TIPS_1_DEFINITION, 
        QUANTITY_TIPS_1, SETTLE_DATE_TIPS_1, PRICE_TIPS_1 + psComputed);
    BondCapitalIndexedTransaction<Coupon> transaction0 = transaction0Definition.toDerivative(PRICING_DATE, US_CPI);
    MultipleCurrencyAmount pv0 = METHOD_BOND_TRANSACTION.presentValue(transaction0, MARKET);
    assertEquals("BondCapitalIndexedTransactionDiscountingMethod: parSpread", pv0.getAmount(USD), 0.0, TOLERANCE_PV);    
  }

  @Test
  /** Test the par spread when the bond settlement date is today. */
  public void parSpreadSettlementToday() {
    BondCapitalIndexedTransactionDefinition<CouponInflationZeroCouponInterpolationGearingDefinition> 
      transactionTodayDefinition = new BondCapitalIndexedTransactionDefinition<>(BOND_SECURITY_TIPS_1_DEFINITION, 
      QUANTITY_TIPS_1, PRICING_DATE, PRICE_TIPS_1);
    BondCapitalIndexedTransaction<Coupon> transactionToday = 
        transactionTodayDefinition.toDerivative(PRICING_DATE, US_CPI);
    double psComputed = METHOD_BOND_TRANSACTION.parSpread(transactionToday, MARKET);
    BondCapitalIndexedTransactionDefinition<CouponInflationZeroCouponInterpolationGearingDefinition> 
      transaction0Definition = new BondCapitalIndexedTransactionDefinition<>(BOND_SECURITY_TIPS_1_DEFINITION, 
        QUANTITY_TIPS_1, PRICING_DATE, PRICE_TIPS_1 + psComputed);
    BondCapitalIndexedTransaction<Coupon> transaction0 = transaction0Definition.toDerivative(PRICING_DATE, US_CPI);
    MultipleCurrencyAmount pv0 = METHOD_BOND_TRANSACTION.presentValue(transaction0, MARKET);
    assertEquals("BondCapitalIndexedTransactionDiscountingMethod: parSpread", pv0.getAmount(USD), 0.0, TOLERANCE_PV);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  /** Test the par spread when the bond settlement date is in the past.: Should throw an illegal argument exception. */
  public void parSpreadSettlementPast() {
    BondCapitalIndexedTransactionDefinition<CouponInflationZeroCouponInterpolationGearingDefinition> 
      transactionPastDefinition = new BondCapitalIndexedTransactionDefinition<>(BOND_SECURITY_TIPS_1_DEFINITION, 
      QUANTITY_TIPS_1, PRICING_DATE.minusDays(2), PRICE_TIPS_1);
    BondCapitalIndexedTransaction<Coupon> transactionPast = 
        transactionPastDefinition.toDerivative(PRICING_DATE, US_CPI);
    METHOD_BOND_TRANSACTION.parSpread(transactionPast, MARKET); 
  }

  @Test
  /** Test the par spread when the bond settlement date is in the future. */
  public void parSpreadMethodVsCalculator() {
    double psMethod = METHOD_BOND_TRANSACTION.parSpread(BOND_TIPS_1_TRANSACTION, MARKET);
    double psCalculator = BOND_TIPS_1_TRANSACTION.accept(PSMQIIC, MARKET);
    assertEquals("BondCapitalIndexedTransactionDiscountingMethod: parSpread", psMethod, psCalculator, TOLERANCE_PRICE);
  }

  @Test
  /** Test the par spread curve sensitivity when the bond settlement date is in the future. */
  public void parSpreadCurveSensitivitySettlementFuture() {
    double ps = METHOD_BOND_TRANSACTION.parSpread(BOND_TIPS_1_TRANSACTION, MARKET);
    double pvSettle = BOND_TIPS_1_TRANSACTION.getBondTransaction().getSettlement().
        accept(PVDIC, MARKET.getInflationProvider()).getAmount(USD);
    InflationSensitivity pscsComputed = 
        METHOD_BOND_TRANSACTION.parSpreadCurveSensitivity(BOND_TIPS_1_TRANSACTION, MARKET);
    InflationSensitivity pvcsBond = METHOD_BOND_TRANSACTION.presentValueCurveSensitivity(BOND_TIPS_1_TRANSACTION, MARKET).
        getSensitivity(USD).multipliedBy(1.0d/QUANTITY_TIPS_1);
    InflationSensitivity pvcsSettle = BOND_TIPS_1_TRANSACTION.getBondTransaction().getSettlement().
        accept(PVCSIC, MARKET.getInflationProvider()).getSensitivity(USD);
    InflationSensitivity pvcsTotal = pvcsBond.plus(pvcsSettle.multipliedBy(-ps)).
        plus(pscsComputed.multipliedBy(-pvSettle)).cleaned();
    InflationSensitivity pvcsExpected = new InflationSensitivity();
    AssertSensitivityObjects.assertEquals("BondCapitalIndexedTransactionDiscountingMethod: parSpreadCurveSensitivity ", 
        pvcsExpected, pvcsTotal, TOLERANCE_PRICE_DELTA);  
  }
  
}
