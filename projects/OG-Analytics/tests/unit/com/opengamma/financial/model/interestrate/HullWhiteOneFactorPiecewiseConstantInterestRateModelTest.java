/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.model.interestrate;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertFalse;
import static org.testng.AssertJUnit.assertTrue;

import javax.time.calendar.LocalDate;
import javax.time.calendar.LocalDateTime;
import javax.time.calendar.Period;
import javax.time.calendar.TimeZone;
import javax.time.calendar.ZonedDateTime;

import org.testng.annotations.Test;

import com.opengamma.financial.convention.businessday.BusinessDayConvention;
import com.opengamma.financial.convention.businessday.BusinessDayConventionFactory;
import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.financial.convention.calendar.MondayToFridayCalendar;
import com.opengamma.financial.convention.daycount.DayCount;
import com.opengamma.financial.convention.daycount.DayCountFactory;
import com.opengamma.financial.instrument.index.IborIndex;
import com.opengamma.financial.interestrate.future.InterestRateFutureSecurity;
import com.opengamma.financial.model.interestrate.definition.HullWhiteOneFactorPiecewiseConstantDataBundle;
import com.opengamma.financial.schedule.ScheduleCalculator;
import com.opengamma.util.money.Currency;
import com.opengamma.util.time.DateUtil;

/**
 * Tests related to the construction of the Hull-White one factor model with piecewise constant volatility. The computation of several model related factors are also tested.
 */
public class HullWhiteOneFactorPiecewiseConstantInterestRateModelTest {

  private static final double MEAN_REVERSION = 0.01;
  private static final double[] VOLATILITY = new double[] {0.01, 0.011, 0.012, 0.013, 0.014};
  private static final double[] VOLATILITY_TIME = new double[] {0.5, 1.0, 2.0, 5.0};
  private static final HullWhiteOneFactorPiecewiseConstantDataBundle MODEL_PARAMETERS = new HullWhiteOneFactorPiecewiseConstantDataBundle(MEAN_REVERSION, VOLATILITY, VOLATILITY_TIME);
  private static final HullWhiteOneFactorPiecewiseConstantInterestRateModel MODEL = new HullWhiteOneFactorPiecewiseConstantInterestRateModel();

  @Test
  /**
   * Tests the class getters.
   */
  public void getter() {
    assertEquals(MEAN_REVERSION, MODEL_PARAMETERS.getMeanReversion());
    assertEquals(VOLATILITY, MODEL_PARAMETERS.getVolatility());
    double[] volTime = MODEL_PARAMETERS.getVolatilityTime();
    for (int loopperiod = 0; loopperiod < VOLATILITY_TIME.length; loopperiod++) {
      assertEquals(VOLATILITY_TIME[loopperiod], volTime[loopperiod + 1]);
    }
  }

  @Test
  /**
   * Tests the equal and hash code methods.
   */
  public void equalHash() {
    HullWhiteOneFactorPiecewiseConstantDataBundle newParameter = new HullWhiteOneFactorPiecewiseConstantDataBundle(MEAN_REVERSION, VOLATILITY, VOLATILITY_TIME);
    assertTrue("Hull-White model equals", MODEL_PARAMETERS.equals(newParameter));
    assertTrue("Hull-White model hash code", MODEL_PARAMETERS.hashCode() == newParameter.hashCode());
    HullWhiteOneFactorPiecewiseConstantDataBundle modifiedParameter = new HullWhiteOneFactorPiecewiseConstantDataBundle(MEAN_REVERSION + 0.01, VOLATILITY, VOLATILITY_TIME);
    assertFalse("Hull-White model equals", MODEL_PARAMETERS.equals(modifiedParameter));
  }

