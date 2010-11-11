/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.master.holiday.impl;

import java.util.Collections;

import javax.time.Instant;
import javax.time.InstantProvider;
import javax.time.calendar.DayOfWeek;
import javax.time.calendar.LocalDate;

import com.opengamma.DataNotFoundException;
import com.opengamma.core.common.Currency;
import com.opengamma.core.holiday.HolidaySource;
import com.opengamma.core.holiday.HolidayType;
import com.opengamma.id.Identifier;
import com.opengamma.id.IdentifierBundle;
import com.opengamma.id.UniqueIdentifier;
import com.opengamma.master.holiday.HolidayDocument;
import com.opengamma.master.holiday.HolidayMaster;
import com.opengamma.master.holiday.HolidaySearchRequest;
import com.opengamma.master.holiday.ManageableHoliday;
import com.opengamma.util.ArgumentChecker;

/**
 * A {@code HolidaySource} implemented using an underlying {@code HolidayMaster}.
 * <p>
 * The {@link HolidaySource} interface provides holidays to the application via a narrow API.
 * This class provides the source on top of a standard {@link HolidayMaster}.
 */
public class MasterHolidaySource implements HolidaySource {

  /**
   * The underlying master.
   */
  private final HolidayMaster _holidayMaster;
  /**
   * The instant to search for a version at.
   * Null is treated as the latest version.
   */
  private final Instant _versionAsOfInstant;
  /**
   * The instant to search for corrections for.
   * Null is treated as the latest correction.
   */
  private final Instant _correctedToInstant;

  /**
   * Creates an instance with an underlying holiday master.
   * 
   * @param holidayMaster  the holiday master, not null
   */
  public MasterHolidaySource(final HolidayMaster holidayMaster) {
    this(holidayMaster, null, null);
  }

  /**
   * Creates an instance with an underlying holiday master viewing the version
   * that existed on the specified instant.
   * 
   * @param holidayMaster  the holiday master, not null
   * @param versionAsOfInstantProvider  the version instant to retrieve, null for latest version
   */
  public MasterHolidaySource(final HolidayMaster holidayMaster, final InstantProvider versionAsOfInstantProvider) {
    this(holidayMaster, versionAsOfInstantProvider, null);
  }

  /**
   * Creates an instance with an underlying holiday master viewing the version
   * that existed on the specified instant as corrected to the correction instant.
   * 
   * @param holidayMaster  the holiday master, not null
   * @param versionAsOfInstantProvider  the version instant to retrieve, null for latest version
   * @param correctedToInstantProvider  the instant that the data should be corrected to, null for latest correction
   */
  public MasterHolidaySource(final HolidayMaster holidayMaster, final InstantProvider versionAsOfInstantProvider, final InstantProvider correctedToInstantProvider) {
    ArgumentChecker.notNull(holidayMaster, "holidayMaster");
    _holidayMaster = holidayMaster;
    if (versionAsOfInstantProvider != null) {
      _versionAsOfInstant = Instant.of(versionAsOfInstantProvider);
    } else {
      _versionAsOfInstant = null;
    }
    if (correctedToInstantProvider != null) {
      _correctedToInstant = Instant.of(correctedToInstantProvider);
    } else {
      _correctedToInstant = null;
    }
  }

  //-------------------------------------------------------------------------
  /**
   * Gets the underlying holiday master.
   * 
   * @return the holiday master, not null
   */
  public HolidayMaster getHolidayMaster() {
    return _holidayMaster;
  }

  /**
   * Gets the version instant to retrieve.
   * 
   * @return the version instant to retrieve, null for latest version
   */
  public Instant getVersionAsOfInstant() {
    return _versionAsOfInstant;
  }

  /**
   * Gets the instant that the data should be corrected to.
   * 
   * @return the instant that the data should be corrected to, null for latest correction
   */
  public Instant getCorrectedToInstant() {
    return _correctedToInstant;
  }

  //-------------------------------------------------------------------------
  @Override
  public ManageableHoliday getHoliday(UniqueIdentifier uid) {
    try {
      return getHolidayMaster().get(uid).getHoliday();
    } catch (DataNotFoundException ex) {
      return null;
    }
  }

  @Override
  public boolean isHoliday(final LocalDate dateToCheck, final Currency currency) {
    HolidaySearchRequest request = new HolidaySearchRequest(currency);
    request.setVersionAsOfInstant(_versionAsOfInstant);
    request.setCorrectedToInstant(_correctedToInstant);
    return isHoliday(request, dateToCheck);
  }

  @Override
  public boolean isHoliday(final LocalDate dateToCheck, final HolidayType holidayType, final IdentifierBundle regionOrExchangeIds) {
    HolidaySearchRequest request = new HolidaySearchRequest(holidayType, regionOrExchangeIds);
    request.setVersionAsOfInstant(_versionAsOfInstant);
    request.setCorrectedToInstant(_correctedToInstant);
    return isHoliday(request, dateToCheck);
  }

  @Override
  public boolean isHoliday(final LocalDate dateToCheck, final HolidayType holidayType, final Identifier regionOrExchangeId) {
    HolidaySearchRequest request = new HolidaySearchRequest(holidayType, IdentifierBundle.of(regionOrExchangeId));
    request.setVersionAsOfInstant(_versionAsOfInstant);
    request.setCorrectedToInstant(_correctedToInstant);
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
    HolidayDocument doc = getHolidayMaster().search(request).getFirstDocument();
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

  //-------------------------------------------------------------------------
  @Override
  public String toString() {
    String str = "MasterHolidaySource[" + getHolidayMaster();
    if (_versionAsOfInstant != null) {
      str += ",versionAsOf=" + _versionAsOfInstant;
    }
    if (_versionAsOfInstant != null) {
      str += ",correctedTo=" + _correctedToInstant;
    }
    return str + "]";
  }

}
