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
 * A Fudge builder for {@code IRFutureOptionSecurity}.
 */
@FudgeBuilderFor(IRFutureOptionSecurity.class)
public class IRFutureOptionSecurityFudgeBuilder extends AbstractFudgeBuilder implements FudgeBuilder<IRFutureOptionSecurity> {

  /** Field name. */
  public static final String EXCHANGE_KEY = "exchange";
  /** Field name. */
  public static final String EXPIRY_KEY = "expiry";
  /** Field name. */
  public static final String EXERCISE_TYPE_KEY = "exerciseType";
  /** Field name. */
  public static final String UNDERLYING_IDENTIFIER_KEY = "underlyingIdentifier";
  /** Field name. */
  public static final String POINT_VALUE_KEY = "pointValue";
  /** Field name. */
  public static final String IS_MARGINED_KEY = "isMargined";
  /** Field name. */
  public static final String CURRENCY_KEY = "currency";
  /** Field name. */
  public static final String STRIKE_KEY = "strike";
  /** Field name. */
  public static final String OPTION_TYPE_KEY = "optionType";

  @Override
  public MutableFudgeMsg buildMessage(FudgeSerializer serializer, IRFutureOptionSecurity object) {
    final MutableFudgeMsg msg = serializer.newMessage();
    IRFutureOptionSecurityFudgeBuilder.toFudgeMsg(serializer, object, msg);
    return msg;
  }

  public static void toFudgeMsg(FudgeSerializer serializer, IRFutureOptionSecurity object, final MutableFudgeMsg msg) {
    FinancialSecurityFudgeBuilder.toFudgeMsg(serializer, object, msg);
    addToMessage(msg, EXCHANGE_KEY, object.getExchange());
    addToMessage(msg, EXPIRY_KEY, ExpiryFudgeBuilder.toFudgeMsg(serializer, object.getExpiry()));
    addToMessage(msg, EXERCISE_TYPE_KEY, ExerciseTypeFudgeBuilder.toFudgeMsg(serializer, object.getExerciseType()));
    addToMessage(msg, UNDERLYING_IDENTIFIER_KEY, ExternalIdFudgeBuilder.toFudgeMsg(serializer, object.getUnderlyingId()));
    addToMessage(msg, POINT_VALUE_KEY, object.getPointValue());
    addToMessage(msg, IS_MARGINED_KEY, object.isMargined());
    addToMessage(msg, CURRENCY_KEY, object.getCurrency());
    addToMessage(msg, STRIKE_KEY, object.getStrike());
    addToMessage(msg, OPTION_TYPE_KEY, object.getOptionType());
  }

  @Override
  public IRFutureOptionSecurity buildObject(FudgeDeserializer deserializer, FudgeMsg msg) {
    IRFutureOptionSecurity object = new IRFutureOptionSecurity();
    IRFutureOptionSecurityFudgeBuilder.fromFudgeMsg(deserializer, msg, object);
    return object;
  }

  public static void fromFudgeMsg(FudgeDeserializer deserializer, FudgeMsg msg, IRFutureOptionSecurity object) {
    FinancialSecurityFudgeBuilder.fromFudgeMsg(deserializer, msg, object);
    object.setExchange(msg.getString(EXCHANGE_KEY));
    object.setExpiry(ExpiryFudgeBuilder.fromFudgeMsg(deserializer, msg.getMessage(EXPIRY_KEY)));
    object.setUnderlyingId(ExternalIdFudgeBuilder.fromFudgeMsg(deserializer, msg.getMessage(UNDERLYING_IDENTIFIER_KEY)));
    object.setExerciseType(ExerciseTypeFudgeBuilder.fromFudgeMsg(deserializer, msg.getMessage(EXERCISE_TYPE_KEY)));
    object.setPointValue(msg.getDouble(POINT_VALUE_KEY));
    object.setMargined(msg.getBoolean(IS_MARGINED_KEY));
    object.setCurrency(msg.getValue(Currency.class, CURRENCY_KEY));
    object.setStrike(msg.getDouble(STRIKE_KEY));
    object.setOptionType(msg.getFieldValue(OptionType.class, msg.getByName(OPTION_TYPE_KEY)));
  }

}
