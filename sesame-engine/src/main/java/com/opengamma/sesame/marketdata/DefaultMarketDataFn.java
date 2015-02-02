/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.sesame.marketdata;

import com.opengamma.core.security.Security;
import com.opengamma.financial.analytics.ircurve.strips.CurveNodeWithIdentifier;
import com.opengamma.financial.analytics.ircurve.strips.PointsCurveNodeWithIdentifier;
import com.opengamma.financial.currency.CurrencyPair;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.sesame.Environment;
import com.opengamma.util.result.Result;

/**
 * Function that return market data values.
 */
public class DefaultMarketDataFn implements MarketDataFn {

  @Override
  public Result<Double> getCurveNodeValue(Environment env, CurveNodeWithIdentifier node) {
    FieldName fieldName = FieldName.of(node.getDataField());
    ExternalIdBundle id = node.getIdentifier().toBundle();
    // this should use a link but that requires changes to the curve configuration classes.
    // CurveNodeWithIdentifier should refer to its data using a link (probably a SecurityLink) instead of an ID
    return env.getMarketDataBundle().get(RawId.of(id, Double.class, fieldName), Double.class);
  }

  @Override
  public Result<Double> getCurveNodeUnderlyingValue(Environment env, PointsCurveNodeWithIdentifier node) {
    ExternalIdBundle id = node.getUnderlyingIdentifier().toBundle();
    FieldName fieldName = FieldName.of(node.getUnderlyingDataField());
    return env.getMarketDataBundle().get(RawId.of(id, fieldName), Double.class);
  }

  @Override
  public Result<Double> getMarketValue(Environment env, Security security) {
    MarketDataId key = SecurityId.of(security);
    return env.getMarketDataBundle().get(key, Double.class);
  }

  @Override
  public <T> Result<T> getValue(Environment env, Security security, FieldName fieldName, Class<T> valueType) {
    MarketDataId key = SecurityId.of(security, valueType, fieldName);
    return env.getMarketDataBundle().get(key, valueType);
  }

  @Override
  public Result<Double> getFxRate(final Environment env, CurrencyPair currencyPair) {
    FxRateId rateId = FxRateId.of(currencyPair);
    return env.getMarketDataBundle().get(rateId, Double.class);
  }
}
