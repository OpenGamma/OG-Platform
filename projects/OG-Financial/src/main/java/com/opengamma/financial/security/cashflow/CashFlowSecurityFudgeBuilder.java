/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.security.cashflow;

import org.fudgemsg.FudgeMsg;
import org.fudgemsg.MutableFudgeMsg;
import org.fudgemsg.mapping.FudgeBuilder;
import org.fudgemsg.mapping.FudgeBuilderFor;
import org.fudgemsg.mapping.FudgeDeserializer;
import org.fudgemsg.mapping.FudgeSerializer;

import com.opengamma.financial.security.FinancialSecurityFudgeBuilder;
import com.opengamma.util.fudgemsg.AbstractFudgeBuilder;
import com.opengamma.util.money.Currency;
import com.opengamma.util.time.ZonedDateTimeFudgeBuilder;

/**
 * A Fudge builder for {@code CashSecurity}.
 */
@FudgeBuilderFor(CashFlowSecurity.class)
public class CashFlowSecurityFudgeBuilder extends AbstractFudgeBuilder implements FudgeBuilder<CashFlowSecurity> {

  /** Field name. */
  public static final String CURRENCY_FIELD_NAME = "currency";
  /** Field name. */
  public static final String SETTLEMENT_FIELD_NAME = "settlement";
  /** Field name. */
  public static final String AMOUNT_FIELD_NAME = "amount";

  @Override
  public MutableFudgeMsg buildMessage(FudgeSerializer serializer, CashFlowSecurity object) {
    final MutableFudgeMsg msg = serializer.newMessage();
    CashFlowSecurityFudgeBuilder.toFudgeMsg(serializer, object, msg);
    return msg;
  }

  public static void toFudgeMsg(FudgeSerializer serializer, CashFlowSecurity object, final MutableFudgeMsg msg) {
    FinancialSecurityFudgeBuilder.toFudgeMsg(serializer, object, msg);
    addToMessage(msg, CURRENCY_FIELD_NAME, object.getCurrency());
    addToMessage(msg, SETTLEMENT_FIELD_NAME, ZonedDateTimeFudgeBuilder.toFudgeMsg(serializer, object.getSettlement()));
    addToMessage(msg, AMOUNT_FIELD_NAME, object.getAmount());
  }

  @Override
  public CashFlowSecurity buildObject(FudgeDeserializer deserializer, FudgeMsg msg) {
    CashFlowSecurity object = new CashFlowSecurity();
    CashFlowSecurityFudgeBuilder.fromFudgeMsg(deserializer, msg, object);
    return object;
  }

  public static void fromFudgeMsg(FudgeDeserializer deserializer, FudgeMsg msg, CashFlowSecurity object) {
    FinancialSecurityFudgeBuilder.fromFudgeMsg(deserializer, msg, object);
    object.setCurrency(msg.getValue(Currency.class, CURRENCY_FIELD_NAME));
    object.setSettlement(ZonedDateTimeFudgeBuilder.fromFudgeMsg(deserializer, msg.getMessage(SETTLEMENT_FIELD_NAME)));
    object.setAmount(msg.getDouble(AMOUNT_FIELD_NAME));
  }

}
