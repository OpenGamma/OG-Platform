/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

package com.opengamma.language.livedata;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import com.opengamma.language.context.ContextInitializationBean;
import com.opengamma.language.context.MutableGlobalContext;
import com.opengamma.language.context.MutableSessionContext;
import com.opengamma.language.context.MutableUserContext;
import com.opengamma.util.ArgumentChecker;

/**
 * Extends a context factory to attach a live data provider.
 */
public class Loader extends ContextInitializationBean {

  private List<LiveDataProvider> _liveDataProviders;

  public void setLiveDataProvider(final LiveDataProvider liveDataProvider) {
    ArgumentChecker.notNull(liveDataProvider, "liveDataProvider");
    _liveDataProviders = Collections.singletonList(liveDataProvider);
  }

  public LiveDataProvider getLiveDataProvider() {
    if ((_liveDataProviders == null) || _liveDataProviders.isEmpty()) {
      return null;
    } else {
      return _liveDataProviders.get(0);
    }
  }

  public void setLiveDataProviders(final Collection<LiveDataProvider> liveDataProviders) {
    ArgumentChecker.noNulls(liveDataProviders, "liveDataProviders");
    ArgumentChecker.isFalse(liveDataProviders.isEmpty(), "liveDataProviders");
    _liveDataProviders = new ArrayList<LiveDataProvider>(liveDataProviders);
  }

  public Collection<LiveDataProvider> getLiveDataProviders() {
    return _liveDataProviders;
  }

  protected void addProviders(final AggregatingLiveDataProvider aggregator) {
    for (LiveDataProvider provider : getLiveDataProviders()) {
      aggregator.addProvider(provider);
    }
  }

  // ContextInitializationBean

  @Override
  protected void assertPropertiesSet() {
    ArgumentChecker.notNull(getLiveDataProviders(), "liveDataProviders");
  }

  @Override
  protected void initContext(final MutableSessionContext sessionContext) {
    addProviders(sessionContext.getLiveDataProvider());
  }

  @Override
  protected void initContext(final MutableUserContext userContext) {
    addProviders(userContext.getLiveDataProvider());
  }

  @Override
  protected void initContext(final MutableGlobalContext globalContext) {
    addProviders(globalContext.getLiveDataProvider());
  }

}
