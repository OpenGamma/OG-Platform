/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.core.holiday;

import static org.threeten.bp.DayOfWeek.SATURDAY;
import static org.threeten.bp.DayOfWeek.SUNDAY;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;

import org.threeten.bp.LocalDate;

import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.id.ObjectId;
import com.opengamma.id.UniqueId;
import com.opengamma.id.VersionCorrection;
import com.opengamma.util.money.Currency;

/**
 * A holiday source that delegates to an another source, but which ensures that
 * it only calls the get methods on the delegate. This is intended to allow
 * the use of proxy classes as the delegates which allows different
 * behaviours e.g. capturing the data returned from sources.
 */
public class NarrowingHolidaySource implements HolidaySource {

  private final HolidaySource _delegate;

  /**
   * Create a narrowing source, wrapping the provided source.
   *
   * @param delegate the source to delegate to, not null
   */
  public NarrowingHolidaySource(HolidaySource delegate) {
    _delegate = delegate;
  }

  @Override
  public Collection<Holiday> get(HolidayType holidayType,
                                 ExternalIdBundle regionOrExchangeIds) {
    return _delegate.get(holidayType, regionOrExchangeIds);
  }

  @Override
  public Collection<Holiday> get(Currency currency) {
    return _delegate.get(currency);
  }

  @Override
  public boolean isHoliday(LocalDate dateToCheck, Currency currency) {
    return isWeekend(dateToCheck) || isHoliday(dateToCheck, get(currency));
  }

  @Override
  public boolean isHoliday(LocalDate dateToCheck, HolidayType holidayType, ExternalIdBundle regionOrExchangeIds) {
    return isWeekend(dateToCheck) || isHoliday(dateToCheck, get(holidayType, regionOrExchangeIds));
  }

  @Override
  public boolean isHoliday(LocalDate dateToCheck, HolidayType holidayType, ExternalId regionOrExchangeId) {
    return isHoliday(dateToCheck, holidayType, regionOrExchangeId.toBundle());
  }

  private boolean isHoliday(LocalDate dateToCheck, Collection<Holiday> holidays) {
    for (Holiday holiday : holidays) {
      if (Collections.binarySearch(holiday.getHolidayDates(), dateToCheck) >= 0) {
        return true;
      }
    }
    return false;
  }

  @Override
  public Holiday get(UniqueId uniqueId) {
    return _delegate.get(uniqueId);
  }

  @Override
  public Holiday get(ObjectId objectId, VersionCorrection versionCorrection) {
    return _delegate.get(objectId, versionCorrection);
  }

  @Override
  public Map<UniqueId, Holiday> get(Collection<UniqueId> uniqueIds) {
    return _delegate.get(uniqueIds);
  }

  @Override
  public Map<ObjectId, Holiday> get(Collection<ObjectId> objectIds, VersionCorrection versionCorrection) {
    return _delegate.get(objectIds, versionCorrection);
  }

  private boolean isWeekend(LocalDate date) {
    return date.getDayOfWeek() == SATURDAY || date.getDayOfWeek() == SUNDAY;
  }

}
