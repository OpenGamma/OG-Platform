/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.var.parametric;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.Validate;

import com.opengamma.analytics.financial.covariance.CovarianceMatrixCalculator;
import com.opengamma.analytics.financial.greeks.MixedOrderUnderlying;
import com.opengamma.analytics.financial.greeks.NthOrderUnderlying;
import com.opengamma.analytics.financial.pnl.SensitivityAndReturnDataBundle;
import com.opengamma.analytics.financial.pnl.UnderlyingType;
import com.opengamma.analytics.financial.sensitivity.Sensitivity;
import com.opengamma.analytics.math.function.Function;
import com.opengamma.analytics.math.matrix.DoubleMatrix1D;
import com.opengamma.analytics.math.matrix.DoubleMatrix2D;
import com.opengamma.timeseries.DoubleTimeSeries;

/**
 * 
 */
public class VaRCovarianceMatrixCalculator implements Function<SensitivityAndReturnDataBundle, Map<Integer, ParametricVaRDataBundle>> {
  private final CovarianceMatrixCalculator _calculator;
  private static final DoubleTimeSeries<?>[] EMPTY = new DoubleTimeSeries<?>[0];

  public VaRCovarianceMatrixCalculator(final CovarianceMatrixCalculator calculator) {
    Validate.notNull(calculator, "calculator");
    _calculator = calculator;
  }

  @Override
  public Map<Integer, ParametricVaRDataBundle> evaluate(final SensitivityAndReturnDataBundle... data) {
    Validate.notNull(data, "data");
    Validate.notEmpty(data, "data");
    Validate.noNullElements(data, "data");
    List<String> firstOrderNames = null;
    final List<Double> firstOrderSensitivity = new ArrayList<>();
    final List<DoubleTimeSeries<?>> firstOrderTimeSeries = new ArrayList<>();
    final List<DoubleTimeSeries<?>> secondOrderTimeSeries = new ArrayList<>();
    List<String> secondOrderNames = null;
    for (final SensitivityAndReturnDataBundle dataForSensitivity : data) {
      final Sensitivity<?> sensitivity = dataForSensitivity.getSensitivity();
      final String identifier = sensitivity.getIdentifier();
      if (sensitivity.getOrder() == 1) {
        final UnderlyingType type = sensitivity.getUnderlyingTypes().get(0);
        final String name = identifier + "_" + type;
        if (firstOrderNames == null) {
          firstOrderNames = new ArrayList<>();
        }
        if (!firstOrderNames.contains(name)) {
          firstOrderNames.add(name);
          firstOrderSensitivity.add(dataForSensitivity.getValue());
          firstOrderTimeSeries.add(dataForSensitivity.getReturnTimeSeriesForUnderlying(type));
        } else {
          final int index = firstOrderNames.indexOf(name);
          firstOrderSensitivity.set(index, firstOrderSensitivity.get(index) + dataForSensitivity.getValue());
        }
      } else if (sensitivity.getOrder() == 2) {
        if (secondOrderNames == null) {
          secondOrderNames = new ArrayList<>();
        }
        if (sensitivity.getUnderlying() instanceof NthOrderUnderlying) {
          final UnderlyingType type = sensitivity.getUnderlyingTypes().get(0);
          final String name = identifier + "_" + type;
          if (!secondOrderNames.contains(name)) {
            secondOrderNames.add(name);
            secondOrderTimeSeries.add(dataForSensitivity.getReturnTimeSeriesForUnderlying(type));
          }
        } else if (sensitivity.getUnderlying() instanceof MixedOrderUnderlying) {
          final UnderlyingType type1 = sensitivity.getUnderlyingTypes().get(0);
          final UnderlyingType type2 = sensitivity.getUnderlyingTypes().get(1);
          final String name1 = identifier + "_" + type1;
          final String name2 = identifier + "_" + type2;
          if (!secondOrderNames.contains(name1)) {
            secondOrderNames.add(name1);
            secondOrderTimeSeries.add(dataForSensitivity.getReturnTimeSeriesForUnderlying(type1));
          }
          if (!secondOrderNames.contains(name2)) {
            secondOrderNames.add(name2);
            secondOrderTimeSeries.add(dataForSensitivity.getReturnTimeSeriesForUnderlying(type2));
          }
        }
      } else {
        throw new IllegalArgumentException("Can only handle first and second order sensitivities");
      }
    }
    final Map<Integer, ParametricVaRDataBundle> result = new HashMap<>();
    DoubleMatrix2D firstOrderCovarianceMatrix = null;
    DoubleMatrix1D firstOrderSensitivityMatrix = null;
    DoubleMatrix2D secondOrderCovarianceMatrix = null;
    DoubleMatrix2D secondOrderSensitivityMatrix = null;
    if (firstOrderNames != null) {
      firstOrderCovarianceMatrix = _calculator.evaluate(firstOrderTimeSeries.toArray(EMPTY));
      firstOrderSensitivityMatrix = new DoubleMatrix1D(firstOrderSensitivity.toArray(new Double[firstOrderSensitivity.size()]));
      result.put(1, new ParametricVaRDataBundle(firstOrderNames, firstOrderSensitivityMatrix, firstOrderCovarianceMatrix, 1));
    }
    if (secondOrderNames != null) {
      final int n = secondOrderNames.size();
      final double[][] secondOrderSensitivities = new double[n][n];
      for (final SensitivityAndReturnDataBundle bundle : data) {
        final Sensitivity<?> sensitivity = bundle.getSensitivity();
        final String identifier = sensitivity.getIdentifier();
        if (sensitivity.getOrder() == 2) {
          if (sensitivity.getUnderlying() instanceof NthOrderUnderlying) {
            final UnderlyingType type = sensitivity.getUnderlyingTypes().get(0);
            final String name = identifier + "_" + type;
            final int index = secondOrderNames.indexOf(name);
            secondOrderSensitivities[index][index] += bundle.getValue();
          } else if (sensitivity.getUnderlying() instanceof MixedOrderUnderlying) {
            final UnderlyingType type1 = sensitivity.getUnderlyingTypes().get(0);
            final UnderlyingType type2 = sensitivity.getUnderlyingTypes().get(1);
            final String name1 = identifier + "_" + type1;
            final String name2 = identifier + "_" + type2;
            final int index1 = secondOrderNames.indexOf(name1);
            final int index2 = secondOrderNames.indexOf(name2);
            secondOrderSensitivities[index1][index2] += bundle.getValue();
            secondOrderSensitivities[index2][index1] = secondOrderSensitivities[index1][index2];
          }
        }
      }
      secondOrderCovarianceMatrix = _calculator.evaluate(secondOrderTimeSeries.toArray(EMPTY));
      secondOrderSensitivityMatrix = new DoubleMatrix2D(secondOrderSensitivities);
      result.put(2, new ParametricVaRDataBundle(secondOrderNames, secondOrderSensitivityMatrix, secondOrderCovarianceMatrix, 2));
    }
    return result;
  }
}
