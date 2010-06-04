/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.util.timeseries.fudge;

import org.fudgemsg.FudgeContextConfiguration;
import org.fudgemsg.mapping.FudgeObjectDictionary;

import com.opengamma.util.timeseries.date.ArrayDateDoubleTimeSeries;
import com.opengamma.util.timeseries.date.DateEpochDaysConverter;
import com.opengamma.util.timeseries.date.ListDateDoubleTimeSeries;
import com.opengamma.util.timeseries.date.MapDateDoubleTimeSeries;
import com.opengamma.util.timeseries.date.time.ArrayDateTimeDoubleTimeSeries;
import com.opengamma.util.timeseries.date.time.DateEpochMillisConverter;
import com.opengamma.util.timeseries.date.time.ListDateTimeDoubleTimeSeries;
import com.opengamma.util.timeseries.date.time.MapDateTimeDoubleTimeSeries;
import com.opengamma.util.timeseries.fast.integer.FastArrayIntDoubleTimeSeries;
import com.opengamma.util.timeseries.fast.integer.FastListIntDoubleTimeSeries;
import com.opengamma.util.timeseries.fast.integer.FastMapIntDoubleTimeSeries;
import com.opengamma.util.timeseries.fast.longint.FastArrayLongDoubleTimeSeries;
import com.opengamma.util.timeseries.fast.longint.FastListLongDoubleTimeSeries;
import com.opengamma.util.timeseries.fast.longint.FastMapLongDoubleTimeSeries;
import com.opengamma.util.timeseries.localdate.ArrayLocalDateDoubleTimeSeries;
import com.opengamma.util.timeseries.localdate.ListLocalDateDoubleTimeSeries;
import com.opengamma.util.timeseries.localdate.LocalDateEpochDaysConverter;
import com.opengamma.util.timeseries.localdate.MapLocalDateDoubleTimeSeries;
import com.opengamma.util.timeseries.sqldate.ArraySQLDateDoubleTimeSeries;
import com.opengamma.util.timeseries.sqldate.ListSQLDateDoubleTimeSeries;
import com.opengamma.util.timeseries.sqldate.MapSQLDateDoubleTimeSeries;
import com.opengamma.util.timeseries.sqldate.SQLDateEpochDaysConverter;
import com.opengamma.util.timeseries.yearoffset.ArrayYearOffsetDoubleTimeSeries;
import com.opengamma.util.timeseries.yearoffset.ListYearOffsetDoubleTimeSeries;
import com.opengamma.util.timeseries.yearoffset.MapYearOffsetDoubleTimeSeries;
import com.opengamma.util.timeseries.yearoffset.YearOffsetEpochMillisConverter;
import com.opengamma.util.timeseries.zoneddatetime.ArrayZonedDateTimeDoubleTimeSeries;
import com.opengamma.util.timeseries.zoneddatetime.ListZonedDateTimeDoubleTimeSeries;
import com.opengamma.util.timeseries.zoneddatetime.MapZonedDateTimeDoubleTimeSeries;
import com.opengamma.util.timeseries.zoneddatetime.ZonedDateTimeEpochMillisConverter;

/**
 * Configure a fudge dictionary to add handlers (builders) for the classes that descend from FastBackedTimeSeries.
 * It does not support other implementations of DoubleTimeSeries, and does not support ObjectTimeSeries at all.
 */
public class TimeSeriesFudgeContextConfiguration extends FudgeContextConfiguration {
  private static final FudgeContextConfiguration INSTANCE = new TimeSeriesFudgeContextConfiguration();
  
  public static FudgeContextConfiguration getInstance() {
    return INSTANCE;
  }
  @Override
  public void configureFudgeObjectDictionary(final FudgeObjectDictionary dictionary) {
    addConcreteTimeSeriesBuilders(dictionary);
    addConverterBuilders(dictionary);
  }
  
