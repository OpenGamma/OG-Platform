/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate.capletstripping;

import java.util.List;

import com.opengamma.analytics.financial.interestrate.payments.derivative.CapFloorIbor;
import com.opengamma.analytics.financial.interestrate.payments.provider.CapFloorIborInArrearsSmileModelCapGenericReplicationMethod;
import com.opengamma.analytics.financial.model.volatility.BlackFormulaRepository;
import com.opengamma.analytics.financial.model.volatility.SimpleOptionData;
import com.opengamma.analytics.financial.model.volatility.smile.fitting.interpolation.GeneralSmileInterpolator;
import com.opengamma.analytics.financial.model.volatility.smile.fitting.interpolation.InterpolatedSmileFunction;
import com.opengamma.analytics.financial.provider.calculator.discounting.ParRateDiscountingCalculator;
import com.opengamma.analytics.financial.provider.description.interestrate.MulticurveProviderInterface;
import com.opengamma.analytics.math.interpolation.CombinedInterpolatorExtrapolator;
import com.opengamma.analytics.math.interpolation.CombinedInterpolatorExtrapolatorFactory;
import com.opengamma.analytics.math.interpolation.GridInterpolator2D;
import com.opengamma.analytics.math.interpolation.Interpolator1DFactory;
import com.opengamma.analytics.math.matrix.DoubleMatrix1D;
import com.opengamma.analytics.math.surface.InterpolatedDoublesSurface;
import com.opengamma.util.money.MultipleCurrencyAmount;
import com.opengamma.util.tuple.DoublesPair;

/**
 * 
 */
public class CapFloorInArrearsCalculation {

  private static final ParRateDiscountingCalculator PRC = ParRateDiscountingCalculator.getInstance();

  private final CapletStrippingResult _capletStrippingResult;
  private final MulticurveProviderInterface _curves;
  private final InterpolatedDoublesSurface _surface;

  public CapFloorInArrearsCalculation(CapletStripper stripper, List<CapFloor> caps, double[] mrkPrices, MarketDataType type, double[] errors, DoubleMatrix1D guess, MulticurveProviderInterface curves) {

    // perform caplet stripping
    long t0 = System.nanoTime();
    _capletStrippingResult = stripper.solve(mrkPrices, type, errors, guess);
    long t1 = System.nanoTime();
    System.out.println("Chi2: " + _capletStrippingResult.getChiSqr());
    System.out.println("Time for stripping :" + (t1 - t0) * 1e-9 + "s");
    _curves = curves;

    // The stripper works with discrete caplets. Need to construct a continuous surface in order to sample at any expiry-strike
    // For strippers that work with a volatility surface (e.g. the smile based strippers), we have thrown away information
    // which would be useful here
    DoublesPair[] expStrikes = _capletStrippingResult.getPricer().getExpiryStrikeArray();
    DoubleMatrix1D vols = _capletStrippingResult.getCapletVols();
    CombinedInterpolatorExtrapolator interpolator = CombinedInterpolatorExtrapolatorFactory.getInterpolator(Interpolator1DFactory.LINEAR, Interpolator1DFactory.LINEAR_EXTRAPOLATOR);
    GridInterpolator2D interpolator2D = new GridInterpolator2D(interpolator, interpolator);

    _surface = new InterpolatedDoublesSurface(expStrikes, vols.getData(), interpolator2D);

  }

  public double simpleOptionPrice(SimpleOptionData option) {
    return BlackFormulaRepository.price(option, _surface.getZValue(option.getTimeToExpiry(), option.getStrike()));
  }

  /**
   * 
   * @param caplet The caplet being priced
   * @param mu the extrapolation control parameter
   * @return multi-currency amount
   */
  public MultipleCurrencyAmount presentValue(CapFloorIbor caplet, final GeneralSmileInterpolator interpolator) {

    double forward = caplet.accept(PRC, _curves);
    MultiCapFloorPricer pricer = _capletStrippingResult.getPricer();

    double[] sampleStrikes = pricer.getStrikes();
    double expiry = caplet.getFixingTime();

    int nStrikes = sampleStrikes.length;
    double[] sampleVols = new double[nStrikes];
    for (int i = 0; i < nStrikes; i++) {
      sampleVols[i] = _surface.getZValue(expiry, sampleStrikes[i]);
    }

    // System.out.println("Time to extract vols:" + (t1 - t0) * 1e-9 + "s");

    // construct a interpolated/extrapolated smile
    InterpolatedSmileFunction smileFunction = new InterpolatedSmileFunction(interpolator, forward, sampleStrikes, expiry, sampleVols);
    CapFloorIborInArrearsSmileModelCapGenericReplicationMethod inArrearsCal = new CapFloorIborInArrearsSmileModelCapGenericReplicationMethod(smileFunction);

    MultipleCurrencyAmount pv = inArrearsCal.presentValue(caplet, _curves);

    return pv;
  }
}
