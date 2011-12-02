/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.equity.variance;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.function.FunctionInputs;
import com.opengamma.engine.value.ComputedValue;
import com.opengamma.engine.value.ValueProperties;
import com.opengamma.engine.value.ValuePropertyNames;
import com.opengamma.engine.value.ValueRequirementNames;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.financial.analytics.DoubleLabelledMatrix2D;
import com.opengamma.financial.analytics.volatility.surface.RawVolatilitySurfaceDataFunction;
import com.opengamma.financial.equity.variance.VarianceSwapDataBundle;
import com.opengamma.financial.equity.variance.VarianceSwapRatesSensitivityCalculator;
import com.opengamma.financial.equity.variance.derivative.VarianceSwap;
import com.opengamma.financial.security.equity.EquityVarianceSwapSecurity;
import com.opengamma.math.surface.NodalDoublesSurface;

/**
 * 
 */
public class EquityVarianceSwapVegaFunction extends EquityVarianceSwapFunction {
  private static final VarianceSwapRatesSensitivityCalculator CALCULATOR = VarianceSwapRatesSensitivityCalculator.getInstance();
  
  public EquityVarianceSwapVegaFunction(String curveDefinitionName, String surfaceDefinitionName, String forwardCalculationMethod, String strikeParameterizationMethodName) {
    super(curveDefinitionName, surfaceDefinitionName, forwardCalculationMethod, strikeParameterizationMethodName);
  }

  @Override
  protected Set<ComputedValue> getResults(final ComputationTarget target, final FunctionInputs inputs, final VarianceSwap derivative, final VarianceSwapDataBundle market) {
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
        } catch (IllegalArgumentException e) {
          vega = 0;
        }
        values[j++][i] = vega;
      }
      i++;
    }
    final DoubleLabelledMatrix2D matrix = new DoubleLabelledMatrix2D(uniqueX, uniqueY, values);
    return Collections.singleton(new ComputedValue(getValueSpecification(target), matrix));
  }
  
  @Override
  protected ValueSpecification getValueSpecification(final ComputationTarget target) {
    final EquityVarianceSwapSecurity security = (EquityVarianceSwapSecurity) target.getSecurity();
    final ValueProperties properties = createValueProperties()
        .with(ValuePropertyNames.CURRENCY, security.getCurrency().getCode())
        .with(ValuePropertyNames.CURVE, getCurveDefinitionName())
        .with(ValuePropertyNames.SURFACE, getSurfaceName())
        .with(RawVolatilitySurfaceDataFunction.PROPERTY_SURFACE_INSTRUMENT_TYPE, "EQUITY_OPTION").get();
    return new ValueSpecification(ValueRequirementNames.VEGA_QUOTE_MATRIX, target.toSpecification(), properties);
  }

}
