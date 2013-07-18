/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
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
import org.threeten.bp.Period;

import com.opengamma.util.fudgemsg.AbstractFudgeBuilder;
import com.opengamma.util.time.Tenor;

/**
 * A Fudge builder for {@link YearOnYearInflationSwapSecurity}
 */
@FudgeBuilderFor(YearOnYearInflationSwapSecurity.class)
public class YearOnYearInflationSwapSecurityFudgeBuilder extends AbstractFudgeBuilder implements FudgeBuilder<YearOnYearInflationSwapSecurity> {
  /** The exchange notional field */
  private static final String EXCHANGE_NOTIONAL_FIELD = "exchangeNotional";
  /** The tenor field */
  private static final String TENOR_FIELD = "tenor";

  @Override
  public MutableFudgeMsg buildMessage(final FudgeSerializer serializer, final YearOnYearInflationSwapSecurity object) {
    final MutableFudgeMsg msg = serializer.newMessage();
    SwapSecurityFudgeBuilder.toFudgeMsg(serializer, object, msg);
    addToMessage(msg, TENOR_FIELD, object.getTenor().getPeriod().toString());
    msg.add(EXCHANGE_NOTIONAL_FIELD, object.isExchangeNotional());
    return msg;
  }

  @Override
  public YearOnYearInflationSwapSecurity buildObject(final FudgeDeserializer deserializer, final FudgeMsg msg) {
    final YearOnYearInflationSwapSecurity object = new YearOnYearInflationSwapSecurity();
    final Tenor tenor = Tenor.of(Period.parse(msg.getString(TENOR_FIELD)));
    SwapSecurityFudgeBuilder.fromFudgeMsg(deserializer, msg, object);
    object.setTenor(tenor);
    object.setExchangeNotional(msg.getBoolean(EXCHANGE_NOTIONAL_FIELD));
    return object;
  }
}
