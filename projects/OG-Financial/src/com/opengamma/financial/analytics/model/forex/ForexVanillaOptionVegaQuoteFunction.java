/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.forex;

import java.text.DecimalFormat;
import java.util.Collections;
import java.util.Set;

import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.function.FunctionInputs;
import com.opengamma.engine.value.ComputedValue;
import com.opengamma.engine.value.ValueProperties;
import com.opengamma.engine.value.ValuePropertyNames;
import com.opengamma.engine.value.ValueRequirementNames;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.financial.analytics.DoubleLabelledMatrix2D;
import com.opengamma.financial.forex.calculator.ForexDerivative;
import com.opengamma.financial.forex.calculator.PresentValueForexVegaQuoteSensitivityCalculator;
import com.opengamma.financial.forex.method.PresentValueVolatilityQuoteSensitivityDataBundle;
import com.opengamma.financial.model.option.definition.SmileDeltaTermStructureDataBundle;

/**
 * 
 */
public class ForexVanillaOptionVegaQuoteFunction extends ForexVanillaOptionFunction {

  private static final PresentValueForexVegaQuoteSensitivityCalculator CALCULATOR = PresentValueForexVegaQuoteSensitivityCalculator.getInstance();
  private static final DecimalFormat DELTA_FORMATTER = new DecimalFormat("##");

  public ForexVanillaOptionVegaQuoteFunction(final String putCurveName, final String callCurveName, final String surfaceName) {
    super(putCurveName, callCurveName, surfaceName, ValueRequirementNames.VEGA_MATRIX);
  }

  @Override
  protected Set<ComputedValue> getResult(final ForexDerivative fxOption, final SmileDeltaTermStructureDataBundle data, final FunctionInputs inputs, final ComputationTarget target) {
    final PresentValueVolatilityQuoteSensitivityDataBundle result = CALCULATOR.visit(fxOption, data);
    final double[] expiries = result.getExpiries();
    final double[] delta = result.getDelta();
    final double[][] vega = result.getVega();
    final int nDelta = delta.length;
    final int nExpiries = expiries.length;
    final Double[] rowValues = new Double[nExpiries];
    final String[] rowLabels = new String[nExpiries];
    final Double[] columnValues = new Double[2 * nDelta + 1];
    final String[] columnLabels = new String[2 * nDelta + 1];
    final double[][] values = new double[nDelta][nExpiries];
    columnLabels[0] = "ATM " + " " + result.getCurrencyPair().getFirst() + "/" + result.getCurrencyPair().getSecond();
    for (int i = 0; i < (nDelta - 1) / 2; i++) {
      columnLabels[1 + i] = "RR" + DELTA_FORMATTER.format(delta[i] * 100) + " " + result.getCurrencyPair().getFirst() + "/" + result.getCurrencyPair().getSecond();
      columnLabels[nDelta + 1 + i] = "Str" + DELTA_FORMATTER.format(delta[i] * 100) + " " + result.getCurrencyPair().getFirst() + "/" + result.getCurrencyPair().getSecond();
    }
    for (int j = 0; j < nExpiries; j++) {
      rowValues[j] = expiries[j];
      rowLabels[j] = getFormattedExpiry(expiries[j]);
    }
    for (int i = 0; i < nDelta; i++) {
      for (int j = 0; j < nExpiries; j++) {
        values[i][j] = vega[j][i];
      }
    }
    final ValueProperties properties = createValueProperties().with(ValuePropertyNames.PAY_CURVE, getPutCurveName()).with(ValuePropertyNames.RECEIVE_CURVE, getCallCurveName())
        .with(ValuePropertyNames.SURFACE, getSurfaceName()).get();
    final ValueSpecification spec = new ValueSpecification(getValueRequirementName(), target.toSpecification(), properties);
    return Collections.singleton(new ComputedValue(spec, new DoubleLabelledMatrix2D(rowValues, rowLabels, columnValues, columnLabels, values)));
  }

  private String getFormattedExpiry(final double expiry) {
    if (expiry < 1. / 54) {
      final int days = (int) Math.ceil((365 * expiry));
      return days + "D";
    }
    if (expiry < 1. / 13) {
      final int weeks = (int) Math.ceil((52 * expiry));
      return weeks + "W";
    }
    if (expiry < 0.95) {
      final int months = (int) Math.ceil((12 * expiry));
      return months + "M";
    }
    return ((int) Math.ceil(expiry)) + "Y";
  }
}
