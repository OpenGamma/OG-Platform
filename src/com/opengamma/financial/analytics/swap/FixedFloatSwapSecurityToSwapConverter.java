/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.swap;

import javax.time.calendar.ZonedDateTime;

import org.apache.commons.lang.Validate;
import org.openid4java.consumer.InMemoryConsumerAssociationStore;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.financial.analytics.schedule.ScheduleCalculator;
import com.opengamma.financial.convention.*;
import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.financial.convention.daycount.DayCountFactory;
import com.opengamma.financial.convention.businessday.HolidaySourceCalendarAdapter;
import com.opengamma.financial.interestrate.annuity.definition.ConstantCouponAnnuity;
import com.opengamma.financial.interestrate.annuity.definition.VariableAnnuity;
import com.opengamma.financial.interestrate.swap.definition.FixedFloatSwap;
import com.opengamma.financial.security.swap.FixedInterestRateLeg;
import com.opengamma.financial.security.swap.FloatingInterestRateLeg;
import com.opengamma.financial.security.swap.InterestRateNotional;
import com.opengamma.financial.security.swap.SwapLeg;
import com.opengamma.financial.security.swap.SwapSecurity;
import com.opengamma.financial.world.region.*;
import com.opengamma.financial.world.holiday.*;
import com.opengamma.id.Identifier;

/**
 * 
 */
public class FixedFloatSwapSecurityToSwapConverter {
  
  private HolidaySource _holidaySource;
  private RegionSource _regionSource;
  private ConventionBundleSource _conventionSource;

  public FixedFloatSwapSecurityToSwapConverter(HolidaySource holidaySource, RegionSource regionSource, ConventionBundleSource conventionSource) {
    _holidaySource = holidaySource;
    _regionSource = regionSource;
    _conventionSource = conventionSource;
  }
  
  //TODO erm 
  public FixedFloatSwap getSwap(SwapSecurity swapSecurity, String fundingCurveName, String liborCurveName, double marketRate) {
    Validate.notNull(swapSecurity, "swap security");
    ZonedDateTime tradeDate = swapSecurity.getTradeDate().toZonedDateTime();
    ZonedDateTime effectiveDate = swapSecurity.getEffectiveDate().toZonedDateTime();
    ZonedDateTime maturityDate = swapSecurity.getMaturityDate().toZonedDateTime();
    SwapLeg payLeg = swapSecurity.getPayLeg();
    SwapLeg receiveLeg = swapSecurity.getReceiveLeg();
    if (!payLeg.getRegionIdentifier().equals(receiveLeg.getRegionIdentifier())) {
      throw new OpenGammaRuntimeException("Pay and receive legs must be from same region");
    }
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
    Region payRegion = _regionSource.getHighestLevelRegion(payLeg.getRegionIdentifier()); // we've checked that they are the same.
    Calendar calendar = new HolidaySourceCalendarAdapter(_holidaySource, payRegion);
    String currency = ((InterestRateNotional)payLeg.getNotional()).getCurrency().getISOCode();
    ConventionBundle conventions = _conventionSource.getConventionBundle(Identifier.of(InMemoryConventionBundleMaster.SIMPLE_NAME_SCHEME, currency+"_SWAP"));
    return new FixedFloatSwap(getFixedLeg(fixedLeg, tradeDate, effectiveDate, maturityDate, marketRate, fundingCurveName, calendar), getFloatLeg(floatLeg, tradeDate, effectiveDate, 
        maturityDate, fundingCurveName, liborCurveName, calendar, conventions.getSwapFloatingLegSettlementDays()));
  }
  
  public VariableAnnuity getFloatLeg(FloatingInterestRateLeg floatLeg, ZonedDateTime tradeDate, ZonedDateTime effectiveDate, ZonedDateTime maturityDate,
      String fundingCurveName, String liborCurveName, Calendar calendar, int settlementDays) {
    ZonedDateTime[] unadjustedDates = ScheduleCalculator.getUnadjustedDateSchedule(effectiveDate, maturityDate, floatLeg.getFrequency());
    ZonedDateTime[] adjustedDates = ScheduleCalculator.getAdjustedDateSchedule(unadjustedDates, floatLeg.getBusinessDayConvention(), calendar); 
    ZonedDateTime[] resetDates = ScheduleCalculator.getAdjustedResetDateSchedule(effectiveDate, unadjustedDates, floatLeg.getBusinessDayConvention(), calendar, settlementDays);
    ZonedDateTime[] maturityDates = ScheduleCalculator.getAdjustedMaturityDateSchedule(effectiveDate, unadjustedDates, floatLeg.getBusinessDayConvention(), calendar, floatLeg.getFrequency());
    
    double[] paymentTimes = ScheduleCalculator.getTimes(adjustedDates, DayCountFactory.INSTANCE.getDayCount("Actual/Actual"), tradeDate);
    double[] resetTimes = ScheduleCalculator.getTimes(resetDates, DayCountFactory.INSTANCE.getDayCount("Actual/Actual"), tradeDate);
    double[] maturityTimes = ScheduleCalculator.getTimes(maturityDates, DayCountFactory.INSTANCE.getDayCount("Actual/Actual"), tradeDate);
    double[] yearFractions = ScheduleCalculator.getYearFractions(adjustedDates, floatLeg.getDayCount(), effectiveDate);
    double notional = ((InterestRateNotional) floatLeg.getNotional()).getAmount();
  
    return new VariableAnnuity(paymentTimes, resetTimes, maturityTimes, yearFractions, notional, fundingCurveName, liborCurveName);
  }
  
  public ConstantCouponAnnuity getFixedLeg(FixedInterestRateLeg fixedLeg, ZonedDateTime tradeDate, ZonedDateTime effectiveDate, ZonedDateTime maturityDate, double marketRate, 
      String fundingCurveName, Calendar calendar) {
    ZonedDateTime[] unadjustedDates = ScheduleCalculator.getUnadjustedDateSchedule(effectiveDate, maturityDate, fixedLeg.getFrequency());
    ZonedDateTime[] adjustedDates = ScheduleCalculator.getAdjustedDateSchedule(unadjustedDates, fixedLeg.getBusinessDayConvention(), calendar); 
    double[] paymentTimes = ScheduleCalculator.getTimes(adjustedDates, DayCountFactory.INSTANCE.getDayCount("Actual/Actual"), tradeDate);
    double[] yearFractions = ScheduleCalculator.getYearFractions(adjustedDates, fixedLeg.getDayCount(), effectiveDate);
    double notional = ((InterestRateNotional) fixedLeg.getNotional()).getAmount();
    return new ConstantCouponAnnuity(paymentTimes, notional, marketRate, yearFractions, fundingCurveName);
  }
  
  
  
}
