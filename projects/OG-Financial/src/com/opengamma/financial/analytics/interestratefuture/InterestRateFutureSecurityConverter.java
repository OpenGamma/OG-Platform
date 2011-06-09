/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.interestratefuture;

import javax.time.calendar.ZonedDateTime;

import org.apache.commons.lang.Validate;

import com.opengamma.core.holiday.HolidaySource;
import com.opengamma.core.region.RegionSource;
import com.opengamma.financial.analytics.fixedincome.CalendarUtil;
import com.opengamma.financial.convention.ConventionBundle;
import com.opengamma.financial.convention.ConventionBundleSource;
import com.opengamma.financial.convention.InMemoryConventionBundleMaster;
import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.financial.instrument.future.InterestRateFutureSecurityDefinition;
import com.opengamma.financial.instrument.index.IborIndex;
import com.opengamma.financial.security.future.InterestRateFutureSecurity;
import com.opengamma.id.Identifier;
import com.opengamma.util.money.Currency;

/**
 * 
 */
public class InterestRateFutureSecurityConverter {
  private final HolidaySource _holidaySource;
  private final ConventionBundleSource _conventionSource;
  private final RegionSource _regionSource;

  public InterestRateFutureSecurityConverter(final HolidaySource holidaySource,
      final ConventionBundleSource conventionSource, final RegionSource regionSource) {
    Validate.notNull(holidaySource, "holiday source");
    Validate.notNull(conventionSource, "convention source");
    Validate.notNull(regionSource, "region source");
    _holidaySource = holidaySource;
    _conventionSource = conventionSource;
    _regionSource = regionSource;
  }

  public InterestRateFutureSecurityDefinition convert(final InterestRateFutureSecurity security) {
    Validate.notNull(security, "security");
    final ZonedDateTime lastTradeDate = security.getExpiry().getExpiry(); //TODO is this the same thing? 
    final Currency currency = security.getCurrency();
    final ConventionBundle iborConvention = _conventionSource.getConventionBundle(Identifier.of(
        InMemoryConventionBundleMaster.SIMPLE_NAME_SCHEME, currency.getCode() + "_IRFUTURE")); //TODO this is wrong - need to get the ibor convention
    final Identifier regionId = null;
    final Calendar calendar = CalendarUtil.getCalendar(_regionSource, _holidaySource, regionId);
    final IborIndex iborIndex = new IborIndex(currency, iborConvention.getPeriod(), iborConvention.getSettlementDays(),
        calendar, iborConvention.getDayCount(), iborConvention.getBusinessDayConvention(),
        iborConvention.isEOMConvention());
    final double notional = 0; //TODO how do we get the notional?
    final double paymentAccrualFactor = iborConvention.getFutureYearFraction();
    return new InterestRateFutureSecurityDefinition(lastTradeDate, iborIndex, notional, paymentAccrualFactor);
  }

}
