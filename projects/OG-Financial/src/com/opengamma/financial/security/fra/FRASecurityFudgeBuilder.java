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
  public static final String CURRENCY_KEY = "currency";
  /** Field name. */
  public static final String REGION_KEY = "region";
  /** Field name. */
  public static final String START_DATE_KEY = "startDate";
  /** Field name. */
  public static final String END_DATE_KEY = "endDate";
  /** Field name. */
  public static final String RATE_KEY = "rate";
  /** Field name. */
  public static final String AMOUNT_KEY = "amount";
  /** Field name. */
  public static final String UNDERLYING_IDENTIFIER_KEY = "underlyingIdentifier";

  @Override
  public MutableFudgeMsg buildMessage(FudgeSerializer serializer, FRASecurity object) {
    final MutableFudgeMsg msg = serializer.newMessage();
    FRASecurityFudgeBuilder.toFudgeMsg(serializer, object, msg);
    return msg;
  }

  public static void toFudgeMsg(FudgeSerializer serializer, FRASecurity object, final MutableFudgeMsg msg) {
    FinancialSecurityFudgeBuilder.toFudgeMsg(serializer, object, msg);
    addToMessage(msg, CURRENCY_KEY, object.getCurrency());
    addToMessage(msg, REGION_KEY, ExternalIdFudgeBuilder.toFudgeMsg(serializer, object.getRegionId()));
    addToMessage(msg, START_DATE_KEY, ZonedDateTimeFudgeBuilder.toFudgeMsg(serializer, object.getStartDate()));
    addToMessage(msg, END_DATE_KEY, ZonedDateTimeFudgeBuilder.toFudgeMsg(serializer, object.getEndDate()));
    addToMessage(msg, RATE_KEY, object.getRate());
    addToMessage(msg, AMOUNT_KEY, object.getAmount());
    addToMessage(msg, UNDERLYING_IDENTIFIER_KEY, ExternalIdFudgeBuilder.toFudgeMsg(serializer, object.getUnderlyingId()));
  }

  @Override
  public FRASecurity buildObject(FudgeDeserializer deserializer, FudgeMsg msg) {
    FRASecurity object = new FRASecurity();
    FRASecurityFudgeBuilder.fromFudgeMsg(deserializer, msg, object);
    return object;
  }

  public static void fromFudgeMsg(FudgeDeserializer deserializer, FudgeMsg msg, FRASecurity object) {
    FinancialSecurityFudgeBuilder.fromFudgeMsg(deserializer, msg, object);
    object.setCurrency(msg.getValue(Currency.class, CURRENCY_KEY));
    object.setRegionId(ExternalIdFudgeBuilder.fromFudgeMsg(deserializer, msg.getMessage(REGION_KEY)));
    object.setStartDate(ZonedDateTimeFudgeBuilder.fromFudgeMsg(deserializer, msg.getMessage(START_DATE_KEY)));
    object.setEndDate(ZonedDateTimeFudgeBuilder.fromFudgeMsg(deserializer, msg.getMessage(END_DATE_KEY)));
    object.setRate(msg.getDouble(RATE_KEY));
    object.setAmount(msg.getDouble(AMOUNT_KEY));
    object.setUnderlyingId(ExternalIdFudgeBuilder.fromFudgeMsg(deserializer, msg.getMessage(UNDERLYING_IDENTIFIER_KEY)));
  }

}
