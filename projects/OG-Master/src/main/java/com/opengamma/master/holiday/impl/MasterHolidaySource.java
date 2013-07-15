/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.master.holiday.impl;

import java.util.Collections;

import org.threeten.bp.DayOfWeek;
import org.threeten.bp.LocalDate;

import com.opengamma.core.holiday.Holiday;
import com.opengamma.core.holiday.HolidaySource;
import com.opengamma.core.holiday.HolidayType;
import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.id.VersionCorrection;
import com.opengamma.master.AbstractMasterSource;
import com.opengamma.master.holiday.HolidayDocument;
import com.opengamma.master.holiday.HolidayMaster;
import com.opengamma.master.holiday.HolidaySearchRequest;
import com.opengamma.util.PublicSPI;
import com.opengamma.util.money.Currency;

/**
 * A {@code HolidaySource} implemented using an underlying {@code HolidayMaster}.
 * <p>
 * The {@link HolidaySource} interface provides holidays to the application via a narrow API.
 * This class provides the source on top of a standard {@link HolidayMaster}.
 */
@PublicSPI
public class MasterHolidaySource
    extends AbstractMasterSource<Holiday, HolidayDocument, HolidayMaster>
    implements HolidaySource {

  /**
   * Creates an instance with an underlying master which does not override versions.
   * 
   * @param master  the master, not null
   */
  public MasterHolidaySource(final HolidayMaster master) {
    super(master);
  }

  /**
   * Creates an instance with an underlying master optionally overriding the requested version.
   * 
   * @param master  the master, not null
   * @param versionCorrection  the version-correction locator to search at, null to not override versions
   */
  public MasterHolidaySource(final HolidayMaster master, VersionCorrection versionCorrection) {
    super(master, versionCorrection);
  }

  //-------------------------------------------------------------------------
  @Override
  public boolean isHoliday(final LocalDate dateToCheck, final Currency currency) {
    HolidaySearchRequest request = new HolidaySearchRequest(currency);
    request.setVersionCorrection(getVersionCorrection());
    return isHoliday(request, dateToCheck);
  }

  @Override
  public boolean isHoliday(final LocalDate dateToCheck, final HolidayType holidayType, final ExternalIdBundle regionOrExchangeIds) {
    HolidaySearchRequest request = getSearchRequest(holidayType, regionOrExchangeIds);
    return isHoliday(request, dateToCheck);
  }

  protected HolidaySearchRequest getSearchRequest(final HolidayType holidayType, final ExternalIdBundle regionOrExchangeIds) {
    HolidaySearchRequest request = new HolidaySearchRequest(holidayType, regionOrExchangeIds);
    request.setVersionCorrection(getVersionCorrection());
    return request;
  }

  @Override
  public boolean isHoliday(final LocalDate dateToCheck, final HolidayType holidayType, final ExternalId regionOrExchangeId) {
    HolidaySearchRequest request = new HolidaySearchRequest(holidayType, ExternalIdBundle.of(regionOrExchangeId));
    request.setVersionCorrection(getVersionCorrection());
    return isHoliday(request, dateToCheck);
  }

  //-------------------------------------------------------------------------
  /**
   * Checks if the specified date is a holiday.
   * 
   * @param request  the request to search base on, not null
   * @param dateToCheck  the date to check, not null
   * @return true if the date is a holiday
   */
  protected boolean isHoliday(final HolidaySearchRequest request, final LocalDate dateToCheck) {
    if (isWeekend(dateToCheck)) {
      return true;
    }
    request.setDateToCheck(dateToCheck);
    HolidayDocument doc = getMaster().search(request).getFirstDocument();
    return isHoliday(doc, dateToCheck);
  }

  /**
   * Checks if the specified date is a holiday.
   * 
   * @param doc document retrieved from underlying holiday master, may be null
   * @param dateToCheck  the date to check, not null
   * @return false if nothing was retrieved from underlying holiday master. 
   * Otherwise, true if and only if the date is a holiday based on the underlying holiday master
   */
  protected boolean isHoliday(final HolidayDocument doc, final LocalDate dateToCheck) {
    if (doc == null) {
      return false;
    }
    return Collections.binarySearch(doc.getHoliday().getHolidayDates(), dateToCheck) >= 0;
  }

  /**
   * Checks if the date is at the weekend, defined as a Saturday or Sunday.
   * 
   * @param date  the date to check, not null
   * @return true if it is a weekend
   */
  protected boolean isWeekend(LocalDate date) {
    return (date.getDayOfWeek() == DayOfWeek.SATURDAY || date.getDayOfWeek() == DayOfWeek.SUNDAY);
  }

}
