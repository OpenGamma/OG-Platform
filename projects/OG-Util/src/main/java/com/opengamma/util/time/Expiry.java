/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.util.time;

import java.io.Serializable;

import org.threeten.bp.Instant;
import org.threeten.bp.ZonedDateTime;

import com.opengamma.util.ArgumentChecker;

/**
 * An indication of when something expires.
 */
public class Expiry implements Serializable {

  /** Serialization version. */
  private static final long serialVersionUID = 1L;

  /**
   * The expiry date-time.
   */
  private final ZonedDateTime _expiry;
  /**
   * The accuracy of the expiry.
   */
  private final ExpiryAccuracy _accuracy;

  /**
   * Creates an expiry with no specific accuracy.
   * @param expiry  the expiry date-time
   */
  public Expiry(final ZonedDateTime expiry) {
    this(expiry, ExpiryAccuracy.DAY_MONTH_YEAR);
  }

  /**
   * Creates an expiry with an accuracy.
   * @param expiry  the expiry date-time, not-null
   * @param accuracy  the accuracy
   */
  public Expiry(final ZonedDateTime expiry, final ExpiryAccuracy accuracy) {
    ArgumentChecker.notNull(expiry, "expiry");
    ArgumentChecker.notNull(accuracy, "accuracy");
    _expiry = expiry;
    _accuracy = accuracy;
  }

  //-------------------------------------------------------------------------
  /**
   * Gets the expiry date-time.
   * @return the date-time
   */
  // TODO Consider changing this to getZonedDateTime
  public ZonedDateTime getExpiry() {
    return _expiry;
  }

  /**
   * Gets the accuracy of the expiry.
   * @return the accuracy
   */
  public ExpiryAccuracy getAccuracy() {
    return _accuracy;
  }

  /**
   * Converts the expiry date-time to an instant.
   * 
   * @return the instant of the expiry, not null
   */
  public Instant toInstant() {
    return _expiry.toInstant();
  }

  //-------------------------------------------------------------------------
  @Override
  public boolean equals(final Object obj) {
    if (this == obj) {
      return true;
    }
    if (!(obj instanceof Expiry)) {
      return false;
    }
    final Expiry other = (Expiry) obj;
    // Can't be the same if different accuracies encoded
    if (!getAccuracy().equals(other.getAccuracy())) {
      return false;
    }
    // Only compare to the accuracy agreed
    return equalsToAccuracy(getAccuracy(), getExpiry(), other.getExpiry());
  }

  /**
   * Compares two expiry dates for equality to the given level of accuracy only.
   *
   * @param accuracy  the accuracy to compare to, not null
   * @param expiry1  the first date/time to compare, not null
   * @param expiry2  the second date/time to compare, not null
   * @return true if the two dates/times are equal to the requested accuracy
   */
  public static boolean equalsToAccuracy(final ExpiryAccuracy accuracy, final ZonedDateTime expiry1, final ZonedDateTime expiry2) {
    switch (accuracy) {
      case MIN_HOUR_DAY_MONTH_YEAR:
        return (expiry1.getMinute() == expiry2.getMinute()) && (expiry1.getHour() == expiry2.getHour()) && (expiry1.getDayOfMonth() == expiry2.getDayOfMonth())
            && (expiry1.getMonth() == expiry2.getMonth()) && (expiry1.getYear() == expiry2.getYear());
      case HOUR_DAY_MONTH_YEAR:
        return (expiry1.getHour() == expiry2.getHour()) && (expiry1.getDayOfMonth() == expiry2.getDayOfMonth()) && (expiry1.getMonth() == expiry2.getMonth())
            && (expiry1.getYear() == expiry2.getYear());
      case DAY_MONTH_YEAR:
        return (expiry1.getDayOfMonth() == expiry2.getDayOfMonth()) && (expiry1.getMonth() == expiry2.getMonth()) && (expiry1.getYear() == expiry2.getYear());
      case MONTH_YEAR:
        return (expiry1.getMonth() == expiry2.getMonth()) && (expiry1.getYear() == expiry2.getYear());
      case YEAR:
        return (expiry1.getYear() == expiry2.getYear());
      default:
        throw new IllegalArgumentException("accuracy");
    }
  }

  @Override
  public int hashCode() {
    return (_accuracy != null ? _accuracy.hashCode() : 0) ^ _expiry.hashCode();
  }

  @Override
  public String toString() {
    if (_accuracy != null) {
      return "Expiry[" + _expiry + " accuracy " + _accuracy + "]";
    } else {
      return "Expiry[" + _expiry + "]";
    }
  }

}
