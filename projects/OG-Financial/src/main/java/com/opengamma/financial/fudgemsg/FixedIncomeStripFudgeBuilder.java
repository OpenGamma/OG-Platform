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
import com.opengamma.financial.analytics.ircurve.IndexType;
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
  private static final String PAY_TENOR = "payTenor";
  private static final String RECEIVE_TENOR = "receiveTenor";
  private static final String PAY_INDEX_TYPE = "payIndexType";
  private static final String RECEIVE_INDEX_TYPE = "receiveIndexType";
  private static final String RESET_TENOR = "resetTenor";
  private static final String INDEX_TYPE = "indexType";

  @Override
  public MutableFudgeMsg buildMessage(final FudgeSerializer serializer, final FixedIncomeStrip object) {
    final MutableFudgeMsg message = serializer.newMessage();
    serializer.addToMessage(message, TYPE, null, object.getInstrumentType());
    message.add(CONVENTION_NAME, object.getConventionName());
    serializer.addToMessage(message, TENOR, null, object.getCurveNodePointTime());
    if (object.getInstrumentType() == StripInstrumentType.FUTURE) {
      message.add(NUM_FUTURES, object.getNumberOfFuturesAfterTenor());
    } else if (object.getInstrumentType() == StripInstrumentType.PERIODIC_ZERO_DEPOSIT) {
      message.add(PERIODS_PER_YEAR, object.getPeriodsPerYear());
    } else if (object.getResetTenor() != null) {
      serializer.addToMessage(message, RESET_TENOR, null, object.getResetTenor());
      serializer.addToMessage(message, INDEX_TYPE, null, object.getIndexType());
    } else if (object.getInstrumentType() == StripInstrumentType.BASIS_SWAP) {
      serializer.addToMessage(message, PAY_TENOR, null, object.getPayTenor());
      serializer.addToMessage(message, RECEIVE_TENOR, null, object.getReceiveTenor());
      serializer.addToMessage(message, PAY_INDEX_TYPE, null, object.getPayIndexType());
      serializer.addToMessage(message, RECEIVE_INDEX_TYPE, null, object.getReceiveIndexType());
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
    } else if (message.hasField(RESET_TENOR)) {
      final Tenor resetTenor = deserializer.fieldValueToObject(Tenor.class, message.getByName(RESET_TENOR));
      final IndexType indexType = deserializer.fieldValueToObject(IndexType.class, message.getByName(INDEX_TYPE));
      return new FixedIncomeStrip(type, tenor, resetTenor, indexType, conventionName);
    } else if (type == StripInstrumentType.BASIS_SWAP) {
      final Tenor payTenor = deserializer.fieldValueToObject(Tenor.class, message.getByName(PAY_TENOR));
      final Tenor receiveTenor = deserializer.fieldValueToObject(Tenor.class, message.getByName(RECEIVE_TENOR));
      final IndexType payIndexType = deserializer.fieldValueToObject(IndexType.class, message.getByName(PAY_INDEX_TYPE));
      final IndexType receiveIndexType = deserializer.fieldValueToObject(IndexType.class, message.getByName(RECEIVE_INDEX_TYPE));
      return new FixedIncomeStrip(type, tenor, payTenor, receiveTenor, payIndexType, receiveIndexType, conventionName);
    } else {
      return new FixedIncomeStrip(type, tenor, conventionName);
    }
  }

}
