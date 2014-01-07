/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.volatility.surface;

import org.threeten.bp.LocalDate;

import com.opengamma.financial.convention.expirycalc.ExchangeTradedInstrumentExpiryCalculator;
import com.opengamma.id.ExternalId;

/**
 * Provides instrument tickers, {@link ExternalId}'s, for each point on the curve.
 * It is likely that the implementing classes will be specific to the data provider.
 * @param <X> The type of the x-axis values
 */
public interface FuturePriceCurveInstrumentProvider<X> {

  ExternalId getInstrument(X xAxis); // TODO: Remove this. It will never be used by any implementing class

  ExternalId getInstrument(X xAxis, LocalDate curveDate);

  String getDataFieldName();
  
  String getTickerScheme();
  
  ExchangeTradedInstrumentExpiryCalculator getExpiryRuleCalculator();
}
