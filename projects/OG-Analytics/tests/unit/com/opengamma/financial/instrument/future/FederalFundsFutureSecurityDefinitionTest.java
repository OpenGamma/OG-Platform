/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.instrument.future;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertFalse;
import static org.testng.AssertJUnit.assertTrue;

import java.util.ArrayList;
import java.util.List;

import javax.time.calendar.ZonedDateTime;

import org.testng.annotations.Test;

import com.opengamma.financial.convention.businessday.BusinessDayConvention;
import com.opengamma.financial.convention.businessday.BusinessDayConventionFactory;
import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.financial.convention.calendar.MondayToFridayCalendar;
import com.opengamma.financial.instrument.index.IndexON;
import com.opengamma.financial.instrument.index.indexon.EONIA;
import com.opengamma.financial.instrument.index.indexon.FEDFUND;
import com.opengamma.financial.interestrate.future.derivative.FederalFundsFutureSecurity;
import com.opengamma.util.time.DateUtils;
import com.opengamma.util.time.TimeCalculator;

/**
 * Tests related to the construction of Federal Fund future.
 */
public class FederalFundsFutureSecurityDefinitionTest {

  private static final Calendar NYC = new MondayToFridayCalendar("NYC");
  private static final IndexON INDEX_FEDFUND = new FEDFUND(NYC);
  private static final BusinessDayConvention BUSINESS_DAY_PRECEDING = BusinessDayConventionFactory.INSTANCE.getBusinessDayConvention("Preceding");
  private static final BusinessDayConvention BUSINESS_DAY_FOLLOWING = BusinessDayConventionFactory.INSTANCE.getBusinessDayConvention("Following");
  private static final ZonedDateTime MARCH_1 = DateUtils.getUTCDate(2012, 3, 1);
  private static final ZonedDateTime APRIL_1 = DateUtils.getUTCDate(2012, 4, 1);
  private static final ZonedDateTime LAST_TRADING_DATE = BUSINESS_DAY_PRECEDING.adjustDate(NYC, APRIL_1);
  private static final ZonedDateTime PERIOD_FIRST_DATE = BUSINESS_DAY_FOLLOWING.adjustDate(NYC, MARCH_1);
  private static final ZonedDateTime PERIOD_LAST_DATE = BUSINESS_DAY_FOLLOWING.adjustDate(NYC, APRIL_1.minusDays(1));
  private static final List<ZonedDateTime> FIXING_LIST = new ArrayList<ZonedDateTime>();
  private static final ZonedDateTime[] FIXING_DATE;
  static {
    ZonedDateTime date = PERIOD_FIRST_DATE;
    while (!date.isAfter(PERIOD_LAST_DATE)) {
      FIXING_LIST.add(date);
      date = BUSINESS_DAY_FOLLOWING.adjustDate(NYC, date.plusDays(1));
    }
    FIXING_DATE = FIXING_LIST.toArray(new ZonedDateTime[0]);
  }
  private static final double[] FIXING_ACCURAL_FACTOR = new double[FIXING_DATE.length - 1];
  static {
    for (int loopfix = 0; loopfix < FIXING_DATE.length - 1; loopfix++) {
      FIXING_ACCURAL_FACTOR[loopfix] = INDEX_FEDFUND.getDayCount().getDayCountFraction(FIXING_DATE[loopfix], FIXING_DATE[loopfix + 1]);
    }
  }
  private static final double NOTIONAL = 5000000;
  private static final double PAYMENT_ACCURAL_FACTOR = 1.0 / 12.0;
  private static final String NAME = "FFH2";

  private static final FederalFundsFutureSecurityDefinition FUTURE_FEDFUND_DEFINITION = new FederalFundsFutureSecurityDefinition(LAST_TRADING_DATE, INDEX_FEDFUND, FIXING_DATE, FIXING_ACCURAL_FACTOR,
      NOTIONAL, PAYMENT_ACCURAL_FACTOR, NAME);

