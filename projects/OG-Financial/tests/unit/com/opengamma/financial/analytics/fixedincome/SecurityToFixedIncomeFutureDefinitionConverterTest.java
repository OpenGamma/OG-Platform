/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.fixedincome;

import java.util.Collection;
import java.util.Collections;

import javax.time.calendar.LocalDate;
import javax.time.calendar.TimeZone;

import org.testng.annotations.Test;

import com.opengamma.core.exchange.Exchange;
import com.opengamma.core.exchange.ExchangeSource;
import com.opengamma.core.holiday.Holiday;
import com.opengamma.core.holiday.HolidaySource;
import com.opengamma.core.holiday.HolidayType;
import com.opengamma.financial.convention.ConventionBundleSource;
import com.opengamma.financial.convention.DefaultConventionBundleSource;
import com.opengamma.financial.convention.InMemoryConventionBundleMaster;
import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.financial.convention.calendar.MondayToFridayCalendar;
import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.id.ObjectId;
import com.opengamma.id.UniqueId;
import com.opengamma.id.VersionCorrection;
import com.opengamma.util.money.Currency;

/**
 * 
 */
public class SecurityToFixedIncomeFutureDefinitionConverterTest {
  private static final HolidaySource HOLIDAY_SOURCE = new MyHolidaySource();
  private static final ExchangeSource EXCHANGE_SOURCE = new MyExchangeSource();
  private static final ConventionBundleSource CONVENTION_SOURCE = new DefaultConventionBundleSource(
      new InMemoryConventionBundleMaster());

  private static class MyHolidaySource implements HolidaySource {
    private static final Calendar WEEKEND_HOLIDAY = new MondayToFridayCalendar("D");

    @Override
    public Holiday getHoliday(final UniqueId uniqueId) {
      throw new UnsupportedOperationException();
    }
    @Override
    public Holiday getHoliday(final ObjectId objectId, final VersionCorrection versionCorrection) {
      throw new UnsupportedOperationException();
    }
    @Override
    public boolean isHoliday(final LocalDate dateToCheck, final Currency currency) {
      return WEEKEND_HOLIDAY.isWorkingDay(dateToCheck);
    }
    @Override
    public boolean isHoliday(final LocalDate dateToCheck, final HolidayType holidayType,
        final ExternalIdBundle regionOrExchangeIds) {
      return WEEKEND_HOLIDAY.isWorkingDay(dateToCheck);
    }
    @Override
    public boolean isHoliday(final LocalDate dateToCheck, final HolidayType holidayType,
        final ExternalId regionOrExchangeId) {
      return WEEKEND_HOLIDAY.isWorkingDay(dateToCheck);
    }
  }

  @Test
  public void test() {
    //TODO
  }
  private static class MyExchangeSource implements ExchangeSource {
    private static final Exchange EXCHANGE = new Exchange() {

      @Override
      public UniqueId getUniqueId() {
        return UniqueId.of("SOMETHING", "SOMETHING ELSE");
      }

      @Override
      public ExternalIdBundle getExternalIdBundle() {
        return null;
      }

      @Override
      public String getName() {
        return null;
      }

      @Override
      public ExternalIdBundle getRegionIdBundle() {
        return null;
      }

      @Override
      public TimeZone getTimeZone() {
        return null;
      }

    };

    @Override
    public Exchange getExchange(final UniqueId uid) {
      return EXCHANGE;
    }
    @Override
    public Exchange getExchange(ObjectId objectId, VersionCorrection versionCorrection) {
      return EXCHANGE;
    }
    @Override
    public Collection<? extends Exchange> getExchanges(ExternalIdBundle bundle, VersionCorrection versionCorrection) {
      return Collections.singleton(EXCHANGE);
    }
    @Override
    public Exchange getSingleExchange(final ExternalId identifier) {
      return EXCHANGE;
    }
    @Override
    public Exchange getSingleExchange(final ExternalIdBundle identifierBundle) {
      return EXCHANGE;
    }
  }
}
