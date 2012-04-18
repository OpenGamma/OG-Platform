/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.model.volatility.local;

import com.opengamma.analytics.financial.model.finitedifference.PDEGrid1D;
import com.opengamma.analytics.financial.model.finitedifference.PDETerminalResults1D;
import com.opengamma.analytics.financial.model.interestrate.curve.ForwardCurve;
import com.opengamma.analytics.financial.model.option.pricing.analytic.formula.EuropeanVanillaOption;
import com.opengamma.analytics.math.interpolation.Interpolator1D;
import com.opengamma.analytics.math.interpolation.data.Interpolator1DDataBundle;

/**
 * 
 */
public class LocalVolatilityBackwardPDEPriceGridCalculator {
  private final LocalVolatilityBackwardPDECalculator _pdeCalculator;
  private final Interpolator1D _interpolator;

  public LocalVolatilityBackwardPDEPriceGridCalculator(final double theta, final int nTimeSteps, final int nYSteps, final double timeMeshLambda, final double yMeshBunching,
      final Interpolator1D interpolator) {
    _pdeCalculator = new LocalVolatilityBackwardPDECalculator(theta, nTimeSteps, nYSteps, timeMeshLambda, yMeshBunching);
    _interpolator = interpolator;
  }

  public Interpolator1DDataBundle getGridPrices(final LocalVolatilitySurfaceMoneyness localVolatility, final ForwardCurve forwardCurve, final EuropeanVanillaOption option) {
    final PDETerminalResults1D pdeGrid = _pdeCalculator.runPDESolver(localVolatility, forwardCurve, option);
    final PDEGrid1D grid = pdeGrid.getGrid();
    final double[] moneynesses = grid.getSpaceNodes();
    final double[] prices = pdeGrid.getFinalTimePrices();
    return _interpolator.getDataBundleFromSortedArrays(moneynesses, prices); //TODO
  }

}
