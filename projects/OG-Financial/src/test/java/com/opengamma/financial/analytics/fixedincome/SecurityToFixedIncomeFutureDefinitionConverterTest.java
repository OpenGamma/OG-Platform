/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.fixedincome;

import java.util.Collection;
import java.util.Collections;

import org.mockito.Mockito;
import org.mockito.stubbing.OngoingStubbing;
import org.testng.annotations.Test;
import org.threeten.bp.LocalDate;

import com.opengamma.core.AbstractSource;
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
import com.opengamma.util.test.TestGroup;

/**
 * Test.
 */
@Test(groups = TestGroup.UNIT)
public class SecurityToFixedIncomeFutureDefinitionConverterTest {

  private static final HolidaySource HOLIDAY_SOURCE = new MyHolidaySource();
  private static final ExchangeSource EXCHANGE_SOURCE = myExchangeSource();
  private static final ConventionBundleSource CONVENTION_SOURCE = new DefaultConventionBundleSource(
      new InMemoryConventionBundleMaster());

  private static class MyHolidaySource extends AbstractSource<Holiday> implements HolidaySource {
    private static final Calendar WEEKEND_HOLIDAY = new MondayToFridayCalendar("D");

    @Override
    public Holiday get(final UniqueId uniqueId) {
      throw new UnsupportedOperationException();
    }

    @Override
    public Holiday get(final ObjectId objectId, final VersionCorrection versionCorrection) {
      throw new UnsupportedOperationException();
    }

    @Override
    public Collection<Holiday> get(HolidayType holidayType,
                                   ExternalIdBundle regionOrExchangeIds) {
      throw new UnsupportedOperationException();
    }

    @Override
    public Collection<Holiday> get(Currency currency) {
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

  @SuppressWarnings("unchecked")
  private static ExchangeSource myExchangeSource() {
    final Exchange exchange = Mockito.mock(Exchange.class);
    Mockito.when(exchange.getUniqueId()).thenReturn(UniqueId.of("SOMETHING", "SOMETHING ELSE"));
    final ExchangeSource source = Mockito.mock(ExchangeSource.class);
    Mockito.when(source.get(Mockito.any(UniqueId.class))).thenReturn(exchange);
    Mockito.when(source.get(Mockito.any(ObjectId.class), Mockito.any(VersionCorrection.class))).thenReturn(exchange);
    ((OngoingStubbing) Mockito.when(source.get(Mockito.any(ExternalIdBundle.class), Mockito.any(VersionCorrection.class)))).thenReturn(Collections.singleton(exchange));
    Mockito.when(source.getSingle(Mockito.any(ExternalId.class))).thenReturn(exchange);
    Mockito.when(source.getSingle(Mockito.any(ExternalIdBundle.class))).thenReturn(exchange);
    return source;
  }

}
