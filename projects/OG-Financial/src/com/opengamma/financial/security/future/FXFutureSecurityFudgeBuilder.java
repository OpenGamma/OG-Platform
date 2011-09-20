/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.security.future;

import org.fudgemsg.FudgeMsg;
import org.fudgemsg.MutableFudgeMsg;
import org.fudgemsg.mapping.FudgeBuilder;
import org.fudgemsg.mapping.FudgeBuilderFor;
import org.fudgemsg.mapping.FudgeDeserializer;
import org.fudgemsg.mapping.FudgeSerializer;

import com.opengamma.util.fudgemsg.AbstractFudgeBuilder;
import com.opengamma.util.money.Currency;

/**
 * A Fudge builder for {@code FXFutureSecurity}.
 */
@FudgeBuilderFor(FXFutureSecurity.class)
public class FXFutureSecurityFudgeBuilder extends AbstractFudgeBuilder implements FudgeBuilder<FXFutureSecurity> {

  /** Field name. */
  public static final String NUMERATOR_FIELD_NAME = "numerator";
  /** Field name. */
  public static final String DENOMINATOR_FIELD_NAME = "denominator";
  /** Field name. */
  public static final String MULTIPLICATION_FACTOR_FIELD_NAME = "multiplicationFactor";

  @Override
  public MutableFudgeMsg buildMessage(FudgeSerializer serializer, FXFutureSecurity object) {
    final MutableFudgeMsg msg = serializer.newMessage();
    FXFutureSecurityFudgeBuilder.toFudgeMsg(serializer, object, msg);
    return msg;
  }

  public static void toFudgeMsg(FudgeSerializer serializer, FXFutureSecurity object, final MutableFudgeMsg msg) {
    FutureSecurityFudgeBuilder.toFudgeMsg(serializer, object, msg);
    addToMessage(msg, NUMERATOR_FIELD_NAME, object.getNumerator());
    addToMessage(msg, DENOMINATOR_FIELD_NAME, object.getDenominator());
    addToMessage(msg, MULTIPLICATION_FACTOR_FIELD_NAME, object.getMultiplicationFactor());
  }

  @Override
  public FXFutureSecurity buildObject(FudgeDeserializer deserializer, FudgeMsg msg) {
    FXFutureSecurity object = new FXFutureSecurity();
    FXFutureSecurityFudgeBuilder.fromFudgeMsg(deserializer, msg, object);
    return object;
  }

  public static void fromFudgeMsg(FudgeDeserializer deserializer, FudgeMsg msg, FXFutureSecurity object) {
    FutureSecurityFudgeBuilder.fromFudgeMsg(deserializer, msg, object);
    object.setNumerator(msg.getValue(Currency.class, NUMERATOR_FIELD_NAME));
    object.setDenominator(msg.getValue(Currency.class, DENOMINATOR_FIELD_NAME));
    object.setMultiplicationFactor(msg.getDouble(MULTIPLICATION_FACTOR_FIELD_NAME));
  }

}
