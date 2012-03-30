/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.instrument.bond;

import static org.testng.AssertJUnit.assertEquals;

import javax.time.calendar.Period;
import javax.time.calendar.ZonedDateTime;

import org.testng.annotations.Test;

import com.opengamma.analytics.financial.instrument.bond.BondFixedSecurityDefinition;
import com.opengamma.analytics.financial.instrument.bond.BondFixedTransactionDefinition;
import com.opengamma.analytics.financial.interestrate.bond.definition.BondFixedSecurity;
import com.opengamma.analytics.financial.interestrate.bond.definition.BondFixedTransaction;
import com.opengamma.financial.convention.businessday.BusinessDayConvention;
import com.opengamma.financial.convention.businessday.BusinessDayConventionFactory;
import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.financial.convention.calendar.MondayToFridayCalendar;
import com.opengamma.financial.convention.daycount.DayCount;
import com.opengamma.financial.convention.daycount.DayCountFactory;
import com.opengamma.financial.convention.yield.YieldConvention;
import com.opengamma.financial.convention.yield.YieldConventionFactory;
import com.opengamma.util.money.Currency;
import com.opengamma.util.time.DateUtils;

public class BondFixedTransactionDefinitionTest {
  //Issue: Semi-annual 2Y
  private static final Currency CUR = Currency.USD;
  private static final Period PAYMENT_TENOR = Period.ofMonths(6);
  private static final Calendar CALENDAR = new MondayToFridayCalendar("A");
  private static final DayCount DAY_COUNT = DayCountFactory.INSTANCE.getDayCount("Actual/Actual ICMA");//("Actual/Actual ICMA");("Actual/Actual ISDA")
  private static final BusinessDayConvention BUSINESS_DAY = BusinessDayConventionFactory.INSTANCE.getBusinessDayConvention("Following");
  private static final boolean IS_EOM = false;
  private static final Period BOND_TENOR = Period.ofYears(2);
  private static final int SETTLEMENT_DAYS = 2;
  private static final ZonedDateTime START_ACCRUAL_DATE = DateUtils.getUTCDate(2011, 7, 13);
  private static final ZonedDateTime MATURITY_DATE = START_ACCRUAL_DATE.plus(BOND_TENOR);
  private static final double RATE = 0.0325;
  private static final YieldConvention YIELD_CONVENTION = YieldConventionFactory.INSTANCE.getYieldConvention("STREET CONVENTION");
  private static final BondFixedSecurityDefinition BOND_SECURITY_DEFINITION = BondFixedSecurityDefinition.from(CUR, MATURITY_DATE, START_ACCRUAL_DATE, PAYMENT_TENOR, RATE, SETTLEMENT_DAYS, CALENDAR,
      DAY_COUNT, BUSINESS_DAY, YIELD_CONVENTION, IS_EOM);
  // Transaction
  private static final double PRICE = 0.90;
  private static final ZonedDateTime SETTLEMENT_DATE = DateUtils.getUTCDate(2011, 8, 18);
  private static final double QUANTITY = 100000000; //100m
  private static final BondFixedTransactionDefinition BOND_TRANSACTION_DEFINITION = new BondFixedTransactionDefinition(BOND_SECURITY_DEFINITION, QUANTITY, SETTLEMENT_DATE, PRICE);
  // to derivatives: common
  private static final String CREDIT_CURVE_NAME = "Credit";
  private static final String REPO_CURVE_NAME = "Repo";
  private static final String[] CURVES_NAME = {CREDIT_CURVE_NAME, REPO_CURVE_NAME};
  private static final ZonedDateTime REFERENCE_DATE_1 = DateUtils.getUTCDate(2011, 8, 17);

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullUnderlying() {
    new BondFixedTransactionDefinition(null, QUANTITY, SETTLEMENT_DATE, PRICE);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullSettle() {
    new BondFixedTransactionDefinition(BOND_SECURITY_DEFINITION, QUANTITY, null, PRICE);
  }

  @Test
  public void testGetters() {
    assertEquals(PRICE, BOND_TRANSACTION_DEFINITION.getPrice());
    assertEquals(QUANTITY, BOND_TRANSACTION_DEFINITION.getQuantity());
    assertEquals(SETTLEMENT_DATE, BOND_TRANSACTION_DEFINITION.getSettlementDate());
    assertEquals(BOND_SECURITY_DEFINITION, BOND_TRANSACTION_DEFINITION.getUnderlyingBond());
    double expectedAccrued = 0.195652174 * RATE / 2; //36 days out of 184 in Actual/Actual ICMA.
    assertEquals(expectedAccrued, BOND_TRANSACTION_DEFINITION.getAccruedInterestAtSettlement(), 1E-6);
    assertEquals(DateUtils.getUTCDate(2011, 7, 13), BOND_TRANSACTION_DEFINITION.getPreviousAccrualDate());
    assertEquals(DateUtils.getUTCDate(2012, 1, 13), BOND_TRANSACTION_DEFINITION.getNextAccrualDate());
  }

  @Test
  public void toDerivatives() {
    BondFixedSecurity bondSecurityStandard = BOND_SECURITY_DEFINITION.toDerivative(REFERENCE_DATE_1, CURVES_NAME);
    BondFixedTransaction bondTransaction = BOND_TRANSACTION_DEFINITION.toDerivative(REFERENCE_DATE_1, CURVES_NAME);
    assertEquals("Bond transaction: toDerivative", bondSecurityStandard, bondTransaction.getBondStandard());
    BondFixedSecurity bondSecurityPurchase = BOND_SECURITY_DEFINITION.toDerivative(REFERENCE_DATE_1, SETTLEMENT_DATE, CURVES_NAME);
    assertEquals("Bond transaction: toDerivative", bondSecurityPurchase.getAccruedInterest(), bondTransaction.getBondTransaction().getAccruedInterest());
    assertEquals("Bond transaction: toDerivative", bondSecurityPurchase.getCouponPerYear(), bondTransaction.getBondTransaction().getCouponPerYear());
    assertEquals("Bond transaction: toDerivative", bondSecurityPurchase.getYieldConvention(), bondTransaction.getBondTransaction().getYieldConvention());
  }

}
