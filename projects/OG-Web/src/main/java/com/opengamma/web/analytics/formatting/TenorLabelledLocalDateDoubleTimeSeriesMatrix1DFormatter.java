/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.web.analytics.formatting;

import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.financial.analytics.TenorLabelledLocalDateDoubleTimeSeriesMatrix1D;
import com.opengamma.timeseries.date.localdate.LocalDateDoubleTimeSeries;
import com.opengamma.util.time.Tenor;

/**
 * 
 */
/*package*/ class TenorLabelledLocalDateDoubleTimeSeriesMatrix1DFormatter extends AbstractFormatter<TenorLabelledLocalDateDoubleTimeSeriesMatrix1D> {

  private LocalDateDoubleTimeSeriesFormatter _timeSeriesFormatter;
  
  /*package*/ TenorLabelledLocalDateDoubleTimeSeriesMatrix1DFormatter(LocalDateDoubleTimeSeriesFormatter timeSeriesFormatter) {
    super(TenorLabelledLocalDateDoubleTimeSeriesMatrix1D.class);
    _timeSeriesFormatter = timeSeriesFormatter;
    
    addFormatter(new Formatter<TenorLabelledLocalDateDoubleTimeSeriesMatrix1D>(Format.EXPANDED) {
      @Override
      Object format(TenorLabelledLocalDateDoubleTimeSeriesMatrix1D value, ValueSpecification valueSpec, Object inlineKey) {
        return formatExpanded(value, valueSpec, inlineKey);
      }
    });

  }
  
  @Override
  public Object formatCell(TenorLabelledLocalDateDoubleTimeSeriesMatrix1D value, ValueSpecification valueSpec, Object inlineKey) {
    if (inlineKey == null) {
      return "Vector (" + value.size() + ")";
    } else {
      return formatInline(value, valueSpec, Format.CELL, inlineKey);
    }
  }
  
  private Object formatInline(TenorLabelledLocalDateDoubleTimeSeriesMatrix1D matrix, ValueSpecification valueSpec, Format format, Object inlineKey) {
    LocalDateDoubleTimeSeries ts = getTimeSeries(matrix, inlineKey);
    return ts != null ? _timeSeriesFormatter.format(ts, valueSpec, format, inlineKey) : null;
  }

  private Object formatExpanded(TenorLabelledLocalDateDoubleTimeSeriesMatrix1D matrix, ValueSpecification valueSpec, Object inlineKey) {
    LocalDateDoubleTimeSeries ts = getTimeSeries(matrix, inlineKey);
    return ts != null ? _timeSeriesFormatter.formatExpanded(ts) : null;
  }

  private LocalDateDoubleTimeSeries getTimeSeries(TenorLabelledLocalDateDoubleTimeSeriesMatrix1D matrix, Object inlineKey) {
    Tenor tenorKey = (Tenor) inlineKey;
    for (int i = 0; i < matrix.size(); i++) {
      if (tenorKey.equals(matrix.getKeys()[i])) {
        return matrix.getValues()[i];
      }
    }
    return null;
  }

  @Override
  public DataType getDataType() {
    return DataType.LABELLED_MATRIX_1D;
  }

}
