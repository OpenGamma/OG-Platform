/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.fxforwardcurve;

import com.opengamma.core.value.MarketDataRequirementNames;
import com.opengamma.engine.value.ValueRequirementNames;
import com.opengamma.financial.analytics.ircurve.CurveInstrumentProvider;
import com.opengamma.id.ExternalId;

/**
 * Interface for instrument providers that get FX forward curve market data
 * tickers.
 */
public interface FXForwardCurveInstrumentProvider extends CurveInstrumentProvider {

  /**
   * If true, uses the spot ticker defined in this provider to get the spot market
   * data. Otherwise, gets spot rate information from the dependency graph using
   * {@link ValueRequirementNames#SPOT_RATE}
   * @return True if the spot ticker is to be used to get the spot rate
   */
  boolean useSpotRateFromGraph();

  /**
   * The data field name for the spot ticker (e.g. {@link MarketDataRequirementNames#MARKET_VALUE}.
   * @return The spot ticker data field name
   */
  String getDataFieldName();

  /**
   * The external id of the spot ticker.
   * @return The spot ticker external id
   */
  ExternalId getSpotInstrument();

}
