/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.forex;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.ArrayUtils;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.function.FunctionInputs;
import com.opengamma.engine.value.ComputedValue;
import com.opengamma.engine.value.ValueProperties;
import com.opengamma.engine.value.ValueRequirementNames;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.financial.analytics.DoubleLabelledMatrix2D;
import com.opengamma.financial.forex.calculator.PresentValueVolatilitySensitivityBlackForexCalculator;
import com.opengamma.financial.forex.method.PresentValueForexBlackVolatilitySensitivity;
import com.opengamma.financial.interestrate.InstrumentDerivative;
import com.opengamma.financial.model.option.definition.SmileDeltaTermStructureDataBundle;
import com.opengamma.util.money.Currency;
import com.opengamma.util.tuple.DoublesPair;
import com.opengamma.util.tuple.Pair;

/**
 * 
 */
public class ForexOptionPresentValueVolatilitySensitivityFunction extends ForexOptionFunction {
  private static final Double[] EMPTY_ARRAY = ArrayUtils.EMPTY_DOUBLE_OBJECT_ARRAY;
  private static final PresentValueVolatilitySensitivityBlackForexCalculator CALCULATOR = PresentValueVolatilitySensitivityBlackForexCalculator.getInstance();
  private static final DecimalFormat TIME_FORMATTER = new DecimalFormat("##.###");
  private static final DecimalFormat STRIKE_FORMATTER = new DecimalFormat("###.#####");

  @Override
  protected Set<ComputedValue> getResult(final InstrumentDerivative fxOption, final SmileDeltaTermStructureDataBundle data, final FunctionInputs inputs, final ComputationTarget target,
      final String putFundingCurveName, final String putForwardCurveName, final String callFundingCurveName, final String callForwardCurveName, final String surfaceName) {
    final PresentValueForexBlackVolatilitySensitivity result = CALCULATOR.visit(fxOption, data);
    final Map<DoublesPair, Double> vega = result.getVega().getMap();
    final List<Double> rowValue = new ArrayList<Double>();
    final List<String> rowLabel = new ArrayList<String>();
    final List<Double> columnValue = new ArrayList<Double>();
    final List<String> columnLabel = new ArrayList<String>();
    final double[][] values = new double[1][1];
    for (final Map.Entry<DoublesPair, Double> entry : vega.entrySet()) {
      if (columnValue.contains(entry.getKey().first)) {
        throw new OpenGammaRuntimeException("Should only have one vega for each time");
      }
      final double t = entry.getKey().first;
      columnValue.add(t);
      columnLabel.add(getFormattedTime(t));
      if (rowValue.contains(entry.getKey().second)) {
        throw new OpenGammaRuntimeException("Should only have one vega for each strike");
      }
      final double k = entry.getKey().second;
      rowValue.add(k);
      rowLabel.add(getFormattedStrike(k, result.getCurrencyPair()));
      values[0][0] = entry.getValue();
    }
    final ValueProperties.Builder properties = getResultProperties(putFundingCurveName, putForwardCurveName, callFundingCurveName, callForwardCurveName, surfaceName, target);
    final ValueSpecification spec = new ValueSpecification(getValueRequirementName(), target.toSpecification(), properties.get());
    return Collections
        .singleton(new ComputedValue(spec, new DoubleLabelledMatrix2D(columnValue.toArray(EMPTY_ARRAY), columnLabel.toArray(), rowValue.toArray(EMPTY_ARRAY), rowLabel.toArray(), values)));
  }

  @Override
  protected String getValueRequirementName() {
    return ValueRequirementNames.FX_VOLATILITY_SENSITIVITIES;
  }

  @Override
  protected ValueProperties.Builder getResultProperties(final ComputationTarget target) {
    return createValueProperties()
        .withAny(PROPERTY_PUT_FUNDING_CURVE_NAME)
        .withAny(PROPERTY_PUT_FORWARD_CURVE_NAME)
        .withAny(PROPERTY_CALL_FUNDING_CURVE_NAME)
        .withAny(PROPERTY_CALL_FORWARD_CURVE_NAME)
        .withAny(PROPERTY_FX_VOLATILITY_SURFACE_NAME);
  }

  @Override
  protected ValueProperties.Builder getResultProperties(final String putFundingCurveName, final String putForwardCurveName, final String callFundingCurveName,
      final String callForwardCurveName, final String surfaceName, final ComputationTarget target) {
    return createValueProperties()
        .with(PROPERTY_PUT_FUNDING_CURVE_NAME, putFundingCurveName)
        .with(PROPERTY_PUT_FORWARD_CURVE_NAME, putForwardCurveName)
        .with(PROPERTY_CALL_FUNDING_CURVE_NAME, callFundingCurveName)
        .with(PROPERTY_CALL_FORWARD_CURVE_NAME, callForwardCurveName)
        .with(PROPERTY_FX_VOLATILITY_SURFACE_NAME, surfaceName);
  }

  private String getFormattedTime(final double t) {
    if (t < 1. / 52) {
      return TIME_FORMATTER.format((t * 365)) + "D";
    }
    if (t < 1. / 12) {
      return TIME_FORMATTER.format((t * 52)) + "W";
    }
    if (t < 1) {
      return TIME_FORMATTER.format((t * 12)) + "M";
    }
    return TIME_FORMATTER.format(t) + "Y";
  }

  private String getFormattedStrike(final double k, final Pair<Currency, Currency> pair) {
    return STRIKE_FORMATTER.format(k) + " " + pair.getFirst() + "/" + pair.getSecond();
  }
}
