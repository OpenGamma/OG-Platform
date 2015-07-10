/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.instrument.bond;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertFalse;

import java.util.Collections;

import org.testng.annotations.Test;
import org.threeten.bp.ZonedDateTime;

import com.opengamma.analytics.financial.interestrate.bond.definition.BillSecurity;
import com.opengamma.analytics.financial.interestrate.bond.definition.BillTransaction;
import com.opengamma.analytics.financial.interestrate.bond.provider.BillSecurityDiscountingMethod;
import com.opengamma.analytics.financial.legalentity.CreditRating;
import com.opengamma.analytics.financial.legalentity.LegalEntity;
import com.opengamma.analytics.financial.legalentity.Region;
import com.opengamma.analytics.financial.legalentity.Sector;
import com.opengamma.analytics.financial.schedule.ScheduleCalculator;
import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.financial.convention.calendar.MondayToFridayCalendar;
import com.opengamma.financial.convention.daycount.DayCount;
import com.opengamma.financial.convention.daycount.DayCounts;
import com.opengamma.financial.convention.yield.YieldConvention;
import com.opengamma.financial.convention.yield.YieldConventionFactory;
import com.opengamma.util.i18n.Country;
import com.opengamma.util.money.Currency;
import com.opengamma.util.test.TestGroup;
import com.opengamma.util.time.DateUtils;

/**
 * Tests related to the construction of bills transaction.
 */
@Test(groups = TestGroup.UNIT)
public class BillTransactionDefinitionTest {
  /** The currency */
  private final static Currency EUR = Currency.EUR;
  /** The holiday calendar */
  private static final Calendar CALENDAR = new MondayToFridayCalendar("TARGET");

  /** The day count */
  private static final DayCount ACT360 = DayCounts.ACT_360;
  /** The settlement days */
  private static final int SETTLEMENT_DAYS = 2;
  /** The yield convention */
  private static final YieldConvention YIELD_CONVENTION = YieldConventionFactory.INSTANCE.getYieldConvention("INTEREST@MTY");

  /** Belgian government name */
  private final static String ISSUER_BEL_NAME = "BELGIUM GOVT";
  /** German government name */
  private final static String ISSUER_GER_NAME = "GERMANY GOVT";
  /** Belgian government entity */
  private final static LegalEntity ISSUER_BEL = new LegalEntity(null, ISSUER_BEL_NAME, Collections.singleton(CreditRating.of("A", "Custom", true)), Sector.of("Government"), Region.of("Belgium", Country.BE, Currency.EUR));
  /** German government entity */
  private final static LegalEntity ISSUER_GER = new LegalEntity(null, ISSUER_GER_NAME, Collections.singleton(CreditRating.of("AA", "Custom", true)), Sector.of("Government"), Region.of("Germany", Country.DE, Currency.EUR));
  /** The maturity */
  private final static ZonedDateTime END_DATE = DateUtils.getUTCDate(2012, 2, 29);
  /** The notional */
  private final static double NOTIONAL = 1000;
  /** The underlying bill */
  private final static BillSecurityDefinition BILL_SEC_DEFINITION = new BillSecurityDefinition(EUR, END_DATE, NOTIONAL, SETTLEMENT_DAYS, CALENDAR, YIELD_CONVENTION, ACT360, ISSUER_BEL);

  /** The quantity */
  private final static double QUANTITY = 123456;
  /** The settlement date */
  private final static ZonedDateTime SETTLE_DATE = DateUtils.getUTCDate(2012, 1, 18);
  /** The settlement amount */
  private final static double SETTLE_AMOUNT = -NOTIONAL * QUANTITY * 99.95;

  /** The discounting curve name */
  private final static String DSC_NAME = "EUR Discounting";
  /** The credit curve name */
  private final static String CREDIT_NAME = "EUR BELGIUM GOVT";

  /** The transaction */
  private final static BillTransactionDefinition BILL_TRA_DEFINITION = new BillTransactionDefinition(BILL_SEC_DEFINITION, QUANTITY, SETTLE_DATE, SETTLE_AMOUNT);
  /** The pricing method */
  private static final BillSecurityDiscountingMethod METHOD_BILL_SECURITY = BillSecurityDiscountingMethod.getInstance();

