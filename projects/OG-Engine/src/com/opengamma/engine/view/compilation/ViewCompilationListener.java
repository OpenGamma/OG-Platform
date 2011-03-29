/**
 * Copyright (C) 2009 - 2011 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.view.compilation;

import com.opengamma.util.PublicAPI;

/**
 * Provides a callback for receiving view compilation results. A single compilation result may be valid for many
 * computation cycles.
 */
@PublicAPI
public interface ViewCompilationListener {

  /**
   * Called to indicate that the view has been compiled.
   * <p>
   * Implementers must be especially careful with resource management, as the compilation results could have large
   * memory requirements. 
   *
   * @param evaluationModel  the compilation result, not null
   */
  void viewCompiled(ViewEvaluationModel evaluationModel);
  
}
