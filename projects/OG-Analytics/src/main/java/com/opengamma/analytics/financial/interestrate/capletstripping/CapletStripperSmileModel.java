/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate.capletstripping;

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
 * Smile model based caplet stripper. The parameters of the smile model (e.g. SABR, Heston, SVI) are themselves 
 * represented as parameterised term structures (one for each smile model parameter), and this collection of parameters
 * are the <i>model parameters</i>. For a particular caplet, we can find the smile model parameters at its expiry, 
 * then (using the smile model) find the (Black) volatility at its strike; hence the model parameters describe a caplet
 * volatility surface. <p>
 * For a set of market cap values, we can find, in a least-square sense, the optimal set of model parameters to reproduce 
 * the market values. Since the smiles are smooth functions of a few (4-5) parameters, it is generally not possible to 
 * recover exactly market values using this method.
 * @param <T> The type of smile model data 
 */
public class CapletStripperSmileModel<T extends SmileModelData> implements CapletStripper {

  private final CapletStrippingImp _imp;

  /**
   * Set up the stripper. 
   * @param pricer The pricer (which contained the details of the market values of the caps/floors)
   * @param volProvider The smile model 
   * @param modelToSmileModelMap mapping from the model parameters to the smile model parameters at each caplet expiry (from the
   * decomposed caps in pricer). This gives a lot of flexibility as to how the (smile model) parameter term structures
   * are represented; the only constraint is that the smile model parameters must be order as - 1st parameter at each
   * (caplet) expiry, then 2nd parameter at each expiry etc
   */
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
