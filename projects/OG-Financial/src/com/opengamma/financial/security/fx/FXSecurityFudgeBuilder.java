/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.security.fx;

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

/**
 * A Fudge builder for {@code FXSecurity}.
 */
@FudgeBuilderFor(FXSecurity.class)
public class FXSecurityFudgeBuilder extends AbstractFudgeBuilder implements FudgeBuilder<FXSecurity> {

  /** Field name. */
  public static final String PAY_CURRENCY_FIELD_NAME = "payCurrency";
  /** Field name. */
  public static final String RECEIVE_CURRENCY_FIELD_NAME = "receiveCurrency";
  /** Field name. */
  public static final String PAY_AMOUNT_FIELD_NAME = "payAmount";
  /** Field name. */
  public static final String RECEIVE_AMOUNT_FIELD_NAME = "receiveAmount";
  /** Field name. */
  public static final String REGION_FIELD_NAME = "region";

  @Override
  public MutableFudgeMsg buildMessage(FudgeSerializer serializer, FXSecurity object) {
    final MutableFudgeMsg msg = serializer.newMessage();
    FXSecurityFudgeBuilder.toFudgeMsg(serializer, object, msg);
    return msg;
  }

  public static void toFudgeMsg(FudgeSerializer serializer, FXSecurity object, final MutableFudgeMsg msg) {
    FinancialSecurityFudgeBuilder.toFudgeMsg(serializer, object, msg);
    addToMessage(msg, PAY_CURRENCY_FIELD_NAME, object.getPayCurrency());
    addToMessage(msg, RECEIVE_CURRENCY_FIELD_NAME, object.getReceiveCurrency());
    addToMessage(msg, PAY_AMOUNT_FIELD_NAME, object.getPayAmount());
    addToMessage(msg, RECEIVE_AMOUNT_FIELD_NAME, object.getReceiveAmount());
    addToMessage(msg, REGION_FIELD_NAME, ExternalIdFudgeBuilder.toFudgeMsg(serializer, object.getRegionId()));
  }

  @Override
  public FXSecurity buildObject(FudgeDeserializer deserializer, FudgeMsg msg) {
    FXSecurity object = new FXSecurity();
    FXSecurityFudgeBuilder.fromFudgeMsg(deserializer, msg, object);
    return object;
  }

  public static void fromFudgeMsg(FudgeDeserializer deserializer, FudgeMsg msg, FXSecurity object) {
    FinancialSecurityFudgeBuilder.fromFudgeMsg(deserializer, msg, object);
    object.setPayCurrency(msg.getValue(Currency.class, PAY_CURRENCY_FIELD_NAME));
    object.setReceiveCurrency(msg.getValue(Currency.class, RECEIVE_CURRENCY_FIELD_NAME));
    object.setPayAmount(msg.getDouble(PAY_AMOUNT_FIELD_NAME));
    object.setReceiveAmount(msg.getDouble(RECEIVE_AMOUNT_FIELD_NAME));
    object.setRegionId(ExternalIdFudgeBuilder.fromFudgeMsg(deserializer, msg.getMessage(REGION_FIELD_NAME)));
  }

}
