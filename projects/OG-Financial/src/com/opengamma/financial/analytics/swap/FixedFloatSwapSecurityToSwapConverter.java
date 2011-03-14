/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.swap;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import javax.time.calendar.ZonedDateTime;

import org.apache.commons.lang.Validate;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.core.holiday.HolidaySource;
import com.opengamma.core.region.Region;
import com.opengamma.core.region.RegionSource;
import com.opengamma.core.region.RegionUtils;
import com.opengamma.financial.convention.ConventionBundle;
import com.opengamma.financial.convention.ConventionBundleSource;
import com.opengamma.financial.convention.HolidaySourceCalendarAdapter;
import com.opengamma.financial.convention.InMemoryConventionBundleMaster;
import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.financial.convention.daycount.DayCountFactory;
import com.opengamma.financial.interestrate.annuity.definition.AnnuityCouponFixed;
import com.opengamma.financial.interestrate.annuity.definition.GenericAnnuity;
import com.opengamma.financial.interestrate.payments.CouponFixed;
import com.opengamma.financial.interestrate.payments.PaymentFixed;
import com.opengamma.financial.interestrate.payments.CouponIbor;
import com.opengamma.financial.interestrate.payments.Payment;
import com.opengamma.financial.interestrate.swap.definition.FixedCouponSwap;
import com.opengamma.financial.schedule.ScheduleCalculator;
import com.opengamma.financial.security.swap.FixedInterestRateLeg;
import com.opengamma.financial.security.swap.FloatingInterestRateLeg;
import com.opengamma.financial.security.swap.InterestRateNotional;
import com.opengamma.financial.security.swap.SwapLeg;
import com.opengamma.financial.security.swap.SwapSecurity;
import com.opengamma.id.Identifier;

/**
 * 
 */
public class FixedFloatSwapSecurityToSwapConverter {

  private final HolidaySource _holidaySource;
  private final RegionSource _regionSource;
  private final ConventionBundleSource _conventionSource;

  public FixedFloatSwapSecurityToSwapConverter(final HolidaySource holidaySource, final RegionSource regionSource, final ConventionBundleSource conventionSource) {
    _holidaySource = holidaySource;
    _regionSource = regionSource;
    _conventionSource = conventionSource;

  }

  // REVIEW: jim 8-Oct-2010 -- we might want to move this logic inside the RegionMaster.
  protected Calendar getCalendar(final Identifier regionId) {
    if (regionId.isScheme(RegionUtils.FINANCIAL) && regionId.getValue().contains("+")) {
      final String[] regions = regionId.getValue().split("\\+");
      final Set<Region> resultRegions = new HashSet<Region>();
      for (final String region : regions) {
        resultRegions.add(_regionSource.getHighestLevelRegion(RegionUtils.financialRegionId(region)));
      }
      return new HolidaySourceCalendarAdapter(_holidaySource, resultRegions);
    } else {
      final Region payRegion = _regionSource.getHighestLevelRegion(regionId); // we've checked that they are the same.
      return new HolidaySourceCalendarAdapter(_holidaySource, payRegion);
    }
  }

  public FixedCouponSwap<Payment> getSwap(final SwapSecurity swapSecurity, final String fundingCurveName, final String liborCurveName, final double marketRate,
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
    final Identifier regionId = payLeg.getRegionIdentifier();
    final Calendar calendar = getCalendar(regionId);
    final String currency = ((InterestRateNotional) payLeg.getNotional()).getCurrency().getCode();
    final ConventionBundle conventions = _conventionSource.getConventionBundle(Identifier.of(InMemoryConventionBundleMaster.SIMPLE_NAME_SCHEME, currency + "_SWAP"));

    return new FixedCouponSwap<Payment>(getFixedLeg(fixedLeg, now, effectiveDate, maturityDate, marketRate, fundingCurveName, calendar),
        getFloatLeg(floatLeg, now, effectiveDate, maturityDate,
            fundingCurveName, liborCurveName, calendar, initialRate, conventions.getSwapFloatingLegSettlementDays()));

  }

