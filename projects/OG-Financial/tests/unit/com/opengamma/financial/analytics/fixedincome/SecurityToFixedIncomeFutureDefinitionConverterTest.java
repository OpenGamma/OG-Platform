/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.fixedincome;

import org.testng.annotations.Test;
import javax.time.calendar.LocalDate;
import javax.time.calendar.TimeZone;

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
import com.opengamma.id.Identifier;
import com.opengamma.id.IdentifierBundle;
import com.opengamma.id.UniqueId;
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
    public boolean isHoliday(final LocalDate dateToCheck, final Currency currency) {
      return WEEKEND_HOLIDAY.isWorkingDay(dateToCheck);
    }

    @Override
    public boolean isHoliday(final LocalDate dateToCheck, final HolidayType holidayType,
        final IdentifierBundle regionOrExchangeIds) {
      return WEEKEND_HOLIDAY.isWorkingDay(dateToCheck);
    }

    @Override
    public boolean isHoliday(final LocalDate dateToCheck, final HolidayType holidayType,
        final Identifier regionOrExchangeId) {
      return WEEKEND_HOLIDAY.isWorkingDay(dateToCheck);
    }

    @Override
    public Holiday getHoliday(final UniqueId uid) {
      return null;
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
      public IdentifierBundle getIdentifiers() {
        return null;
      }

      @Override
      public String getName() {
        return null;
      }

      @Override
      public IdentifierBundle getRegionKey() {
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
    public Exchange getSingleExchange(final Identifier identifier) {
      return EXCHANGE;
    }

    @Override
    public Exchange getSingleExchange(final IdentifierBundle identifierBundle) {
      return EXCHANGE;
    }

  }
}
