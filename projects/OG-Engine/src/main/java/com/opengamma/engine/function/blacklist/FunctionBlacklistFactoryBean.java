/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.function.blacklist;

import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.SingletonFactoryBean;

/**
 * Bean for obtaining {@link FunctionBlacklist} instances from a {@link FunctionBlacklistProvider}.
 */
public class FunctionBlacklistFactoryBean extends SingletonFactoryBean<FunctionBlacklist> {

  private String _identifier;
  private FunctionBlacklistProvider _provider;

  public String getIdentifier() {
    return _identifier;
  }

  public void setIdentifier(final String identifier) {
    _identifier = identifier;
  }

  public FunctionBlacklistProvider getProvider() {
    return _provider;
  }

  public void setProvider(final FunctionBlacklistProvider provider) {
    _provider = provider;
  }

  @Override
  protected FunctionBlacklist createObject() {
    ArgumentChecker.notNullInjected(getIdentifier(), "identifier");
    ArgumentChecker.notNullInjected(getProvider(), "provider");
    return getProvider().getBlacklist(getIdentifier());
  }

}
