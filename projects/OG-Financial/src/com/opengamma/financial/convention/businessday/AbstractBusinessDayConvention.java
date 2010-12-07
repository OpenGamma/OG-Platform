/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.convention.businessday;

import javax.time.calendar.DateAdjuster;
import javax.time.calendar.LocalDate;
import javax.time.calendar.ZonedDateTime;

import com.opengamma.core.convention.BusinessDayConvention;
import com.opengamma.core.convention.Calendar;

/**
 * Abstract implementation of a convention for handling business days.
 */
public abstract class AbstractBusinessDayConvention implements BusinessDayConvention {

  @Override
  public ZonedDateTime adjustDate(final Calendar workingDayCalendar, final ZonedDateTime dateTime) {
    LocalDate adjusted = adjustDate(workingDayCalendar, dateTime.toLocalDate());
    return ZonedDateTime.of(adjusted, dateTime.toLocalTime(), dateTime.getZone());
  }

  @Override
  public DateAdjuster getDateAdjuster(final Calendar workingDayCalendar) {
    return new BusinessDayConventionWithCalendar(this, workingDayCalendar);
  }

}
