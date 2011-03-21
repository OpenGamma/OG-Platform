/**
 * Copyright (C) 2009 - 2011 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.instrument.fra;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import javax.time.calendar.LocalDate;
import javax.time.calendar.ZonedDateTime;

import org.junit.Test;

import com.opengamma.financial.convention.businessday.BusinessDayConvention;
import com.opengamma.financial.convention.businessday.BusinessDayConventionFactory;
import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.financial.convention.calendar.MondayToFridayCalendar;
import com.opengamma.financial.convention.daycount.DayCount;
import com.opengamma.financial.convention.daycount.DayCountFactory;
import com.opengamma.financial.instrument.Convention;
import com.opengamma.financial.interestrate.fra.definition.ForwardRateAgreement;
import com.opengamma.util.time.DateUtil;

/**
 * 
 */
public class FRADefinitionTest {
  private static final int SETTLEMENT_DAYS = 2;
  private static final DayCount DAY_COUNT = DayCountFactory.INSTANCE.getDayCount("Actual/360");
  private static final BusinessDayConvention BUSINESS_DAY = BusinessDayConventionFactory.INSTANCE.getBusinessDayConvention("Following");
  private static final Calendar CALENDAR = new MondayToFridayCalendar("A");
  private static final String NAME = "CONVENTION";
  private static final Convention CONVENTION = new Convention(SETTLEMENT_DAYS, DAY_COUNT, BUSINESS_DAY, CALENDAR, NAME);
  private static final LocalDate DATE = LocalDate.of(2011, 1, 25);
  private static final ZonedDateTime START = DateUtil.getUTCDate(2011, 4, 25);
  private static final ZonedDateTime MATURITY = DateUtil.getUTCDate(2011, 7, 25);
  private static final double RATE = 0.05;
  private static final FRADefinition DEFINITION = new FRADefinition(START, MATURITY, RATE, CONVENTION);

  @Test(expected = IllegalArgumentException.class)
  public void testNullStart() {
    new FRADefinition(null, MATURITY, RATE, CONVENTION);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testNullMaturity() {
    new FRADefinition(START, null, RATE, CONVENTION);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testNullConvention() {
    new FRADefinition(START, MATURITY, RATE, null);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testNullDate() {
    DEFINITION.toDerivative(null, new String[] {"A"});
  }

  @Test(expected = IllegalArgumentException.class)
  public void testNullCurveNames() {
    DEFINITION.toDerivative(DATE, (String[]) null);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testEmptyCurveNames() {
    DEFINITION.toDerivative(DATE, new String[0]);
  }

  @Test
  public void test() {
    assertEquals(START, DEFINITION.getStartDate());
    assertEquals(MATURITY, DEFINITION.getMaturity());
    assertEquals(RATE, DEFINITION.getRate(), 0);
    assertEquals(CONVENTION, DEFINITION.getConvention());
    FRADefinition other = new FRADefinition(START, MATURITY, RATE, CONVENTION);
    assertEquals(other, DEFINITION);
    assertEquals(other.hashCode(), DEFINITION.hashCode());
    other = new FRADefinition(START, MATURITY.plusDays(1), RATE, CONVENTION);
    assertFalse(other.equals(DEFINITION));
    other = new FRADefinition(START.plusDays(1), MATURITY, RATE, CONVENTION);
    assertFalse(other.equals(DEFINITION));
    other = new FRADefinition(START, MATURITY, RATE + 0.01, CONVENTION);
    assertFalse(other.equals(DEFINITION));
    other = new FRADefinition(START, MATURITY, RATE, new Convention(SETTLEMENT_DAYS + 1, DAY_COUNT, BUSINESS_DAY, CALENDAR, NAME));
    assertFalse(other.equals(DEFINITION));
  }

  @Test
  public void testConversion() {
    final String fundingCurveName = "A";
    final String indexCurveName = "B";
    final ForwardRateAgreement fra = DEFINITION.toDerivative(DATE, fundingCurveName, indexCurveName);
    assertEquals(fra.getFixingDate(), 90. / 365, 0);
    assertEquals(fra.getSettlementDate(), 92. / 365, 0);
    assertEquals(fra.getMaturity(), 181. / 365, 0);
    assertEquals(fra.getDiscountingYearFraction(), 89. / 360, 0);
    assertEquals(fra.getForwardYearFraction(), 91. / 360, 0);
    assertEquals(fra.getFundingCurveName(), fundingCurveName);
    assertEquals(fra.getIndexCurveName(), indexCurveName);
    assertEquals(fra.getStrike(), RATE, 0);
  }
}
