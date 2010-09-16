/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.swap;

import javax.time.calendar.ZonedDateTime;

import org.apache.commons.lang.Validate;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.financial.analytics.schedule.ScheduleCalculator;
import com.opengamma.financial.convention.ConventionBundle;
import com.opengamma.financial.convention.ConventionBundleSource;
import com.opengamma.financial.convention.InMemoryConventionBundleMaster;
import com.opengamma.financial.convention.businessday.HolidaySourceCalendarAdapter;
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
import com.opengamma.financial.world.holiday.HolidaySource;
import com.opengamma.financial.world.region.Region;
import com.opengamma.financial.world.region.RegionSource;
import com.opengamma.id.Identifier;

/**
 * 
 */
public class FixedFloatSwapSecurityToSwapConverter {
  //TODO there doesn't seem to be any mention of "now" in here - suspect that some of uses of the effective date should actually be now. 
  private final HolidaySource _holidaySource;
  private final RegionSource _regionSource;
  private final ConventionBundleSource _conventionSource;

  public FixedFloatSwapSecurityToSwapConverter(final HolidaySource holidaySource, final RegionSource regionSource, final ConventionBundleSource conventionSource) {
    _holidaySource = holidaySource;
    _regionSource = regionSource;
    _conventionSource = conventionSource;
  }

  public FixedFloatSwap getSwap(final SwapSecurity swapSecurity, final String fundingCurveName, final String liborCurveName, final double marketRate) {
    Validate.notNull(swapSecurity, "swap security");
    final ZonedDateTime tradeDate = swapSecurity.getTradeDate().toZonedDateTime();
    final ZonedDateTime effectiveDate = swapSecurity.getEffectiveDate().toZonedDateTime();
    final ZonedDateTime maturityDate = swapSecurity.getMaturityDate().toZonedDateTime();
    final SwapLeg payLeg = swapSecurity.getPayLeg();
    final SwapLeg receiveLeg = swapSecurity.getReceiveLeg();
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
      throw new OpenGammaRuntimeException("Can only handle fixed-floating swaps");
    }
    final Region payRegion = _regionSource.getHighestLevelRegion(payLeg.getRegionIdentifier()); // we've checked that they are the same.
    final Calendar calendar = new HolidaySourceCalendarAdapter(_holidaySource, payRegion);
    final String currency = ((InterestRateNotional) payLeg.getNotional()).getCurrency().getISOCode();
    final ConventionBundle conventions = _conventionSource.getConventionBundle(Identifier.of(InMemoryConventionBundleMaster.SIMPLE_NAME_SCHEME, currency + "_SWAP"));
    return new FixedFloatSwap(getFixedLeg(fixedLeg, tradeDate, effectiveDate, maturityDate, marketRate, fundingCurveName, calendar), getFloatLeg(floatLeg, tradeDate, effectiveDate,
        maturityDate, fundingCurveName, liborCurveName, calendar, conventions.getSwapFloatingLegSettlementDays()));
  }

  public VariableAnnuity getFloatLeg(final FloatingInterestRateLeg floatLeg, final ZonedDateTime tradeDate, final ZonedDateTime effectiveDate, final ZonedDateTime maturityDate,
      final String fundingCurveName, final String liborCurveName, final Calendar calendar, final int settlementDays) {
    final ZonedDateTime[] unadjustedDates = ScheduleCalculator.getUnadjustedDateSchedule(effectiveDate, maturityDate, floatLeg.getFrequency());
    final ZonedDateTime[] adjustedDates = ScheduleCalculator.getAdjustedDateSchedule(unadjustedDates, floatLeg.getBusinessDayConvention(), calendar);
    final ZonedDateTime[] resetDates = ScheduleCalculator.getAdjustedResetDateSchedule(effectiveDate, unadjustedDates, floatLeg.getBusinessDayConvention(), calendar, settlementDays);
    final ZonedDateTime[] maturityDates = ScheduleCalculator.getAdjustedMaturityDateSchedule(effectiveDate, unadjustedDates, floatLeg.getBusinessDayConvention(), calendar, floatLeg.getFrequency());

    final double[] paymentTimes = ScheduleCalculator.getTimes(adjustedDates, DayCountFactory.INSTANCE.getDayCount("Actual/Actual"), tradeDate);
    final double[] resetTimes = ScheduleCalculator.getTimes(resetDates, DayCountFactory.INSTANCE.getDayCount("Actual/Actual"), tradeDate);
    final double[] maturityTimes = ScheduleCalculator.getTimes(maturityDates, DayCountFactory.INSTANCE.getDayCount("Actual/Actual"), tradeDate);
    final double[] yearFractions = ScheduleCalculator.getYearFractions(adjustedDates, floatLeg.getDayCount(), effectiveDate);
    final double notional = ((InterestRateNotional) floatLeg.getNotional()).getAmount();

    return new VariableAnnuity(paymentTimes, resetTimes, maturityTimes, yearFractions, notional, fundingCurveName, liborCurveName);
  }

  public ConstantCouponAnnuity getFixedLeg(final FixedInterestRateLeg fixedLeg, final ZonedDateTime tradeDate, final ZonedDateTime effectiveDate, final ZonedDateTime maturityDate,
      final double marketRate, final String fundingCurveName, final Calendar calendar) {
    final ZonedDateTime[] unadjustedDates = ScheduleCalculator.getUnadjustedDateSchedule(effectiveDate, maturityDate, fixedLeg.getFrequency());
    final ZonedDateTime[] adjustedDates = ScheduleCalculator.getAdjustedDateSchedule(unadjustedDates, fixedLeg.getBusinessDayConvention(), calendar);
    final double[] paymentTimes = ScheduleCalculator.getTimes(adjustedDates, DayCountFactory.INSTANCE.getDayCount("Actual/Actual"), tradeDate);
    final double[] yearFractions = ScheduleCalculator.getYearFractions(adjustedDates, fixedLeg.getDayCount(), effectiveDate);
    final double notional = ((InterestRateNotional) fixedLeg.getNotional()).getAmount();
    return new ConstantCouponAnnuity(paymentTimes, notional, marketRate, yearFractions, fundingCurveName);
  }



}
