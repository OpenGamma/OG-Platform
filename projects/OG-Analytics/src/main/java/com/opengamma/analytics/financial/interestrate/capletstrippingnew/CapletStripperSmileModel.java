/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate.capletstrippingnew;

import java.util.Arrays;

import org.apache.commons.lang.NotImplementedException;

import com.opengamma.analytics.financial.model.interestrate.curve.ForwardCurve;
import com.opengamma.analytics.financial.model.volatility.smile.function.SmileModelData;
import com.opengamma.analytics.financial.model.volatility.smile.function.VolatilityFunctionProvider;
import com.opengamma.analytics.math.curve.InterpolatedDoublesCurve;
import com.opengamma.analytics.math.interpolation.CombinedInterpolatorExtrapolatorFactory;
import com.opengamma.analytics.math.interpolation.Interpolator1DFactory;
import com.opengamma.analytics.math.matrix.DoubleMatrix1D;
import com.opengamma.util.ArgumentChecker;

/**
 * 
 */
public class CapletStripperSmileModel<T extends SmileModelData> implements CapletStripper {

  private final CapletStrippingImp _imp;

  public CapletStripperSmileModel(final MultiCapFloorPricer pricer, final VolatilityFunctionProvider<T> volProvider, final VectorFunction modelToSmileModelMap) {
    ArgumentChecker.notNull(pricer, "pricer");
    ArgumentChecker.notNull(volProvider, "volProvider");
    ArgumentChecker.notNull(modelToSmileModelMap, "modelToSmileModelMap");
    final double[] capletExpiries = pricer.getCapletExpiries();
    final int nCapletExp = capletExpiries.length;
    final int nSmileModelParamters = volProvider.getNumberOfParameters();
    ArgumentChecker.isTrue(modelToSmileModelMap.getSizeOfRange() == nCapletExp * nSmileModelParamters,
        "modelToSmileModelMap should have a range of {}, to provide {} (smile model) paramters for {} expiries", modelToSmileModelMap.getSizeOfRange(), nSmileModelParamters, nCapletExp);

    final double[] fwds = pricer.getCapletForwardRates();
    //TODO seams unnecessary to build an  interpolated curve that will only be hit at the knots 
    final ForwardCurve fwdCurve = new ForwardCurve(InterpolatedDoublesCurve.from(capletExpiries, fwds,
        CombinedInterpolatorExtrapolatorFactory.getInterpolator(Interpolator1DFactory.LINEAR, Interpolator1DFactory.FLAT_EXTRAPOLATOR)));

    final DiscreteVolatilityFunctionProvider dvfp = new ParameterizedSmileModelDiscreateVolatilityFunctionProvider<T>(volProvider, fwdCurve, modelToSmileModelMap);
    _imp = new CapletStrippingImp(pricer, dvfp);
  }

  @Override
  public CapletStrippingResult solve(final double[] marketValues, final MarketDataType type) {
    throw new NotImplementedException("must provide a guess");
  }

  @Override
  public CapletStrippingResult solve(final double[] marketValues, final MarketDataType type, final double[] errors) {
    throw new NotImplementedException("must provide a guess");
  }

  @Override
  public CapletStrippingResult solve(final double[] marketValues, final MarketDataType type, final DoubleMatrix1D guess) {
    ArgumentChecker.notNull(marketValues, "marketValues");
    final int n = marketValues.length;
    final double[] errors = new double[n];
    Arrays.fill(errors, 1.0);
    return solve(marketValues, type, errors, guess);
  }

  @Override
  public CapletStrippingResult solve(final double[] marketValues, final MarketDataType type, final double[] errors, final DoubleMatrix1D guess) {
    if (type == MarketDataType.PRICE) {
      return _imp.solveForCapPrices(marketValues, errors, guess);
    } else if (type == MarketDataType.VOL) {
      return _imp.solveForCapVols(marketValues, errors, guess);
    }
    throw new IllegalArgumentException("Unknown MarketDataType " + type.toString());
  }

}
