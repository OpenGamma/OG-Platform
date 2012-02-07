/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.equity.variance;

import org.apache.commons.lang.NotImplementedException;

import com.opengamma.financial.equity.variance.pricing.VarianceSwapStaticReplication2;
import com.opengamma.financial.equity.variance.pricing.VarianceSwapStaticReplicationDelta;
import com.opengamma.financial.equity.variance.pricing.VarianceSwapStaticReplicationStrike;
import com.opengamma.financial.model.volatility.surface.BlackVolatilitySurface;
import com.opengamma.financial.model.volatility.surface.BlackVolatilitySurfaceDelta;
import com.opengamma.financial.model.volatility.surface.BlackVolatilitySurfaceStrike;

/**
 * 
 */
public class VarianceSwapStaticReplicationFactory {

  //TODO replace this with a vistor pattern
  public VarianceSwapStaticReplication2<?> make(VarianceSwapDataBundle2<?> data)  {
    BlackVolatilitySurface<?> volSurface = data.getVolatilitySurface();
    if (volSurface instanceof BlackVolatilitySurfaceStrike) {
      return new VarianceSwapStaticReplicationStrike();
    } else if (volSurface instanceof BlackVolatilitySurfaceDelta) {
      return new VarianceSwapStaticReplicationDelta();
    } else {
      throw new  NotImplementedException(volSurface.getClass().toString());
    }
  }

}
