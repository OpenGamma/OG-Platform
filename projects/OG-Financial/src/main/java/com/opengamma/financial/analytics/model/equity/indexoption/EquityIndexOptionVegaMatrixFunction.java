/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.equity.indexoption;

import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.lang.ArrayUtils;

import com.opengamma.analytics.financial.equity.DerivativeSensitivityCalculator;
import com.opengamma.analytics.financial.equity.StaticReplicationDataBundle;
import com.opengamma.analytics.financial.equity.option.EquityIndexOption;
import com.opengamma.analytics.financial.equity.option.EquityIndexOptionPresentValueCalculator;
import com.opengamma.analytics.financial.model.volatility.surface.BlackVolatilitySurfaceMoneynessFcnBackedByGrid;
import com.opengamma.analytics.math.surface.NodalDoublesSurface;
import com.opengamma.core.historicaltimeseries.HistoricalTimeSeriesSource;
import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.function.FunctionCompilationContext;
import com.opengamma.engine.function.FunctionExecutionContext;
import com.opengamma.engine.value.ValueProperties;
import com.opengamma.engine.value.ValueProperties.Builder;
import com.opengamma.engine.value.ValuePropertyNames;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueRequirementNames;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.financial.OpenGammaCompilationContext;
import com.opengamma.financial.OpenGammaExecutionContext;
import com.opengamma.financial.analytics.DoubleLabelledMatrix2D;
import com.opengamma.financial.analytics.model.VegaMatrixHelper;
import com.opengamma.financial.security.option.EquityIndexOptionSecurity;

/**
 * TODO: REVIEW- These options, if priced under Black, fail as the vol surface is functional => No X-Y nodes. It seems desirable to be able to establish where the risk is, so that we can sum it up.
 * We can rework so that the requirement is VALUE_VEGA and the Initial Vol Matrix, which still has X-Y nodes, and each option shows risk only in itself.
 * UPDATE: What has been done is to bundle the Interpolator and the Initial Vol Matrix into the BlackVolSurface as extended class BlackVolatilitySurfaceMoneynessFcnBackedByGrid...
 */
public class EquityIndexOptionVegaMatrixFunction  extends EquityIndexOptionFunction {
  private static final EquityIndexOptionPresentValueCalculator PVC = EquityIndexOptionPresentValueCalculator.getInstance();
  private static final DerivativeSensitivityCalculator CALCULATOR = new DerivativeSensitivityCalculator(PVC);
  private static final double SHIFT = 0.0001; // FIXME This really should be configurable by the user!

  public EquityIndexOptionVegaMatrixFunction() {
    super(ValueRequirementNames.VEGA_QUOTE_MATRIX);
  }

  @Override
  protected Object computeValues(final EquityIndexOption derivative, final StaticReplicationDataBundle market) {
    final NodalDoublesSurface vegaSurface = CALCULATOR.calcBlackVegaForEntireSurface(derivative, market, SHIFT);
    final Double[] xValues;
    final Double[] yValues;
    if (market.getVolatilitySurface() instanceof BlackVolatilitySurfaceMoneynessFcnBackedByGrid) {
      final BlackVolatilitySurfaceMoneynessFcnBackedByGrid volDataBundle = (BlackVolatilitySurfaceMoneynessFcnBackedByGrid) market.getVolatilitySurface();
      xValues = ArrayUtils.toObject(volDataBundle.getGridData().getExpiries());
      final double[][] strikes2d = volDataBundle.getGridData().getStrikes();
      final Set<Double> strikeSet = new HashSet<Double>();
      for (final double[] element : strikes2d) {
        strikeSet.addAll(Arrays.asList(ArrayUtils.toObject(element)));
      }
      yValues = strikeSet.toArray(new Double[] {});
    } else {
      xValues = vegaSurface.getXData();
      yValues = vegaSurface.getYData();
    }

    final Set<Double> xSet = new HashSet<Double>(Arrays.asList(xValues));
    final Set<Double> ySet = new HashSet<Double>(Arrays.asList(yValues));
    final Double[] uniqueX = xSet.toArray(new Double[0]);
    final String[] expLabels = new String[uniqueX.length];
    // Format the expiries for display
    for (int i = 0; i < uniqueX.length; i++) {
      uniqueX[i] = roundTwoDecimals(uniqueX[i]);
      expLabels[i] = VegaMatrixHelper.getFXVolatilityFormattedExpiry(uniqueX[i]);
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
    return matrix;
  }

  private double roundTwoDecimals(final double d) {
    final DecimalFormat twoDForm = new DecimalFormat("#.##");
    return Double.valueOf(twoDForm.format(d));
  }

  @Override
  /* The VegaMatrixFunction advertises the particular underlying Bloomberg ticker that it applies to. The target must share this underlying. */
  public Set<ValueSpecification> getResults(final FunctionCompilationContext context, final ComputationTarget target) {

    final String bbgTicker = getBloombergTicker(OpenGammaCompilationContext.getHistoricalTimeSeriesSource(context), ((EquityIndexOptionSecurity) target.getSecurity()).getUnderlyingId());
    return Collections.singleton(new ValueSpecification(getValueRequirementName(), target.toSpecification(), createValueProperties(target, bbgTicker).get()));
  }


  /* We specify one additional property, the UnderlyingTicker, to allow a View to contain a VegaQuoteMatrix for each VolMatrix */
  protected ValueProperties.Builder createValueProperties(final ComputationTarget target, final String bbgTicker) {
    return super.createValueProperties(target)
      .with(ValuePropertyNames.UNDERLYING_TICKER, bbgTicker);
  }

  @Override
  protected ValueProperties.Builder createValueProperties(final ComputationTarget target, final ValueRequirement desiredValue, final FunctionExecutionContext executionContext) {
    final HistoricalTimeSeriesSource tsSource = OpenGammaExecutionContext.getHistoricalTimeSeriesSource(executionContext);
    final String bbgTicker = getBloombergTicker(tsSource, getEquityIndexOptionSecurity(target).getUnderlyingId());
    final Builder propsBuilder =  super.createValueProperties(target, desiredValue, executionContext)
      .with(ValuePropertyNames.UNDERLYING_TICKER, bbgTicker);
    return propsBuilder;
  }
}

