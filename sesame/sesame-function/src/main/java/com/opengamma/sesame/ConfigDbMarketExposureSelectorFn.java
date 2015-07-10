/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.sesame;

import com.opengamma.core.security.SecuritySource;
import com.opengamma.financial.analytics.curve.exposure.ExposureFunctions;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.result.Result;

/**
 * Function implementation that provides a market exposure selector.
 * @deprecated this serves no useful purpose, the engine can create {@link MarketExposureSelector} directly.
 */
@Deprecated
public class ConfigDbMarketExposureSelectorFn implements MarketExposureSelectorFn {

  /**
   * The exposure config.
   */
  private final ExposureFunctions _exposures;
  /**
   * The underlying security source.
   */
  private final SecuritySource _securitySource;

  public ConfigDbMarketExposureSelectorFn(ExposureFunctions exposureConfig, SecuritySource securitySource) {
    _exposures = ArgumentChecker.notNull(exposureConfig, "exposureConfig");
    _securitySource = ArgumentChecker.notNull(securitySource, "securitySource");
  }

  //-------------------------------------------------------------------------
  @Override
  public Result<MarketExposureSelector> getMarketExposureSelector() {
    return Result.success(new MarketExposureSelector(_exposures, _securitySource));
  }

}
