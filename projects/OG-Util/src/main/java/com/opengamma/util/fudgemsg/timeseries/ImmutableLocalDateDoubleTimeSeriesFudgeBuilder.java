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
import org.threeten.bp.LocalDate;

import com.opengamma.timeseries.date.localdate.ImmutableLocalDateDoubleTimeSeries;
import com.opengamma.timeseries.date.localdate.LocalDateToIntConverter;

/**
 * Fudge message encoder/decoder (builder) for ImmutableLocalDateDoubleTimeSeries
 */
@FudgeBuilderFor(ImmutableLocalDateDoubleTimeSeries.class)
public class ImmutableLocalDateDoubleTimeSeriesFudgeBuilder implements FudgeBuilder<ImmutableLocalDateDoubleTimeSeries> {

  /** Field name. */
  public static final String DATES = "dates";
  /** Field name. */
  public static final String VALUES = "values";

  @Override
  public MutableFudgeMsg buildMessage(FudgeSerializer serializer, ImmutableLocalDateDoubleTimeSeries object) {
    final MutableFudgeMsg message = serializer.newMessage();
    message.add(null, 0, FudgeWireType.STRING, object.getClass().getName()); // we need to stick the class name in so receiver knows.
    message.add(DATES, null, FudgeWireType.INT_ARRAY, object.timesArrayFast());
    message.add(VALUES, null, FudgeWireType.DOUBLE_ARRAY, object.valuesArrayFast());
    return message;
  }

  @Override
  public ImmutableLocalDateDoubleTimeSeries buildObject(FudgeDeserializer deserializer, FudgeMsg message) {
    // read old LocalDateDoubleTimeSeries, see OpenGammaFudgeContext
    if (message.getByOrdinal(0).toString().contains("ArrayLocalDateDoubleTimeSeries") ||
        message.getByOrdinal(0).toString().contains("ListLocalDateDoubleTimeSeries") ||
        message.getByOrdinal(0).toString().contains("MapLocalDateDoubleTimeSeries")) {
      FudgeMsg fastSeries = message.getMessage(2);
      String encoding = fastSeries.getMessage(1).getString(1);
      int[] dates = (int[]) fastSeries.getValue(2);
      double[] values = (double[]) fastSeries.getValue(3);
      if (encoding.equals("DATE_DDMMYYYY")) {  // CSIGNORE
        // correct encoding
      } else if (encoding.equals("DATE_EPOCH_DAYS")) {
        for (int i = 0; i < dates.length; i++) {
          LocalDate d = LocalDate.ofEpochDay(dates[i]);
          dates[i] = LocalDateToIntConverter.convertToInt(d);
        }
      } else {
        throw new IllegalStateException("Unknown time-series encoding");
      }
      return ImmutableLocalDateDoubleTimeSeries.of(dates, values);
    }
    // read new format
    int[] dates = (int[]) message.getValue(DATES);
    double[] values = (double[]) message.getValue(VALUES);
    return ImmutableLocalDateDoubleTimeSeries.of(dates, values);
  }

}
