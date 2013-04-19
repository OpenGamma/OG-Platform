/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.timeseries.precise.instant;

import org.threeten.bp.Instant;
import org.threeten.bp.jdk8.Jdk8Methods;

/**
 * An encoder between {@code Instant} and {@code long}.
 * <p>
 * Any far future or maximum instant must be converted to {@code Long.MAX_VALUE}.
 * Any far past or minimum instant must be converted to {@code Long.MIN_VALUE}.
 * Other values are encoded as the number of nanoseconds from 1970-01-01, with
 * a range of +-292 years.
 */
public final class InstantToLongConverter {

  /**
   * Restricted constructor.
   */
  private InstantToLongConverter() {
  }

  //-------------------------------------------------------------------------
  /**
   * Converts a {@code Instant} to a {@code long}.
   * <p>
   * See the class Javadoc for the format of the {@code long}.
   * 
   * @param instant  the instant to convert, not null
   * @return the {@code long} equivalent
   * @throws IllegalArgumentException if the instant is too large
   */
  public static long convertToLong(Instant instant) {
    if (instant.equals(Instant.MAX)) {
      return Long.MAX_VALUE;
    }
    if (instant.equals(Instant.MIN)) {
      return Long.MIN_VALUE;
    }
    try {
      long secs = Jdk8Methods.safeMultiply(instant.getEpochSecond(), 1_000_000_000);
      return Jdk8Methods.safeAdd(secs, instant.getNano());
    } catch (RuntimeException ex) {
      throw new IllegalArgumentException("Instant is too large/small: " + instant);
    }
  }

  /**
   * Converts a {@code long} to an {@code Instant}.
   * <p>
   * See the class Javadoc for the format of the {@code long}.
   * 
   * @param instant  the {@code long} nanos to convert, not null
   * @return the {@code Instant} equivalent, not null
   */
  public static Instant convertToInstant(long instant) {
    if (instant == Long.MAX_VALUE) {
      return Instant.MAX;
    }
    if (instant == Long.MIN_VALUE) {
      return Instant.MIN;
    }
    return Instant.ofEpochSecond(0, instant);
  }

}
