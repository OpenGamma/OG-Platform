/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.equity.varianceswap;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import com.opengamma.analytics.financial.equity.EquityDerivativeSensitivityCalculator;
import com.opengamma.analytics.financial.equity.StaticReplicationDataBundle;
import com.opengamma.analytics.financial.equity.variance.VarianceSwapPresentValueCalculator;
import com.opengamma.analytics.financial.varianceswap.VarianceSwap;
import com.opengamma.analytics.math.surface.NodalDoublesSurface;
import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.function.FunctionInputs;
import com.opengamma.engine.value.ComputedValue;
import com.opengamma.engine.value.ValueProperties;
import com.opengamma.engine.value.ValuePropertyNames;
import com.opengamma.engine.value.ValueRequirementNames;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.financial.analytics.DoubleLabelledMatrix2D;
import com.opengamma.financial.analytics.model.InstrumentTypeProperties;
import com.opengamma.financial.security.equity.EquityVarianceSwapSecurity;

/**
 *
 */
public class EquityVarianceSwapStaticReplicationVegaFunction extends EquityVarianceSwapStaticReplicationFunction {
  private static final EquityDerivativeSensitivityCalculator CALCULATOR = new EquityDerivativeSensitivityCalculator(VarianceSwapPresentValueCalculator.getInstance());

  public EquityVarianceSwapStaticReplicationVegaFunction() {
    super(ValueRequirementNames.VEGA_QUOTE_MATRIX);
  }

  @Override
  protected Set<ComputedValue> computeValues(final ValueSpecification resultSpec, final FunctionInputs inputs, final VarianceSwap derivative, final StaticReplicationDataBundle market) {
    final NodalDoublesSurface vegaSurface = CALCULATOR.calcBlackVegaForEntireSurface(derivative, market);
    final Double[] xValues = vegaSurface.getXData();
    final Double[] yValues = vegaSurface.getYData();
    final Set<Double> xSet = new HashSet<Double>(Arrays.asList(xValues));
    final Set<Double> ySet = new HashSet<Double>(Arrays.asList(yValues));
    final Double[] uniqueX = xSet.toArray(new Double[0]);
    final Double[] uniqueY = ySet.toArray(new Double[0]);
    final double[][] values = new double[ySet.size()][xSet.size()];
    int i = 0;
    for (final Double x : xSet) {
      int j = 0;
      for (final Double y : ySet) {
        double vega;
        try {
          vega = vegaSurface.getZValue(x, y);
        } catch (final IllegalArgumentException e) {
          vega = 0;
        }
        values[j++][i] = vega;
      }
      i++;
    }
    final DoubleLabelledMatrix2D matrix = new DoubleLabelledMatrix2D(uniqueX, uniqueY, values);
    return Collections.singleton(new ComputedValue(resultSpec, matrix));
  }

  @Override
  protected ValueSpecification getValueSpecification(final ComputationTarget target) {
    final EquityVarianceSwapSecurity security = (EquityVarianceSwapSecurity) target.getSecurity();
    final ValueProperties properties = createValueProperties()
        .with(ValuePropertyNames.CURRENCY, security.getCurrency().getCode())
        .withAny(ValuePropertyNames.CURVE)
        .withAny(ValuePropertyNames.CURVE_CALCULATION_CONFIG)
        .withAny(ValuePropertyNames.SURFACE)
        .with(InstrumentTypeProperties.PROPERTY_SURFACE_INSTRUMENT_TYPE, InstrumentTypeProperties.EQUITY_OPTION)
        .with(ValuePropertyNames.CALCULATION_METHOD, CALCULATION_METHOD)
        .get();
    return new ValueSpecification(ValueRequirementNames.VEGA_QUOTE_MATRIX, target.toSpecification(), properties);
  }

  @Override
  protected ValueSpecification getValueSpecification(final ComputationTarget target, final String curveName, final String curveCalculationConfig, final String surfaceName) {
    final EquityVarianceSwapSecurity security = (EquityVarianceSwapSecurity) target.getSecurity();
    final ValueProperties properties = createValueProperties()
        .with(ValuePropertyNames.CURRENCY, security.getCurrency().getCode())
        .with(ValuePropertyNames.CURVE, curveName)
        .with(ValuePropertyNames.CURVE_CALCULATION_CONFIG, curveCalculationConfig)
        .with(ValuePropertyNames.SURFACE, surfaceName)
        .with(InstrumentTypeProperties.PROPERTY_SURFACE_INSTRUMENT_TYPE, InstrumentTypeProperties.EQUITY_OPTION)
        .with(ValuePropertyNames.CALCULATION_METHOD, CALCULATION_METHOD)
        .get();
    return new ValueSpecification(ValueRequirementNames.VEGA_QUOTE_MATRIX, target.toSpecification(), properties);
  }
}
