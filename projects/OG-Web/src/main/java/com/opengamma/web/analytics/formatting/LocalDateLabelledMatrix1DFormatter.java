/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.analytics.formatting;

import java.util.List;
import java.util.Map;

import org.threeten.bp.LocalDate;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.financial.analytics.LocalDateLabelledMatrix1D;
import com.opengamma.util.ArgumentChecker;

/**
 *
 */
/* package */ class LocalDateLabelledMatrix1DFormatter extends AbstractFormatter<LocalDateLabelledMatrix1D> {

  private static final String DATA = "data";
  private static final String LABELS = "labels";
  private static final String LABEL = "Label";
  private static final String VALUE = "Value";

  private final DoubleFormatter _doubleFormatter;

  /* package */ LocalDateLabelledMatrix1DFormatter(DoubleFormatter doubleFormatter) {
    super(LocalDateLabelledMatrix1D.class);
    ArgumentChecker.notNull(doubleFormatter, "doubleFormatter");
    _doubleFormatter = doubleFormatter;
    addFormatter(new Formatter<LocalDateLabelledMatrix1D>(Format.EXPANDED) {
      @Override
      Object format(LocalDateLabelledMatrix1D value, ValueSpecification valueSpec, Object inlineKey) {
        return formatExpanded(value, valueSpec);
      }
    });
    addFormatter(new Formatter<LocalDateLabelledMatrix1D>(Format.HISTORY) {
      @Override
      Object format(LocalDateLabelledMatrix1D value, ValueSpecification valueSpec, Object inlineKey) {
        if (inlineKey == null) {
          return ResultsFormatter.VALUE_UNAVAILABLE;
        } else {
          return formatInline(value, valueSpec, Format.HISTORY, inlineKey);
        }
      }
    });
  }

  @Override
  public Object formatCell(LocalDateLabelledMatrix1D value, ValueSpecification valueSpec, Object inlineKey) {
    if (inlineKey == null) {
      return "Vector (" + value.getKeys().length + ")";
    } else {
      return formatInline(value, valueSpec, Format.CELL, inlineKey);
    }
  }

  private Map<String, Object> formatExpanded(LocalDateLabelledMatrix1D value, ValueSpecification valueSpec) {
    Map<String, Object> resultsMap = Maps.newHashMap();
    int length = value.getKeys().length;
    List<List<String>> results = Lists.newArrayListWithCapacity(length);
    for (int i = 0; i < length; i++) {
      String label = value.getLabels()[i].toString();
      String formattedValue = _doubleFormatter.formatCell(value.getValues()[i], valueSpec, null);
      List<String> rowResults = ImmutableList.of(label, formattedValue);
      results.add(rowResults);
    }
    resultsMap.put(DATA, results);
    String labelsTitle = value.getLabelsTitle() != null ? value.getLabelsTitle() : LABEL;
    String valuesTitle = value.getValuesTitle() != null ? value.getValuesTitle() : VALUE;
    resultsMap.put(LABELS, ImmutableList.of(labelsTitle, valuesTitle));
    return resultsMap;
  }

  private Object formatInline(LocalDateLabelledMatrix1D matrix,
                              ValueSpecification valueSpec,
                              Format format,
                              Object inlineKey) {
    // if there are matrices of different lengths on different rows then the shorter ones will be missing values for
    // the last columns
    LocalDate dateKey = (LocalDate) inlineKey;
    int index = 0;
    for (LocalDate localDate : matrix.getKeys()) {
      if (dateKey.equals(localDate)) {
        return _doubleFormatter.format(matrix.getValues()[index], valueSpec, format, inlineKey);
      }
      index++;
    }
    return ResultsFormatter.VALUE_UNAVAILABLE;
  }

  @Override
  public DataType getDataType() {
    return DataType.LABELLED_MATRIX_1D;
  }
}
