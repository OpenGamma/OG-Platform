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
  private final CurveSelector _curveSelector;

  /**
   * @param curveSelector specifies which curve should be used for a trade
   */
  public LookupIssuerProviderFn(CurveSelector curveSelector) {
    _curveSelector = ArgumentChecker.notNull(curveSelector, "curveSelectorFn");
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
    Set<String> multicurveNames = _curveSelector.getMulticurveNames(trade);

    switch (multicurveNames.size()) {
      case 0:
        return Result.failure(FailureStatus.CALCULATION_FAILED, "No curves configured for trade {}", trade);
      case 1:
        String multicurveName = multicurveNames.iterator().next();
        IssuerMulticurveId multicurveId = IssuerMulticurveId.of(multicurveName);
        return env.getMarketDataBundle().get(multicurveId, IssuerProviderBundle.class);
      default:
        return Result.failure(FailureStatus.CALCULATION_FAILED,
                              "Only one issuer curve bundle is supported per trade. Bundle names: {}, trade: {}",
                              multicurveNames, trade);
    }
  }
}
