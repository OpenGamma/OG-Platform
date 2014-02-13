/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics;

import static com.opengamma.engine.value.ValueRequirementNames.ALL_PV01S;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.opengamma.analytics.financial.provider.sensitivity.inflation.InflationSensitivity;
import com.opengamma.analytics.financial.provider.sensitivity.inflation.MultipleCurrencyInflationSensitivity;
import com.opengamma.analytics.financial.provider.sensitivity.multicurve.MulticurveSensitivity;
import com.opengamma.analytics.financial.provider.sensitivity.multicurve.MultipleCurrencyMulticurveSensitivity;
import com.opengamma.analytics.financial.provider.sensitivity.multicurve.MultipleCurrencyParameterSensitivity;
import com.opengamma.analytics.util.amount.ReferenceAmount;
import com.opengamma.engine.value.ValueProperties;
import com.opengamma.engine.value.ValueRequirementNames;
import com.opengamma.financial.analytics.cashflow.FixedPaymentMatrix;
import com.opengamma.financial.analytics.cashflow.FloatingPaymentMatrix;
import com.opengamma.timeseries.DoubleTimeSeries;
import com.opengamma.util.money.Currency;
import com.opengamma.util.money.CurrencyAmount;
import com.opengamma.util.money.MultipleCurrencyAmount;
import com.opengamma.util.tuple.DoublesPair;
import com.opengamma.util.tuple.Pair;

/**
 *
 */
public class SumUtils {

