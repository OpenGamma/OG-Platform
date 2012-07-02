/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.model.volatility.local;

import com.opengamma.analytics.financial.model.volatility.surface.VolatilitySurface;
import com.opengamma.analytics.math.surface.Surface;

/**
 * Under the assumption of cash and proportional discrete dividends, the stock price $S_t$ can be modelled as $S_t = (F_t-D_t)x_t + D_t$ where $F_t$ is the forward, $D_t$
 * is the growth-rate discounted value of future cash dividends, and $X_t$ is a positive martingale with $X_0 = 1$, known as the pure stock process. If the SDE for the pure 
 * stock process is $\frac{dX_t}{X_t} = \sigma^X(t,X_t)dW_t$ then $\sigma^X(t,X_t)$ is the pure local volatility, and can be found by applying the Dupire formula to call options
 * on the the pure stock. See white (2012), Equity Variance Swap with Dividends, for details. 
 */
public class PureLocalVolatilitySurface extends VolatilitySurface {

  /**
   * @param surface A pure local volatility surface
   */
  public PureLocalVolatilitySurface(Surface<Double, Double, Double> surface) {
    super(surface);
  }

}
