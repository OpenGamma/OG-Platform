/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.view;

import com.opengamma.engine.function.FunctionCompilationContext;

/**
 * Callback object to allow Spring based injection to deep state in the ViewProcessor (e.g. the CompilationContext)
 */
public abstract class ViewProcessorConfigBean {
  
  protected void visitCompilationContext(FunctionCompilationContext context) {
  }
  
}