  public static Object addValue(final Object currentTotal, final Object value, final String valueName) {
    if (currentTotal == null) {
      return value;
    }
    if (currentTotal.getClass() != value.getClass()) {
      if (!(currentTotal.getClass() == MultipleCurrencyAmount.class && value.getClass() == CurrencyAmount.class)
          && !(currentTotal.getClass() == CurrencyAmount.class && value.getClass() == MultipleCurrencyAmount.class)) {
        throw new IllegalArgumentException("Inputs have different value types for requirement " + valueName + " currentTotal type = " + currentTotal.getClass() + " value type = " + value.getClass());
      }
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
    } else if (value instanceof TenorLabelledMatrix1D) {
      final TenorLabelledMatrix1D previousMatrix = (TenorLabelledMatrix1D) currentTotal;
      final TenorLabelledMatrix1D currentMatrix = (TenorLabelledMatrix1D) value;
      return previousMatrix.add(currentMatrix);
    } else if (value instanceof ZonedDateTimeLabelledMatrix1D) {
      final ZonedDateTimeLabelledMatrix1D previousMatrix = (ZonedDateTimeLabelledMatrix1D) currentTotal;
      final ZonedDateTimeLabelledMatrix1D currentMatrix = (ZonedDateTimeLabelledMatrix1D) value;
      return previousMatrix.add(currentMatrix);
    } else if (value instanceof CurrencyLabelledMatrix1D) {
      final CurrencyLabelledMatrix1D previousMatrix = (CurrencyLabelledMatrix1D) currentTotal;
      final CurrencyLabelledMatrix1D currentMatrix = (CurrencyLabelledMatrix1D) value;
      return previousMatrix.addIgnoringLabel(currentMatrix);
    } else if (value instanceof CurrencyAmount) {
      return calculateCurrencyAmount(currentTotal, (CurrencyAmount) value);
    } else if (value instanceof MultipleCurrencyAmount) {
      return calculateCurrencyAmount(currentTotal, (MultipleCurrencyAmount) value);
    } else if (value instanceof StringLabelledMatrix1D) {
      final StringLabelledMatrix1D previousMatrix = (StringLabelledMatrix1D) currentTotal;
      final StringLabelledMatrix1D currentMatrix = (StringLabelledMatrix1D) value;
      return previousMatrix.addIgnoringLabel(currentMatrix);
    } else if (valueName.equals(ValueRequirementNames.PRESENT_VALUE_CURVE_SENSITIVITY)) { //TODO this should probably not be done like this
      @SuppressWarnings("unchecked")
      final Map<String, List<DoublesPair>> previousMap = (Map<String, List<DoublesPair>>) currentTotal;
      @SuppressWarnings("unchecked")
      final Map<String, List<DoublesPair>> currentMap = (Map<String, List<DoublesPair>>) value;
      final Map<String, List<DoublesPair>> result = new HashMap<>();
      for (final String name : previousMap.keySet()) {
        final List<DoublesPair> temp = new ArrayList<>();
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
          final List<DoublesPair> temp = new ArrayList<>();
          for (final DoublesPair pair : currentMap.get(name)) {
            temp.add(pair);
          }
          result.put(name, temp);
        }
      }
      return result;
    } else if (value instanceof DoubleLabelledMatrix2D) {
      final DoubleLabelledMatrix2D previousMatrix = (DoubleLabelledMatrix2D) currentTotal;
      final DoubleLabelledMatrix2D currentMatrix = (DoubleLabelledMatrix2D) value;
      return previousMatrix.add(currentMatrix, 0.005, 0.005);
    } else if (value instanceof DoubleLabelledMatrix3D) {
      final DoubleLabelledMatrix3D previousMatrix = (DoubleLabelledMatrix3D) currentTotal;
      final DoubleLabelledMatrix3D currentMatrix = (DoubleLabelledMatrix3D) value;
      return previousMatrix.add(currentMatrix, 0.005, 0.005, 0.005);
    } else if (value instanceof FixedPaymentMatrix) {
      final FixedPaymentMatrix previousMatrix = (FixedPaymentMatrix) currentTotal;
      final FixedPaymentMatrix currentMatrix = (FixedPaymentMatrix) value;
      return previousMatrix.add(currentMatrix);
    } else if (value instanceof FloatingPaymentMatrix) {
      final FloatingPaymentMatrix previousMatrix = (FloatingPaymentMatrix) currentTotal;
      final FloatingPaymentMatrix currentMatrix = (FloatingPaymentMatrix) value;
      return previousMatrix.add(currentMatrix);
    } else if (value instanceof MulticurveSensitivity) {
      final MulticurveSensitivity previousSensitivity = (MulticurveSensitivity) currentTotal;
      final MulticurveSensitivity currentSensitivity = (MulticurveSensitivity) value;
      return previousSensitivity.plus(currentSensitivity);
    } else if (value instanceof MultipleCurrencyMulticurveSensitivity) {
      final MultipleCurrencyMulticurveSensitivity previousSensitivity = (MultipleCurrencyMulticurveSensitivity) currentTotal;
      final MultipleCurrencyMulticurveSensitivity currentSensitivity = (MultipleCurrencyMulticurveSensitivity) value;
      return previousSensitivity.plus(currentSensitivity);
    } else if (value instanceof MultipleCurrencyParameterSensitivity) {
      final MultipleCurrencyParameterSensitivity previousSensitivity = (MultipleCurrencyParameterSensitivity) currentTotal;
      final MultipleCurrencyParameterSensitivity currentSensitivity = (MultipleCurrencyParameterSensitivity) value;
      return previousSensitivity.plus(currentSensitivity);
    } else if (value instanceof InflationSensitivity) {
      final InflationSensitivity previousSensitivity = (InflationSensitivity) currentTotal;
      final InflationSensitivity currentSensitivity = (InflationSensitivity) value;
      return previousSensitivity.plus(currentSensitivity);
    } else if (value instanceof MultipleCurrencyInflationSensitivity) {
      final MultipleCurrencyInflationSensitivity previousSensitivity = (MultipleCurrencyInflationSensitivity) currentTotal;
      final MultipleCurrencyInflationSensitivity currentSensitivity = (MultipleCurrencyInflationSensitivity) value;
      return previousSensitivity.plus(currentSensitivity);
    } else if (valueName.equals(ALL_PV01S)) {
      @SuppressWarnings("unchecked")
      final Map<Pair<String, Currency>, Double> previousAmount = (Map<Pair<String, Currency>, Double>) currentTotal;
      final ReferenceAmount<Pair<String, Currency>> referenceAmount = new ReferenceAmount<>();
      for (final Map.Entry<Pair<String, Currency>, Double> entry : previousAmount.entrySet()) {
        referenceAmount.add(entry.getKey(), entry.getValue());
      }
      @SuppressWarnings("unchecked")
      final Map<Pair<String, Currency>, Double> currentAmount = (Map<Pair<String, Currency>, Double>) value;
      for (final Map.Entry<Pair<String, Currency>, Double> entry : currentAmount.entrySet()) {
        referenceAmount.add(entry.getKey(), entry.getValue());
      }
      return referenceAmount.getMap();
    } else {
      throw new IllegalArgumentException("Cannot sum results of type " + value.getClass());
    }
  }

  private static Object calculateCurrencyAmount(final Object currentTotal, final CurrencyAmount currentAmount) {

    // if we have a currency amount and the requested addition is the same currency then we add to it
    // If we have a multiple currency amount we use it,
    // Otherwise we create a new MultipleCurrencyAmount
    if (currentTotal instanceof CurrencyAmount) {

      final CurrencyAmount total = (CurrencyAmount) currentTotal;
      if (total.getCurrency() == currentAmount.getCurrency()) {
        return total.plus(currentAmount);
      } else {
        return MultipleCurrencyAmount.of(total).plus(currentAmount);
      }
    } else if (currentTotal instanceof MultipleCurrencyAmount) {
      return ((MultipleCurrencyAmount) currentTotal).plus(currentAmount);
    } else {
      throw new IllegalArgumentException("Expected current total to be of type " + CurrencyAmount.class +
          " or " + MultipleCurrencyAmount.class + " but was: " + currentTotal.getClass());
    }
  }

  private static Object calculateCurrencyAmount(final Object currentTotal, final MultipleCurrencyAmount currentAmount) {

    // if we have a currency amount and the requested addition is the same currency then we add to it
    // If we have a multiple currency amount we use it,
    // Otherwise we create a new MultipleCurrencyAmount
    if (currentTotal instanceof CurrencyAmount) {
      return currentAmount.plus((CurrencyAmount) currentTotal);
    } else if (currentTotal instanceof MultipleCurrencyAmount) {
      return ((MultipleCurrencyAmount) currentTotal).plus(currentAmount);
    } else {
      throw new IllegalArgumentException("Expected current total to be of type " + CurrencyAmount.class +
          " or " + MultipleCurrencyAmount.class + " but was: " + currentTotal.getClass());
    }
  }

  /**
   * Gets the intersection of two sets of properties.
   *
   * @param currentIntersection The current intersection of the properties
   * @param properties The new set of properties
   * @return The intersection of the two sets of properties
   */
  public static ValueProperties addProperties(final ValueProperties currentIntersection, final ValueProperties properties) {
    if (currentIntersection == null) {
      return properties;
    }
    return currentIntersection.intersect(properties);
  }

}
