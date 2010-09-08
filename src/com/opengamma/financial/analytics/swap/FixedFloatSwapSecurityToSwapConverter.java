/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.swap;

import javax.time.calendar.ZonedDateTime;

import org.apache.commons.lang.Validate;

import com.opengamma.financial.analytics.schedule.ScheduleCalculator;
import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.financial.convention.daycount.DayCountFactory;
import com.opengamma.financial.interestrate.annuity.definition.ConstantCouponAnnuity;
import com.opengamma.financial.interestrate.annuity.definition.VariableAnnuity;
import com.opengamma.financial.interestrate.swap.definition.FixedFloatSwap;
import com.opengamma.financial.security.swap.FixedInterestRateLeg;
import com.opengamma.financial.security.swap.FloatingInterestRateLeg;
import com.opengamma.financial.security.swap.InterestRateNotional;
import com.opengamma.financial.security.swap.SwapLeg;
import com.opengamma.financial.security.swap.SwapSecurity;

/**
 * 
 */
public class FixedFloatSwapSecurityToSwapConverter {
  //TODO erm 
  public static FixedFloatSwap getSwap(SwapSecurity swapSecurity, String fundingCurveName, String liborCurveName, double marketRate, Calendar calendar) {
    Validate.notNull(swapSecurity, "swap security");
    ZonedDateTime tradeDate = swapSecurity.getTradeDate().toZonedDateTime();
    ZonedDateTime effectiveDate = swapSecurity.getEffectiveDate().toZonedDateTime();
    ZonedDateTime maturityDate = swapSecurity.getMaturityDate().toZonedDateTime();
    SwapLeg payLeg = swapSecurity.getPayLeg();
    SwapLeg receiveLeg = swapSecurity.getReceiveLeg();
    FixedInterestRateLeg fixedLeg;
    FloatingInterestRateLeg floatLeg;
    if (payLeg instanceof FixedInterestRateLeg && receiveLeg instanceof FloatingInterestRateLeg) {
      fixedLeg = (FixedInterestRateLeg) payLeg;
      floatLeg = (FloatingInterestRateLeg) receiveLeg;
    } else if (payLeg instanceof FloatingInterestRateLeg && receiveLeg instanceof FixedInterestRateLeg) {
      fixedLeg = (FixedInterestRateLeg) receiveLeg;
      floatLeg = (FloatingInterestRateLeg) payLeg;
    } else {
      throw new IllegalArgumentException("Can only handle fixed-floating swaps");
    }
    return new FixedFloatSwap(getFixedLeg(fixedLeg, tradeDate, effectiveDate, maturityDate, marketRate, fundingCurveName, calendar), getFloatLeg(floatLeg, tradeDate, effectiveDate, 
        maturityDate, fundingCurveName, liborCurveName, calendar));
  }
  
  public static VariableAnnuity getFloatLeg(FloatingInterestRateLeg floatLeg, ZonedDateTime tradeDate, ZonedDateTime effectiveDate, ZonedDateTime maturityDate,
      String fundingCurveName, String liborCurveName, Calendar calendar) {
    ZonedDateTime[] unadjustedDates = ScheduleCalculator.getUnadjustedDateSchedule(effectiveDate, maturityDate, floatLeg.getFrequency());
    ZonedDateTime[] adjustedDates = ScheduleCalculator.getAdjustedDateSchedule(unadjustedDates, floatLeg.getBusinessDayConvention(), calendar); 
    ZonedDateTime[] resetDates = ScheduleCalculator.getAdjustedResetDateSchedule(effectiveDate,unadjustedDates, floatLeg.getBusinessDayConvention(), calendar);
    ZonedDateTime[] maturityDates = ScheduleCalculator.getAdjustedMaturityDateSchedule(effectiveDate, unadjustedDates, floatLeg.getBusinessDayConvention(),calendar, floatLeg.getFrequency());
    
    double[] paymentTimes = ScheduleCalculator.getTimes(adjustedDates, DayCountFactory.INSTANCE.getDayCount("Actual/Actual"), tradeDate);
    double[] resetTimes = ScheduleCalculator.getTimes(resetDates, DayCountFactory.INSTANCE.getDayCount("Actual/Actual"), tradeDate);
    double[] maturityTimes = ScheduleCalculator.getTimes(maturityDates, DayCountFactory.INSTANCE.getDayCount("Actual/Actual"), tradeDate);
    double[] yearFractions = ScheduleCalculator.getYearFractions(adjustedDates, floatLeg.getDayCount(), effectiveDate);
    double notional = ((InterestRateNotional) floatLeg.getNotional()).getAmount();
  
    return new VariableAnnuity(paymentTimes, resetTimes,maturityTimes, yearFractions, notional, fundingCurveName, liborCurveName);
  }
  
  public static ConstantCouponAnnuity getFixedLeg(FixedInterestRateLeg fixedLeg, ZonedDateTime tradeDate, ZonedDateTime effectiveDate, ZonedDateTime maturityDate, double marketRate, 
      String fundingCurveName, Calendar calendar) {
    ZonedDateTime[] unadjustedDates = ScheduleCalculator.getUnadjustedDateSchedule(effectiveDate, maturityDate, fixedLeg.getFrequency());
    ZonedDateTime[] adjustedDates = ScheduleCalculator.getAdjustedDateSchedule(unadjustedDates, fixedLeg.getBusinessDayConvention(), calendar); 
    double[] paymentTimes = ScheduleCalculator.getTimes(adjustedDates, DayCountFactory.INSTANCE.getDayCount("Actual/Actual"), tradeDate);
    double[] yearFractions = ScheduleCalculator.getYearFractions(adjustedDates, fixedLeg.getDayCount(), effectiveDate);
    double notional = ((InterestRateNotional) fixedLeg.getNotional()).getAmount();
    return new ConstantCouponAnnuity(paymentTimes, notional, marketRate, yearFractions, fundingCurveName);
  }
  
  
  
}