  public static void addConcreteTimeSeriesBuilders(FudgeObjectDictionary dictionary) {
    dictionary.addBuilder(FastArrayIntDoubleTimeSeries.class, new FastArrayIntDoubleTimeSeriesBuilder());
    dictionary.addBuilder(FastListIntDoubleTimeSeries.class, new FastListIntDoubleTimeSeriesBuilder());
    dictionary.addBuilder(FastMapIntDoubleTimeSeries.class, new FastMapIntDoubleTimeSeriesBuilder());
    dictionary.addBuilder(FastArrayLongDoubleTimeSeries.class, new FastArrayLongDoubleTimeSeriesBuilder());
    dictionary.addBuilder(FastListLongDoubleTimeSeries.class, new FastListLongDoubleTimeSeriesBuilder());
    dictionary.addBuilder(FastMapLongDoubleTimeSeries.class, new FastMapLongDoubleTimeSeriesBuilder());
    dictionary.addBuilder(ArrayDateDoubleTimeSeries.class, new ArrayDateDoubleTimeSeriesBuilder());
    dictionary.addBuilder(ListDateDoubleTimeSeries.class, new ListDateDoubleTimeSeriesBuilder());
    dictionary.addBuilder(MapDateDoubleTimeSeries.class, new MapDateDoubleTimeSeriesBuilder());
    dictionary.addBuilder(ArrayDateTimeDoubleTimeSeries.class, new ArrayDateTimeDoubleTimeSeriesBuilder());
    dictionary.addBuilder(ListDateTimeDoubleTimeSeries.class, new ListDateTimeDoubleTimeSeriesBuilder());
    dictionary.addBuilder(MapDateTimeDoubleTimeSeries.class, new MapDateTimeDoubleTimeSeriesBuilder());
    dictionary.addBuilder(ArraySQLDateDoubleTimeSeries.class, new ArraySQLDateDoubleTimeSeriesBuilder());
    dictionary.addBuilder(ListSQLDateDoubleTimeSeries.class, new ListSQLDateDoubleTimeSeriesBuilder());
    dictionary.addBuilder(MapSQLDateDoubleTimeSeries.class, new MapSQLDateDoubleTimeSeriesBuilder());
    dictionary.addBuilder(ArrayLocalDateDoubleTimeSeries.class, new ArrayLocalDateDoubleTimeSeriesBuilder());
    dictionary.addBuilder(ListLocalDateDoubleTimeSeries.class, new ListLocalDateDoubleTimeSeriesBuilder());
    dictionary.addBuilder(MapLocalDateDoubleTimeSeries.class, new MapLocalDateDoubleTimeSeriesBuilder());
    dictionary.addBuilder(ArrayZonedDateTimeDoubleTimeSeries.class, new ArrayZonedDateTimeDoubleTimeSeriesBuilder());
    dictionary.addBuilder(ListZonedDateTimeDoubleTimeSeries.class, new ListZonedDateTimeDoubleTimeSeriesBuilder());
    dictionary.addBuilder(MapZonedDateTimeDoubleTimeSeries.class, new MapZonedDateTimeDoubleTimeSeriesBuilder());
    dictionary.addBuilder(ArrayYearOffsetDoubleTimeSeries.class, new ArrayYearOffsetDoubleTimeSeriesBuilder());
    dictionary.addBuilder(ListYearOffsetDoubleTimeSeries.class, new ListYearOffsetDoubleTimeSeriesBuilder());
    dictionary.addBuilder(MapYearOffsetDoubleTimeSeries.class, new MapYearOffsetDoubleTimeSeriesBuilder());
  }
  
  public static void addConverterBuilders(FudgeObjectDictionary dictionary) {
    dictionary.addBuilder(DateEpochDaysConverter.class, new DateEpochDaysConverterBuilder());
    dictionary.addBuilder(DateEpochMillisConverter.class, new DateEpochMillisConverterBuilder());
    dictionary.addBuilder(LocalDateEpochDaysConverter.class, new LocalDateEpochMillisConverterBuilder());
    dictionary.addBuilder(SQLDateEpochDaysConverter.class, new SQLDateEpochDaysConverterBuilder());
    dictionary.addBuilder(ZonedDateTimeEpochMillisConverter.class, new ZonedDateTimeEpochMillisConverterBuilder());
    dictionary.addBuilder(YearOffsetEpochMillisConverter.class, new YearOffsetEpochMillisConverterBuilder());
  }
}
