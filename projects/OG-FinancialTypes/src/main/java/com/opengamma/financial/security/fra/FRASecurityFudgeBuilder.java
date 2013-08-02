/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.security.fra;

import org.fudgemsg.FudgeMsg;
import org.fudgemsg.MutableFudgeMsg;
import org.fudgemsg.mapping.FudgeBuilder;
import org.fudgemsg.mapping.FudgeBuilderFor;
import org.fudgemsg.mapping.FudgeDeserializer;
import org.fudgemsg.mapping.FudgeSerializer;

import com.opengamma.financial.security.FinancialSecurityFudgeBuilder;
import com.opengamma.id.ExternalIdFudgeBuilder;
import com.opengamma.util.fudgemsg.AbstractFudgeBuilder;
import com.opengamma.util.money.Currency;
import com.opengamma.util.time.ZonedDateTimeFudgeBuilder;

/**
 * A Fudge builder for {@code FRASecurity}.
 */
@FudgeBuilderFor(FRASecurity.class)
public class FRASecurityFudgeBuilder extends AbstractFudgeBuilder implements FudgeBuilder<FRASecurity> {

  /** Field name. */
  public static final String CURRENCY_FIELD_NAME = "currency";
  /** Field name. */
  public static final String REGION_FIELD_NAME = "region";
  /** Field name. */
  public static final String START_DATE_FIELD_NAME = "startDate";
  /** Field name. */
  public static final String END_DATE_FIELD_NAME = "endDate";
  /** Field name. */
  public static final String RATE_FIELD_NAME = "rate";
  /** Field name. */
  public static final String AMOUNT_FIELD_NAME = "amount";
  /** Field name. */
  public static final String UNDERLYING_IDENTIFIER_FIELD_NAME = "underlyingIdentifier";
  /** Field name. */
  public static final String FIXING_DATE_FIELD_NAME = "fixingDate";

  @Override
  public MutableFudgeMsg buildMessage(FudgeSerializer serializer, FRASecurity object) {
    final MutableFudgeMsg msg = serializer.newMessage();
    FRASecurityFudgeBuilder.toFudgeMsg(serializer, object, msg);
    return msg;
  }

  public static void toFudgeMsg(FudgeSerializer serializer, FRASecurity object, final MutableFudgeMsg msg) {
    FinancialSecurityFudgeBuilder.toFudgeMsg(serializer, object, msg);
    addToMessage(msg, CURRENCY_FIELD_NAME, object.getCurrency());
    addToMessage(msg, REGION_FIELD_NAME, ExternalIdFudgeBuilder.toFudgeMsg(serializer, object.getRegionId()));
    addToMessage(msg, START_DATE_FIELD_NAME, ZonedDateTimeFudgeBuilder.toFudgeMsg(serializer, object.getStartDate()));
    addToMessage(msg, END_DATE_FIELD_NAME, ZonedDateTimeFudgeBuilder.toFudgeMsg(serializer, object.getEndDate()));
    addToMessage(msg, RATE_FIELD_NAME, object.getRate());
    addToMessage(msg, AMOUNT_FIELD_NAME, object.getAmount());
    addToMessage(msg, UNDERLYING_IDENTIFIER_FIELD_NAME, ExternalIdFudgeBuilder.toFudgeMsg(serializer, object.getUnderlyingId()));
    addToMessage(msg, FIXING_DATE_FIELD_NAME, ZonedDateTimeFudgeBuilder.toFudgeMsg(serializer, object.getFixingDate()));
  }

  @Override
  public FRASecurity buildObject(FudgeDeserializer deserializer, FudgeMsg msg) {
    FRASecurity object = new FRASecurity();
    FRASecurityFudgeBuilder.fromFudgeMsg(deserializer, msg, object);
    return object;
  }

  public static void fromFudgeMsg(FudgeDeserializer deserializer, FudgeMsg msg, FRASecurity object) {
    FinancialSecurityFudgeBuilder.fromFudgeMsg(deserializer, msg, object);
    object.setCurrency(msg.getValue(Currency.class, CURRENCY_FIELD_NAME));
    object.setRegionId(ExternalIdFudgeBuilder.fromFudgeMsg(deserializer, msg.getMessage(REGION_FIELD_NAME)));
    object.setStartDate(ZonedDateTimeFudgeBuilder.fromFudgeMsg(deserializer, msg.getMessage(START_DATE_FIELD_NAME)));
    object.setEndDate(ZonedDateTimeFudgeBuilder.fromFudgeMsg(deserializer, msg.getMessage(END_DATE_FIELD_NAME)));
    object.setRate(msg.getDouble(RATE_FIELD_NAME));
    object.setAmount(msg.getDouble(AMOUNT_FIELD_NAME));
    object.setUnderlyingId(ExternalIdFudgeBuilder.fromFudgeMsg(deserializer, msg.getMessage(UNDERLYING_IDENTIFIER_FIELD_NAME)));
    object.setFixingDate(ZonedDateTimeFudgeBuilder.fromFudgeMsg(deserializer, msg.getMessage(FIXING_DATE_FIELD_NAME)));
  }

}
