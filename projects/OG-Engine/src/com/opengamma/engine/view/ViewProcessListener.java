/**
 * Copyright (C) 2009 - 2011 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.view;

import com.opengamma.engine.view.compilation.CompiledViewDefinitionImpl;

/**
 * 
 */
public interface ViewProcessListener {

  boolean isDeltaResultRequired();
  
  void viewDefinitionCompiled(CompiledViewDefinitionImpl compiledViewDefinition);
  
  void result(ViewComputationResultModel fullResult, ViewDeltaResultModel deltaResult);
  
  void processCompleted();
  
  void shutdown();
  
}
