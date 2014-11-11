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
import com.opengamma.analytics.financial.provider.calculator.inflationissuer.PresentValueCurveSensitivityDiscountingInflationIssuerCalculator;
import com.opengamma.analytics.financial.provider.calculator.inflationissuer.PresentValueDiscountingInflationIssuerCalculator;
import com.opengamma.analytics.financial.provider.description.MulticurveProviderDiscountDataSets;
import com.opengamma.analytics.financial.provider.description.inflation.InflationIssuerProviderDiscount;
import com.opengamma.analytics.financial.provider.description.inflation.ParameterInflationProviderInterface;
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

  private static final double SHIFT_FD = 1.0E-7;
  private static final double TOLERANCE_PV_DELTA = 1.0E+2;

  private static final BondCapitalIndexedSecurityDiscountingMethod METHOD_BOND_SECURITY = new BondCapitalIndexedSecurityDiscountingMethod();
  private static final BondCapitalIndexedTransactionDiscountingMethod METHOD_BOND_TRANSACTION = new BondCapitalIndexedTransactionDiscountingMethod();
  private static final PresentValueDiscountingInflationCalculator PVDIC = PresentValueDiscountingInflationCalculator.getInstance();
  private static final PresentValueDiscountingInflationIssuerCalculator PVDIIC = PresentValueDiscountingInflationIssuerCalculator.getInstance();
  private static final PresentValueCurveSensitivityDiscountingInflationCalculator PVCSIC = 
      PresentValueCurveSensitivityDiscountingInflationCalculator.getInstance();
  private static final PresentValueCurveSensitivityDiscountingInflationIssuerCalculator PVCSIIC =
      PresentValueCurveSensitivityDiscountingInflationIssuerCalculator.getInstance();
  private static final ParameterSensitivityInflationParameterCalculator<ParameterInflationProviderInterface> PSC = new ParameterSensitivityInflationParameterCalculator<>(PVCSIC);
  private static final ParameterSensitivityInflationMulticurveDiscountInterpolatedFDCalculator PS_PV_FDC = new ParameterSensitivityInflationMulticurveDiscountInterpolatedFDCalculator(PVDIC, SHIFT_FD);

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
        pv.getAmount(BOND_SECURITY_TIPS_1.getCurrency()), 1.0E-2);
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
    MultipleCurrencyInflationSensitivity pvcsiExpected = pvcisSecurity.multipliedBy(QUANTITY_TIPS_1).
        plus(pvcisSettle.multipliedBy(QUANTITY_TIPS_1 * -PRICE_TIPS_1));
    MultipleCurrencyInflationSensitivity pvcisComputed = 
        METHOD_BOND_TRANSACTION.presentValueCurveSensitivity(BOND_TIPS_1_TRANSACTION, MARKET);
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
  
}
