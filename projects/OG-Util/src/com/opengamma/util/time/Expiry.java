/**
 * Copyright (C) 2009 - Present by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.util.time;

import java.io.Serializable;

import javax.time.Instant;
import javax.time.InstantProvider;
import javax.time.calendar.TimeZone;
import javax.time.calendar.ZonedDateTime;

import org.apache.commons.lang.ObjectUtils;
import org.fudgemsg.FudgeFieldContainer;
import org.fudgemsg.FudgeMessageFactory;
import org.fudgemsg.MutableFudgeFieldContainer;

import com.opengamma.util.ArgumentChecker;

/**
 * An indication of when something expires.
 */
public class Expiry implements InstantProvider, Serializable {

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
    if (this == obj) {
      return true;
    }
    if (!(obj instanceof Expiry)) {
      return false;
    }
    final Expiry other = (Expiry) obj;
    // REVIEW Yomi 20100905 expiry should not be null to start with
    if (getExpiry() == null) {
      return (other.getExpiry() == null);
    }
    if (other.getExpiry() == null) {
      return false;
    }
    if (getAccuracy() == null) {
      if (other.getAccuracy() != null) {
        return false;
      }
      return getExpiry().equalInstant(other.getExpiry());
    } else {
      if (!getAccuracy().equals(other.getAccuracy())) {
        return false;
      }
    }
    // Only compare to the accuracy agreed
    // REVIEW Yomi 20100905 is above "getExpiry().equalInstant(other.getExpiry()" not testing the same?
    if (getAccuracy() == null) {
      return ObjectUtils.equals(getExpiry(), other.getExpiry());
    }
    // convert both to UTC to compare with accuracy
    ZonedDateTime utc = ZonedDateTime.ofInstant(toInstant(), TimeZone.UTC);
    ZonedDateTime otherUtc = ZonedDateTime.ofInstant(other.toInstant(), TimeZone.UTC);
    switch (getAccuracy()) {
      case MIN_HOUR_DAY_MONTH_YEAR:
        return (utc.getMinuteOfHour() == otherUtc.getMinuteOfHour()) && (utc.getHourOfDay() == otherUtc.getHourOfDay()) && (utc.getDayOfMonth() == otherUtc.getDayOfMonth())
            && (utc.getMonthOfYear() == otherUtc.getMonthOfYear()) && (utc.getYear() == otherUtc.getYear());
      case HOUR_DAY_MONTH_YEAR:
        return (utc.getHourOfDay() == otherUtc.getHourOfDay()) && (utc.getDayOfMonth() == otherUtc.getDayOfMonth()) && (utc.getMonthOfYear() == otherUtc.getMonthOfYear())
            && (utc.getYear() == otherUtc.getYear());
      case DAY_MONTH_YEAR:
        return (utc.getDayOfMonth() == otherUtc.getDayOfMonth()) && (utc.getMonthOfYear() == otherUtc.getMonthOfYear()) && (utc.getYear() == otherUtc.getYear());
      case MONTH_YEAR:
        return (utc.getMonthOfYear() == otherUtc.getMonthOfYear()) && (utc.getYear() == otherUtc.getYear());
      case YEAR:
        return (utc.getYear() == otherUtc.getYear());
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

  //-------------------------------------------------------------------------
  /**
   * This is for more efficient code within the .proto representations of securities, allowing Expiry to be
   * used directly as a message type instead of through the serialization framework.
   * 
   * @param factory  the Fudge message factory
   * @param message  the message to populate
   */
  public void toFudgeMsg(final FudgeMessageFactory factory, final MutableFudgeFieldContainer message) {
    ExpiryBuilder.toFudgeMsg(this, message);
  }

  /**
   * This is for more efficient code within the .proto representations of securities, allowing Expiry to be
   * used directly as a message type instead of through the serialization framework.
   * 
   * @param message the message to decode
   * @return the expiry object
   */
  public static Expiry fromFudgeMsg(final FudgeFieldContainer message) {
    return ExpiryBuilder.fromFudgeMsg(message);
  }

}
