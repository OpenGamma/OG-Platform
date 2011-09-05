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

import com.opengamma.financial.security.cash.CashSecurity;
import com.opengamma.util.fudgemsg.AbstractFudgeBuilder;
import com.opengamma.util.fudgemsg.ExternalIdBuilder;
import com.opengamma.util.fudgemsg.ZonedDateTimeBuilder;
import com.opengamma.util.money.Currency;

/**
 * A Fudge builder for {@code CashSecurity}.
 */
@FudgeBuilderFor(CashSecurity.class)
public class CashSecurityBuilder extends AbstractFudgeBuilder implements FudgeBuilder<CashSecurity> {

  /** Field name. */
  public static final String CURRENCY_KEY = "currency";
  /** Field name. */
  public static final String REGION_KEY = "region";
  /** Field name. */
  public static final String MATURITY_KEY = "maturity";
  /** Field name. */
  public static final String RATE_KEY = "rate";
  /** Field name. */
  public static final String AMOUNT_KEY = "amount";

  @Override
  public MutableFudgeMsg buildMessage(FudgeSerializer serializer, CashSecurity object) {
    final MutableFudgeMsg msg = serializer.newMessage();
    CashSecurityBuilder.toFudgeMsg(serializer, object, msg);
    return msg;
  }

  public static void toFudgeMsg(FudgeSerializer serializer, CashSecurity object, final MutableFudgeMsg msg) {
    FinancialSecurityBuilder.toFudgeMsg(serializer, object, msg);
    addToMessage(msg, CURRENCY_KEY, object.getCurrency());
    addToMessage(msg, REGION_KEY, ExternalIdBuilder.toFudgeMsg(serializer, object.getRegionId()));
    addToMessage(msg, MATURITY_KEY, ZonedDateTimeBuilder.toFudgeMsg(serializer, object.getMaturity()));
    addToMessage(msg, RATE_KEY, object.getRate());
    addToMessage(msg, AMOUNT_KEY, object.getAmount());
  }

  @Override
  public CashSecurity buildObject(FudgeDeserializer deserializer, FudgeMsg msg) {
    CashSecurity object = new CashSecurity();
    CashSecurityBuilder.fromFudgeMsg(deserializer, msg, object);
    return object;
  }

  public static void fromFudgeMsg(FudgeDeserializer deserializer, FudgeMsg msg, CashSecurity object) {
    FinancialSecurityBuilder.fromFudgeMsg(deserializer, msg, object);
    object.setCurrency(msg.getValue(Currency.class, CURRENCY_KEY));
    object.setRegionId(ExternalIdBuilder.fromFudgeMsg(deserializer, msg.getMessage(REGION_KEY)));
    object.setMaturity(ZonedDateTimeBuilder.fromFudgeMsg(deserializer, msg.getMessage(MATURITY_KEY)));
    object.setRate(msg.getDouble(RATE_KEY));
    object.setAmount(msg.getDouble(AMOUNT_KEY));
  }

}
