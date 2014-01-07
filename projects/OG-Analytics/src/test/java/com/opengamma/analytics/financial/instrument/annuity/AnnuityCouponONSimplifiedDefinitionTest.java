/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.instrument.annuity;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertFalse;

import java.util.ArrayList;
import java.util.List;

import org.testng.annotations.Test;
import org.threeten.bp.DayOfWeek;
import org.threeten.bp.Period;
import org.threeten.bp.ZoneOffset;
import org.threeten.bp.ZonedDateTime;

import com.opengamma.analytics.financial.instrument.index.GeneratorSwapFixedON;
import com.opengamma.analytics.financial.instrument.index.IndexON;
import com.opengamma.financial.convention.businessday.BusinessDayConvention;
import com.opengamma.financial.convention.businessday.BusinessDayConventions;
import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.financial.convention.calendar.MondayToFridayCalendar;
import com.opengamma.financial.convention.daycount.DayCount;
import com.opengamma.financial.convention.daycount.DayCounts;
import com.opengamma.timeseries.DoubleTimeSeries;
import com.opengamma.timeseries.precise.zdt.ImmutableZonedDateTimeDoubleTimeSeries;
import com.opengamma.util.money.Currency;
import com.opengamma.util.test.TestGroup;
import com.opengamma.util.time.DateUtils;

/**
 * Test.
 */
@Test(groups = TestGroup.UNIT)
public class AnnuityCouponONSimplifiedDefinitionTest {
  private static final Currency CCY = Currency.EUR;
  private static final Period PAYMENT_PERIOD = Period.ofMonths(6);
  private static final Calendar CALENDAR = new MondayToFridayCalendar("Weekend");
  private static final DayCount DAY_COUNT = DayCounts.ACT_360;
  private static final boolean IS_EOM = true;
  private static final ZonedDateTime SETTLEMENT_DATE = DateUtils.getUTCDate(2012, 2, 1);
  private static final ZonedDateTime MATURITY_DATE = DateUtils.getUTCDate(2022, 2, 1);
  private static final Period MATURITY_TENOR = Period.ofYears(10);
  private static final double NOTIONAL = 100000000;
  private static final IndexON INDEX = new IndexON("O/N", CCY, DAY_COUNT, 1);
  private static final BusinessDayConvention BUSINESS_DAY = BusinessDayConventions.FOLLOWING;
  private static final GeneratorSwapFixedON GENERATOR = new GeneratorSwapFixedON("OIS", INDEX, PAYMENT_PERIOD, DAY_COUNT, BUSINESS_DAY, IS_EOM, 1, CALENDAR);
  private static final boolean IS_PAYER = true;
  private static final AnnuityCouponONSimplifiedDefinition DEFINITION = AnnuityCouponONSimplifiedDefinition.from(SETTLEMENT_DATE, MATURITY_DATE, NOTIONAL, GENERATOR, IS_PAYER);
  private static final DoubleTimeSeries<ZonedDateTime> FIXING_TS;
  private static final ZonedDateTime DATE = DateUtils.getUTCDate(2012, 3, 15);

