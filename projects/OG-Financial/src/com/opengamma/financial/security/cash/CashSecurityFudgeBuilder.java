/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.security.cash;

import org.fudgemsg.FudgeMsg;
import org.fudgemsg.MutableFudgeMsg;
import org.fudgemsg.mapping.FudgeBuilder;
import org.fudgemsg.mapping.FudgeBuilderFor;
import org.fudgemsg.mapping.FudgeDeserializer;
import org.fudgemsg.mapping.FudgeSerializer;

import com.opengamma.financial.convention.daycount.DayCount;
import com.opengamma.financial.security.FinancialSecurityFudgeBuilder;
import com.opengamma.id.ExternalIdFudgeBuilder;
import com.opengamma.util.fudgemsg.AbstractFudgeBuilder;
import com.opengamma.util.money.Currency;
import com.opengamma.util.time.ZonedDateTimeFudgeBuilder;

/**
 * A Fudge builder for {@code CashSecurity}.
 */
@FudgeBuilderFor(CashSecurity.class)
public class CashSecurityFudgeBuilder extends AbstractFudgeBuilder implements FudgeBuilder<CashSecurity> {

  /** Field name. */
  public static final String CURRENCY_FIELD_NAME = "currency";
  /** Field name. */
  public static final String REGION_FIELD_NAME = "region";
  /** Field name. */
  public static final String START_FIELD_NAME = "start";
  /** Field name. */
  public static final String MATURITY_FIELD_NAME = "maturity";
  /** Field name. */
  public static final String DAYCOUNT_FIELD_NAME = "dayCount";
  /** Field name. */
  public static final String RATE_FIELD_NAME = "rate";
  /** Field name. */
  public static final String AMOUNT_FIELD_NAME = "amount";

  @Override
  public MutableFudgeMsg buildMessage(FudgeSerializer serializer, CashSecurity object) {
    final MutableFudgeMsg msg = serializer.newMessage();
    CashSecurityFudgeBuilder.toFudgeMsg(serializer, object, msg);
    return msg;
  }

  public static void toFudgeMsg(FudgeSerializer serializer, CashSecurity object, final MutableFudgeMsg msg) {
    FinancialSecurityFudgeBuilder.toFudgeMsg(serializer, object, msg);
    addToMessage(msg, CURRENCY_FIELD_NAME, object.getCurrency());
    addToMessage(msg, REGION_FIELD_NAME, ExternalIdFudgeBuilder.toFudgeMsg(serializer, object.getRegionId()));
    addToMessage(msg, START_FIELD_NAME, ZonedDateTimeFudgeBuilder.toFudgeMsg(serializer, object.getStart()));
    addToMessage(msg, MATURITY_FIELD_NAME, ZonedDateTimeFudgeBuilder.toFudgeMsg(serializer, object.getMaturity()));
    addToMessage(msg, DAYCOUNT_FIELD_NAME, object.getDayCount());
    addToMessage(msg, RATE_FIELD_NAME, object.getRate());
    addToMessage(msg, AMOUNT_FIELD_NAME, object.getAmount());
  }

  @Override
  public CashSecurity buildObject(FudgeDeserializer deserializer, FudgeMsg msg) {
    CashSecurity object = new CashSecurity();
    CashSecurityFudgeBuilder.fromFudgeMsg(deserializer, msg, object);
    return object;
  }

  public static void fromFudgeMsg(FudgeDeserializer deserializer, FudgeMsg msg, CashSecurity object) {
    FinancialSecurityFudgeBuilder.fromFudgeMsg(deserializer, msg, object);
    object.setCurrency(msg.getValue(Currency.class, CURRENCY_FIELD_NAME));
    object.setRegionId(ExternalIdFudgeBuilder.fromFudgeMsg(deserializer, msg.getMessage(REGION_FIELD_NAME)));
    object.setStart(ZonedDateTimeFudgeBuilder.fromFudgeMsg(deserializer, msg.getMessage(START_FIELD_NAME)));
    object.setMaturity(ZonedDateTimeFudgeBuilder.fromFudgeMsg(deserializer, msg.getMessage(MATURITY_FIELD_NAME)));
    object.setDayCount(msg.getValue(DayCount.class, DAYCOUNT_FIELD_NAME));
    object.setRate(msg.getDouble(RATE_FIELD_NAME));
    object.setAmount(msg.getDouble(AMOUNT_FIELD_NAME));
  }

}
