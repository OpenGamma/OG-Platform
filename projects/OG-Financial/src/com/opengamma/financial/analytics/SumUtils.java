/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.opengamma.engine.value.ValueProperties;
import com.opengamma.engine.value.ValueRequirementNames;
import com.opengamma.util.timeseries.DoubleTimeSeries;
import com.opengamma.util.tuple.DoublesPair;

/**
 * 
 */
public class SumUtils {

  public static Object addValue(final Object currentTotal, final Object value, final String valueName) {
    if (currentTotal == null) {
      return value;
    }
    if (currentTotal.getClass() != value.getClass()) {
      throw new IllegalArgumentException("Inputs have different value types for requirement " + valueName);
    }
    if (value instanceof Double) {
      final Double previousDouble = (Double) currentTotal;
      return previousDouble + (Double) value;
    } else if (value instanceof BigDecimal) {
      final BigDecimal previousDecimal = (BigDecimal) currentTotal;
      return previousDecimal.add((BigDecimal) value);
    } else if (value instanceof DoubleTimeSeries<?>) {
      final DoubleTimeSeries<?> previousTS = (DoubleTimeSeries<?>) currentTotal;
      return previousTS.add((DoubleTimeSeries<?>) value);
    } else if (value instanceof DoubleLabelledMatrix1D) {
      final DoubleLabelledMatrix1D previousMatrix = (DoubleLabelledMatrix1D) currentTotal;
      final DoubleLabelledMatrix1D currentMatrix = (DoubleLabelledMatrix1D) value;
      return previousMatrix.add(currentMatrix);
    } else if (value instanceof LocalDateLabelledMatrix1D) {
      final LocalDateLabelledMatrix1D previousMatrix = (LocalDateLabelledMatrix1D) currentTotal;
      final LocalDateLabelledMatrix1D currentMatrix = (LocalDateLabelledMatrix1D) value;
      return previousMatrix.add(currentMatrix);
    } else if (value instanceof ZonedDateTimeLabelledMatrix1D) {
      final ZonedDateTimeLabelledMatrix1D previousMatrix = (ZonedDateTimeLabelledMatrix1D) currentTotal;
      final ZonedDateTimeLabelledMatrix1D currentMatrix = (ZonedDateTimeLabelledMatrix1D) value;
      return previousMatrix.add(currentMatrix);
    } else if (value instanceof CurrencyLabelledMatrix1D) {
      final CurrencyLabelledMatrix1D previousMatrix = (CurrencyLabelledMatrix1D) currentTotal;
      final CurrencyLabelledMatrix1D currentMatrix = (CurrencyLabelledMatrix1D) value;
      return previousMatrix.addIgnoringLabel(currentMatrix);
    } else if (value instanceof StringLabelledMatrix1D) {
      final StringLabelledMatrix1D previousMatrix = (StringLabelledMatrix1D) currentTotal;
      final StringLabelledMatrix1D currentMatrix = (StringLabelledMatrix1D) value;
      return previousMatrix.addIgnoringLabel(currentMatrix);
    } else if (valueName.equals(ValueRequirementNames.PRESENT_VALUE_CURVE_SENSITIVITY)) { //TODO this should probably not be done like this
      @SuppressWarnings("unchecked")
      final Map<String, List<DoublesPair>> previousMap = (Map<String, List<DoublesPair>>) currentTotal;
      @SuppressWarnings("unchecked")
      final Map<String, List<DoublesPair>> currentMap = (Map<String, List<DoublesPair>>) value;
      final Map<String, List<DoublesPair>> result = new HashMap<String, List<DoublesPair>>();
      for (final String name : previousMap.keySet()) {
        final List<DoublesPair> temp = new ArrayList<DoublesPair>();
        for (final DoublesPair pair : previousMap.get(name)) {
          temp.add(pair);
        }
        if (currentMap.containsKey(name)) {
          for (final DoublesPair pair : currentMap.get(name)) {
            temp.add(pair);
          }
        }
        result.put(name, temp);
      }
      for (final String name : currentMap.keySet()) {
        if (!result.containsKey(name)) {
          final List<DoublesPair> temp = new ArrayList<DoublesPair>();
          for (final DoublesPair pair : currentMap.get(name)) {
            temp.add(pair);
          }
          result.put(name, temp);
        }
      }
    } else if (value instanceof DoubleLabelledMatrix2D) {
      final DoubleLabelledMatrix2D previousMatrix = (DoubleLabelledMatrix2D) currentTotal;
      final DoubleLabelledMatrix2D currentMatrix = (DoubleLabelledMatrix2D) value;
      return previousMatrix.add(currentMatrix, 0.005, 0.005);
    } else if (value instanceof DoubleLabelledMatrix3D) {
      final DoubleLabelledMatrix3D previousMatrix = (DoubleLabelledMatrix3D) currentTotal;
      final DoubleLabelledMatrix3D currentMatrix = (DoubleLabelledMatrix3D) value;
      return previousMatrix.add((LabelledMatrix3D<Double, Double, Double, Double, Double, Double, DoubleLabelledMatrix3D>) currentMatrix, 0.005, 0.005, 0.005);
    }
    throw new IllegalArgumentException("Can only add Doubles, BigDecimal, DoubleTimeSeries and LabelledMatrix1D (Double, LocalDate and ZonedDateTime), " +
        "or present value curve sensitivities right now.");
  }
  
  public static ValueProperties addProperties(final ValueProperties currentIntersection, final ValueProperties properties) {
    if (currentIntersection == null) {
      return properties;
    } else {
      return currentIntersection.intersect(properties);
    }
  }

}