  @Test
  /**
   * Test the future convexity adjustment factor v a hard-coded value.
   */
  public void futureConvexityFactor() {
    //EURIBOR 3M Index
    final Period TENOR = Period.ofMonths(3);
    final int SETTLEMENT_DAYS = 2;
    final Calendar CALENDAR = new MondayToFridayCalendar("A");
    final DayCount DAY_COUNT_INDEX = DayCountFactory.INSTANCE.getDayCount("Actual/360");
    final BusinessDayConvention BUSINESS_DAY = BusinessDayConventionFactory.INSTANCE.getBusinessDayConvention("Modified Following");
    final boolean IS_EOM = true;
    final Currency CUR = Currency.EUR;
    final IborIndex IBOR_INDEX = new IborIndex(CUR, TENOR, SETTLEMENT_DAYS, CALENDAR, DAY_COUNT_INDEX, BUSINESS_DAY, IS_EOM);
    // Future
    final ZonedDateTime SPOT_LAST_TRADING_DATE = DateUtil.getUTCDate(2012, 9, 19);
    final ZonedDateTime LAST_TRADING_DATE = ScheduleCalculator.getAdjustedDate(SPOT_LAST_TRADING_DATE, CALENDAR, -SETTLEMENT_DAYS);
    final ZonedDateTime FIXING_END_DATE = ScheduleCalculator.getAdjustedDate(SPOT_LAST_TRADING_DATE, BUSINESS_DAY, CALENDAR, IS_EOM, TENOR);
    final double NOTIONAL = 1000000.0; // 1m
    final double FUTURE_FACTOR = 0.25;
    final String NAME = "ERU2";
    final LocalDate REFERENCE_DATE = LocalDate.of(2010, 8, 18);
    DayCount ACT_ACT = DayCountFactory.INSTANCE.getDayCount("Actual/Actual ISDA");
    ZonedDateTime REFERENCE_DATE_ZONED = ZonedDateTime.of(LocalDateTime.ofMidnight(REFERENCE_DATE), TimeZone.UTC);
    double LAST_TRADING_TIME = ACT_ACT.getDayCountFraction(REFERENCE_DATE_ZONED, LAST_TRADING_DATE);
    double FIXING_START_TIME = ACT_ACT.getDayCountFraction(REFERENCE_DATE_ZONED, SPOT_LAST_TRADING_DATE);
    double FIXING_END_TIME = ACT_ACT.getDayCountFraction(REFERENCE_DATE_ZONED, FIXING_END_DATE);
    double FIXING_ACCRUAL = DAY_COUNT_INDEX.getDayCountFraction(SPOT_LAST_TRADING_DATE, FIXING_END_DATE);
    final String DISCOUNTING_CURVE_NAME = "Funding";
    final String FORWARD_CURVE_NAME = "Forward";
    InterestRateFutureSecurity ERU2 = new InterestRateFutureSecurity(LAST_TRADING_TIME, IBOR_INDEX, FIXING_START_TIME, FIXING_END_TIME, FIXING_ACCRUAL, NOTIONAL, FUTURE_FACTOR, NAME,
        DISCOUNTING_CURVE_NAME, FORWARD_CURVE_NAME);
    double factor = MODEL.futureConvexityFactor(ERU2, MODEL_PARAMETERS);
    double expectedFactor = 1.000079130767980;
    assertEquals("Hull-White one factor: future convexity adjusment factor", expectedFactor, factor, 1E-10);
  }

  @Test
  /**
   * Test the bond volatility (called alpha) vs a hard-coded value.
   */
  public void bondVolatility() {
    double expiry1 = 0.25;
    double expiry2 = 2.25;
    double numeraire = 10.0;
    double maturity = 9.0;
    double alphaExpected = -0.015191631;
    double alpha = MODEL.alpha(expiry1, expiry2, numeraire, maturity, MODEL_PARAMETERS); //All data
    assertEquals("Hull-White one factor: bond volatility (alpha) - all", alphaExpected, alpha, 1E-8);
    alphaExpected = -0.015859116;
    alpha = MODEL.alpha(0.0, expiry2, numeraire, maturity, MODEL_PARAMETERS);//From today
    assertEquals("Hull-White one factor: bond volatility (alpha)- today", alphaExpected, alpha, 1E-8);
    alphaExpected = 0.111299267;
    alpha = MODEL.alpha(0.0, expiry2, expiry2, maturity, MODEL_PARAMETERS);// From today with expiry numeraire
    assertEquals("Hull-White one factor: bond volatility (alpha) - today and expiry numeraire", alphaExpected, alpha, 1E-8);
  }
}
