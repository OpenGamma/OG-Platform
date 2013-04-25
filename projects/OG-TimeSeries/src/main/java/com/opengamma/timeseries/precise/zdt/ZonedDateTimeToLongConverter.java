/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.timeseries.precise.zdt;

import org.threeten.bp.Instant;
import org.threeten.bp.LocalDateTime;
import org.threeten.bp.ZoneId;
import org.threeten.bp.ZonedDateTime;
import org.threeten.bp.jdk8.Jdk8Methods;

/**
 * An encoder between {@code ZonedDateTime} and {@code long}.
 * <p>
 * Any far future or maximum instant must be converted to {@code Long.MAX_VALUE}.
 * Any far past or minimum instant must be converted to {@code Long.MIN_VALUE}.
 * Other values are encoded as the number of nanoseconds from 1970-01-01, with
 * a range of +-292 years.
 */
public final class ZonedDateTimeToLongConverter {

  /**
   * Restricted constructor.
   */
  private ZonedDateTimeToLongConverter() {
  }

  //-------------------------------------------------------------------------
  /**
   * Converts a {@code ZonedDateTime} to a {@code long}.
   * <p>
   * See the class Javadoc for the format of the {@code long}.
   * 
   * @param instant  the instant to convert, not null
   * @return the {@code long} equivalent
   * @throws IllegalArgumentException if the instant is too large
   */
  public static long convertToLong(ZonedDateTime instant) {
    if (instant.getYear() >= 1_000_000) {
      return Long.MAX_VALUE;
    }
    if (instant.getYear() <= -1_000_000) {
      return Long.MIN_VALUE;
    }
    try {
      long secs = Jdk8Methods.safeMultiply(instant.toEpochSecond(), 1_000_000_000);
      return Jdk8Methods.safeAdd(secs, instant.getNano());
    } catch (RuntimeException ex) {
      throw new IllegalArgumentException("ZonedDateTime is too large/small: " + instant);
    }
  }

  /**
   * Converts a {@code long} to an {@code ZonedDateTime}.
   * <p>
   * See the class Javadoc for the format of the {@code long}.
   * 
   * @param instant  the {@code long} nanos to convert, not null
   * @param zone  the zone to use, not null
   * @return the {@code ZonedDateTime} equivalent, not null
   */
  public static ZonedDateTime convertToZonedDateTime(long instant, ZoneId zone) {
    if (instant == Long.MAX_VALUE) {
      return LocalDateTime.MAX.atZone(zone);
    }
    if (instant == Long.MIN_VALUE) {
      return LocalDateTime.MIN.atZone(zone);
    }
    return Instant.ofEpochSecond(0, instant).atZone(zone);
  }

}
