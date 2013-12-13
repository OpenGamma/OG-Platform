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
import com.opengamma.analytics.financial.legalentity.CreditRating;
import com.opengamma.analytics.financial.legalentity.LegalEntity;
import com.opengamma.analytics.financial.legalentity.Region;
import com.opengamma.analytics.financial.legalentity.Sector;
import com.opengamma.analytics.financial.schedule.ScheduleCalculator;
import com.opengamma.analytics.util.time.TimeCalculator;
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
 * Tests related to the construction of bills security.
 */
@Test(groups = TestGroup.UNIT)
public class BillSecurityDefinitionTest {
  /** The currency */
  private final static Currency EUR = Currency.EUR;
  /** A holiday calendar */
  private static final Calendar CALENDAR = new MondayToFridayCalendar("TARGET");
  /** The day count */
  private static final DayCount ACT360 = DayCounts.ACT_360;
  /** The number of settlement days */
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

  /** A security definition */
  private final static BillSecurityDefinition BILL_SEC_DEFINITION1 = new BillSecurityDefinition(EUR, END_DATE, NOTIONAL, SETTLEMENT_DAYS, CALENDAR, YIELD_CONVENTION, ACT360, ISSUER_BEL_NAME);
  /** A security definition */
  private final static BillSecurityDefinition BILL_SEC_DEFINITION2 = new BillSecurityDefinition(EUR, END_DATE, NOTIONAL, SETTLEMENT_DAYS, CALENDAR, YIELD_CONVENTION, ACT360, ISSUER_BEL);
  /** The pricing date */
  private final static ZonedDateTime REFERENCE_DATE = DateUtils.getUTCDate(2012, 1, 17);

