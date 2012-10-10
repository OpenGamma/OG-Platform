/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.equity.indexoption;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.lang.ArrayUtils;

import com.opengamma.analytics.financial.equity.DerivativeSensitivityCalculator;
import com.opengamma.analytics.financial.equity.StaticReplicationDataBundle;
import com.opengamma.analytics.financial.equity.option.EquityIndexOption;
import com.opengamma.analytics.financial.equity.option.EquityIndexOptionPresentValueCalculator;
import com.opengamma.analytics.financial.model.volatility.surface.BlackVolatilitySurfaceMoneynessFcnBackedByGrid;
import com.opengamma.analytics.math.surface.NodalDoublesSurface;
import com.opengamma.engine.value.ValueRequirementNames;
import com.opengamma.financial.analytics.DoubleLabelledMatrix2D;

/**
 * TODO: REVIEW- These options, if priced under Black, fail as the vol surface is functional => No X-Y nodes. It seems desirable to be able to establish where the risk is, so that we can sum it up.
 * We can rework so that the requirement is VALUE_VEGA and the Initial Vol Matrix, which still has X-Y nodes, and each option shows risk only in itself.
 * UPDATE: What has been done is to bundle the Interpolator and the Initial Vol Matrix into the BlackVolSurface as extended class BlackVolatilitySurfaceMoneynessFcnBackedByGrid...
 */
public class EquityIndexOptionVegaMatrixFunction extends EquityIndexOptionFunction {
  private static final EquityIndexOptionPresentValueCalculator PVC = EquityIndexOptionPresentValueCalculator.getInstance();
  private static final DerivativeSensitivityCalculator CALCULATOR = new DerivativeSensitivityCalculator(PVC);
  private static final double SHIFT = 0.0001; // FIXME This really should be configurable by the user!

  public EquityIndexOptionVegaMatrixFunction() {
    super(ValueRequirementNames.VEGA_QUOTE_MATRIX);
  }

  @Override
  protected Object computeValues(EquityIndexOption derivative, StaticReplicationDataBundle market) {
    final NodalDoublesSurface vegaSurface = CALCULATOR.calcBlackVegaForEntireSurface(derivative, market, SHIFT);
    final Double[] xValues;
    final Double[] yValues;
    if (market.getVolatilitySurface() instanceof BlackVolatilitySurfaceMoneynessFcnBackedByGrid) {
      BlackVolatilitySurfaceMoneynessFcnBackedByGrid volDataBundle = (BlackVolatilitySurfaceMoneynessFcnBackedByGrid) market.getVolatilitySurface();
      xValues = ArrayUtils.toObject(volDataBundle.getGridData().getExpiries());
      double[][] strikes2d = volDataBundle.getGridData().getStrikes();
      Set<Double> strikeSet = new HashSet<Double>();
      for (int i = 0; i < strikes2d.length; i++) {
        strikeSet.addAll(Arrays.asList(ArrayUtils.toObject(strikes2d[i])));
      }
      yValues = strikeSet.toArray(new Double[] {});
    } else {
      xValues = vegaSurface.getXData();
      yValues = vegaSurface.getYData();
    }

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
    return matrix;
  }
}
