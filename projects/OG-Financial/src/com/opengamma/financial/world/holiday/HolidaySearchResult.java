/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.world.holiday;

import java.util.Collection;

import javax.time.calendar.LocalDate;

import org.apache.commons.lang.Validate;

import com.opengamma.OpenGammaRuntimeException;

/**
 * Container for result documents from a holiday search
 */
public class HolidaySearchResult {
  private Collection<HolidayDocument> _results;
  private LocalDate _holidayDate;
  private boolean _isHoliday;
  
  public HolidaySearchResult(LocalDate holidayDate, boolean isHoliday) {
    //Validate.notNull(holidayDate, "Holiday Date");
    _holidayDate = holidayDate;
    _isHoliday = isHoliday;
  }

  public HolidaySearchResult(Collection<HolidayDocument> holidayDocuments) {
    Validate.notNull(holidayDocuments, "Holiday Documents");
    _results = holidayDocuments;
  }
  
  public Collection<HolidayDocument> getResults() {
    return _results;
  }
  
  /**
   * @throws OpenGammaRuntimeException if called when results are a collection rather than a point lookup
   * @return true if it's a holiday or false if it's not a holiday OR if search found no data (check holiday date for null)
   */
  public boolean isHoliday() {
    if (_results != null) {
      throw new OpenGammaRuntimeException("Cannot test if holiday when results contain holiday objects");
    }
    return _isHoliday;
  }
  
  /**
   * @throws OpenGammaRuntimeException if called when results are a collection rather than a point lookup
   * @return holiday date, or null if search found no data
   */
  public LocalDate getHolidayDate() {
    if (_results != null) {
      throw new OpenGammaRuntimeException("Cannot test if holiday when results contain holiday objects");
    }
    return _holidayDate;
  }
}