  /**
   * Tests failure for a null currency.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void nullCurrency1() {
    new BillSecurityDefinition(null, END_DATE, NOTIONAL, SETTLEMENT_DAYS, CALENDAR, YIELD_CONVENTION, ACT360, ISSUER_BEL_NAME);
  }

  /**
   * Tests failure for a null maturity.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void nullEndDate1() {
    new BillSecurityDefinition(EUR, null, NOTIONAL, SETTLEMENT_DAYS, CALENDAR, YIELD_CONVENTION, ACT360, ISSUER_BEL_NAME);
  }

  /**
   * Tests failure for a null calendar.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void nullCalendar1() {
    new BillSecurityDefinition(EUR, END_DATE, NOTIONAL, SETTLEMENT_DAYS, null, YIELD_CONVENTION, ACT360, ISSUER_BEL_NAME);
  }

  /**
   * Tests failure for a null yield convention.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void nullYield1() {
    new BillSecurityDefinition(EUR, END_DATE, NOTIONAL, SETTLEMENT_DAYS, CALENDAR, null, ACT360, ISSUER_BEL_NAME);
  }

  /**
   * Tests failure for a null day count.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void nullDayCount1() {
    new BillSecurityDefinition(EUR, END_DATE, NOTIONAL, SETTLEMENT_DAYS, CALENDAR, YIELD_CONVENTION, null, ISSUER_BEL_NAME);
  }

  /**
   * Tests failure for a null issuer name.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void nullIssuerName() {
    new BillSecurityDefinition(EUR, END_DATE, NOTIONAL, SETTLEMENT_DAYS, CALENDAR, YIELD_CONVENTION, ACT360, (String) null);
  }

  /**
   * Tests failure for a null issuer.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void nullIssuer() {
    new BillSecurityDefinition(EUR, END_DATE, NOTIONAL, SETTLEMENT_DAYS, CALENDAR, YIELD_CONVENTION, ACT360, (LegalEntity) null);
  }

  /**
   * Tests failure for a negative notional.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void notionalPositive() {
    new BillSecurityDefinition(EUR, END_DATE, -NOTIONAL, SETTLEMENT_DAYS, CALENDAR, YIELD_CONVENTION, ACT360, ISSUER_BEL_NAME);
  }

  /**
   * Tests failure for a null currency.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void nullCurrency2() {
    new BillSecurityDefinition(null, END_DATE, NOTIONAL, SETTLEMENT_DAYS, CALENDAR, YIELD_CONVENTION, ACT360, ISSUER_BEL);
  }

  /**
   * Tests failure for a null maturity.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void nullEndDate2() {
    new BillSecurityDefinition(EUR, null, NOTIONAL, SETTLEMENT_DAYS, CALENDAR, YIELD_CONVENTION, ACT360, ISSUER_BEL);
  }

  /**
   * Tests failure for a null calendar.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void nullCalendar2() {
    new BillSecurityDefinition(EUR, END_DATE, NOTIONAL, SETTLEMENT_DAYS, null, YIELD_CONVENTION, ACT360, ISSUER_BEL);
  }

  /**
   * Tests failure for a null yield convention.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void nullYield2() {
    new BillSecurityDefinition(EUR, END_DATE, NOTIONAL, SETTLEMENT_DAYS, CALENDAR, null, ACT360, ISSUER_BEL);
  }

  /**
   * Tests failure for a null day count.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void nullDayCount2() {
    new BillSecurityDefinition(EUR, END_DATE, NOTIONAL, SETTLEMENT_DAYS, CALENDAR, YIELD_CONVENTION, null, ISSUER_BEL);
  }

  /**
   * Tests the bill getters.
   */
  @Test
  public void getters() {
    assertEquals("Bill Security Definition: getter", EUR, BILL_SEC_DEFINITION1.getCurrency());
    assertEquals("Bill Security Definition: getter", END_DATE, BILL_SEC_DEFINITION1.getEndDate());
    assertEquals("Bill Security Definition: getter", NOTIONAL, BILL_SEC_DEFINITION1.getNotional());
    assertEquals("Bill Security Definition: getter", SETTLEMENT_DAYS, BILL_SEC_DEFINITION1.getSettlementDays());
    assertEquals("Bill Security Definition: getter", CALENDAR, BILL_SEC_DEFINITION1.getCalendar());
    assertEquals("Bill Security Definition: getter", YIELD_CONVENTION, BILL_SEC_DEFINITION1.getYieldConvention());
    assertEquals("Bill Security Definition: getter", ACT360, BILL_SEC_DEFINITION1.getDayCount());
    assertEquals("Bill Security Definition: getter", new LegalEntity(null, ISSUER_BEL_NAME, null, null, null), BILL_SEC_DEFINITION1.getIssuerEntity());
    assertEquals("Bill Security Definition: getter", ISSUER_BEL_NAME, BILL_SEC_DEFINITION1.getIssuer());
  }

