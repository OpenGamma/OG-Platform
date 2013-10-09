/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.view.worker;

import java.util.Collection;

import com.opengamma.engine.marketdata.MarketDataListener;
import com.opengamma.engine.value.ValueSpecification;

/**
 * Simple interface for the clients interested only in market data value changes,
 * as opposed to all the subscription events exposed by {@link MarketDataListener}.
 */
public interface MarketDataChangeListener {

  /**
   * Callback to be invoked when market data items have changed values.
   *
   * @param valueSpecifications the value specifcations whose values have changed, which
   * will include new subscriptions
   */
  void onMarketDataValuesChanged(Collection<ValueSpecification> valueSpecifications);
}
