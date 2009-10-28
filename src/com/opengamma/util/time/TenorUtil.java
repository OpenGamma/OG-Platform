/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.util.time;

import javax.time.calendar.ZonedDateTime;

/**
 * 
 * 
 * @author emcleod
 */
public class TenorUtil {

  public static double getDaysInTenor(final Tenor tenor) {
    return tenor.getPeriod().totalHoursWith24HourDays() / 24.;
  }

  public static ZonedDateTime getDateWithTenorOffset(final ZonedDateTime date, final Tenor tenor) {
    return date.minus(tenor.getPeriod());
  }

  public static double getTenorsInTenor(final Tenor first, final Tenor second) {
    return TenorUtil.getDaysInTenor(first) / TenorUtil.getDaysInTenor(second);
  }
}
