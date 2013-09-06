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

  /** The tenor field */
  private static final String MATURITY_TENOR_FIELD = "maturity_tenor";

  @Override
  public MutableFudgeMsg buildMessage(final FudgeSerializer serializer, final YearOnYearInflationSwapSecurity object) {
    final MutableFudgeMsg msg = serializer.newMessage();
    SwapSecurityFudgeBuilder.toFudgeMsg(serializer, object, msg);
    addToMessage(msg, MATURITY_TENOR_FIELD, object.getMaturityTenor().getPeriod().toString());
    return msg;
  }

  @Override
  public YearOnYearInflationSwapSecurity buildObject(final FudgeDeserializer deserializer, final FudgeMsg msg) {
    final YearOnYearInflationSwapSecurity object = new YearOnYearInflationSwapSecurity();
    final Tenor tenor = Tenor.of(Period.parse(msg.getString(MATURITY_TENOR_FIELD)));
    SwapSecurityFudgeBuilder.fromFudgeMsg(deserializer, msg, object);
    object.setMaturityTenor(tenor);
    return object;
  }
}