  /**
   * Tests the equal and hash-code methods.
   */
  @Test
  public void equalHash() {
    assertEquals("Bill Security Definition: equal-hash code", BILL_SEC_DEFINITION1, BILL_SEC_DEFINITION1);
    BillSecurityDefinition other = new BillSecurityDefinition(EUR, END_DATE, NOTIONAL, SETTLEMENT_DAYS, CALENDAR, YIELD_CONVENTION, ACT360, ISSUER_BEL_NAME);
    assertEquals("Bill Security Definition: equal-hash code", BILL_SEC_DEFINITION1, other);
    assertEquals("Bill Security Definition: equal-hash code", BILL_SEC_DEFINITION1.hashCode(), other.hashCode());
    BillSecurityDefinition modified;
    modified = new BillSecurityDefinition(Currency.USD, END_DATE, NOTIONAL, SETTLEMENT_DAYS, CALENDAR, YIELD_CONVENTION, ACT360, ISSUER_BEL_NAME);
    assertFalse("Bill Security Definition: equal-hash code", BILL_SEC_DEFINITION1.equals(modified));
    modified = new BillSecurityDefinition(EUR, END_DATE.plusDays(1), NOTIONAL, SETTLEMENT_DAYS, CALENDAR, YIELD_CONVENTION, ACT360, ISSUER_BEL_NAME);
    assertFalse("Bill Security Definition: equal-hash code", BILL_SEC_DEFINITION1.equals(modified));
    modified = new BillSecurityDefinition(EUR, END_DATE, NOTIONAL + 10.0, SETTLEMENT_DAYS, CALENDAR, YIELD_CONVENTION, ACT360, ISSUER_BEL_NAME);
    assertFalse("Bill Security Definition: equal-hash code", BILL_SEC_DEFINITION1.equals(modified));
    modified = new BillSecurityDefinition(EUR, END_DATE, NOTIONAL, SETTLEMENT_DAYS + 1, CALENDAR, YIELD_CONVENTION, ACT360, ISSUER_BEL_NAME);
    assertFalse("Bill Security Definition: equal-hash code", BILL_SEC_DEFINITION1.equals(modified));
    modified = new BillSecurityDefinition(EUR, END_DATE, NOTIONAL, SETTLEMENT_DAYS, new MondayToFridayCalendar("OTHER"), YIELD_CONVENTION, ACT360, ISSUER_BEL_NAME);
    assertFalse("Bill Security Definition: equal-hash code", BILL_SEC_DEFINITION1.equals(modified));
    modified = new BillSecurityDefinition(EUR, END_DATE, NOTIONAL, SETTLEMENT_DAYS, CALENDAR, YieldConventionFactory.INSTANCE.getYieldConvention("DISCOUNT"), ACT360, ISSUER_BEL_NAME);
    assertFalse("Bill Security Definition: equal-hash code", BILL_SEC_DEFINITION1.equals(modified));
    modified = new BillSecurityDefinition(EUR, END_DATE, NOTIONAL, SETTLEMENT_DAYS, CALENDAR, YIELD_CONVENTION, DayCounts.ACT_365, ISSUER_BEL_NAME);
    assertFalse("Bill Security Definition: equal-hash code", BILL_SEC_DEFINITION1.equals(modified));
    modified = new BillSecurityDefinition(EUR, END_DATE, NOTIONAL, SETTLEMENT_DAYS, CALENDAR, YIELD_CONVENTION, ACT360, ISSUER_GER_NAME);
    assertFalse("Bill Security Definition: equal-hash code", BILL_SEC_DEFINITION1.equals(modified));
    other = new BillSecurityDefinition(EUR, END_DATE, NOTIONAL, SETTLEMENT_DAYS, CALENDAR, YIELD_CONVENTION, ACT360, new LegalEntity(null, ISSUER_BEL_NAME, null, null, null));
    assertEquals("Bill Security Definition: equal-hash code", BILL_SEC_DEFINITION1, other);
    assertEquals("Bill Security Definition: equal-hash code", BILL_SEC_DEFINITION1.hashCode(), other.hashCode());
    other = new BillSecurityDefinition(EUR, END_DATE, NOTIONAL, SETTLEMENT_DAYS, CALENDAR, YIELD_CONVENTION, ACT360, ISSUER_BEL);
    assertEquals("Bill Security Definition: equal-hash code", BILL_SEC_DEFINITION2, other);
    assertEquals("Bill Security Definition: equal-hash code", BILL_SEC_DEFINITION2.hashCode(), other.hashCode());
    modified = new BillSecurityDefinition(EUR, END_DATE, NOTIONAL, SETTLEMENT_DAYS, CALENDAR, YIELD_CONVENTION, ACT360, ISSUER_GER);
    assertFalse("Bill Security Definition: equal-hash code", BILL_SEC_DEFINITION1.equals(modified));
  }

