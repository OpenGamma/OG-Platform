/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.swap;

import javax.time.calendar.ZonedDateTime;

import org.apache.commons.lang.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.primitives.Doubles;
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

import edu.emory.mathcs.backport.java.util.Arrays;

/**
 * 
 */
public class FixedFloatSwapSecurityToSwapConverter {

  private static final Logger s_logger = LoggerFactory.getLogger(FixedFloatSwapSecurityToSwapConverter.class);
  private final HolidaySource _holidaySource;
  private final RegionSource _regionSource;
  private final ConventionBundleSource _conventionSource;

  public FixedFloatSwapSecurityToSwapConverter(final HolidaySource holidaySource, final RegionSource regionSource, final ConventionBundleSource conventionSource) {
    _holidaySource = holidaySource;
    _regionSource = regionSource;
    _conventionSource = conventionSource;

  }

  public FixedFloatSwap getSwap(final SwapSecurity swapSecurity, final String fundingCurveName, final String liborCurveName, final double marketRate, 
      final double initialRate, final ZonedDateTime now) {
    Validate.notNull(swapSecurity, "swap security");
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
    return new FixedFloatSwap(getFixedLeg(fixedLeg, now, effectiveDate, maturityDate, marketRate, fundingCurveName, calendar), getFloatLeg(floatLeg, now, effectiveDate, maturityDate,
        fundingCurveName, liborCurveName, calendar, initialRate, conventions.getSwapFloatingLegSettlementDays()));
  }

  public VariableAnnuity getFloatLeg(final FloatingInterestRateLeg floatLeg, final ZonedDateTime now, final ZonedDateTime effectiveDate, final ZonedDateTime maturityDate,
      final String fundingCurveName, final String liborCurveName, final Calendar calendar, final double initialRate, final int settlementDays) {
    s_logger.debug("getFloatLeg(floatLeg=" + floatLeg + ", now=" + now + ", effectiveDate=" + effectiveDate + ", maturityDate=" + maturityDate + ", fundingCurveName=" + fundingCurveName
        + ", liborCurveName" + liborCurveName + ", calendar=" + calendar + ", settlementDays=" + settlementDays);
    final ZonedDateTime[] unadjustedDates = ScheduleCalculator.getUnadjustedDateSchedule(effectiveDate, maturityDate, floatLeg.getFrequency());
    s_logger.debug("unadjustedDates=" + Arrays.asList(unadjustedDates));
    final ZonedDateTime[] adjustedDates = ScheduleCalculator.getAdjustedDateSchedule(unadjustedDates, floatLeg.getBusinessDayConvention(), calendar);
    s_logger.debug("adjustedDates=" + Arrays.asList(adjustedDates));
    final ZonedDateTime[] resetDates = ScheduleCalculator.getAdjustedResetDateSchedule(effectiveDate, unadjustedDates, floatLeg.getBusinessDayConvention(), calendar, settlementDays);
    s_logger.debug("resetDates=" + Arrays.asList(resetDates));
    final ZonedDateTime[] maturityDates = ScheduleCalculator.getAdjustedMaturityDateSchedule(effectiveDate, unadjustedDates, floatLeg.getBusinessDayConvention(), calendar, floatLeg.getFrequency());
    s_logger.debug("maturityDates=" + Arrays.asList(maturityDates));

    double[] paymentTimes = ScheduleCalculator.getTimes(adjustedDates, DayCountFactory.INSTANCE.getDayCount("Actual/Actual"), now);
    s_logger.debug("paymentTimes=" + Doubles.asList(paymentTimes));
    double[] resetTimes = ScheduleCalculator.getTimes(resetDates, DayCountFactory.INSTANCE.getDayCount("Actual/Actual"), now);
    s_logger.debug("resetTimes=" + Doubles.asList(resetTimes));
    double[] maturityTimes = ScheduleCalculator.getTimes(maturityDates, DayCountFactory.INSTANCE.getDayCount("Actual/Actual"), now);
    s_logger.debug("maturityTimes=" + Doubles.asList(maturityTimes));
    double[] yearFractions = ScheduleCalculator.getYearFractions(adjustedDates, floatLeg.getDayCount(), effectiveDate);
    s_logger.debug("yearFractions=" + Doubles.asList(yearFractions));
    final double notional = ((InterestRateNotional) floatLeg.getNotional()).getAmount();
    final double spread = floatLeg.getSpread();

    final int n = ScheduleCalculator.numberOfNegativeValues(paymentTimes);
    if (n >= paymentTimes.length) {
      //all payments are in the past - return a dummy annuity with zero notional a one payment (of zero) at zero and zero spread 
      //TODO may want to handle this case differently 
      return new VariableAnnuity(new double[] {0.0}, new double[] {-1.0}, new double[] {0.0}, new double[] {1.0}, new double[] {0, 0}, 0.0, 0.0, fundingCurveName, liborCurveName);
    }

    if (n > 0) {
      paymentTimes = ScheduleCalculator.removeFirstNValues(paymentTimes, n);
      resetTimes = ScheduleCalculator.removeFirstNValues(resetTimes, n);
      maturityTimes = ScheduleCalculator.removeFirstNValues(maturityTimes, n);
      yearFractions = ScheduleCalculator.removeFirstNValues(yearFractions, n);
    }
    final double[] spreads = new double[paymentTimes.length];
    Arrays.fill(spreads, spread);
    //TODO need to set an initial rate //REVIEW elaine 29-09-2010: this has been done now, hasn't it? 
    return new VariableAnnuity(paymentTimes, resetTimes, maturityTimes, yearFractions, spreads, notional, initialRate, fundingCurveName, liborCurveName);
  }

