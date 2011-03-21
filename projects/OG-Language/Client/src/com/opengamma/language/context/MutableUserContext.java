/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.language.context;

import com.opengamma.language.function.AggregatingFunctionProvider;
import com.opengamma.language.livedata.AggregatingLiveDataProvider;
import com.opengamma.language.procedure.AggregatingProcedureProvider;

/**
 * A mutable version of {@link UserContext}. 
 */
public class MutableUserContext extends UserContext {

  private final UserContextEventHandler _eventHandler;

  /* package */MutableUserContext(final GlobalContext globalContext, final String userName,
      final UserContextEventHandler eventHandler) {
    super(globalContext, userName);
    _eventHandler = eventHandler;
    eventHandler.initContext(this);
  }

  private UserContextEventHandler getEventHandler() {
    return _eventHandler;
  }

  // Context initialization

  @Override
  protected void doneContext() {
    getEventHandler().doneContext(this);
  }

  // Definition providers

  @Override
  public AggregatingFunctionProvider getFunctionProvider() {
    return getFunctionProviderImpl();
  }

  @Override
  public AggregatingLiveDataProvider getLiveDataProvider() {
    return getLiveDataProviderImpl();
  }

  @Override
  public AggregatingProcedureProvider getProcedureProvider() {
    return getProcedureProviderImpl();
  }

  // Arbitrary values

  @Override
  public void setValue(final String key, final Object value) {
    super.setValue(key, value);
  }

}
