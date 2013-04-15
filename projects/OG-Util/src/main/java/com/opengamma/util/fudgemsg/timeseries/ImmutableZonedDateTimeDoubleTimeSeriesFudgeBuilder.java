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
import org.threeten.bp.ZoneId;
import org.threeten.bp.ZoneOffset;

import com.opengamma.timeseries.precise.zdt.ImmutableZonedDateTimeDoubleTimeSeries;

/**
 * Fudge message encoder/decoder (builder) for ImmutableZonedDateTimeDoubleTimeSeries
 */
@FudgeBuilderFor(ImmutableZonedDateTimeDoubleTimeSeries.class)
public class ImmutableZonedDateTimeDoubleTimeSeriesFudgeBuilder implements FudgeBuilder<ImmutableZonedDateTimeDoubleTimeSeries> {

  /** Field name. */
  public static final String INSTANTS = "instants";
  /** Field name. */
  public static final String VALUES = "values";
  /** Field name. */
  public static final String ZONE = "zone";

  @Override
  public MutableFudgeMsg buildMessage(FudgeSerializer serializer, ImmutableZonedDateTimeDoubleTimeSeries object) {
    final MutableFudgeMsg message = serializer.newMessage();
    message.add(null, 0, FudgeWireType.STRING, object.getClass().getName()); // we need to stick the class name in so receiver knows.
    message.add(INSTANTS, null, FudgeWireType.LONG_ARRAY, object.timesArrayFast());
    message.add(VALUES, null, FudgeWireType.DOUBLE_ARRAY, object.valuesArrayFast());
    message.add(ZONE, null, FudgeWireType.STRING, object.getZone().getId());
    return message;
  }

  @Override
  public ImmutableZonedDateTimeDoubleTimeSeries buildObject(FudgeDeserializer deserializer, FudgeMsg message) {
    // read old ZonedDateTimeDoubleTimeSeries, see OpenGammaFudgeContext
    if (message.getByOrdinal(0).toString().contains("ZonedDateTimeDoubleTimeSeries")) {
      ZoneId zone = ZoneOffset.UTC;
      try {
        FudgeMsg converter = message.getMessage(1);
        zone = ZoneId.of(converter.getString(1));
      } catch (RuntimeException ex) {
        // ignore
      }
      FudgeMsg fastSeries = message.getMessage(2);
      String encoding = fastSeries.getMessage(1).getString(1);
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
      return ImmutableZonedDateTimeDoubleTimeSeries.of(instants, values, zone);
    }
    long[] instants = (long[]) message.getValue(INSTANTS);
    double[] values = (double[]) message.getValue(VALUES);
    ZoneId zone = ZoneId.of(message.getString(ZONE));
    return ImmutableZonedDateTimeDoubleTimeSeries.of(instants, values, zone);
  }

}
