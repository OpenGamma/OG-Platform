/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.security.swap;

import org.fudgemsg.FudgeMsg;
import org.fudgemsg.MutableFudgeMsg;
import org.fudgemsg.mapping.FudgeBuilder;
import org.fudgemsg.mapping.FudgeBuilderFor;
import org.fudgemsg.mapping.FudgeDeserializer;
import org.fudgemsg.mapping.FudgeSerializer;

import com.opengamma.financial.security.FinancialSecurityFudgeBuilder;
import com.opengamma.util.fudgemsg.AbstractFudgeBuilder;
import com.opengamma.util.time.ZonedDateTimeFudgeBuilder;

/**
 * A Fudge builder for {@code SwapSecurity}.
 */
@FudgeBuilderFor(SwapSecurity.class)
public class SwapSecurityFudgeBuilder extends AbstractFudgeBuilder implements FudgeBuilder<SwapSecurity> {

  /** Field name. */
  public static final String TRADE_DATE_FIELD_NAME = "tradeDate";
  /** Field name. */
  public static final String EFFECTIVE_DATE_FIELD_NAME = "effectiveDate";
  /** Field name. */
  public static final String MATURITY_DATE_FIELD_NAME = "maturityDate";
  /** Field name. */
  public static final String COUNTERPARTY_FIELD_NAME = "counterparty";
  /** Field name. */
  public static final String PAY_LEG_FIELD_NAME = "payLeg";
  /** Field name. */
  public static final String RECEIVE_LEG_FIELD_NAME = "receiveLeg";
  /** The exchange initial notional field */
  private static final String EXCHANGE_INITIAL_NOTIONAL_FIELD = "exchangeInitialNotional";
  /** The exchange final notional field */
  private static final String EXCHANGE_FINAL_NOTIONAL_FIELD = "exchangeExchangeNotional";

  @Override
  public MutableFudgeMsg buildMessage(final FudgeSerializer serializer, final SwapSecurity object) {
    final MutableFudgeMsg msg = serializer.newMessage();
    SwapSecurityFudgeBuilder.toFudgeMsg(serializer, object, msg);
    return msg;
  }

  public static void toFudgeMsg(final FudgeSerializer serializer, final SwapSecurity object, final MutableFudgeMsg msg) {
    FinancialSecurityFudgeBuilder.toFudgeMsg(serializer, object, msg);
    addToMessage(msg, TRADE_DATE_FIELD_NAME, ZonedDateTimeFudgeBuilder.toFudgeMsg(serializer, object.getTradeDate()));
    addToMessage(msg, EFFECTIVE_DATE_FIELD_NAME, ZonedDateTimeFudgeBuilder.toFudgeMsg(serializer, object.getEffectiveDate()));
    addToMessage(msg, MATURITY_DATE_FIELD_NAME, ZonedDateTimeFudgeBuilder.toFudgeMsg(serializer, object.getMaturityDate()));
    addToMessage(msg, COUNTERPARTY_FIELD_NAME, object.getCounterparty());
    addToMessage(msg, EXCHANGE_INITIAL_NOTIONAL_FIELD, object.isExchangeInitialNotional());
    addToMessage(msg, EXCHANGE_FINAL_NOTIONAL_FIELD, object.isExchangeFinalNotional());
    addToMessage(serializer, msg, PAY_LEG_FIELD_NAME, object.getPayLeg(), SwapLeg.class);
    addToMessage(serializer, msg, RECEIVE_LEG_FIELD_NAME, object.getReceiveLeg(), SwapLeg.class);
  }

  @Override
  public SwapSecurity buildObject(final FudgeDeserializer deserializer, final FudgeMsg msg) {
    final SwapSecurity object = new SwapSecurity();
    SwapSecurityFudgeBuilder.fromFudgeMsg(deserializer, msg, object);
    return object;
  }

  public static void fromFudgeMsg(final FudgeDeserializer deserializer, final FudgeMsg msg, final SwapSecurity object) {
    FinancialSecurityFudgeBuilder.fromFudgeMsg(deserializer, msg, object);
    object.setTradeDate(ZonedDateTimeFudgeBuilder.fromFudgeMsg(deserializer, msg.getMessage(TRADE_DATE_FIELD_NAME)));
    object.setEffectiveDate(ZonedDateTimeFudgeBuilder.fromFudgeMsg(deserializer, msg.getMessage(EFFECTIVE_DATE_FIELD_NAME)));
    object.setMaturityDate(ZonedDateTimeFudgeBuilder.fromFudgeMsg(deserializer, msg.getMessage(MATURITY_DATE_FIELD_NAME)));
    object.setCounterparty(msg.getString(COUNTERPARTY_FIELD_NAME));
    if (msg.hasField(EXCHANGE_INITIAL_NOTIONAL_FIELD)) {
      object.setExchangeInitialNotional(msg.getBoolean(EXCHANGE_INITIAL_NOTIONAL_FIELD));
    }
    if (msg.hasField(EXCHANGE_FINAL_NOTIONAL_FIELD)) {
      object.setExchangeFinalNotional(msg.getBoolean(EXCHANGE_FINAL_NOTIONAL_FIELD));
    }
    object.setPayLeg(deserializer.fudgeMsgToObject(SwapLeg.class, msg.getMessage(PAY_LEG_FIELD_NAME)));
    object.setReceiveLeg(deserializer.fudgeMsgToObject(SwapLeg.class, msg.getMessage(RECEIVE_LEG_FIELD_NAME)));
  }
}
