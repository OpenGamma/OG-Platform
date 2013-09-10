/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.forex.option.black;

import java.text.DecimalFormat;
import java.util.Collections;
import java.util.Set;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.analytics.financial.forex.calculator.PresentValueBlackVolatilityNodeSensitivityBlackForexCalculator;
import com.opengamma.analytics.financial.forex.method.PresentValueForexBlackVolatilityNodeSensitivityDataBundle;
import com.opengamma.analytics.financial.interestrate.InstrumentDerivative;
import com.opengamma.analytics.financial.model.option.definition.ForexOptionDataBundle;
import com.opengamma.analytics.financial.model.option.definition.SmileDeltaTermStructureDataBundle;
import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.function.FunctionExecutionContext;
import com.opengamma.engine.function.FunctionInputs;
import com.opengamma.engine.value.ComputedValue;
import com.opengamma.engine.value.ValueProperties;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueRequirementNames;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.financial.analytics.DoubleLabelledMatrix2D;
import com.opengamma.financial.analytics.model.InstrumentTypeProperties;
import com.opengamma.financial.analytics.model.VegaMatrixUtils;
import com.opengamma.financial.analytics.model.black.BlackDiscountingVegaMatrixFXOptionFunction;
import com.opengamma.financial.currency.CurrencyPair;

/**
 * Calculates the bucketed vega matrix for FX options.
 * @deprecated Use {@link BlackDiscountingVegaMatrixFXOptionFunction}
 */
@Deprecated
public class FXOptionBlackVegaMatrixFunction extends FXOptionBlackSingleValuedFunction {
  private static final PresentValueBlackVolatilityNodeSensitivityBlackForexCalculator CALCULATOR = PresentValueBlackVolatilityNodeSensitivityBlackForexCalculator.getInstance();
  private static final DecimalFormat DELTA_FORMATTER = new DecimalFormat("##");

  public FXOptionBlackVegaMatrixFunction() {
    super(ValueRequirementNames.VEGA_MATRIX);
  }

  @Override
  protected Set<ComputedValue> getResult(final InstrumentDerivative forex, final ForexOptionDataBundle<?> data, final ComputationTarget target,
      final Set<ValueRequirement> desiredValues, final FunctionInputs inputs, final ValueSpecification spec, final FunctionExecutionContext executionContext) {
    if (data instanceof SmileDeltaTermStructureDataBundle) {
      final PresentValueForexBlackVolatilityNodeSensitivityDataBundle result = forex.accept(CALCULATOR, (SmileDeltaTermStructureDataBundle) data);
      final double[] expiries = result.getExpiries().getData();
      final double[] delta = result.getDelta().getData();
      final double[][] vega = result.getVega().getData();
      final int nDelta = delta.length;
      final int nExpiries = expiries.length;
      final Double[] rowValues = new Double[nExpiries];
      final String[] rowLabels = new String[nExpiries];
      final Double[] columnValues = new Double[nDelta];
      final String[] columnLabels = new String[nDelta];
      final double[][] values = new double[nDelta][nExpiries];
      for (int i = 0; i < nDelta; i++) {
        columnValues[i] = delta[i];
        columnLabels[i] = "P" + DELTA_FORMATTER.format(delta[i] * 100) + " " + result.getCurrencyPair().getFirst() + "/" + result.getCurrencyPair().getSecond();
        for (int j = 0; j < nExpiries; j++) {
          if (i == 0) {
            rowValues[j] = expiries[j];
            rowLabels[j] = VegaMatrixUtils.getFXVolatilityFormattedExpiry(expiries[j]);
          }
          values[i][j] = vega[j][i];
        }
      }
      return Collections.singleton(new ComputedValue(spec, new DoubleLabelledMatrix2D(rowValues, rowLabels, columnValues, columnLabels, values)));
    }
    throw new OpenGammaRuntimeException("Can only calculate vega matrix for surfaces with smiles");
  }

  @Override
  protected ValueProperties.Builder getResultProperties(final ComputationTarget target) {
    final ValueProperties.Builder properties = super.getResultProperties(target);
    properties.with(InstrumentTypeProperties.PROPERTY_SURFACE_INSTRUMENT_TYPE, InstrumentTypeProperties.FOREX);
    return properties;
  }

  @Override
  protected ValueProperties.Builder getResultProperties(final ComputationTarget target, final String putCurve, final String putCurveCalculationConfig,
      final String callCurve, final String callCurveCalculationConfig, final CurrencyPair baseQuotePair, final ValueProperties optionalProperties) {
    final ValueProperties.Builder properties = super.getResultProperties(target, putCurve, putCurveCalculationConfig, callCurve, callCurveCalculationConfig, baseQuotePair,
        optionalProperties);
    properties.with(InstrumentTypeProperties.PROPERTY_SURFACE_INSTRUMENT_TYPE, InstrumentTypeProperties.FOREX);
    return properties;
  }

  @Override
  protected ValueProperties.Builder getResultProperties(final ComputationTarget target, final ValueRequirement desiredValue, final CurrencyPair baseQuotePair) {
    final ValueProperties.Builder properties = super.getResultProperties(target, desiredValue, baseQuotePair);
    properties.with(InstrumentTypeProperties.PROPERTY_SURFACE_INSTRUMENT_TYPE, InstrumentTypeProperties.FOREX);
    return properties;
  }
}
