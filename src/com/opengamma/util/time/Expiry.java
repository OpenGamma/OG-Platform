/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.util.time;

import javax.time.Instant;
import javax.time.InstantProvider;
import javax.time.calendar.ZonedDateTime;

import org.apache.commons.lang.ObjectUtils;

/**
 * An indication of when something expires.
 */
public class Expiry implements InstantProvider {

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
    _expiry = expiry;
    _accuracy = null;
  }

  /**
   * Creates an expiry with an accuracy.
   * @param expiry  the expiry date-time
   * @param accuracy  the accuracy
   */
  public Expiry(final ZonedDateTime expiry, final ExpiryAccuracy accuracy) {
    _expiry = expiry;
    _accuracy = accuracy;
  }

  //-------------------------------------------------------------------------
  /**
   * Gets the expiry date-time.
   * @return the date-time
   */
  // we probably don't need this.
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

  @Override
  public Instant toInstant() {
    return _expiry.toInstant();
  }

  //-------------------------------------------------------------------------
  @Override
  public boolean equals(final Object obj) {
    if (!(obj instanceof Expiry)) {
      return false;
    }
    final Expiry other = (Expiry) obj;
    if (!ObjectUtils.equals(other.getAccuracy(), getAccuracy())) {
      return false;
    }
    if (getExpiry() == null) {
      return (other.getExpiry() == null);
    }
    if (other.getExpiry() == null) {
      return false;
    }
    // Only compare to the accuracy agreed
    switch (getAccuracy()) {
      case DAY_MONTH_YEAR:
        return (getExpiry().getDayOfMonth() == other.getExpiry().getDayOfMonth()) && (getExpiry().getMonthOfYear() == other.getExpiry().getMonthOfYear())
            && (getExpiry().getYear() == other.getExpiry().getYear());
      case MONTH_YEAR:
        return (getExpiry().getMonthOfYear() == other.getExpiry().getMonthOfYear()) && (getExpiry().getYear() == other.getExpiry().getYear());
      case YEAR:
        return (getExpiry().getYear() == other.getExpiry().getYear());
    }
    return false;
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
