/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.sesame;

import java.util.Set;

import com.opengamma.analytics.financial.forex.method.FXMatrix;
import com.opengamma.core.position.Trade;
import com.opengamma.financial.security.FinancialSecurity;
import com.opengamma.sesame.marketdata.IssuerMulticurveId;
import com.opengamma.sesame.marketdata.MarketDataEnvironment;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.result.FailureStatus;
import com.opengamma.util.result.Result;

/**
 * Provides issuer multicurve bundles by looking them up in the {@link MarketDataEnvironment}.
 */
public class LookupIssuerProviderFn implements IssuerProviderFn {

  /** Specifies which curve should be used for a trade. */
  private final CurveSelectorFn _curveSelectorFn;

  /**
   * @param curveSelectorFn specifies which curve should be used for a trade
   */
  public LookupIssuerProviderFn(CurveSelectorFn curveSelectorFn) {
    _curveSelectorFn = ArgumentChecker.notNull(curveSelectorFn, "curveSelectorFn");
  }

  @Override
  public Result<IssuerProviderBundle> createBundle(Environment env, FinancialSecurity security, FXMatrix fxMatrix) {
    throw new UnsupportedOperationException("createBundle not implemented");
  }

  @Override
  public Result<IssuerProviderBundle> createBundle(Environment env, Trade trade, FXMatrix fxMatrix) {
    return getMulticurveBundle(env, trade);
  }

  @Override
  public Result<IssuerProviderBundle> getMulticurveBundle(Environment env, Trade trade) {
    Set<String> multicurveNames = _curveSelectorFn.getMulticurveNames(trade);

    if (multicurveNames.isEmpty()) {
      return Result.failure(FailureStatus.CALCULATION_FAILED, "No curves configured for trade {}", trade);
    } else if (multicurveNames.size() > 1) {
      // TODO confirm the status of merging issuer bundles
      return Result.failure(FailureStatus.CALCULATION_FAILED, "Only one issuer curve bundle is supported per trade. " +
          "Bundle names: {}, trade: {}", multicurveNames, trade);
    } else {
      String multicurveName = multicurveNames.iterator().next();
      return env.getMarketDataBundle().get(IssuerMulticurveId.of(multicurveName), IssuerProviderBundle.class);
    }
  }
}
