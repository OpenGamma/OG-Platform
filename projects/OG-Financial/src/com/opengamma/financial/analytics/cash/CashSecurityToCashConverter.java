/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.cash;

import javax.time.calendar.ZonedDateTime;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.core.holiday.HolidaySource;
import com.opengamma.financial.convention.ConventionBundle;
import com.opengamma.financial.convention.ConventionBundleSource;
import com.opengamma.financial.convention.HolidaySourceCalendarAdapter;
import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.financial.convention.daycount.DayCount;
import com.opengamma.financial.convention.daycount.DayCountFactory;
import com.opengamma.financial.interestrate.cash.definition.Cash;
import com.opengamma.financial.security.cash.CashSecurity;
import com.opengamma.util.ArgumentChecker;

/**
 * Converts a {@code CashSecurity} to an analytics cash loan instance.
 * <p>
 * This extracts information about the security to create a {@link Cash} instance.
 */
public class CashSecurityToCashConverter {

  /**
   * The holiday source.
   */
  private final HolidaySource _holidaySource;
  /**
   * The convention source.
   */
  private final ConventionBundleSource _conventionSource;

  /**
   * Creates an instance.
   * 
   * @param holidaySource  the holiday source, not null
   * @param conventionSource  the convention source, not null
   */
  public CashSecurityToCashConverter(final HolidaySource holidaySource, final ConventionBundleSource conventionSource) {
    ArgumentChecker.notNull(holidaySource, "holidaySource");
    ArgumentChecker.notNull(conventionSource, "conventionSource");
    _holidaySource = holidaySource;
    _conventionSource = conventionSource;
  }

  //-------------------------------------------------------------------------
  /**
   * Gets the analytics cash loan definition for the specified security.
   * 
   * @param security  the security to use to create the analytics object, not null
   * @param curveName  the name of the curve, not null
   * @param marketRate  the market rate
   * @param now  the applicable time, not null
   * @return the analytic cash load instance, not null
   */
  public Cash getCash(final CashSecurity security, final String curveName, final double marketRate, final ZonedDateTime now) {
    final ConventionBundle conventions = _conventionSource.getConventionBundle(security.getIdentifiers());
    final Calendar calendar = new HolidaySourceCalendarAdapter(_holidaySource, security.getCurrency());
    final ZonedDateTime startDate = conventions.getBusinessDayConvention().adjustDate(calendar, now.plusDays(conventions.getSettlementDays()));
    final DayCount dayCount = conventions.getDayCount();
    final double tradeTime = dayCount.getDayCountFraction(now, startDate);
    final ZonedDateTime maturityDate = security.getMaturity();
    final DayCount actAct = DayCountFactory.INSTANCE.getDayCount("Actual/Actual ISDA");
    final double paymentTime = actAct.getDayCountFraction(now, maturityDate);
    final double yearFraction = dayCount.getDayCountFraction(startDate, maturityDate);
    if (startDate.isAfter(maturityDate)) {
      throw new OpenGammaRuntimeException("startDate " + startDate + " is after maturity date " + maturityDate + " probably caused by market holiday, so no data anyway");
    }
    return new Cash(paymentTime, marketRate, tradeTime, yearFraction, curveName);
  }

}
