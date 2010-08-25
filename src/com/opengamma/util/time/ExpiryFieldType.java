/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.util.time;

import javax.time.calendar.LocalDateTime;
import javax.time.calendar.TimeZone;
import javax.time.calendar.ZonedDateTime;

import org.fudgemsg.types.DateFieldType;
import org.fudgemsg.types.FudgeDate;
import org.fudgemsg.types.FudgeSecondaryType;
import org.fudgemsg.types.SecondaryFieldType;

/**
 * Secondary type for Expiry conversion to/from Fudge date type.
 *
 * @author Andrew Griffin
 */
public final class ExpiryFieldType extends SecondaryFieldType<Expiry, FudgeDate> {

  /**
   * Singleton instance of the type.
   */
  @FudgeSecondaryType
  public static final ExpiryFieldType INSTANCE = new ExpiryFieldType();

  private ExpiryFieldType() {
    super(DateFieldType.INSTANCE, Expiry.class);
  }

  /**
   * 
   * @param object the Expiry
   * @return the FudgeDate representation of the Expiry
   */
  @Override
  public FudgeDate secondaryToPrimary(final Expiry object) {
    ExpiryAccuracy accuracy = object.getAccuracy();
    if (accuracy == null) {
      accuracy = ExpiryAccuracy.DAY_MONTH_YEAR;
    }
    switch (accuracy) {
      case DAY_MONTH_YEAR:
        return new FudgeDate(object.getExpiry().getYear(), object.getExpiry().getMonthOfYear().getValue(), object
            .getExpiry().getDayOfMonth());
      case MONTH_YEAR:
        return new FudgeDate(object.getExpiry().getYear(), object.getExpiry().getMonthOfYear().getValue());
      case YEAR:
        return new FudgeDate(object.getExpiry().getYear());
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
  public Expiry primaryToSecondary(final FudgeDate data) {
    final ZonedDateTime zdt = ZonedDateTime.of(LocalDateTime.ofMidnight(data), TimeZone.UTC);
    switch (data.getAccuracy()) {
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
