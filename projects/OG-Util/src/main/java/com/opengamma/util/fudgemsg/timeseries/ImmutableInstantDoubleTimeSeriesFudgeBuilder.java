/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.util.fudgemsg.timeseries;

import org.fudgemsg.FudgeMsg;
import org.fudgemsg.MutableFudgeMsg;
import org.fudgemsg.mapping.FudgeBuilder;
import org.fudgemsg.mapping.FudgeBuilderFor;
import org.fudgemsg.mapping.FudgeDeserializer;
import org.fudgemsg.mapping.FudgeSerializer;
import org.fudgemsg.wire.types.FudgeWireType;

import com.opengamma.timeseries.precise.instant.ImmutableInstantDoubleTimeSeries;

/**
 * Fudge message encoder/decoder (builder) for ImmutableInstantDoubleTimeSeries
 */
@FudgeBuilderFor(ImmutableInstantDoubleTimeSeries.class)
public class ImmutableInstantDoubleTimeSeriesFudgeBuilder implements FudgeBuilder<ImmutableInstantDoubleTimeSeries> {

  /** Field name. */
  public static final String INSTANTS = "instants";
  /** Field name. */
  public static final String VALUES = "values";

  @Override
  public MutableFudgeMsg buildMessage(FudgeSerializer serializer, ImmutableInstantDoubleTimeSeries object) {
    final MutableFudgeMsg message = serializer.newMessage();
    message.add(null, 0, FudgeWireType.STRING, object.getClass().getName()); // we need to stick the class name in so receiver knows.
    message.add(INSTANTS, null, FudgeWireType.LONG_ARRAY, object.timesArrayFast());
    message.add(VALUES, null, FudgeWireType.DOUBLE_ARRAY, object.valuesArrayFast());
    return message;
  }

  @Override
  public ImmutableInstantDoubleTimeSeries buildObject(FudgeDeserializer deserializer, FudgeMsg message) {
    // read old ZonedDateTimeDoubleTimeSeries, see OpenGammaFudgeContext
    if (message.getByOrdinal(0).toString().contains("ZonedDateTimeDoubleTimeSeries")) {
      FudgeMsg fastSeries = message.getMessage(2);
      String encoding = fastSeries.getString(1);
      long[] instants = (long[]) fastSeries.getValue(2);
      double[] values = (double[]) fastSeries.getValue(3);
      if (encoding.equals("TIME_EPOCH_NANOS")) {  // CSIGNORE
        // correct encoding
      } else if (encoding.equals("TIME_EPOCH_MILLIS")) {
        for (int i = 0; i < instants.length; i++) {
          instants[i] = instants[i] * 1_000_000;
        }
      } else if (encoding.equals("TIME_EPOCH_SECONDS")) {
        for (int i = 0; i < instants.length; i++) {
          instants[i] = instants[i] * 1_000_000_000;
        }
      } else {
        throw new IllegalStateException("Unknown time-series encoding");
      }
      return ImmutableInstantDoubleTimeSeries.of(instants, values);
    }
    long[] instants = (long[]) message.getValue(INSTANTS);
    double[] values = (double[]) message.getValue(VALUES);
    return ImmutableInstantDoubleTimeSeries.of(instants, values);
  }

}
