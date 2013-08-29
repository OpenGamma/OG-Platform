/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.equity.option;

import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.ArrayUtils;

import com.google.common.collect.Sets;
import com.opengamma.analytics.financial.equity.EquityDerivativeSensitivityCalculator;
import com.opengamma.analytics.financial.equity.EquityOptionBlackPresentValueCalculator;
import com.opengamma.analytics.financial.equity.StaticReplicationDataBundle;
import com.opengamma.analytics.financial.interestrate.InstrumentDerivative;
import com.opengamma.analytics.financial.model.volatility.surface.BlackVolatilitySurfaceMoneynessFcnBackedByGrid;
import com.opengamma.analytics.math.surface.NodalDoublesSurface;
import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.ComputationTargetSpecification;
import com.opengamma.engine.function.FunctionCompilationContext;
import com.opengamma.engine.function.FunctionInputs;
import com.opengamma.engine.value.ComputedValue;
import com.opengamma.engine.value.ValueProperties;
import com.opengamma.engine.value.ValuePropertyNames;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueRequirementNames;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.financial.analytics.DoubleLabelledMatrix2D;
import com.opengamma.financial.analytics.model.InstrumentTypeProperties;
import com.opengamma.financial.analytics.model.VegaMatrixUtils;
import com.opengamma.financial.analytics.model.equity.EquitySecurityUtils;
import com.opengamma.financial.security.FinancialSecurity;

/**
 * Calculates the bucketed vega of an equity index or equity option using the Black formula.
 */
public class EquityOptionBlackVegaMatrixFunction extends EquityOptionBlackFunction {
  /** The Black present value calculator */
  private static final EquityOptionBlackPresentValueCalculator PVC = EquityOptionBlackPresentValueCalculator.getInstance();
  /** Calculates derivative sensitivities */
  private static final EquityDerivativeSensitivityCalculator CALCULATOR = new EquityDerivativeSensitivityCalculator(PVC);
  /** The format for the output */
  private static final DecimalFormat FORMATTER = new DecimalFormat("#.##");
  /** The shift to use in bumping */
  private static final double SHIFT = 0.0001; // FIXME This really should be configurable by the user!

  /**
   * Default constructor
   */
  public EquityOptionBlackVegaMatrixFunction() {
    super(ValueRequirementNames.VEGA_QUOTE_MATRIX);
  }

  @Override
  protected Set<ComputedValue> computeValues(final InstrumentDerivative derivative, final StaticReplicationDataBundle market, final FunctionInputs inputs,
      final Set<ValueRequirement> desiredValues, final ComputationTargetSpecification targetSpec, final ValueProperties resultProperties) {
    final ValueSpecification resultSpec = new ValueSpecification(getValueRequirementNames()[0], targetSpec, resultProperties);
    final NodalDoublesSurface vegaSurface = CALCULATOR.calcBlackVegaForEntireSurface(derivative, market, SHIFT);
    final Double[] xValues;
    final Double[] yValues;
    if (market.getVolatilitySurface() instanceof BlackVolatilitySurfaceMoneynessFcnBackedByGrid) {
      final BlackVolatilitySurfaceMoneynessFcnBackedByGrid volDataBundle = (BlackVolatilitySurfaceMoneynessFcnBackedByGrid) market.getVolatilitySurface();
      xValues = ArrayUtils.toObject(volDataBundle.getGridData().getExpiries());
      final double[][] strikes2d = volDataBundle.getGridData().getStrikes();
      final Set<Double> strikeSet = new HashSet<>();
      for (final double[] element : strikes2d) {
        strikeSet.addAll(Arrays.asList(ArrayUtils.toObject(element)));
      }
      yValues = strikeSet.toArray(new Double[] {});
    } else {
      xValues = vegaSurface.getXData();
      yValues = vegaSurface.getYData();
    }

    final Set<Double> xSet = new HashSet<>(Arrays.asList(xValues));
    final Set<Double> ySet = new HashSet<>(Arrays.asList(yValues));
    final Double[] uniqueX = xSet.toArray(new Double[0]);
    final String[] expLabels = new String[uniqueX.length];
    // Format the expiries for display
    for (int i = 0; i < uniqueX.length; i++) {
      uniqueX[i] = roundTwoDecimals(uniqueX[i]);
      expLabels[i] = VegaMatrixUtils.getFXVolatilityFormattedExpiry(uniqueX[i]);
    }
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
    final DoubleLabelledMatrix2D matrix = new DoubleLabelledMatrix2D(uniqueX, expLabels, uniqueY, uniqueY, values);
    return Collections.singleton(new ComputedValue(resultSpec, matrix));
  }

  private double roundTwoDecimals(final double d) {
    return Double.valueOf(FORMATTER.format(d));
  }

  @Override
  public Set<ValueSpecification> getResults(final FunctionCompilationContext context, final ComputationTarget target, final Map<ValueSpecification, ValueRequirement> inputs) {
    final Set<ValueSpecification> results = super.getResults(context, target, inputs);
    final FinancialSecurity security = (FinancialSecurity) target.getSecurity();
    final String bbgTicker = EquitySecurityUtils.getIndexOrEquityNameFromUnderlying(security);
    final Set<ValueSpecification> resultsWithExtraProperties = Sets.newHashSetWithExpectedSize(results.size());
    for (final ValueSpecification spec : results) {
      final String name = spec.getValueName();
      final ComputationTargetSpecification targetSpec = spec.getTargetSpecification();
      final ValueProperties properties = spec.getProperties().copy()
          .with(InstrumentTypeProperties.PROPERTY_SURFACE_INSTRUMENT_TYPE, InstrumentTypeProperties.EQUITY_OPTION)
          .with(ValuePropertyNames.UNDERLYING_TICKER, bbgTicker)
          .get();
      resultsWithExtraProperties.add(new ValueSpecification(name, targetSpec, properties));
    }
    return results;
  }

}
