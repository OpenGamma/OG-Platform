/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.util.time;

import java.io.Serializable;

import javax.time.Instant;
import javax.time.InstantProvider;
import javax.time.calendar.ZonedDateTime;

import org.fudgemsg.FudgeMsg;
import org.fudgemsg.FudgeMsgFactory;
import org.fudgemsg.MutableFudgeMsg;
import org.fudgemsg.mapping.FudgeDeserializer;

import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.fudgemsg.ExpiryBuilder;

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
   * @param accuracy accuracy to compare to
   * @param expiry1 first date/time to compare
   * @param expiry2 second date/time to compare
   * @return {@code true} if the two dates/times are equal to the requested accuracy, {@code false} otherwise
   */
  public static boolean equalsToAccuracy(final ExpiryAccuracy accuracy, final ZonedDateTime expiry1, final ZonedDateTime expiry2) {
    switch (accuracy) {
      case MIN_HOUR_DAY_MONTH_YEAR:
        return (expiry1.getMinuteOfHour() == expiry2.getMinuteOfHour()) && (expiry1.getHourOfDay() == expiry2.getHourOfDay()) && (expiry1.getDayOfMonth() == expiry2.getDayOfMonth())
            && (expiry1.getMonthOfYear() == expiry2.getMonthOfYear()) && (expiry1.getYear() == expiry2.getYear());
      case HOUR_DAY_MONTH_YEAR:
        return (expiry1.getHourOfDay() == expiry2.getHourOfDay()) && (expiry1.getDayOfMonth() == expiry2.getDayOfMonth()) && (expiry1.getMonthOfYear() == expiry2.getMonthOfYear())
            && (expiry1.getYear() == expiry2.getYear());
      case DAY_MONTH_YEAR:
        return (expiry1.getDayOfMonth() == expiry2.getDayOfMonth()) && (expiry1.getMonthOfYear() == expiry2.getMonthOfYear()) && (expiry1.getYear() == expiry2.getYear());
      case MONTH_YEAR:
        return (expiry1.getMonthOfYear() == expiry2.getMonthOfYear()) && (expiry1.getYear() == expiry2.getYear());
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

  //-------------------------------------------------------------------------
  /**
   * This is for more efficient code within the .proto representations of securities, allowing Expiry to be
   * used directly as a message type instead of through the serialization framework.
   * 
   * @param factory  the Fudge message factory
   * @param message  the message to populate
   * @deprecated Use builder
   */
  @Deprecated
  public void toFudgeMsg(final FudgeMsgFactory factory, final MutableFudgeMsg message) {
    ExpiryBuilder.toFudgeMsg(this, message);
  }

  /**
   * This is for more efficient code within the .proto representations of securities, allowing Expiry to be
   * used directly as a message type instead of through the serialization framework.
   * 
   * @param fudgeContext  the Fudge context
   * @param message the message to decode
   * @return the expiry object
   * @deprecated Use builder
   */
  @Deprecated
  public static Expiry fromFudgeMsg(final FudgeDeserializer fudgeContext, final FudgeMsg message) {
    return ExpiryBuilder.fromFudgeMsg(message);
  }

}
