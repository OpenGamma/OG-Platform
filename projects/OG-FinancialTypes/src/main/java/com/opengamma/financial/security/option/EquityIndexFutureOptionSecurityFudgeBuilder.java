/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.security.option;

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
import com.opengamma.util.time.ExpiryFudgeBuilder;

/**
 * A Fudge builder for {@code EquityIndexFutureOptionSecurity}.
 */
@FudgeBuilderFor(EquityIndexFutureOptionSecurity.class)
public class EquityIndexFutureOptionSecurityFudgeBuilder extends AbstractFudgeBuilder implements FudgeBuilder<EquityIndexFutureOptionSecurity> {

  /** Field name. */
  public static final String EXCHANGE_FIELD_NAME = "exchange";
  /** Field name. */
  public static final String EXPIRY_FIELD_NAME = "expiry";
  /** Field name. */
  public static final String EXERCISE_TYPE_FIELD_NAME = "exerciseType";
  /** Field name. */
  public static final String UNDERLYING_IDENTIFIER_FIELD_NAME = "underlyingIdentifier";
  /** Field name. */
  public static final String POINT_VALUE_FIELD_NAME = "pointValue";
  /** Field name. */
  public static final String IS_MARGINED_FIELD_NAME = "isMargined";
  /** Field name. */
  public static final String CURRENCY_FIELD_NAME = "currency";
  /** Field name. */
  public static final String STRIKE_FIELD_NAME = "strike";
  /** Field name. */
  public static final String OPTION_TYPE_FIELD_NAME = "optionType";

  @Override
  public MutableFudgeMsg buildMessage(final FudgeSerializer serializer, final EquityIndexFutureOptionSecurity object) {
    final MutableFudgeMsg msg = serializer.newMessage();
    EquityIndexFutureOptionSecurityFudgeBuilder.toFudgeMsg(serializer, object, msg);
    return msg;
  }

  public static void toFudgeMsg(final FudgeSerializer serializer, final EquityIndexFutureOptionSecurity object, final MutableFudgeMsg msg) {
    FinancialSecurityFudgeBuilder.toFudgeMsg(serializer, object, msg);
    addToMessage(msg, EXCHANGE_FIELD_NAME, object.getExchange());
    addToMessage(msg, EXPIRY_FIELD_NAME, ExpiryFudgeBuilder.toFudgeMsg(serializer, object.getExpiry()));
    addToMessage(msg, EXERCISE_TYPE_FIELD_NAME, ExerciseTypeFudgeBuilder.toFudgeMsg(serializer, object.getExerciseType()));
    addToMessage(msg, UNDERLYING_IDENTIFIER_FIELD_NAME, ExternalIdFudgeBuilder.toFudgeMsg(serializer, object.getUnderlyingId()));
    addToMessage(msg, POINT_VALUE_FIELD_NAME, object.getPointValue());
    addToMessage(msg, IS_MARGINED_FIELD_NAME, object.isMargined());
    addToMessage(msg, CURRENCY_FIELD_NAME, object.getCurrency());
    addToMessage(msg, STRIKE_FIELD_NAME, object.getStrike());
    addToMessage(msg, OPTION_TYPE_FIELD_NAME, object.getOptionType());
  }

  @Override
  public EquityIndexFutureOptionSecurity buildObject(final FudgeDeserializer deserializer, final FudgeMsg msg) {
    final EquityIndexFutureOptionSecurity object = new EquityIndexFutureOptionSecurity();
    EquityIndexFutureOptionSecurityFudgeBuilder.fromFudgeMsg(deserializer, msg, object);
    return object;
  }

  public static void fromFudgeMsg(final FudgeDeserializer deserializer, final FudgeMsg msg, final EquityIndexFutureOptionSecurity object) {
    FinancialSecurityFudgeBuilder.fromFudgeMsg(deserializer, msg, object);
    object.setExchange(msg.getString(EXCHANGE_FIELD_NAME));
    object.setExpiry(ExpiryFudgeBuilder.fromFudgeMsg(deserializer, msg.getMessage(EXPIRY_FIELD_NAME)));
    object.setUnderlyingId(ExternalIdFudgeBuilder.fromFudgeMsg(deserializer, msg.getMessage(UNDERLYING_IDENTIFIER_FIELD_NAME)));
    object.setExerciseType(ExerciseTypeFudgeBuilder.fromFudgeMsg(deserializer, msg.getMessage(EXERCISE_TYPE_FIELD_NAME)));
    object.setPointValue(msg.getDouble(POINT_VALUE_FIELD_NAME));
    object.setMargined(msg.getBoolean(IS_MARGINED_FIELD_NAME));
    object.setCurrency(msg.getValue(Currency.class, CURRENCY_FIELD_NAME));
    object.setStrike(msg.getDouble(STRIKE_FIELD_NAME));
    object.setOptionType(msg.getFieldValue(OptionType.class, msg.getByName(OPTION_TYPE_FIELD_NAME)));
  }

}
