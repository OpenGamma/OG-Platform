/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.integration.marketdata.manipulator.dsl;

import com.opengamma.engine.marketdata.manipulator.MarketDataSelector;

/**
 * Base class for selectors. This isn't an interface because the methods need to be package scoped so they're
 * not on the public API and don't interfere with the DSL.
 */
/* package */ abstract class Selector {

  /* package */ abstract MarketDataSelector getMarketDataSelector();
}
