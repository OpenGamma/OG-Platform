/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

package com.opengamma.language.timeseries;

import static com.opengamma.language.convert.TypeMap.ZERO_LOSS;

import java.util.Iterator;
import java.util.Map;

import org.threeten.bp.LocalDate;

import com.opengamma.language.Value;
import com.opengamma.language.ValueUtils;
import com.opengamma.language.convert.TypeMap;
import com.opengamma.language.convert.ValueConversionContext;
import com.opengamma.language.definition.JavaTypeInfo;
import com.opengamma.language.invoke.AbstractTypeConverter;
import com.opengamma.timeseries.date.localdate.ImmutableLocalDateDoubleTimeSeries;
import com.opengamma.timeseries.date.localdate.LocalDateDoubleTimeSeries;

/**
 * Converts a {@link LocalDateDoubleTimeSeries} to/from a 2D value array.
 */
public class LocalDateDoubleTimeSeriesConverter extends AbstractTypeConverter {

  /**
   * Default instance.
   */
  public static final LocalDateDoubleTimeSeriesConverter INSTANCE = new LocalDateDoubleTimeSeriesConverter();

  // TODO: handle nulls

  private static final JavaTypeInfo<Value> VALUE = JavaTypeInfo.builder(Value.class).get();
  private static final JavaTypeInfo<Value[][]> VALUES = JavaTypeInfo.builder(Value[][].class).get();
  private static final JavaTypeInfo<LocalDate> LOCAL_DATE = JavaTypeInfo.builder(LocalDate.class).get();
  private static final JavaTypeInfo<Double> DOUBLE = JavaTypeInfo.builder(Double.class).get();
  private static final JavaTypeInfo<LocalDateDoubleTimeSeries> LOCAL_DATE_DOUBLE_TIME_SERIES = JavaTypeInfo.builder(LocalDateDoubleTimeSeries.class).get();

  private static final TypeMap TO_LOCAL_DATE_DOUBLE_TIME_SERIES = TypeMap.of(ZERO_LOSS, VALUES);
  private static final TypeMap FROM_LOCAL_DATE_DOUBLE_TIME_SERIES = TypeMap.of(ZERO_LOSS, LOCAL_DATE_DOUBLE_TIME_SERIES);

  protected LocalDateDoubleTimeSeriesConverter() {
  }

  @Override
  public boolean canConvertTo(final JavaTypeInfo<?> targetType) {
    return (targetType.getRawClass() == LocalDateDoubleTimeSeries.class) || (targetType.getRawClass() == Value[][].class);
  }

  @Override
  public void convertValue(final ValueConversionContext conversionContext, final Object value, final JavaTypeInfo<?> type) {
    if (type.getRawClass() == LocalDateDoubleTimeSeries.class) {
      // Converting from Values[][] to LocalDateDoubleTimeSeries
      final Value[][] values = (Value[][]) value;
      final LocalDate[] timeSeriesDates = new LocalDate[values.length];
      final double[] timeSeriesValues = new double[values.length];
      int i = 0;
      for (Value[] entry : values) {
        if (entry.length != 2) {
          conversionContext.setFail();
          return;
        }
        conversionContext.convertValue(entry[0], LOCAL_DATE);
        if (conversionContext.isFailed()) {
          conversionContext.setFail();
          return;
        }
        timeSeriesDates[i] = (LocalDate) conversionContext.getResult();
        conversionContext.convertValue(entry[1], DOUBLE);
        if (conversionContext.isFailed()) {
          conversionContext.setFail();
          return;
        }
        timeSeriesValues[i++] = (Double) conversionContext.getResult();
      }
      conversionContext.setResult(ImmutableLocalDateDoubleTimeSeries.of(timeSeriesDates, timeSeriesValues));
    } else {
      // Converting from LocalDateDoubleTimeSeries to Values[][]
      final LocalDateDoubleTimeSeries timeSeries = (LocalDateDoubleTimeSeries) value;
      final Value[][] result = new Value[timeSeries.size()][2];
      final Iterator<Map.Entry<LocalDate, Double>> entries = timeSeries.iterator();
      int i = 0;
      while (entries.hasNext()) {
        final Map.Entry<LocalDate, Double> entry = entries.next();
        conversionContext.convertValue(entry.getKey(), VALUE);
        if (conversionContext.isFailed()) {
          conversionContext.setFail();
          return;
        }
        result[i][0] = (Value) conversionContext.getResult();
        result[i++][1] = ValueUtils.of(entry.getValue());
      }
      conversionContext.setResult(result);
    }
  }

  @Override
  public Map<JavaTypeInfo<?>, Integer> getConversionsTo(final JavaTypeInfo<?> targetType) {
    if (targetType.getRawClass() == LocalDateDoubleTimeSeries.class) {
      return TO_LOCAL_DATE_DOUBLE_TIME_SERIES;
    } else {
      return FROM_LOCAL_DATE_DOUBLE_TIME_SERIES;
    }
  }

}
