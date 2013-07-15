/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.volatility.local;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.engine.target.ComputationTargetType;
import com.opengamma.financial.analytics.model.InstrumentTypeProperties;
import com.opengamma.financial.analytics.model.volatility.surface.black.BlackVolatilitySurfacePropertyNamesAndValues;

/**
 *
 */
public abstract class ForexDupireLocalVolatilitySurfaceFunction extends DupireLocalVolatilitySurfaceFunction {
  private static final Logger s_logger = LoggerFactory.getLogger(ForexDupireLocalVolatilitySurfaceFunction.class);

  @Override
  public ComputationTargetType getTargetType() {
    return ComputationTargetType.UNORDERED_CURRENCY_PAIR;
  }

  @Override
  protected String getInstrumentType() {
    return InstrumentTypeProperties.FOREX;
  }

  /**
   * Function producing a local volatility surface using a Black volatility surface with spline interpolation
   */
  public static class Spline extends ForexDupireLocalVolatilitySurfaceFunction {

    @Override
    protected String getBlackSmileInterpolatorName() {
      return BlackVolatilitySurfacePropertyNamesAndValues.SPLINE;
    }

  }

  /**
   * Function producing a local volatility surface using a Black volatility surface with SABR interpolation
   */
  public static class SABR extends ForexDupireLocalVolatilitySurfaceFunction {

    @Override
    protected String getBlackSmileInterpolatorName() {
      return BlackVolatilitySurfacePropertyNamesAndValues.SABR;
    }

  }

  /**
   * Function producing a local volatility surface using a Black volatility surface with mixed log-normal interpolation
   */
  public static class MixedLogNormal extends ForexDupireLocalVolatilitySurfaceFunction {

    @Override
    protected String getBlackSmileInterpolatorName() {
      return BlackVolatilitySurfacePropertyNamesAndValues.MIXED_LOG_NORMAL;
    }

  }
}
