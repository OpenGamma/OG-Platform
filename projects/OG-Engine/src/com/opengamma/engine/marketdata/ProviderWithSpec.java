/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.marketdata;

import com.opengamma.engine.marketdata.spec.MarketDataSpecification;
import com.opengamma.util.ArgumentChecker;

/**
 * @deprecated probably not needed any more
 */
/* package */ class ProviderWithSpec {

  private final MarketDataProvider _provider;
  private final MarketDataSpecification _specification;

  /* package */ ProviderWithSpec(MarketDataProvider provider, MarketDataSpecification specification) {
    ArgumentChecker.notNull(provider, "provider");
    ArgumentChecker.notNull(specification, "specification");
    _provider = provider;
    _specification = specification;
  }

  /* package */ MarketDataProvider getProvider() {
    return _provider;
  }

  /* package */ MarketDataSpecification getSpecification() {
    return _specification;
  }
}