  static {
    ZonedDateTime date = SETTLEMENT_DATE;
    final List<ZonedDateTime> dates = new ArrayList<>();
    final List<Double> data = new ArrayList<>();
    while (date.isBefore(DATE) || date.equals(DATE)) {
      dates.add(date);
      data.add(Math.random() / 100);
      date = date.plusDays(1);
      if (date.getDayOfWeek().equals(DayOfWeek.SATURDAY)) {
        date = date.plusDays(2);
      }
    }
    FIXING_TS = ImmutableZonedDateTimeDoubleTimeSeries.of(dates, data, ZoneOffset.UTC);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullCoupons() {
    new AnnuityCouponONSimplifiedDefinition(null, GENERATOR.getIndex(), null);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullSettlementDate1() {
    AnnuityCouponONSimplifiedDefinition.from(null, MATURITY_TENOR, NOTIONAL, GENERATOR, IS_PAYER);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullPaymentFrequency() {
    AnnuityCouponONSimplifiedDefinition.from(SETTLEMENT_DATE, (Period) null, NOTIONAL, GENERATOR, IS_PAYER);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullGenerator1() {
    AnnuityCouponONSimplifiedDefinition.from(SETTLEMENT_DATE, MATURITY_TENOR, NOTIONAL, null, IS_PAYER);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullSettlementDate2() {
    AnnuityCouponONSimplifiedDefinition.from(null, MATURITY_DATE, NOTIONAL, GENERATOR, IS_PAYER);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullMaturityDate() {
    AnnuityCouponONSimplifiedDefinition.from(SETTLEMENT_DATE, (ZonedDateTime) null, NOTIONAL, GENERATOR, IS_PAYER);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullGenerator2() {
    AnnuityCouponONSimplifiedDefinition.from(SETTLEMENT_DATE, MATURITY_DATE, NOTIONAL, null, IS_PAYER);
  }

  @SuppressWarnings("deprecation")
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNoIndexTSDeprecated() {
    DEFINITION.toDerivative(DATE, "A", "B");
  }

  @SuppressWarnings("deprecation")
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullDateDeprecated() {
    DEFINITION.toDerivative(null, FIXING_TS, "A", "B");
  }

  @SuppressWarnings("deprecation")
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullIndexTSDeprecated() {
    DEFINITION.toDerivative(DATE, (DoubleTimeSeries<ZonedDateTime>) null, "A", "B");
  }

  @SuppressWarnings("deprecation")
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullNamesDeprecated() {
    DEFINITION.toDerivative(DATE, FIXING_TS, (String[]) null);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNoIndexTS() {
    DEFINITION.toDerivative(DATE);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullDate() {
    DEFINITION.toDerivative(null, FIXING_TS);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullIndexTS() {
    DEFINITION.toDerivative(DATE, (DoubleTimeSeries<ZonedDateTime>) null);
  }

  @Test
  public void testHashCodeAndEquals() {
    AnnuityCouponONSimplifiedDefinition definition = AnnuityCouponONSimplifiedDefinition.from(SETTLEMENT_DATE, MATURITY_DATE, NOTIONAL, GENERATOR, IS_PAYER);
    assertEquals(DEFINITION, definition);
    assertEquals(DEFINITION.hashCode(), definition.hashCode());
    definition = AnnuityCouponONSimplifiedDefinition.from(SETTLEMENT_DATE, MATURITY_TENOR, NOTIONAL, GENERATOR, IS_PAYER);
    assertEquals(DEFINITION, definition);
    assertEquals(DEFINITION.hashCode(), definition.hashCode());
    definition = AnnuityCouponONSimplifiedDefinition.from(SETTLEMENT_DATE.plusDays(1), MATURITY_DATE, NOTIONAL, GENERATOR, IS_PAYER);
    assertFalse(DEFINITION.equals(definition));
    definition = AnnuityCouponONSimplifiedDefinition.from(SETTLEMENT_DATE, MATURITY_DATE.plusDays(1), NOTIONAL, GENERATOR, IS_PAYER);
    assertFalse(DEFINITION.equals(definition));
    definition = AnnuityCouponONSimplifiedDefinition.from(SETTLEMENT_DATE, MATURITY_DATE, NOTIONAL / 2, GENERATOR, IS_PAYER);
    assertFalse(DEFINITION.equals(definition));
    definition = AnnuityCouponONSimplifiedDefinition.from(SETTLEMENT_DATE, MATURITY_DATE, NOTIONAL, new GeneratorSwapFixedON("OIS", INDEX, PAYMENT_PERIOD, DAY_COUNT, BUSINESS_DAY, IS_EOM, 0, CALENDAR),
        IS_PAYER);
    assertFalse(DEFINITION.equals(definition));
    definition = AnnuityCouponONSimplifiedDefinition.from(SETTLEMENT_DATE, MATURITY_DATE, NOTIONAL, GENERATOR, !IS_PAYER);
    assertFalse(DEFINITION.equals(definition));
  }
}
