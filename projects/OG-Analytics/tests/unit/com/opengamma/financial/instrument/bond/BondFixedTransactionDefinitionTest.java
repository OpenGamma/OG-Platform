/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.instrument.bond;

import static org.testng.AssertJUnit.assertEquals;

import javax.time.calendar.Period;
import javax.time.calendar.ZonedDateTime;

import org.testng.annotations.Test;

import com.opengamma.financial.convention.businessday.BusinessDayConvention;
import com.opengamma.financial.convention.businessday.BusinessDayConventionFactory;
import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.financial.convention.calendar.MondayToFridayCalendar;
import com.opengamma.financial.convention.daycount.DayCount;
import com.opengamma.financial.convention.daycount.DayCountFactory;
import com.opengamma.financial.convention.yield.YieldConvention;
import com.opengamma.financial.convention.yield.YieldConventionFactory;
import com.opengamma.util.money.Currency;
import com.opengamma.util.time.DateUtil;

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
  private static final ZonedDateTime START_ACCRUAL_DATE = DateUtil.getUTCDate(2011, 7, 13);
  private static final ZonedDateTime MATURITY_DATE = START_ACCRUAL_DATE.plus(BOND_TENOR);
  private static final double RATE = 0.0325;
  private static final YieldConvention YIELD_CONVENTION = YieldConventionFactory.INSTANCE.getYieldConvention("STREET CONVENTION");
  private static final BondFixedDescriptionDefinition BOND_DESCRIPTION = BondFixedDescriptionDefinition.from(CUR, MATURITY_DATE, START_ACCRUAL_DATE, PAYMENT_TENOR, RATE, SETTLEMENT_DAYS, CALENDAR,
      DAY_COUNT, BUSINESS_DAY, YIELD_CONVENTION, IS_EOM);
  // Transaction
  private static final double PRICE = 0.90;
  private static final ZonedDateTime SETTLEMENT_DATE = DateUtil.getUTCDate(2011, 8, 18);
  private static final double QUANTITY = 100000000; //100m
  private static final BondFixedTransactionDefinition BOND_TRANSACTION = new BondFixedTransactionDefinition(BOND_DESCRIPTION, QUANTITY, SETTLEMENT_DATE, PRICE);

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullUnderlying() {
    new BondFixedTransactionDefinition(null, QUANTITY, SETTLEMENT_DATE, PRICE);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullSettle() {
    new BondFixedTransactionDefinition(BOND_DESCRIPTION, QUANTITY, null, PRICE);
  }

  @Test
  public void testGetters() {
    assertEquals(PRICE, BOND_TRANSACTION.getPrice());
    assertEquals(QUANTITY, BOND_TRANSACTION.getQuantity());
    assertEquals(SETTLEMENT_DATE, BOND_TRANSACTION.getSettlementDate());
    assertEquals(BOND_DESCRIPTION, BOND_TRANSACTION.getUnderlyingBond());
    double expectedAccrued = 0.195652174 * RATE / 2; //36 days out of 184 in Actual/Actual ICMA.
    assertEquals(expectedAccrued, BOND_TRANSACTION.getAccruedInterestAtSettlement(), 1E-6);
    assertEquals(DateUtil.getUTCDate(2011, 7, 13), BOND_TRANSACTION.getPreviousAccrualDate());
    assertEquals(DateUtil.getUTCDate(2012, 1, 13), BOND_TRANSACTION.getNextAccrualDate());
    assertEquals(PRICE * QUANTITY, BOND_TRANSACTION.getPaymentAmount());
  }
}
