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
import org.threeten.bp.ZoneId;
import org.threeten.bp.ZoneOffset;

import com.opengamma.timeseries.DoubleTimeSeries;
import com.opengamma.timeseries.date.DateDoubleTimeSeries;
import com.opengamma.timeseries.date.localdate.ImmutableLocalDateDoubleTimeSeries;
import com.opengamma.timeseries.date.localdate.LocalDateToIntConverter;
import com.opengamma.timeseries.precise.PreciseDoubleTimeSeries;
import com.opengamma.timeseries.precise.instant.ImmutableInstantDoubleTimeSeries;
import com.opengamma.timeseries.precise.zdt.ImmutableZonedDateTimeDoubleTimeSeries;
import com.opengamma.timeseries.precise.zdt.ZonedDateTimeDoubleTimeSeries;

/**
 * Fudge message encoder/decoder (builder) for DoubleTimeSeries
 */
@FudgeBuilderFor(DoubleTimeSeries.class)
public class DoubleTimeSeriesFudgeBuilder implements FudgeBuilder<DoubleTimeSeries<?>> {

  /** Field name. */
  public static final String DATES = "dates";
  /** Field name. */
  public static final String INSTANTS = "instants";
  /** Field name. */
  public static final String VALUES = "values";
  /** Field name. */
  public static final String ZONE = "zone";

  /**
   * Singleton instance.
   */
  static final DoubleTimeSeriesFudgeBuilder INSTANCE = new DoubleTimeSeriesFudgeBuilder();

  @Override
  public MutableFudgeMsg buildMessage(FudgeSerializer serializer, DoubleTimeSeries<?> object) {
    final MutableFudgeMsg message = serializer.newMessage();
    message.add(null, 0, FudgeWireType.STRING, DoubleTimeSeries.class.getName()); // we need to stick the class name in so receiver knows.
    if (object instanceof DateDoubleTimeSeries) {
      buildMessage(message, (DateDoubleTimeSeries<?>) object);
    } else if (object instanceof ZonedDateTimeDoubleTimeSeries) {
      buildMessage(message, (ZonedDateTimeDoubleTimeSeries) object);
    } else if (object instanceof PreciseDoubleTimeSeries) {
      buildMessage(message, (PreciseDoubleTimeSeries<?>) object);
    } else {
      throw new IllegalArgumentException("Unknown time-series type");
    }
    return message;
  }

  void buildMessage(final MutableFudgeMsg message, DateDoubleTimeSeries<?> series) {
    message.add(DATES, null, FudgeWireType.INT_ARRAY, series.timesArrayFast());
    message.add(VALUES, null, FudgeWireType.DOUBLE_ARRAY, series.valuesArrayFast());
  }

  void buildMessage(final MutableFudgeMsg message, PreciseDoubleTimeSeries<?> series) {
    message.add(INSTANTS, null, FudgeWireType.LONG_ARRAY, series.timesArrayFast());
    message.add(VALUES, null, FudgeWireType.DOUBLE_ARRAY, series.valuesArrayFast());
  }

  void buildMessage(final MutableFudgeMsg message, ZonedDateTimeDoubleTimeSeries series) {
    message.add(INSTANTS, null, FudgeWireType.LONG_ARRAY, series.timesArrayFast());
    message.add(VALUES, null, FudgeWireType.DOUBLE_ARRAY, series.valuesArrayFast());
    message.add(ZONE, null, FudgeWireType.STRING, series.getZone().getId());
  }

  @Override
  public DoubleTimeSeries<?> buildObject(FudgeDeserializer deserializer, FudgeMsg message) {
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
        throw new IllegalArgumentException("Unknown time-series encoding");
      }
      return ImmutableLocalDateDoubleTimeSeries.of(dates, values);
    }
    
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
        throw new IllegalArgumentException("Unknown time-series encoding");
      }
      return ImmutableZonedDateTimeDoubleTimeSeries.of(instants, values, zone);
    }
    
    // read new format
    int[] dates = (int[]) message.getValue(DATES);
    long[] instants = (long[]) message.getValue(INSTANTS);
    double[] values = (double[]) message.getValue(VALUES);
    String zoneId = message.getString(ZONE);
    if (dates != null) {
      return ImmutableLocalDateDoubleTimeSeries.of(dates, values);
    }
    if (instants != null) {
      if (zoneId != null) {
        ZoneId zone = ZoneId.of(zoneId);
        return ImmutableZonedDateTimeDoubleTimeSeries.of(instants, values, zone);
      } else {
        return ImmutableInstantDoubleTimeSeries.of(instants, values);
      }
    }
    throw new IllegalArgumentException("Unrecognized Fudge message: " + dates + " " + instants + " " + zoneId);
  }

}