  public GenericAnnuity<Payment> getFloatLeg(final FloatingInterestRateLeg floatLeg, final ZonedDateTime now, final ZonedDateTime effectiveDate, final ZonedDateTime maturityDate,
      final String fundingCurveName, final String liborCurveName, final Calendar calendar, final double initialRate, final int settlementDays) {
    final ZonedDateTime[] unadjustedDates = ScheduleCalculator.getUnadjustedDateSchedule(effectiveDate, maturityDate, floatLeg.getFrequency());
    final ZonedDateTime[] adjustedDates = ScheduleCalculator.getAdjustedDateSchedule(unadjustedDates, floatLeg.getBusinessDayConvention(), calendar, 0);
    final ZonedDateTime[] resetDates = ScheduleCalculator.getAdjustedResetDateSchedule(effectiveDate, unadjustedDates, floatLeg.getBusinessDayConvention(), calendar, settlementDays); //TODO should settlement days be negative?
    final ZonedDateTime[] maturityDates = ScheduleCalculator.getAdjustedMaturityDateSchedule(effectiveDate, unadjustedDates, floatLeg.getBusinessDayConvention(), calendar, floatLeg.getFrequency());

    double[] paymentTimes = ScheduleCalculator.getTimes(adjustedDates, DayCountFactory.INSTANCE.getDayCount("Actual/Actual"), now);
    double[] resetTimes = ScheduleCalculator.getTimes(resetDates, DayCountFactory.INSTANCE.getDayCount("Actual/Actual"), now);
    double[] maturityTimes = ScheduleCalculator.getTimes(maturityDates, DayCountFactory.INSTANCE.getDayCount("Actual/Actual"), now);
    double[] yearFractions = ScheduleCalculator.getYearFractions(adjustedDates, floatLeg.getDayCount(), effectiveDate);
    final double notional = ((InterestRateNotional) floatLeg.getNotional()).getAmount();
    final double spread = floatLeg.getSpread();

    final int n = ScheduleCalculator.numberOfNegativeValues(paymentTimes);
    if (n >= paymentTimes.length) {
      //all payments are in the past - return a dummy annuity with zero notional a one payment (of zero) at zero and zero spread 
      //TODO may want to handle this case differently 
      return new GenericAnnuity<Payment>(new Payment[] {new PaymentFixed(0, 0, fundingCurveName)});
    }

    if (n > 0) {
      paymentTimes = ScheduleCalculator.removeFirstNValues(paymentTimes, n);
      resetTimes = ScheduleCalculator.removeFirstNValues(resetTimes, n);
      maturityTimes = ScheduleCalculator.removeFirstNValues(maturityTimes, n);
      yearFractions = ScheduleCalculator.removeFirstNValues(yearFractions, n);
    }
    final double[] spreads = new double[paymentTimes.length];
    Arrays.fill(spreads, spread);

    final Payment[] payments = new Payment[paymentTimes.length];
    for (int i = 0; i < payments.length; i++) {
      if (resetTimes[i] < 0.0) {
        payments[i] = new CouponFixed(paymentTimes[i], fundingCurveName, yearFractions[i], notional, initialRate);
      } else {
        payments[i] = new CouponIbor(paymentTimes[i], fundingCurveName, yearFractions[i], notional, resetTimes[i], resetTimes[i], maturityTimes[i], yearFractions[i], spreads[i], liborCurveName);
      }
    }

    //TODO need to handle paymentYearFraction differently from forwardYearFraction 
    return new GenericAnnuity<Payment>(payments);
  }

  public AnnuityCouponFixed getFixedLeg(final FixedInterestRateLeg fixedLeg, final ZonedDateTime now, final ZonedDateTime effectiveDate, final ZonedDateTime maturityDate, final double marketRate,
      final String fundingCurveName, final Calendar calendar) {
    final ZonedDateTime[] unadjustedDates = ScheduleCalculator.getUnadjustedDateSchedule(effectiveDate, maturityDate, fixedLeg.getFrequency());
    final ZonedDateTime[] adjustedDates = ScheduleCalculator.getAdjustedDateSchedule(unadjustedDates, fixedLeg.getBusinessDayConvention(), calendar, 0); //TODO are settlement days really 0 for swaps?    
    double[] paymentTimes = ScheduleCalculator.getTimes(adjustedDates, DayCountFactory.INSTANCE.getDayCount("Actual/Actual ISDA"), now);
    double[] yearFractions = ScheduleCalculator.getYearFractions(adjustedDates, fixedLeg.getDayCount(), effectiveDate);
    final double notional = ((InterestRateNotional) fixedLeg.getNotional()).getAmount();
    final int n = ScheduleCalculator.numberOfNegativeValues(paymentTimes);
    if (n >= paymentTimes.length) {
      return new AnnuityCouponFixed(new double[] {0.0}, 0.0, 0.0, fundingCurveName);
    }
    if (n > 0) {
      paymentTimes = ScheduleCalculator.removeFirstNValues(paymentTimes, n);
      yearFractions = ScheduleCalculator.removeFirstNValues(yearFractions, n);
    }
    return new AnnuityCouponFixed(paymentTimes, notional, marketRate, yearFractions, fundingCurveName);
  }

}