  /**
   * Tests failure for a null underlying.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void nullUnderlying() {
    new BillTransactionDefinition(null, QUANTITY, SETTLE_DATE, SETTLE_AMOUNT);
  }

  /**
   * Tests failure for a null settlement date.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void nullSettleDate() {
    new BillTransactionDefinition(BILL_SEC_DEFINITION, QUANTITY, null, SETTLE_AMOUNT);
  }

  /**
   * Tests failure for a negative settle amount
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void wrongSettleAmount() {
    new BillTransactionDefinition(BILL_SEC_DEFINITION, QUANTITY, SETTLE_DATE, -SETTLE_AMOUNT);
  }

  /**
   * Tests the bill getters.
   */
  @Test
  public void getters() {
    assertEquals("Bill Transaction Definition: getter", BILL_SEC_DEFINITION, BILL_TRA_DEFINITION.getUnderlying());
    assertEquals("Bill Transaction Definition: getter", QUANTITY, BILL_TRA_DEFINITION.getQuantity());
    assertEquals("Bill Transaction Definition: getter", SETTLE_DATE, BILL_TRA_DEFINITION.getSettlementDate());
    assertEquals("Bill Transaction Definition: getter", SETTLE_AMOUNT, BILL_TRA_DEFINITION.getSettlementAmount());
  }

  /**
   * Tests the constructor from yield.
   */
  @Test
  public void fromYield() {
    final double yield = 0.0020;
    final double accrualFactor = ACT360.getDayCountFraction(SETTLE_DATE, END_DATE);
    final double price = METHOD_BILL_SECURITY.priceFromYield(YIELD_CONVENTION, yield, accrualFactor);
    final double settlementAmount = -QUANTITY * price * NOTIONAL;
    final BillTransactionDefinition from = BillTransactionDefinition.fromYield(BILL_SEC_DEFINITION, QUANTITY, SETTLE_DATE, yield, CALENDAR);
    final BillTransactionDefinition constructed = new BillTransactionDefinition(BILL_SEC_DEFINITION, QUANTITY, SETTLE_DATE, settlementAmount);
    assertEquals("Bill Transaction Definition: fromYield", constructed, from);
  }

  /**
   * Tests the equal and hash-code methods.
   */
  @Test
  public void equalHash() {
    assertEquals("Bill Transaction Definition: equal-hash code", BILL_TRA_DEFINITION, BILL_TRA_DEFINITION);
    final BillTransactionDefinition other = new BillTransactionDefinition(BILL_SEC_DEFINITION, QUANTITY, SETTLE_DATE, SETTLE_AMOUNT);
    assertEquals("Bill Transaction Definition: equal-hash code", BILL_TRA_DEFINITION, other);
    assertEquals("Bill Transaction Definition: equal-hash code", BILL_TRA_DEFINITION.hashCode(), other.hashCode());
    BillTransactionDefinition modified;
    modified = new BillTransactionDefinition(new BillSecurityDefinition(EUR, END_DATE, NOTIONAL, SETTLEMENT_DAYS, CALENDAR, YIELD_CONVENTION, ACT360, ISSUER_GER), QUANTITY, SETTLE_DATE, SETTLE_AMOUNT);
    assertFalse("Bill Security Definition: equal-hash code", BILL_SEC_DEFINITION.equals(modified));
    modified = new BillTransactionDefinition(BILL_SEC_DEFINITION, QUANTITY + 10.0, SETTLE_DATE, SETTLE_AMOUNT);
    assertFalse("Bill Security Definition: equal-hash code", BILL_SEC_DEFINITION.equals(modified));
    modified = new BillTransactionDefinition(BILL_SEC_DEFINITION, QUANTITY, SETTLE_DATE.plusDays(1), SETTLE_AMOUNT);
    assertFalse("Bill Security Definition: equal-hash code", BILL_SEC_DEFINITION.equals(modified));
    modified = new BillTransactionDefinition(BILL_SEC_DEFINITION, QUANTITY, SETTLE_DATE, SETTLE_AMOUNT + 1.0);
    assertFalse("Bill Security Definition: equal-hash code", BILL_SEC_DEFINITION.equals(modified));
  }

  /**
   * Tests the toDerivative methods.
   */
  @Test
  public void toDerivativeStandard() {
    final ZonedDateTime referenceDateStandard = ScheduleCalculator.getAdjustedDate(SETTLE_DATE, -SETTLEMENT_DAYS, CALENDAR);
    final BillTransaction transactionConverted1 = BILL_TRA_DEFINITION.toDerivative(referenceDateStandard);
    final BillSecurity purchased1 = BILL_SEC_DEFINITION.toDerivative(referenceDateStandard, SETTLE_DATE);
    final BillSecurity standard1 = BILL_SEC_DEFINITION.toDerivative(referenceDateStandard);
    final BillTransaction transactionExpected1 = new BillTransaction(purchased1, QUANTITY, SETTLE_AMOUNT, standard1);
    assertEquals("Bill Transaction: toDerivatives", transactionExpected1, transactionConverted1);
  }

