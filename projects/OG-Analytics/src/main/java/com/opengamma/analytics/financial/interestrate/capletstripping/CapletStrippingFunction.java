/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate.capletstripping;

import java.util.ArrayList;
import java.util.List;

import com.opengamma.analytics.financial.interestrate.capletstrippingnew.CapFloor;
import com.opengamma.analytics.financial.interestrate.capletstrippingnew.CapFloorPricer;
import com.opengamma.analytics.financial.model.volatility.VolatilityModel1D;
import com.opengamma.analytics.financial.model.volatility.VolatilityModelProvider;
import com.opengamma.analytics.financial.provider.description.interestrate.MulticurveProviderInterface;
import com.opengamma.analytics.math.function.Function1D;
import com.opengamma.analytics.math.matrix.DoubleMatrix1D;
import com.opengamma.util.ArgumentChecker;

/**
 * This takes a set of (abstract) nodes and uses the supplied volatility-model-provider to produced a (caplet) volatility surface, which is
 * then used to price a set of caps. So it is a function from a vector (the nodes) to another vector (the cap implied volatilities), and should
 * be used in a fixing routine.
 */
public class CapletStrippingFunction extends Function1D<DoubleMatrix1D, DoubleMatrix1D> {

  private final List<CapFloorPricer> _capPricers;
  private final VolatilityModelProvider _volModelProvider;

  // private final int _totalNodes;

  public CapletStrippingFunction(final List<CapFloor> caps, final MulticurveProviderInterface curves, final VolatilityModelProvider volModelProvider) {

    ArgumentChecker.noNulls(caps, "caps null");
    ArgumentChecker.notNull(curves, "null curves");
    ArgumentChecker.notNull(volModelProvider, "null vol Model provider");

    _volModelProvider = volModelProvider;
    _capPricers = new ArrayList<>(caps.size());
    for (final CapFloor cap : caps) {
      _capPricers.add(new CapFloorPricer(cap, curves));
    }
  }

  @Override
  public DoubleMatrix1D evaluate(final DoubleMatrix1D x) {

    final VolatilityModel1D volModel = _volModelProvider.evaluate(x);

    final double[] res = new double[_capPricers.size()];
    for (int i = 0; i < _capPricers.size(); i++) {
      res[i] = _capPricers.get(i).impliedVol(volModel);
    }

    return new DoubleMatrix1D(res);
  }

}
