/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.fudgemsg.security;

import org.fudgemsg.FudgeMsg;
import org.fudgemsg.MutableFudgeMsg;
import org.fudgemsg.mapping.FudgeBuilder;
import org.fudgemsg.mapping.FudgeBuilderFor;
import org.fudgemsg.mapping.FudgeDeserializer;
import org.fudgemsg.mapping.FudgeSerializer;

import com.opengamma.financial.convention.daycount.DayCount;
import com.opengamma.financial.convention.frequency.Frequency;
import com.opengamma.financial.security.capfloor.CapFloorCMSSpreadSecurity;
import com.opengamma.util.fudgemsg.AbstractFudgeBuilder;
import com.opengamma.util.fudgemsg.ExternalIdBuilder;
import com.opengamma.util.fudgemsg.ZonedDateTimeBuilder;
import com.opengamma.util.money.Currency;

/**
 * A Fudge builder for {@code CapFloorCMSSpreadSecurity}.
 */
@FudgeBuilderFor(CapFloorCMSSpreadSecurity.class)
public class CapFloorCMSSpreadSecurityBuilder extends AbstractFudgeBuilder implements FudgeBuilder<CapFloorCMSSpreadSecurity> {

  /** Field name. */
  public static final String START_DATE_KEY = "startDate";
  /** Field name. */
  public static final String MATURITY_DATE_KEY = "maturityDate";
  /** Field name. */
  public static final String NOTIONAL_KEY = "notional";
  /** Field name. */
  public static final String LONG_IDENTIFIER_KEY = "longIdentifier";
  /** Field name. */
  public static final String SHORT_IDENTIFIER_KEY = "shortIdentifier";
  /** Field name. */
  public static final String STRIKE_KEY = "strike";
  /** Field name. */
  public static final String FREQUENCY_KEY = "frequency";
  /** Field name. */
  public static final String CURRENCY_KEY = "currency";
  /** Field name. */
  public static final String DAY_COUNT_KEY = "dayCount";
  /** Field name. */
  public static final String IS_PAYER_KEY = "isPayer";
  /** Field name. */
  public static final String IS_CAP_KEY = "isCap";

  @Override
  public MutableFudgeMsg buildMessage(FudgeSerializer serializer, CapFloorCMSSpreadSecurity object) {
    final MutableFudgeMsg msg = serializer.newMessage();
    CapFloorCMSSpreadSecurityBuilder.toFudgeMsg(serializer, object, msg);
    return msg;
  }

  public static void toFudgeMsg(FudgeSerializer serializer, CapFloorCMSSpreadSecurity object, final MutableFudgeMsg msg) {
    FinancialSecurityBuilder.toFudgeMsg(serializer, object, msg);
    addToMessage(msg, START_DATE_KEY, ZonedDateTimeBuilder.toFudgeMsg(serializer, object.getStartDate()));
    addToMessage(msg, MATURITY_DATE_KEY, ZonedDateTimeBuilder.toFudgeMsg(serializer, object.getMaturityDate()));
    addToMessage(msg, NOTIONAL_KEY, object.getNotional());
    addToMessage(msg, LONG_IDENTIFIER_KEY, ExternalIdBuilder.toFudgeMsg(serializer, object.getLongIdentifier()));
    addToMessage(msg, SHORT_IDENTIFIER_KEY, ExternalIdBuilder.toFudgeMsg(serializer, object.getShortIdentifier()));
    addToMessage(msg, STRIKE_KEY, object.getStrike());
    addToMessage(msg, FREQUENCY_KEY, object.getFrequency());
    addToMessage(msg, CURRENCY_KEY, object.getCurrency());
    addToMessage(msg, DAY_COUNT_KEY, object.getDayCount());
    addToMessage(msg, IS_PAYER_KEY, object.getIsPayer());
    addToMessage(msg, IS_CAP_KEY, object.getIsCap());
  }

  @Override
  public CapFloorCMSSpreadSecurity buildObject(FudgeDeserializer deserializer, FudgeMsg msg) {
    CapFloorCMSSpreadSecurity object = FinancialSecurityBuilder.backdoorCreateClass(CapFloorCMSSpreadSecurity.class);
    CapFloorCMSSpreadSecurityBuilder.fromFudgeMsg(deserializer, msg, object);
    return object;
  }

  public static void fromFudgeMsg(FudgeDeserializer deserializer, FudgeMsg msg, CapFloorCMSSpreadSecurity object) {
    FinancialSecurityBuilder.fromFudgeMsg(deserializer, msg, object);
    object.setStartDate(ZonedDateTimeBuilder.fromFudgeMsg(deserializer, msg.getMessage(START_DATE_KEY)));
    object.setMaturityDate(ZonedDateTimeBuilder.fromFudgeMsg(deserializer, msg.getMessage(MATURITY_DATE_KEY)));
    object.setNotional(msg.getDouble(NOTIONAL_KEY));
    object.setLongIdentifier(ExternalIdBuilder.fromFudgeMsg(deserializer, msg.getMessage(LONG_IDENTIFIER_KEY)));
    object.setShortIdentifier(ExternalIdBuilder.fromFudgeMsg(deserializer, msg.getMessage(SHORT_IDENTIFIER_KEY)));
    object.setStrike(msg.getDouble(STRIKE_KEY));
    object.setFrequency(msg.getValue(Frequency.class, FREQUENCY_KEY));
    object.setCurrency(msg.getValue(Currency.class, CURRENCY_KEY));
    object.setDayCount(msg.getValue(DayCount.class, DAY_COUNT_KEY));
    object.setIsPayer(msg.getBoolean(IS_PAYER_KEY));
    object.setIsCap(msg.getBoolean(IS_CAP_KEY));
  }

}
