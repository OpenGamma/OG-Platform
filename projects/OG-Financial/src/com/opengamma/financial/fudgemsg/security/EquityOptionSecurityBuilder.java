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

import com.opengamma.financial.fudgemsg.ExerciseTypeBuilder;
import com.opengamma.financial.security.option.EquityOptionSecurity;
import com.opengamma.financial.security.option.OptionType;
import com.opengamma.util.fudgemsg.AbstractFudgeBuilder;
import com.opengamma.util.fudgemsg.ExpiryBuilder;
import com.opengamma.util.fudgemsg.ExternalIdBuilder;
import com.opengamma.util.money.Currency;

/**
 * A Fudge builder for {@code EquityOptionSecurity}.
 */
@FudgeBuilderFor(EquityOptionSecurity.class)
public class EquityOptionSecurityBuilder extends AbstractFudgeBuilder implements FudgeBuilder<EquityOptionSecurity> {

  /** Field name. */
  public static final String OPTION_TYPE_KEY = "optionType";
  /** Field name. */
  public static final String STRIKE_KEY = "strike";
  /** Field name. */
  public static final String CURRENCY_KEY = "currency";
  /** Field name. */
  public static final String UNDERLYING_IDENTIFIER_KEY = "underlyingIdentifier";
  /** Field name. */
  public static final String EXERCISE_TYPE_KEY = "exerciseType";
  /** Field name. */
  public static final String EXPIRY_KEY = "expiry";
  /** Field name. */
  public static final String POINT_VALUE_KEY = "pointValue";
  /** Field name. */
  public static final String EXCHANGE_KEY = "exchange";

  @Override
  public MutableFudgeMsg buildMessage(FudgeSerializer serializer, EquityOptionSecurity object) {
    final MutableFudgeMsg msg = serializer.newMessage();
    EquityOptionSecurityBuilder.toFudgeMsg(serializer, object, msg);
    return msg;
  }

  public static void toFudgeMsg(FudgeSerializer serializer, EquityOptionSecurity object, final MutableFudgeMsg msg) {
    FinancialSecurityBuilder.toFudgeMsg(serializer, object, msg);
    addToMessage(msg, OPTION_TYPE_KEY, object.getOptionType());
    addToMessage(msg, STRIKE_KEY, object.getStrike());
    addToMessage(msg, CURRENCY_KEY, object.getCurrency());
    addToMessage(msg, UNDERLYING_IDENTIFIER_KEY, ExternalIdBuilder.toFudgeMsg(serializer, object.getUnderlyingId()));
    addToMessage(msg, EXERCISE_TYPE_KEY, ExerciseTypeBuilder.toFudgeMsg(serializer, object.getExerciseType()));
    addToMessage(msg, EXPIRY_KEY, ExpiryBuilder.toFudgeMsg(serializer, object.getExpiry()));
    addToMessage(msg, POINT_VALUE_KEY, object.getPointValue());
    addToMessage(msg, EXCHANGE_KEY, object.getExchange());
  }

  @Override
  public EquityOptionSecurity buildObject(FudgeDeserializer deserializer, FudgeMsg msg) {
    EquityOptionSecurity object = new EquityOptionSecurity();
    EquityOptionSecurityBuilder.fromFudgeMsg(deserializer, msg, object);
    return object;
  }

  public static void fromFudgeMsg(FudgeDeserializer deserializer, FudgeMsg msg, EquityOptionSecurity object) {
    FinancialSecurityBuilder.fromFudgeMsg(deserializer, msg, object);
    object.setOptionType(msg.getFieldValue(OptionType.class, msg.getByName(OPTION_TYPE_KEY)));
    object.setStrike(msg.getDouble(STRIKE_KEY));
    object.setCurrency(msg.getValue(Currency.class, CURRENCY_KEY));
    object.setUnderlyingId(ExternalIdBuilder.fromFudgeMsg(deserializer, msg.getMessage(UNDERLYING_IDENTIFIER_KEY)));
    object.setExerciseType(ExerciseTypeBuilder.fromFudgeMsg(deserializer, msg.getMessage(EXERCISE_TYPE_KEY)));
    object.setExpiry(ExpiryBuilder.fromFudgeMsg(deserializer, msg.getMessage(EXPIRY_KEY)));
    object.setPointValue(msg.getDouble(POINT_VALUE_KEY));
    object.setExchange(msg.getString(EXCHANGE_KEY));
  }

}
