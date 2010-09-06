/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.util.time;

import javax.time.calendar.TimeZone;
import javax.time.calendar.ZonedDateTime;

import org.fudgemsg.types.DateTimeAccuracy;
import org.fudgemsg.types.DateTimeFieldType;
import org.fudgemsg.types.FudgeDateTime;
import org.fudgemsg.types.FudgeSecondaryType;
import org.fudgemsg.types.SecondaryFieldType;

/**
 * Secondary type for Expiry conversion to/from Fudge date type.
 *
 * @author Andrew Griffin
 */
public final class ExpiryFieldType extends SecondaryFieldType<Expiry, FudgeDateTime> {
  /**
   * Singleton instance of the type.
   */
  @FudgeSecondaryType
  public static final ExpiryFieldType INSTANCE = new ExpiryFieldType();

  private ExpiryFieldType() {
    super(DateTimeFieldType.INSTANCE, Expiry.class);
  }

  /**
   * 
   * @param object the Expiry
   * @return the FudgeDate representation of the Expiry
   */
  @Override
  public FudgeDateTime secondaryToPrimary(final Expiry object) {
    ExpiryAccuracy accuracy = object.getAccuracy();
    if (accuracy == null) {
      accuracy = ExpiryAccuracy.DAY_MONTH_YEAR;
    }
    switch (accuracy) {
      case MIN_HOUR_DAY_MONTH_YEAR:
        return new FudgeDateTime(DateTimeAccuracy.MINUTE, object.getExpiry().toInstant());
      case HOUR_DAY_MONTH_YEAR:
        return new FudgeDateTime(DateTimeAccuracy.HOUR, object.getExpiry().toInstant());
      case DAY_MONTH_YEAR:
        return new FudgeDateTime(DateTimeAccuracy.DAY, object.getExpiry().toInstant());
      case MONTH_YEAR:
        return new FudgeDateTime(DateTimeAccuracy.MONTH, object.getExpiry().toInstant());
      case YEAR: 
        return new FudgeDateTime(DateTimeAccuracy.YEAR, object.getExpiry().toInstant());
      default:
        throw new IllegalArgumentException("Invalid accuracy value on " + object);
    }
  }

  /**
   * 
   * @param data the FudgeDate representation
   * @return the Expiry
   */
  @Override
  public Expiry primaryToSecondary(final FudgeDateTime data) {
    final ZonedDateTime zdt = ZonedDateTime.ofInstant(data.toInstant(), TimeZone.UTC);
    switch (data.getAccuracy()) {
      case MINUTE:
        return new Expiry(zdt, ExpiryAccuracy.MIN_HOUR_DAY_MONTH_YEAR);
      case HOUR:
        return new Expiry(zdt, ExpiryAccuracy.HOUR_DAY_MONTH_YEAR);
      case DAY:
        return new Expiry(zdt, ExpiryAccuracy.DAY_MONTH_YEAR);
      case MONTH:
        return new Expiry(zdt, ExpiryAccuracy.MONTH_YEAR);
      case YEAR:
        return new Expiry(zdt, ExpiryAccuracy.YEAR);
      default:
        throw new IllegalArgumentException("Invalid accuracy value on " + data);
    }
  }

}