  /**
   * Tests the toDerivative methods.
   */
  @SuppressWarnings("deprecation")
  @Test
  public void toDerivativeDeprecated() {
    final String dsc = "EUR Discounting";
    final String credit = "EUR BELGIUM GOVT";
    final ZonedDateTime standardSettlementDate = ScheduleCalculator.getAdjustedDate(REFERENCE_DATE, SETTLEMENT_DAYS, CALENDAR);
    final BillSecurity securityConverted1 = BILL_SEC_DEFINITION1.toDerivative(REFERENCE_DATE, standardSettlementDate, dsc, credit);
    final double standardSettlementTime = TimeCalculator.getTimeBetween(REFERENCE_DATE, standardSettlementDate);
    final double endTime = TimeCalculator.getTimeBetween(REFERENCE_DATE, END_DATE);
    final double accrualFactorStandard = ACT360.getDayCountFraction(standardSettlementDate, END_DATE);
    final BillSecurity securityExpected1 = new BillSecurity(EUR, standardSettlementTime, endTime, NOTIONAL, YIELD_CONVENTION, accrualFactorStandard, ISSUER_BEL_NAME, credit, dsc);
    assertEquals("Bill Security Definition: toDerivative", securityExpected1, securityConverted1);
    final BillSecurity securityConverted2 = BILL_SEC_DEFINITION1.toDerivative(REFERENCE_DATE, dsc, credit);
    assertEquals("Bill Security Definition: toDerivative", securityExpected1, securityConverted2);
    final ZonedDateTime otherSettlementDate = ScheduleCalculator.getAdjustedDate(REFERENCE_DATE, SETTLEMENT_DAYS + 1, CALENDAR);
    final BillSecurity securityConverted3 = BILL_SEC_DEFINITION1.toDerivative(REFERENCE_DATE, otherSettlementDate, dsc, credit);
    final double otherSettlementTime = TimeCalculator.getTimeBetween(REFERENCE_DATE, otherSettlementDate);
    final double accrualFactorOther = ACT360.getDayCountFraction(otherSettlementDate, END_DATE);
    final BillSecurity securityExpected3 = new BillSecurity(EUR, otherSettlementTime, endTime, NOTIONAL, YIELD_CONVENTION, accrualFactorOther, ISSUER_BEL_NAME, credit, dsc);
    assertEquals("Bill Security Definition: toDerivative", securityExpected3, securityConverted3);
  }

  /**
   * Tests the toDerivative methods.
   */
  @Test
  public void toDerivative() {
    final ZonedDateTime standardSettlementDate = ScheduleCalculator.getAdjustedDate(REFERENCE_DATE, SETTLEMENT_DAYS, CALENDAR);
    final BillSecurity securityConverted1 = BILL_SEC_DEFINITION1.toDerivative(REFERENCE_DATE, standardSettlementDate);
    final double standardSettlementTime = TimeCalculator.getTimeBetween(REFERENCE_DATE, standardSettlementDate);
    final double endTime = TimeCalculator.getTimeBetween(REFERENCE_DATE, END_DATE);
    final double accrualFactorStandard = ACT360.getDayCountFraction(standardSettlementDate, END_DATE);
    final BillSecurity securityExpected1 = new BillSecurity(EUR, standardSettlementTime, endTime, NOTIONAL, YIELD_CONVENTION, accrualFactorStandard, ISSUER_BEL_NAME);
    assertEquals("Bill Security Definition: toDerivative", securityExpected1, securityConverted1);
    final BillSecurity securityConverted2 = BILL_SEC_DEFINITION1.toDerivative(REFERENCE_DATE);
    assertEquals("Bill Security Definition: toDerivative", securityExpected1, securityConverted2);
    final ZonedDateTime otherSettlementDate = ScheduleCalculator.getAdjustedDate(REFERENCE_DATE, SETTLEMENT_DAYS + 1, CALENDAR);
    final BillSecurity securityConverted3 = BILL_SEC_DEFINITION1.toDerivative(REFERENCE_DATE, otherSettlementDate);
    final double otherSettlementTime = TimeCalculator.getTimeBetween(REFERENCE_DATE, otherSettlementDate);
    final double accrualFactorOther = ACT360.getDayCountFraction(otherSettlementDate, END_DATE);
    final BillSecurity securityExpected3 = new BillSecurity(EUR, otherSettlementTime, endTime, NOTIONAL, YIELD_CONVENTION, accrualFactorOther, ISSUER_BEL_NAME);
    assertEquals("Bill Security Definition: toDerivative", securityExpected3, securityConverted3);
  }
}
