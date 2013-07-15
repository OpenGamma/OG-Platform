/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.function;

import com.opengamma.util.PublicAPI;

/**
 * Marker interface for {@link FunctionCompilationContext} members that should be notified of the context after it has been fully configured. Any context members implementing this interface will be
 * notified by the context when its {@link FunctionCompilationContext#init} method is called.
 */
@PublicAPI
public interface FunctionCompilationContextAware {

  /**
   * Notifies the instance of its owning execution context. An object implementing this interface should not be added to multiple execution contexts or this method may be called multiple times.
   *
   * @param context the owning execution context, not null
   */
  void setFunctionCompilationContext(FunctionCompilationContext context);

}
