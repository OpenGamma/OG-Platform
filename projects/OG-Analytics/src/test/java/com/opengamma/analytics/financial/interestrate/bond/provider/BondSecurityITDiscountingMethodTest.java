/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate.bond.provider;

import static org.testng.AssertJUnit.assertEquals;

import org.testng.annotations.Test;
import org.threeten.bp.Period;
import org.threeten.bp.ZonedDateTime;

import com.opengamma.analytics.financial.instrument.bond.BondFixedSecurityDefinition;
import com.opengamma.analytics.financial.interestrate.bond.definition.BondFixedSecurity;
import com.opengamma.analytics.financial.provider.description.IssuerProviderDiscountDataSets;
import com.opengamma.financial.convention.businessday.BusinessDayConvention;
import com.opengamma.financial.convention.businessday.BusinessDayConventions;
import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.financial.convention.calendar.MondayToFridayCalendar;
import com.opengamma.financial.convention.daycount.DayCount;
import com.opengamma.financial.convention.daycount.DayCounts;
import com.opengamma.financial.convention.yield.YieldConvention;
import com.opengamma.financial.convention.yield.YieldConventionFactory;
import com.opengamma.util.money.Currency;
import com.opengamma.util.test.TestGroup;
import com.opengamma.util.time.DateUtils;

/**
 * Tests related to the discounting method for Italian Government bond security.
 */
@Test(groups = TestGroup.UNIT)
public class BondSecurityITDiscountingMethodTest {

  private static final String[] ISSUER_NAMES = IssuerProviderDiscountDataSets.getIssuerNames();

  // BTPS 1-Sep-2002
  private static final String ISSUER_IT_NAME = ISSUER_NAMES[4];
  private static final Period PAYMENT_TENOR_IT = Period.ofMonths(6);
  private static final Calendar CALENDAR_IT = new MondayToFridayCalendar("A");
  private static final DayCount DAY_COUNT_IT = DayCounts.ACT_ACT_ICMA;
  private static final BusinessDayConvention BUSINESS_DAY_IT = BusinessDayConventions.FOLLOWING;
  private static final boolean IS_EOM_IT = false;
  private static final Period BOND_TENOR_IT = Period.ofYears(3);
  private static final int SETTLEMENT_DAYS_IT = 3;
  private static final int EX_DIVIDEND_DAYS_IT = 0;
  private static final ZonedDateTime START_ACCRUAL_DATE_IT = DateUtils.getUTCDate(1999, 9, 1);
  private static final ZonedDateTime MATURITY_DATE_IT = START_ACCRUAL_DATE_IT.plus(BOND_TENOR_IT);
  private static final double RATE_IT = 0.0375;
  private static final double NOTIONAL_IT = 100;
  private static final YieldConvention YIELD_CONVENTION_IT = YieldConventionFactory.INSTANCE.getYieldConvention("ITALY:TRSY BONDS");
  private static final BondFixedSecurityDefinition BOND_FIXED_SECURITY_DEFINITION_IT = BondFixedSecurityDefinition.from(Currency.EUR, MATURITY_DATE_IT,
      START_ACCRUAL_DATE_IT, PAYMENT_TENOR_IT, RATE_IT, SETTLEMENT_DAYS_IT, NOTIONAL_IT, EX_DIVIDEND_DAYS_IT, CALENDAR_IT, DAY_COUNT_IT, BUSINESS_DAY_IT,
      YIELD_CONVENTION_IT, IS_EOM_IT, ISSUER_IT_NAME, "RepoType");
  private static final ZonedDateTime REFERENCE_DATE_IT = DateUtils.getUTCDate(2000, 9, 5);
  private static final BondFixedSecurity BOND_FIXED_SECURITY_IT = BOND_FIXED_SECURITY_DEFINITION_IT.toDerivative(REFERENCE_DATE_IT);

  private static final BondSecurityDiscountingMethod METHOD_BOND_SECURITY = BondSecurityDiscountingMethod.getInstance();

  private static final double TOLERANCE_PRICE_IT = 1.0E-7;
  private static final double TOLERANCE_YIELD_IT = 1.0E-7;
  private static final double TOLERANCE_CONV = 1.0E-8;
  private static final double TOLERANCE_CONV_FD = 1.0E-5;

