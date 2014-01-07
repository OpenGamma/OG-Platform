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
 * A Fudge builder for {@code EquityBarrierOptionSecurity}.
 */
@FudgeBuilderFor(EquityBarrierOptionSecurity.class)
public class EquityBarrierOptionSecurityFudgeBuilder extends AbstractFudgeBuilder implements FudgeBuilder<EquityBarrierOptionSecurity> {

  /** Field name. */
  public static final String OPTION_TYPE_FIELD_NAME = "optionType";
  /** Field name. */
  public static final String STRIKE_FIELD_NAME = "strike";
  /** Field name. */
  public static final String CURRENCY_FIELD_NAME = "currency";
  /** Field name. */
  public static final String UNDERLYING_IDENTIFIER_FIELD_NAME = "underlyingIdentifier";
  /** Field name. */
  public static final String EXERCISE_TYPE_FIELD_NAME = "exerciseType";
  /** Field name. */
  public static final String EXPIRY_FIELD_NAME = "expiry";
  /** Field name. */
  public static final String POINT_VALUE_FIELD_NAME = "pointValue";
  /** Field name. */
  public static final String EXCHANGE_FIELD_NAME = "exchange";
  /** Field name. */
  public static final String BARRIER_TYPE_FIELD_NAME = "barrierType";
  /** Field name. */
  public static final String BARRIER_DIRECTION_FIELD_NAME = "barrierDirection";
  /** Field name. */
  public static final String MONITORING_TYPE_FIELD_NAME = "monitoringType";
  /** Field name. */
  public static final String SAMPLING_FREQUENCY_FIELD_NAME = "samplingFrequency";
  /** Field name. */
  public static final String BARRIER_LEVEL_FIELD_NAME = "barrierLevel";

  @Override
  public MutableFudgeMsg buildMessage(FudgeSerializer serializer, EquityBarrierOptionSecurity object) {
    final MutableFudgeMsg msg = serializer.newMessage();
    EquityBarrierOptionSecurityFudgeBuilder.toFudgeMsg(serializer, object, msg);
    return msg;
  }

  public static void toFudgeMsg(FudgeSerializer serializer, EquityBarrierOptionSecurity object, final MutableFudgeMsg msg) {
    FinancialSecurityFudgeBuilder.toFudgeMsg(serializer, object, msg);
    addToMessage(msg, OPTION_TYPE_FIELD_NAME, object.getOptionType());
    addToMessage(msg, STRIKE_FIELD_NAME, object.getStrike());
    addToMessage(msg, CURRENCY_FIELD_NAME, object.getCurrency());
    addToMessage(msg, UNDERLYING_IDENTIFIER_FIELD_NAME, ExternalIdFudgeBuilder.toFudgeMsg(serializer, object.getUnderlyingId()));
    addToMessage(msg, EXERCISE_TYPE_FIELD_NAME, ExerciseTypeFudgeBuilder.toFudgeMsg(serializer, object.getExerciseType()));
    addToMessage(msg, EXPIRY_FIELD_NAME, ExpiryFudgeBuilder.toFudgeMsg(serializer, object.getExpiry()));
    addToMessage(msg, POINT_VALUE_FIELD_NAME, object.getPointValue());
    addToMessage(msg, EXCHANGE_FIELD_NAME, object.getExchange());
    addToMessage(msg, BARRIER_TYPE_FIELD_NAME, object.getBarrierType());
    addToMessage(msg, BARRIER_DIRECTION_FIELD_NAME, object.getBarrierDirection());
    addToMessage(msg, MONITORING_TYPE_FIELD_NAME, object.getMonitoringType());
    addToMessage(msg, SAMPLING_FREQUENCY_FIELD_NAME, object.getSamplingFrequency());
    addToMessage(msg, BARRIER_LEVEL_FIELD_NAME, object.getBarrierLevel());
  }

  @Override
  public EquityBarrierOptionSecurity buildObject(FudgeDeserializer deserializer, FudgeMsg msg) {
    EquityBarrierOptionSecurity object = new EquityBarrierOptionSecurity();
    EquityBarrierOptionSecurityFudgeBuilder.fromFudgeMsg(deserializer, msg, object);
    return object;
  }

  public static void fromFudgeMsg(FudgeDeserializer deserializer, FudgeMsg msg, EquityBarrierOptionSecurity object) {
    FinancialSecurityFudgeBuilder.fromFudgeMsg(deserializer, msg, object);
    object.setOptionType(msg.getFieldValue(OptionType.class, msg.getByName(OPTION_TYPE_FIELD_NAME)));
    object.setStrike(msg.getDouble(STRIKE_FIELD_NAME));
    object.setCurrency(msg.getValue(Currency.class, CURRENCY_FIELD_NAME));
    object.setUnderlyingId(ExternalIdFudgeBuilder.fromFudgeMsg(deserializer, msg.getMessage(UNDERLYING_IDENTIFIER_FIELD_NAME)));
    object.setExerciseType(ExerciseTypeFudgeBuilder.fromFudgeMsg(deserializer, msg.getMessage(EXERCISE_TYPE_FIELD_NAME)));
    object.setExpiry(ExpiryFudgeBuilder.fromFudgeMsg(deserializer, msg.getMessage(EXPIRY_FIELD_NAME)));
    object.setPointValue(msg.getDouble(POINT_VALUE_FIELD_NAME));
    object.setExchange(msg.getString(EXCHANGE_FIELD_NAME));
    object.setBarrierType(msg.getFieldValue(BarrierType.class, msg.getByName(BARRIER_TYPE_FIELD_NAME)));
    object.setBarrierDirection(msg.getFieldValue(BarrierDirection.class, msg.getByName(BARRIER_DIRECTION_FIELD_NAME)));
    object.setMonitoringType(msg.getFieldValue(MonitoringType.class, msg.getByName(MONITORING_TYPE_FIELD_NAME)));
    object.setSamplingFrequency(msg.getFieldValue(SamplingFrequency.class, msg.getByName(SAMPLING_FREQUENCY_FIELD_NAME)));
    object.setBarrierLevel(msg.getDouble(BARRIER_LEVEL_FIELD_NAME));
  }

}
