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

import com.opengamma.financial.security.equity.EquitySecurity;
import com.opengamma.financial.security.equity.GICSCode;
import com.opengamma.util.fudgemsg.AbstractFudgeBuilder;
import com.opengamma.util.money.Currency;

/**
 * A Fudge builder for {@code EquitySecurity}.
 */
@FudgeBuilderFor(EquitySecurity.class)
public class EquitySecurityBuilder extends AbstractFudgeBuilder implements FudgeBuilder<EquitySecurity> {

  /** Field name. */
  public static final String SHORT_NAME_KEY = "shortName";
  /** Field name. */
  public static final String EXCHANGE_KEY = "exchange";
  /** Field name. */
  public static final String EXCHANGE_CODE_KEY = "exchangeCode";
  /** Field name. */
  public static final String COMPANY_NAME_KEY = "companyName";
  /** Field name. */
  public static final String CURRENCY_KEY = "currency";
  /** Field name. */
  public static final String GICS_CODE_KEY = "gicsCode";

  @Override
  public MutableFudgeMsg buildMessage(FudgeSerializer serializer, EquitySecurity object) {
    final MutableFudgeMsg msg = serializer.newMessage();
    EquitySecurityBuilder.toFudgeMsg(serializer, object, msg);
    return msg;
  }

  public static void toFudgeMsg(FudgeSerializer serializer, EquitySecurity object, final MutableFudgeMsg msg) {
    FinancialSecurityBuilder.toFudgeMsg(serializer, object, msg);
    addToMessage(msg, SHORT_NAME_KEY, object.getShortName());
    addToMessage(msg, EXCHANGE_KEY, object.getExchange());
    addToMessage(msg, EXCHANGE_CODE_KEY, object.getExchangeCode());
    addToMessage(msg, COMPANY_NAME_KEY, object.getCompanyName());
    addToMessage(msg, CURRENCY_KEY, object.getCurrency());
    addToMessage(msg, GICS_CODE_KEY, object.getGicsCode());
  }

  @Override
  public EquitySecurity buildObject(FudgeDeserializer deserializer, FudgeMsg msg) {
    EquitySecurity object = FinancialSecurityBuilder.backdoorCreateClass(EquitySecurity.class);
    EquitySecurityBuilder.fromFudgeMsg(deserializer, msg, object);
    return object;
  }

  public static void fromFudgeMsg(FudgeDeserializer deserializer, FudgeMsg msg, EquitySecurity object) {
    FinancialSecurityBuilder.fromFudgeMsg(deserializer, msg, object);
    object.setShortName(msg.getString(SHORT_NAME_KEY));
    object.setExchange(msg.getString(EXCHANGE_KEY));
    object.setExchangeCode(msg.getString(EXCHANGE_CODE_KEY));
    object.setCompanyName(msg.getString(COMPANY_NAME_KEY));
    object.setCurrency(msg.getValue(Currency.class, CURRENCY_KEY));
    object.setGicsCode(msg.getValue(GICSCode.class, GICS_CODE_KEY));
  }

}
