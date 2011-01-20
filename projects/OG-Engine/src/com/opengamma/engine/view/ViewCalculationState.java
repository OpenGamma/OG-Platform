/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.view;

// REVIEW kirk 2010-05-21 -- This is currently package protected as it's only used
// to control the View itself. However, it's not an inner enum as we might want to use
// it for other things that are expensive to do lifecycle on as a general
// enum, and it might want to be exposed to clients.
// I'm keeping it in limbo for now to avoid documenting it, and we'll
// see how it goes.

/**
 * Specifies the current state of a {@link View}.
 */
/*package*/ enum ViewCalculationState {
  /**
   * The view has not been initialized and cannot be used other than to access underlying data structures such as the
   * view definition. 
   */
  NOT_INITIALIZED,
  /**
   * The view is initialized but live computations are not running because no clients require them. Clients can ask for
   * one-off results.
   */
  STOPPED,
  /**
   * Live computations are running.
   */
  RUNNING,
  /**
   * A terminated view has no clients, is not running live computations, and cannot create new clients. It is therefore
   * useless and suitable only for discarding. It could be in this state, for example, because the parent view
   * processor has been shut down.
   */
  TERMINATED
}
