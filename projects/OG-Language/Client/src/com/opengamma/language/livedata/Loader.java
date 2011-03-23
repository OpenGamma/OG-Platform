/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

package com.opengamma.language.livedata;

import com.opengamma.language.context.ContextInitializationBean;
import com.opengamma.language.context.MutableGlobalContext;
import com.opengamma.language.context.MutableSessionContext;
import com.opengamma.language.context.MutableUserContext;
import com.opengamma.util.ArgumentChecker;

/**
 * Extends a context factory to attach a live data provider.
 */
public class Loader extends ContextInitializationBean {

  private LiveDataProvider _liveData;

  public void setLiveData(final LiveDataProvider liveData) {
    ArgumentChecker.notNull(liveData, "liveData");
    _liveData = liveData;
  }

  public LiveDataProvider getLiveData() {
    return _liveData;
  }

  // ContextInitializationBean

  @Override
  protected void assertPropertiesSet() {
    ArgumentChecker.notNull(getLiveData(), "liveData");
  }

  @Override
  protected void initContext(final MutableSessionContext sessionContext) {
    sessionContext.getLiveDataProvider().addProvider(getLiveData());
  }

  @Override
  protected void initContext(final MutableUserContext userContext) {
    userContext.getLiveDataProvider().addProvider(getLiveData());
  }

  @Override
  protected void initContext(final MutableGlobalContext globalContext) {
    globalContext.getLiveDataProvider().addProvider(getLiveData());
  }

}