  private static final ZonedDateTime REFERENCE_DATE = DateUtils.getUTCDate(2012, 1, 30);
  private static final String CURVE_NAME = "OIS";

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void nullLastTrading() {
    new FederalFundsFutureSecurityDefinition(null, INDEX_FEDFUND, FIXING_DATE, FIXING_ACCURAL_FACTOR, NOTIONAL, PAYMENT_ACCURAL_FACTOR, NAME);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void nullIndex() {
    new FederalFundsFutureSecurityDefinition(LAST_TRADING_DATE, null, FIXING_DATE, FIXING_ACCURAL_FACTOR, NOTIONAL, PAYMENT_ACCURAL_FACTOR, NAME);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void nullFixingDate() {
    new FederalFundsFutureSecurityDefinition(LAST_TRADING_DATE, INDEX_FEDFUND, null, FIXING_ACCURAL_FACTOR, NOTIONAL, PAYMENT_ACCURAL_FACTOR, NAME);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void nullFixingAccrual() {
    new FederalFundsFutureSecurityDefinition(LAST_TRADING_DATE, INDEX_FEDFUND, FIXING_DATE, null, NOTIONAL, PAYMENT_ACCURAL_FACTOR, NAME);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void nullName() {
    new FederalFundsFutureSecurityDefinition(LAST_TRADING_DATE, INDEX_FEDFUND, FIXING_DATE, FIXING_ACCURAL_FACTOR, NOTIONAL, PAYMENT_ACCURAL_FACTOR, null);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void fixingLength() {
    new FederalFundsFutureSecurityDefinition(LAST_TRADING_DATE, INDEX_FEDFUND, new ZonedDateTime[] {LAST_TRADING_DATE}, FIXING_ACCURAL_FACTOR, NOTIONAL, PAYMENT_ACCURAL_FACTOR, NAME);
  }

  @Test
  /**
   * Tests the getter methods.
   */
  public void getter() {
    assertEquals("Fed fund future security definition", LAST_TRADING_DATE, FUTURE_FEDFUND_DEFINITION.getLastTradingDate());
    assertEquals("Fed fund future security definition", INDEX_FEDFUND, FUTURE_FEDFUND_DEFINITION.getIndex());
    assertEquals("Fed fund future security definition", FIXING_DATE, FUTURE_FEDFUND_DEFINITION.getFixingPeriodDate());
    assertEquals("Fed fund future security definition", FIXING_ACCURAL_FACTOR, FUTURE_FEDFUND_DEFINITION.getFixingPeriodAccrualFactor());
    assertEquals("Fed fund future security definition", NOTIONAL, FUTURE_FEDFUND_DEFINITION.getNotional());
    assertEquals("Fed fund future security definition", PAYMENT_ACCURAL_FACTOR, FUTURE_FEDFUND_DEFINITION.getPaymentAccrualFactor());
    assertEquals("Fed fund future security definition", NAME, FUTURE_FEDFUND_DEFINITION.getName());
    assertEquals("Fed fund future security definition", INDEX_FEDFUND.getCurrency(), FUTURE_FEDFUND_DEFINITION.getCurrency());
  }

  @Test
  /**
   * Tests the equal and hashCode methods.
   */
  public void equalHash() {
    assertTrue(FUTURE_FEDFUND_DEFINITION.equals(FUTURE_FEDFUND_DEFINITION));
    FederalFundsFutureSecurityDefinition other = new FederalFundsFutureSecurityDefinition(LAST_TRADING_DATE, INDEX_FEDFUND, FIXING_DATE, FIXING_ACCURAL_FACTOR, NOTIONAL, PAYMENT_ACCURAL_FACTOR, NAME);
    assertTrue(FUTURE_FEDFUND_DEFINITION.equals(other));
    assertTrue(FUTURE_FEDFUND_DEFINITION.hashCode() == other.hashCode());
    FederalFundsFutureSecurityDefinition modifiedFuture;
    modifiedFuture = new FederalFundsFutureSecurityDefinition(PERIOD_LAST_DATE, INDEX_FEDFUND, FIXING_DATE, FIXING_ACCURAL_FACTOR, NOTIONAL, PAYMENT_ACCURAL_FACTOR, NAME);
    assertFalse(FUTURE_FEDFUND_DEFINITION.equals(modifiedFuture));
    modifiedFuture = new FederalFundsFutureSecurityDefinition(LAST_TRADING_DATE, new EONIA(NYC), FIXING_DATE, FIXING_ACCURAL_FACTOR, NOTIONAL, PAYMENT_ACCURAL_FACTOR, NAME);
    assertFalse(FUTURE_FEDFUND_DEFINITION.equals(modifiedFuture));
    modifiedFuture = new FederalFundsFutureSecurityDefinition(LAST_TRADING_DATE, INDEX_FEDFUND, FIXING_DATE, FIXING_ACCURAL_FACTOR, NOTIONAL + 1.0, PAYMENT_ACCURAL_FACTOR, NAME);
    assertFalse(FUTURE_FEDFUND_DEFINITION.equals(modifiedFuture));
    modifiedFuture = new FederalFundsFutureSecurityDefinition(LAST_TRADING_DATE, INDEX_FEDFUND, FIXING_DATE, FIXING_ACCURAL_FACTOR, NOTIONAL, 0.25, NAME);
    assertFalse(FUTURE_FEDFUND_DEFINITION.equals(modifiedFuture));
    modifiedFuture = new FederalFundsFutureSecurityDefinition(LAST_TRADING_DATE, INDEX_FEDFUND, FIXING_DATE, FIXING_ACCURAL_FACTOR, NOTIONAL, PAYMENT_ACCURAL_FACTOR, "Wrong");
    assertFalse(FUTURE_FEDFUND_DEFINITION.equals(modifiedFuture));
    assertFalse(FUTURE_FEDFUND_DEFINITION.equals(LAST_TRADING_DATE));
    assertFalse(FUTURE_FEDFUND_DEFINITION.equals(null));
  }

  @Test
  /**
   * Tests the from method
   */
  public void from() {
    FederalFundsFutureSecurityDefinition from = FederalFundsFutureSecurityDefinition.from(MARCH_1, INDEX_FEDFUND, NOTIONAL, PAYMENT_ACCURAL_FACTOR, NAME);
    assertEquals("Fed fund future security definition: builder", FUTURE_FEDFUND_DEFINITION, from);
  }

  @Test
  /**
   * Tests the from method
   */
  public void from2() {
    FederalFundsFutureSecurityDefinition from = FederalFundsFutureSecurityDefinition.from(MARCH_1, INDEX_FEDFUND, NOTIONAL, PAYMENT_ACCURAL_FACTOR, "FFMar12");
    FederalFundsFutureSecurityDefinition fromFF = FederalFundsFutureSecurityDefinition.fromFedFund(MARCH_1, INDEX_FEDFUND);
    assertEquals("Fed fund future security definition: builder", from, fromFF);
  }

  @Test
  /**
   * Tests the toDerivative method before the first fixing date.
   */
  public void toDerivativeNoFixing() {
    double[] fixingPeriodTime = new double[FIXING_DATE.length];
    for (int loopfix = 0; loopfix < FIXING_DATE.length; loopfix++) {
      fixingPeriodTime[loopfix] = TimeCalculator.getTimeBetween(REFERENCE_DATE, FIXING_DATE[loopfix]);
    }
    FederalFundsFutureSecurity futureFedFundExpected = new FederalFundsFutureSecurity(INDEX_FEDFUND, 0.0, fixingPeriodTime, FIXING_ACCURAL_FACTOR, NOTIONAL, PAYMENT_ACCURAL_FACTOR, NAME, CURVE_NAME);
    FederalFundsFutureSecurity futureFedFundConverted = FUTURE_FEDFUND_DEFINITION.toDerivative(REFERENCE_DATE, CURVE_NAME);
    assertEquals("Fed fund future security definition: toDerivative", futureFedFundExpected, futureFedFundConverted);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void toDerivativeAfterStartFixing() {
    FUTURE_FEDFUND_DEFINITION.toDerivative(FIXING_DATE[0].plusDays(1), CURVE_NAME);
  }

}
