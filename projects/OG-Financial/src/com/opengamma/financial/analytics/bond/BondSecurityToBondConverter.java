/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.bond;

import java.util.Arrays;

import javax.time.calendar.ZonedDateTime;

import org.apache.commons.lang.Validate;

import com.opengamma.financial.Currency;
import com.opengamma.financial.analytics.schedule.ScheduleCalculator;
import com.opengamma.financial.analytics.timeseries.ScheduleFactory;
import com.opengamma.financial.convention.ConventionBundle;
import com.opengamma.financial.convention.ConventionBundleSource;
import com.opengamma.financial.convention.InMemoryConventionBundleMaster;
import com.opengamma.financial.convention.businessday.BusinessDayConventionFactory;
import com.opengamma.financial.convention.businessday.HolidaySourceCalendarAdapter;
import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.financial.convention.daycount.DayCount;
import com.opengamma.financial.convention.daycount.DayCountFactory;
import com.opengamma.financial.convention.frequency.Frequency;
import com.opengamma.financial.convention.frequency.PeriodFrequency;
import com.opengamma.financial.convention.frequency.SimpleFrequency;
import com.opengamma.financial.interestrate.bond.definition.Bond;
import com.opengamma.financial.security.bond.BondSecurity;
import com.opengamma.financial.world.holiday.master.HolidaySource;
import com.opengamma.id.Identifier;

/**
 * 
 */
public class BondSecurityToBondConverter {
  private final HolidaySource _holidaySource;
  private final ConventionBundleSource _conventionSource;

  public BondSecurityToBondConverter(final HolidaySource holidaySource, final ConventionBundleSource conventionSource) {
    Validate.notNull(holidaySource, "holiday source");
    Validate.notNull(conventionSource, "convention source");
    _holidaySource = holidaySource;
    _conventionSource = conventionSource;
  }

  public Bond getBond(final BondSecurity security, final String curveName, final ZonedDateTime now) {
    Validate.notNull(security, "security");
    Validate.notNull(curveName, "curve name");
    Validate.notNull(now, "now");
    final ZonedDateTime maturityDate = security.getMaturity().getExpiry();
    Validate.isTrue(now.isBefore(maturityDate), "The bond has expired");
    final Calendar calendar = new HolidaySourceCalendarAdapter(_holidaySource, security.getCurrency());
    final ZonedDateTime datedDate = security.getInterestAccrualDate().toZonedDateTime();
    final ZonedDateTime firstCouponDate = security.getFirstCouponDate().toZonedDateTime();
    final Frequency frequency = security.getCouponFrequency();
    final SimpleFrequency simpleFrequency;
    if (frequency instanceof PeriodFrequency) {
      simpleFrequency = ((PeriodFrequency) frequency).toSimpleFrequency();
    } else if (frequency instanceof SimpleFrequency) {
      simpleFrequency = (SimpleFrequency) frequency;
    } else {
      throw new IllegalArgumentException("For the moment can only deal with PeriodFrequency and SimpleFrequency");
    }

    final double paymentYearFraction = 1.0 / simpleFrequency.getPeriodsPerYear();
    final Currency currency = security.getCurrency();
    final Identifier id = Identifier.of(InMemoryConventionBundleMaster.SIMPLE_NAME_SCHEME, currency + "_TREASURY_COUPON_DATE_CONVENTION");
    final ConventionBundle convention = _conventionSource.getConventionBundle(id);
    ZonedDateTime[] couponDates = ScheduleFactory.getSchedule(datedDate, maturityDate, simpleFrequency, convention.isEOMConvention(), convention.calculateScheduleFromMaturity());
    couponDates = Arrays.copyOfRange(couponDates, 1, couponDates.length);
    if (convention.calculateScheduleFromMaturity() && firstCouponDate != null) {
      Validate.isTrue(couponDates[0].equals(firstCouponDate), "Calculated first coupon date did not match that in the security definition");
    }
    final int n = couponDates.length;
    //adjust all coupon dates to fall on business days 
    final ZonedDateTime[] couponPaymentDates = ScheduleCalculator.getAdjustedDateSchedule(couponDates, BusinessDayConventionFactory.INSTANCE.getBusinessDayConvention("Following"), calendar);
    final ZonedDateTime[] exDividendDates = getExDividendDates(couponDates, convention.getExDividendDays());

    //for a seasoned bond, find out the next coupon
    //TODO if a bonds nominal coupon payment date is a non-business day, does it count as being ex-dividend on that date even if the actual payment may have rolled a
    //few days forward to the next business day?
    int index = 0;
    while (!now.isBefore(couponDates[index])) { //if now is on or after nominalCouponDate[index], assume coupon has been paid
      index++;
    }

    //Accrued interest calculated from nominal coupon dates
    ZonedDateTime previousCouponDate;
    if (index == 0) {
      previousCouponDate = security.getInterestAccrualDate().toZonedDateTime();
    } else {
      previousCouponDate = couponDates[index - 1];
    }

    final double periodYearFrac = security.getDayCountConvention().getDayCountFraction(previousCouponDate, couponDates[index]);

    double accrualFraction;
    if (now.isBefore(exDividendDates[index])) {
      final double accualTime = security.getDayCountConvention().getDayCountFraction(previousCouponDate, now);
      accrualFraction = accualTime / periodYearFrac;
    } else {
      final double accualTime = -security.getDayCountConvention().getDayCountFraction(now, couponDates[index]);
      accrualFraction = accualTime / periodYearFrac;
      index++; //drop the next coupon from the bond as we are ex-dividend 
    }
    //TODO what happens when this is zero, i.e. the last coupon has go ex-dividend - does the buyer still get the principle (in which case the bond still has value)?
    final int nPayments = n + 1 - index;
    final double[] paymentTimes = new double[nPayments];
    final DayCount actAct = DayCountFactory.INSTANCE.getDayCount("Actual/Actual");
    for (int i = 0; i < nPayments; i++) {
      paymentTimes[i] = actAct.getDayCountFraction(now, couponPaymentDates[i + index]);
    }

    return new Bond(paymentTimes, security.getCouponRate() / 100., paymentYearFraction, accrualFraction, curveName);
  }

  /**
   * @param nominalCouponDate
   * @return
   */
  private ZonedDateTime[] getExDividendDates(final ZonedDateTime[] nominalCouponDates, final int exDividendDays) {
    for (int i = 0; i < nominalCouponDates.length; i++) {
      nominalCouponDates[i] = nominalCouponDates[i].minusDays(exDividendDays);
    }
    return nominalCouponDates; //TODO get these dates from somewhere 
  }

}