  public ConstantCouponAnnuity getFixedLeg(final FixedInterestRateLeg fixedLeg, final ZonedDateTime now, final ZonedDateTime effectiveDate, final ZonedDateTime maturityDate, final double marketRate,
      final String fundingCurveName, final Calendar calendar) {
    s_logger.debug("getFixedLeg(fixedLeg=" + fixedLeg + ", tradeDate=" + now + ", effectiveDate=" + effectiveDate + ", maturityDate=" + maturityDate + ", marketRate=" + marketRate
        + ", fundingCurveName=" + fundingCurveName + ", calendar=" + calendar);
    final ZonedDateTime[] unadjustedDates = ScheduleCalculator.getUnadjustedDateSchedule(effectiveDate, maturityDate, fixedLeg.getFrequency());
    s_logger.debug("unadjustedDates = " + Arrays.asList(unadjustedDates));
    final ZonedDateTime[] adjustedDates = ScheduleCalculator.getAdjustedDateSchedule(unadjustedDates, fixedLeg.getBusinessDayConvention(), calendar);
    s_logger.debug("adjustedDates = " + Arrays.asList(adjustedDates));
    double[] paymentTimes = ScheduleCalculator.getTimes(adjustedDates, DayCountFactory.INSTANCE.getDayCount("Actual/Actual"), now);
    s_logger.debug("paymentTimes = " + Doubles.asList(paymentTimes));
    double[] yearFractions = ScheduleCalculator.getYearFractions(adjustedDates, fixedLeg.getDayCount(), effectiveDate);
    s_logger.debug("yearFractions = " + Doubles.asList(yearFractions));
    final double notional = ((InterestRateNotional) fixedLeg.getNotional()).getAmount();
    s_logger.debug("notional = " + Doubles.asList(notional));

    final int n = ScheduleCalculator.numberOfNegativeValues(paymentTimes);
    if (n >= paymentTimes.length) {
      return new ConstantCouponAnnuity(new double[] {0.0}, 0.0, 0.0, fundingCurveName);
    }
    if (n > 0) {
      paymentTimes = ScheduleCalculator.removeFirstNValues(paymentTimes, n);
      yearFractions = ScheduleCalculator.removeFirstNValues(yearFractions, n);
    }

    return new ConstantCouponAnnuity(paymentTimes, notional, marketRate, yearFractions, fundingCurveName);
  }

}
