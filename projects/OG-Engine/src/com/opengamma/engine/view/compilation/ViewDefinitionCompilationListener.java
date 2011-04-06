/**
 * Copyright (C) 2009 - 2011 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.view.compilation;

import com.opengamma.util.PublicAPI;

/**
 * Provides a callback for receiving compiled view definitions. A single compiled view may be valid for many
 * computation cycles.
 */
@PublicAPI
public interface ViewDefinitionCompilationListener {

  /**
   * Called to indicate that the view definition has been compiled.
   * <p>
   * Implementers must be especially careful with resource management, as the compilation results could have large
   * memory requirements.
   *
   * @param compiledViewDefinition  the compilation result, not null
   */
  void viewDefinitionCompiled(CompiledViewDefinition compiledViewDefinition);
  
  /**
   * Called to indicate that compilation of the view definition failed.
   * 
   * @param details  a description of the compilation failure, possibly null
   * @param exception  any exception associated with the compilation failure, possibly null
   */
  void compilationFailed(String details, Exception exception);
  
}
