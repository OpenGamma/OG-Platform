/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.instrument.future;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertFalse;
import static org.testng.AssertJUnit.assertTrue;

import com.opengamma.financial.convention.businessday.BusinessDayConvention;
import com.opengamma.financial.convention.businessday.BusinessDayConventionFactory;
import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.financial.convention.calendar.MondayToFridayCalendar;
import com.opengamma.financial.convention.daycount.DayCount;
import com.opengamma.financial.convention.daycount.DayCountFactory;
import com.opengamma.financial.instrument.index.IborIndex;
import com.opengamma.financial.interestrate.future.definition.InterestRateFuture;
import com.opengamma.financial.interestrate.future.definition.InterestRateFutureOptionPremiumSecurity;
import com.opengamma.financial.schedule.ScheduleCalculator;
import com.opengamma.util.money.Currency;
import com.opengamma.util.time.DateUtils;

import javax.time.calendar.Period;
import javax.time.calendar.ZonedDateTime;

import org.testng.annotations.Test;

/**
 * Tests the interest rate future option with initial premium security description.
 */
public class InterestRateFutureOptionPremiumSecurityDefinitionTest {
  //EURIBOR 3M Index
  private static final Period TENOR = Period.ofMonths(3);
  private static final int SETTLEMENT_DAYS = 2;
  private static final Calendar CALENDAR = new MondayToFridayCalendar("A");
  private static final DayCount DAY_COUNT_INDEX = DayCountFactory.INSTANCE.getDayCount("Actual/360");
  private static final BusinessDayConvention BUSINESS_DAY = BusinessDayConventionFactory.INSTANCE.getBusinessDayConvention("Modified Following");
  private static final boolean IS_EOM = true;
  private static final Currency CUR = Currency.USD;
  private static final IborIndex IBOR_INDEX = new IborIndex(CUR, TENOR, SETTLEMENT_DAYS, CALENDAR, DAY_COUNT_INDEX, BUSINESS_DAY, IS_EOM);
  // Future option mid-curve 1Y
  private static final ZonedDateTime SPOT_LAST_TRADING_DATE = DateUtils.getUTCDate(2012, 9, 19);
  private static final ZonedDateTime LAST_TRADING_DATE = ScheduleCalculator.getAdjustedDate(SPOT_LAST_TRADING_DATE, -SETTLEMENT_DAYS, CALENDAR);
  private static final double NOTIONAL = 1000000.0; // 1m
  private static final double FUTURE_FACTOR = 0.25;
  private static final double REFERENCE_PRICE = 0.0; // TODO - CASE - Future refactor - 0.0 Refence Price here
  private static final String NAME = "EDU2";
  private static final InterestRateFutureDefinition EDU2_DEFINITION = new InterestRateFutureDefinition(LAST_TRADING_DATE, IBOR_INDEX, REFERENCE_PRICE, NOTIONAL, FUTURE_FACTOR, NAME);
  private static final ZonedDateTime EXPIRATION_DATE = DateUtils.getUTCDate(2011, 9, 16);
  private static final double STRIKE = 0.9895;
  private static final boolean IS_CALL = true;
  private static final InterestRateFutureOptionPremiumSecurityDefinition OPTION_EDU2_DEFINITION = new InterestRateFutureOptionPremiumSecurityDefinition(EDU2_DEFINITION, EXPIRATION_DATE, STRIKE,
      IS_CALL);
  // Derivative
  private static final ZonedDateTime REFERENCE_DATE = DateUtils.getUTCDate(2010, 8, 18);
  private static final DayCount ACT_ACT = DayCountFactory.INSTANCE.getDayCount("Actual/Actual ISDA");
  private static final String DISCOUNTING_CURVE_NAME = "Funding";
  private static final String FORWARD_CURVE_NAME = "Forward";
  private static final String[] CURVES = {DISCOUNTING_CURVE_NAME, FORWARD_CURVE_NAME };

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullUnderlying() {
    new InterestRateFutureOptionPremiumSecurityDefinition(null, EXPIRATION_DATE, STRIKE, IS_CALL);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullExpiration() {
    new InterestRateFutureOptionPremiumSecurityDefinition(EDU2_DEFINITION, null, STRIKE, IS_CALL);
  }

  @Test
  /**
   * Tests the class getters.
   */
  public void getter() {
    assertEquals(EDU2_DEFINITION, OPTION_EDU2_DEFINITION.getUnderlyingFuture());
    assertEquals(EXPIRATION_DATE, OPTION_EDU2_DEFINITION.getExpirationDate());
    assertEquals(STRIKE, OPTION_EDU2_DEFINITION.getStrike());
    assertEquals(IS_CALL, OPTION_EDU2_DEFINITION.isCall());
  }

  @Test
  /**
   * Tests the equal and hashCode methods.
   */
  public void equalHash() {
    InterestRateFutureOptionPremiumSecurityDefinition other = new InterestRateFutureOptionPremiumSecurityDefinition(EDU2_DEFINITION, EXPIRATION_DATE, STRIKE, IS_CALL);
    assertTrue(OPTION_EDU2_DEFINITION.equals(other));
    assertTrue(OPTION_EDU2_DEFINITION.hashCode() == other.hashCode());
    InterestRateFutureOptionPremiumSecurityDefinition modifiedFuture;
    modifiedFuture = new InterestRateFutureOptionPremiumSecurityDefinition(EDU2_DEFINITION, LAST_TRADING_DATE, STRIKE, IS_CALL);
    assertFalse(OPTION_EDU2_DEFINITION.equals(modifiedFuture));
    modifiedFuture = new InterestRateFutureOptionPremiumSecurityDefinition(EDU2_DEFINITION, EXPIRATION_DATE, STRIKE + 0.001, IS_CALL);
    assertFalse(OPTION_EDU2_DEFINITION.equals(modifiedFuture));
    modifiedFuture = new InterestRateFutureOptionPremiumSecurityDefinition(EDU2_DEFINITION, EXPIRATION_DATE, STRIKE, !IS_CALL);
    assertFalse(OPTION_EDU2_DEFINITION.equals(modifiedFuture));
  }

  @Test
  /**
   * Tests the toDerivative method.
   */
  public void toDerivative() {
    InterestRateFutureOptionPremiumSecurity optionEDU2Converted = OPTION_EDU2_DEFINITION.toDerivative(REFERENCE_DATE, CURVES);
    InterestRateFuture future = EDU2_DEFINITION.toDerivative(REFERENCE_DATE, REFERENCE_PRICE, CURVES);
    double expirationTime = ACT_ACT.getDayCountFraction(REFERENCE_DATE, EXPIRATION_DATE);
    InterestRateFutureOptionPremiumSecurity optionEDU2 = new InterestRateFutureOptionPremiumSecurity(future, expirationTime, STRIKE, IS_CALL);
    assertEquals("Option on future: to derivative", optionEDU2, optionEDU2Converted);
  }
}
