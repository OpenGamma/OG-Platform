/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.equity.indexoption;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.Validate;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.analytics.financial.equity.StaticReplicationDataBundle;
import com.opengamma.analytics.financial.equity.option.EquityIndexOption;
import com.opengamma.analytics.financial.equity.option.EquityIndexOptionPresentValueCalculator;
import com.opengamma.analytics.financial.model.volatility.smile.fitting.interpolation.GeneralSmileInterpolator;
import com.opengamma.analytics.financial.model.volatility.smile.fitting.interpolation.SurfaceArrayUtils;
import com.opengamma.analytics.financial.model.volatility.smile.fitting.sabr.SmileSurfaceDataBundle;
import com.opengamma.analytics.financial.model.volatility.surface.BlackVolatilitySurfaceMoneynessFcnBackedByGrid;
import com.opengamma.analytics.financial.model.volatility.surface.VolatilitySurfaceInterpolator;
import com.opengamma.analytics.math.function.Function1D;
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
import com.opengamma.financial.security.option.EquityBarrierOptionSecurity;
import com.opengamma.util.tuple.Triple;

/**
 *
 */
public class EquityIndexVanillaBarrierOptionVegaMatrixFunction extends EquityIndexVanillaBarrierOptionFunction {

  private static final EquityIndexOptionPresentValueCalculator PVC = EquityIndexOptionPresentValueCalculator.getInstance(); // Vanilla PV Calculator
  private static final double SHIFT = 0.0001; // FIXME This really should be configurable by the user!

  public EquityIndexVanillaBarrierOptionVegaMatrixFunction() {
    super(ValueRequirementNames.VEGA_QUOTE_MATRIX);
  }

