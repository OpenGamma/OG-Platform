/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.security.equity;

import org.apache.commons.lang.BooleanUtils;
import org.fudgemsg.FudgeMsg;
import org.fudgemsg.MutableFudgeMsg;
import org.fudgemsg.mapping.FudgeBuilder;
import org.fudgemsg.mapping.FudgeBuilderFor;
import org.fudgemsg.mapping.FudgeDeserializer;
import org.fudgemsg.mapping.FudgeSerializer;

import com.opengamma.financial.security.FinancialSecurityFudgeBuilder;
import com.opengamma.util.fudgemsg.AbstractFudgeBuilder;
import com.opengamma.util.money.Currency;

/**
 * A Fudge builder for {@code EquitySecurity}.
 */
@FudgeBuilderFor(EquitySecurity.class)
public class EquitySecurityFudgeBuilder extends AbstractFudgeBuilder implements FudgeBuilder<EquitySecurity> {

  /** Field name. */
  public static final String SHORT_NAME_FIELD_NAME = "shortName";
  /** Field name. */
  public static final String EXCHANGE_FIELD_NAME = "exchange";
  /** Field name. */
  public static final String EXCHANGE_CODE_FIELD_NAME = "exchangeCode";
  /** Field name. */
  public static final String COMPANY_NAME_FIELD_NAME = "companyName";
  /** Field name. */
  public static final String CURRENCY_FIELD_NAME = "currency";
  /** Field name. */
  public static final String GICS_CODE_FIELD_NAME = "gicsCode";
  /** Field name. */
  public static final String PREFERRED_FIELD_NAME = "preferred";

  @Override
  public MutableFudgeMsg buildMessage(FudgeSerializer serializer, EquitySecurity object) {
    final MutableFudgeMsg msg = serializer.newMessage();
    EquitySecurityFudgeBuilder.toFudgeMsg(serializer, object, msg);
    return msg;
  }

  public static void toFudgeMsg(FudgeSerializer serializer, EquitySecurity object, final MutableFudgeMsg msg) {
    FinancialSecurityFudgeBuilder.toFudgeMsg(serializer, object, msg);
    addToMessage(msg, SHORT_NAME_FIELD_NAME, object.getShortName());
    addToMessage(msg, EXCHANGE_FIELD_NAME, object.getExchange());
    addToMessage(msg, EXCHANGE_CODE_FIELD_NAME, object.getExchangeCode());
    addToMessage(msg, COMPANY_NAME_FIELD_NAME, object.getCompanyName());
    addToMessage(msg, CURRENCY_FIELD_NAME, object.getCurrency());
    addToMessage(msg, GICS_CODE_FIELD_NAME, object.getGicsCode());
    addToMessage(msg, PREFERRED_FIELD_NAME, object.isPreferred());
  }

  @Override
  public EquitySecurity buildObject(FudgeDeserializer deserializer, FudgeMsg msg) {
    EquitySecurity object = new EquitySecurity();
    EquitySecurityFudgeBuilder.fromFudgeMsg(deserializer, msg, object);
    return object;
  }

  public static void fromFudgeMsg(FudgeDeserializer deserializer, FudgeMsg msg, EquitySecurity object) {
    FinancialSecurityFudgeBuilder.fromFudgeMsg(deserializer, msg, object);
    object.setShortName(msg.getString(SHORT_NAME_FIELD_NAME));
    object.setExchange(msg.getString(EXCHANGE_FIELD_NAME));
    object.setExchangeCode(msg.getString(EXCHANGE_CODE_FIELD_NAME));
    object.setCompanyName(msg.getString(COMPANY_NAME_FIELD_NAME));
    object.setCurrency(msg.getValue(Currency.class, CURRENCY_FIELD_NAME));
    object.setGicsCode(msg.getValue(GICSCode.class, GICS_CODE_FIELD_NAME));
    object.setPreferred(BooleanUtils.isTrue(msg.getBoolean(PREFERRED_FIELD_NAME)));
  }

}
