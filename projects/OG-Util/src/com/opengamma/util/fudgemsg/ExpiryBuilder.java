/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.util.fudgemsg;

import javax.time.calendar.TimeZone;
import javax.time.calendar.ZonedDateTime;

import org.fudgemsg.FudgeMsg;
import org.fudgemsg.MutableFudgeMsg;
import org.fudgemsg.mapping.FudgeBuilder;
import org.fudgemsg.mapping.FudgeBuilderFor;
import org.fudgemsg.mapping.FudgeDeserializationContext;
import org.fudgemsg.mapping.FudgeSerializationContext;
import org.fudgemsg.types.DateTimeAccuracy;
import org.fudgemsg.types.FudgeDateTime;
import org.fudgemsg.types.FudgeSecondaryType;
import org.fudgemsg.types.SecondaryFieldType;
import org.fudgemsg.wire.types.FudgeWireType;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.util.time.Expiry;
import com.opengamma.util.time.ExpiryAccuracy;

/**
 * Fudge builder for {@code Expiry}.
 */
@FudgeBuilderFor(Expiry.class)
public final class ExpiryBuilder implements FudgeBuilder<Expiry> {

  /**
   * Field name for the date & time component.
   */
  public static final String DATETIME_KEY = "datetime";
  /**
   * Field name for the timezone component.
   */
  public static final String TIMEZONE_KEY = "timezone";

  /**
   * Dummy secondary type to force serialization.
   */
  @FudgeSecondaryType
  public static final SecondaryFieldType<Expiry, FudgeMsg> SECONDARY_TYPE_INSTANCE = new SecondaryFieldType<Expiry, FudgeMsg>(FudgeWireType.SUB_MESSAGE, Expiry.class) {
    private static final long serialVersionUID = 1L;

    @Override
    public FudgeMsg secondaryToPrimary(final Expiry object) {
      throw new OpenGammaRuntimeException("Expiry should be serialized, not added directly to a Fudge message");
    }

    @Override
    public Expiry primaryToSecondary(final FudgeMsg message) {
      return fromFudgeMsg(message);
    }
  };

  protected static FudgeDateTime expiryToDateTime(final Expiry object) {
    ExpiryAccuracy accuracy = object.getAccuracy();
    if (accuracy == null) {
      accuracy = ExpiryAccuracy.DAY_MONTH_YEAR;
    }
    switch (accuracy) {
      case MIN_HOUR_DAY_MONTH_YEAR:
        return new FudgeDateTime(DateTimeAccuracy.MINUTE, object.getExpiry().toOffsetDateTime());
      case HOUR_DAY_MONTH_YEAR:
        return new FudgeDateTime(DateTimeAccuracy.HOUR, object.getExpiry().toOffsetDateTime());
      case DAY_MONTH_YEAR:
        return new FudgeDateTime(DateTimeAccuracy.DAY, object.getExpiry().toOffsetDateTime());
      case MONTH_YEAR:
        return new FudgeDateTime(DateTimeAccuracy.MONTH, object.getExpiry().toOffsetDateTime());
      case YEAR:
        return new FudgeDateTime(DateTimeAccuracy.YEAR, object.getExpiry().toOffsetDateTime());
      default:
        throw new IllegalArgumentException("Invalid accuracy value on " + object);
    }
  }

  protected static Expiry dateTimeToExpiry(final FudgeDateTime datetime, final String timezone) {
    final ZonedDateTime zdt = ZonedDateTime.ofInstant(datetime.toOffsetDateTime(), TimeZone.of(timezone));
    switch (datetime.getAccuracy()) {
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
        throw new IllegalArgumentException("Invalid accuracy value on " + datetime);
    }
  }

  public static void toFudgeMsg(final Expiry expiry, final MutableFudgeMsg message) {
    message.add(DATETIME_KEY, expiryToDateTime(expiry));
    message.add(TIMEZONE_KEY, expiry.getExpiry().getZone().getID());
  }

  public static Expiry fromFudgeMsg(final FudgeMsg message) {
    return dateTimeToExpiry(
        message.getFieldValue(FudgeDateTime.class, message.getByName(DATETIME_KEY)),
        message.getFieldValue(String.class, message.getByName(TIMEZONE_KEY)));
  }

  //-------------------------------------------------------------------------
  @Override
  public MutableFudgeMsg buildMessage(final FudgeSerializationContext context, final Expiry expiry) {
    final MutableFudgeMsg message = context.newMessage();
    toFudgeMsg(expiry, message);
    return message;
  }

  @Override
  public Expiry buildObject(final FudgeDeserializationContext context, final FudgeMsg message) {
    return fromFudgeMsg(message);
  }

}
