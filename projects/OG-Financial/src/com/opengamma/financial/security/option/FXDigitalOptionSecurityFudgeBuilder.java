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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.financial.security.FinancialSecurityFudgeBuilder;
import com.opengamma.financial.security.LongShort;
import com.opengamma.util.fudgemsg.AbstractFudgeBuilder;
import com.opengamma.util.money.Currency;
import com.opengamma.util.time.ExpiryFudgeBuilder;
import com.opengamma.util.time.ZonedDateTimeFudgeBuilder;

/**
 * A Fudge builder for {@code FXOptionSecurity}.
 */
@FudgeBuilderFor(FXDigitalOptionSecurity.class)
public class FXDigitalOptionSecurityFudgeBuilder extends AbstractFudgeBuilder implements FudgeBuilder<FXDigitalOptionSecurity> {

  private static final Logger s_logger = LoggerFactory.getLogger(FXDigitalOptionSecurityFudgeBuilder.class);
  /** Field name. */
  public static final String PUT_CURRENCY_FIELD_NAME = "putCurrency";
  /** Field name. */
  public static final String CALL_CURRENCY_FIELD_NAME = "callCurrency";
  /** Field name. */
  public static final String PAYMENT_CURRENCY_FIELD_NAME = "paymentCurrency";
  /** Field name. */
  public static final String PUT_AMOUNT_FIELD_NAME = "putAmount";
  /** Field name. */
  public static final String CALL_AMOUNT_FIELD_NAME = "callAmount";
  /** Field name. */
  public static final String EXPIRY_FIELD_NAME = "expiry";
  /** Field name. */
  public static final String SETTLEMENT_DATE_FIELD_NAME = "settlementDate";
  /** Field name. */
  public static final String IS_LONG_FIELD_NAME = "isLong";

  @Override
  public MutableFudgeMsg buildMessage(FudgeSerializer serializer, FXDigitalOptionSecurity object) {
    final MutableFudgeMsg msg = serializer.newMessage();
    FXDigitalOptionSecurityFudgeBuilder.toFudgeMsg(serializer, object, msg);
    return msg;
  }

  public static void toFudgeMsg(FudgeSerializer serializer, FXDigitalOptionSecurity object, final MutableFudgeMsg msg) {
    FinancialSecurityFudgeBuilder.toFudgeMsg(serializer, object, msg);
    addToMessage(msg, PUT_CURRENCY_FIELD_NAME, object.getPutCurrency());
    addToMessage(msg, CALL_CURRENCY_FIELD_NAME, object.getCallCurrency());
    addToMessage(msg, PAYMENT_CURRENCY_FIELD_NAME, object.getPaymentCurrency());
    addToMessage(msg, PUT_AMOUNT_FIELD_NAME, object.getPutAmount());
    addToMessage(msg, CALL_AMOUNT_FIELD_NAME, object.getCallAmount());
    addToMessage(msg, EXPIRY_FIELD_NAME, ExpiryFudgeBuilder.toFudgeMsg(serializer, object.getExpiry()));
    addToMessage(msg, SETTLEMENT_DATE_FIELD_NAME, ZonedDateTimeFudgeBuilder.toFudgeMsg(serializer, object.getSettlementDate()));
    addToMessage(msg, IS_LONG_FIELD_NAME, object.isLong());
  }

  @Override
  public FXDigitalOptionSecurity buildObject(FudgeDeserializer deserializer, FudgeMsg msg) {
    FXDigitalOptionSecurity object = new FXDigitalOptionSecurity();
    FXDigitalOptionSecurityFudgeBuilder.fromFudgeMsg(deserializer, msg, object);
    return object;
  }

  public static void fromFudgeMsg(FudgeDeserializer deserializer, FudgeMsg msg, FXDigitalOptionSecurity object) {
    FinancialSecurityFudgeBuilder.fromFudgeMsg(deserializer, msg, object);
    object.setPutCurrency(msg.getValue(Currency.class, PUT_CURRENCY_FIELD_NAME));
    object.setCallCurrency(msg.getValue(Currency.class, CALL_CURRENCY_FIELD_NAME));
    if (msg.hasField(PAYMENT_CURRENCY_FIELD_NAME)) {
      object.setPaymentCurrency(msg.getValue(Currency.class, PAYMENT_CURRENCY_FIELD_NAME));
    } else {
      s_logger.warn("Found old version of FXDigitalOption, setting payment currency to put currency - this should not happen, report to support@opengamma.com");
      object.setPaymentCurrency(object.getPutCurrency());
    }
    object.setPutAmount(msg.getDouble(PUT_AMOUNT_FIELD_NAME));
    object.setCallAmount(msg.getDouble(CALL_AMOUNT_FIELD_NAME));
    object.setExpiry(ExpiryFudgeBuilder.fromFudgeMsg(deserializer, msg.getMessage(EXPIRY_FIELD_NAME)));
    object.setSettlementDate(ZonedDateTimeFudgeBuilder.fromFudgeMsg(deserializer, msg.getMessage(SETTLEMENT_DATE_FIELD_NAME)));
    object.setLongShort(LongShort.ofLong(msg.getBoolean(IS_LONG_FIELD_NAME)));
  }

}