  /**
   * Tests the toDerivative methods.
   */
  @Test
  public void toDerivativeEarly() {
    final ZonedDateTime referenceDateEarly = ScheduleCalculator.getAdjustedDate(SETTLE_DATE, -(SETTLEMENT_DAYS + 1), CALENDAR);
    final BillTransaction transactionConverted2 = BILL_TRA_DEFINITION.toDerivative(referenceDateEarly);
    final BillSecurity purchased2 = BILL_SEC_DEFINITION.toDerivative(referenceDateEarly, SETTLE_DATE);
    final BillSecurity standard2 = BILL_SEC_DEFINITION.toDerivative(referenceDateEarly);
    final BillTransaction transactionExpected2 = new BillTransaction(purchased2, QUANTITY, SETTLE_AMOUNT, standard2);
    assertEquals("Bill Transaction: toDerivatives", transactionExpected2, transactionConverted2);
  }

  /**
   * Tests the toDerivative methods.
   */
  @Test
  public void toDerivativeAtSettle() {
    final ZonedDateTime referenceDate = SETTLE_DATE;
    final BillTransaction transactionConverted = BILL_TRA_DEFINITION.toDerivative(referenceDate);
    final BillSecurity purchased = BILL_SEC_DEFINITION.toDerivative(referenceDate, SETTLE_DATE);
    final BillSecurity standard = BILL_SEC_DEFINITION.toDerivative(referenceDate);
    final BillTransaction transactionExpected = new BillTransaction(purchased, QUANTITY, SETTLE_AMOUNT, standard);
    assertEquals("Bill Transaction: toDerivatives", transactionExpected, transactionConverted);
  }

  /**
   * Tests the toDerivative methods.
   */
  @Test
  public void toDerivativeBetweenSettleAndMaturity() {
    final ZonedDateTime referenceDate = ScheduleCalculator.getAdjustedDate(SETTLE_DATE, SETTLEMENT_DAYS, CALENDAR);
    final BillTransaction transactionConverted = BILL_TRA_DEFINITION.toDerivative(referenceDate);
    final BillSecurity purchased = BILL_SEC_DEFINITION.toDerivative(referenceDate, SETTLE_DATE);
    final BillSecurity standard = BILL_SEC_DEFINITION.toDerivative(referenceDate);
    final BillTransaction transactionExpected = new BillTransaction(purchased, QUANTITY, 0.0, standard);
    assertEquals("Bill Transaction: toDerivatives", transactionExpected, transactionConverted);
  }

  /**
   * Tests the toDerivative methods.
   */
  @Test
  public void toDerivativeBetweenJustBeforeMaturity() {
    final ZonedDateTime referenceDate = ScheduleCalculator.getAdjustedDate(END_DATE, -1, CALENDAR);
    final BillTransaction transactionConverted = BILL_TRA_DEFINITION.toDerivative(referenceDate);
    final BillSecurity purchased = BILL_SEC_DEFINITION.toDerivative(referenceDate, SETTLE_DATE);
    final BillSecurity standard = BILL_SEC_DEFINITION.toDerivative(referenceDate);
    final BillTransaction transactionExpected = new BillTransaction(purchased, QUANTITY, 0.0, standard);
    assertEquals("Bill Transaction: toDerivatives", transactionExpected, transactionConverted);
  }

  /**
   * Tests the toDerivative methods.
   */
  @Test
  public void toDerivativeBetweenAtMaturity() {
    final ZonedDateTime referenceDate = END_DATE;
    final BillTransaction transactionConverted = BILL_TRA_DEFINITION.toDerivative(referenceDate);
    final BillSecurity purchased = BILL_SEC_DEFINITION.toDerivative(referenceDate, SETTLE_DATE);
    final BillSecurity standard = BILL_SEC_DEFINITION.toDerivative(referenceDate);
    final BillTransaction transactionExpected = new BillTransaction(purchased, QUANTITY, 0.0, standard);
    assertEquals("Bill Transaction: toDerivatives", transactionExpected, transactionConverted);
  }
}
