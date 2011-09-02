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

import com.opengamma.financial.security.option.BarrierDirection;
import com.opengamma.financial.security.option.BarrierType;
import com.opengamma.financial.security.option.FXBarrierOptionSecurity;
import com.opengamma.financial.security.option.MonitoringType;
import com.opengamma.financial.security.option.SamplingFrequency;
import com.opengamma.util.fudgemsg.AbstractFudgeBuilder;
import com.opengamma.util.fudgemsg.ExpiryBuilder;
import com.opengamma.util.fudgemsg.ZonedDateTimeBuilder;
import com.opengamma.util.money.Currency;

/**
 * A Fudge builder for {@code FXBarrierOptionSecurity}.
 */
@FudgeBuilderFor(FXBarrierOptionSecurity.class)
public class FXBarrierOptionSecurityBuilder extends AbstractFudgeBuilder implements FudgeBuilder<FXBarrierOptionSecurity> {

  /** Field name. */
  public static final String PUT_CURRENCY_KEY = "putCurrency";
  /** Field name. */
  public static final String CALL_CURRENCY_KEY = "callCurrency";
  /** Field name. */
  public static final String PUT_AMOUNT_KEY = "putAmount";
  /** Field name. */
  public static final String CALL_AMOUNT_KEY = "callAmount";
  /** Field name. */
  public static final String EXPIRY_KEY = "expiry";
  /** Field name. */
  public static final String SETTLEMENT_DATE_KEY = "settlementDate";
  /** Field name. */
  public static final String BARRIER_TYPE_KEY = "barrierType";
  /** Field name. */
  public static final String BARRIER_DIRECTION_KEY = "barrierDirection";
  /** Field name. */
  public static final String MONITORING_TYPE_KEY = "monitoringType";
  /** Field name. */
  public static final String SAMPLING_FREQUENCY_KEY = "samplingFrequency";
  /** Field name. */
  public static final String BARRIER_LEVEL_KEY = "barrierLevel";
  /** Field name. */
  public static final String IS_LONG_KEY = "isLong";

  @Override
  public MutableFudgeMsg buildMessage(FudgeSerializer serializer, FXBarrierOptionSecurity object) {
    final MutableFudgeMsg msg = serializer.newMessage();
    FXBarrierOptionSecurityBuilder.toFudgeMsg(serializer, object, msg);
    return msg;
  }

  public static void toFudgeMsg(FudgeSerializer serializer, FXBarrierOptionSecurity object, final MutableFudgeMsg msg) {
    FinancialSecurityBuilder.toFudgeMsg(serializer, object, msg);
    addToMessage(msg, PUT_CURRENCY_KEY, object.getPutCurrency());
    addToMessage(msg, CALL_CURRENCY_KEY, object.getCallCurrency());
    addToMessage(msg, PUT_AMOUNT_KEY, object.getPutAmount());
    addToMessage(msg, CALL_AMOUNT_KEY, object.getCallAmount());
    addToMessage(msg, EXPIRY_KEY, ExpiryBuilder.toFudgeMsg(serializer, object.getExpiry()));
    addToMessage(msg, SETTLEMENT_DATE_KEY, ZonedDateTimeBuilder.toFudgeMsg(serializer, object.getSettlementDate()));
    addToMessage(msg, BARRIER_TYPE_KEY, object.getBarrierType());
    addToMessage(msg, BARRIER_DIRECTION_KEY, object.getBarrierDirection());
    addToMessage(msg, MONITORING_TYPE_KEY, object.getMonitoringType());
    addToMessage(msg, SAMPLING_FREQUENCY_KEY, object.getSamplingFrequency());
    addToMessage(msg, BARRIER_LEVEL_KEY, object.getBarrierLevel());
    addToMessage(msg, IS_LONG_KEY, object.getIsLong());
  }

  @Override
  public FXBarrierOptionSecurity buildObject(FudgeDeserializer deserializer, FudgeMsg msg) {
    FXBarrierOptionSecurity object = new FXBarrierOptionSecurity();
    FXBarrierOptionSecurityBuilder.fromFudgeMsg(deserializer, msg, object);
    return object;
  }

  public static void fromFudgeMsg(FudgeDeserializer deserializer, FudgeMsg msg, FXBarrierOptionSecurity object) {
    FinancialSecurityBuilder.fromFudgeMsg(deserializer, msg, object);
    object.setPutCurrency(msg.getValue(Currency.class, PUT_CURRENCY_KEY));
    object.setCallCurrency(msg.getValue(Currency.class, CALL_CURRENCY_KEY));
    object.setPutAmount(msg.getDouble(PUT_AMOUNT_KEY));
    object.setCallAmount(msg.getDouble(CALL_AMOUNT_KEY));
    object.setExpiry(ExpiryBuilder.fromFudgeMsg(deserializer, msg.getMessage(EXPIRY_KEY)));
    object.setSettlementDate(ZonedDateTimeBuilder.fromFudgeMsg(deserializer, msg.getMessage(SETTLEMENT_DATE_KEY)));
    object.setBarrierType(msg.getFieldValue(BarrierType.class, msg.getByName(BARRIER_TYPE_KEY)));
    object.setBarrierDirection(msg.getFieldValue(BarrierDirection.class, msg.getByName(BARRIER_DIRECTION_KEY)));
    object.setMonitoringType(msg.getFieldValue(MonitoringType.class, msg.getByName(MONITORING_TYPE_KEY)));
    object.setSamplingFrequency(msg.getFieldValue(SamplingFrequency.class, msg.getByName(SAMPLING_FREQUENCY_KEY)));
    object.setBarrierLevel(msg.getDouble(BARRIER_LEVEL_KEY));
    object.setIsLong(msg.getBoolean(IS_LONG_KEY));
  }

}
