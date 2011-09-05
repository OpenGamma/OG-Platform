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

import com.opengamma.financial.security.fx.FXSecurity;
import com.opengamma.util.fudgemsg.AbstractFudgeBuilder;
import com.opengamma.util.fudgemsg.ExternalIdBuilder;
import com.opengamma.util.money.Currency;

/**
 * A Fudge builder for {@code FXSecurity}.
 */
@FudgeBuilderFor(FXSecurity.class)
public class FXSecurityBuilder extends AbstractFudgeBuilder implements FudgeBuilder<FXSecurity> {

  /** Field name. */
  public static final String PAY_CURRENCY_KEY = "payCurrency";
  /** Field name. */
  public static final String RECEIVE_CURRENCY_KEY = "receiveCurrency";
  /** Field name. */
  public static final String PAY_AMOUNT_KEY = "payAmount";
  /** Field name. */
  public static final String RECEIVE_AMOUNT_KEY = "receiveAmount";
  /** Field name. */
  public static final String REGION_KEY = "region";

  @Override
  public MutableFudgeMsg buildMessage(FudgeSerializer serializer, FXSecurity object) {
    final MutableFudgeMsg msg = serializer.newMessage();
    FXSecurityBuilder.toFudgeMsg(serializer, object, msg);
    return msg;
  }

  public static void toFudgeMsg(FudgeSerializer serializer, FXSecurity object, final MutableFudgeMsg msg) {
    FinancialSecurityBuilder.toFudgeMsg(serializer, object, msg);
    addToMessage(msg, PAY_CURRENCY_KEY, object.getPayCurrency());
    addToMessage(msg, RECEIVE_CURRENCY_KEY, object.getReceiveCurrency());
    addToMessage(msg, PAY_AMOUNT_KEY, object.getPayAmount());
    addToMessage(msg, RECEIVE_AMOUNT_KEY, object.getReceiveAmount());
    addToMessage(msg, REGION_KEY, ExternalIdBuilder.toFudgeMsg(serializer, object.getRegionId()));
  }

  @Override
  public FXSecurity buildObject(FudgeDeserializer deserializer, FudgeMsg msg) {
    FXSecurity object = new FXSecurity();
    FXSecurityBuilder.fromFudgeMsg(deserializer, msg, object);
    return object;
  }

  public static void fromFudgeMsg(FudgeDeserializer deserializer, FudgeMsg msg, FXSecurity object) {
    FinancialSecurityBuilder.fromFudgeMsg(deserializer, msg, object);
    object.setPayCurrency(msg.getValue(Currency.class, PAY_CURRENCY_KEY));
    object.setReceiveCurrency(msg.getValue(Currency.class, RECEIVE_CURRENCY_KEY));
    object.setPayAmount(msg.getDouble(PAY_AMOUNT_KEY));
    object.setReceiveAmount(msg.getDouble(RECEIVE_AMOUNT_KEY));
    object.setRegionId(ExternalIdBuilder.fromFudgeMsg(deserializer, msg.getMessage(REGION_KEY)));
  }

}
