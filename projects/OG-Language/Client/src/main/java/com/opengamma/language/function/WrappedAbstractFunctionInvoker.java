/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

package com.opengamma.language.function;

import com.opengamma.language.context.SessionContext;
import com.opengamma.util.async.AsynchronousExecution;

/**
 * Form of {@link AbstractFunctionInvoker} that allows another instance to be wrapped.
 */
public abstract class WrappedAbstractFunctionInvoker extends AbstractFunctionInvoker {

  private final AbstractFunctionInvoker _wrapped;

  protected WrappedAbstractFunctionInvoker(final AbstractFunctionInvoker wrapped) {
    super(wrapped.getParameters());
    _wrapped = wrapped;
  }

  protected AbstractFunctionInvoker getWrapped() {
    return _wrapped;
  }

  @Override
  protected Object invokeImpl(final SessionContext sessionContext, final Object[] parameters) throws AsynchronousExecution {
    return getWrapped().invokeImpl(sessionContext, parameters);
  }

}
