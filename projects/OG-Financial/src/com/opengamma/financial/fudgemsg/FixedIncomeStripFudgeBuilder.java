/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.fudgemsg;

import org.fudgemsg.FudgeMsg;
import org.fudgemsg.MutableFudgeMsg;
import org.fudgemsg.mapping.FudgeBuilder;
import org.fudgemsg.mapping.FudgeBuilderFor;
import org.fudgemsg.mapping.FudgeDeserializer;
import org.fudgemsg.mapping.FudgeSerializer;

import com.opengamma.financial.analytics.ircurve.FixedIncomeStrip;
import com.opengamma.financial.analytics.ircurve.StripInstrumentType;
import com.opengamma.util.time.Tenor;

/**
 * Builder for converting FixedIncomeStrip instances to/from Fudge messages.
 */
@FudgeBuilderFor(FixedIncomeStrip.class)
public class FixedIncomeStripFudgeBuilder implements FudgeBuilder<FixedIncomeStrip> {
  private static final String TYPE = "type";
  private static final String TENOR = "tenor";
  private static final String CONVENTION_NAME = "conventionName";
  private static final String NUM_FUTURES = "numFutures";
  private static final String PERIODS_PER_YEAR = "periodsPerYear";

  @Override
  public MutableFudgeMsg buildMessage(final FudgeSerializer serializer, final FixedIncomeStrip object) {
    final MutableFudgeMsg message = serializer.newMessage();
    serializer.addToMessage(message, TYPE, null, object.getInstrumentType());
    message.add(CONVENTION_NAME, object.getConventionName());
    serializer.addToMessage(message, TENOR, null, object.getCurveNodePointTime());
    if (object.getInstrumentType() == StripInstrumentType.FUTURE) {
      message.add(NUM_FUTURES, object.getNumberOfFuturesAfterTenor());
    }
    if (object.getInstrumentType() == StripInstrumentType.PERIODIC_ZERO_DEPOSIT) {
      message.add(PERIODS_PER_YEAR, object.getPeriodsPerYear());
    }
    return message;
  }

  @Override
  public FixedIncomeStrip buildObject(final FudgeDeserializer deserializer, final FudgeMsg message) {
    final StripInstrumentType type = deserializer.fieldValueToObject(StripInstrumentType.class, message.getByName(TYPE));
    final String conventionName = message.getString(CONVENTION_NAME);
    final Tenor tenor = deserializer.fieldValueToObject(Tenor.class, message.getByName(TENOR));
    if (type == StripInstrumentType.FUTURE) {
      final int numFutures = message.getInt(NUM_FUTURES);
      return new FixedIncomeStrip(type, tenor, numFutures, conventionName);
    } else if (type == StripInstrumentType.PERIODIC_ZERO_DEPOSIT) {
      final int periodsPerYear = message.getInt(PERIODS_PER_YEAR);
      return new FixedIncomeStrip(type, tenor, periodsPerYear, true, conventionName);
    } else {
      return new FixedIncomeStrip(type, tenor, conventionName);
    }
  }

}
