/**
 * Copyright (C) 2009 - 2011 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.fixedincome;

import java.util.HashSet;
import java.util.Set;

import javax.time.calendar.ZonedDateTime;

import org.apache.commons.lang.Validate;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.core.holiday.HolidaySource;
import com.opengamma.core.region.Region;
import com.opengamma.core.region.RegionSource;
import com.opengamma.core.region.RegionUtils;
import com.opengamma.core.security.Security;
import com.opengamma.financial.analytics.ircurve.FixedIncomeStripWithSecurity;
import com.opengamma.financial.analytics.ircurve.StripInstrumentType;
import com.opengamma.financial.convention.ConventionBundle;
import com.opengamma.financial.convention.ConventionBundleSource;
import com.opengamma.financial.convention.HolidaySourceCalendarAdapter;
import com.opengamma.financial.convention.InMemoryConventionBundleMaster;
import com.opengamma.financial.convention.businessday.BusinessDayConvention;
import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.financial.instrument.FixedIncomeFutureInstrumentDefinition;
import com.opengamma.financial.instrument.future.IRFutureConvention;
import com.opengamma.financial.instrument.future.IRFutureDefinition;
import com.opengamma.financial.interestrate.future.definition.InterestRateFuture;
import com.opengamma.financial.security.future.InterestRateFutureSecurity;
import com.opengamma.id.Identifier;

/**
 * 
 */
public class FixedIncomeStripToFixedIncomeFutureDefinitionConverter {
  private final HolidaySource _holidaySource;
  private final ConventionBundleSource _conventionSource;
  private final RegionSource _regionSource;

  public FixedIncomeStripToFixedIncomeFutureDefinitionConverter(final HolidaySource holidaySource, final ConventionBundleSource conventionSource, final RegionSource regionSource) {
    Validate.notNull(holidaySource, "holiday source");
    Validate.notNull(conventionSource, "convention source");
    Validate.notNull(regionSource, "region source");
    _holidaySource = holidaySource;
    _conventionSource = conventionSource;
    _regionSource = regionSource;
  }

  public FixedIncomeFutureInstrumentDefinition<?> getDefinitionForSecurity(final FixedIncomeStripWithSecurity strip) {
    Validate.notNull(strip, "strip");
    final StripInstrumentType type = strip.getInstrumentType();
    final Security security = strip.getSecurity();
    switch (type) {
      case FUTURE:
        return getIRFutureDefinition((InterestRateFutureSecurity) security);
      default:
        throw new OpenGammaRuntimeException("Do not know how to handle StripInstrumentType " + type);
    }
  }

  private FixedIncomeFutureInstrumentDefinition<InterestRateFuture> getIRFutureDefinition(final InterestRateFutureSecurity irSecurity) {
    final String currency = irSecurity.getCurrency().getISOCode();
    final ConventionBundle conventions = _conventionSource.getConventionBundle(Identifier.of(InMemoryConventionBundleMaster.SIMPLE_NAME_SCHEME, currency + "_IRFUTURE"));
    //final Identifier regionId = irSecurity.getRegion().getIdentityKey();
    final Identifier regionId = null;
    final Calendar calendar = getCalendar(regionId);
    final BusinessDayConvention businessDayConvention = conventions.getBusinessDayConvention();
    // Expiry is actually the last trade date rather than the expiry date of the future so we have to go forward in time.
    final ZonedDateTime lastTradeDate = businessDayConvention.adjustDate(calendar, irSecurity.getExpiry().getExpiry());
    final ZonedDateTime settlementDate = businessDayConvention.adjustDate(calendar, lastTradeDate.plusDays(conventions.getSettlementDays()));
    final ZonedDateTime maturityDate = businessDayConvention.adjustDate(calendar, settlementDate.plusMonths(3));
    final IRFutureConvention convention = new IRFutureConvention(conventions.getSettlementDays(), conventions.getDayCount(), businessDayConvention, calendar, conventions.getFutureYearFraction(),
        currency + "_IR_FUTURE_CONVENTION");
    return new IRFutureDefinition(lastTradeDate, maturityDate, convention);
  }

  // REVIEW: jim 8-Oct-2010 -- we might want to move this logic inside the RegionMaster.
  private Calendar getCalendar(final Identifier regionId) {
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

}