  @Test
  public void dirtyPriceFromYieldIT() {
    final double yieldAnnual = 0.05;
    final double dirtyPrice = METHOD_BOND_SECURITY.dirtyPriceFromYield(BOND_FIXED_SECURITY_IT, yieldAnnual);
    final double cleanPriceExpected = 0.977816013;
    final double accruedExpected = 0.0007251;
    assertEquals("Fixed coupon bond security: dirty price from yield IT", accruedExpected, BOND_FIXED_SECURITY_IT.getAccruedInterest() / NOTIONAL_IT, TOLERANCE_PRICE_IT);
    final double dirtyPriceExpected = cleanPriceExpected + accruedExpected;
    assertEquals("Fixed coupon bond security: dirty price from yield IT", dirtyPriceExpected, dirtyPrice, TOLERANCE_PRICE_IT);
  }

  @Test
  public void yieldPriceFromCleanPriceIT() {
    final double cleanPriceExpected = 0.977816013;
    final double accruedExpected = 0.0007251;
    final double dirtyPriceExpected = cleanPriceExpected + accruedExpected;
    final double yieldAnnual = 0.05;
    final double yieldComputedClean = METHOD_BOND_SECURITY.yieldFromCleanPrice(BOND_FIXED_SECURITY_IT, cleanPriceExpected);
    assertEquals("Fixed coupon bond security: dirty price from clean price IT", yieldAnnual, yieldComputedClean, TOLERANCE_YIELD_IT);
    final double yieldComputedDirty = METHOD_BOND_SECURITY.yieldFromDirtyPrice(BOND_FIXED_SECURITY_IT, dirtyPriceExpected);
    assertEquals("Fixed coupon bond security: dirty price from clean price IT", yieldAnnual, yieldComputedDirty, TOLERANCE_YIELD_IT);
  }

  @Test
  public void modifiedDurationFromYieldIT() {
    final double yield = 0.04;
    final double modifiedDuration = METHOD_BOND_SECURITY.modifiedDurationFromYield(BOND_FIXED_SECURITY_IT, yield);
    final double modifiedDurationExpected = 1.8519196700216516; // To be check with another source.
    assertEquals("Fixed coupon bond security: modified duration from yield US Street - hard coded value", modifiedDurationExpected, modifiedDuration, TOLERANCE_PRICE_IT);
    final double shift = 1.0E-6;
    final double dirty = METHOD_BOND_SECURITY.dirtyPriceFromYield(BOND_FIXED_SECURITY_IT, yield);
    final double dirtyP = METHOD_BOND_SECURITY.dirtyPriceFromYield(BOND_FIXED_SECURITY_IT, yield + shift);
    final double dirtyM = METHOD_BOND_SECURITY.dirtyPriceFromYield(BOND_FIXED_SECURITY_IT, yield - shift);
    final double modifiedDurationFD = -(dirtyP - dirtyM) / (2 * shift) / dirty;
    assertEquals("Fixed coupon bond security: modified duration from yield IT - finite difference", modifiedDurationFD, modifiedDuration, TOLERANCE_PRICE_IT);
  }

  @Test
  public void convexityFromYieldIT() {
    final double yield = 0.04;
    final double convexity = METHOD_BOND_SECURITY.convexityFromYield(BOND_FIXED_SECURITY_IT, yield);
    final double convexityExpected = 5.266775524453431; // To be check with another source.
    assertEquals("Fixed coupon bond security: convexity from yield IT - hard coded value", convexityExpected, convexity, TOLERANCE_CONV);
    final double shift = 1.0E-5;
    final double dirty = METHOD_BOND_SECURITY.dirtyPriceFromYield(BOND_FIXED_SECURITY_IT, yield);
    final double dirtyP = METHOD_BOND_SECURITY.dirtyPriceFromYield(BOND_FIXED_SECURITY_IT, yield + shift);
    final double dirtyM = METHOD_BOND_SECURITY.dirtyPriceFromYield(BOND_FIXED_SECURITY_IT, yield - shift);
    final double cvFD = (dirtyP + dirtyM - 2 * dirty) / (shift * shift) / dirty;
    assertEquals("Fixed coupon bond security: convexity from yield IT - finite difference", cvFD, convexity, TOLERANCE_CONV_FD);
  }
}
