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
import com.opengamma.engine.function.FunctionCompilationContext;
import com.opengamma.engine.function.FunctionInputs;
import com.opengamma.engine.value.ComputedValue;
import com.opengamma.engine.value.ValueProperties;
import com.opengamma.engine.value.ValuePropertyNames;
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
public class ForexSingleBarrierOptionPresentValueVolatilitySensitivityFunction extends ForexSingleBarrierOptionFunction {
  private static final Double[] EMPTY_ARRAY = ArrayUtils.EMPTY_DOUBLE_OBJECT_ARRAY;
  private static final PresentValueVolatilitySensitivityBlackForexCalculator CALCULATOR = PresentValueVolatilitySensitivityBlackForexCalculator.getInstance();
  private static final DecimalFormat TIME_FORMATTER = new DecimalFormat("##.###");
  private static final DecimalFormat STRIKE_FORMATTER = new DecimalFormat("###.#####");

  public ForexSingleBarrierOptionPresentValueVolatilitySensitivityFunction(final String putFundingCurveName, final String putForwardCurveName, final String callFundingCurveName,
      final String callForwardCurveName, final String surfaceName) {
    super(putFundingCurveName, putForwardCurveName, callFundingCurveName, callForwardCurveName, surfaceName);
  }

  @Override
  protected Set<ComputedValue> getResult(final InstrumentDerivative fxSingleBarrierOption, final SmileDeltaTermStructureDataBundle data, final FunctionInputs inputs, final ComputationTarget target) {
    final PresentValueForexBlackVolatilitySensitivity result = CALCULATOR.visit(fxSingleBarrierOption, data);
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
    final ValueProperties properties = createValueProperties().with(ValuePropertyNames.PAY_CURVE, getPutFundingCurveName(), getPutForwardCurveName())
        .with(ValuePropertyNames.RECEIVE_CURVE, getCallFundingCurveName(), getCallForwardCurveName()).with(ValuePropertyNames.SURFACE, getSurfaceName()).get();
    final ValueSpecification spec = new ValueSpecification(ValueRequirementNames.FX_VOLATILITY_SENSITIVITIES, target.toSpecification(), properties);
    return Collections
        .singleton(new ComputedValue(spec, new DoubleLabelledMatrix2D(columnValue.toArray(EMPTY_ARRAY), columnLabel.toArray(), rowValue.toArray(EMPTY_ARRAY), rowLabel.toArray(), values)));
  }

  @Override
  public Set<ValueSpecification> getResults(final FunctionCompilationContext context, final ComputationTarget target) {
    final ValueProperties properties = createValueProperties().with(ValuePropertyNames.PAY_CURVE, getPutFundingCurveName(), getPutForwardCurveName())
        .with(ValuePropertyNames.RECEIVE_CURVE, getCallFundingCurveName(), getCallForwardCurveName()).with(ValuePropertyNames.SURFACE, getSurfaceName()).get();
    return Collections.singleton(new ValueSpecification(ValueRequirementNames.FX_VOLATILITY_SENSITIVITIES, target.toSpecification(), properties));
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
