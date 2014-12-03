/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.sesame;

import java.math.BigDecimal;
import java.util.Set;

import org.threeten.bp.LocalDate;
import org.threeten.bp.OffsetTime;

import com.google.common.collect.Iterables;
import com.opengamma.analytics.financial.forex.method.FXMatrix;
import com.opengamma.core.position.Counterparty;
import com.opengamma.core.position.Trade;
import com.opengamma.core.position.impl.SimpleCounterparty;
import com.opengamma.core.position.impl.SimpleTrade;
import com.opengamma.financial.analytics.curve.CurveConstructionConfiguration;
import com.opengamma.financial.security.FinancialSecurity;
import com.opengamma.id.ExternalId;
import com.opengamma.util.result.FailureStatus;
import com.opengamma.util.result.Result;

/**
 * Default implementation of IssuerProviderFn that returns a multicurve bundle of curves by issuer.
 */
public class ExposureFunctionsIssuerProviderFn implements IssuerProviderFn {
  
  private final MarketExposureSelector _marketExposureSelector;
  private final IssuerProviderBundleFn _issuerProviderBundleFn;
  
  public ExposureFunctionsIssuerProviderFn(MarketExposureSelector marketExposureSelector,
                                           IssuerProviderBundleFn issuerProviderBundleFn) {
    _marketExposureSelector = marketExposureSelector;
    _issuerProviderBundleFn = issuerProviderBundleFn;
  }
  @Override
  public Result<IssuerProviderBundle> createBundle(Environment env, FinancialSecurity security, FXMatrix fxMatrix) {
    Trade tradeWrapper = new SimpleTrade(security,
                                         BigDecimal.ONE,
                                         new SimpleCounterparty(ExternalId.of(Counterparty.DEFAULT_SCHEME, "CPARTY")),
                                         LocalDate.now(),
                                         OffsetTime.now());
    return createBundle(env, tradeWrapper, fxMatrix);
  }

  
  @Override
  public Result<IssuerProviderBundle> createBundle(Environment env, Trade trade, FXMatrix fxMatrix) {
    return getMulticurveBundle(env, trade);
  }

  @Override
  public Result<IssuerProviderBundle> getMulticurveBundle(Environment env, Trade trade) {
    Set<CurveConstructionConfiguration> curveConfigs = _marketExposureSelector.determineCurveConfigurations(trade);

    if (curveConfigs.size() == 1) {
      Result<IssuerProviderBundle> bundle =
          _issuerProviderBundleFn.generateBundle(env, Iterables.getOnlyElement(curveConfigs));
      if (bundle.isSuccess()) {
        return Result.success(bundle.getValue());
      } else {
        return Result.failure(bundle);
      }
    } else if (curveConfigs.isEmpty()) {
      return Result.failure(FailureStatus.MISSING_DATA, "No curve construction configs found for {}", trade);
    } else {
      return Result.failure(FailureStatus.MULTIPLE, "Found {} configs, expected one", curveConfigs.size());
    }
  }
}