  @Override
  protected Object computeValues(final Set<EquityIndexOption> vanillaOptions, final StaticReplicationDataBundle market) {
    final NodalDoublesSurface vegaSurface;
    if (market.getVolatilitySurface() instanceof BlackVolatilitySurfaceMoneynessFcnBackedByGrid) {
      // unpack the market data, including the interpolators
      final BlackVolatilitySurfaceMoneynessFcnBackedByGrid surfaceBundle = (BlackVolatilitySurfaceMoneynessFcnBackedByGrid) market.getVolatilitySurface();
      final VolatilitySurfaceInterpolator surfaceInterpolator = surfaceBundle.getInterpolator();
      final GeneralSmileInterpolator strikeInterpolator = surfaceInterpolator.getSmileInterpolator();
      final SmileSurfaceDataBundle volGrid = surfaceBundle.getGridData();
      final double[] forwards = volGrid.getForwards();
      final double[] expiries = volGrid.getExpiries();
      final int nExpiries = volGrid.getNumExpiries();
      final double optionExpiry = vanillaOptions.iterator().next().getTimeToExpiry();
      final double[][] strikes = volGrid.getStrikes();
      final double[][] vols = volGrid.getVolatilities();

      // Prices of vanillas in base scenario
      final int nVanillas = vanillaOptions.size();
      final EquityIndexOption[] vanillas = vanillaOptions.toArray(new EquityIndexOption[nVanillas]);
      final Double[] basePrices = new Double[nVanillas];
      for (int v = 0; v < nVanillas; v++) {
        basePrices[v] = PVC.visitEquityIndexOption(vanillas[v], market);
      }

      // Smile fits across strikes in base scenario, one per expiry
      final Function1D<Double, Double>[] smileFitsBase = surfaceInterpolator.getIndependentSmileFits(volGrid);

      // Bump market at each expiry and strike scenario
      // In each scenario, reprice each of the underlying vanillaOptions
      // NOTE: Only computing down-shift as this appears to produce more stable risk, and is faster
      final List<Triple<Double, Double, Double>> triplesExpiryStrikeVega = new ArrayList<Triple<Double, Double, Double>>();
      final int expiryIndex = SurfaceArrayUtils.getLowerBoundIndex(expiries, optionExpiry);
      for (int t = Math.max(0, expiryIndex - 3); t < Math.min(nExpiries, expiryIndex + 4); t++) {
        final int nStrikes = strikes[t].length;
        int idxLow = SurfaceArrayUtils.getLowerBoundIndex(strikes[t], vanillas[0].getStrike());
        int idxHigh = idxLow;
        for (int v = 1; v < nVanillas; v++) {
          final int idxV = SurfaceArrayUtils.getLowerBoundIndex(strikes[t], vanillas[v].getStrike());
          idxLow = Math.min(idxLow, idxV);
          idxHigh = Math.max(idxHigh, idxV);
        }

        for (int k = Math.max(0, idxLow - 6); k < Math.min(nStrikes, idxHigh + 16); k++) {
          // Scenario (t,k)
          // TODO: REVIEW Each scenario only requires a single new smile fit in k. We only recompute the smile function for the expiry we are bumping..
          final double[] bumpedVols = Arrays.copyOf(vols[t], nStrikes);
          bumpedVols[k] = vols[t][k] - SHIFT;
          final Function1D<Double, Double> thisExpirysSmile = strikeInterpolator.getVolatilityFunction(forwards[t], strikes[t], expiries[t], bumpedVols);
          final Function1D<Double, Double>[] scenarioSmileFits = Arrays.copyOf(smileFitsBase, smileFitsBase.length);
          scenarioSmileFits[t] = thisExpirysSmile;
          final BlackVolatilitySurfaceMoneynessFcnBackedByGrid shiftedSurface = surfaceInterpolator.combineIndependentSmileFits(scenarioSmileFits, volGrid);
          final StaticReplicationDataBundle shiftedMarket = market.withShiftedSurface(shiftedSurface);
          // Sensitivities
          for (int v = 0; v < nVanillas; v++) {
            final Double shiftedPV = PVC.visit(vanillas[v], shiftedMarket);
            Validate.notNull(shiftedPV, "Null PV in shifted scenario, T = " + expiries[t] + ", k = " + strikes[t][k]);
            final Double vega = (shiftedPV - basePrices[v]) / -SHIFT;
            final Triple<Double, Double, Double> xyz = new Triple<Double, Double, Double>(expiries[t], strikes[t][k], vega);
            triplesExpiryStrikeVega.add(xyz);
          }
        }
      }
      vegaSurface = NodalDoublesSurface.from(triplesExpiryStrikeVega);

      // Repackage into DoubleLabelledMatrix2D
      // Find unique set of expiries,
      final Double[] uniqueX = ArrayUtils.toObject(expiries);
      // and strikes
      final Set<Double> strikeSet = new HashSet<Double>();
      for (final double[] strike : strikes) {
        strikeSet.addAll(Arrays.asList(ArrayUtils.toObject(strike)));
      }
      final Double[] uniqueY = strikeSet.toArray(new Double[0]);
      // Fill matrix with values, zero where no vega is available
      final double[][] values = new double[uniqueY.length][uniqueX.length];
      int i = 0;
      for (final Double x : uniqueX) {
        int j = 0;
        for (final Double y : uniqueY) {
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
      final DoubleLabelledMatrix2D vegaMatrix = new DoubleLabelledMatrix2D(uniqueX, uniqueY, values);
      return vegaMatrix;


    }
    throw new OpenGammaRuntimeException("Currently will only accept a VolatilitySurface of type: BlackVolatilitySurfaceMoneynessFcnBackedByGrid");
  }

  @Override
  /* The VegaMatrixFunction advertises the particular underlying Bloomberg ticker that it applies to. The target must share this underlying. */
  public Set<ValueSpecification> getResults(final FunctionCompilationContext context, final ComputationTarget target) {

    final String bbgTicker = getBloombergTicker(OpenGammaCompilationContext.getHistoricalTimeSeriesSource(context), ((EquityBarrierOptionSecurity) target.getSecurity()).getUnderlyingId());
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
    final String bbgTicker = getBloombergTicker(tsSource, getEquityBarrierOptionSecurity(target).getUnderlyingId());
    final Builder propsBuilder =  super.createValueProperties(target, desiredValue, executionContext)
      .with(ValuePropertyNames.UNDERLYING_TICKER, bbgTicker);
    return propsBuilder;
  }

}
